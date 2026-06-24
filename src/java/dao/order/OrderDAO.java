package dao.order;

import dao.system.BaseDAO;
import model.entity.order.Order;
import model.entity.order.OrderItem;
import util.PaginationHelper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Set;

/**
 * OrderDAO â€” DAO cho entity Order.
 *
 * QUY Táº®C:
 *   - Chá»‰ chá»©a SQL, khÃ´ng chá»©a business logic
 *   - DÃ¹ng PreparedStatement, KHÃ”NG ná»‘i chuá»—i SQL
 *   - Má»—i method nÃ©m SQLException Ä‘á»ƒ Service xá»­ lÃ½
 *   - DÃ¹ng try-with-resources cho Connection + PreparedStatement
 *
 * @author fruitmkt-team
 */
public class OrderDAO extends BaseDAO {

    @FunctionalInterface
    private interface SqlAction<T> {
        T run() throws SQLException;
    }

    private <T> T executeWithTransientRetry(SqlAction<T> action) throws SQLException {
        SQLException last = null;
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                return action.run();
            } catch (SQLException e) {
                last = e;
                String sqlState = e.getSQLState();
                String message = e.getMessage();
                boolean transientConnectionError = (sqlState != null && sqlState.startsWith("08"))
                        || (message != null && message.toLowerCase(Locale.ROOT).contains("socket closed"));
                if (attempt == 1 && transientConnectionError) {
                    continue;
                }
                throw e;
            }
        }
        throw last;
    }

    /**
     * TÃ¬m Ä‘Æ¡n hÃ ng theo ID.
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
     * TÃ¬m Ä‘Æ¡n hÃ ng theo ID vÃ  tráº£ vá» 1 object duy nháº¥t.
     */
    public Order findOneById(int id) throws SQLException {
        try (Connection conn = getConnection()) {
            return findOneById(conn, id);
        }
    }

    /**
     * TÃ¬m Ä‘Æ¡n hÃ ng theo ID trÃªn connection hiá»‡n táº¡i.
     */
    public Order findOneById(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM orders WHERE order_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

        /**
     * TÃ¬m táº¥t cáº£ Ä‘Æ¡n hÃ ng (bao gá»“m cáº£ Parent vÃ  Child) cá»§a khÃ¡ch hÃ ng.
     */
    public List<Order> findByCustomerId(int customerId) throws SQLException {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE customer_id = ? ORDER BY order_id DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }


    /**
     * Tìm đơn hàng theo ID và trả về 1 object duy nhất.
     */

    /**
     * Tìm đơn hàng theo ID khách hàng có phân trang.
     */
    public List<Order> findByCustomer(int customerId, int page, int pageSize) throws SQLException {
        return findByCustomer(customerId, null, page, pageSize);
    }

    /**
     * TÃ¬m Ä‘Æ¡n hÃ ng theo ID khÃ¡ch hÃ ng, lá»c theo tráº¡ng thÃ¡i cÃ³ phÃ¢n trang.
     */
    public List<Order> findByCustomer(int customerId, String status, int page, int pageSize) throws SQLException {
        List<Order> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder("SELECT * FROM orders WHERE customer_id = ? AND parent_order_id IS NULL ");
        List<Object> params = new ArrayList<>();
        params.add(customerId);

        if (status != null && !status.trim().isEmpty()) {
            sql.append("AND status = ? ");
            params.add(status);
        }

        sql.append("ORDER BY order_id DESC").append(PaginationHelper.OFFSET_FETCH_SQL);

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            for (Object param : params) {
                ps.setObject(paramIndex++, param);
            }
            PaginationHelper.bindOffsetFetch(ps, paramIndex, page, pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }


    /**
     * TÃ¬m Ä‘Æ¡n hÃ ng thuá»™c vá» chá»§ shop theo tráº¡ng thÃ¡i cÃ³ phÃ¢n trang.
     */
    public List<Order> findByOwner(int ownerId, String status, int page, int pageSize) throws SQLException {
        return executeWithTransientRetry(() -> findByOwnerInternal(ownerId, status, page, pageSize));
    }

    private List<Order> findByOwnerInternal(int ownerId, String status, int page, int pageSize) throws SQLException {
        List<Order> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM orders WHERE owner_id = ? ");
        List<Object> params = new ArrayList<>();
        params.add(ownerId);
        if (status != null && !status.trim().isEmpty()) {
            sql.append("AND status = ? ");
            params.add(status);
        }
        sql.append("ORDER BY order_id DESC").append(PaginationHelper.OFFSET_FETCH_SQL);
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            for (Object param : params) {
                ps.setObject(paramIndex++, param);
            }
            PaginationHelper.bindOffsetFetch(ps, paramIndex, page, pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /** Láº¥y toÃ n bá»™ Ä‘Æ¡n hÃ ng cá»§a shop cÃ²n Ä‘ang má»Ÿ Ä‘á»ƒ phá»¥c vá»¥ cascade khi shop bá»‹ Ä‘Ã¬nh chá»‰. */
    public List<Order> findOpenByOwner(int ownerId) throws SQLException {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE owner_id = ? "
                + "AND status NOT IN ('CANCELLED', 'DELIVERED') "
                + "ORDER BY order_id DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public int countAll(String status) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM orders WHERE parent_order_id IS NULL ");
        List<Object> params = new ArrayList<>();
        if (status != null && !status.trim().isEmpty()) {
            sql.append("AND status = ? ");
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
     * Láº¥y toÃ n bá»™ danh sÃ¡ch Ä‘Æ¡n hÃ ng cÃ³ phÃ¢n trang, cÃ³ thá»ƒ lá»c theo tráº¡ng thÃ¡i.
     */
    public List<Order> findAll(String status, int page, int pageSize) throws SQLException {
        List<Order> list = new ArrayList<>();
        int offset = (page - 1) * pageSize;
        StringBuilder sql = new StringBuilder("SELECT * FROM orders WHERE parent_order_id IS NULL ");
        List<Object> params = new ArrayList<>();
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

    public int countAll(String status, String paymentMethod, String paymentStatus) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM orders o ");
        if (paymentStatus != null && !paymentStatus.trim().isEmpty()) {
            sql.append("JOIN payment_transactions pt ON o.order_id = pt.order_id ");
        }
        sql.append("WHERE o.parent_order_id IS NULL ");
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
        
        sql.append("WHERE o.parent_order_id IS NULL ");
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
     * LÆ°u Ä‘Æ¡n hÃ ng má»›i vÃ o DB vÃ  tráº£ vá» ID Ä‘Æ¡n hÃ ng tá»± sinh.
     */
    public int save(Order order) throws SQLException {
        String sql = "INSERT INTO orders (customer_id, owner_id, parent_order_id, order_type, delivery_address, delivery_time_slot, notes, cancelled_at, cancelled_by, cancellation_reason, status, total_amount, delivery_fee, discount_amount, system_discount_amount, shop_discount_amount, platform_fee, final_amount, payment_method, refund_status, created_at, updated_at) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), GETDATE())";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, order.getCustomerId());
            if (order.getOwnerIdObject() != null && order.getOwnerIdObject() > 0) {
                ps.setInt(2, order.getOwnerIdObject());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            if (order.getParentOrderId() != null && order.getParentOrderId() > 0) {
                ps.setInt(3, order.getParentOrderId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, order.getOrderType() != null ? order.getOrderType() : "CHILD");
            ps.setString(5, order.getDeliveryAddress());
            ps.setString(6, order.getDeliveryTimeSlot());
            ps.setString(7, order.getNotes());
            
            if (order.getCancelledAt() != null) {
                ps.setTimestamp(8, Timestamp.valueOf(order.getCancelledAt()));
            } else {
                ps.setNull(8, Types.TIMESTAMP);
            }
            
            if (order.getCancelledBy() != null) {
                ps.setInt(9, order.getCancelledBy());
            } else {
                ps.setNull(9, Types.INTEGER);
            }
            
            ps.setString(10, order.getCancellationReason());
            ps.setString(11, order.getStatus() != null ? order.getStatus() : "PENDING_PAYMENT");
            ps.setBigDecimal(12, order.getTotalAmount() != null ? order.getTotalAmount() : java.math.BigDecimal.ZERO);
            ps.setBigDecimal(13, order.getDeliveryFee() != null ? order.getDeliveryFee() : java.math.BigDecimal.ZERO);
            ps.setBigDecimal(14, order.getDiscountAmount() != null ? order.getDiscountAmount() : java.math.BigDecimal.ZERO);
            ps.setBigDecimal(15, order.getSystemDiscountAmount() != null ? order.getSystemDiscountAmount() : java.math.BigDecimal.ZERO);
            ps.setBigDecimal(16, order.getShopDiscountAmount() != null ? order.getShopDiscountAmount() : java.math.BigDecimal.ZERO);
            ps.setBigDecimal(17, order.getPlatformFee() != null ? order.getPlatformFee() : java.math.BigDecimal.ZERO);
            ps.setBigDecimal(18, order.getFinalAmount() != null ? order.getFinalAmount() : java.math.BigDecimal.ZERO);
            ps.setString(19, order.getPaymentMethod() != null ? order.getPaymentMethod() : "COD");
            ps.setString(20, order.getRefundStatus() != null ? order.getRefundStatus() : "NONE");
            
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("LÆ°u Ä‘Æ¡n hÃ ng tháº¥t báº¡i, khÃ´ng láº¥y Ä‘Æ°á»£c mÃ£ khÃ³a tá»± tÄƒng.");
    }

    /**
     * Cáº­p nháº­t tráº¡ng thÃ¡i Ä‘Æ¡n hÃ ng.
     */
    public int save(Connection conn, Order order) throws SQLException {
        String sql = "INSERT INTO orders (customer_id, owner_id, parent_order_id, order_type, delivery_address, "
                + "recipient_name, recipient_phone, delivery_time_slot, notes, cancelled_at, cancelled_by, "
                + "cancellation_reason, status, total_amount, delivery_fee, discount_amount, system_discount_amount, "
                + "shop_discount_amount, platform_fee, final_amount, payment_method, refund_status, "
                + "shop_acceptance_deadline, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), GETDATE())";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, order.getCustomerId());
            if (order.getOwnerIdObject() != null && order.getOwnerIdObject() > 0) {
                ps.setInt(2, order.getOwnerIdObject());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            if (order.getParentOrderId() != null && order.getParentOrderId() > 0) {
                ps.setInt(3, order.getParentOrderId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, order.getOrderType() != null ? order.getOrderType() : "CHILD");
            ps.setString(5, order.getDeliveryAddress());
            ps.setString(6, order.getRecipientName());
            ps.setString(7, order.getRecipientPhone());
            ps.setString(8, order.getDeliveryTimeSlot());
            ps.setString(9, order.getNotes());
            if (order.getCancelledAt() != null) {
                ps.setTimestamp(10, Timestamp.valueOf(order.getCancelledAt()));
            } else {
                ps.setNull(10, Types.TIMESTAMP);
            }
            if (order.getCancelledBy() != null) {
                ps.setInt(11, order.getCancelledBy());
            } else {
                ps.setNull(11, Types.INTEGER);
            }
            ps.setString(12, order.getCancellationReason());
            String status = order.getStatus() != null ? order.getStatus() : "PENDING_PAYMENT";
            ps.setString(13, status);
            ps.setBigDecimal(14, order.getTotalAmount() != null ? order.getTotalAmount() : java.math.BigDecimal.ZERO);
            ps.setBigDecimal(15, order.getDeliveryFee() != null ? order.getDeliveryFee() : java.math.BigDecimal.ZERO);
            ps.setBigDecimal(16, order.getDiscountAmount() != null ? order.getDiscountAmount() : java.math.BigDecimal.ZERO);
            ps.setBigDecimal(17, order.getSystemDiscountAmount() != null ? order.getSystemDiscountAmount() : java.math.BigDecimal.ZERO);
            ps.setBigDecimal(18, order.getShopDiscountAmount() != null ? order.getShopDiscountAmount() : java.math.BigDecimal.ZERO);
            ps.setBigDecimal(19, order.getPlatformFee() != null ? order.getPlatformFee() : java.math.BigDecimal.ZERO);
            ps.setBigDecimal(20, order.getFinalAmount() != null ? order.getFinalAmount() : java.math.BigDecimal.ZERO);
            ps.setString(21, order.getPaymentMethod() != null ? order.getPaymentMethod() : "COD");
            ps.setString(22, order.getRefundStatus() != null ? order.getRefundStatus() : "NONE");
            if ("CONFIRMED".equals(status)) {
                ps.setTimestamp(23, Timestamp.valueOf(java.time.LocalDateTime.now().plusMinutes(30)));
            } else {
                ps.setNull(23, Types.TIMESTAMP);
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Luu don hang that bai, khong lay duoc ma khoa tu tang.");
    }

    public void updateStatus(int orderId, String status) throws SQLException {
        try (Connection conn = getConnection()) {
            updateStatus(conn, orderId, status);
        }
    }

    public void updateStatus(Connection conn, int orderId, String status) throws SQLException {
        String sql;
        if ("CONFIRMED".equals(status)) {
            sql = "UPDATE orders SET status = ?, shop_acceptance_deadline = DATEADD(minute, 30, GETDATE()), updated_at = GETDATE() WHERE order_id = ?";
        } else if ("APPROVED".equals(status)) {
            sql = "UPDATE orders SET status = ?, shop_accepted_at = GETDATE(), shop_acceptance_deadline = NULL, updated_at = GETDATE() WHERE order_id = ?";
        } else {
            sql = "UPDATE orders SET status = ?, updated_at = GETDATE() WHERE order_id = ?";
        }
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
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
     * Há»§y Ä‘Æ¡n hÃ ng.
     */
    public void cancel(int orderId, int cancelledBy, String reason) throws SQLException {
        try (Connection conn = getConnection()) {
            cancel(conn, orderId, cancelledBy, reason);
        }
    }

    public void cancel(Connection conn, int orderId, int cancelledBy, String reason) throws SQLException {
        String sql = "UPDATE orders SET status = 'CANCELLED', cancelled_at = GETDATE(), cancelled_by = ?, cancellation_reason = ?, updated_at = GETDATE() WHERE order_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cancelledBy);
            ps.setString(2, reason);
            ps.setInt(3, orderId);
            ps.executeUpdate();
        }
    }

    /**
     * HoÃ n tráº£ láº¡i sá»‘ lÆ°á»£ng tá»“n kho cho cÃ¡c sáº£n pháº©m trong Ä‘Æ¡n hÃ ng.
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
     * HoÃ n tráº£ láº¡i sá»‘ lÆ°á»£ng tá»“n kho cho má»™t sáº£n pháº©m cá»¥ thá»ƒ dá»±a trÃªn order_item_id.
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
     * Láº¥y owner_id cá»§a sáº£n pháº©m chá»©a variant Ä‘Æ°á»£c chá»‰ Ä‘á»‹nh.
     * DÃ¹ng khi táº¡o Ä‘Æ¡n hÃ ng Ä‘á»ƒ xÃ¡c Ä‘á»‹nh chá»§ shop.
     *
     * @param productId ID sáº£n pháº©m (Ä‘Ã£ Ä‘Æ°á»£c láº¥y tá»« ProductVariant)
     * @return owner_id, hoáº·c -1 náº¿u khÃ´ng tÃ¬m tháº¥y
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
     * Má»Ÿ káº¿t ná»‘i DB Ä‘á»ƒ dÃ¹ng trong transaction ngoÃ i DAO (vÃ­ dá»¥: CheckoutServlet).
     * Caller pháº£i tá»± Ä‘Ã³ng connection trong try-finally.
     */
    public Connection openConnection() throws SQLException {
        return getConnection();
    }

    public void updatePlatformFee(Connection conn, int orderId, java.math.BigDecimal platformFee) throws SQLException {
        String sql = "UPDATE orders SET platform_fee = ?, updated_at = GETDATE() WHERE order_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, platformFee != null ? platformFee : java.math.BigDecimal.ZERO);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        }
    }


    /**
     * [RBAC-safe] TÃ¬m Ä‘Æ¡n hÃ ng theo ID CHá»ˆ KHI thuá»™c vá» customerId Ä‘Ã³.
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
     * [RBAC-safe] TÃ¬m Ä‘Æ¡n hÃ ng theo ID CHá»ˆ KHI thuá»™c vá» shop ownerId Ä‘Ã³.
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

    /** Äáº¿m tá»•ng Ä‘Æ¡n hÃ ng cá»§a customer (phÃ¢n trang). */
    public int countByCustomer(int customerId) throws SQLException {
        return countByCustomer(customerId, null);
    }

    /** Äáº¿m tá»•ng Ä‘Æ¡n hÃ ng cá»§a customer vá»›i status filter (phÃ¢n trang). */
    public int countByCustomer(int customerId, String status) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM orders WHERE customer_id = ? AND parent_order_id IS NULL");
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


    /** Äáº¿m tá»•ng Ä‘Æ¡n hÃ ng cá»§a shop owner (phÃ¢n trang). */
    public int countByOwner(int ownerId, String status) throws SQLException {
        return executeWithTransientRetry(() -> countByOwnerInternal(ownerId, status));
    }

    private int countByOwnerInternal(int ownerId, String status) throws SQLException {
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

    /** TÃ­nh tá»•ng doanh thu cá»§a shop owner (chá»‰ cÃ¡c Ä‘Æ¡n hÃ ng DELIVERED). */
    public java.math.BigDecimal getRevenueByOwner(int ownerId) throws SQLException {
        String sql = "SELECT SUM(final_amount) FROM orders WHERE owner_id = ? AND status = 'DELIVERED' AND order_type = 'CHILD'";
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

    /** Tính tổng doanh thu tạm tính của shop owner (các đơn hàng active chưa DELIVERED/CANCELLED). */
    public java.math.BigDecimal getEstimatedRevenueByOwner(int ownerId) throws SQLException {
        String sql = "SELECT SUM(final_amount) FROM orders WHERE owner_id = ? AND status IN ('PENDING_PAYMENT', 'CONFIRMED', 'APPROVED', 'PREPARING', 'DISPATCHED') AND order_type = 'CHILD'";
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


    /** Ãnh xáº¡ ResultSet -> Order â€” gá»i trong má»i query SELECT */
    private Order mapRow(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setOrderId(rs.getInt("order_id"));
        o.setCustomerId(rs.getInt("customer_id"));
        int ownerId = rs.getInt("owner_id");
        o.setOwnerId(rs.wasNull() ? null : ownerId);
        int parentOrderId = rs.getInt("parent_order_id");
        o.setParentOrderId(rs.wasNull() ? null : parentOrderId);
        o.setOrderType(rs.getString("order_type"));
        o.setDeliveryAddress(rs.getString("delivery_address"));
        o.setRecipientName(rs.getString("recipient_name"));
        o.setRecipientPhone(rs.getString("recipient_phone"));
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
        o.setReceivedStatus(rs.getString("received_status"));

        Timestamp deadlineVal = rs.getTimestamp("shop_acceptance_deadline");
        if (deadlineVal != null) o.setShopAcceptanceDeadline(deadlineVal.toLocalDateTime());

        Timestamp acceptedAtVal = rs.getTimestamp("shop_accepted_at");
        if (acceptedAtVal != null) o.setShopAcceptedAt(acceptedAtVal.toLocalDateTime());

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
        try (Connection conn = getConnection()) {
            return findItemsByOrderId(conn, orderId);
        }
    }

    public List<OrderItem> findItemsByOrderId(Connection conn, int orderId) throws SQLException {
        List<OrderItem> list = new ArrayList<>();
        String sql = "SELECT oi.*, pi.file_path AS image_path "
                   + "FROM order_items oi "
                   + "LEFT JOIN product_variants pv ON pv.variant_id = oi.variant_id "
                   + "LEFT JOIN product_images pi ON pi.product_id = pv.product_id AND pi.is_primary = 1 "
                   + "WHERE oi.order_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapOrderItem(rs));
                }
            }
        }
        return list;
    }

    /**
     * Batch load order_items cho nhiá»u order_id Ä‘á»ƒ trÃ¡nh N+1 khi render danh sÃ¡ch Ä‘Æ¡n.
     */
    public Map<Integer, List<OrderItem>> findItemsByOrderIds(Collection<Integer> orderIds) throws SQLException {
        Map<Integer, List<OrderItem>> map = new LinkedHashMap<>();
        if (orderIds == null || orderIds.isEmpty()) {
            return map;
        }

        Set<Integer> distinctIds = new LinkedHashSet<>(orderIds);
        StringBuilder placeholders = new StringBuilder();
        int index = 0;
        for (Integer ignored : distinctIds) {
            if (index++ > 0) {
                placeholders.append(",");
            }
            placeholders.append("?");
        }

        String sql = "SELECT oi.*, pi.file_path AS image_path "
                   + "FROM order_items oi "
                   + "LEFT JOIN product_variants pv ON pv.variant_id = oi.variant_id "
                   + "LEFT JOIN product_images pi ON pi.product_id = pv.product_id AND pi.is_primary = 1 "
                   + "WHERE oi.order_id IN (" + placeholders + ") "
                   + "ORDER BY oi.order_id ASC, oi.order_item_id ASC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            for (Integer orderId : distinctIds) {
                ps.setInt(paramIndex++, orderId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = mapOrderItem(rs);
                    map.computeIfAbsent(item.getOrderId(), key -> new ArrayList<>()).add(item);
                }
            }
        }
        return map;
    }

    private OrderItem mapOrderItem(ResultSet rs) throws SQLException {
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
        item.setPackagingLabelSnapshot(rs.getString("packaging_label_snapshot"));
        item.setPackagingPriceSnapshot(rs.getBigDecimal("packaging_price_snapshot"));
        item.setImagePath(rs.getString("image_path"));
        return item;
    }

    public List<Map<String, Object>> getRevenueTrend(Integer ownerId, String startDate, String endDate, Integer categoryId) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();
        
        if (categoryId != null) {
            sql.append(
                "SELECT CAST(o.created_at AS DATE) AS order_date, SUM(oi.subtotal) AS total_revenue " +
                "FROM orders o " +
                "JOIN order_items oi ON o.order_id = oi.order_id " +
                "JOIN product_variants pv ON oi.variant_id = pv.variant_id " +
                "JOIN products p ON pv.product_id = p.product_id " +
                "WHERE o.status IN ('DELIVERED', 'APPROVED', 'CONFIRMED', 'PREPARING', 'DISPATCHED') AND o.order_type = 'CHILD' "
            );
            if (ownerId != null) {
                sql.append("AND o.owner_id = ? ");
                params.add(ownerId);
            }
            if (startDate != null && !startDate.trim().isEmpty()) {
                sql.append("AND CAST(o.created_at AS DATE) >= ? ");
                params.add(java.sql.Date.valueOf(startDate));
            }
            if (endDate != null && !endDate.trim().isEmpty()) {
                sql.append("AND CAST(o.created_at AS DATE) <= ? ");
                params.add(java.sql.Date.valueOf(endDate));
            }
            sql.append("AND p.category_id = ? ");
            params.add(categoryId);
            sql.append("GROUP BY CAST(o.created_at AS DATE) ORDER BY order_date");
        } else {
            sql.append(
                "SELECT CAST(created_at AS DATE) AS order_date, SUM(final_amount) AS total_revenue " +
                "FROM orders " +
                "WHERE status IN ('DELIVERED', 'APPROVED', 'CONFIRMED', 'PREPARING', 'DISPATCHED') AND order_type = 'CHILD' "
            );
            if (ownerId != null) {
                sql.append("AND owner_id = ? ");
                params.add(ownerId);
            }
            if (startDate != null && !startDate.trim().isEmpty()) {
                sql.append("AND CAST(created_at AS DATE) >= ? ");
                params.add(java.sql.Date.valueOf(startDate));
            }
            if (endDate != null && !endDate.trim().isEmpty()) {
                sql.append("AND CAST(created_at AS DATE) <= ? ");
                params.add(java.sql.Date.valueOf(endDate));
            }
            sql.append("GROUP BY CAST(created_at AS DATE) ORDER BY order_date");
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date", rs.getDate("order_date").toString());
                    map.put("revenue", rs.getBigDecimal("total_revenue"));
                    list.add(map);
                }
            }
        }
        return list;
    }

    public List<Map<String, Object>> getOrderStatusStats(Integer ownerId, String startDate, String endDate, Integer categoryId) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();
        
        if (categoryId != null) {
            sql.append(
                "SELECT o.status, COUNT(DISTINCT o.order_id) AS order_count " +
                "FROM orders o " +
                "JOIN order_items oi ON o.order_id = oi.order_id " +
                "JOIN product_variants pv ON oi.variant_id = pv.variant_id " +
                "JOIN products p ON pv.product_id = p.product_id " +
                "WHERE o.order_type = 'CHILD' "
            );
            if (ownerId != null) {
                sql.append("AND o.owner_id = ? ");
                params.add(ownerId);
            }
            if (startDate != null && !startDate.trim().isEmpty()) {
                sql.append("AND CAST(o.created_at AS DATE) >= ? ");
                params.add(java.sql.Date.valueOf(startDate));
            }
            if (endDate != null && !endDate.trim().isEmpty()) {
                sql.append("AND CAST(o.created_at AS DATE) <= ? ");
                params.add(java.sql.Date.valueOf(endDate));
            }
            sql.append("AND p.category_id = ? ");
            params.add(categoryId);
            sql.append("GROUP BY o.status");
        } else {
            sql.append(
                "SELECT status, COUNT(*) AS order_count " +
                "FROM orders " +
                "WHERE order_type = 'CHILD' "
            );
            if (ownerId != null) {
                sql.append("AND owner_id = ? ");
                params.add(ownerId);
            }
            if (startDate != null && !startDate.trim().isEmpty()) {
                sql.append("AND CAST(created_at AS DATE) >= ? ");
                params.add(java.sql.Date.valueOf(startDate));
            }
            if (endDate != null && !endDate.trim().isEmpty()) {
                sql.append("AND CAST(created_at AS DATE) <= ? ");
                params.add(java.sql.Date.valueOf(endDate));
            }
            sql.append("GROUP BY status");
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", rs.getString("status"));
                    map.put("count", rs.getInt("order_count"));
                    list.add(map);
                }
            }
        }
        return list;
    }

    public List<Map<String, Object>> getCancellationReasonStats(Integer ownerId, String startDate, String endDate, Integer categoryId) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();
        
        if (categoryId != null) {
            sql.append(
                "SELECT COALESCE(o.cancellation_reason, N'KhÃ´ng cÃ³ lÃ½ do') AS reason, COUNT(DISTINCT o.order_id) AS cancel_count " +
                "FROM orders o " +
                "JOIN order_items oi ON o.order_id = oi.order_id " +
                "JOIN product_variants pv ON oi.variant_id = pv.variant_id " +
                "JOIN products p ON pv.product_id = p.product_id " +
                "WHERE o.status = 'CANCELLED' AND o.order_type = 'CHILD' "
            );
            if (ownerId != null) {
                sql.append("AND o.owner_id = ? ");
                params.add(ownerId);
            }
            if (startDate != null && !startDate.trim().isEmpty()) {
                sql.append("AND CAST(o.created_at AS DATE) >= ? ");
                params.add(java.sql.Date.valueOf(startDate));
            }
            if (endDate != null && !endDate.trim().isEmpty()) {
                sql.append("AND CAST(o.created_at AS DATE) <= ? ");
                params.add(java.sql.Date.valueOf(endDate));
            }
            sql.append("AND p.category_id = ? ");
            params.add(categoryId);
            sql.append("GROUP BY o.cancellation_reason ORDER BY cancel_count DESC");
        } else {
            sql.append(
                "SELECT COALESCE(cancellation_reason, N'KhÃ´ng cÃ³ lÃ½ do') AS reason, COUNT(*) AS cancel_count " +
                "FROM orders " +
                "WHERE status = 'CANCELLED' AND order_type = 'CHILD' "
            );
            if (ownerId != null) {
                sql.append("AND owner_id = ? ");
                params.add(ownerId);
            }
            if (startDate != null && !startDate.trim().isEmpty()) {
                sql.append("AND CAST(created_at AS DATE) >= ? ");
                params.add(java.sql.Date.valueOf(startDate));
            }
            if (endDate != null && !endDate.trim().isEmpty()) {
                sql.append("AND CAST(created_at AS DATE) <= ? ");
                params.add(java.sql.Date.valueOf(endDate));
            }
            sql.append("GROUP BY cancellation_reason ORDER BY cancel_count DESC");
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("reason", rs.getString("reason"));
                    map.put("count", rs.getInt("cancel_count"));
                    list.add(map);
                }
            }
        }
        return list;
    }

    public List<Map<String, Object>> getFruitUsageReport(Integer ownerId, String startDate, String endDate, Integer categoryId) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT oi.product_name_snapshot, oi.variant_label_snapshot, " +
            "       SUM(oi.quantity) AS total_quantity, SUM(oi.subtotal) AS total_amount, " +
            "       COUNT(DISTINCT o.order_id) AS order_count "
        );
        
        if (ownerId == null) {
            sql.append(", s.shop_name ");
        }
        
        sql.append(
            "FROM order_items oi " +
            "JOIN orders o ON oi.order_id = o.order_id "
        );
        
        if (ownerId == null) {
            sql.append("LEFT JOIN shop_owner_profiles s ON o.owner_id = s.user_id ");
        }
        
        if (categoryId != null) {
            sql.append("JOIN product_variants pv ON oi.variant_id = pv.variant_id ");
            sql.append("JOIN products p ON pv.product_id = p.product_id ");
        }
        
        sql.append("WHERE o.status IN ('DELIVERED', 'APPROVED', 'CONFIRMED', 'PREPARING', 'DISPATCHED') AND o.order_type = 'CHILD' ");
        
        List<Object> params = new ArrayList<>();
        if (ownerId != null) {
            sql.append("AND o.owner_id = ? ");
            params.add(ownerId);
        }
        if (startDate != null && !startDate.trim().isEmpty()) {
            sql.append("AND CAST(o.created_at AS DATE) >= ? ");
            params.add(java.sql.Date.valueOf(startDate));
        }
        if (endDate != null && !endDate.trim().isEmpty()) {
            sql.append("AND CAST(o.created_at AS DATE) <= ? ");
            params.add(java.sql.Date.valueOf(endDate));
        }
        if (categoryId != null) {
            sql.append("AND p.category_id = ? ");
            params.add(categoryId);
        }
        
        sql.append("GROUP BY oi.product_name_snapshot, oi.variant_label_snapshot ");
        if (ownerId == null) {
            sql.append(", s.shop_name ");
        }
        sql.append("ORDER BY total_quantity DESC");

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("productName", rs.getString("product_name_snapshot"));
                    map.put("variantLabel", rs.getString("variant_label_snapshot"));
                    map.put("totalQuantity", rs.getInt("total_quantity"));
                    map.put("totalAmount", rs.getBigDecimal("total_amount"));
                    map.put("orderCount", rs.getInt("order_count"));
                    if (ownerId == null) {
                        map.put("shopName", rs.getString("shop_name") != null ? rs.getString("shop_name") : "Há»‡ thá»‘ng");
                    }
                    list.add(map);
                }
            }
        }
        return list;
    }

    public List<Order> findChildrenByParentId(int parentOrderId) throws SQLException {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE parent_order_id = ? ORDER BY order_id ASC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, parentOrderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Batch load orders theo danh sÃ¡ch order_id Ä‘á»ƒ trÃ¡nh N+1.
     * Tráº£ vá» Map<orderId, Order> â€” cÃ¡c id khÃ´ng tá»“n táº¡i sáº½ khÃ´ng cÃ³ trong map.
     */
    public Map<Integer, Order> findByIds(Collection<Integer> orderIds) throws SQLException {
        Map<Integer, Order> map = new LinkedHashMap<>();
        if (orderIds == null || orderIds.isEmpty()) {
            return map;
        }

        Set<Integer> distinctIds = new LinkedHashSet<>(orderIds);
        String placeholders = String.join(",", java.util.Collections.nCopies(distinctIds.size(), "?"));
        String sql = "SELECT * FROM orders WHERE order_id IN (" + placeholders + ")";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            for (Integer id : distinctIds) {
                ps.setInt(paramIndex++, id);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order o = mapRow(rs);
                    map.put(o.getOrderId(), o);
                }
            }
        }
        return map;
    }

    public void updateReceivedStatus(int orderId, String receivedStatus) throws SQLException {
        String sql = "UPDATE orders SET received_status = ?, updated_at = GETDATE() WHERE order_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, receivedStatus);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        }
    }

    /**
     * SEC-01 â€” Count FAILED deliveries for a customer in the last {@code days} days.
     * Used by OrderService.isCodEligible() to gate COD payment eligibility.
     */
    public int countRecentFailedDeliveries(int customerId, int days) throws SQLException {
        String sql = "SELECT COUNT(*) FROM deliveries d "
                   + "JOIN orders o ON d.order_id = o.order_id "
                   + "WHERE o.customer_id = ? "
                   + "  AND d.status = 'FAILED' "
                   + "  AND d.updated_at >= DATEADD(day, ?, GETDATE())";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setInt(2, -days);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * INV-01 â€” Find orders in PENDING_PAYMENT whose created_at is older than
     * {@code minutes} minutes. Used by AutoCancelUnpaidListener to release
     * reserved stock and cancel unpaid orders.
     */
    public List<Order> findExpiredPendingPayment(int minutes) throws SQLException {
        String sql = "SELECT * FROM orders "
                   + "WHERE status = 'PENDING_PAYMENT' "
                   + "  AND created_at < DATEADD(minute, ?, GETDATE())";
        List<Order> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, -minutes);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }
}
