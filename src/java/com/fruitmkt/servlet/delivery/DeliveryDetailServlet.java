package com.fruitmkt.servlet.delivery;

import com.fruitmkt.dao.OrderDAO;
import com.fruitmkt.dao.ShopProfileDAO;
import com.fruitmkt.model.entity.Delivery;
import com.fruitmkt.model.entity.Order;
import com.fruitmkt.model.entity.ShopProfile;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.service.DeliveryService;
import com.fruitmkt.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

/**
 * DeliveryDetailServlet — Xem chi tiết đơn giao hàng.
 * URL: /delivery/detail
 */
@WebServlet("/delivery/detail")
public class DeliveryDetailServlet extends HttpServlet {

    private final DeliveryService deliveryService = new DeliveryService();
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
                Delivery d = new com.fruitmkt.dao.DeliveryDAO().findById(deliveryId);
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
            List<Order> orders = orderDAO.findById(delivery.getOrderId());
            if (!orders.isEmpty()) {
                order = orders.get(0);
            }

            // Load shop pickup address
            String pickupAddress = "Chưa xác định";
            String shopName = "Cửa hàng";
            if (order != null) {
                List<ShopProfile> shops = shopProfileDAO.findByUserId(order.getOwnerId());
                if (!shops.isEmpty()) {
                    ShopProfile shop = shops.get(0);
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
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi tải chi tiết đơn hàng.");
        }
    }
}
