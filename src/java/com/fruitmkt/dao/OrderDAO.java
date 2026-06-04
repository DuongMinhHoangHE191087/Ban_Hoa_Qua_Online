package com.fruitmkt.dao;

import com.fruitmkt.dao.base.BaseDAO;
import com.fruitmkt.model.entity.Order;
import com.fruitmkt.model.entity.OrderItem;
import java.sql.*;
import java.util.*;

/**
 * OrderDAO — DAO cho entity Order.
 *
 * QUY TẮC:
 *   - Chỉ chứa SQL, không chứa business logic
 *   - Dùng PreparedStatement, KHÔNG nối chuỗi SQL
 *   - Mỗi method ném SQLException để Service xử lý
 *   - Dùng try-with-resources cho Connection + PreparedStatement
 *
 * @author fruitmkt-team
 */
public class OrderDAO extends BaseDAO {

    /**
     * Tìm đơn hàng theo ID.
     */
    public List<Order> findById(int id) throws SQLException {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE order_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Tìm đơn hàng theo ID khách hàng có phân trang.
     */
    public List<Order> findByCustomer(int customerId, int page, int pageSize) throws SQLException {
        return findByCustomer(customerId, null, page, pageSize);
    }

    /**
     * Tìm đơn hàng theo ID khách hàng, lọc theo trạng thái có phân trang.
     */
    public List<Order> findByCustomer(int customerId, String status, int page, int pageSize) throws SQLException {
        List<Order> list = new ArrayList<>();
        int offset = (page - 1) * pageSize;
        if (offset < 0) offset = 0;
        
        StringBuilder sql = new StringBuilder("SELECT * FROM orders WHERE customer_id = ? ");
        List<Object> params = new ArrayList<>();
        params.add(customerId);
        
        if (status != null && !status.trim().isEmpty()) {
            sql.append("AND status = ? ");
            params.add(status);
        }
        
        sql.append("ORDER BY order_id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(offset);
        params.add(pageSize);
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }


    /**
     * Tìm đơn hàng thuộc về chủ shop theo trạng thái có phân trang.
     */
    public List<Order> findByOwner(int ownerId, String status, int page, int pageSize) throws SQLException {
        List<Order> list = new ArrayList<>();
        int offset = (page - 1) * pageSize;
        StringBuilder sql = new StringBuilder("SELECT * FROM orders WHERE owner_id = ? ");
        List<Object> params = new ArrayList<>();
        params.add(ownerId);
        if (status != null && !status.trim().isEmpty()) {
            sql.append("AND status = ? ");
            params.add(status);
        }
        sql.append("ORDER BY order_id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(offset);
        params.add(pageSize);
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public int countAll(String status) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM orders ");
        List<Object> params = new ArrayList<>();
        if (status != null && !status.trim().isEmpty()) {
            sql.append("WHERE status = ? ");
            params.add(status);
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) { return rs.getInt(1); }
            }
        }
        return 0;
    }

    /**
     * Lấy toàn bộ danh sách đơn hàng có phân trang, có thể lọc theo trạng thái.
     */
    public List<Order> findAll(String status, int page, int pageSize) throws SQLException {
        List<Order> list = new ArrayList<>();
        int offset = (page - 1) * pageSize;
        StringBuilder sql = new StringBuilder("SELECT * FROM orders ");
        List<Object> params = new ArrayList<>();
        if (status != null && !status.trim().isEmpty()) {
            sql.append("WHERE status = ? ");
            params.add(status);
        }
        sql.append("ORDER BY order_id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(offset);
        params.add(pageSize);
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public int countAll(String status, String paymentMethod, String paymentStatus) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM orders o ");
        if (paymentStatus != null && !paymentStatus.trim().isEmpty()) {
            sql.append("JOIN payment_transactions pt ON o.order_id = pt.order_id ");
        }
        sql.append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        
        if (status != null && !status.trim().isEmpty()) {
            sql.append("AND o.status = ? ");
            params.add(status);
        }
        if (paymentMethod != null && !paymentMethod.trim().isEmpty()) {
            sql.append("AND o.payment_method = ? ");
            params.add(paymentMethod);
        }
        if (paymentStatus != null && !paymentStatus.trim().isEmpty()) {
            sql.append("AND pt.status = ? ");
            params.add(paymentStatus);
        }
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) { return rs.getInt(1); }
            }
        }
        return 0;
    }

    public List<Order> findAll(String status, String paymentMethod, String paymentStatus, int page, int pageSize) throws SQLException {
        List<Order> list = new ArrayList<>();
        int offset = (page - 1) * pageSize;
        if (offset < 0) offset = 0;
        
        StringBuilder sql = new StringBuilder("SELECT o.* FROM orders o ");
        if (paymentStatus != null && !paymentStatus.trim().isEmpty()) {
            sql.append("JOIN payment_transactions pt ON o.order_id = pt.order_id ");
        }
        
        sql.append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        
        if (status != null && !status.trim().isEmpty()) {
            sql.append("AND o.status = ? ");
            params.add(status);
        }
        if (paymentMethod != null && !paymentMethod.trim().isEmpty()) {
            sql.append("AND o.payment_method = ? ");
            params.add(paymentMethod);
        }
        if (paymentStatus != null && !paymentStatus.trim().isEmpty()) {
            sql.append("AND pt.status = ? ");
            params.add(paymentStatus);
        }
        
        sql.append("ORDER BY o.order_id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(offset);
        params.add(pageSize);
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Lưu đơn hàng mới vào DB và trả về ID đơn hàng tự sinh.
     */
    public int save(Order order) throws SQLException {
        String sql = "INSERT INTO orders (customer_id, owner_id, delivery_address, user_address, delivery_time_slot, notes, cancelled_at, cancelled_by, cancellation_reason, status, total_amount, delivery_fee, discount_amount, system_discount_amount, shop_discount_amount, platform_fee, final_amount, payment_method, refund_status, created_at, updated_at) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), GETDATE())";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, order.getCustomerId());
            ps.setInt(2, order.getOwnerId());
            ps.setString(3, order.getDeliveryAddress());
            ps.setString(4, order.getUserAddress());
            ps.setString(5, order.getDeliveryTimeSlot());
            ps.setString(6, order.getNotes());
            
            if (order.getCancelledAt() != null) {
                ps.setTimestamp(7, Timestamp.valueOf(order.getCancelledAt()));
            } else {
                ps.setNull(7, Types.TIMESTAMP);
            }
            
            if (order.getCancelledBy() != null) {
                ps.setInt(8, order.getCancelledBy());
            } else {
                ps.setNull(8, Types.INTEGER);
            }
            
            ps.setString(9, order.getCancellationReason());
            ps.setString(10, order.getStatus() != null ? order.getStatus() : "PENDING_PAYMENT");
            ps.setBigDecimal(11, order.getTotalAmount() != null ? order.getTotalAmount() : java.math.BigDecimal.ZERO);
            ps.setBigDecimal(12, order.getDeliveryFee() != null ? order.getDeliveryFee() : java.math.BigDecimal.ZERO);
            ps.setBigDecimal(13, order.getDiscountAmount() != null ? order.getDiscountAmount() : java.math.BigDecimal.ZERO);
            ps.setBigDecimal(14, order.getSystemDiscountAmount() != null ? order.getSystemDiscountAmount() : java.math.BigDecimal.ZERO);
            ps.setBigDecimal(15, order.getShopDiscountAmount() != null ? order.getShopDiscountAmount() : java.math.BigDecimal.ZERO);
            ps.setBigDecimal(16, order.getPlatformFee() != null ? order.getPlatformFee() : java.math.BigDecimal.ZERO);
            ps.setBigDecimal(17, order.getFinalAmount() != null ? order.getFinalAmount() : java.math.BigDecimal.ZERO);
            ps.setString(18, order.getPaymentMethod() != null ? order.getPaymentMethod() : "COD");
            ps.setString(19, order.getRefundStatus() != null ? order.getRefundStatus() : "NONE");
            
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Lưu đơn hàng thất bại, không lấy được mã khóa tự tăng.");
    }

    /**
     * Cập nhật trạng thái đơn hàng.
     */
    public void updateStatus(int orderId, String status) throws SQLException {
        String sql;
        if ("CONFIRMED".equals(status)) {
            sql = "UPDATE orders SET status = ?, shop_acceptance_deadline = DATEADD(minute, 30, GETDATE()), updated_at = GETDATE() WHERE order_id = ?";
        } else if ("APPROVED".equals(status)) {
            sql = "UPDATE orders SET status = ?, shop_accepted_at = GETDATE(), shop_acceptance_deadline = NULL, updated_at = GETDATE() WHERE order_id = ?";
        } else {
            sql = "UPDATE orders SET status = ?, updated_at = GETDATE() WHERE order_id = ?";
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        }
    }


    public void updateRefundStatus(int orderId, String refundStatus) throws SQLException {
        String sql = "UPDATE orders SET refund_status = ?, updated_at = GETDATE() WHERE order_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, refundStatus);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        }
    }


    /**
     * Hủy đơn hàng.
     */
    public void cancel(int orderId, int cancelledBy, String reason) throws SQLException {
        String sql = "UPDATE orders SET status = 'CANCELLED', cancelled_at = GETDATE(), cancelled_by = ?, cancellation_reason = ?, updated_at = GETDATE() WHERE order_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cancelledBy);
            ps.setString(2, reason);
            ps.setInt(3, orderId);
            ps.executeUpdate();
        }
    }

    /**
     * Hoàn trả lại số lượng tồn kho cho các sản phẩm trong đơn hàng.
     */
    public void restoreInventoryStock(int orderId) throws SQLException {
        String sql = "UPDATE pv "
                   + "SET pv.stock_quantity = pv.stock_quantity + oi.quantity "
                   + "FROM product_variants pv "
                   + "JOIN order_items oi ON pv.variant_id = oi.variant_id "
                   + "WHERE oi.order_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.executeUpdate();
        }
    }

    /**
     * Hoàn trả lại số lượng tồn kho cho một sản phẩm cụ thể dựa trên order_item_id.
     */
    public void restoreItemInventoryStock(int orderItemId, int quantity) throws SQLException {
        String sql = "UPDATE pv "
                   + "SET pv.stock_quantity = pv.stock_quantity + ? "
                   + "FROM product_variants pv "
                   + "JOIN order_items oi ON pv.variant_id = oi.variant_id "
                   + "WHERE oi.order_item_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, orderItemId);
            ps.executeUpdate();
        }
    }

    /**
     * Lấy owner_id của sản phẩm chứa variant được chỉ định.
     * Dùng khi tạo đơn hàng để xác định chủ shop.
     *
     * @param productId ID sản phẩm (đã được lấy từ ProductVariant)
     * @return owner_id, hoặc -1 nếu không tìm thấy
     */
    public int getOwnerIdByProductId(int productId) throws SQLException {
        String sql = "SELECT owner_id FROM products WHERE product_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("owner_id");
                }
            }
        }
        return -1;
    }

    /**
     * Mở kết nối DB để dùng trong transaction ngoài DAO (ví dụ: CheckoutServlet).
     * Caller phải tự đóng connection trong try-finally.
     */
    public Connection openConnection() throws SQLException {
        return getConnection();
    }


    /**
     * [RBAC-safe] Tìm đơn hàng theo ID CHỈ KHI thuộc về customerId đó.
     */
    public Order findByIdForCustomer(int orderId, int customerId) throws SQLException {
        String sql = "SELECT * FROM orders WHERE order_id = ? AND customer_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /**
     * [RBAC-safe] Tìm đơn hàng theo ID CHỈ KHI thuộc về shop ownerId đó.
     */
    public Order findByIdForOwner(int orderId, int ownerId) throws SQLException {
        String sql = "SELECT * FROM orders WHERE order_id = ? AND owner_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /** Đếm tổng đơn hàng của customer (phân trang). */
    public int countByCustomer(int customerId) throws SQLException {
        return countByCustomer(customerId, null);
    }

    /** Đếm tổng đơn hàng của customer với status filter (phân trang). */
    public int countByCustomer(int customerId, String status) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM orders WHERE customer_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(customerId);
        
        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs2 = ps.executeQuery()) {
                if (rs2.next()) return rs2.getInt(1);
            }
        }
        return 0;
    }


    /** Đếm tổng đơn hàng của shop owner (phân trang). */
    public int countByOwner(int ownerId, String status) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM orders WHERE owner_id = ?");
        if (status != null && !status.trim().isEmpty()) sql.append(" AND status = ?");
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, ownerId);
            if (status != null && !status.trim().isEmpty()) ps.setString(2, status);
            try (ResultSet rs2 = ps.executeQuery()) {
                if (rs2.next()) return rs2.getInt(1);
            }
        }
        return 0;
    }

    /** Tính tổng doanh thu của shop owner (chỉ các đơn hàng DELIVERED). */
    public java.math.BigDecimal getRevenueByOwner(int ownerId) throws SQLException {
        String sql = "SELECT SUM(final_amount) FROM orders WHERE owner_id = ? AND status = 'DELIVERED'";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    java.math.BigDecimal revenue = rs.getBigDecimal(1);
                    return revenue != null ? revenue : java.math.BigDecimal.ZERO;
                }
            }
        }
        return java.math.BigDecimal.ZERO;
    }


    /** Ánh xạ ResultSet -> Order — gọi trong mọi query SELECT */
    private Order mapRow(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setOrderId(rs.getInt("order_id"));
        o.setCustomerId(rs.getInt("customer_id"));
        o.setOwnerId(rs.getInt("owner_id"));
        o.setDeliveryAddress(rs.getString("delivery_address"));
        o.setUserAddress(rs.getString("user_address"));
        o.setDeliveryTimeSlot(rs.getString("delivery_time_slot"));
        o.setNotes(rs.getString("notes"));
        
        Timestamp cancelledAtVal = rs.getTimestamp("cancelled_at");
        if (cancelledAtVal != null) {
            o.setCancelledAt(cancelledAtVal.toLocalDateTime());
        }
        
        int cancelledByVal = rs.getInt("cancelled_by");
        o.setCancelledBy(rs.wasNull() ? null : cancelledByVal);
        
        o.setCancellationReason(rs.getString("cancellation_reason"));
        o.setStatus(rs.getString("status"));
        o.setTotalAmount(rs.getBigDecimal("total_amount"));
        o.setDeliveryFee(rs.getBigDecimal("delivery_fee"));
        o.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        o.setSystemDiscountAmount(rs.getBigDecimal("system_discount_amount"));
        o.setShopDiscountAmount(rs.getBigDecimal("shop_discount_amount"));
        o.setPlatformFee(rs.getBigDecimal("platform_fee"));
        o.setFinalAmount(rs.getBigDecimal("final_amount"));
        o.setPaymentMethod(rs.getString("payment_method"));
        o.setRefundStatus(rs.getString("refund_status"));
        
        Timestamp createdAtVal = rs.getTimestamp("created_at");
        if (createdAtVal != null) {
            o.setCreatedAt(createdAtVal.toLocalDateTime());
        }
        
        Timestamp updatedAtVal = rs.getTimestamp("updated_at");
        if (updatedAtVal != null) {
            o.setUpdatedAt(updatedAtVal.toLocalDateTime());
        }
        return o;
    }

    public List<OrderItem> findItemsByOrderId(int orderId) throws SQLException {
        List<OrderItem> list = new ArrayList<>();
        String sql = "SELECT * FROM order_items WHERE order_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setOrderItemId(rs.getInt("order_item_id"));
                    item.setOrderId(rs.getInt("order_id"));
                    int vId = rs.getInt("variant_id");
                    item.setVariantId(rs.wasNull() ? null : vId);
                    item.setProductNameSnapshot(rs.getString("product_name_snapshot"));
                    item.setVariantLabelSnapshot(rs.getString("variant_label_snapshot"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setSubtotal(rs.getBigDecimal("subtotal"));
                    list.add(item);
                }
            }
        }
        return list;
    }
}
