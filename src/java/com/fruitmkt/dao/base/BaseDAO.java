package com.fruitmkt.dao.base;

import com.fruitmkt.config.AppConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * BaseDAO — Lớp cha cho tất cả các DAO.
 *
 * TRÁCH NHIỆM:
 *   - Cung cấp getConnection() tiện lợi cho các lớp con
 *   - Tập trung xử lý close/cleanup
 *
 * QUY TẮC DAO (BẮT BUỘC với toàn team):
 *   1. Chỉ viết SQL trong lớp DAO, KHÔNG ở Service hay Servlet
 *   2. Luôn dùng PreparedStatement — KHÔNG nối chuỗi SQL
 *   3. Luôn đóng Connection/PreparedStatement trong try-with-resources
 *   4. Ném SQLException lên Service để xử lý, KHÔNG ăn exception
 *   5. Tên method theo pattern: findById, findAll, findBy*, save, update, delete
 *
 * VÍ DỤ CHUẨN:
 * <pre>
 *   public MyEntity findById(int id) throws SQLException {
 *       String sql = "SELECT * FROM my_table WHERE id = ?";
 *       try (Connection conn = getConnection();
 *            PreparedStatement ps = conn.prepareStatement(sql)) {
 *           ps.setInt(1, id);
 *           try (ResultSet rs = ps.executeQuery()) {
 *               return rs.next() ? mapRow(rs) : null;
 *           }
 *       }
 *   }
 * </pre>
 *
 * @author fruitmkt-team
 */
public abstract class BaseDAO {

    static {
        try {
            Class.forName(AppConfig.DB_DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("Không tìm thấy JDBC driver cho SQL Server: " + AppConfig.DB_DRIVER_CLASS);
        }
    }

    /**
     * Lấy kết nối trực tiếp từ cấu hình database.
     * Kết nối phải được đóng bởi caller trong try-with-resources.
     */
    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(AppConfig.DB_JDBC_URL, AppConfig.DB_USER, AppConfig.DB_PASSWORD);
    }
}
