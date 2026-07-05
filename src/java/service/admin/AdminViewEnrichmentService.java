package service.admin;

import dao.auth.UserDAO;
import dao.catalog.CategoryDAO;
import dao.shop.ShopProfileDAO;
import model.entity.auth.User;
import model.entity.catalog.Category;
import model.entity.catalog.Product;
import model.entity.chat.Notification;
import model.entity.order.Order;
import model.entity.order.ReturnRequest;
import model.entity.shop.ShopProfile;
import model.entity.shop.ShopSettlement;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * AdminViewEnrichmentService - Gắn tên hiển thị vào các entity admin list.
 *
 * Lớp này chỉ làm việc với dữ liệu đã lấy từ service/DAO:
 * - batch load user/shop/category
 * - set các field hiển thị phụ để JSP không phải suy luận từ ID
 */
public class AdminViewEnrichmentService {

    private final UserDAO userDAO = new UserDAO();
    private final ShopProfileDAO shopProfileDAO = new ShopProfileDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    public void enrichShopProfile(ShopProfile profile) throws SQLException {
        if (profile == null) {
            return;
        }
        Map<Integer, User> users = loadUsers(Collections.singleton(profile.getUserId()));
        profile.setOwnerName(resolveUserName(users.get(profile.getUserId()), profile.getUserId()));
    }

    public void enrichShopProfiles(List<ShopProfile> profiles) throws SQLException {
        if (profiles == null || profiles.isEmpty()) {
            return;
        }

        Map<Integer, User> users = loadUsers(extractShopUserIds(profiles));
        for (ShopProfile profile : profiles) {
            if (profile == null) {
                continue;
            }
            profile.setOwnerName(resolveUserName(users.get(profile.getUserId()), profile.getUserId()));
        }
    }

    public void enrichOrders(List<Order> orders) throws SQLException {
        if (orders == null || orders.isEmpty()) {
            return;
        }

        LinkedHashSet<Integer> userIds = new LinkedHashSet<>();
        LinkedHashSet<Integer> ownerIds = new LinkedHashSet<>();
        for (Order order : orders) {
            if (order == null) {
                continue;
            }
            userIds.add(order.getCustomerId());
            Integer ownerId = order.getOwnerIdObject();
            if (ownerId != null) {
                ownerIds.add(ownerId);
            }
        }

        Map<Integer, User> users = loadUsers(userIds);
        Map<Integer, ShopProfile> shops = loadShopProfiles(ownerIds);

        for (Order order : orders) {
            if (order == null) {
                continue;
            }

            order.setCustomerName(resolveUserName(users.get(order.getCustomerId()), order.getCustomerId()));

            Integer ownerId = order.getOwnerIdObject();
            if (ownerId == null) {
                continue;
            }

            User ownerUser = users.get(ownerId);
            String ownerName = resolveUserName(ownerUser, ownerId);
            order.setOwnerName(ownerName);

            ShopProfile shop = shops.get(ownerId);
            String shopName = resolveShopName(shop, ownerName);
            order.setShopName(shopName);
        }
    }

    public void enrichSettlements(List<ShopSettlement> settlements) throws SQLException {
        if (settlements == null || settlements.isEmpty()) {
            return;
        }

        LinkedHashSet<Integer> ownerIds = new LinkedHashSet<>();
        for (ShopSettlement settlement : settlements) {
            if (settlement != null) {
                ownerIds.add(settlement.getOwnerId());
            }
        }

        Map<Integer, User> users = loadUsers(ownerIds);
        Map<Integer, ShopProfile> shops = loadShopProfiles(ownerIds);

        for (ShopSettlement settlement : settlements) {
            if (settlement == null) {
                continue;
            }
            User owner = users.get(settlement.getOwnerId());
            String ownerName = resolveUserName(owner, settlement.getOwnerId());
            settlement.setOwnerName(ownerName);
            settlement.setShopName(resolveShopName(shops.get(settlement.getOwnerId()), ownerName));
        }
    }

    public void enrichReturnRequests(List<ReturnRequest> requests) throws SQLException {
        if (requests == null || requests.isEmpty()) {
            return;
        }

        LinkedHashSet<Integer> customerIds = new LinkedHashSet<>();
        for (ReturnRequest request : requests) {
            if (request != null) {
                customerIds.add(request.getCustomerId());
            }
        }

        Map<Integer, User> users = loadUsers(customerIds);
        for (ReturnRequest request : requests) {
            if (request == null) {
                continue;
            }
            request.setCustomerName(resolveUserName(users.get(request.getCustomerId()), request.getCustomerId()));
        }
    }

    public void enrichNotifications(List<Notification> notifications) throws SQLException {
        if (notifications == null || notifications.isEmpty()) {
            return;
        }

        LinkedHashSet<Integer> userIds = new LinkedHashSet<>();
        for (Notification notification : notifications) {
            if (notification != null) {
                userIds.add(notification.getUserId());
            }
        }

        Map<Integer, User> users = loadUsers(userIds);
        for (Notification notification : notifications) {
            if (notification == null) {
                continue;
            }
            notification.setRecipientName(resolveUserName(users.get(notification.getUserId()), notification.getUserId()));
        }
    }

    public void enrichProducts(List<Product> products) throws SQLException {
        if (products == null || products.isEmpty()) {
            return;
        }

        LinkedHashSet<Integer> ownerIds = new LinkedHashSet<>();
        LinkedHashSet<Integer> categoryIds = new LinkedHashSet<>();
        for (Product product : products) {
            if (product == null) {
                continue;
            }
            ownerIds.add(product.getOwnerId());
            categoryIds.add(product.getCategoryId());
        }

        Map<Integer, User> users = loadUsers(ownerIds);
        Map<Integer, ShopProfile> shops = loadShopProfiles(ownerIds);
        Map<Integer, String> categoryNames = loadCategoryNames(categoryIds);

        for (Product product : products) {
            if (product == null) {
                continue;
            }

            User owner = users.get(product.getOwnerId());
            String ownerName = resolveUserName(owner, product.getOwnerId());
            product.setOwnerName(ownerName);

            ShopProfile shop = shops.get(product.getOwnerId());
            product.setShopName(resolveShopName(shop, ownerName));
            product.setCategoryName(resolveCategoryName(categoryNames, product.getCategoryId()));
        }
    }

    private Map<Integer, User> loadUsers(Collection<Integer> ids) throws SQLException {
        return userDAO.findByIds(normalizeIds(ids));
    }

    private Map<Integer, ShopProfile> loadShopProfiles(Collection<Integer> userIds) throws SQLException {
        return shopProfileDAO.findByUserIds(normalizeIds(userIds));
    }

    private Map<Integer, String> loadCategoryNames(Collection<Integer> categoryIds) throws SQLException {
        Map<Integer, String> categoryNames = new LinkedHashMap<>();
        if (categoryIds == null || categoryIds.isEmpty()) {
            return categoryNames;
        }

        LinkedHashSet<Integer> wantedIds = normalizeIds(categoryIds);
        for (Category category : categoryDAO.findAll()) {
            if (category != null && wantedIds.contains(category.getCategoryId())) {
                categoryNames.put(category.getCategoryId(), category.getName());
            }
        }
        return categoryNames;
    }

    private LinkedHashSet<Integer> extractShopUserIds(List<ShopProfile> profiles) {
        LinkedHashSet<Integer> ids = new LinkedHashSet<>();
        for (ShopProfile profile : profiles) {
            if (profile != null) {
                ids.add(profile.getUserId());
            }
        }
        return ids;
    }

    private LinkedHashSet<Integer> normalizeIds(Collection<Integer> ids) {
        LinkedHashSet<Integer> normalized = new LinkedHashSet<>();
        if (ids == null) {
            return normalized;
        }
        for (Integer id : ids) {
            if (id != null && id > 0) {
                normalized.add(id);
            }
        }
        return normalized;
    }

    private String resolveUserName(User user, int fallbackId) {
        String primary = null;
        if (user != null) {
            primary = user.getFullName();
            if (primary == null || primary.trim().isEmpty()) {
                primary = user.getEmail();
            }
        }
        return resolveDisplayText(primary, "Người dùng #" + fallbackId);
    }

    private String resolveShopName(ShopProfile shop, String fallbackName) {
        return resolveDisplayText(shop != null ? shop.getShopName() : null, fallbackName);
    }

    private String resolveCategoryName(Map<Integer, String> categoryNames, int categoryId) {
        String categoryName = categoryNames.get(categoryId);
        return resolveDisplayText(categoryName, "Chưa phân loại");
    }

    private String resolveDisplayText(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }
}
