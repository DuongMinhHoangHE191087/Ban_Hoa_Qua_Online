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
 * DashboardDAO chuyên xử lý các truy vấn thống kê dữ liệu cho trang Admin Dashboard.
 * Kế thừa BaseDAO để sử dụng chung hàm getConnection() của toàn dự án.
 */
public class DashboardDAO extends BaseDAO {

    /**
     * Tính tổng doanh thu của sàn.
     * Logic: Chỉ cộng tổng cột final_amount của những đơn hàng đã giao thành công (status = 'DELIVERED').
     */
    public double getTotalRevenue() throws SQLException {
        String sql = "SELECT SUM(final_amount) AS Total FROM orders WHERE status = 'DELIVERED'";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble("Total");
            }
        }
        return 0;
    }

    /**
     * Đếm tổng số đơn hàng mới sinh ra trong 30 ngày qua.
     * Logic: Lấy COUNT tất cả các dòng trong bảng orders có created_at >= (Ngày hiện tại - 30 ngày).
     */
    public int getNewOrdersCount() throws SQLException {
        // Dùng DATEADD của SQL Server để trừ đi 30 ngày từ GETDATE()
        String sql = "SELECT COUNT(*) AS Count FROM orders WHERE created_at >= DATEADD(day, -30, GETDATE())";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("Count");
            }
        }
        return 0;
    }

    /**
     * Đếm số lượng cửa hàng (Shop) đang chờ duyệt.
     * Logic: Kiểm tra bảng shop_owner_profiles, đếm các dòng có trạng thái là 'PENDING'.
     */
    public int getPendingShopsCount() throws SQLException {
        String sql = "SELECT COUNT(*) AS Count FROM shop_owner_profiles WHERE approval_status = 'PENDING'";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("Count");
            }
        }
        return 0;
    }

    /**
     * Đếm số tài khoản người dùng đăng ký mới trong vòng 30 ngày qua.
     * Logic: Tương tự như đếm đơn hàng, nhưng truy vấn trên bảng users.
     */
    public int getNewUsersCount() throws SQLException {
        String sql = "SELECT COUNT(*) AS Count FROM users WHERE created_at >= DATEADD(day, -30, GETDATE())";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("Count");
            }
        }
        return 0;
    }

    /**
     * Tính tổng doanh thu theo từng tuần trong 4 tuần gần nhất.
     * Dữ liệu này dùng để cung cấp mảng vẽ biểu đồ Chart.js trên giao diện.
     * @return Một danh sách gồm 4 phần tử Double, phần tử đầu là tuần cũ nhất, phần tử cuối là tuần gần nhất.
     */
    public List<Double> getRevenueLast4Weeks() throws SQLException {
        List<Double> revenues = new ArrayList<>();
        // Vòng lặp 4 lần, truy vấn lùi dần từng tuần.
        // i=0: 7 ngày trước -> hiện tại (Tuần gần nhất)
        // i=1: 14 ngày trước -> 7 ngày trước ...
        for (int i = 0; i < 4; i++) {
            // Hàm ISNULL giúp thay thế giá trị NULL thành 0 nếu tuần đó không có đơn hàng nào
            String sql = "SELECT ISNULL(SUM(final_amount), 0) AS Total FROM orders " +
                         "WHERE status = 'DELIVERED' " +
                         "AND created_at >= DATEADD(day, ?, GETDATE()) " +
                         "AND created_at < DATEADD(day, ?, GETDATE())";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                
                // Set tham số cho ngày bắt đầu của tuần (âm)
                ps.setInt(1, -(i + 1) * 7); 
                // Set tham số cho ngày kết thúc của tuần (âm)
                ps.setInt(2, -i * 7);       
                
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        // Thêm vào vị trí index 0 để đảo ngược thứ tự (tuần cũ đẩy lên trước, tuần mới ở cuối)
                        revenues.add(0, rs.getDouble("Total")); 
                    } else {
                        revenues.add(0, 0.0);
                    }
                }
            }
        }
        return revenues;
    }

    /**
     * Lấy danh sách Top 5 sản phẩm bán chạy nhất.
     * @return Danh sách các Map chứa name (tên sản phẩm) và sold_quantity (số lượng đã bán).
     */
    public List<Map<String, Object>> getTopProducts() throws SQLException {
        List<Map<String, Object>> topProducts = new ArrayList<>();
        // Câu lệnh TOP 5 của SQL Server kết hợp ORDER BY DESC để lấy ra các dòng có sold_quantity lớn nhất
        String sql = "SELECT TOP 5 name, sold_quantity FROM products ORDER BY sold_quantity DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> product = new HashMap<>();
                product.put("name", rs.getString("name"));
                product.put("sold_quantity", rs.getInt("sold_quantity"));
                topProducts.add(product);
            }
        }
        return topProducts;
    }
}
