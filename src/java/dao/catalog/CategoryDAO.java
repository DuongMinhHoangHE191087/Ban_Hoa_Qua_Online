package dao.catalog;

import dao.system.BaseDAO;
import model.entity.catalog.Category;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import util.PaginationHelper;

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

    public List<Category> findAll(int page, int pageSize) throws SQLException {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM categories ORDER BY display_order ASC "
                + PaginationHelper.OFFSET_FETCH_SQL;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            PaginationHelper.bindOffsetFetch(ps, 1, page, pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM categories";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
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
        String countSql = "SELECT COUNT(*) FROM products WHERE category_id = ?";
        String deleteSql = "DELETE FROM categories WHERE category_id = ?";
        String deactivateSql = "UPDATE categories SET is_active = 0 WHERE category_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement countPs = conn.prepareStatement(countSql);
             PreparedStatement deletePs = conn.prepareStatement(deleteSql);
             PreparedStatement deactivatePs = conn.prepareStatement(deactivateSql)) {
            countPs.setInt(1, categoryId);
            try (ResultSet rs = countPs.executeQuery()) {
                int productCount = 0;
                if (rs.next()) {
                    productCount = rs.getInt(1);
                }
                if (productCount > 0) {
                    deactivatePs.setInt(1, categoryId);
                    deactivatePs.executeUpdate();
                } else {
                    deletePs.setInt(1, categoryId);
                    deletePs.executeUpdate();
                }
            }
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
