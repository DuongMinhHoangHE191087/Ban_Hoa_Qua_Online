package filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * SecurityHeadersFilter — Thêm HTTP security headers vào mọi response.
 *
 * Headers được set:
 *   - X-Content-Type-Options: nosniff
 *   - X-Frame-Options: DENY
 *   - Referrer-Policy: strict-origin-when-cross-origin
 *   - Content-Security-Policy: cho phép 'self' + các CDN đang dùng trong JSPs
 *   - Strict-Transport-Security: chỉ khi request đến qua HTTPS
 *
 * THỨ TỰ CHẠY: Đầu tiên (trước EncodingFilter) — đảm bảo headers luôn được gửi.
 * @author fruitmkt-team
 */
public class SecurityHeadersFilter implements Filter {

    /**
     * Content-Security-Policy.
     *
     * Các nguồn bên ngoài đang được dùng trong JSPs:
     *   - fonts.googleapis.com  — Google Fonts stylesheet
     *   - fonts.gstatic.com     — Google Fonts font files
     *   - cdn.jsdelivr.net      — Chart.js (admin/report.jsp, shop/report.jsp)
     *   - cdnjs.cloudflare.com  — Font Awesome (shop/report.jsp)
     *
     * 'unsafe-inline' cho style-src và script-src được giữ để tránh vỡ UI hiện tại
     * vì toàn bộ JSP đang dùng inline styles/scripts. Khi JSPs được refactor, hãy
     * thay thế bằng nonces hoặc hashes để loại bỏ 'unsafe-inline'.
     *
     * img-src bao gồm data: vì một số trang dùng data URI cho ảnh thumbnail.
     * connect-src 'self' cho phép XMLHttpRequest / Fetch tới cùng origin.
     */
    private static final String CSP =
        "default-src 'self'; " +
        "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; " +
        "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com https://cdnjs.cloudflare.com; " +
        "font-src 'self' https://fonts.gstatic.com https://cdnjs.cloudflare.com; " +
        "img-src 'self' data: https:; " +
        "connect-src 'self'; " +
        "frame-src 'none'; " +
        "object-src 'none'; " +
        "base-uri 'self'";

    /**
     * HSTS: khai báo HTTPS trong 1 năm, bao gồm subdomain.
     * Chỉ gửi khi request đến qua HTTPS để tránh downgrade loop ở môi trường HTTP dev.
     */
    private static final String HSTS = "max-age=31536000; includeSubDomains";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse resp = (HttpServletResponse) response;

        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("X-Frame-Options", "DENY");
        resp.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        resp.setHeader("Content-Security-Policy", CSP);

        // Strict-Transport-Security chỉ gửi qua HTTPS — không gửi qua HTTP dev
        if (request.isSecure()) {
            resp.setHeader("Strict-Transport-Security", HSTS);
        }

        chain.doFilter(request, response);
    }

    @Override public void init(FilterConfig fc) {}
    @Override public void destroy() {}
}
