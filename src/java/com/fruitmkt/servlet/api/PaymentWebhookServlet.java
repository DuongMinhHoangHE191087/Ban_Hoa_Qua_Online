package com.fruitmkt.servlet.api;

import com.fruitmkt.service.PaymentService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;

/**
 * PaymentWebhookServlet — Nhận webhook từ SePay để tự động xác nhận thanh toán.
 *
 * URL: /api/payment/webhook
 * POST: Nhận JSON payload từ SePay, xử lý idempotent
 *
 * Setup SePay:
 *   1. Đăng nhập https://my.sepay.vn → Cài đặt → Webhook
 *   2. Điền URL: https://{your-domain}/Ban_Hoa_Qua_Online/api/payment/webhook
 *   3. Method: POST
 *
 * Luồng xử lý:
 *   1. Đọc raw JSON body
 *   2. Gọi PaymentService.processWebhook() — dedup + match + update
 *   3. Luôn trả HTTP 200 + {"success": true}
 *      (SePay retry nếu nhận non-200 — phải tránh bằng mọi giá)
 *
 * @author fruitmkt-team
 */
@WebServlet("/api/payment/webhook")
public class PaymentWebhookServlet extends HttpServlet {

    private final PaymentService paymentService = new PaymentService();

    /**
     * GET không được phép — chỉ POST.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write("{\"error\":\"Method Not Allowed\"}");
    }

    /**
     * POST — Nhận và xử lý webhook từ SePay.
     * Luôn trả 200 để SePay không retry (lỗi được log, không throw ra ngoài).
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        // Đọc raw JSON body
        String jsonPayload = readBody(req);
        System.out.println("[SePay Webhook] Received webhook payload from SePay");

        try {
            paymentService.processWebhook(jsonPayload);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"success\":true}");
        } catch (Exception e) {
            // Log lỗi nhưng PHẢI trả 200 để SePay không retry
            System.err.println("[SePay Webhook] Lỗi xử lý: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_OK); // intentional — SePay rule
            resp.getWriter().write("{\"success\":false,\"error\":\"Internal processing error\"}");
        }
    }

    /** Đọc toàn bộ request body dưới dạng String. */
    private String readBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }
        return sb.toString().trim();
    }
}
