package service.shop;

import config.AppConfig;
import dao.shop.ShopProfileDAO;
import model.entity.catalog.Product;
import model.entity.shop.ShopProfile;
import service.catalog.ProductService;
import service.order.OrderService;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class ShopService {

    private final ShopProfileDAO shopProfileDAO;
    private final ProductService productService;
    private final OrderService orderService;

    public ShopService() {
        this.shopProfileDAO = new ShopProfileDAO();
        this.productService = new ProductService();
        this.orderService = new OrderService();
    }

    public List<ShopProfile> getAllShops() throws SQLException {
        return shopProfileDAO.findAll(null); // null means get all regardless of status
    }

    public List<ShopProfile> getShopsByStatus(String status) throws SQLException {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status không được để trống.");
        }
        return shopProfileDAO.findAll(status);
    }

    public void updateShopStatus(int profileId, String status, String rejectionReason) throws SQLException {
        if (profileId <= 0) {
            throw new IllegalArgumentException("Profile ID không hợp lệ.");
        }
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Trạng thái không được để trống.");
        }
        String normalizedStatus = status.trim().toUpperCase(Locale.ROOT);
        if (AppConfig.SHOP_REJECTED.equals(normalizedStatus)
                && (rejectionReason == null || rejectionReason.trim().isEmpty())) {
            throw new IllegalArgumentException("Lý do từ chối không được để trống.");
        }
        ShopProfile shop = shopProfileDAO.findById(profileId);
        if (shop != null) {
            shopProfileDAO.updateApprovalStatus(profileId, shop.getUserId(), normalizedStatus, rejectionReason);
            if (AppConfig.SHOP_SUSPENDED.equals(normalizedStatus) || AppConfig.SHOP_REJECTED.equals(normalizedStatus)) {
                SQLException cascadeFailure = null;
                try {
                    hideProductsForOwner(shop.getUserId());
                } catch (SQLException ex) {
                    cascadeFailure = ex;
                }
                try {
                    orderService.cancelOpenOrdersByOwner(shop.getUserId(),
                            buildSystemCancellationReason(normalizedStatus, rejectionReason));
                } catch (SQLException ex) {
                    if (cascadeFailure == null) {
                        cascadeFailure = ex;
                    } else {
                        cascadeFailure.addSuppressed(ex);
                    }
                }
                if (cascadeFailure != null) {
                    throw cascadeFailure;
                }
            }
        }
    }

    public ShopProfile getShopByUserId(int userId) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID không hợp lệ.");
        }
        List<ShopProfile> profiles = shopProfileDAO.findByUserId(userId);
        if (profiles != null && !profiles.isEmpty()) {
            return profiles.get(0);
        }
        return null;
    }

    public void updateShopProfile(ShopProfile profile) throws SQLException {
        if (profile == null) {
            throw new IllegalArgumentException("Profile không được null.");
        }
        shopProfileDAO.update(profile);
    }

    public ShopProfile getShopById(int profileId) throws SQLException {
        if (profileId <= 0) {
            throw new IllegalArgumentException("Profile ID không hợp lệ.");
        }
        return shopProfileDAO.findById(profileId);
    }

    private void hideProductsForOwner(int ownerId) throws SQLException {
        List<Product> products = productService.getProductsByOwner(ownerId);
        for (Product product : products) {
            if (product.getProductId() > 0) {
                productService.toggleStatus(product.getProductId(), "INACTIVE");
            }
        }
    }

    private String buildSystemCancellationReason(String status, String rejectionReason) {
        if (AppConfig.SHOP_REJECTED.equals(status) && rejectionReason != null && !rejectionReason.trim().isEmpty()) {
            return "Shop bị từ chối: " + rejectionReason.trim();
        }
        if (AppConfig.SHOP_SUSPENDED.equals(status)) {
            return "Shop bị đình chỉ bởi quản trị viên.";
        }
        return "Shop không còn hoạt động.";
    }
}
