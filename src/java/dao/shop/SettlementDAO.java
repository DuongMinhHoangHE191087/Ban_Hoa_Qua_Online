package dao.shop;
import dao.system.BaseDAO;
import model.entity.shop.ShopSettlement;
import util.LoggerUtil;
import util.PaginationHelper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class SettlementDAO extends BaseDAO {

    private static final Logger log = Logger.getLogger(SettlementDAO.class.getName());

    public List<ShopSettlement> findAll(String status, int page, int pageSize) throws SQLException {
        List<ShopSettlement> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM shop_settlements ");
        List<Object> params = new ArrayList<>();
        if (status != null && !status.trim().isEmpty()) {
            sql.append("WHERE status = ? ");
            params.add(status);
        }
        sql.append("ORDER BY settlement_id DESC ").append(PaginationHelper.OFFSET_FETCH_SQL);
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

    public int countAll(String status) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM shop_settlements ");
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

    public void markPaid(int settlementId) throws SQLException {
        String sql = "UPDATE shop_settlements SET status = 'PAID', paid_at = GETDATE() WHERE settlement_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, settlementId);
            ps.executeUpdate();
        }
    }

    public List<ShopSettlement> findByOwner(int ownerId) throws SQLException {
        List<ShopSettlement> list = new ArrayList<>();
        String sql = "SELECT * FROM shop_settlements WHERE owner_id = ? ORDER BY settlement_id DESC";
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

    /**
     * PAY-03 — Auto-settlement using an hour-based freeze window.
     * Moves eligible delivered orders to settlement after {@code freezeHours} hours
     * have elapsed since delivery. Keeps 5% commission deduction (FIN-04) intact via
     * the platform_fee already stored on the order row.
     */
    public int runAutoSettlementByHours(int freezeHours) throws SQLException {
        return runAutoSettlementInternal(freezeHours, "hour");
    }

    /** Legacy day-based variant — retained for backward compatibility. */
    public int runAutoSettlement(int freezeDays) throws SQLException {
        return runAutoSettlementInternal(freezeDays, "day");
    }

    private int runAutoSettlementInternal(int freezeValue, String datepart) throws SQLException {
        int settlementsCreated = 0;

        List<Integer> owners = new ArrayList<>();
        String getOwnersSql = "SELECT DISTINCT o.owner_id "
                            + "FROM orders o "
                            + "LEFT JOIN deliveries d ON d.order_id = o.order_id "
                            + "WHERE o.status = 'DELIVERED' "
                            + "  AND NOT EXISTS (SELECT 1 FROM shop_settlement_orders sso WHERE sso.order_id = o.order_id) "
                            + "  AND NOT EXISTS (SELECT 1 FROM return_requests r WHERE r.order_id = o.order_id AND r.status IN ('REQUESTED', 'PROCESSING', 'APPROVED')) "
                            + "  AND COALESCE(d.delivered_at, o.updated_at) < DATEADD(" + datepart + ", ?, GETDATE())";
                            
        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(getOwnersSql)) {
                ps.setInt(1, -freezeValue);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        owners.add(rs.getInt("owner_id"));
                    }
                }
            }

            if (owners.isEmpty()) {
                return 0;
            }

            conn.setAutoCommit(false);

            try {
                String getOrdersSql = "SELECT o.order_id, o.final_amount, o.platform_fee, o.discount_amount, o.created_at, "
                                    + "  COALESCE((SELECT SUM(r.refund_amount) FROM return_requests r WHERE r.order_id = o.order_id AND r.status = 'COMPLETED'), 0) AS refund_amount "
                                    + "FROM orders o "
                                    + "LEFT JOIN deliveries d ON d.order_id = o.order_id "
                                    + "WHERE o.owner_id = ? "
                                    + "  AND o.status = 'DELIVERED' "
                                    + "  AND NOT EXISTS (SELECT 1 FROM shop_settlement_orders sso WHERE sso.order_id = o.order_id) "
                                    + "  AND NOT EXISTS (SELECT 1 FROM return_requests r WHERE r.order_id = o.order_id AND r.status IN ('REQUESTED', 'PROCESSING', 'APPROVED')) "
                                    + "  AND COALESCE(d.delivered_at, o.updated_at) < DATEADD(" + datepart + ", ?, GETDATE())";
                                    
                String insertSettlementSql = "INSERT INTO shop_settlements (owner_id, period_start, period_end, gross_amount, platform_fee_amount, refund_amount, adjustment_amount, net_amount, status, calculated_at, created_by, note) "
                                           + "VALUES (?, ?, ?, ?, ?, ?, 0, ?, 'PENDING', GETDATE(), 1, ?)";
                                           
                String insertSettlementOrderSql = "INSERT INTO shop_settlement_orders (settlement_id, order_id, order_amount, platform_fee_amount, discount_amount, refund_amount, net_amount) "
                                                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
                                                
                try (PreparedStatement psOrders = conn.prepareStatement(getOrdersSql);
                     PreparedStatement psInsertSettle = conn.prepareStatement(insertSettlementSql, Statement.RETURN_GENERATED_KEYS);
                     PreparedStatement psInsertSettleOrder = conn.prepareStatement(insertSettlementOrderSql)) {
                     
                    for (int ownerId : owners) {
                        psOrders.setInt(1, ownerId);
                        psOrders.setInt(2, -freezeValue);
                        
                        List<Map<String, Object>> ordersList = new ArrayList<>();
                        java.sql.Date periodStart = null;
                        java.sql.Date periodEnd = null;
                        
                        java.math.BigDecimal gross = java.math.BigDecimal.ZERO;
                        java.math.BigDecimal platformFee = java.math.BigDecimal.ZERO;
                        java.math.BigDecimal refund = java.math.BigDecimal.ZERO;
                        
                        try (ResultSet rsOrders = psOrders.executeQuery()) {
                            while (rsOrders.next()) {
                                Map<String, Object> orderData = new HashMap<>();
                                int orderId = rsOrders.getInt("order_id");
                                java.math.BigDecimal finalAmount = rsOrders.getBigDecimal("final_amount");
                                java.math.BigDecimal pFee = rsOrders.getBigDecimal("platform_fee");
                                java.math.BigDecimal disc = rsOrders.getBigDecimal("discount_amount");
                                java.math.BigDecimal ref = rsOrders.getBigDecimal("refund_amount");
                                java.sql.Date orderDate = rsOrders.getDate("created_at");
                                
                                orderData.put("order_id", orderId);
                                orderData.put("final_amount", finalAmount);
                                orderData.put("platform_fee", pFee);
                                orderData.put("discount_amount", disc);
                                orderData.put("refund_amount", ref);
                                
                                ordersList.add(orderData);
                                
                                gross = gross.add(finalAmount != null ? finalAmount : java.math.BigDecimal.ZERO);
                                platformFee = platformFee.add(pFee != null ? pFee : java.math.BigDecimal.ZERO);
                                refund = refund.add(ref != null ? ref : java.math.BigDecimal.ZERO);
                                
                                if (orderDate != null) {
                                    if (periodStart == null || orderDate.before(periodStart)) {
                                        periodStart = orderDate;
                                    }
                                    if (periodEnd == null || orderDate.after(periodEnd)) {
                                        periodEnd = orderDate;
                                    }
                                }
                            }
                        }
                        
                        if (ordersList.isEmpty()) {
                            continue;
                        }
                        
                        if (periodStart == null) periodStart = new java.sql.Date(System.currentTimeMillis());
                        if (periodEnd == null) periodEnd = new java.sql.Date(System.currentTimeMillis());
                        
                        java.math.BigDecimal net = gross.subtract(platformFee).subtract(refund);
                        
                        String note = String.format("Quyết toán tự động hệ thống cho %d đơn hàng giao hàng thành công.", ordersList.size());
                        
                        psInsertSettle.setInt(1, ownerId);
                        psInsertSettle.setDate(2, periodStart);
                        psInsertSettle.setDate(3, periodEnd);
                        psInsertSettle.setBigDecimal(4, gross);
                        psInsertSettle.setBigDecimal(5, platformFee);
                        psInsertSettle.setBigDecimal(6, refund);
                        psInsertSettle.setBigDecimal(7, net);
                        psInsertSettle.setString(8, note);
                        
                        psInsertSettle.executeUpdate();
                        int settlementId = -1;
                        try (ResultSet gk = psInsertSettle.getGeneratedKeys()) {
                            if (gk.next()) {
                                settlementId = gk.getInt(1);
                            }
                        }
                        
                        if (settlementId == -1) {
                            throw new SQLException("Cannot retrieve generated key for shop_settlements.");
                        }
                        
                        for (Map<String, Object> orderData : ordersList) {
                            int orderId = (int) orderData.get("order_id");
                            java.math.BigDecimal finalAmount = (java.math.BigDecimal) orderData.get("final_amount");
                            java.math.BigDecimal pFee = (java.math.BigDecimal) orderData.get("platform_fee");
                            java.math.BigDecimal disc = (java.math.BigDecimal) orderData.get("discount_amount");
                            java.math.BigDecimal ref = (java.math.BigDecimal) orderData.get("refund_amount");
                            
                            java.math.BigDecimal netOrder = finalAmount.subtract(pFee).subtract(ref);
                            
                            psInsertSettleOrder.setInt(1, settlementId);
                            psInsertSettleOrder.setInt(2, orderId);
                            psInsertSettleOrder.setBigDecimal(3, finalAmount);
                            psInsertSettleOrder.setBigDecimal(4, pFee);
                            psInsertSettleOrder.setBigDecimal(5, disc);
                            psInsertSettleOrder.setBigDecimal(6, ref);
                            psInsertSettleOrder.setBigDecimal(7, netOrder);
                            psInsertSettleOrder.addBatch();
                        }
                        psInsertSettleOrder.executeBatch();
                        
                        settlementsCreated++;
                        
                        try {
                            service.chat.NotificationService notifService = new service.chat.NotificationService();
                            notifService.send(ownerId, "PAYMENT", "Đối soát tự động mới", 
                                String.format("Hệ thống đã tự động chốt kỳ đối soát mới #%d với doanh thu gộp: %,.0f đ, thực nhận: %,.0f đ. Vui lòng kiểm tra chi tiết.", 
                                settlementId, gross.doubleValue(), net.doubleValue()), "/shop/settlement");
                        } catch (Exception e) {
                            LoggerUtil.warn(log, "Failed to send auto-settlement notification to shop owner " + ownerId, e);
                        }
                    }
                }
                
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
        
        return settlementsCreated;
    }

    public ShopSettlement findById(int settlementId) throws SQLException {
        String sql = "SELECT * FROM shop_settlements WHERE settlement_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, settlementId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<model.entity.shop.ShopSettlementOrder> findOrdersBySettlementId(int settlementId) throws SQLException {
        List<model.entity.shop.ShopSettlementOrder> list = new ArrayList<>();
        String sql = "SELECT * FROM shop_settlement_orders WHERE settlement_id = ? ORDER BY settlement_order_id ASC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, settlementId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.entity.shop.ShopSettlementOrder sso = new model.entity.shop.ShopSettlementOrder();
                    sso.setSettlementOrderId(rs.getInt("settlement_order_id"));
                    sso.setSettlementId(rs.getInt("settlement_id"));
                    sso.setOrderId(rs.getInt("order_id"));
                    sso.setOrderAmount(rs.getBigDecimal("order_amount"));
                    sso.setPlatformFeeAmount(rs.getBigDecimal("platform_fee_amount"));
                    sso.setDiscountAmount(rs.getBigDecimal("discount_amount"));
                    sso.setRefundAmount(rs.getBigDecimal("refund_amount"));
                    sso.setNetAmount(rs.getBigDecimal("net_amount"));
                    list.add(sso);
                }
            }
        }
        return list;
    }

    private ShopSettlement mapRow(ResultSet rs) throws SQLException {
        ShopSettlement s = new ShopSettlement();
        s.setSettlementId(rs.getInt("settlement_id"));
        s.setOwnerId(rs.getInt("owner_id"));
        s.setPeriodStart(rs.getDate("period_start") != null ? rs.getDate("period_start").toLocalDate() : null);
        s.setPeriodEnd(rs.getDate("period_end") != null ? rs.getDate("period_end").toLocalDate() : null);
        s.setGrossAmount(rs.getBigDecimal("gross_amount"));
        s.setPlatformFeeAmount(rs.getBigDecimal("platform_fee_amount"));
        s.setRefundAmount(rs.getBigDecimal("refund_amount"));
        s.setAdjustmentAmount(rs.getBigDecimal("adjustment_amount"));
        s.setNetAmount(rs.getBigDecimal("net_amount"));
        s.setStatus(rs.getString("status"));
        
        Timestamp calcAt = rs.getTimestamp("calculated_at");
        if (calcAt != null) s.setCalculatedAt(calcAt.toLocalDateTime());
        
        Timestamp confAt = rs.getTimestamp("confirmed_at");
        if (confAt != null) s.setConfirmedAt(confAt.toLocalDateTime());
        
        Timestamp paidAt = rs.getTimestamp("paid_at");
        if (paidAt != null) s.setPaidAt(paidAt.toLocalDateTime());
        
        s.setCreatedBy(rs.getInt("created_by"));
        s.setNote(rs.getString("note"));
        return s;
    }
}
