package com.fruitmkt.servlet.admin;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.util.SessionUtil;
import com.fruitmkt.service.OrderService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

/**
 * AdminDashboardServlet — Controller cho chức năng: KPI dashboard toàn hệ thống
 *
 * URL: /admin/dashboard
 * GET : KPI dashboard toàn hệ thống
 * POST: -
 *
 * QUY TẮC SERVLET:
 *   1. Không viết SQL ở đây — gọi Service
 *   2. Sau POST thành công dùng PRG pattern (sendRedirect)
 *   3. Lưu flash message vào session trước redirect
 *   4. Forward đến /WEB-INF/jsp/admin/... (không để truy cập trực tiếp)
 *   5. Kiểm tra quyền bằng SessionUtil trước khi xử lý
 *
 * @author fruitmkt-team
 */
@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {

    // TODO: Inject service — thêm service cần dùng ở đây
    // private final XxxService xxxService = new XxxService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/admin/dashboard.jsp").forward(req, resp);
    }

}
