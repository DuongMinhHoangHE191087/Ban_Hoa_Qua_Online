package com.fruitmkt.dao;

import com.fruitmkt.dao.base.BaseDAO;
import com.fruitmkt.model.entity.Review;
import java.sql.*;
import java.util.*;

/**
 * ReviewDAO — DAO cho entity Review.
 *
 * QUY TẮC:
 *   - Chỉ chứa SQL, không chứa business logic
 *   - Dùng PreparedStatement, KHÔNG nối chuỗi SQL
 *   - Mỗi method ném SQLException để Service xử lý
 *   - Dùng try-with-resources cho Connection + PreparedStatement
 *
 * @author fruitmkt-team
 */
public class ReviewDAO extends BaseDAO {

    /**
     * TODO: Implement — findByOrderItem(int orderItemId)
     */
    public List<Review> findByOrderItem(int orderItemId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findByOrderItem(int orderItemId)");
    }

    /**
     * TODO: Implement — findByProduct(int productId)
     */
    public List<Review> findByProduct(int productId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findByProduct(int productId)");
    }

    /**
     * TODO: Implement — existsByCustomerAndItem(int customerId, int orderItemId)
     */
    public boolean existsByCustomerAndItem(int customerId, int orderItemId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: existsByCustomerAndItem(int customerId, int orderItemId)");
    }

    /**
     * TODO: Implement — save(Review review)
     */
    public int save(Review review) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: save(Review review)");
    }

    /**
     * TODO: Implement — hide(int reviewId)
     */
    public void hide(int reviewId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: hide(int reviewId)");
    }

    /** Ánh xạ ResultSet -> Review — gọi trong mọi query SELECT */
    private Review mapRow(ResultSet rs) throws SQLException {
        // TODO: rs.getInt(), rs.getString()... theo Schema.sql
        throw new UnsupportedOperationException("mapRow not implemented");
    }
}
