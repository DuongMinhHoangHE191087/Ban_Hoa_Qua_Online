package com.fruitmkt.servlet.admin;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.util.SessionUtil;
import com.fruitmkt.service.OrderService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

/**
 * AdminOrderServlet — Controller cho chức năng: Monitoring đơn hàng toàn sàn
 *
 * URL: /admin/orders
 * GET : Monitoring đơn hàng toàn sàn
 * POST: Can thiệp đơn bất thường
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
@WebServlet("/admin/orders")
public class AdminOrderServlet extends HttpServlet {

    // TODO: Inject service — thêm service cần dùng ở đây
    // private final XxxService xxxService = new XxxService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/admin/coming-soon.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: 1. Đọc params / JSON body
        //        2. Validate input
        //        3. Gọi service
        //        4. Set flash message
        //        5. Redirect (PRG pattern)
        //
        // Ví dụ:
        // req.getSession().setAttribute(AppConfig.SESSION_FLASH_MSG, "Thành công!");
        // req.getSession().setAttribute(AppConfig.SESSION_FLASH_TYPE, "success");
        // resp.sendRedirect(req.getContextPath() + "/..");
        throw new UnsupportedOperationException("doPost not implemented: AdminOrderServlet");
    }

}
