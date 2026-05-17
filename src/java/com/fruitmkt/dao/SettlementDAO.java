package com.fruitmkt.dao;

import com.fruitmkt.dao.base.BaseDAO;
import com.fruitmkt.model.entity.ShopSettlement;
import java.sql.*;
import java.util.*;

/**
 * SettlementDAO — DAO cho entity ShopSettlement.
 *
 * QUY TẮC:
 *   - Chỉ chứa SQL, không chứa business logic
 *   - Dùng PreparedStatement, KHÔNG nối chuỗi SQL
 *   - Mỗi method ném SQLException để Service xử lý
 *   - Dùng try-with-resources cho Connection + PreparedStatement
 *
 * @author fruitmkt-team
 */
public class SettlementDAO extends BaseDAO {

    /**
     * TODO: Implement — findById(int id)
     */
    public List<ShopSettlement> findById(int id) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findById(int id)");
    }

    /**
     * TODO: Implement — findByOwner(int ownerId)
     */
    public List<ShopSettlement> findByOwner(int ownerId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findByOwner(int ownerId)");
    }

    /**
     * TODO: Implement — findAll(String status)
     */
    public List<ShopSettlement> findAll(String status) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findAll(String status)");
    }

    /**
     * TODO: Implement — save(ShopSettlement settlement)
     */
    public int save(ShopSettlement settlement) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: save(ShopSettlement settlement)");
    }

    /**
     * TODO: Implement — confirm(int settlementId, int confirmedBy)
     */
    public void confirm(int settlementId, int confirmedBy) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: confirm(int settlementId, int confirmedBy)");
    }

    /**
     * TODO: Implement — markPaid(int settlementId)
     */
    public void markPaid(int settlementId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: markPaid(int settlementId)");
    }

    /** Ánh xạ ResultSet -> ShopSettlement — gọi trong mọi query SELECT */
    private ShopSettlement mapRow(ResultSet rs) throws SQLException {
        // TODO: rs.getInt(), rs.getString()... theo Schema.sql
        throw new UnsupportedOperationException("mapRow not implemented");
    }
}
