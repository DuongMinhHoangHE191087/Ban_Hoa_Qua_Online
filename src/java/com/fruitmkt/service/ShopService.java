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
        shopProfileDAO.updateApprovalStatus(profileId, status, rejectionReason);
    }
}
