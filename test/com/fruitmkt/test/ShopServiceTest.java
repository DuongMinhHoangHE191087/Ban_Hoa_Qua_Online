package com.fruitmkt.test;

import com.fruitmkt.service.ShopService;
import com.fruitmkt.model.entity.ShopProfile;
import org.junit.Test;
import org.junit.Before;

import java.sql.SQLException;

import static org.junit.Assert.*;

public class ShopServiceTest {

    private ShopService shopService;

    @Before
    public void setUp() {
        shopService = new ShopService();
    }

    @Test(expected = SQLException.class)
    public void getAllShops_canThrow() throws SQLException {
        shopService.getAllShops();
    }

    @Test(expected = SQLException.class)
    public void getShopsByStatus_nullStatus_throws() throws SQLException {
        shopService.getShopsByStatus(null);
    }

    @Test(expected = SQLException.class)
    public void getShopsByStatus_canThrow() throws SQLException {
        shopService.getShopsByStatus("ACTIVE");
    }

    @Test(expected = SQLException.class)
    public void updateShopStatus_negativeProfileId_throws() throws SQLException {
        shopService.updateShopStatus(-1, "APPROVED", "");
    }

    @Test(expected = SQLException.class)
    public void updateShopStatus_nullStatus_throws() throws SQLException {
        shopService.updateShopStatus(1, null, "");
    }

    @Test(expected = SQLException.class)
    public void updateShopStatus_nullRejectionReason_throws() throws SQLException {
        shopService.updateShopStatus(1, "REJECTED", null);
    }

    @Test(expected = SQLException.class)
    public void getShopByUserId_negativeUserId_throws() throws SQLException {
        shopService.getShopByUserId(-1);
    }

    @Test(expected = SQLException.class)
    public void getShopByUserId_zeroUserId_throws() throws SQLException {
        shopService.getShopByUserId(0);
    }

    @Test(expected = SQLException.class)
    public void updateShopProfile_nullProfile_throws() throws SQLException {
        shopService.updateShopProfile(null);
    }

    @Test(expected = SQLException.class)
    public void getShopById_negativeProfileId_throws() throws SQLException {
        shopService.getShopById(-1);
    }

    @Test(expected = SQLException.class)
    public void getShopById_zeroProfileId_throws() throws SQLException {
        shopService.getShopById(0);
    }
}
