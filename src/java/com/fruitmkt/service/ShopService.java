package com.fruitmkt.service;

import com.fruitmkt.dao.ShopProfileDAO;
import com.fruitmkt.model.entity.ShopProfile;
import java.sql.SQLException;
import java.util.List;

public class ShopService {
    private final ShopProfileDAO shopProfileDAO;

    public ShopService() {
        this.shopProfileDAO = new ShopProfileDAO();
    }

    public List<ShopProfile> getAllShops() throws SQLException {
        return shopProfileDAO.findAll(null); // null means get all regardless of status
    }

    public List<ShopProfile> getShopsByStatus(String status) throws SQLException {
        return shopProfileDAO.findAll(status);
    }

    public void updateShopStatus(int profileId, String status, String rejectionReason) throws SQLException {
        ShopProfile shop = shopProfileDAO.findById(profileId);
        if (shop != null) {
            shopProfileDAO.updateApprovalStatus(profileId, shop.getUserId(), status, rejectionReason);
        }
    }

    public ShopProfile getShopByUserId(int userId) throws SQLException {
        List<ShopProfile> profiles = shopProfileDAO.findByUserId(userId);
        if (profiles != null && !profiles.isEmpty()) {
            return profiles.get(0);
        }
        return null;
    }

    public void updateShopProfile(ShopProfile profile) throws SQLException {
        shopProfileDAO.update(profile);
    }

    public ShopProfile getShopById(int profileId) throws SQLException {
        return shopProfileDAO.findById(profileId);
    }
}
