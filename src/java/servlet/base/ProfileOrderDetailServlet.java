package servlet.base;

import config.AppConfig;
import dao.order.OrderDAO;
import model.dto.order.OrderDetailViewDTO;
import model.entity.order.Order;
import model.entity.auth.User;
import service.order.OrderViewService;
import util.SessionUtil;
import util.LoggerUtil;

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

                java.util.List<Integer> childOrderIds = new java.util.ArrayList<>();
                java.util.List<Integer> ownerIds = new java.util.ArrayList<>();
                for (Order child : childOrders) {
                    childOrderIds.add(child.getOrderId());
                    ownerIds.add(child.getOwnerId());
                }
                java.util.Map<Integer, java.util.List<model.entity.order.OrderItem>> childItemsMap = new java.util.HashMap<>();
                try {
                    childItemsMap = orderViewService.getOrderItemsMap(childOrderIds);
                } catch (Exception e) {
                    LoggerUtil.warn(log, "Không thể tải items của đơn con theo batch", e);
                    for (Order child : childOrders) {
                        childItemsMap.put(child.getOrderId(), new java.util.ArrayList<>());
                    }
                }

                java.util.Map<Integer, String> ownerNames = new java.util.HashMap<>();
                try {
                    ownerNames = orderViewService.getShopNamesMap(ownerIds);
                } catch (Exception e) {
                    LoggerUtil.warn(log, "Không thể tải tên shop cho đơn con theo batch", e);
                }
                java.util.Map<Integer, String> shopNamesMap = new java.util.HashMap<>();
                for (Order child : childOrders) {
                    shopNamesMap.put(child.getOrderId(), ownerNames.getOrDefault(child.getOwnerId(), "Cửa hàng"));
                }
                req.setAttribute("childOrderItemsMap", childItemsMap);
                req.setAttribute("shopNamesMap", shopNamesMap);
            } else {
                java.util.Map<Integer, String> ownerNames;
                try {
                    ownerNames = orderViewService.getShopNamesMap(java.util.List.of(order.getOwnerId()));
                } catch (Exception e) {
                    LoggerUtil.warn(log, "Không thể tải tên shop cho đơn #" + orderId, e);
                    ownerNames = new java.util.HashMap<>();
                }
                req.setAttribute("shopName", ownerNames.getOrDefault(order.getOwnerId(), "Cửa hàng"));
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

    private Integer parseOrderId(String str) {
        if (str == null || str.trim().isEmpty()) return null;
        try { return Integer.parseInt(str.trim()); } catch (NumberFormatException e) { return null; }
    }
}
