package com.fruitmkt.service;

import com.fruitmkt.dao.OrderDAO;
import com.fruitmkt.dao.CartDAO;
import com.fruitmkt.dao.ProductDAO;
import com.fruitmkt.dao.ProductVariantDAO;
import com.fruitmkt.dao.base.BaseDAO;
import com.fruitmkt.model.dto.CheckoutDTO;
import com.fruitmkt.model.dto.PagedResultDTO;
import com.fruitmkt.model.entity.Order;
import com.fruitmkt.model.entity.OrderItem;
import com.fruitmkt.model.entity.Product;
import com.fruitmkt.model.entity.ProductVariant;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * OrderService — Tầng business logic cho nghiệp vụ tương ứng.
 *
 * QUY TẮC:
 *   - Chỉ gọi DAO, không viết SQL ở đây
 *   - Chứa tất cả validation và business rule
 *   - Ném RuntimeException hoặc custom exception cho Servlet xử lý
 *   - Không tương tác trực tiếp với HttpRequest/Response
 *
 * @author fruitmkt-team
 */
public class OrderService {

    private final OrderDAO orderDAO = new OrderDAO();
    private final CartDAO cartDAO = new CartDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final ProductVariantDAO productVariantDAO = new ProductVariantDAO();

    /**
     * Đặt hàng có giao dịch và khóa dòng (II.11 - II.13, II.8, II.10).
     */
    public Order placeOrder(int customerId, CheckoutDTO dto) throws SQLException {
        if (dto.getCartItemIds() == null || dto.getCartItemIds().isEmpty()) {
            throw new IllegalArgumentException("Không có sản phẩm nào được chọn để thanh toán.");
        }
        if (dto.getDeliveryAddress() == null || dto.getDeliveryAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("Địa chỉ giao hàng không được trống.");
        }

        List<com.fruitmkt.model.entity.Cart> carts = cartDAO.findByCustomer(customerId);
        if (carts.isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng của bạn trống.");
        }
        int cartId = carts.get(0).getCartId();
        List<com.fruitmkt.model.entity.CartItem> allItems = cartDAO.findItems(cartId);

        List<com.fruitmkt.model.entity.CartItem> itemsToOrder = new ArrayList<>();
        for (com.fruitmkt.model.entity.CartItem ci : allItems) {
            if (dto.getCartItemIds().contains(ci.getCartItemId())) {
                itemsToOrder.add(ci);
            }
        }

        if (itemsToOrder.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn các sản phẩm hợp lệ trong giỏ hàng.");
        }

        // 1. Validate shop consistency (One order per shop owner)
        Integer shopOwnerId = null;
        for (com.fruitmkt.model.entity.CartItem ci : itemsToOrder) {
            ProductVariant pv = productVariantDAO.findById(ci.getVariantId());
            if (pv == null) throw new IllegalArgumentException("Sản phẩm biến thể không hợp lệ.");
            Product p = productDAO.findById(pv.getProductId()).get(0);
            if (shopOwnerId == null) {
                shopOwnerId = p.getOwnerId();
            } else if (shopOwnerId != p.getOwnerId()) {
                throw new IllegalArgumentException("Một đơn hàng chỉ được đặt từ một chủ cửa hàng duy nhất. Vui lòng thanh toán riêng từng shop.");
            }
        }

        // 2. Begin Transaction
        Connection conn = BaseDAO.getConnection();
        conn.setAutoCommit(false);

        try {
            BigDecimal totalAmount = BigDecimal.ZERO;
            List<OrderItem> orderItems = new ArrayList<>();

            int currentMonth = LocalDate.now().getMonthValue();

            // Lock and deduct stock for each variant
            for (com.fruitmkt.model.entity.CartItem ci : itemsToOrder) {
                ProductVariant pv = productVariantDAO.findById(ci.getVariantId());
                Product p = productDAO.findById(pv.getProductId()).get(0);

                // II.10: Seasonal check
                if (p.getSeasonStart() != null && p.getSeasonEnd() != null) {
                    int start = p.getSeasonStart();
                    int end = p.getSeasonEnd();
                    boolean inSeason;
                    if (start <= end) {
                        inSeason = (currentMonth >= start && currentMonth <= end);
                    } else {
                        // Wraparound support (e.g. Nov to Feb: 11, 12, 1, 2)
                        inSeason = (currentMonth >= start || currentMonth <= end);
                    }
                    if (!inSeason) {
                        throw new IllegalArgumentException("Sản phẩm " + p.getName() + " hiện tại đã hết mùa thu hoạch!");
                    }
                }

                // Row-level lock stock
                int currentStock = 0;
                String lockSql = "SELECT stock_quantity FROM product_variants WITH (UPDLOCK) WHERE variant_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(lockSql)) {
                    ps.setInt(1, ci.getVariantId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            currentStock = rs.getInt("stock_quantity");
                        }
                    }
                }

                if (currentStock < ci.getQuantity()) {
                    throw new IllegalArgumentException("Sản phẩm '" + p.getName() + " - " + pv.getVariantLabel() + "' đã hết hàng hoặc không đủ số lượng tồn kho (Còn lại: " + currentStock + ").");
                }

                // Deduct stock
                String deductSql = "UPDATE product_variants SET stock_quantity = stock_quantity - ?, updated_at = GETDATE() WHERE variant_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(deductSql)) {
                    ps.setInt(1, ci.getQuantity());
                    ps.setInt(2, ci.getVariantId());
                    ps.executeUpdate();
                }

                int quantityAfter = currentStock - ci.getQuantity();

                // Log inventory log inside transaction
                String logSql = "INSERT INTO inventory_logs (variant_id, changed_by, change_type, quantity_delta, quantity_after, note, changed_at) VALUES (?, ?, 'ORDER_RESERVE', ?, ?, ?, GETDATE())";
                try (PreparedStatement ps = conn.prepareStatement(logSql)) {
                    ps.setInt(1, ci.getVariantId());
                    ps.setInt(2, customerId);
                    ps.setInt(3, -ci.getQuantity());
                    ps.setInt(4, quantityAfter);
                    ps.setString(5, "Customer placed order.");
                    ps.executeUpdate();
                }

                // II.8: Packaging cost addition
                BigDecimal baseUnitPrice = pv.getDiscountPrice() != null ? pv.getDiscountPrice() : pv.getPrice();
                BigDecimal addOn = BigDecimal.ZERO;
                if ("Gift Box".equals(pv.getPackagingOption())) {
                    addOn = new BigDecimal("50000");
                } else if ("Foam Tray".equals(pv.getPackagingOption())) {
                    addOn = new BigDecimal("15000");
                }

                BigDecimal finalUnitPrice = baseUnitPrice.add(addOn);
                BigDecimal subtotal = finalUnitPrice.multiply(new BigDecimal(ci.getQuantity()));
                totalAmount = totalAmount.add(subtotal);

                OrderItem item = new OrderItem();
                item.setVariantId(ci.getVariantId());
                item.setProductNameSnapshot(p.getName());
                item.setVariantLabelSnapshot(pv.getVariantLabel() + (pv.getPackagingOption() != null ? " (" + pv.getPackagingOption() + ")" : ""));
                item.setQuantity(ci.getQuantity());
                item.setUnitPrice(finalUnitPrice);
                item.setSubtotal(subtotal);

                orderItems.add(item);
            }

            // Create Order
            BigDecimal deliveryFee = new BigDecimal("30000"); // flat fee
            BigDecimal finalAmount = totalAmount.add(deliveryFee);

            Order order = new Order();
            order.setCustomerId(customerId);
            order.setOwnerId(shopOwnerId);
            order.setDeliveryAddress(dto.getDeliveryAddress());
            order.setDeliveryTimeSlot(dto.getDeliveryTimeSlot());
            order.setNotes(dto.getNotes());
            order.setStatus(dto.getPaymentMethod().equals("CK") ? "PENDING_PAYMENT" : "CONFIRMED");
            order.setTotalAmount(totalAmount);
            order.setDeliveryFee(deliveryFee);
            order.setFinalAmount(finalAmount);
            order.setPaymentMethod(dto.getPaymentMethod());

            // Save order inside transaction
            int orderId;
            String insertOrderSql = "INSERT INTO orders (customer_id, owner_id, delivery_address, user_address, delivery_time_slot, notes, status, total_amount, delivery_fee, final_amount, payment_method, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), GETDATE())";
            try (PreparedStatement ps = conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, order.getCustomerId());
                ps.setInt(2, order.getOwnerId());
                ps.setString(3, order.getDeliveryAddress());
                ps.setString(4, dto.getDeliveryAddress()); // snapshot user address
                ps.setString(5, order.getDeliveryTimeSlot());
                ps.setString(6, order.getNotes());
                ps.setString(7, order.getStatus());
                ps.setBigDecimal(8, order.getTotalAmount());
                ps.setBigDecimal(9, order.getDeliveryFee());
                ps.setBigDecimal(10, order.getFinalAmount());
                ps.setString(11, order.getPaymentMethod());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        orderId = rs.getInt(1);
                    } else {
                        throw new SQLException("Failed to retrieve generated order_id.");
                    }
                }
            }

            order.setOrderId(orderId);

            // Save order items inside transaction
            for (OrderItem item : orderItems) {
                item.setOrderId(orderId);
                orderDAO.saveOrderItem(item, conn);
            }

            // Clear cart items
            String deleteCartItemsSql = "DELETE FROM cart_items WHERE cart_item_id = ?";
            for (com.fruitmkt.model.entity.CartItem ci : itemsToOrder) {
                try (PreparedStatement ps = conn.prepareStatement(deleteCartItemsSql)) {
                    ps.setInt(1, ci.getCartItemId());
                    ps.executeUpdate();
                }
            }

            conn.commit();
            return order;
        } catch (Exception e) {
            conn.rollback();
            if (e instanceof SQLException) {
                throw (SQLException) e;
            } else {
                throw new RuntimeException(e.getMessage(), e);
            }
        } finally {
            conn.close();
        }
    }

    /**
     * Lấy chi tiết đơn hàng (bao gồm danh sách mặt hàng đã snap).
     */
    public Order getOrderDetail(int orderId) throws SQLException {
        List<Order> list = orderDAO.findById(orderId);
        if (list.isEmpty()) {
            throw new SQLException("Đơn hàng không tồn tại.");
        }
        return list.get(0);
    }

    /**
     * Lấy danh sách mặt hàng của đơn hàng.
     */
    public List<OrderItem> getOrderItems(int orderId) throws SQLException {
        return orderDAO.findOrderItems(orderId);
    }

    /**
     * Lấy lịch sử mua hàng của khách hàng (II.35).
     */
    public PagedResultDTO getOrderHistory(int customerId, int page) throws SQLException {
        if (page < 1) page = 1;
        int pageSize = 10;
        
        // Count total orders
        int total = 0;
        String sql = "SELECT COUNT(*) FROM orders WHERE customer_id = ?";
        try (Connection conn = BaseDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) total = rs.getInt(1);
            }
        }

        int totalPages = Math.max(1, (int) Math.ceil((double) total / pageSize));
        if (page > totalPages) page = totalPages;

        List<Order> items = orderDAO.findByCustomer(customerId, page, pageSize);
        return new PagedResultDTO(items, page, totalPages, total, pageSize);
    }

    /**
     * Xác nhận đơn hàng từ phía shop owner.
     */
    public void confirmOrder(int orderId, int ownerId) throws SQLException {
        Order order = getOrderDetail(orderId);
        if (order.getOwnerId() != ownerId) {
            throw new IllegalArgumentException("Bạn không có quyền thao tác trên đơn hàng của shop khác.");
        }
        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            throw new IllegalArgumentException("Không thể xác nhận đơn hàng ở trạng thái hiện tại.");
        }
        orderDAO.updateStatus(orderId, "CONFIRMED");
    }

    /**
     * Hủy đơn hàng và hoàn trả kho transactionally (II.11).
     */
    public void cancelOrder(int orderId, int cancelledBy, String reason) throws SQLException {
        Order order = getOrderDetail(orderId);
        if ("CANCELLED".equals(order.getStatus())) {
            throw new IllegalArgumentException("Đơn hàng đã được hủy trước đó.");
        }

        List<OrderItem> items = orderDAO.findOrderItems(orderId);

        // Begin transaction
        Connection conn = BaseDAO.getConnection();
        conn.setAutoCommit(false);

        try {
            // Cancel order
            String cancelSql = "UPDATE orders SET status = 'CANCELLED', cancelled_at = GETDATE(), cancelled_by = ?, cancellation_reason = ?, updated_at = GETDATE() WHERE order_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(cancelSql)) {
                ps.setInt(1, cancelledBy);
                ps.setString(2, reason);
                ps.setInt(3, orderId);
                ps.executeUpdate();
            }

            // Restore stocks
            for (OrderItem item : items) {
                if (item.getVariantId() == null) continue;

                // Acquire Lock and get stock
                int currentStock = 0;
                String lockSql = "SELECT stock_quantity FROM product_variants WITH (UPDLOCK) WHERE variant_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(lockSql)) {
                    ps.setInt(1, item.getVariantId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            currentStock = rs.getInt("stock_quantity");
                        }
                    }
                }

                // Increment stock
                String restoreSql = "UPDATE product_variants SET stock_quantity = stock_quantity + ?, updated_at = GETDATE() WHERE variant_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(restoreSql)) {
                    ps.setInt(1, item.getQuantity());
                    ps.setInt(2, item.getVariantId());
                    ps.executeUpdate();
                }

                int quantityAfter = currentStock + item.getQuantity();

                // Log inventory log
                String logSql = "INSERT INTO inventory_logs (variant_id, changed_by, change_type, quantity_delta, quantity_after, note, changed_at) VALUES (?, ?, 'ORDER_RELEASE', ?, ?, ?, GETDATE())";
                try (PreparedStatement ps = conn.prepareStatement(logSql)) {
                    ps.setInt(1, item.getVariantId());
                    ps.setInt(2, cancelledBy);
                    ps.setInt(3, item.getQuantity());
                    ps.setInt(4, quantityAfter);
                    ps.setString(5, "Order #" + orderId + " was cancelled. Stock restored.");
                    ps.executeUpdate();
                }
            }

            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            if (e instanceof SQLException) {
                throw (SQLException) e;
            } else {
                throw new RuntimeException(e.getMessage(), e);
            }
        } finally {
            conn.close();
        }
    }

    /**
     * Lấy danh sách đơn hàng cho chủ cửa hàng có lọc trạng thái.
     */
    public PagedResultDTO shopOrders(int ownerId, String status, int page) throws SQLException {
        if (page < 1) page = 1;
        int pageSize = 15;

        // Count total orders
        int total = 0;
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM orders WHERE owner_id = ? ");
        if (status != null && !status.trim().isEmpty()) {
            sql.append("AND status = ? ");
        }
        try (Connection conn = BaseDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, ownerId);
            if (status != null && !status.trim().isEmpty()) {
                ps.setString(2, status);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) total = rs.getInt(1);
            }
        }

        int totalPages = Math.max(1, (int) Math.ceil((double) total / pageSize));
        if (page > totalPages) page = totalPages;

        List<Order> items = orderDAO.findByOwner(ownerId, status, page, pageSize);
        return new PagedResultDTO(items, page, totalPages, total, pageSize);
    }
}
