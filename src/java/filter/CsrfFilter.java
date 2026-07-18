package filter;

import config.AppConfig;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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

        // Chỉ kiểm tra POST đối với các route protected.
        // /api/payment/webhook bỏ qua: SePay webhook không dùng session cookie — CSRF N/A.
        //   Các /api/* khác vẫn được kiểm tra CSRF khi POST, kể cả endpoint công khai
        //   nếu client chủ động gửi token (ví dụ AI search).
        // /auth/* bỏ qua ở filter-level vì LoginServlet, RegisterServlet, ForgotPasswordServlet
        //   đã tự check CSRF token thủ công (manual check). Token vẫn được tạo ở trên
        //   cho mọi request nên form luôn có token khi render.
        // /cart không còn bypass riêng cho syncOnUnload; client phải gửi CSRF token.
        // /ws/* bỏ qua: WebSocket handshake là GET với header Upgrade — không có CSRF token body.
        // GET / HEAD / OPTIONS không thay đổi trạng thái — không cần CSRF.
        String method = req.getMethod();
        if ("POST".equalsIgnoreCase(method)
                && !isSePayWebhook(req)
                && !req.getRequestURI().startsWith(req.getContextPath() + "/auth/")
                && !req.getRequestURI().startsWith(req.getContextPath() + "/ws/")) {
            String sessionToken = (String) session.getAttribute(AppConfig.SESSION_CSRF_TOKEN);
            String requestToken = req.getParameter("_csrf");
            if (requestToken == null || requestToken.trim().isEmpty()) {
                requestToken = req.getHeader("X-CSRF-Token");
            }
            if (requestToken == null || requestToken.trim().isEmpty()) {
                requestToken = req.getHeader("X-XSRF-TOKEN");
            }
            
            if (sessionToken == null || !sessionToken.equals(requestToken)) {
                boolean isAjax = "XMLHttpRequest".equals(req.getHeader("X-Requested-With"))
                        || "json".equals(req.getParameter("format"))
                        || (req.getHeader("Accept") != null && req.getHeader("Accept").contains("application/json"));
                
                if (isAjax) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.setContentType("application/json;charset=UTF-8");
                    resp.getWriter().write("{\"success\":false,\"error\":\"CSRF token không hợp lệ.\"}");
                } else {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token không hợp lệ.");
                }
                return;
            }
        }
        chain.doFilter(request, response);
    }

    /**
     * Trả về true nếu request là SePay webhook POST.
     * Đây là endpoint duy nhất trong /api/* được miễn CSRF vì không dùng session cookie.
     */
    private boolean isSePayWebhook(HttpServletRequest req) {
        String uri = req.getRequestURI();
        String webhookPath = req.getContextPath() + AppConfig.SEPAY_WEBHOOK_PATH;
        return uri.equals(webhookPath) || uri.equals(webhookPath + "/");
    }

}
