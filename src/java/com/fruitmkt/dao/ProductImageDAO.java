package com.fruitmkt.dao;

import com.fruitmkt.dao.base.BaseDAO;
import com.fruitmkt.model.entity.ProductImage;
import java.sql.*;
import java.util.*;

/**
 * ProductImageDAO — DAO cho entity ProductImage.
 *
 * QUY TẮC:
 *   - Chỉ chứa SQL, không chứa business logic
 *   - Dùng PreparedStatement, KHÔNG nối chuỗi SQL
 *   - Mỗi method ném SQLException để Service xử lý
 *   - Dùng try-with-resources cho Connection + PreparedStatement
 *
 * @author fruitmkt-team
 */
public class ProductImageDAO extends BaseDAO {

    /**
     * TODO: Implement — findByProduct(int productId)
     */
    public List<ProductImage> findByProduct(int productId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findByProduct(int productId)");
    }

    /**
     * TODO: Implement — findPrimary(int productId)
     */
    public ProductImage findPrimary(int productId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findPrimary(int productId)");
    }

    /**
     * TODO: Implement — save(ProductImage image)
     */
    public int save(ProductImage image) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: save(ProductImage image)");
    }

    /**
     * TODO: Implement — delete(int imageId)
     */
    public ProductImage delete(int imageId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: delete(int imageId)");
    }

    /**
     * TODO: Implement — updateDisplayOrder(int imageId, int order)
     */
    public void updateDisplayOrder(int imageId, int order) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: updateDisplayOrder(int imageId, int order)");
    }

    /**
     * TODO: Implement — setPrimary(int imageId, int productId)
     */
    public void setPrimary(int imageId, int productId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: setPrimary(int imageId, int productId)");
    }

    /** Ánh xạ ResultSet -> ProductImage — gọi trong mọi query SELECT */
    private ProductImage mapRow(ResultSet rs) throws SQLException {
        // TODO: rs.getInt(), rs.getString()... theo Schema.sql
        throw new UnsupportedOperationException("mapRow not implemented");
    }
}
