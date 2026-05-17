package com.fruitmkt.dao;

import com.fruitmkt.dao.base.BaseDAO;
import com.fruitmkt.model.entity.Promotion;
import java.sql.*;
import java.util.*;

/**
 * PromotionDAO — DAO cho entity Promotion.
 *
 * QUY TẮC:
 *   - Chỉ chứa SQL, không chứa business logic
 *   - Dùng PreparedStatement, KHÔNG nối chuỗi SQL
 *   - Mỗi method ném SQLException để Service xử lý
 *   - Dùng try-with-resources cho Connection + PreparedStatement
 *
 * @author fruitmkt-team
 */
public class PromotionDAO extends BaseDAO {

    /**
     * TODO: Implement — findById(int id)
     */
    public List<Promotion> findById(int id) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findById(int id)");
    }

    /**
     * TODO: Implement — findByCode(String code)
     */
    public List<Promotion> findByCode(String code) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findByCode(String code)");
    }

    /**
     * TODO: Implement — findByOwner(int ownerId)
     */
    public List<Promotion> findByOwner(int ownerId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findByOwner(int ownerId)");
    }

    /**
     * TODO: Implement — save(Promotion promo)
     */
    public int save(Promotion promo) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: save(Promotion promo)");
    }

    /**
     * TODO: Implement — update(Promotion promo)
     */
    public void update(Promotion promo) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: update(Promotion promo)");
    }

    /**
     * TODO: Implement — incrementUsedCount(int promoId)
     */
    public void incrementUsedCount(int promoId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: incrementUsedCount(int promoId)");
    }

    /**
     * TODO: Implement — deactivate(int promoId)
     */
    public void deactivate(int promoId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: deactivate(int promoId)");
    }

    /** Ánh xạ ResultSet -> Promotion — gọi trong mọi query SELECT */
    private Promotion mapRow(ResultSet rs) throws SQLException {
        // TODO: rs.getInt(), rs.getString()... theo Schema.sql
        throw new UnsupportedOperationException("mapRow not implemented");
    }
}
