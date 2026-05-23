package com.fruitmkt.dao;

import com.fruitmkt.dao.base.BaseDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CategoryManagementDAO xử lý các thao tác Quản lý danh mục.
 * Bao gồm: Lấy danh sách, Thêm mới, Cập nhật thông tin, và Bật/Tắt trạng thái.
 */
public class CategoryManagementDAO extends BaseDAO {

    /**
     * Lấy danh sách toàn bộ danh mục sản phẩm (hoa quả).
     * Sắp xếp theo cột display_order để hiển thị đúng thứ tự trên web.
     */
    public List<Map<String, Object>> getAllCategories() throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT category_id, name, slug, display_order, is_active FROM categories ORDER BY display_order ASC";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> cat = new HashMap<>();
                cat.put("category_id", rs.getInt("category_id"));
                cat.put("name", rs.getString("name"));
                cat.put("slug", rs.getString("slug"));
                cat.put("display_order", rs.getInt("display_order"));
                cat.put("is_active", rs.getBoolean("is_active")); // Trả về dạng boolean true/false
                list.add(cat);
            }
        }
        return list;
    }

    /**
     * Thêm một danh mục mới vào hệ thống.
     * @return true nếu thêm thành công vào cơ sở dữ liệu
     */
    public boolean addCategory(String name, String slug, int displayOrder, boolean isActive) throws SQLException {
        String sql = "INSERT INTO categories (name, slug, display_order, is_active) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, slug);
            ps.setInt(3, displayOrder);
            ps.setBoolean(4, isActive);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Thay đổi trạng thái hiển thị của danh mục (Bật/Tắt).
     * @param categoryId Mã danh mục cần đổi
     * @param isActive Trạng thái (1 = Hiển thị, 0 = Ẩn)
     */
    public boolean toggleCategoryStatus(int categoryId, boolean isActive) throws SQLException {
        String sql = "UPDATE categories SET is_active = ? WHERE category_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, isActive);
            ps.setInt(2, categoryId);
            return ps.executeUpdate() > 0;
        }
    }
}
