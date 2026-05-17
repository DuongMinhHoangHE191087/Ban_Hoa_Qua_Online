package com.fruitmkt.dao;

import com.fruitmkt.dao.base.BaseDAO;
import com.fruitmkt.model.entity.ProductVariant;
import java.sql.*;
import java.util.*;

/**
 * ProductVariantDAO — DAO cho entity ProductVariant.
 *
 * QUY TẮC:
 *   - Chỉ chứa SQL, không chứa business logic
 *   - Dùng PreparedStatement, KHÔNG nối chuỗi SQL
 *   - Mỗi method ném SQLException để Service xử lý
 *   - Dùng try-with-resources cho Connection + PreparedStatement
 *
 * @author fruitmkt-team
 */
public class ProductVariantDAO extends BaseDAO {

    /**
     * TODO: Implement — findById(int id)
     */
    public List<ProductVariant> findById(int id) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findById(int id)");
    }

    /**
     * TODO: Implement — findByProduct(int productId)
     */
    public List<ProductVariant> findByProduct(int productId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findByProduct(int productId)");
    }

    /**
     * TODO: Implement — findBySku(String sku)
     */
    public List<ProductVariant> findBySku(String sku) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findBySku(String sku)");
    }

    /**
     * TODO: Implement — save(ProductVariant variant)
     */
    public int save(ProductVariant variant) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: save(ProductVariant variant)");
    }

    /**
     * TODO: Implement — update(ProductVariant variant)
     */
    public void update(ProductVariant variant) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: update(ProductVariant variant)");
    }

    /**
     * TODO: Implement — updateStock(int variantId, int delta)
     */
    public void updateStock(int variantId, int delta) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: updateStock(int variantId, int delta)");
    }

    /**
     * TODO: Implement — deactivate(int variantId)
     */
    public void deactivate(int variantId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: deactivate(int variantId)");
    }

    /** Ánh xạ ResultSet -> ProductVariant — gọi trong mọi query SELECT */
    private ProductVariant mapRow(ResultSet rs) throws SQLException {
        // TODO: rs.getInt(), rs.getString()... theo Schema.sql
        throw new UnsupportedOperationException("mapRow not implemented");
    }
}
