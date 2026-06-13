package com.fruitmkt.servlet.base;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.dao.OrderDAO;
import com.fruitmkt.dao.ShopProfileDAO;
import com.fruitmkt.model.dto.OrderDetailViewDTO;
import com.fruitmkt.model.entity.Order;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.service.OrderViewService;
import com.fruitmkt.util.SessionUtil;
import com.fruitmkt.util.LoggerUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Trang chi tiet don hang dung rieng cho profile customer.
 * URL: /profile/order-detail?orderId=X
 *      /profile/order-detail?action=invoice&orderId=X
 *
 * Dam bao ownership: chi customer so huu don hang moi xem duoc.
 */
@WebServlet("/profile/order-detail")
public class ProfileOrderDetailServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(ProfileOrderDetailServlet.class.getName());

    private final OrderViewService orderViewService = new OrderViewService();
    private final OrderDAO orderDAO = new OrderDAO();
    private final ShopProfileDAO shopProfileDAO = new ShopProfileDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = SessionUtil.getCurrentUser(req.getSession());
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }
        // Chi CUSTOMER moi dung duoc trang nay
        if (!AppConfig.ROLE_CUSTOMER.equals(user.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        String action = req.getParameter("action");

        // Invoice view
        if ("invoice".equals(action)) {
            handleInvoice(req, resp, user);
            return;
        }

        // Default: order detail view
        handleDetail(req, resp, user);
    }

    private void handleDetail(HttpServletRequest req, HttpServletResponse resp, User user)
            throws ServletException, IOException {
        Integer orderId = parseOrderId(req.getParameter("orderId"));
        if (orderId == null) {
            resp.sendRedirect(req.getContextPath() + "/profile?tab=orders");
            return;
        }
        try {
            OrderDetailViewDTO view = orderViewService.getOrderDetailView(user, orderId);
            if (view == null || view.getOrder() == null) {
                SessionUtil.flashError(req.getSession(), "Không tìm thấy đơn hàng hoặc bạn không có quyền xem đơn này.");
                resp.sendRedirect(req.getContextPath() + "/profile?tab=orders");
                return;
            }

            Order order = view.getOrder();
            req.setAttribute("order", order);
            req.setAttribute("orderItems", view.getOrderItems());
            req.setAttribute("paymentTx", view.getPaymentTransaction());
            req.setAttribute("delivery", view.getDelivery());

            // Parent order: load children
            if ("PARENT".equals(order.getOrderType())) {
                List<Order> childOrders = orderDAO.findChildrenByParentId(orderId);
                req.setAttribute("childOrders", childOrders);

                java.util.Map<Integer, List<com.fruitmkt.model.entity.OrderItem>> childItemsMap = new java.util.HashMap<>();
                java.util.Map<Integer, String> shopNamesMap = new java.util.HashMap<>();
                for (Order child : childOrders) {
                    childItemsMap.put(child.getOrderId(), orderDAO.findItemsByOrderId(child.getOrderId()));
                    shopNamesMap.put(child.getOrderId(), resolveShopName(child.getOwnerId()));
                }
                req.setAttribute("childOrderItemsMap", childItemsMap);
                req.setAttribute("shopNamesMap", shopNamesMap);
            } else {
                req.setAttribute("shopName", resolveShopName(order.getOwnerId()));
            }

            req.getRequestDispatcher("/WEB-INF/jsp/customer/profile-order-detail.jsp").forward(req, resp);

        } catch (Exception e) {
            LoggerUtil.error(log, "Lỗi khi tải chi tiết đơn hàng profile", e);
            SessionUtil.flashError(req.getSession(), "Có lỗi xảy ra khi tải đơn hàng.");
            resp.sendRedirect(req.getContextPath() + "/profile?tab=orders");
        }
    }

    private void handleInvoice(HttpServletRequest req, HttpServletResponse resp, User user)
            throws ServletException, IOException {
        Integer orderId = parseOrderId(req.getParameter("orderId"));
        if (orderId == null) {
            resp.sendRedirect(req.getContextPath() + "/profile?tab=orders");
            return;
        }
        try {
            OrderDetailViewDTO view = orderViewService.getInvoiceView(user, orderId);
            if (view != null && view.getOrder() != null) {
                req.setAttribute("order", view.getOrder());
                req.setAttribute("orderItems", view.getOrderItems());
                req.getRequestDispatcher("/WEB-INF/jsp/customer/invoice.jsp").forward(req, resp);
                return;
            }
            SessionUtil.flashError(req.getSession(), "Hóa đơn chỉ khả dụng khi đơn hàng đã giao thành công.");
        } catch (Exception e) {
            LoggerUtil.error(log, "Lỗi khi tải hóa đơn profile", e);
            SessionUtil.flashError(req.getSession(), "Có lỗi xảy ra khi tải hóa đơn.");
        }
        resp.sendRedirect(req.getContextPath() + "/profile?tab=orders");
    }

    private String resolveShopName(int ownerId) {
        try {
            java.util.List<com.fruitmkt.model.entity.ShopProfile> profiles = shopProfileDAO.findByUserId(ownerId);
            if (!profiles.isEmpty() && profiles.get(0).getShopName() != null) {
                return profiles.get(0).getShopName();
            }
        } catch (Exception e) {
            // fallback
        }
        return "Cửa hàng";
    }

    private Integer parseOrderId(String str) {
        if (str == null || str.trim().isEmpty()) return null;
        try { return Integer.parseInt(str.trim()); } catch (NumberFormatException e) { return null; }
    }
}
