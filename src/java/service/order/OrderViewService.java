package service.order;
import service.shop.PaymentService;

import config.AppConfig;
import dao.auth.UserDAO;
import dao.order.DeliveryDAO;
import dao.order.OrderDAO;
import dao.shop.ShopProfileDAO;
import model.dto.order.OrderDetailViewDTO;
import model.dto.order.OrderListViewDTO;
import model.entity.auth.User;
import model.entity.order.Delivery;
import model.entity.order.Order;
import model.entity.order.OrderItem;
import model.entity.shop.PaymentTransaction;
import model.entity.shop.ShopProfile;
import util.LoggerUtil;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.logging.Logger;

/**
 * Query/view service cho customer order pages.
 */
public class OrderViewService {

    private static final Logger log = LoggerUtil.getLogger(OrderViewService.class);
    private static final int ORDER_PAGE_SIZE = 10;

    private final OrderDAO orderDAO = new OrderDAO();
    private final DeliveryDAO deliveryDAO = new DeliveryDAO();
    private final PaymentService paymentService = new PaymentService();
    private final ShopProfileDAO shopProfileDAO = new ShopProfileDAO();
    private final UserDAO userDAO = new UserDAO();

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
        } catch (Exception e) {
            LoggerUtil.warn(log, "Could not load payment transaction for orderId=" + orderId, e);
        }
        try {
            view.setDelivery(deliveryDAO.findByOrderId(orderId));
        } catch (Exception e) {
            LoggerUtil.warn(log, "Could not load delivery for orderId=" + orderId, e);
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

    public Map<Integer, List<OrderItem>> getOrderItemsMap(Collection<Integer> orderIds) throws SQLException {
        return orderDAO.findItemsByOrderIds(orderIds);
    }

    public Map<Integer, Delivery> getDeliveryMap(Collection<Integer> orderIds) throws SQLException {
        return deliveryDAO.findByOrderIds(orderIds);
    }

    public Map<Integer, PaymentTransaction> getPaymentTransactionMap(Collection<Integer> orderIds) throws SQLException {
        return paymentService.getPaymentMapByOrderIds(orderIds);
    }

    public Map<Integer, String> getShopNamesMap(Collection<Integer> ownerIds) throws SQLException {
        Map<Integer, String> names = new LinkedHashMap<>();
        if (ownerIds == null || ownerIds.isEmpty()) {
            return names;
        }

        List<Integer> distinctOwnerIds = new ArrayList<>(ownerIds.stream().distinct().collect(Collectors.toList()));
        Map<Integer, ShopProfile> shopProfiles = shopProfileDAO.findByUserIds(distinctOwnerIds);
        Map<Integer, User> users = userDAO.findByIds(distinctOwnerIds);
        for (Integer ownerId : distinctOwnerIds) {
            String shopName = null;
            ShopProfile profile = shopProfiles.get(ownerId);
            if (profile != null && profile.getShopName() != null && !profile.getShopName().trim().isEmpty()) {
                shopName = profile.getShopName().trim();
            } else {
                User owner = users.get(ownerId);
                if (owner != null && owner.getFullName() != null && !owner.getFullName().trim().isEmpty()) {
                    shopName = owner.getFullName().trim();
                }
            }
            names.put(ownerId, shopName != null ? shopName : "Cửa hàng");
        }
        return names;
    }

    public OrderListViewDTO getOrderListView(User user, String status, int page) throws SQLException {
        int resolvedPage = Math.max(1, page);
        List<Order> orders;
        int totalCount;
        if (AppConfig.ROLE_SHOP_OWNER.equals(user.getRole())) {
            orders = orderDAO.findByOwner(user.getUserId(), status, resolvedPage, ORDER_PAGE_SIZE);
            totalCount = orderDAO.countByOwner(user.getUserId(), status);
        } else {
            orders = orderDAO.findByCustomer(user.getUserId(), status, resolvedPage, ORDER_PAGE_SIZE);
            totalCount = orderDAO.countByCustomer(user.getUserId(), status);
        }

        List<Integer> orderIds = new ArrayList<>();
        for (Order order : orders) {
            orderIds.add(order.getOrderId());
        }

        Map<Integer, Delivery> deliveryMap = new LinkedHashMap<>();
        try {
            deliveryMap = getDeliveryMap(orderIds);
        } catch (SQLException e) {
            LoggerUtil.warn(log, "Could not batch load deliveries for order list", e);
        }

        Map<Integer, PaymentTransaction> allPaymentTransactions = new LinkedHashMap<>();
        try {
            allPaymentTransactions = getPaymentTransactionMap(orderIds);
        } catch (SQLException e) {
            LoggerUtil.warn(log, "Could not batch load payment transactions for order list", e);
        }
        Map<Integer, PaymentTransaction> paymentTransactionMap = new LinkedHashMap<>();
        for (Order order : orders) {
            if (AppConfig.PAYMENT_CK.equals(order.getPaymentMethod())) {
                PaymentTransaction tx = allPaymentTransactions.get(order.getOrderId());
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
