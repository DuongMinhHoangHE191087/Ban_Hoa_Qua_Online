package com.fruitmkt.servlet.admin;

import com.fruitmkt.dao.ShopApprovalDAO;
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
 * Controller quản lý luồng duyệt gian hàng (Shop Approval)
 * Hỗ trợ 3 đường dẫn: Xem danh sách, Duyệt, và Từ chối.
 */
@WebServlet(name = "ShopApprovalController", urlPatterns = {"/admin/shops", "/admin/shops/approve", "/admin/shops/reject"})
public class ShopApprovalController extends HttpServlet {

    /**
     * Hàm doGet dùng để hiển thị danh sách các Shop đang chờ duyệt.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        ShopApprovalDAO dao = new ShopApprovalDAO();

        try {
            if ("/admin/shops".equals(path)) {
                // Liệt kê danh sách các shop đang chờ duyệt (PENDING)
                List<Map<String, Object>> pendingShops = dao.getPendingShops();
                // Truyền danh sách qua JSP
                request.setAttribute("pendingShops", pendingShops);
                // Điều hướng sang giao diện JSP
                request.getRequestDispatcher("/admin/shop-approval.jsp").forward(request, response);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi truy xuất cơ sở dữ liệu");
        }
    }

    /**
     * Hàm doPost dùng để nhận yêu cầu Duyệt hoặc Từ chối từ phía giao diện.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Cài đặt encoding utf-8 để đọc tiếng Việt (cho phần lý do từ chối)
        request.setCharacterEncoding("UTF-8");
        String path = request.getServletPath();
        ShopApprovalDAO dao = new ShopApprovalDAO();

        try {
            // Lấy profile_id của shop cần thao tác
            int profileId = Integer.parseInt(request.getParameter("profileId"));
            
            if ("/admin/shops/approve".equals(path)) {
                // Hành động: Duyệt shop
                dao.approveShop(profileId);
                // Redirect về trang danh sách kèm theo message thành công
                response.sendRedirect(request.getContextPath() + "/admin/shops?msg=approve_success");
                
            } else if ("/admin/shops/reject".equals(path)) {
                // Hành động: Từ chối shop
                String reason = request.getParameter("reason"); // Lấy lý do từ form
                dao.rejectShop(profileId, reason);
                // Redirect về trang danh sách kèm message thành công
                response.sendRedirect(request.getContextPath() + "/admin/shops?msg=reject_success");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/shops?msg=error");
        }
    }
}
