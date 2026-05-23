package com.fruitmkt.servlet.admin;

import com.fruitmkt.dao.DashboardDAO;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet điều hướng và cung cấp dữ liệu cho trang Admin Dashboard.
 * Lắng nghe tại đường dẫn (URL Pattern): /admin/dashboard
 */
@WebServlet(name = "AdminDashboardController", urlPatterns = {"/admin/dashboard"})
public class AdminDashboardController extends HttpServlet {

    /**
     * Hàm dùng chung xử lý cả yêu cầu GET và POST từ trình duyệt.
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Khởi tạo đối tượng DAO chuyên xử lý thống kê cho Dashboard
        DashboardDAO dao = new DashboardDAO();
        try {
            // 1. GỌI DAO LẤY CÁC CHỈ SỐ TỔNG QUAN (Doanh thu, số đơn, user, shop)
            double totalRevenue = dao.getTotalRevenue();
            int newOrdersCount = dao.getNewOrdersCount();
            int pendingShopsCount = dao.getPendingShopsCount();
            int newUsersCount = dao.getNewUsersCount();

            // 2. GỌI DAO LẤY DỮ LIỆU BIỂU ĐỒ (Mảng doanh thu 4 tuần)
            List<Double> revenueLast4Weeks = dao.getRevenueLast4Weeks();

            // 3. GỌI DAO LẤY DANH SÁCH TOP SẢN PHẨM (Để hiển thị ra bảng)
            List<Map<String, Object>> topProducts = dao.getTopProducts();

            // 4. LƯU DỮ LIỆU VÀO REQUEST (Để trang JSP có thể đọc được qua cú pháp JSTL EL ${...})
            request.setAttribute("totalRevenue", totalRevenue);
            request.setAttribute("newOrdersCount", newOrdersCount);
            request.setAttribute("pendingShopsCount", pendingShopsCount);
            request.setAttribute("newUsersCount", newUsersCount);
            request.setAttribute("revenueLast4Weeks", revenueLast4Weeks);
            request.setAttribute("topProducts", topProducts);

            // 5. ĐIỀU HƯỚNG SANG GIAO DIỆN JSP (Chuyển tiếp luồng xử lý sang file admin-dashboard.jsp)
            request.getRequestDispatcher("/admin/admin-dashboard.jsp").forward(request, response);

        } catch (SQLException ex) {
            // Bắt lỗi liên quan đến Database (kết nối, sai cú pháp SQL...)
            ex.printStackTrace();
            // Trả về mã lỗi 500 (Internal Server Error) ra màn hình người dùng
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi truy xuất cơ sở dữ liệu");
        }
    }

    // Ghi đè phương thức doGet để bắt các yêu cầu truy cập thông thường qua đường dẫn URL
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    // Ghi đè phương thức doPost để bắt các yêu cầu gửi Form (dù hiện tại chưa dùng tới)
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}
