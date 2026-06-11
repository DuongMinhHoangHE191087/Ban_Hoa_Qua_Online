package com.fruitmkt.servlet.customer;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.dao.OrderDAO;
import com.fruitmkt.model.dto.CheckoutRequestDTO;
import com.fruitmkt.model.dto.CheckoutResultDTO;
import com.fruitmkt.model.dto.CheckoutViewData;
import com.fruitmkt.model.entity.Order;
import com.fruitmkt.model.entity.PaymentTransaction;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.model.response.ApiResponse;
import com.fruitmkt.service.CheckoutService;
import com.fruitmkt.service.PaymentService;
import com.fruitmkt.util.JsonUtil;
import com.fruitmkt.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.fruitmkt.util.LoggerUtil;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Controller cho checkout.
 */
@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(CheckoutServlet.class.getName());

    private static final String BANK_ID = AppConfig.SEPAY_BANK_ID;
    private static final String ACCOUNT_NO = AppConfig.SEPAY_ACCOUNT_NO;
    private static final String ACCOUNT_NAME = AppConfig.SEPAY_ACCOUNT_NAME;
    private static final String REF_PREFIX = AppConfig.PAYMENT_REF_PREFIX;
    private static final int QR_EXPIRE_MIN = AppConfig.QR_EXPIRE_MINUTES;

    private final CheckoutService checkoutService = new CheckoutService();
    private final PaymentService paymentService = new PaymentService();
    private final OrderDAO orderDAO = new OrderDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        HttpSession session = req.getSession();
        User user = SessionUtil.getCurrentUser(session);
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }
        if (!AppConfig.ROLE_CUSTOMER.equals(user.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Ban khong co quyen thuc hien thanh toan.");
            return;
        }

        String action = req.getParameter("action");
        if ("success".equals(action)) {
            handleSuccessView(req, resp, user);
            return;
        }
        if ("payment".equals(action)) {
            handlePaymentView(req, resp, user);
            return;
        }
        if ("status".equals(action)) {
            handleStatusView(req, resp, user);
            return;
        }

        try {
            CheckoutViewData viewData = checkoutService.buildCheckoutView(user, parseVariantIds(req.getParameter("variantIds")));
            req.setAttribute("userAddresses", viewData.getUserAddresses());
            req.setAttribute("cartSummary", viewData.getCartSummary());
            req.setAttribute("userAddress", user.getUserAddress());
            req.setAttribute("shopCount", viewData.getShopCount());
            req.setAttribute("directSaleAmount", viewData.getDirectSaleAmount());
            if (viewData.getShopOwnerId() != null) {
                req.setAttribute("shopOwnerId", viewData.getShopOwnerId());
            }
            req.getRequestDispatcher("/WEB-INF/jsp/customer/checkout.jsp").forward(req, resp);
        } catch (Exception e) {
            SessionUtil.flashError(session, "Loi khi tai trang thanh toan: " + e.getMessage() + ". Vui long thu lai.");
            resp.sendRedirect(req.getContextPath() + "/cart");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession();
        User user = SessionUtil.getCurrentUser(session);
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }
        if (!AppConfig.ROLE_CUSTOMER.equals(user.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Ban khong co quyen thuc hien thanh toan.");
            return;
        }
        if (!isValidCsrf(session, req.getParameter("_csrf"))) {
            SessionUtil.flashError(session, "Yeu cau khong hop le (CSRF). Vui long thu lai.");
            resp.sendRedirect(req.getContextPath() + "/cart");
            return;
        }

        String action = req.getParameter("action");
        if ("confirmPayment".equals(action)) {
            handleConfirmPayment(req, resp, session, user);
            return;
        }

        CheckoutRequestDTO checkoutRequest = buildCheckoutRequest(req);
        try {
            CheckoutResultDTO result = checkoutService.placeOrder(user, checkoutRequest, req.getRemoteAddr());
            SessionUtil.setCurrentUser(session, user);
            session.setAttribute("_purgedVariantIds", result.getPurgedVariantIds());
            SessionUtil.flashSuccess(session, result.getSuccessMessage());
            if (result.isPaymentRequired()) {
                resp.sendRedirect(req.getContextPath() + "/checkout?action=payment&orderId=" + result.getOrderId());
            } else {
                resp.sendRedirect(req.getContextPath() + "/checkout?action=success&orderId=" + result.getOrderId());
            }
        } catch (SecurityException e) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        } catch (Exception e) {
            SessionUtil.flashError(session, "Da xay ra loi trong qua trinh dat hang: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + buildCheckoutRedirect(checkoutRequest.getVariantIds()));
        }
    }

    private void handleSuccessView(HttpServletRequest req, HttpServletResponse resp, User user)
            throws IOException, ServletException {
        req.setAttribute("isSuccess", true);
        Order order = findCustomerOrder(req.getParameter("orderId"), user.getUserId());
        if (order != null) {
            req.setAttribute("order", order);
        }
        req.getRequestDispatcher("/WEB-INF/jsp/customer/order-success.jsp").forward(req, resp);
    }

    private void handlePaymentView(HttpServletRequest req, HttpServletResponse resp, User user)
            throws IOException, ServletException {
        Order order = findCustomerOrder(req.getParameter("orderId"), user.getUserId());
        if (order == null || !AppConfig.PAYMENT_CK.equals(order.getPaymentMethod())) {
            resp.sendRedirect(req.getContextPath() + "/home");
            return;
        }

        String reference = REF_PREFIX + order.getOrderId();
        String amountFormatted = order.getFinalAmount().setScale(0, RoundingMode.HALF_UP).toString();
        req.setAttribute("order", order);
        req.setAttribute("qrUrl", buildQrUrl(reference, amountFormatted));
        req.setAttribute("bankId", BANK_ID);
        req.setAttribute("accountNo", ACCOUNT_NO);
        req.setAttribute("accountName", ACCOUNT_NAME);
        req.setAttribute("reference", reference);
        req.setAttribute("amountFormatted", amountFormatted);
        req.setAttribute("qrExpireMin", QR_EXPIRE_MIN);
        try {
            PaymentTransaction paymentTx = paymentService.getPaymentByOrder(order.getOrderId());
            if (paymentTx != null) {
                req.setAttribute("paymentTx", paymentTx);
            }
        } catch (Exception e) {
            LoggerUtil.warn(log, "Không thể tải thông tin giao dịch thanh toán cho đơn hàng", e);
        }
        req.getRequestDispatcher("/WEB-INF/jsp/customer/order-payment.jsp").forward(req, resp);
    }

    private void handleStatusView(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        Order order = findCustomerOrder(req.getParameter("orderId"), user.getUserId());
        if (order != null) {
            JsonUtil.writeJson(resp, ApiResponse.ok(Map.of("status", order.getStatus())));
            return;
        }
        JsonUtil.writeJson(resp, ApiResponse.ok(Map.of("status", "UNKNOWN")));
    }

    private void handleConfirmPayment(HttpServletRequest req, HttpServletResponse resp, HttpSession session, User user)
            throws IOException {
        String orderId = req.getParameter("orderId");
        try {
            boolean ok = paymentService.confirmManualPayment(Integer.parseInt(orderId), user.getUserId());
            if (ok) {
                SessionUtil.flashSuccess(session,
                        "Chung toi da nhan thong bao thanh toan. Admin se xac minh va duyet trong 1-24 gio lam viec.");
            } else {
                SessionUtil.flashError(session, "Ma QR da het han. Vui long lam moi ma QR va thanh toan lai.");
            }
        } catch (SecurityException e) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        } catch (Exception e) {
            SessionUtil.flashError(session, "Loi: " + e.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/checkout?action=payment&orderId=" + orderId);
    }

    private CheckoutRequestDTO buildCheckoutRequest(HttpServletRequest req) {
        CheckoutRequestDTO request = new CheckoutRequestDTO();
        request.setFullName(req.getParameter("fullName"));
        request.setPhone(req.getParameter("phone"));
        request.setDeliveryAddress(req.getParameter("deliveryAddress"));
        request.setDeliveryTimeSlot(req.getParameter("deliveryTimeSlot"));
        request.setNotes(req.getParameter("notes"));

        String paymentMethod = req.getParameter("paymentMethod");
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            paymentMethod = AppConfig.PAYMENT_COD;
        }
        request.setPaymentMethod(paymentMethod.trim());
        request.setShopCouponCode(req.getParameter("shopCouponCode"));
        request.setSystemCouponCode(req.getParameter("systemCouponCode"));
        request.setVariantIds(parseVariantIds(req.getParameter("variantIds")));
        request.setSaveAddressToBook("true".equals(req.getParameter("saveAddressToBook")));
        return request;
    }

    private List<Integer> parseVariantIds(String variantIdsParam) {
        List<Integer> variantIds = new ArrayList<>();
        if (variantIdsParam == null || variantIdsParam.trim().isEmpty()) {
            return variantIds;
        }
        for (String part : variantIdsParam.split(",")) {
            try {
                variantIds.add(Integer.parseInt(part.trim()));
            } catch (NumberFormatException e) {
                LoggerUtil.warn(log, "ID biến thể không hợp lệ: " + part, e);
            }
        }
        return variantIds;
    }

    private boolean isValidCsrf(HttpSession session, String csrfParam) {
        String csrfSession = (String) session.getAttribute("_csrfToken");
        if (csrfSession == null || csrfParam == null) {
            return false;
        }
        return MessageDigest.isEqual(csrfSession.getBytes(), csrfParam.getBytes());
    }

    private String buildCheckoutRedirect(List<Integer> variantIds) {
        if (variantIds == null || variantIds.isEmpty()) {
            return "/cart";
        }
        StringBuilder builder = new StringBuilder("/checkout?variantIds=");
        for (int i = 0; i < variantIds.size(); i++) {
            builder.append(variantIds.get(i));
            if (i < variantIds.size() - 1) {
                builder.append(",");
            }
        }
        return builder.toString();
    }

    private Order findCustomerOrder(String orderIdParam, int customerId) {
        if (orderIdParam == null || orderIdParam.trim().isEmpty()) {
            return null;
        }
        try {
            return orderDAO.findByIdForCustomer(Integer.parseInt(orderIdParam), customerId);
        } catch (NumberFormatException | SQLException e) {
            return null;
        }
    }

    private String buildQrUrl(String reference, String amount) throws java.io.UnsupportedEncodingException {
        return "https://img.vietqr.io/image/" + BANK_ID + "-" + ACCOUNT_NO + "-compact2.png"
                + "?amount=" + amount
                + "&addInfo=" + java.net.URLEncoder.encode(reference, "UTF-8")
                + "&accountName=" + java.net.URLEncoder.encode(ACCOUNT_NAME, "UTF-8");
    }
}
