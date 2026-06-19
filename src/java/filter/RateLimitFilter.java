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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * RateLimitFilter — Giới hạn tốc độ request theo IP để chống brute-force và DoS.
 *
 * Quy tắc:
 *   - /auth/login, /auth/register: tối đa 5 request/phút mỗi IP
 *   - /api/*: tối đa 60 request/phút mỗi IP
 *
 * Phản hồi khi vượt: HTTP 429 Too Many Requests + header Retry-After
 *
 * Bảo vệ X-Forwarded-For spoofing:
 *   - Chỉ tin X-Forwarded-For / X-Real-IP nếu kết nối đến từ proxy đáng tin cậy
 *     (loopback 127.0.0.1 / ::1 hoặc danh sách TRUSTED_PROXIES).
 *   - Ngược lại dùng req.getRemoteAddr() trực tiếp.
 *
 * Chống memory-exhaustion:
 *   - Giới hạn số IP được theo dõi (MAX_TRACKED_IPS mỗi bucket).
 *   - Dọn dẹp định kỳ mọi 5 phút: xóa entry rỗng/expired.
 *
 * @author fruitmkt-team
 */
public class RateLimitFilter implements Filter {

    // Giới hạn cho từng nhóm URL (requests/phút)
    private static final int  AUTH_MAX_RPM     = 5;
    private static final int  API_MAX_RPM      = 60;
    private static final long WINDOW_MS        = 60_000L;

    // Giới hạn số IP tối đa được theo dõi mỗi bucket — ngăn OOM khi flood từ nhiều IP
    private static final int  MAX_TRACKED_IPS  = 10_000;

    // Proxy nội bộ đáng tin cậy — chỉ những địa chỉ này mới được tin X-Forwarded-For
    private static final Set<String> TRUSTED_PROXIES = Set.of(
        "127.0.0.1",
        "::1"
        // Thêm IP load-balancer / reverse-proxy nội bộ ở đây khi cần
    );

    // IP → timestamps của requests (dùng sliding window)
    private final Map<String, Deque<Long>> authBucket = new ConcurrentHashMap<>();
    private final Map<String, Deque<Long>> apiBucket  = new ConcurrentHashMap<>();

    private ScheduledExecutorService cleaner;

    @Override
    public void init(FilterConfig fc) {
        // Dọn dẹp entry rỗng/expired mỗi 5 phút để tránh rò rỉ bộ nhớ
        cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "rate-limit-cleaner");
            t.setDaemon(true);
            return t;
        });
        cleaner.scheduleAtFixedRate(() -> {
            evictStale(authBucket);
            evictStale(apiBucket);
        }, 5, 5, TimeUnit.MINUTES);
    }

    @Override
    public void destroy() {
        if (cleaner != null) {
            cleaner.shutdownNow();
        }
    }

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

        // Từ chối khi bucket đã đầy và IP mới chưa có entry — tránh OOM
        if (!bucket.containsKey(ip) && bucket.size() >= MAX_TRACKED_IPS) {
            return false;
        }

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

    /** Xóa các entry rỗng hoặc đã hết cửa sổ khỏi bucket. */
    private void evictStale(Map<String, Deque<Long>> bucket) {
        long cutoff = System.currentTimeMillis() - WINDOW_MS;
        Iterator<Map.Entry<String, Deque<Long>>> it = bucket.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Deque<Long>> entry = it.next();
            Deque<Long> deque = entry.getValue();
            synchronized (deque) {
                // Xóa timestamps cũ
                while (!deque.isEmpty() && deque.peekFirst() <= cutoff) {
                    deque.pollFirst();
                }
                // Nếu hàng đợi rỗng thì xóa entry hoàn toàn
                if (deque.isEmpty()) {
                    it.remove();
                }
            }
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

    /**
     * Lấy IP thực của client.
     *
     * Chỉ tin X-Forwarded-For / X-Real-IP khi kết nối TCP đến từ một proxy đáng tin cậy
     * (TRUSTED_PROXIES). Nếu không, dùng getRemoteAddr() để tránh spoofing.
     */
    private String getClientIp(HttpServletRequest req) {
        String remoteAddr = req.getRemoteAddr();

        if (isTrustedProxy(remoteAddr)) {
            // Tin X-Forwarded-For: lấy IP đầu tiên (leftmost = client thật)
            String xForwardedFor = req.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()
                    && !"unknown".equalsIgnoreCase(xForwardedFor)) {
                int commaIdx = xForwardedFor.indexOf(',');
                String candidate = commaIdx > 0
                        ? xForwardedFor.substring(0, commaIdx).trim()
                        : xForwardedFor.trim();
                if (!candidate.isEmpty()) {
                    return candidate;
                }
            }
            // Fallback: X-Real-IP
            String xRealIp = req.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty()) {
                return xRealIp.trim();
            }
        }

        // Kết nối trực tiếp hoặc proxy không tin cậy — dùng địa chỉ TCP thực
        return remoteAddr;
    }

    private boolean isTrustedProxy(String addr) {
        if (addr == null) return false;
        return TRUSTED_PROXIES.contains(addr);
    }

    private void sendTooManyRequests(HttpServletResponse resp, int retryAfterSeconds)
            throws IOException {
        resp.setStatus(429);
        resp.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write("{\"success\":false,\"message\":\"Quá nhiều yêu cầu. Vui lòng thử lại sau " + retryAfterSeconds + " giây.\"}");
    }
}
