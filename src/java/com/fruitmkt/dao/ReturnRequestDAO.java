package com.fruitmkt.dao;

import com.fruitmkt.dao.base.BaseDAO;
import com.fruitmkt.model.entity.ReturnRequest;
import java.sql.*;
import java.util.*;

public class ReturnRequestDAO extends BaseDAO {

    public List<ReturnRequest> findAll(String status, int page, int pageSize) throws SQLException {
        List<ReturnRequest> list = new ArrayList<>();
        int offset = (page - 1) * pageSize;
        StringBuilder sql = new StringBuilder("SELECT * FROM return_requests ");
        List<Object> params = new ArrayList<>();
        if (status != null && !status.trim().isEmpty()) {
            sql.append("WHERE status = ? ");
            params.add(status);
        }
        sql.append("ORDER BY return_request_id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
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
        ReturnRequest r = new ReturnRequest();
        r.setReturnRequestId(rs.getInt("return_request_id"));
        r.setOrderId(rs.getInt("order_id"));
        r.setCustomerId(rs.getInt("customer_id"));
        r.setReasonCode(rs.getString("reason_code"));
        r.setDescription(rs.getString("description"));
        r.setEvidenceUrl(rs.getString("evidence_url"));
        r.setRefundAmount(rs.getBigDecimal("refund_amount"));
        r.setStatus(rs.getString("status"));
        
        Timestamp decidedAt = rs.getTimestamp("resolved_at");
        if (decidedAt != null) r.setResolvedAt(decidedAt.toLocalDateTime());
        
        r.setDecidedBy(rs.getInt("decided_by"));
        r.setDecisionReason(rs.getString("decision_reason"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) r.setCreatedAt(createdAt.toLocalDateTime());
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) r.setUpdatedAt(updatedAt.toLocalDateTime());
        
        return r;
    }
}
