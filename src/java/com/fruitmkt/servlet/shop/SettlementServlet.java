package com.fruitmkt.servlet.shop;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.util.SessionUtil;
import com.fruitmkt.service.SettlementService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

/**
 * SettlementServlet — Controller cho chức năng: Xem settlement summary và chi tiết kỳ
 *
 * URL: /shop/settlement
 * GET : Xem settlement summary và chi tiết kỳ
 * POST: -
 *
 * QUY TẮC SERVLET:
 *   1. Không viết SQL ở đây — gọi Service
 *   2. Sau POST thành công dùng PRG pattern (sendRedirect)
 *   3. Lưu flash message vào session trước redirect
 *   4. Forward đến /WEB-INF/jsp/shop/... (không để truy cập trực tiếp)
 *   5. Kiểm tra quyền bằng SessionUtil trước khi xử lý
 *
 * @author fruitmkt-team
 */
@WebServlet("/shop/settlement")
public class SettlementServlet extends HttpServlet {

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
        // req.getRequestDispatcher("/WEB-INF/jsp/shop/xxx.jsp").forward(req, resp);
        throw new UnsupportedOperationException("doGet not implemented: SettlementServlet");
    }

}
