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
     * Lấy toàn bộ danh sách ảnh của một sản phẩm.
     */
    public List<ProductImage> findByProduct(int productId) throws SQLException {
        List<ProductImage> list = new ArrayList<>();
        String sql = "SELECT * FROM product_images WHERE product_id = ? ORDER BY display_order ASC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Lấy ảnh chính (primary) của một sản phẩm. Nếu không có ảnh primary, lấy ảnh đầu tiên.
     */
    public ProductImage findPrimary(int productId) throws SQLException {
        String sql = "SELECT * FROM product_images WHERE product_id = ? ORDER BY is_primary DESC, display_order ASC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Lưu thông tin ảnh mới vào cơ sở dữ liệu.
     */
    public int save(ProductImage image) throws SQLException {
        String sql = "INSERT INTO product_images (product_id, file_path, display_order, is_primary, uploaded_at) VALUES (?, ?, ?, ?, GETDATE())";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, image.getProductId());
            ps.setString(2, image.getFilePath());
            ps.setInt(3, image.getDisplayOrder());
            ps.setBoolean(4, image.getIsPrimary());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Lưu ảnh sản phẩm thất bại, không lấy được mã khóa tự tăng.");
    }

    /**
     * Xóa ảnh sản phẩm khỏi database.
     */
    public void delete(int imageId) throws SQLException {
        String sql = "DELETE FROM product_images WHERE image_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, imageId);
            ps.executeUpdate();
        }
    }

    /**
     * Cập nhật thứ tự hiển thị của ảnh sản phẩm.
     */
    public void updateDisplayOrder(int imageId, int order) throws SQLException {
        String sql = "UPDATE product_images SET display_order = ? WHERE image_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, order);
            ps.setInt(2, imageId);
            ps.executeUpdate();
        }
    }

    /**
     * Đặt một bức ảnh làm ảnh chính, đồng thời bỏ chọn làm ảnh chính cho các ảnh khác của cùng sản phẩm.
     */
    public void setPrimary(int imageId, int productId) throws SQLException {
        String sqlReset = "UPDATE product_images SET is_primary = 0 WHERE product_id = ?";
        String sqlSet = "UPDATE product_images SET is_primary = 1 WHERE image_id = ?";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psReset = conn.prepareStatement(sqlReset);
                 PreparedStatement psSet = conn.prepareStatement(sqlSet)) {
                
                psReset.setInt(1, productId);
                psReset.executeUpdate();
                
                psSet.setInt(1, imageId);
                psSet.executeUpdate();
                
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /** Ánh xạ ResultSet -> ProductImage */
    private ProductImage mapRow(ResultSet rs) throws SQLException {
        ProductImage img = new ProductImage();
        img.setImageId(rs.getInt("image_id"));
        img.setProductId(rs.getInt("product_id"));
        img.setFilePath(rs.getString("file_path"));
        img.setDisplayOrder(rs.getInt("display_order"));
        img.setIsPrimary(rs.getBoolean("is_primary"));
        
        Timestamp uploadedAtVal = rs.getTimestamp("uploaded_at");
        if (uploadedAtVal != null) {
            img.setUploadedAt(uploadedAtVal.toLocalDateTime());
        }
        return img;
    }
}
