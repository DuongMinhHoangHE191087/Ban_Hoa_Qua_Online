package com.fruitmkt.servlet.admin;
import com.fruitmkt.config.AppConfig;
import com.fruitmkt.dao.ShopProfileDAO;
import com.fruitmkt.dao.UserDAO;
import com.fruitmkt.dao.CartDAO;
import com.fruitmkt.model.entity.ShopProfile;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.service.EmailService;
import com.fruitmkt.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
/* 
 * ShopApprovalServlet — Admin duyệt / từ chối đơn đăng ký shop.
 * GET : Hiển thị danh sách shop đang chờ duyệt (PENDING) và lịch sử
 * POST: Approve hoặc Reject shop — [BUGFIX] khi APPROVE đổi users.role = 'SHOP_OWNER'
 *
 * @author fruitmkt-team
 */
@WebServlet("/admin/shops")
public class ShopApprovalServlet extends HttpServlet {

    private final ShopProfileDAO shopProfileDAO = new ShopProfileDAO();
    private final UserDAO userDAO = new UserDAO();
    private final CartDAO cartDAO = new CartDAO();
    private final EmailService emailService = new EmailService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Kiểm tra quyền ADMIN (Defense in depth)
        if (!SessionUtil.isLoggedIn(req.getSession())) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }
        if (!SessionUtil.hasRole(req.getSession(), AppConfig.ROLE_ADMIN)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập trang này.");
            return;
        }

        String filter = req.getParameter("filter"); // PENDING | APPROVED | REJECTED | ALL
        if (filter == null || filter.trim().isEmpty()) {
            filter = "PENDING";
        }

        try {
            List<ShopProfile> profiles;
            if ("ALL".equalsIgnoreCase(filter)) {
                profiles = shopProfileDAO.findAll();
            } else {
                profiles = shopProfileDAO.findByApprovalStatus(filter.toUpperCase());
            }
            req.setAttribute("profiles", profiles);
            req.setAttribute("currentFilter", filter.toUpperCase());
        } catch (SQLException e) {
            getServletContext().log("ShopApprovalServlet GET error", e);
            req.setAttribute("errorMsg", "Không thể tải danh sách. Vui lòng thử lại.");
        }

        req.getRequestDispatcher("/WEB-INF/jsp/admin/shop-approvals.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Kiểm tra quyền ADMIN (Defense in depth)
        if (!SessionUtil.isLoggedIn(req.getSession())) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }
        if (!SessionUtil.hasRole(req.getSession(), AppConfig.ROLE_ADMIN)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền thực hiện hành động này.");
            return;
        }

        req.setCharacterEncoding("UTF-8");

        // Kiểm tra CSRF
        String sessionCsrf = (String) req.getSession().getAttribute(AppConfig.SESSION_CSRF_TOKEN);
        String reqCsrf = req.getParameter("_csrf");
        if (sessionCsrf != null && !sessionCsrf.equals(reqCsrf)) {
            SessionUtil.flashError(req.getSession(), "CSRF token không hợp lệ.");
            resp.sendRedirect(req.getContextPath() + "/admin/shops");
            return;
        }

        String action = req.getParameter("action");         // "approve" | "reject"
        String profileIdStr = req.getParameter("profileId");
        String userIdStr = req.getParameter("userId");      // userId của shop owner
        String rejectionReason = req.getParameter("rejectionReason");

        try {
            if (profileIdStr == null || userIdStr == null) {
                throw new Exception("Thiếu tham số profileId hoặc userId.");
            }

            int profileId = Integer.parseInt(profileIdStr.trim());
            int userId    = Integer.parseInt(userIdStr.trim());

            if ("approve".equalsIgnoreCase(action)) {
                // [BUGFIX] updateApprovalStatus() giờ cập nhật cả users.role = 'SHOP_OWNER'
                shopProfileDAO.updateApprovalStatus(profileId, userId, "APPROVED", null);

                // Tự động tạo giỏ hàng cho SHOP_OWNER mới (nếu chưa có)
                try {
                    if (cartDAO.findByCustomer(userId).isEmpty()) {
                        cartDAO.createForCustomer(userId);
                    }
                } catch (Exception ex) {
                    getServletContext().log("Tự động tạo giỏ hàng cho shop owner " + userId + " thất bại.", ex);
                }

                // Lấy thông tin để gửi email phê duyệt async
                try {
                    ShopProfile profile = shopProfileDAO.findById(profileId);
                    User user = userDAO.findUserById(userId);
                    if (profile != null && user != null) {
                        new Thread(() -> {
                            try {
                                emailService.sendShopApprovedEmail(user.getEmail(), user.getFullName(), profile.getShopName());
                            } catch (Exception ex) {
                                getServletContext().log("Không thể gửi email phê duyệt cho " + user.getEmail(), ex);
                            }
                        }).start();
                    }
                } catch (Exception ex) {
                    getServletContext().log("Lỗi khi chuẩn bị gửi email phê duyệt shop.", ex);
                }

                SessionUtil.flashSuccess(req.getSession(), "Đã phê duyệt shop thành công! Role người dùng đã được cập nhật và giỏ hàng đã được thiết lập.");

            } else if ("reject".equalsIgnoreCase(action)) {
                rejectionReason = com.fruitmkt.util.ValidationUtil.requireValidRejectionReason(rejectionReason, "Lý do từ chối");
                
                // Lấy thông tin shop profile trước khi update để giữ data gửi email
                ShopProfile profile = shopProfileDAO.findById(profileId);
                int actualUserId = (profile != null) ? profile.getUserId() : userId;

                // userId không cần khi reject (role không thay đổi)
                shopProfileDAO.updateApprovalStatus(profileId, 0, "REJECTED", rejectionReason);

                // Gửi email từ chối async
                try {
                    if (profile != null) {
                        User user = userDAO.findUserById(actualUserId);
                        if (user != null) {
                            final String finalReason = rejectionReason;
                            new Thread(() -> {
                                try {
                                    emailService.sendShopRejectedEmail(user.getEmail(), user.getFullName(), profile.getShopName(), finalReason);
                                } catch (Exception ex) {
                                    getServletContext().log("Không thể gửi email từ chối cho " + user.getEmail(), ex);
                                }
                            }).start();
                        }
                    }
                } catch (Exception ex) {
                    getServletContext().log("Lỗi khi gửi email từ chối shop.", ex);
                }

                SessionUtil.flashSuccess(req.getSession(), "Đã từ chối đơn đăng ký.");

            } else {
                throw new Exception("Hành động không hợp lệ: " + action);
            }

        } catch (NumberFormatException e) {
            SessionUtil.flashError(req.getSession(), "profileId hoặc userId không hợp lệ.");
        } catch (Exception e) {
            getServletContext().log("ShopApprovalServlet POST error: " + e.getMessage(), e);
            SessionUtil.flashError(req.getSession(), "Lỗi: " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/admin/shops");
    }
}
