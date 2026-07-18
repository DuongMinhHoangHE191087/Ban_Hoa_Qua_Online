package service.shop;

import config.AppConfig;
import dao.auth.UserDAO;
import dao.shop.ShopProfileDAO;
import model.entity.catalog.Product;
import model.entity.shop.ShopProfile;
import model.dto.common.PagedResultDTO;
import service.catalog.ProductService;
import service.order.OrderService;
import util.PaginationUtil;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class ShopService {

    private final ShopProfileDAO shopProfileDAO;
    private final UserDAO userDAO;
    private final ProductService productService;
    private final OrderService orderService;

    public ShopService() {
        this.shopProfileDAO = new ShopProfileDAO();
        this.userDAO = new UserDAO();
        this.productService = new ProductService();
        this.orderService = new OrderService();
    }

    @FunctionalInterface
    private interface TransactionWork<T> {
        T execute(Connection conn) throws SQLException;
    }

    private <T> T withTransaction(TransactionWork<T> work) throws SQLException {
        try (Connection conn = shopProfileDAO.getConnection()) {
            conn.setAutoCommit(false);
            try {
                T result = work.execute(conn);
                conn.commit();
                return result;
            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }
                throw e;
            }
        }
    }

    public List<ShopProfile> getAllShops() throws SQLException {
        return shopProfileDAO.findAll(null); // null means get all regardless of status
    }

    public PagedResultDTO getAllShopsPaged(int page, int pageSize) throws SQLException {
        return getShopsPaged(null, page, pageSize);
    }

    public List<ShopProfile> getShopsByStatus(String status) throws SQLException {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status không được để trống.");
        }
        return shopProfileDAO.findAll(status);
    }

    public PagedResultDTO getShopsPaged(String status, int page, int pageSize) throws SQLException {
        String normalizedStatus = (status == null || status.trim().isEmpty()) ? null : status.trim().toUpperCase(Locale.ROOT);
        int validatedPage = PaginationUtil.validatePage(page);
        int validatedPageSize = PaginationUtil.validatePageSize(pageSize);
        List<ShopProfile> shops = shopProfileDAO.findAll(normalizedStatus, validatedPage, validatedPageSize);
        int totalCount = shopProfileDAO.countAll(normalizedStatus);
        return PaginationUtil.buildPagedResult(shops, validatedPage, validatedPageSize, totalCount);
    }

    public int countShops(String status) throws SQLException {
        String normalizedStatus = (status == null || status.trim().isEmpty()) ? null : status.trim().toUpperCase(Locale.ROOT);
        return shopProfileDAO.countAll(normalizedStatus);
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

    public int submitShopApplication(ShopProfile profile, String phoneToUpdate) throws SQLException {
        if (profile == null) {
            throw new IllegalArgumentException("Profile không được null.");
        }
        if (profile.getUserId() <= 0) {
            throw new IllegalArgumentException("User ID không hợp lệ.");
        }
        return withTransaction(conn -> {
            ensureBusinessEmailAvailable(conn, profile.getBusinessEmail(), profile.getUserId());
            updatePhoneIfNeeded(conn, profile.getUserId(), phoneToUpdate);
            return shopProfileDAO.save(conn, profile);
        });
    }

    public void resubmitRejectedShopApplication(ShopProfile profile, String phoneToUpdate) throws SQLException {
        if (profile == null) {
            throw new IllegalArgumentException("Profile không được null.");
        }
        if (profile.getProfileId() <= 0 || profile.getUserId() <= 0) {
            throw new IllegalArgumentException("Thông tin profile không hợp lệ.");
        }
        withTransaction(conn -> {
            ensureBusinessEmailAvailable(conn, profile.getBusinessEmail(), profile.getUserId());
            updatePhoneIfNeeded(conn, profile.getUserId(), phoneToUpdate);
            shopProfileDAO.update(conn, profile);
            return null;
        });
    }

    public ShopProfile finalizeRegisteredShopProfile(int userId, String shopName, String shopAddress,
            String preferredCategoriesJson, String businessEmail, String docPathsJson) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID không hợp lệ.");
        }
        return withTransaction(conn -> {
            ensureBusinessEmailAvailable(conn, businessEmail, userId);
            ShopProfile profile = shopProfileDAO.findOneByUserId(conn, userId);
            if (profile == null) {
                profile = new ShopProfile();
                profile.setUserId(userId);
                profile.setShopName(shopName != null && !shopName.trim().isEmpty()
                        ? shopName.trim()
                        : "Cửa hàng của user #" + userId);
                profile.setShopDescription("Chào mừng tới cửa hàng của chúng tôi!");
                profile.setApprovalStatus(AppConfig.SHOP_PENDING);
                profile.setDeliveryAddress(shopAddress != null && !shopAddress.trim().isEmpty()
                        ? shopAddress.trim()
                        : null);
                profile.setRating(BigDecimal.ZERO);
                profile.setPreferredCategories(preferredCategoriesJson);
                profile.setDocPaths(docPathsJson);
                profile.setBusinessEmail(businessEmail != null ? businessEmail.trim() : null);
                shopProfileDAO.save(conn, profile);
                return profile;
            }
            if (shopName != null && !shopName.trim().isEmpty()) {
                profile.setShopName(shopName.trim());
            }
            if (shopAddress != null && !shopAddress.trim().isEmpty()) {
                profile.setDeliveryAddress(shopAddress.trim());
            }
            if (preferredCategoriesJson != null) {
                profile.setPreferredCategories(preferredCategoriesJson);
            }
            if (docPathsJson != null) {
                profile.setDocPaths(docPathsJson);
            }
            profile.setBusinessEmail(businessEmail != null ? businessEmail.trim() : null);
            shopProfileDAO.update(conn, profile);
            return profile;
        });
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

    private void updatePhoneIfNeeded(Connection conn, int userId, String phoneToUpdate) throws SQLException {
        if (phoneToUpdate == null || phoneToUpdate.trim().isEmpty()) {
            return;
        }
        if (userDAO.isPhoneExists(conn, phoneToUpdate, userId)) {
            throw new SQLException("Số điện thoại đã được đăng ký bởi tài khoản khác, vui lòng đăng nhập!");
        }
        userDAO.updatePhone(conn, userId, phoneToUpdate.trim());
    }

    private void ensureBusinessEmailAvailable(Connection conn, String businessEmail, int userId) throws SQLException {
        if (businessEmail == null || businessEmail.trim().isEmpty()) {
            throw new SQLException("Email liên hệ kinh doanh không được để trống.");
        }
        if (shopProfileDAO.isBusinessEmailExists(conn, businessEmail, userId)) {
            throw new SQLException("Mỗi doanh nghiệp chỉ được đăng ký tối đa 1 gian hàng! Email kinh doanh này đã được sử dụng.");
        }
    }
}
