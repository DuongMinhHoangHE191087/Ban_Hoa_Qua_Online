package util;

import config.AppConfig;
import model.entity.auth.User;
import model.entity.order.Order;

/**
 * ActorAccessPolicy - Gom các kiểm tra actor/role dùng chung cho servlet và filter.
 */
public final class ActorAccessPolicy {

    private ActorAccessPolicy() {
    }

    public static boolean isAdmin(User user) {
        return hasRole(user, AppConfig.ROLE_ADMIN);
    }

    public static boolean isCustomer(User user) {
        return hasRole(user, AppConfig.ROLE_CUSTOMER);
    }

    public static boolean isShopOwner(User user) {
        return hasRole(user, AppConfig.ROLE_SHOP_OWNER);
    }

    public static boolean isDelivery(User user) {
        return hasRole(user, AppConfig.ROLE_DELIVERY);
    }

    public static boolean canAccessCustomerArea(User user) {
        return isCustomer(user);
    }

    public static boolean canAccessOrderArea(User user) {
        return isCustomer(user) || isShopOwner(user);
    }

    public static boolean canAccessReturnRequestArea(User user) {
        return isCustomer(user) || isShopOwner(user) || isAdmin(user);
    }

    public static boolean canViewReturnRequestList(User user) {
        return isAdmin(user) || isShopOwner(user);
    }

    public static boolean canDecideReturnRequest(User user) {
        return isAdmin(user);
    }

    public static boolean canCreateReturnRequest(User user) {
        return isCustomer(user);
    }

    public static boolean canOpenAdminSupportSession(User user) {
        return isCustomer(user);
    }

    public static boolean canOpenShopSupportSession(User user) {
        return isCustomer(user);
    }

    public static boolean isOrderOwnedByUser(Order order, User user) {
        return order != null && user != null && order.getCustomerId() == user.getUserId();
    }

    public static boolean isDelivered(Order order) {
        return order != null && "DELIVERED".equals(order.getStatus());
    }

    private static boolean hasRole(User user, String role) {
        return user != null && role != null && role.equals(user.getRole());
    }
}
