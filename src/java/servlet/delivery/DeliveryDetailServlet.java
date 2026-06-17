package servlet.delivery;
import dao.order.DeliveryDAO;

import dao.order.OrderDAO;
import dao.shop.ShopProfileDAO;
import model.entity.order.Delivery;
import model.entity.order.Order;
import model.entity.shop.ShopProfile;
import model.entity.auth.User;
import service.order.DeliveryService;
import util.LoggerUtil;
import util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * DeliveryDetailServlet — Xem chi tiết đơn giao hàng.
 * URL: /delivery/detail
 */
@WebServlet("/delivery/detail")
public class DeliveryDetailServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(DeliveryDetailServlet.class.getName());

    private final DeliveryService deliveryService = new DeliveryService();
    private final DeliveryDAO deliveryDAO = new DeliveryDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final ShopProfileDAO shopProfileDAO = new ShopProfileDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = SessionUtil.getCurrentUser(req.getSession(false));
        if (currentUser == null || !"DELIVERY".equals(currentUser.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String idStr = req.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/delivery/dashboard");
            return;
        }

        try {
            int deliveryId = Integer.parseInt(idStr);
            // Load delivery
            List<Delivery> list = deliveryService.getDeliveriesForStaff(currentUser.getUserId());
            Delivery delivery = null;
            for (Delivery d : list) {
                if (d.getDeliveryId() == deliveryId) {
                    delivery = d;
                    break;
                }
            }

            // Fallback: search in unassigned if not found in staff's list
            if (delivery == null) {
                Delivery d = deliveryDAO.findById(deliveryId);
                if (d != null && (d.getStaffId() == null || d.getStaffId().equals(currentUser.getUserId()))) {
                    delivery = d;
                }
            }

            if (delivery == null) {
                req.setAttribute("errorMsg", "Không tìm thấy thông tin giao hàng hoặc bạn không có quyền xem đơn này.");
                req.getRequestDispatcher("/WEB-INF/jsp/delivery/delivery-detail.jsp").forward(req, resp);
                return;
            }

            // Load order
            Order order = null;
            order = orderDAO.findOneById(delivery.getOrderId());

            // Load shop pickup address
            String pickupAddress = "Chưa xác định";
            String shopName = "Cửa hàng";
            if (order != null) {
                ShopProfile shop = shopProfileDAO.findOneByUserId(order.getOwnerId());
                if (shop != null) {
                    shopName = shop.getShopName();
                    if (shop.getDeliveryAddress() != null && !shop.getDeliveryAddress().trim().isEmpty()) {
                        pickupAddress = shop.getDeliveryAddress();
                    }
                }
            }

            req.setAttribute("delivery", delivery);
            req.setAttribute("order", order);
            req.setAttribute("shopName", shopName);
            req.setAttribute("pickupAddress", pickupAddress);
            req.getRequestDispatcher("/WEB-INF/jsp/delivery/delivery-detail.jsp").forward(req, resp);
        } catch (Exception e) {
            LoggerUtil.error(log, "Lỗi tải chi tiết đơn hàng", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi tải chi tiết đơn hàng.");
        }
    }
}
