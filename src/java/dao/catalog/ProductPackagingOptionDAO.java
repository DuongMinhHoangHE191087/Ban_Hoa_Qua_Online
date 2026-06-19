package dao.catalog;

import dao.system.BaseDAO;
import model.entity.catalog.ProductPackagingOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * ProductPackagingOptionDAO — DAO xử lý thông tin bảng product_packaging_options.
 *
 * @author fruitmkt-team
 */
public class ProductPackagingOptionDAO extends BaseDAO {

    public List<ProductPackagingOption> findByProduct(int productId) throws SQLException {
        List<ProductPackagingOption> list = new ArrayList<>();
        String sql = "SELECT * FROM product_packaging_options WHERE product_id = ? AND is_active = 1";
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

    public ProductPackagingOption findById(int packagingId) throws SQLException {
        String sql = "SELECT * FROM product_packaging_options WHERE packaging_id = ? AND is_active = 1";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, packagingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public int save(ProductPackagingOption option) throws SQLException {
        String sql = "INSERT INTO product_packaging_options (product_id, label, price_add, is_active) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, option.getProductId());
            ps.setString(2, option.getLabel());
            ps.setBigDecimal(3, option.getPriceAdd());
            ps.setBoolean(4, option.getIsActive());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to save packaging option.");
    }

    public void deleteByProductExcept(int productId, List<Integer> keepIds) throws SQLException {
        StringBuilder sql = new StringBuilder("DELETE FROM product_packaging_options WHERE product_id = ?");
        if (keepIds != null && !keepIds.isEmpty()) {
            sql.append(" AND packaging_id NOT IN (");
            for (int i = 0; i < keepIds.size(); i++) {
                if (i > 0) sql.append(",");
                sql.append("?");
            }
            sql.append(")");
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, productId);
            if (keepIds != null && !keepIds.isEmpty()) {
                for (int i = 0; i < keepIds.size(); i++) {
                    ps.setInt(i + 2, keepIds.get(i));
                }
            }
            ps.executeUpdate();
        }
    }

    public void update(ProductPackagingOption option) throws SQLException {
        String sql = "UPDATE product_packaging_options SET label = ?, price_add = ?, is_active = ? WHERE packaging_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, option.getLabel());
            ps.setBigDecimal(2, option.getPriceAdd());
            ps.setBoolean(3, option.getIsActive());
            ps.setInt(4, option.getPackagingId());
            ps.executeUpdate();
        }
    }

    private ProductPackagingOption mapRow(ResultSet rs) throws SQLException {
        ProductPackagingOption option = new ProductPackagingOption();
        option.setPackagingId(rs.getInt("packaging_id"));
        option.setProductId(rs.getInt("product_id"));
        option.setLabel(rs.getString("label"));
        option.setPriceAdd(rs.getBigDecimal("price_add"));
        option.setIsActive(rs.getBoolean("is_active"));
        return option;
    }
}
