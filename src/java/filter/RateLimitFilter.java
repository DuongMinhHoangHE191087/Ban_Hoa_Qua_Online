package filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RateLimitFilter — Giới hạn tốc độ request theo IP để chống brute-force và DoS.
 *
 * Quy tắc:
 *   - /auth/login, /auth/register: tối đa 5 request/phút mỗi IP
 *   - /api/*: tối đa 60 request/phút mỗi IP
 *
 * Phản hồi khi vượt: HTTP 429 Too Many Requests + header Retry-After
 * Cấu hình qua FilterConfig params trong web.xml.
 *
 * @author fruitmkt-team
 */
public class RateLimitFilter implements Filter {

    // Giới hạn cho từng nhóm URL (requests/phút)
    private static final int AUTH_MAX_RPM    = 5;
    private static final int API_MAX_RPM     = 60;
    private static final long WINDOW_MS      = 60_000L;

    // IP → timestamps của requests (dùng sliding window)
    private final Map<String, Deque<Long>> authBucket = new ConcurrentHashMap<>();
    private final Map<String, Deque<Long>> apiBucket  = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  req  = (HttpServletRequest)  request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String uri = req.getRequestURI();
        String ip  = getClientIp(req);

        if (isAuthEndpoint(uri)) {
            if (!isAllowed(ip, authBucket, AUTH_MAX_RPM)) {
                sendTooManyRequests(resp, 60);
                return;
            }
        } else if (isApiEndpoint(uri)) {
            if (!isAllowed(ip, apiBucket, API_MAX_RPM)) {
                sendTooManyRequests(resp, 60);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    // Sliding window: loại bỏ timestamps cũ, kiểm tra giới hạn
    private boolean isAllowed(String ip, Map<String, Deque<Long>> bucket, int maxRpm) {
        long now = System.currentTimeMillis();
        bucket.putIfAbsent(ip, new ArrayDeque<>());
        Deque<Long> timestamps = bucket.get(ip);

        synchronized (timestamps) {
            // Xóa timestamps ngoài cửa sổ 1 phút
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > WINDOW_MS) {
                timestamps.pollFirst();
            }

            if (timestamps.size() >= maxRpm) {
                return false;
            }

            timestamps.addLast(now);
            return true;
        }
    }

    private boolean isAuthEndpoint(String uri) {
        return uri.contains("/auth/login") || uri.contains("/auth/register");
    }

    private boolean isApiEndpoint(String uri) {
        String path = uri;
        int ctxEnd = path.indexOf('/', 1);
        if (ctxEnd > 0) path = path.substring(ctxEnd);
        return path.startsWith("/api/");
    }

    private String getClientIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // X-Forwarded-For có thể chứa nhiều IP, lấy IP đầu tiên
            int commaIdx = ip.indexOf(',');
            return commaIdx > 0 ? ip.substring(0, commaIdx).trim() : ip.trim();
        }
        ip = req.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty()) return ip.trim();
        return req.getRemoteAddr();
    }

    private void sendTooManyRequests(HttpServletResponse resp, int retryAfterSeconds)
            throws IOException {
        resp.setStatus(429);
        resp.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write("{\"success\":false,\"message\":\"Quá nhiều yêu cầu. Vui lòng thử lại sau " + retryAfterSeconds + " giây.\"}");
    }

    @Override public void init(FilterConfig fc) {}
    @Override public void destroy() {}
}
