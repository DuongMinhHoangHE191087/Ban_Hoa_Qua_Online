package test;

import config.AppConfig;
import dao.auth.UserDAO;
import dao.shop.ShopProfileDAO;
import model.entity.auth.User;
import model.entity.shop.ShopProfile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import service.shop.ShopService;
import util.HashUtil;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ShopServiceTransactionRollbackTest {

    private UserDAO userDAO;
    private ShopProfileDAO shopProfileDAO;
    private ShopService shopService;
    private final List<Integer> userIds = new ArrayList<>();

    @Before
    public void setUp() {
        userDAO = new UserDAO();
        shopProfileDAO = new ShopProfileDAO();
        shopService = new ShopService();
    }

    @After
    public void tearDown() {
        for (int userId : userIds) {
            try {
                shopProfileDAO.deleteByUserId(userId);
            } catch (Exception ignored) {
            }
            try {
                userDAO.deleteUser(userId);
            } catch (Exception ignored) {
            }
        }
        userIds.clear();
    }

    @Test
    public void should_rollbackPhoneUpdate_when_profileSaveFails() throws Exception {
        String originalPhone = buildPhone();
        int userId = userDAO.saveNewCustomer(
                "Rollback User",
                "shop_txn_rollback_" + System.currentTimeMillis() + "@test.com",
                HashUtil.hashPassword("TestPass@2026"),
                originalPhone,
                AppConfig.ROLE_CUSTOMER,
                AppConfig.ACCOUNT_STATUS_ACTIVE,
                true);
        userIds.add(userId);

        User before = userDAO.findUserById(userId);
        assertEquals(originalPhone, before.getPhone());

        ShopProfile profile = new ShopProfile();
        profile.setUserId(userId);
        profile.setShopName(null);
        profile.setShopDescription("This insert must fail and rollback the phone update.");
        profile.setApprovalStatus(AppConfig.SHOP_PENDING);
        profile.setDeliveryAddress("123 Rollback Street");
        profile.setRating(BigDecimal.ZERO);
        profile.setBusinessEmail("txn_biz_rollback_" + System.currentTimeMillis() + "@company.com");

        String updatedPhone = buildPhone();
        try {
            shopService.submitShopApplication(profile, updatedPhone);
            fail("Expected SQLException when shop profile insert fails.");
        } catch (SQLException expected) {
            // Expected: the transaction must rollback the phone update.
        }

        User after = userDAO.findUserById(userId);
        assertEquals("Phone update must be rolled back when profile save fails",
                originalPhone, after.getPhone());
        assertTrue("No shop profile should be created on rollback",
                shopProfileDAO.findByUserId(userId).isEmpty());
    }

    private String buildPhone() {
        return "09" + String.format("%08d", Math.abs(System.nanoTime() % 100_000_000L));
    }
}
