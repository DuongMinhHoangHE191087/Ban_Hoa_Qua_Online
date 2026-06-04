package com.fruitmkt.dao;

import com.fruitmkt.dao.base.BaseDAO;
import com.fruitmkt.model.entity.ShopSettlement;
import java.sql.*;
import java.util.*;

public class SettlementDAO extends BaseDAO {

    public List<ShopSettlement> findAll(String status, int page, int pageSize) throws SQLException {
        List<ShopSettlement> list = new ArrayList<>();
        int offset = (page - 1) * pageSize;
        StringBuilder sql = new StringBuilder("SELECT * FROM shop_settlements ");
        List<Object> params = new ArrayList<>();
        if (status != null && !status.trim().isEmpty()) {
            sql.append("WHERE status = ? ");
            params.add(status);
        }
        sql.append("ORDER BY settlement_id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
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
