package com.fruitmkt.filter;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.util.SessionUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;

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
@WebFilter(urlPatterns = {"/admin/*", "/shop/*", "/delivery/*", "/customer/*", "/checkout", "/cart"})
public class RoleFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest  req  = (HttpServletRequest)  request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        User user = SessionUtil.getCurrentUser(session);
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
            allowed = AppConfig.ROLE_SHOP_OWNER.equals(user.getRole());
        } else if (uri.equals(ctx + "/delivery") || uri.startsWith(ctx + "/delivery/")) {
            allowed = AppConfig.ROLE_DELIVERY.equals(user.getRole());
        } else if (uri.equals(ctx + "/customer") || uri.startsWith(ctx + "/customer/")) {
            allowed = AppConfig.ROLE_CUSTOMER.equals(user.getRole());
        } else if (uri.equals(ctx + "/checkout") || uri.equals(ctx + "/cart")) {
            allowed = AppConfig.ROLE_CUSTOMER.equals(user.getRole());
        } else {
            allowed = true; // Các URL công cộng khác
        }

        if (!allowed) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập trang này.");
            return;
        }
        chain.doFilter(request, response);
    }
}
