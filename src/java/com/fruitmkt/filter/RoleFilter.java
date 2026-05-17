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
@WebFilter(urlPatterns = {"/admin/*", "/shop/*", "/delivery/*"})
public class RoleFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest  req  = (HttpServletRequest)  request;
        HttpServletResponse resp = (HttpServletResponse) response;

        User user = SessionUtil.getCurrentUser(req.getSession(false));
        String uri = req.getRequestURI();
        String ctx = req.getContextPath();

        boolean allowed = false;
        if (uri.startsWith(ctx + "/admin/"))    allowed = AppConfig.ROLE_ADMIN.equals(user.getRole());
        else if (uri.startsWith(ctx + "/shop/")) allowed = AppConfig.ROLE_SHOP_OWNER.equals(user.getRole());
        else if (uri.startsWith(ctx + "/delivery/")) allowed = AppConfig.ROLE_DELIVERY.equals(user.getRole());
        else allowed = true;

        if (!allowed) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập trang này.");
            return;
        }
        chain.doFilter(request, response);
    }
}
