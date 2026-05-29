package com.fruitmkt.filter;

import com.fruitmkt.config.AppConfig;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.UUID;

/**
 * CsrfFilter — Bảo vệ chống CSRF cho mọi form POST.
 *
 * CÁCH DÙNG TRONG JSP (thêm vào mọi form):
 * <pre>
 *   {@code <input type="hidden" name="_csrf" value="">}
 * </pre>
 *
 * Token được tạo khi session bắt đầu và xác minh mỗi POST.
 * THỨ TỰ CHẠY: 3
 * @author fruitmkt-team
 */
@WebFilter("/*")
public class CsrfFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest  req  = (HttpServletRequest)  request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // Đảm bảo CSRF token tồn tại trong session
        HttpSession session = req.getSession(true);
        if (session.getAttribute(AppConfig.SESSION_CSRF_TOKEN) == null) {
            session.setAttribute(AppConfig.SESSION_CSRF_TOKEN, UUID.randomUUID().toString());
        }

        // Chỉ kiểm tra POST (bỏ qua GET, webhook /api/* và /auth/* trong lúc phát triển)
        if ("POST".equalsIgnoreCase(req.getMethod()) 
                && !req.getRequestURI().startsWith(req.getContextPath() + "/api/")
                && !req.getRequestURI().startsWith(req.getContextPath() + "/auth/")) {
            String sessionToken = (String) session.getAttribute(AppConfig.SESSION_CSRF_TOKEN);
            String requestToken = req.getParameter("_csrf");
            if (requestToken == null || requestToken.trim().isEmpty()) {
                requestToken = req.getHeader("X-CSRF-Token");
            }
            if (requestToken == null || requestToken.trim().isEmpty()) {
                requestToken = req.getHeader("X-XSRF-TOKEN");
            }
            if (sessionToken == null || !sessionToken.equals(requestToken)) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token không hợp lệ.");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
