package filter;

import config.AppConfig;
import dao.shop.ShopProfileDAO;
import dao.auth.UserDAO;
import model.entity.shop.ShopProfile;
import model.entity.auth.User;
import util.LoggerUtil;
import util.SessionUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * RoleFilter — Kiểm tra role sau khi AuthFilter xác nhận đã login.
 *
 * LOGIC:
 *   /admin/*     → chỉ ADMIN
 *   /shop/*      → chỉ SHOP_OWNER
 *   /delivery/*  → chỉ DELIVERY
 *   /customer/*  → CUSTOMER (và SHOP_OWNER nếu cần)
 *
 * THỨ TỰ CHẠY: 5
 * @author fruitmkt-team
 */
public class RoleFilter implements Filter {

    private static final Logger log = Logger.getLogger(RoleFilter.class.getName());

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest  req  = (HttpServletRequest)  request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        User user = SessionUtil.getCurrentUser(session);

        // Đồng bộ hóa trạng thái/vai trò của người dùng từ Database để tránh lệch session khi Admin duyệt shop
        if (user != null) {
            try {
                User dbUser = new UserDAO().findUserById(user.getUserId());
                if (dbUser != null) {
                    if (!"ACTIVE".equals(dbUser.getStatus())) {
                        session.invalidate();
                        user = null;
                    } else if (!dbUser.getRole().equals(user.getRole())
                            || !Objects.equals(dbUser.getPhone(), user.getPhone())) {
                        SessionUtil.setCurrentUser(session, dbUser);
                        user = dbUser;
                    }
                }
            } catch (Exception e) {
                LoggerUtil.error(log, "Lỗi đồng bộ session trong RoleFilter", e);
                throw new ServletException("Không thể đồng bộ phiên đăng nhập", e);
            }
        }

        String uri = req.getRequestURI();
        String ctx = req.getContextPath();

        // 1. Kiểm tra nếu chưa đăng nhập -> Chuyển hướng về Login
        if (user == null) {
            resp.sendRedirect(ctx + "/auth/login");
            return;
        }

        // 2. Kiểm tra quyền truy cập theo Vai trò (RBAC)
        boolean allowed = false;
        if (uri.equals(ctx + "/admin") || uri.startsWith(ctx + "/admin/")) {
            allowed = AppConfig.ROLE_ADMIN.equals(user.getRole());
        } else if (uri.equals(ctx + "/shop") || uri.startsWith(ctx + "/shop/")) {
            if (AppConfig.ROLE_SHOP_OWNER.equals(user.getRole())) {
                if (uri.equals(ctx + "/shop/status") || uri.startsWith(ctx + "/shop/status")) {
                    allowed = true;
                } else {
                    try {
                        // Try to get cached profile from session to avoid DB query per request
                        ShopProfile cachedProfile = (ShopProfile) session.getAttribute("_shopProfile");
                        ShopProfile profile = null;

                        if (cachedProfile != null) {
                            profile = cachedProfile;
                        } else {
                            ShopProfileDAO shopProfileDAO = new ShopProfileDAO();
                            List<ShopProfile> profiles = shopProfileDAO.findByUserId(user.getUserId());
                            if (!profiles.isEmpty()) {
                                profile = profiles.get(0);
                                session.setAttribute("_shopProfile", profile);
                            }
                        }

                        if (profile != null && "APPROVED".equals(profile.getApprovalStatus())) {
                            allowed = true;
                        } else {
                            resp.sendRedirect(ctx + "/shop/status");
                            return;
                        }
                    } catch (Exception e) {
                        LoggerUtil.error(log, "Lỗi kiểm tra trạng thái shop profile", e);
                        throw new ServletException("Không thể kiểm tra trạng thái shop", e);
                    }
                }
            } else {
                allowed = false;
            }
        } else if (uri.equals(ctx + "/delivery") || uri.startsWith(ctx + "/delivery/")) {
            allowed = AppConfig.ROLE_DELIVERY.equals(user.getRole());
        } else if (uri.equals(ctx + "/customer") || uri.startsWith(ctx + "/customer/")) {
            allowed = AppConfig.ROLE_CUSTOMER.equals(user.getRole()) || AppConfig.ROLE_SHOP_OWNER.equals(user.getRole());
        } else if (uri.equals(ctx + "/checkout")
                || uri.equals(ctx + "/orders")
                || uri.equals(ctx + "/orders/detail")
                || uri.equals(ctx + "/notifications")
                || uri.equals(ctx + "/reviews")
                || uri.equals(ctx + "/returns")
                || uri.equals(ctx + "/chat")) {
            allowed = AppConfig.ROLE_CUSTOMER.equals(user.getRole()) || AppConfig.ROLE_SHOP_OWNER.equals(user.getRole());
        } else {
            allowed = true; // Các URL công cộng khác
        }

        if (!allowed) {
            boolean isAjax = "XMLHttpRequest".equals(req.getHeader("X-Requested-With"))
                    || "json".equals(req.getParameter("format"))
                    || (req.getHeader("Accept") != null && req.getHeader("Accept").contains("application/json"));
            
            if (isAjax) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"success\":false,\"error\":\"Bạn không có quyền truy cập trang này.\"}");
            } else {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập trang này.");
            }
            return;
        }
        chain.doFilter(request, response);
    }
}
