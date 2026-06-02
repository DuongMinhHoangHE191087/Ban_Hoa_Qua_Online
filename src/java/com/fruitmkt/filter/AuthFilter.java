package com.fruitmkt.filter;

import com.fruitmkt.dao.UserDAO;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.util.SessionUtil;
import com.fruitmkt.util.TokenUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * AuthFilter — Chặn truy cập các URL yêu cầu đăng nhập.
 *
 * CÁC URL BẢO VỆ: /customer/*, /shop/*, /delivery/*, /admin/*
 * Nếu chưa login → Tự động thẩm định qua Access Token & Refresh Token trước khi redirect.
 *
 * THỨ TỰ CHẠY: 4
 * @author fruitmkt-team
 */
public class AuthFilter implements Filter {

    private final UserDAO userDAO = new UserDAO();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest  req  = (HttpServletRequest)  request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        // Trường hợp đã có session hợp lệ
        if (SessionUtil.isLoggedIn(session)) {
            chain.doFilter(request, response);
            return;
        }

        // Trường hợp chưa có session hợp lệ -> Kiểm tra Access Token trong Cookie
        String accessToken = TokenUtil.getCookieValue(req, "accessToken");
        if (accessToken != null) {
            Integer userId = TokenUtil.verifyAccessToken(accessToken);
            if (userId != null) {
                try {
                    User user = userDAO.findUserById(userId);
                    if (user != null && "ACTIVE".equals(user.getStatus())) {
                        // Tự động khôi phục session đăng nhập
                        SessionUtil.setCurrentUser(req.getSession(true), user);
                        chain.doFilter(request, response);
                        return;
                    }
                } catch (SQLException e) {
                    req.getServletContext().log("Lỗi cơ sở dữ liệu khi khôi phục session qua Access Token: " + e.getMessage(), e);
                }
            }
        }

        // Access Token bị hết hạn hoặc không có -> Kiểm tra Refresh Token trong Cookie
        String refreshToken = TokenUtil.getCookieValue(req, "refreshToken");
        if (refreshToken != null) {
            try {
                Integer userId = userDAO.findUserIdBySessionToken(refreshToken);
                if (userId != null) {
                    User user = userDAO.findUserById(userId);
                    if (user != null && "ACTIVE".equals(user.getStatus())) {
                        // Tái cấp phát Access Token mới
                        String newAccessToken = TokenUtil.generateAccessToken(userId);
                        TokenUtil.addAccessTokenCookie(req, resp, newAccessToken);
                        
                        // Tự động khôi phục session đăng nhập
                        SessionUtil.setCurrentUser(req.getSession(true), user);
                        chain.doFilter(request, response);
                        return;
                    }
                } else {
                    // Refresh token không hợp lệ hoặc đã hết hạn trong database -> Xóa cookie
                    TokenUtil.clearTokens(req, resp);
                }
            } catch (SQLException e) {
                req.getServletContext().log("Lỗi cơ sở dữ liệu khi khôi phục session qua Refresh Token: " + e.getMessage(), e);
            }
        }

        // Không thể xác thực tự động -> Chuyển hướng sang trang Login kèm URL gốc
        String redirectUrl = req.getRequestURI();
        if (req.getQueryString() != null) {
            redirectUrl += "?" + req.getQueryString();
        }
        String encodedRedirect = URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8);
        resp.sendRedirect(req.getContextPath() + "/auth/login?redirect=" + encodedRedirect);
    }
}
