package com.fruitmkt.service;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.dao.DeliveryDAO;
import com.fruitmkt.dao.OrderDAO;
import com.fruitmkt.model.dto.OrderDetailViewDTO;
import com.fruitmkt.model.dto.OrderListViewDTO;
import com.fruitmkt.model.entity.Delivery;
import com.fruitmkt.model.entity.Order;
import com.fruitmkt.model.entity.PaymentTransaction;
import com.fruitmkt.model.entity.User;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Query/view service cho customer order pages.
 */
public class OrderViewService {

    private static final int ORDER_PAGE_SIZE = 10;

    private final OrderDAO orderDAO = new OrderDAO();
    private final DeliveryDAO deliveryDAO = new DeliveryDAO();
    private final PaymentService paymentService = new PaymentService();

    public OrderDetailViewDTO getOrderDetailView(User user, int orderId) throws SQLException {
        Order order = findAccessibleOrder(user, orderId);
        if (order == null) {
            return null;
        }

        OrderDetailViewDTO view = new OrderDetailViewDTO();
        view.setOrder(order);
        view.setOrderItems(orderDAO.findItemsByOrderId(orderId));
        try {
            view.setPaymentTransaction(paymentService.getPaymentByOrder(orderId));
        } catch (Exception ignored) {
        }
        try {
            view.setDelivery(deliveryDAO.findByOrderId(orderId));
        } catch (Exception ignored) {
        }
        return view;
    }

    public OrderDetailViewDTO getInvoiceView(User user, int orderId) throws SQLException {
        OrderDetailViewDTO view = getOrderDetailView(user, orderId);
        if (view == null || view.getOrder() == null) {
            return null;
        }
        if (!AppConfig.ORDER_DELIVERED.equals(view.getOrder().getStatus())) {
            return null;
        }
        return view;
    }

    public OrderListViewDTO getOrderListView(User user, String status, int page) throws SQLException {
        int resolvedPage = Math.max(1, page);
        List<Order> orders = orderDAO.findByCustomer(user.getUserId(), status, resolvedPage, ORDER_PAGE_SIZE);
        int totalCount = orderDAO.countByCustomer(user.getUserId(), status);

        Map<Integer, Delivery> deliveryMap = new HashMap<>();
        Map<Integer, PaymentTransaction> paymentTransactionMap = new HashMap<>();
        for (Order order : orders) {
            Delivery delivery = deliveryDAO.findByOrderId(order.getOrderId());
            if (delivery != null) {
                deliveryMap.put(order.getOrderId(), delivery);
            }
            if (AppConfig.PAYMENT_CK.equals(order.getPaymentMethod())) {
                PaymentTransaction tx = paymentService.getPaymentByOrder(order.getOrderId());
                if (tx != null) {
                    paymentTransactionMap.put(order.getOrderId(), tx);
                }
            }
        }

        OrderListViewDTO view = new OrderListViewDTO();
        view.setOrders(orders);
        view.setDeliveryMap(deliveryMap);
        view.setPaymentTransactionMap(paymentTransactionMap);
        view.setCurrentPage(resolvedPage);
        view.setTotalPages((int) Math.ceil((double) totalCount / ORDER_PAGE_SIZE));
        view.setSelectedStatus(status);
        return view;
    }

    private Order findAccessibleOrder(User user, int orderId) throws SQLException {
        Order order = orderDAO.findByIdForCustomer(orderId, user.getUserId());
        if (order == null && AppConfig.ROLE_SHOP_OWNER.equals(user.getRole())) {
            order = orderDAO.findByIdForOwner(orderId, user.getUserId());
        }
        return order;
    }
}
