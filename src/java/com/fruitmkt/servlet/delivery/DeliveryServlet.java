package com.fruitmkt.servlet.delivery;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.util.SessionUtil;
import com.fruitmkt.service.DeliveryService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

/**
 * DeliveryServlet — Controller cho chức năng: Danh sách đơn được phân công, cập nhật status
 *
 * URL: /delivery/*
 * GET : Danh sách đơn được phân công, cập nhật status
 * POST: -
 *
 * QUY TẮC SERVLET:
 *   1. Không viết SQL ở đây — gọi Service
 *   2. Sau POST thành công dùng PRG pattern (sendRedirect)
 *   3. Lưu flash message vào session trước redirect
 *   4. Forward đến /WEB-INF/jsp/delivery/... (không để truy cập trực tiếp)
 *   5. Kiểm tra quyền bằng SessionUtil trước khi xử lý
 *
 * @author fruitmkt-team
 */
@WebServlet("/delivery/*")
public class DeliveryServlet extends HttpServlet {

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
        // req.getRequestDispatcher("/WEB-INF/jsp/delivery/xxx.jsp").forward(req, resp);
        throw new UnsupportedOperationException("doGet not implemented: DeliveryServlet");
    }

}
