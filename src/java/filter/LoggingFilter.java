package filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * LoggingFilter — Log mỗi request: method, URI, thời gian xử lý.
 *
 * Output ví dụ: GET /products?page=1 [user=admin@example.com] 45ms
 * THỨ TỰ CHẠY: 2
 * @author fruitmkt-team
 */
public class LoggingFilter implements Filter {

    private static final Logger LOG = Logger.getLogger(LoggingFilter.class.getName());

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        long start = System.currentTimeMillis();
        try {
            chain.doFilter(request, response);
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            Object requestId = req.getAttribute("requestId");
            String requestRef = requestId == null ? "-" : String.valueOf(requestId);
            LOG.info(String.format("[%s] %s %s -> %d [%dms]",
                    requestRef,
                    req.getMethod(),
                    req.getRequestURI(),
                    resp.getStatus(),
                    elapsed));
        }
    }
}
