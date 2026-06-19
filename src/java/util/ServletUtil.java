package util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ServletUtil — Helper đồng nhất response pattern trong toàn bộ Servlet layer.
 *
 * Giải quyết vấn đề: mỗi Servlet tự build JSON/redirect theo cách riêng → không nhất quán.
 *
 * PATTERNS:
 *   - AJAX JSON response  → sendJsonSuccess() / sendJsonError()
 *   - PRG redirect        → flashAndRedirect()
 *   - Parse int an toàn   → parseInt() / requireInt()
 *
 * CÁCH DÙNG (JSON AJAX):
 * <pre>
 *   try {
 *       Object result = myService.doWork(id);
 *       ServletUtil.sendJsonSuccess(resp, result);
 *   } catch (BusinessException e) {
 *       ServletUtil.sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
 *   } catch (Exception e) {
 *       ServletUtil.sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi hệ thống.");
 *   }
 * </pre>
 *
 * CÁCH DÙNG (PRG redirect):
 * <pre>
 *   try {
 *       orderService.cancel(orderId, userId);
 *       ServletUtil.flashAndRedirect(req, resp, "success", "Đã hủy đơn hàng.", "/orders");
 *   } catch (BusinessException e) {
 *       ServletUtil.flashAndRedirect(req, resp, "error", e.getMessage(), "/orders");
 *   }
 * </pre>
 *
 * @author fruitmkt-team
 */
public final class ServletUtil {

    // -------------------------------------------------------------------------
    // JSON Responses (dùng cho AJAX / REST endpoints)
    // -------------------------------------------------------------------------

    /**
     * Ghi JSON thành công: {"success":true,"data":{...}}
     * HTTP 200 OK.
     */
    public static void sendJsonSuccess(HttpServletResponse resp, Object data) throws IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json;charset=UTF-8");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("data", data);
        JsonUtil.writeJson(resp, body);
    }

    /**
     * Ghi JSON thành công không có data: {"success":true,"message":"..."}
     */
    public static void sendJsonSuccess(HttpServletResponse resp, String message) throws IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json;charset=UTF-8");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("message", message);
        JsonUtil.writeJson(resp, body);
    }

    /**
     * Ghi JSON lỗi: {"success":false,"message":"..."} với HTTP status code chỉ định.
     *
     * @param status HTTP status (400, 403, 404, 409, 429, 500, ...)
     * @param message Thông điệp thân thiện người dùng — không chứa stack trace
     */
    public static void sendJsonError(HttpServletResponse resp, int status, String message)
            throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json;charset=UTF-8");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("message", message);
        JsonUtil.writeJson(resp, body);
    }

    // -------------------------------------------------------------------------
    // PRG Redirect with Flash (dùng cho POST handlers trả về HTML)
    // -------------------------------------------------------------------------

    /**
     * Đặt flash message + redirect theo PRG pattern.
     * URL là đường dẫn tuyệt đối từ context root (bắt đầu bằng /).
     *
     * @param type    "success" | "error" | "warning" | "info"
     * @param message Nội dung flash message
     * @param url     URL redirect (ví dụ: "/orders" → context + "/orders")
     */
    public static void flashAndRedirect(HttpServletRequest req, HttpServletResponse resp,
            String type, String message, String url) throws IOException {
        HttpSession session = req.getSession();
        SessionUtil.setFlashMessage(session, message, type);
        resp.sendRedirect(req.getContextPath() + url);
    }

    /** Shortcut cho flash thành công + redirect */
    public static void successRedirect(HttpServletRequest req, HttpServletResponse resp,
            String message, String url) throws IOException {
        flashAndRedirect(req, resp, "success", message, url);
    }

    /** Shortcut cho flash lỗi + redirect */
    public static void errorRedirect(HttpServletRequest req, HttpServletResponse resp,
            String message, String url) throws IOException {
        flashAndRedirect(req, resp, "error", message, url);
    }

    // -------------------------------------------------------------------------
    // Parameter Parsing (giảm boilerplate trong Servlet)
    // -------------------------------------------------------------------------

    /**
     * Parse int an toàn từ request parameter.
     * Trả về -1 nếu param null, trống, hoặc không phải số.
     */
    public static int parseInt(HttpServletRequest req, String paramName) {
        String val = req.getParameter(paramName);
        if (val == null || val.isBlank()) return -1;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Parse int bắt buộc. Ném IllegalArgumentException nếu không hợp lệ hoặc <= 0.
     */
    public static int requirePositiveInt(HttpServletRequest req, String paramName) {
        int val = parseInt(req, paramName);
        if (val <= 0) {
            throw new IllegalArgumentException("Tham số '" + paramName + "' phải là số nguyên dương.");
        }
        return val;
    }

    /**
     * Lấy String param đã trim. Trả về "" nếu null.
     */
    public static String getParam(HttpServletRequest req, String paramName) {
        String val = req.getParameter(paramName);
        return val == null ? "" : val.trim();
    }

    /**
     * Lấy String param đã trim. Ném IllegalArgumentException nếu null hoặc trống.
     */
    public static String requireParam(HttpServletRequest req, String paramName) {
        String val = getParam(req, paramName);
        if (val.isEmpty()) {
            throw new IllegalArgumentException("Tham số '" + paramName + "' là bắt buộc.");
        }
        return val;
    }

    private ServletUtil() {}
}
