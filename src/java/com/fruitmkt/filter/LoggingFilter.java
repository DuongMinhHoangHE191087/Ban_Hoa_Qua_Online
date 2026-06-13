package com.fruitmkt.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
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
        long start = System.currentTimeMillis();
        try {
            chain.doFilter(request, response);
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            LOG.info(String.format("%s %s [%dms]", req.getMethod(), req.getRequestURI(), elapsed));
        }
    }
}
