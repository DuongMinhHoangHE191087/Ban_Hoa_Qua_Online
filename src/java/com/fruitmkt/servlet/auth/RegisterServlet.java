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
        HttpSession session = req.getSession(false);
        User currentUser = SessionUtil.getCurrentUser(session);
        if (currentUser != null) {
            if ("SHOP_OWNER".equals(currentUser.getRole())) {
                resp.sendRedirect(req.getContextPath() + "/shop/dashboard");
                return;
            } else if ("ADMIN".equals(currentUser.getRole()) || "DELIVERY".equals(currentUser.getRole())) {
                resp.sendRedirect(req.getContextPath() + "/");
                return;
            }
            req.setAttribute("prefilledUser", currentUser);
        }

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

        // DEBUG LOGGING
        getServletContext().log("DEBUG RegisterServlet: Content-Type = " + req.getContentType());
        getServletContext().log("DEBUG RegisterServlet: Character-Encoding = " + req.getCharacterEncoding());
        java.util.Enumeration<String> paramNames = req.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            getServletContext().log("DEBUG RegisterServlet parameter: " + paramName + " = " + req.getParameter(paramName));
        }
        try {
            java.util.Collection<Part> parts = req.getParts();
            if (parts != null) {
                for (Part part : parts) {
                    getServletContext().log("DEBUG RegisterServlet part: name=" + part.getName() 
                        + ", size=" + part.getSize() + ", contentType=" + part.getContentType());
                }
            } else {
                getServletContext().log("DEBUG RegisterServlet parts is NULL");
            }
        } catch (Exception ex) {
            getServletContext().log("DEBUG RegisterServlet error reading parts: " + ex.getMessage(), ex);
        }

        // 1. Kiểm tra CSRF token thủ công nghiêm ngặt vì /auth/* bị CsrfFilter bỏ qua
        String sessionCsrf = (String) req.getSession().getAttribute(AppConfig.SESSION_CSRF_TOKEN);
        String reqCsrf = req.getParameter("_csrf");
        if (sessionCsrf == null || !sessionCsrf.equals(reqCsrf)) {
            forwardWithError(req, resp, "CSRF token không hợp lệ hoặc phiên làm việc đã hết hạn.");
            return;
        }

        // Check if the user is already logged in (upgrading to SHOP_OWNER)
        HttpSession session = req.getSession(false);
        User currentUser = SessionUtil.getCurrentUser(session);
        if (currentUser != null) {
            handleUserUpgrade(req, resp, currentUser);
            return;
        }

        // 2. Nhận các tham số từ form đăng ký
        String role = req.getParameter("accountType"); // 'CUSTOMER' hoặc 'SHOP_OWNER'
        String fullName = req.getParameter("fullName");
        String email = req.getParameter("email");
        String phone = req.getParameter("phone");
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");

        User newUser = null;
        try {
            // 3. XSS Sanitization - Làm sạch toàn bộ đầu vào văn bản để chống XSS Injection
            fullName = sanitizeInput(fullName);
            email = sanitizeInput(email);
            phone = sanitizeInput(phone);
            phone = com.fruitmkt.util.ValidationUtil.normalizePhone(phone);

            // 4. Validate phía Servlet bằng ValidationUtil
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

            // 5. Đưa dữ liệu thô đã làm sạch vào Entity
            User user = new User();
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPasswordHash(password);
            user.setPhone(phone);
            user.setRole(role);

            String storeName = req.getParameter("storeName");
            String address = req.getParameter("address");
            String businessEmail = req.getParameter("businessEmail");
            String preferredCategoriesJson = null;

            // 6. Xử lý riêng cho SHOP_OWNER
            if ("SHOP_OWNER".equals(role)) {
                storeName = sanitizeInput(storeName);
                address = sanitizeInput(address);
                businessEmail = sanitizeInput(businessEmail);
                storeName = com.fruitmkt.util.ValidationUtil.requireValidShopName(storeName, "Tên cửa hàng");
                address = com.fruitmkt.util.ValidationUtil.requireValidAddress(address, "Địa chỉ kinh doanh");
                businessEmail = com.fruitmkt.util.ValidationUtil.requireValidEmail(businessEmail, "Email liên hệ kinh doanh");

                com.fruitmkt.dao.ShopProfileDAO shopProfileDAO = new com.fruitmkt.dao.ShopProfileDAO();
                if (shopProfileDAO.isBusinessEmailExists(businessEmail)) {
                    throw new Exception("Mỗi doanh nghiệp chỉ được đăng ký tối đa 1 gian hàng! Email kinh doanh này đã được sử dụng.");
                }

                // 6a. Xử lý danh mục kinh doanh → JSON array
                String[] catIds = req.getParameterValues("categoryIds");
                if (catIds != null && catIds.length > 0) {
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < catIds.length; i++) {
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

                // 6b. Thẩm định kích thước & định dạng tệp trước khi lưu (Zero-Guessing Validation)
                List<Part> docParts = new ArrayList<>(req.getParts());
                int docCount = 0;
                for (Part part : docParts) {
                    if (!"businessDocs".equals(part.getName())) continue;
                    if (part.getSize() == 0) continue;

                    String docError = com.fruitmkt.util.ValidationUtil.validateShopDoc(part.getSubmittedFileName(), part.getSize());
                    if (docError != null) {
                        throw new Exception(docError);
                    }
                    docCount++;
                }

                if (docCount > AppConfig.MAX_SHOP_DOC_COUNT) {
                    throw new Exception("Chỉ được upload tối đa " + AppConfig.MAX_SHOP_DOC_COUNT + " tài liệu.");
                }
            }

            // 7. Xử lý Đăng Ký Tài khoản & Shop cơ sở qua Service (docPathsJson ban đầu để null để đồng bộ an toàn)
            newUser = authService.register(user, storeName, address, preferredCategoriesJson, null);

            // 8. Thực hiện Upload thực tế trực tiếp dưới thư mục lưu trữ phân lập theo userId chính xác và lưu nháp ShopProfile vào session
            if ("SHOP_OWNER".equals(role)) {
                String uploadDir = getServletContext().getRealPath("");
                List<String> docPathList = new ArrayList<>();
                List<Part> docParts = new ArrayList<>(req.getParts());
                for (Part part : docParts) {
                    if (!"businessDocs".equals(part.getName())) continue;
                    if (part.getSize() == 0) continue;

                    // Lưu trực tiếp file vào thư mục phân lập uploads/shop-docs/{userId}/ của newUser
                    String savedPath = FileUploadUtil.saveShopDoc(part, uploadDir, newUser.getUserId());
                    if (savedPath != null) {
                        docPathList.add("\"" + savedPath + "\"");
                    }
                }

                String docPathsJson = docPathList.isEmpty() ? null : "[" + String.join(",", docPathList) + "]";

                // Khởi tạo ShopProfile object nhưng chưa lưu vào DB, chỉ lưu vào session để tránh đơn rác
                com.fruitmkt.model.entity.ShopProfile profile = new com.fruitmkt.model.entity.ShopProfile();
                profile.setUserId(newUser.getUserId());
                profile.setShopName(storeName != null && !storeName.trim().isEmpty() ? storeName : "Cửa hàng của " + newUser.getFullName());
                profile.setShopDescription("Chào mừng tới cửa hàng của chúng tôi!");
                profile.setApprovalStatus("PENDING");
                profile.setDeliveryAddress(address != null ? address : newUser.getUserAddress());
                profile.setRating(java.math.BigDecimal.ZERO);
                profile.setPreferredCategories(preferredCategoriesJson);
                profile.setDocPaths(docPathsJson);
                profile.setBusinessEmail(businessEmail);

                req.getSession().setAttribute("pendingShopProfile", profile);
            }

            // 9. Xử lý thành công - PRG pattern
            SessionUtil.flashSuccess(req.getSession(), "Đăng ký thành công! Vui lòng kiểm tra email để xác minh trong 5 phút.");
            req.getSession().setAttribute(AppConfig.SESSION_VERIFY_EMAIL, newUser.getEmail());
            resp.sendRedirect(req.getContextPath() + "/auth/verify");

        } catch (Exception e) {
            // Rollback: Xóa user vừa tạo nếu có lỗi ở bước sau (như upload file hoặc tạo profile shop)
            if (newUser != null) {
                try {
                    com.fruitmkt.dao.UserDAO userDAO = new com.fruitmkt.dao.UserDAO();
                    userDAO.deleteUser(newUser.getUserId());
                } catch (Exception rollbackEx) {
                    getServletContext().log("Rollback user failed: " + rollbackEx.getMessage(), rollbackEx);
                }
            }
            getServletContext().log("RegisterServlet error: " + e.getMessage(), e);
            forwardWithError(req, resp, e.getMessage());
        }
    }

    /** XSS Sanitizer helper to strip HTML tags from user inputs */
    private String sanitizeInput(String input) {
        if (input == null) return null;
        // Strip out HTML tags to defend against XSS Injection attacks
        return input.replaceAll("<[^>]*>", "").trim();
    }

    private void handleUserUpgrade(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws ServletException, IOException {
        String storeName = req.getParameter("storeName");
        String address = req.getParameter("address");
        String businessEmail = req.getParameter("businessEmail");
        String preferredCategoriesJson = null;

        try {
            // Check if they already have a shop profile
            com.fruitmkt.dao.ShopProfileDAO shopProfileDAO = new com.fruitmkt.dao.ShopProfileDAO();
            List<com.fruitmkt.model.entity.ShopProfile> existing = shopProfileDAO.findByUserId(currentUser.getUserId());
            if (!existing.isEmpty()) {
                throw new Exception("Tài khoản của bạn đã đăng ký hoặc nộp đơn mở cửa hàng rồi.");
            }

            storeName = sanitizeInput(storeName);
            address = sanitizeInput(address);
            businessEmail = sanitizeInput(businessEmail);
            storeName = com.fruitmkt.util.ValidationUtil.requireValidShopName(storeName, "Tên cửa hàng");
            address = com.fruitmkt.util.ValidationUtil.requireValidAddress(address, "Địa chỉ kinh doanh");
            businessEmail = com.fruitmkt.util.ValidationUtil.requireValidEmail(businessEmail, "Email liên hệ kinh doanh");

            if (shopProfileDAO.isBusinessEmailExists(businessEmail)) {
                throw new Exception("Mỗi doanh nghiệp chỉ được đăng ký tối đa 1 gian hàng! Email kinh doanh này đã được sử dụng.");
            }

            // Xử lý danh mục kinh doanh → JSON array
            String[] catIds = req.getParameterValues("categoryIds");
            if (catIds != null && catIds.length > 0) {
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < catIds.length; i++) {
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

            // Thẩm định kích thước & định dạng tệp trước khi lưu (Zero-Guessing Validation)
            List<Part> docParts = new ArrayList<>(req.getParts());
            int docCount = 0;
            for (Part part : docParts) {
                if (!"businessDocs".equals(part.getName())) continue;
                if (part.getSize() == 0) continue;

                String docError = com.fruitmkt.util.ValidationUtil.validateShopDoc(part.getSubmittedFileName(), part.getSize());
                if (docError != null) {
                    throw new Exception(docError);
                }
                docCount++;
            }

            if (docCount > AppConfig.MAX_SHOP_DOC_COUNT) {
                throw new Exception("Chỉ được upload tối đa " + AppConfig.MAX_SHOP_DOC_COUNT + " tài liệu.");
            }

            // Tạo ShopProfile mới với PENDING
            com.fruitmkt.model.entity.ShopProfile profile = new com.fruitmkt.model.entity.ShopProfile();
            profile.setUserId(currentUser.getUserId());
            profile.setShopName(storeName);
            profile.setShopDescription("Chào mừng tới cửa hàng của chúng tôi!");
            profile.setApprovalStatus("PENDING");
            profile.setDeliveryAddress(address);
            profile.setRating(java.math.BigDecimal.ZERO);
            profile.setPreferredCategories(preferredCategoriesJson);
            profile.setBusinessEmail(businessEmail);
            
            int profileId = shopProfileDAO.save(profile);

            // Thực hiện Upload thực tế trực tiếp dưới thư mục lưu trữ phân lập theo userId chính xác
            String uploadDir = getServletContext().getRealPath("");
            List<String> docPathList = new ArrayList<>();
            for (Part part : docParts) {
                if (!"businessDocs".equals(part.getName())) continue;
                if (part.getSize() == 0) continue;

                String savedPath = FileUploadUtil.saveShopDoc(part, uploadDir, currentUser.getUserId());
                if (savedPath != null) {
                    docPathList.add("\"" + savedPath + "\"");
                }
            }

            if (!docPathList.isEmpty()) {
                String docPathsJson = "[" + String.join(",", docPathList) + "]";
                shopProfileDAO.updateDocPaths(profileId, docPathsJson);
            }

            // Cập nhật vai trò thành SHOP_OWNER trong DB
            com.fruitmkt.dao.UserDAO userDAO = new com.fruitmkt.dao.UserDAO();
            userDAO.updateRole(currentUser.getUserId(), "SHOP_OWNER");

            // Cập nhật session user
            currentUser.setRole("SHOP_OWNER");
            SessionUtil.setCurrentUser(req.getSession(), currentUser);

            // Gửi email xác nhận nhận đơn đăng ký shop async
            final String finalStoreName = storeName;
            new Thread(() -> {
                try {
                    emailService.sendShopApplicationReceivedEmail(currentUser.getEmail(), currentUser.getFullName(), finalStoreName);
                } catch (Exception ex) {
                    getServletContext().log("Không thể gửi email nhận đơn đăng ký shop cho " + currentUser.getEmail(), ex);
                }
            }).start();

            // Xử lý thành công
            SessionUtil.flashSuccess(req.getSession(), "Đăng ký mở cửa hàng thành công! Đang chờ Admin phê duyệt đơn đăng ký của bạn.");
            resp.sendRedirect(req.getContextPath() + "/");

        } catch (Exception e) {
            getServletContext().log("RegisterServlet upgrade error: " + e.getMessage(), e);
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
        HttpSession session = req.getSession(false);
        User currentUser = SessionUtil.getCurrentUser(session);
        if (currentUser != null) {
            req.setAttribute("prefilledUser", currentUser);
        }
        req.setAttribute("errorMsg", errorMsg);
        req.getRequestDispatcher("/WEB-INF/jsp/auth/register.jsp").forward(req, resp);
    }
}
