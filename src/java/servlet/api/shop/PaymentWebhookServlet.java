package servlet.api.shop;

import model.response.ApiResponse;
import service.shop.PaymentService;
import service.shop.PaymentService.WebhookProcessingResult;
import util.JsonUtil;
import util.LoggerUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Logger;

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
 *   3. Trả HTTP 200 cho trạng thái đã xử lý/bỏ qua hợp lệ; chỉ trả 500 khi lỗi xử lý nội bộ.
 *
 * @author fruitmkt-team
 */
@WebServlet("/api/payment/webhook")
public class PaymentWebhookServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(PaymentWebhookServlet.class.getName());

    private final PaymentService paymentService = new PaymentService();

    /**
     * GET không được phép — chỉ POST.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        JsonUtil.writeJson(resp, ApiResponse.fail(405, "Method Not Allowed"));
    }

    /**
     * POST — Nhận và xử lý webhook từ SePay.
     * Luôn trả 200 để SePay không retry (lỗi được log, không throw ra ngoài).
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        // Đọc raw JSON body
        String jsonPayload = readBody(req);
        LoggerUtil.info(log, "[SePay Webhook] Received webhook payload from SePay");

        try {
            WebhookProcessingResult result = paymentService.processWebhook(jsonPayload);
            LoggerUtil.info(log, "[SePay Webhook] outcome=%s orderId=%d sepayTxId=%s",
                    result.getOutcome(), result.getOrderId(), result.getSepayTxId());
            resp.setStatus(HttpServletResponse.SC_OK);
            JsonUtil.writeJson(resp, ApiResponse.ok(result.toResponseMap()));
        } catch (Exception e) {
            LoggerUtil.error(log, "[SePay Webhook] Lỗi xử lý webhook", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJson(resp, ApiResponse.fail("Internal processing error"));
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
