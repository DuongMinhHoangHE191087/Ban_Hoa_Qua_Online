package com.fruitmkt.filter;

import com.fruitmkt.util.SessionUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;

/**
 * AuthFilter — Chặn truy cập các URL yêu cầu đăng nhập.
 *
 * CÁC URL BẢO VỆ: /customer/*, /shop/*, /delivery/*, /admin/*
 * Nếu chưa login → redirect về /auth/login?redirect=[url gốc]
 *
 * THỨ TỰ CHẠY: 4
 * @author fruitmkt-team
 */
@WebFilter(urlPatterns = {"/customer/*", "/shop/*", "/delivery/*", "/admin/*"})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest  req  = (HttpServletRequest)  request;
        HttpServletResponse resp = (HttpServletResponse) response;

        if (!SessionUtil.isLoggedIn(req.getSession(false))) {
            String redirectUrl = req.getRequestURI();
            resp.sendRedirect(req.getContextPath() + "/auth/login?redirect=" + redirectUrl);
            return;
        }
        chain.doFilter(request, response);
    }
}
