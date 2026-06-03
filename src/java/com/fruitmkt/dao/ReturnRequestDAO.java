package com.fruitmkt.dao;

import com.fruitmkt.dao.base.BaseDAO;
import com.fruitmkt.model.entity.ReturnRequest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ReturnRequestDAO — DAO cho entity ReturnRequest.
 */
public class ReturnRequestDAO extends BaseDAO {

    public ReturnRequest findById(int id) throws SQLException {
        String sql = "SELECT r.*, oi.product_name_snapshot, oi.variant_label_snapshot "
                   + "FROM return_requests r "
                   + "LEFT JOIN order_items oi ON r.order_item_id = oi.order_item_id "
                   + "WHERE r.return_request_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<ReturnRequest> findByOrder(int orderId) throws SQLException {
        String sql = "SELECT r.*, oi.product_name_snapshot, oi.variant_label_snapshot "
                   + "FROM return_requests r "
                   + "LEFT JOIN order_items oi ON r.order_item_id = oi.order_item_id "
                   + "WHERE r.order_id = ? ORDER BY r.created_at DESC";
        List<ReturnRequest> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<ReturnRequest> findByCustomer(int customerId) throws SQLException {
        String sql = "SELECT r.*, oi.product_name_snapshot, oi.variant_label_snapshot "
                   + "FROM return_requests r "
                   + "LEFT JOIN order_items oi ON r.order_item_id = oi.order_item_id "
                   + "WHERE r.customer_id = ? ORDER BY r.created_at DESC";
        List<ReturnRequest> list = new ArrayList<>();
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

    public List<ReturnRequest> findPending() throws SQLException {
        String sql = "SELECT r.*, oi.product_name_snapshot, oi.variant_label_snapshot "
                   + "FROM return_requests r "
                   + "LEFT JOIN order_items oi ON r.order_item_id = oi.order_item_id "
                   + "WHERE r.status = 'REQUESTED' ORDER BY r.created_at DESC";
        List<ReturnRequest> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<ReturnRequest> findAll() throws SQLException {
        String sql = "SELECT r.*, oi.product_name_snapshot, oi.variant_label_snapshot "
                   + "FROM return_requests r "
                   + "LEFT JOIN order_items oi ON r.order_item_id = oi.order_item_id "
                   + "ORDER BY r.created_at DESC";
        List<ReturnRequest> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<ReturnRequest> findAll(String status, int page, int pageSize) throws SQLException {
        List<ReturnRequest> list = new ArrayList<>();
        int offset = (page - 1) * pageSize;
        if (offset < 0) offset = 0;
        
        StringBuilder sql = new StringBuilder("SELECT r.*, oi.product_name_snapshot, oi.variant_label_snapshot ")
                .append("FROM return_requests r ")
                .append("LEFT JOIN order_items oi ON r.order_item_id = oi.order_item_id ");
        List<Object> params = new ArrayList<>();
        if (status != null && !status.trim().isEmpty()) {
            sql.append("WHERE r.status = ? ");
            params.add(status);
        }
        sql.append("ORDER BY r.created_at DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
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
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM return_requests ");
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

    public List<ReturnRequest> findByOwner(int ownerId) throws SQLException {
        String sql = "SELECT r.*, oi.product_name_snapshot, oi.variant_label_snapshot "
                   + "FROM return_requests r "
                   + "JOIN orders o ON r.order_id = o.order_id "
                   + "LEFT JOIN order_items oi ON r.order_item_id = oi.order_item_id "
                   + "WHERE o.owner_id = ? "
                   + "ORDER BY r.created_at DESC";
        List<ReturnRequest> list = new ArrayList<>();
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

    public int save(ReturnRequest req) throws SQLException {
        String sql = "INSERT INTO return_requests (order_id, order_item_id, customer_id, request_type, reason_code, description, evidence_url, requested_quantity, resolution_type, replacement_variant_id, refund_amount, status, decided_by, decision_reason, resolved_at, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), GETDATE())";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, req.getOrderId());
            if (req.getOrderItemId() == null) {
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(2, req.getOrderItemId());
            }
            ps.setInt(3, req.getCustomerId());
            ps.setString(4, req.getRequestType());
            ps.setString(5, req.getReasonCode());
            ps.setString(6, req.getDescription());
            ps.setString(7, req.getEvidenceUrl());
            ps.setInt(8, req.getRequestedQuantity());
            ps.setString(9, req.getResolutionType());
            if (req.getReplacementVariantId() == null) {
                ps.setNull(10, Types.INTEGER);
            } else {
                ps.setInt(10, req.getReplacementVariantId());
            }
            ps.setBigDecimal(11, req.getRefundAmount() != null ? req.getRefundAmount() : java.math.BigDecimal.ZERO);
            ps.setString(12, req.getStatus() != null ? req.getStatus() : "REQUESTED");
            if (req.getDecidedBy() == null) {
                ps.setNull(13, Types.INTEGER);
            } else {
                ps.setInt(13, req.getDecidedBy());
            }
            ps.setString(14, req.getDecisionReason());
            if (req.getResolvedAt() == null) {
                ps.setNull(15, Types.TIMESTAMP);
            } else {
                ps.setTimestamp(15, Timestamp.valueOf(req.getResolvedAt()));
            }

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        }
        return 0;
    }

    public void updateStatus(int id, String status, String decisionReason, int decidedBy) throws SQLException {
        String sql = "UPDATE return_requests SET status = ?, decision_reason = ?, decided_by = ?, resolved_at = GETDATE(), updated_at = GETDATE() WHERE return_request_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, decisionReason);
            ps.setInt(3, decidedBy);
            ps.setInt(4, id);
            ps.executeUpdate();
        }
    }

    private ReturnRequest mapRow(ResultSet rs) throws SQLException {
        ReturnRequest req = new ReturnRequest();
        req.setReturnRequestId(rs.getInt("return_request_id"));
        req.setOrderId(rs.getInt("order_id"));
        int orderItemId = rs.getInt("order_item_id");
        req.setOrderItemId(rs.wasNull() ? null : orderItemId);
        req.setCustomerId(rs.getInt("customer_id"));
        req.setRequestType(rs.getString("request_type"));
        req.setReasonCode(rs.getString("reason_code"));
        req.setDescription(rs.getString("description"));
        req.setEvidenceUrl(rs.getString("evidence_url"));
        req.setRequestedQuantity(rs.getInt("requested_quantity"));
        req.setResolutionType(rs.getString("resolution_type"));
        int replacementVariantId = rs.getInt("replacement_variant_id");
        req.setReplacementVariantId(rs.wasNull() ? null : replacementVariantId);
        req.setRefundAmount(rs.getBigDecimal("refund_amount"));
        req.setStatus(rs.getString("status"));
        int decidedBy = rs.getInt("decided_by");
        req.setDecidedBy(rs.wasNull() ? null : decidedBy);
        req.setDecisionReason(rs.getString("decision_reason"));
        
        Timestamp resolvedAtTs = rs.getTimestamp("resolved_at");
        if (resolvedAtTs != null) {
            req.setResolvedAt(resolvedAtTs.toLocalDateTime());
        }
        Timestamp createdAtTs = rs.getTimestamp("created_at");
        if (createdAtTs != null) {
            req.setCreatedAt(createdAtTs.toLocalDateTime());
        }
        Timestamp updatedAtTs = rs.getTimestamp("updated_at");
        if (updatedAtTs != null) {
            req.setUpdatedAt(updatedAtTs.toLocalDateTime());
        }

        // Map transient fields
        try {
            req.setProductName(rs.getString("product_name_snapshot"));
            req.setVariantLabel(rs.getString("variant_label_snapshot"));
        } catch (SQLException ignored) {}

        return req;
    }
}
