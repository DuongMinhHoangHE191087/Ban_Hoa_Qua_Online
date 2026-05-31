package com.fruitmkt.servlet.admin;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.util.SessionUtil;
import com.fruitmkt.service.AuthService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

/**
 * UserManageServlet — Controller cho chức năng: Danh sách và chi tiết user, lock/unlock
 *
 * URL: /admin/users
 * GET : Danh sách và chi tiết user, lock/unlock
 * POST: Cập nhật trạng thái user
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
@WebServlet("/admin/user-manage")
public class UserManageServlet extends HttpServlet {

    // TODO: Inject service — thêm service cần dùng ở đây
    // private final XxxService xxxService = new XxxService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: 1. Kiểm tra session/quyền nếu cần
        //        2. Đọc request parameters
        //        3. Gọi service để lấy data
        //        4. Set attributes vào request
        //        5. Forward đến JSP tương ứng
        //
        // Ví dụ:
        // req.setAttribute("data", service.getData(...));
        // req.getRequestDispatcher("/WEB-INF/jsp/admin/xxx.jsp").forward(req, resp);
        throw new UnsupportedOperationException("doGet not implemented: UserManageServlet");
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
        throw new UnsupportedOperationException("doPost not implemented: UserManageServlet");
    }

}
