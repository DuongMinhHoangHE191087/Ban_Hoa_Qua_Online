package com.fruitmkt.servlet.auth;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.util.SessionUtil;
import com.fruitmkt.service.AuthService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * RegisterServlet — Controller cho chức năng: Hiển thị form đăng ký
 *
 * URL: /auth/register
 * GET : Hiển thị form đăng ký
 * POST: Tạo tài khoản mới
 *
 * QUY TẮC SERVLET:
 * 1. Không viết SQL ở đây — gọi Service
 * 2. Sau POST thành công dùng PRG pattern (sendRedirect)
 * 3. Lưu flash message vào session trước redirect
 * 4. Forward đến /WEB-INF/jsp/auth/... (không để truy cập trực tiếp)
 * 5. Kiểm tra quyền bằng SessionUtil trước khi xử lý
 *
 * @author fruitmkt-team
 */
@WebServlet("/auth/register")
public class RegisterServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Forward trực tiếp đến trang JSP đăng ký an toàn nằm sau WEB-INF
        req.getRequestDispatcher("/WEB-INF/jsp/auth/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        req.setCharacterEncoding("UTF-8");
        
        // 1. Nhận các tham số từ form đăng ký (register.jsp)
        String role = req.getParameter("accountType"); // 'CUSTOMER' hoặc 'SHOP_OWNER'
        String fullName = req.getParameter("fullName");
        String email = req.getParameter("email");
        String phone = req.getParameter("phone");
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");

        try {
            // 2. Validate phía Servlet (So khớp mật khẩu)
            
            if (fullName == null || fullName.trim().isEmpty()) {
                throw new Exception("Họ và tên không được để trống!");
            }
            if (email == null || email.trim().isEmpty()) {
                throw new Exception("Email không được để trống!");
            }
            if (phone == null || phone.trim().isEmpty()) {
                throw new Exception("Số điện thoại không được để trống!");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new Exception("Mật khẩu không được để trống!");
            }
            if (password.length() < 8 || password.length() > 64) {
                throw new Exception("Mật khẩu phải từ 8 đến 64 ký tự!");
            }
            if (!password.equals(confirmPassword)) {
                throw new Exception("Mật khẩu xác nhận không khớp!");
            }
            // Gán role mặc định nếu bị rỗng hoặc sai
            if (role == null || (!role.equals("CUSTOMER") && !role.equals("SHOP_OWNER"))) {
                role = "CUSTOMER"; 
            }

            // 3. Đưa dữ liệu thô vào Entity để truyền xuống Tầng Service
            User user = new User();
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPasswordHash(password); // Truyền mật khẩu thô để Service validate và Hash
            user.setPhone(phone);
            user.setRole(role);

            // 4. Xử lý Đăng Ký qua Service
            String storeName = req.getParameter("storeName");
            String address = req.getParameter("address");
            User newUser = authService.register(user, storeName, address);

            // 6. Xử lý thành công - Sử dụng Pattern PRG với Flash Message
            SessionUtil.flashSuccess(req.getSession(), "Đăng ký thành công! Vui lòng kiểm tra email để xác minh trong 5 phút.");
            req.getSession().setAttribute(AppConfig.SESSION_VERIFY_EMAIL, newUser.getEmail());
            resp.sendRedirect(req.getContextPath() + "/auth/verify");

        } catch (Exception e) {
            req.getServletContext().log("RegisterServlet error: " + e.getMessage(), e);
            req.setAttribute("errorMsg", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/jsp/auth/register.jsp").forward(req, resp);
        }
    }

}
