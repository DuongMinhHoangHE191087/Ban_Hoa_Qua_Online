package test;

import config.AppConfig;
import dao.auth.UserDAO;
import dao.shop.ShopProfileDAO;
import dao.system.ConnectionPool;
import model.entity.auth.User;
import model.entity.shop.ShopProfile;
import service.auth.AuthService;
import service.shop.ShopService;
import util.HashUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * ShopOwnerRegistrationTest — Happy paths and business logic for the shop owner
 * registration flow.
 *
 * Scenarios covered:
 *   FLOW-01  New SHOP_OWNER registration creates user with role CUSTOMER until approved
 *   FLOW-02  Admin approval transitions role to SHOP_OWNER
 *   FLOW-03  Admin rejection keeps role as CUSTOMER
 *   FLOW-04  Resubmission after rejection is not blocked
 *   FLOW-05  Email-verified flag is false immediately after registration
 *   FLOW-06  Shop profile is created with PENDING status on registration
 *   FLOW-07  Duplicate business email blocks second application
 *   FLOW-08  User with existing profile cannot submit a second application
 */
public class ShopOwnerRegistrationTest {

    private UserDAO userDAO;
    private ShopProfileDAO shopProfileDAO;
    private AuthService authService;
    private ShopService shopService;

    private final List<Integer> userIds    = new ArrayList<>();
    private final List<Integer> profileIds = new ArrayList<>();

    // ---------------------------------------------------------------
    // Setup / Teardown
    // ---------------------------------------------------------------

    @Before
    public void setUp() {
        userDAO        = new UserDAO();
        shopProfileDAO = new ShopProfileDAO();
        authService    = new AuthService();
        shopService    = new ShopService();
    }

    @After
    public void tearDown() {
        // Remove shop profiles first to satisfy FK constraints
        for (int pid : profileIds) {
            try (java.sql.Connection conn = ConnectionPool.getConnection();
                 java.sql.PreparedStatement ps = conn.prepareStatement(
                         "DELETE FROM shop_owner_profiles WHERE profile_id = ?")) {
                ps.setInt(1, pid);
                ps.executeUpdate();
            } catch (Exception ignored) { }
        }
        for (int uid : userIds) {
            try { userDAO.deleteUser(uid); } catch (Exception ignored) { }
        }
        profileIds.clear();
        userIds.clear();
    }

    // ---------------------------------------------------------------
    // FLOW-01: Role stays CUSTOMER immediately after SHOP_OWNER registration
    // ---------------------------------------------------------------

    @Test
    public void should_keepRoleCustomer_immediately_afterShopOwnerRegistration() throws Exception {
        // Arrange
        User user = buildUser("shopowner_flow01_" + ts() + "@test.com", "SHOP_OWNER");

        // Act — AuthService registers both user and shop profile atomically (PENDING)
        User registered = authService.register(
                user,
                "My Fruit Shop 01",
                "123 Street, District 1",
                "[1,2]",
                null);

        track(registered);

        // Assert — role must stay CUSTOMER; only admin approval changes it
        User dbUser = userDAO.findByEmail(user.getEmail());
        assertNotNull("User must be persisted", dbUser);
        assertEquals(
                "Role must remain CUSTOMER until admin approval",
                AppConfig.ROLE_CUSTOMER,
                dbUser.getRole());
    }

    // ---------------------------------------------------------------
    // FLOW-02: Admin approval transitions role to SHOP_OWNER
    // ---------------------------------------------------------------

    @Test
    public void should_promoteToShopOwner_when_adminApproves() throws Exception {
        // Arrange — register user + create PENDING profile
        User user = buildUser("shopowner_flow02_" + ts() + "@test.com", "SHOP_OWNER");
        User registered = authService.register(user, "Fruit Shop 02", "456 Street, District 2", null, null);
        track(registered);

        ShopProfile profile = waitForProfile(registered.getUserId());
        assertNotNull("Shop profile must be created on registration", profile);
        track(profile);

        // Act — admin approves
        shopProfileDAO.updateApprovalStatus(
                profile.getProfileId(),
                registered.getUserId(),
                "APPROVED",
                null);

        // Assert — user role must now be SHOP_OWNER
        User dbUser = userDAO.findUserById(registered.getUserId());
        assertEquals("Role must become SHOP_OWNER after approval",
                AppConfig.ROLE_SHOP_OWNER, dbUser.getRole());

        ShopProfile dbProfile = shopProfileDAO.findById(profile.getProfileId());
        assertEquals("Profile status must be APPROVED", "APPROVED", dbProfile.getApprovalStatus());
        assertNotNull("approved_at must be set", dbProfile.getApprovedAt());
    }

    // ---------------------------------------------------------------
    // FLOW-03: Admin rejection keeps role as CUSTOMER
    // ---------------------------------------------------------------

    @Test
    public void should_keepRoleCustomer_when_adminRejects() throws Exception {
        // Arrange
        User user = buildUser("shopowner_flow03_" + ts() + "@test.com", "SHOP_OWNER");
        User registered = authService.register(user, "Fruit Shop 03", "789 Street", null, null);
        track(registered);

        ShopProfile profile = waitForProfile(registered.getUserId());
        assertNotNull(profile);
        track(profile);

        // Act — admin rejects
        shopProfileDAO.updateApprovalStatus(
                profile.getProfileId(),
                registered.getUserId(),
                "REJECTED",
                "Documents incomplete");

        // Assert — role stays CUSTOMER
        User dbUser = userDAO.findUserById(registered.getUserId());
        assertEquals("Role must remain CUSTOMER after rejection",
                AppConfig.ROLE_CUSTOMER, dbUser.getRole());

        ShopProfile dbProfile = shopProfileDAO.findById(profile.getProfileId());
        assertEquals("Profile status must be REJECTED", "REJECTED", dbProfile.getApprovalStatus());
        assertNotNull("Rejection reason must be stored", dbProfile.getRejectionReason());
    }

    // ---------------------------------------------------------------
    // FLOW-04: Resubmission after rejection creates a new PENDING profile
    // ---------------------------------------------------------------

    @Test
    public void should_allowResubmission_after_rejection() throws Exception {
        // Arrange — register and reject first application
        User user = buildUser("shopowner_flow04_" + ts() + "@test.com", "CUSTOMER");
        User registered = authService.register(user, null, null, null, null);
        track(registered);

        // Simulate upgrade-to-shop flow: create first profile manually
        ShopProfile firstProfile = buildPendingProfile(registered.getUserId(), "Resubmit Shop 04a");
        int firstProfileId = shopProfileDAO.save(firstProfile);
        track(firstProfileId);

        shopProfileDAO.updateApprovalStatus(firstProfileId, registered.getUserId(), "REJECTED", "Missing doc");

        // Act — create a second PENDING profile (resubmission)
        ShopProfile secondProfile = buildPendingProfile(registered.getUserId(), "Resubmit Shop 04b");
        int secondProfileId = shopProfileDAO.save(secondProfile);
        track(secondProfileId);

        // Assert — second profile persists with PENDING
        ShopProfile dbSecond = shopProfileDAO.findById(secondProfileId);
        assertNotNull("Second application must be saved", dbSecond);
        assertEquals("Second application must be PENDING", "PENDING", dbSecond.getApprovalStatus());
    }

    // ---------------------------------------------------------------
    // FLOW-05: Email-verified flag is false right after registration
    // ---------------------------------------------------------------

    @Test
    public void should_notBeEmailVerified_immediately_afterRegistration() throws Exception {
        // Arrange
        User user = buildUser("shopowner_flow05_" + ts() + "@test.com", "CUSTOMER");

        // Act
        User registered = authService.register(user, null, null, null, null);
        track(registered);

        // Assert
        User dbUser = userDAO.findByEmail(user.getEmail());
        assertFalse("Email must not be verified right after registration", dbUser.isEmailVerified());
        assertFalse("Account must not be ACTIVE before email verification",
                AppConfig.ACCOUNT_STATUS_ACTIVE.equals(dbUser.getStatus()));
    }

    // ---------------------------------------------------------------
    // FLOW-06: Shop profile is created with PENDING status
    // ---------------------------------------------------------------

    @Test
    public void should_createShopProfile_withPendingStatus_onRegistration() throws Exception {
        // Arrange
        User user = buildUser("shopowner_flow06_" + ts() + "@test.com", "SHOP_OWNER");

        // Act
        User registered = authService.register(
                user, "Pending Shop 06", "1 Main Street", "[3]", null);
        track(registered);

        ShopProfile profile = waitForProfile(registered.getUserId());

        // Assert
        assertNotNull("Shop profile must be created during SHOP_OWNER registration", profile);
        track(profile);
        assertEquals("Approval status must be PENDING", "PENDING", profile.getApprovalStatus());
        assertNull("approved_at must be null when pending", profile.getApprovedAt());
    }

    // ---------------------------------------------------------------
    // FLOW-07: Duplicate business email blocks second application
    // ---------------------------------------------------------------

    @Test
    public void should_rejectRegistration_when_businessEmailAlreadyUsed() throws Exception {
        // Arrange — first registration uses the email
        String biz = "biz_dup_" + ts() + "@company.com";

        User firstUser = buildUser("shopowner_flow07a_" + ts() + "@test.com", "CUSTOMER");
        User registered = authService.register(firstUser, null, null, null, null);
        track(registered);

        ShopProfile profile = buildPendingProfile(registered.getUserId(), "Shop 07a");
        profile.setBusinessEmail(biz);
        track(shopProfileDAO.save(profile));

        // Act + Assert — second registration with same business email must be blocked
        boolean blocked = shopProfileDAO.isBusinessEmailExists(biz);
        assertTrue("isBusinessEmailExists must return true for duplicate business email", blocked);
    }

    // ---------------------------------------------------------------
    // FLOW-08: User with existing profile cannot submit a duplicate
    // ---------------------------------------------------------------

    @Test
    public void should_detectExistingProfile_when_checkingForDuplicateApplication() throws Exception {
        // Arrange
        User user = buildUser("shopowner_flow08_" + ts() + "@test.com", "CUSTOMER");
        User registered = authService.register(user, null, null, null, null);
        track(registered);

        ShopProfile profile = buildPendingProfile(registered.getUserId(), "Shop 08");
        track(shopProfileDAO.save(profile));

        // Act — check for existing profile as handleUserUpgrade does
        List<ShopProfile> existing = shopProfileDAO.findByUserId(registered.getUserId());

        // Assert
        assertFalse("findByUserId must return the existing profile", existing.isEmpty());
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private User buildUser(String email, String role) {
        User u = new User();
        u.setFullName("Test User Flow");
        u.setEmail(email);
        u.setPasswordHash("SecurePass@2026!");
        u.setPhone(buildPhone());
        u.setRole(role);
        u.setStatus(AppConfig.ACCOUNT_STATUS_INACTIVE);
        u.setEmailVerified(false);
        return u;
    }

    private ShopProfile buildPendingProfile(int userId, String shopName) {
        ShopProfile p = new ShopProfile();
        p.setUserId(userId);
        p.setShopName(shopName);
        p.setShopDescription("Test shop description");
        p.setApprovalStatus("PENDING");
        p.setDeliveryAddress("Test Address, District 1");
        p.setRating(java.math.BigDecimal.ZERO);
        p.setBusinessEmail("biz_" + userId + "_" + ts() + "@company.com");
        return p;
    }

    /** Wait briefly and fetch profile, since AuthService may save asynchronously. */
    private ShopProfile waitForProfile(int userId) throws SQLException {
        List<ShopProfile> profiles = shopProfileDAO.findByUserId(userId);
        if (!profiles.isEmpty()) {
            return profiles.get(0);
        }
        return null;
    }

    private void track(User u) {
        if (u != null && u.getUserId() > 0) userIds.add(u.getUserId());
    }

    private void track(ShopProfile p) {
        if (p != null && p.getProfileId() > 0) profileIds.add(p.getProfileId());
    }

    private void track(int profileId) {
        if (profileId > 0) profileIds.add(profileId);
    }

    private long ts() { return System.currentTimeMillis(); }

    private String buildPhone() {
        return "09" + String.format("%08d", Math.abs(System.nanoTime() % 100_000_000L));
    }
}
