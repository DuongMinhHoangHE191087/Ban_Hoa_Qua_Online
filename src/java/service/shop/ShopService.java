package service.shop;

import dao.shop.ShopProfileDAO;
import model.entity.shop.ShopProfile;
import java.sql.SQLException;
import java.util.List;

import java.util.logging.Logger;
import util.LoggerUtil;

public class ShopService {

    private static final Logger log = LoggerUtil.getLogger(ShopService.class);

    private final ShopProfileDAO shopProfileDAO;

    public ShopService() {
        this.shopProfileDAO = new ShopProfileDAO();
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
        if ("REJECTED".equalsIgnoreCase(status) && (rejectionReason == null || rejectionReason.trim().isEmpty())) {
            throw new IllegalArgumentException("Lý do từ chối không được để trống.");
        }
        ShopProfile shop = shopProfileDAO.findById(profileId);
        if (shop != null) {
            shopProfileDAO.updateApprovalStatus(profileId, shop.getUserId(), status, rejectionReason);
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
}
