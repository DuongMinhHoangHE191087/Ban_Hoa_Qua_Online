package com.fruitmkt.servlet.auth;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.dao.CategoryDAO;
import com.fruitmkt.model.entity.Category;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.util.FileUploadUtil;
import com.fruitmkt.util.SessionUtil;
import com.fruitmkt.service.AuthService;
import com.fruitmkt.service.EmailService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * RegisterServlet — Controller cho chức năng: Hiển thị form đăng ký
 *
 * URL: /auth/register
 * GET : Hiển thị form đăng ký (load categories từ DB)
 * POST: Tạo tài khoản mới, xử lý upload tài liệu (tối đa 10 file, mỗi file ≤ 25MB)
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
@MultipartConfig(
    maxFileSize    = 26_214_400L,   // 25MB mỗi file
    maxRequestSize = 264_241_152L,  // ~252MB tổng (10 file × 25MB + overhead)
    fileSizeThreshold = 1_048_576   // 1MB: dưới ngưỡng này giữ trong RAM
)
public class RegisterServlet extends HttpServlet {

    private final AuthService authService = new AuthService();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final EmailService emailService = new EmailService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Load danh mục active từ DB để render checkbox động
        try {
            List<Category> categories = categoryDAO.findAllActive();
            req.setAttribute("categories", categories);
        } catch (SQLException e) {
            getServletContext().log("RegisterServlet GET: Không tải được categories", e);
            req.setAttribute("categories", new ArrayList<>());
        }
        req.getRequestDispatcher("/WEB-INF/jsp/auth/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        // Kiểm tra CSRF token thủ công vì /auth/* bị CsrfFilter bỏ qua
        String sessionCsrf = (String) req.getSession().getAttribute(AppConfig.SESSION_CSRF_TOKEN);
        String reqCsrf = req.getParameter("_csrf");
        if (sessionCsrf != null && !sessionCsrf.equals(reqCsrf)) {
            forwardWithError(req, resp, "CSRF token không hợp lệ hoặc phiên làm việc đã hết hạn.");
            return;
        }

        // 1. Nhận các tham số từ form đăng ký
        String role = req.getParameter("accountType"); // 'CUSTOMER' hoặc 'SHOP_OWNER'
        String fullName = req.getParameter("fullName");
        String email = req.getParameter("email");
        String phone = req.getParameter("phone");
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");

        try {
            // 2. Validate phía Servlet bằng ValidationUtil
            fullName = com.fruitmkt.util.ValidationUtil.requireNotBlank(fullName, "Họ và tên");
            email = com.fruitmkt.util.ValidationUtil.requireValidEmail(email, "Email");
            phone = com.fruitmkt.util.ValidationUtil.requireValidPhone(phone, "Số điện thoại");
            password = com.fruitmkt.util.ValidationUtil.requireValidPassword(password, "Mật khẩu");
            confirmPassword = com.fruitmkt.util.ValidationUtil.requireNotBlank(confirmPassword, "Mật khẩu xác nhận");

            if (!password.equals(confirmPassword)) {
                throw new Exception("Mật khẩu xác nhận không khớp!");
            }
            // Gán role mặc định nếu bị rỗng hoặc sai
            if (role == null || (!role.equals("CUSTOMER") && !role.equals("SHOP_OWNER"))) {
                role = "CUSTOMER";
            }

            // 3. Đưa dữ liệu thô vào Entity
            User user = new User();
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPasswordHash(password);
            user.setPhone(phone);
            user.setRole(role);

            String storeName = req.getParameter("storeName");
            String address = req.getParameter("address");
            String preferredCategoriesJson = null;
            String docPathsJson = null;

            // 4. Xử lý riêng cho SHOP_OWNER
            if ("SHOP_OWNER".equals(role)) {
                storeName = com.fruitmkt.util.ValidationUtil.requireValidShopName(storeName, "Tên cửa hàng");
                address = com.fruitmkt.util.ValidationUtil.requireValidAddress(address, "Địa chỉ kinh doanh");

                // 4a. Xử lý danh mục kinh doanh → JSON array
                String[] catIds = req.getParameterValues("categoryIds");
                if (catIds != null && catIds.length > 0) {
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < catIds.length; i++) {
                        // Validate là số nguyên hợp lệ
                        try {
                            int catId = Integer.parseInt(catIds[i]);
                            if (i > 0) sb.append(",");
                            sb.append(catId);
                        } catch (NumberFormatException ignored) {
                            // Bỏ qua giá trị không hợp lệ
                        }
                    }
                    sb.append("]");
                    preferredCategoriesJson = sb.toString();
                }

                // 4b. Xử lý upload tài liệu (tối đa 10 file, mỗi file ≤ 25MB)
                String uploadDir = getServletContext().getRealPath("");
                List<String> docPathList = new ArrayList<>();
                List<Part> docParts = new ArrayList<>(req.getParts());

                int docCount = 0;
                for (Part part : docParts) {
                    if (!"businessDocs".equals(part.getName())) continue;
                    if (part.getSize() == 0) continue;

                    // Sử dụng ValidationUtil để check kích thước, extension
                    String docError = com.fruitmkt.util.ValidationUtil.validateShopDoc(part);
                    if (docError != null) {
                        throw new Exception(docError);
                    }

                    if (docCount >= AppConfig.MAX_SHOP_DOC_COUNT) {
                        throw new Exception("Chỉ được upload tối đa " + AppConfig.MAX_SHOP_DOC_COUNT + " tài liệu.");
                    }

                    // Tạm dùng user_id=0 vì user chưa được tạo; sẽ di chuyển sau khi có ID
                    // Pattern: lưu vào temp folder /uploads/shop-docs/tmp/{uuid}/
                    String savedPath = FileUploadUtil.saveShopDoc(part, uploadDir, 0);
                    if (savedPath != null) {
                        docPathList.add("\"" + savedPath + "\"");
                        docCount++;
                    }
                }

                if (!docPathList.isEmpty()) {
                    docPathsJson = "[" + String.join(",", docPathList) + "]";
                }
            }

            // 5. Xử lý Đăng Ký qua Service
            User newUser = authService.register(user, storeName, address, preferredCategoriesJson, docPathsJson);

            // Gửi email xác nhận nhận đơn đăng ký shop async
            if ("SHOP_OWNER".equals(newUser.getRole())) {
                final String finalStoreName = storeName;
                new Thread(() -> {
                    try {
                        emailService.sendShopApplicationReceivedEmail(newUser.getEmail(), newUser.getFullName(), finalStoreName);
                    } catch (Exception ex) {
                        getServletContext().log("Không thể gửi email nhận đơn đăng ký shop cho " + newUser.getEmail(), ex);
                    }
                }).start();
            }

            // 6. Xử lý thành công - PRG pattern
            SessionUtil.flashSuccess(req.getSession(), "Đăng ký thành công! Vui lòng kiểm tra email để xác minh trong 5 phút.");
            req.getSession().setAttribute(AppConfig.SESSION_VERIFY_EMAIL, newUser.getEmail());
            resp.sendRedirect(req.getContextPath() + "/auth/verify");

        } catch (Exception e) {
            getServletContext().log("RegisterServlet error: " + e.getMessage(), e);
            forwardWithError(req, resp, e.getMessage());
        }
    }

    /** Forward về form với thông báo lỗi và load lại categories */
    private void forwardWithError(HttpServletRequest req, HttpServletResponse resp, String errorMsg)
            throws ServletException, IOException {
        try {
            req.setAttribute("categories", categoryDAO.findAllActive());
        } catch (SQLException ex) {
            req.setAttribute("categories", new ArrayList<>());
        }
        req.setAttribute("errorMsg", errorMsg);
        req.getRequestDispatcher("/WEB-INF/jsp/auth/register.jsp").forward(req, resp);
    }
}
