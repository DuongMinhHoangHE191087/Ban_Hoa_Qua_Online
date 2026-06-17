package servlet.guest.cart;

import config.AppConfig;
import util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * GuestCartServlet — Controller cho chức năng: Hiển thị cart placeholder cho guest
 *
 * URL: /guest/cart
 * GET : Hiển thị cart placeholder cho guest
 * POST: Redirect sang login
 *
 * QUY TẮC SERVLET:
 *   1. Không viết SQL ở đây — gọi Service
 *   2. Sau POST thành công dùng PRG pattern (sendRedirect)
 *   3. Lưu flash message vào session trước redirect
 *   4. Forward đến /WEB-INF/jsp/guest/... (không để truy cập trực tiếp)
 *   5. Kiểm tra quyền bằng SessionUtil trước khi xử lý
 *
 * @author fruitmkt-team
 */
@WebServlet("/guest/cart")
public class GuestCartServlet extends HttpServlet {

    // TODO: Inject service — thêm service cần dùng ở đây
    // private final XxxService xxxService = new XxxService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.sendRedirect(req.getContextPath() + "/cart");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String redirectUrl = req.getParameter("redirect");
        if (redirectUrl == null || redirectUrl.isEmpty()) {
            redirectUrl = "/cart";
        }
        resp.sendRedirect(req.getContextPath() + "/auth/login?redirect=" + redirectUrl);
    }

}
