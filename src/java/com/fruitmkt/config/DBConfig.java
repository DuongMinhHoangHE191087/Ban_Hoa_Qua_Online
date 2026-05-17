package com.fruitmkt.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConfig — Quản lý kết nối JDBC đến SQL Server.
 *
 * CẤU HÌNH:
 *   Thay 4 hằng số bên dưới bằng thông tin thực tế của team.
 *   KHÔNG commit file này lên git nếu chứa password thật.
 *   Khuyến nghị: dùng biến môi trường hoặc file .env ngoài source tree.
 *
 * JDBC URL FORMAT:
 *   jdbc:sqlserver://<host>:<port>;databaseName=<db>;encrypt=false;trustServerCertificate=true
 *
 * DRIVER JAR:
 *   Đặt mssql-jdbc-*.jre11.jar vào web/WEB-INF/lib/
 *   Tải tại: https://learn.microsoft.com/en-us/sql/connect/jdbc/download-microsoft-jdbc-driver-for-sql-server
 *
 * @author fruitmkt-team
 */
public class DBConfig {

    // =====================================================================
    // TODO: Cập nhật các giá trị này theo môi trường của team
    // =====================================================================
    private static final String DB_HOST     = "localhost";
    private static final String DB_PORT     = "1433";
    private static final String DB_NAME     = "OnlineFruitShopping";
    private static final String DB_USER     = "sa";
    private static final String DB_PASSWORD = "your_password_here";
    // =====================================================================

    private static final String JDBC_URL =
        "jdbc:sqlserver://" + DB_HOST + ":" + DB_PORT
        + ";databaseName=" + DB_NAME
        + ";encrypt=false;trustServerCertificate=true";

    private static final String DRIVER_CLASS = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

    static {
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(
                "Không tìm thấy MSSQL JDBC driver. Kiểm tra JAR trong WEB-INF/lib.\n" + e.getMessage()
            );
        }
    }

    /**
     * Lấy một Connection từ DriverManager.
     * Mỗi lần gọi sẽ tạo một connection mới — đảm bảo đóng trong try-with-resources.
     *
     * CÁCH DÙNG:
     * <pre>
     *   try (Connection conn = DBConfig.getConnection();
     *        PreparedStatement ps = conn.prepareStatement(sql)) {
     *       // ... xử lý
     *   }
     * </pre>
     *
     * @return Connection tới SQL Server
     * @throws SQLException nếu kết nối thất bại
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
    }

    private DBConfig() { /* Không khởi tạo */ }
}
