package com.fruitmkt.dao;

import com.fruitmkt.dao.base.BaseDAO;
import com.fruitmkt.model.entity.Category;
import java.sql.*;
import java.util.*;

/**
 * CategoryDAO — DAO cho entity Category.
 *
 * @author fruitmkt-team
 */
public class CategoryDAO extends BaseDAO {

    /**
     * Tìm danh mục theo ID.
     */
    public List<Category> findById(int id) throws SQLException {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM categories WHERE category_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Lấy toàn bộ danh mục sắp xếp theo display_order.
     */
    public List<Category> findAll() throws SQLException {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM categories ORDER BY display_order ASC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Lấy toàn bộ danh mục đang hoạt động.
     */
    public List<Category> findAllActive() throws SQLException {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM categories WHERE is_active = 1 ORDER BY display_order ASC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Lưu danh mục mới vào DB.
     */
    public int save(Category category) throws SQLException {
        String sql = "INSERT INTO categories (name, slug, display_order, is_active) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, category.getName());
            ps.setString(2, category.getSlug());
            ps.setInt(3, category.getDisplayOrder());
            ps.setBoolean(4, category.getIsActive());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Lưu danh mục thất bại, không lấy được mã khóa tự tăng.");
    }

    /**
     * Cập nhật thông tin danh mục.
     */
    public void update(Category category) throws SQLException {
        String sql = "UPDATE categories SET name = ?, slug = ?, display_order = ?, is_active = ? WHERE category_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
          ps.setString(1, category.getName());
          ps.setString(2, category.getSlug());
          ps.setInt(3, category.getDisplayOrder());
          ps.setBoolean(4, category.getIsActive());
          ps.setInt(5, category.getCategoryId());
          ps.executeUpdate();
        }
    }

    /**
     * Xóa danh mục (Hard delete - Cần cẩn thận nếu có tham chiếu).
     */
    public void delete(int categoryId) throws SQLException {
        String sql = "DELETE FROM categories WHERE category_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            ps.executeUpdate();
        }
    }

    /**
     * Kiểm tra xem danh mục có sản phẩm đang ACTIVE/INACTIVE (chưa bị DELETE) hay không.
     */
    public boolean hasActiveProducts(int categoryId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM products WHERE category_id = ? AND status != 'DELETE'";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /** Ánh xạ ResultSet -> Category */
    private Category mapRow(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setCategoryId(rs.getInt("category_id"));
        category.setName(rs.getString("name"));
        category.setSlug(rs.getString("slug"));
        category.setDisplayOrder(rs.getInt("display_order"));
        category.setIsActive(rs.getBoolean("is_active"));
        return category;
    }
}
