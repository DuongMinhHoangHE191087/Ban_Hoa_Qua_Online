package filter;

import dao.auth.UserDAO;
import model.entity.shop.ShopProfile;
import model.entity.auth.User;
import service.shop.ShopService;
import util.ActorAccessPolicy;
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
import java.util.Objects;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * RoleFilter — Kiểm tra role sau khi AuthFilter xác nhận đã login.
 *
 * LOGIC:
 *   /admin/*     → chỉ ADMIN
 *   /shop/*      → chỉ SHOP_OWNER
 *   /delivery/*  → chỉ DELIVERY
 *   /customer/*  → CUSTOMER
 *
 * THỨ TỰ CHẠY: 5
 * @author fruitmkt-team
 */
public class RoleFilter implements Filter {

    private static final Logger log = Logger.getLogger(RoleFilter.class.getName());
    private static final String SHOP_PROFILE_CACHE_KEY = "_shopProfile";

    private final ShopService shopService = new ShopService();

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
        if (uri.equals(ctx + "/shop/status") || uri.startsWith(ctx + "/shop/status")) {
            try {
                ShopProfile profile = resolveShopProfile(session, user);
                if (profile != null) {
                    allowed = true;
                } else {
                    resp.sendRedirect(ctx + "/auth/register");
                    return;
                }
            } catch (Exception e) {
                LoggerUtil.error(log, "Lỗi kiểm tra quyền vào shop status", e);
                throw new ServletException("Không thể kiểm tra trạng thái shop", e);
            }
        } else if (uri.equals(ctx + "/admin") || uri.startsWith(ctx + "/admin/")) {
            allowed = ActorAccessPolicy.isAdmin(user);
        } else if (uri.equals(ctx + "/shop/docs") || uri.startsWith(ctx + "/shop/docs")) {
            allowed = true; // ShopDocDownloadServlet performs its own internal ownership/role checks
        } else if (uri.equals(ctx + "/shop") || uri.startsWith(ctx + "/shop/")) {
            if (ActorAccessPolicy.isShopOwner(user)) {
                try {
                    ShopProfile profile = resolveShopProfile(session, user);
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
            } else {
                allowed = false;
            }
        } else if (uri.equals(ctx + "/delivery") || uri.startsWith(ctx + "/delivery/")) {
            allowed = ActorAccessPolicy.isDelivery(user);
        } else if (uri.equals(ctx + "/customer") || uri.startsWith(ctx + "/customer/")) {
            allowed = ActorAccessPolicy.canAccessCustomerArea(user);
        } else if (uri.equals(ctx + "/notifications")) {
            allowed = ActorAccessPolicy.isAdmin(user)
                    || ActorAccessPolicy.isCustomer(user)
                    || ActorAccessPolicy.isShopOwner(user)
                    || ActorAccessPolicy.isDelivery(user);
        } else if (uri.equals(ctx + "/checkout")) {
            allowed = ActorAccessPolicy.canAccessCustomerArea(user);
        } else if (uri.equals(ctx + "/orders")
                || uri.equals(ctx + "/orders/detail")) {
            allowed = ActorAccessPolicy.canAccessOrderArea(user);
        } else if (uri.equals(ctx + "/reviews")) {
            allowed = ActorAccessPolicy.canAccessCustomerArea(user);
        } else if (uri.equals(ctx + "/returns")) {
            allowed = ActorAccessPolicy.canAccessReturnRequestArea(user);
        } else if (uri.equals(ctx + "/chat")) {
            allowed = ActorAccessPolicy.canAccessCustomerArea(user);
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

    private ShopProfile resolveShopProfile(HttpSession session, User user) throws SQLException {
        if (session != null) {
            ShopProfile cachedProfile = (ShopProfile) session.getAttribute(SHOP_PROFILE_CACHE_KEY);
            if (cachedProfile != null) {
                return cachedProfile;
            }
        }

        ShopProfile profile = shopService.getShopByUserId(user.getUserId());
        if (profile != null && session != null) {
            session.setAttribute(SHOP_PROFILE_CACHE_KEY, profile);
        }
        return profile;
    }
}
