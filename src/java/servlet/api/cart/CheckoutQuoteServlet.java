package servlet.api.cart;

import config.AppConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.dto.checkout.CheckoutQuoteRequestDTO;
import model.entity.auth.User;
import model.response.ApiResponse;
import service.cart.CheckoutPricingEngine;
import util.JsonUtil;
import util.SessionUtil;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * API chuẩn mới để preview quote checkout nhiều voucher/shop.
 */
@WebServlet("/api/checkout/quote")
public class CheckoutQuoteServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(CheckoutQuoteServlet.class.getName());

    private final CheckoutPricingEngine pricingEngine = new CheckoutPricingEngine();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession();
        User user = SessionUtil.getCurrentUser(session);
        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_UNAUTHORIZED, "Vui lòng đăng nhập."));
            return;
        }
        if (!AppConfig.ROLE_CUSTOMER.equals(user.getRole())) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền xem quote checkout."));
            return;
        }
        if (!isValidCsrf(session, resolveCsrfToken(req))) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_FORBIDDEN, "CSRF token không hợp lệ."));
            return;
        }

        try {
            CheckoutQuoteRequestDTO quoteRequest = buildRequest(req);
            CheckoutPricingEngine.CheckoutPricingSnapshot snapshot = pricingEngine.buildQuote(user, quoteRequest, false);
            resp.setStatus(HttpServletResponse.SC_OK);
            JsonUtil.writeJson(resp, ApiResponse.ok(snapshot.getQuote()));
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_BAD_REQUEST,
                    "Dữ liệu quote checkout không hợp lệ."));
        } catch (Exception e) {
            util.ServletUtil.sendJsonInternalServerError(
                    req,
                    resp,
                    log,
                    "CheckoutQuoteServlet#doPost",
                    "Không thể tính toán quote checkout.",
                    e);
        }
    }

    private CheckoutQuoteRequestDTO buildRequest(HttpServletRequest req) throws Exception {
        String contentType = req.getContentType();
        if (contentType != null && contentType.contains("application/json")) {
            byte[] bodyBytes = req.getInputStream().readAllBytes();
            String rawJson = new String(bodyBytes, java.nio.charset.StandardCharsets.UTF_8);
            CheckoutQuoteRequestDTO parsed = JsonUtil.fromJson(rawJson, CheckoutQuoteRequestDTO.class);
            normalizeCouponLists(parsed);
            return parsed;
        }

        CheckoutQuoteRequestDTO request = new CheckoutQuoteRequestDTO();
        request.setVariantIds(parseVariantIds(req.getParameter("variantIds")));
        request.setDeliveryAddress(req.getParameter("deliveryAddress"));
        request.setDeliveryTimeSlot(req.getParameter("deliveryTimeSlot"));
        request.setPaymentMethod(req.getParameter("paymentMethod"));
        request.setShopCouponCodes(parseCouponCodes(req.getParameterValues("shopCouponCodes"), req.getParameter("shopCouponCode")));
        request.setSystemCouponCodes(parseCouponCodes(req.getParameterValues("systemCouponCodes"), req.getParameter("systemCouponCode")));
        normalizeCouponLists(request);
        return request;
    }

    private void normalizeCouponLists(CheckoutQuoteRequestDTO request) {
        request.setShopCouponCodes(parseCouponCodes(request.getShopCouponCodes()));
        request.setSystemCouponCodes(parseCouponCodes(request.getSystemCouponCodes()));
    }

    private List<String> parseCouponCodes(String[] values, String fallbackCsv) {
        List<String> codes = new ArrayList<>();
        if (values != null && values.length > 0) {
            for (String value : values) {
                appendCouponCodes(codes, value);
            }
            return codes;
        }
        appendCouponCodes(codes, fallbackCsv);
        return codes;
    }

    private List<String> parseCouponCodes(List<String> values) {
        List<String> codes = new ArrayList<>();
        if (values == null) {
            return codes;
        }
        for (String value : values) {
            appendCouponCodes(codes, value);
        }
        return codes;
    }

    private void appendCouponCodes(List<String> codes, String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return;
        }
        for (String part : raw.split(",")) {
            String normalized = part != null ? part.trim().toUpperCase() : null;
            if (normalized != null && !normalized.isEmpty() && !codes.contains(normalized)) {
                codes.add(normalized);
            }
        }
    }

    private List<Integer> parseVariantIds(String variantIdsParam) {
        List<Integer> variantIds = new ArrayList<>();
        if (variantIdsParam == null || variantIdsParam.trim().isEmpty()) {
            return variantIds;
        }
        for (String part : variantIdsParam.split(",")) {
            try {
                variantIds.add(Integer.parseInt(part.trim()));
            } catch (NumberFormatException ignored) {
                // Bỏ qua giá trị không hợp lệ để engine xử lý danh sách còn lại.
            }
        }
        return variantIds;
    }

    private String resolveCsrfToken(HttpServletRequest req) {
        String token = req.getParameter("_csrf");
        if (token == null || token.trim().isEmpty()) {
            token = req.getHeader("X-CSRF-Token");
        }
        if (token == null || token.trim().isEmpty()) {
            token = req.getHeader("X-XSRF-TOKEN");
        }
        return token;
    }

    private boolean isValidCsrf(HttpSession session, String csrfParam) {
        String csrfSession = (String) session.getAttribute(AppConfig.SESSION_CSRF_TOKEN);
        if (csrfSession == null || csrfParam == null) {
            return false;
        }
        return MessageDigest.isEqual(csrfSession.getBytes(), csrfParam.getBytes());
    }
}
