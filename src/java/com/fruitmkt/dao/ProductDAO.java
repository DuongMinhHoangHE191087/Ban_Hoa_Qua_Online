package com.fruitmkt.dao;

import com.fruitmkt.dao.base.BaseDAO;
import com.fruitmkt.model.entity.Product;
import java.sql.*;
import java.util.*;

/**
 * ProductDAO — DAO cho entity Product.
 *
 * QUY TẮC:
 *   - Chỉ chứa SQL, không chứa business logic
 *   - Dùng PreparedStatement, KHÔNG nối chuỗi SQL
 *   - Mỗi method ném SQLException để Service xử lý
 *   - Dùng try-with-resources cho Connection + PreparedStatement
 *
 * @author fruitmkt-team
 */
public class ProductDAO extends BaseDAO {

    /**
     * TODO: Implement — findById(int id)
     */
    public List<Product> findById(int id) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findById(int id)");
    }

    /**
     * TODO: Implement — findAll(int page, int pageSize)
     */
    public List<Product> findAll(int page, int pageSize) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findAll(int page, int pageSize)");
    }

    /**
     * TODO: Implement — findByOwner(int ownerId)
     */
    public List<Product> findByOwner(int ownerId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findByOwner(int ownerId)");
    }

    /**
     * TODO: Implement — findByCategory(int categoryId, int page, int pageSize)
     */
    public List<Product> findByCategory(int categoryId, int page, int pageSize) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findByCategory(int categoryId, int page, int pageSize)");
    }

    /**
     * TODO: Implement — search(String keyword, Integer categoryId, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice, int page, int pageSize)
     */
    public Product search(String keyword, Integer categoryId, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice, int page, int pageSize) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: search(String keyword, Integer categoryId, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice, int page, int pageSize)");
    }

    /**
     * TODO: Implement — save(Product product)
     */
    public int save(Product product) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: save(Product product)");
    }

    /**
     * TODO: Implement — update(Product product)
     */
    public void update(Product product) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: update(Product product)");
    }

    /**
     * TODO: Implement — updateStatus(int productId, String status)
     */
    public void updateStatus(int productId, String status) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: updateStatus(int productId, String status)");
    }

    /**
     * TODO: Implement — incrementViewCount(int productId)
     */
    public void incrementViewCount(int productId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: incrementViewCount(int productId)");
    }

    /** Ánh xạ ResultSet -> Product — gọi trong mọi query SELECT */
    private Product mapRow(ResultSet rs) throws SQLException {
        // TODO: rs.getInt(), rs.getString()... theo Schema.sql
        throw new UnsupportedOperationException("mapRow not implemented");
    }
}
