package dao.auth;

import dao.system.BaseDAO;
import model.entity.auth.UserAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import java.util.logging.Logger;
import util.LoggerUtil;

public class UserAddressDAO extends BaseDAO {

    private static final Logger log = Logger.getLogger(UserAddressDAO.class.getName());

    public List<UserAddress> findByUser(int userId) throws SQLException {
        List<UserAddress> list = new ArrayList<>();
        String sql = "SELECT * FROM user_addresses WHERE user_id = ? ORDER BY is_default DESC, created_at DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public UserAddress findById(int addressId) throws SQLException {
        String sql = "SELECT * FROM user_addresses WHERE address_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, addressId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public boolean save(UserAddress addr) throws SQLException {
        String sql = "INSERT INTO user_addresses (user_id, recipient_name, recipient_phone, address_detail, is_default, created_at) "
                   + "VALUES (?, ?, ?, ?, ?, GETDATE())";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, addr.getUserId());
            ps.setString(2, addr.getRecipientName());
            ps.setString(3, addr.getRecipientPhone());
            ps.setString(4, addr.getAddressDetail());
            ps.setBoolean(5, addr.isDefault());
            
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        addr.setAddressId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean update(UserAddress addr) throws SQLException {
        String sql = "UPDATE user_addresses SET recipient_name = ?, recipient_phone = ?, address_detail = ?, is_default = ? "
                   + "WHERE address_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, addr.getRecipientName());
            ps.setString(2, addr.getRecipientPhone());
            ps.setString(3, addr.getAddressDetail());
            ps.setBoolean(4, addr.isDefault());
            ps.setInt(5, addr.getAddressId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int addressId) throws SQLException {
        String sql = "DELETE FROM user_addresses WHERE address_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, addressId);
            return ps.executeUpdate() > 0;
        }
    }

    public void clearDefault(int userId) throws SQLException {
        String sql = "UPDATE user_addresses SET is_default = 0 WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    private UserAddress mapRow(ResultSet rs) throws SQLException {
        UserAddress addr = new UserAddress();
        addr.setAddressId(rs.getInt("address_id"));
        addr.setUserId(rs.getInt("user_id"));
        addr.setRecipientName(rs.getString("recipient_name"));
        addr.setRecipientPhone(rs.getString("recipient_phone"));
        addr.setAddressDetail(rs.getString("address_detail"));
        addr.setDefault(rs.getBoolean("is_default"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            addr.setCreatedAt(ts.toLocalDateTime());
        }
        return addr;
    }
}
