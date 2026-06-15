package test;

import config.AppConfig;
import dao.auth.UserDAO;
import dao.cart.CartDAO;
import model.entity.auth.User;
import service.auth.AuthService;
import util.HashUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * RegisterFlowIntegrationTest — Kiểm tra luồng đăng ký:
 * email/phone trùng, định dạng sai, tạo cart sau đăng ký.
 */
public class RegisterFlowIntegrationTest {

    private UserDAO userDAO;
    private CartDAO cartDAO;
    private AuthService authService;
    private final List<Integer> createdUserIds = new ArrayList<>();

    @Before
    public void setUp() {
        userDAO = new UserDAO();
        cartDAO = new CartDAO();
        authService = new AuthService();
    }

    @After
    public void tearDown() {
        for (int userId : createdUserIds) {
            try { userDAO.deleteUser(userId); } catch (SQLException ignored) { }
        }
        createdUserIds.clear();
    }

    // =========================================================
    // TC-REG-01: Đăng ký thành công — tất cả trường hợp lệ
    // =========================================================

    @Test
    public void should_registerSuccessfully_when_allFieldsValid() throws Exception {
        User user = buildRegistrationUser(
                "Test User", "reg_ok_" + System.currentTimeMillis() + "@test.com",
                "SecurePass@2026!", buildPhone(1));

        User registered = authService.register(user);
        assertNotNull("Phải trả về user sau đăng ký", registered);
        assertTrue("User ID phải > 0", registered.getUserId() > 0);
        createdUserIds.add(registered.getUserId());

        User found = userDAO.findByEmail(user.getEmail());
        assertNotNull("Phải tìm thấy user trong DB", found);
        assertEquals("Email phải khớp", user.getEmail(), found.getEmail());
        assertEquals("Role phải là CUSTOMER", AppConfig.ROLE_CUSTOMER, found.getRole());
    }

    // =========================================================
    // TC-REG-02: Đăng ký trùng email — phải báo lỗi
    // =========================================================

    @Test
    public void should_throwException_when_emailAlreadyRegistered() throws Exception {
        String email = "reg_dup_" + System.currentTimeMillis() + "@test.com";

        User first = authService.register(buildRegistrationUser("First User", email, "SecurePass@2026!", buildPhone(2)));
        createdUserIds.add(first.getUserId());

        try {
            User second = authService.register(buildRegistrationUser("Second User", email, "SecurePass@2026!", buildPhone(3)));
            if (second != null) createdUserIds.add(second.getUserId());
            fail("Phải ném exception khi email đã tồn tại");
        } catch (Exception e) {
            assertNotNull("Exception phải có message", e.getMessage());
        }
    }

    // =========================================================
    // TC-REG-03: Đăng ký trùng phone — phải báo lỗi
    // =========================================================

    @Test
    public void should_throwException_when_phoneAlreadyRegistered() throws Exception {
        String phone = buildPhone(4);

        User first = authService.register(buildRegistrationUser(
                "First User", "reg_phone1_" + System.currentTimeMillis() + "@test.com", "SecurePass@2026!", phone));
        createdUserIds.add(first.getUserId());

        try {
            User second = authService.register(buildRegistrationUser(
                    "Second User", "reg_phone2_" + System.currentTimeMillis() + "@test.com", "SecurePass@2026!", phone));
            if (second != null) createdUserIds.add(second.getUserId());
            fail("Phải ném exception khi phone đã tồn tại");
        } catch (Exception e) {
            assertNotNull("Exception phải có message", e.getMessage());
        }
    }

    // =========================================================
    // TC-REG-04: Mật khẩu quá ngắn (< 8 ký tự)
    // =========================================================

    @Test
    public void should_throwException_when_passwordTooShort() {
        try {
            User u = buildRegistrationUser(
                    "Test User", "reg_shortpass_" + System.currentTimeMillis() + "@test.com",
                    "abc", buildPhone(5));
            User result = authService.register(u);
            if (result != null) createdUserIds.add(result.getUserId());
            fail("Phải ném exception khi mật khẩu quá ngắn");
        } catch (Exception e) {
            assertNotNull("Exception phải có message", e.getMessage());
        }
    }

    // =========================================================
    // TC-REG-05: Email không hợp lệ
    // =========================================================

    @Test
    public void should_throwException_when_emailFormatInvalid() {
        String[] badEmails = {"notanemail", "@nolocal.com", "nodomain@", ""};
        for (String badEmail : badEmails) {
            try {
                User u = buildRegistrationUser("Test", badEmail, "SecurePass@2026!", buildPhone(6));
                User result = authService.register(u);
                if (result != null) createdUserIds.add(result.getUserId());
                fail("Phải ném exception cho email không hợp lệ: " + badEmail);
            } catch (Exception e) {
                // expected
            }
        }
    }

    // =========================================================
    // TC-REG-06: Tên trống / null bị từ chối
    // =========================================================

    @Test
    public void should_throwException_when_fullNameIsBlank() {
        try {
            User u = buildRegistrationUser(
                    "", "reg_noname_" + System.currentTimeMillis() + "@test.com",
                    "SecurePass@2026!", buildPhone(7));
            User result = authService.register(u);
            if (result != null) createdUserIds.add(result.getUserId());
            fail("Phải ném exception khi tên trống");
        } catch (Exception e) {
            // expected
        }
    }

    // =========================================================
    // TC-REG-07: Tài khoản mới phải có trạng thái chưa xác minh email
    // =========================================================

    @Test
    public void should_createAccount_with_inactiveOrPendingStatus() throws Exception {
        User user = buildRegistrationUser(
                "Status User", "reg_status_" + System.currentTimeMillis() + "@test.com",
                "SecurePass@2026!", buildPhone(8));
        User registered = authService.register(user);
        createdUserIds.add(registered.getUserId());

        User found = userDAO.findByEmail(user.getEmail());
        assertNotNull(found);
        boolean isNotActiveYet = AppConfig.ACCOUNT_STATUS_INACTIVE.equals(found.getStatus())
                || "PENDING".equals(found.getStatus())
                || !found.isEmailVerified();
        assertTrue("Tài khoản mới không được ở trạng thái ACTIVE ngay", isNotActiveYet);
    }

    // =========================================================
    // TC-REG-08: Đăng ký tạo cart cho customer mới
    // =========================================================

    @Test
    public void should_createCartForNewCustomer() throws Exception {
        User user = buildRegistrationUser(
                "Cart User", "reg_cart_" + System.currentTimeMillis() + "@test.com",
                "SecurePass@2026!", buildPhone(9));
        User registered = authService.register(user);
        createdUserIds.add(registered.getUserId());

        // Cart phải được tạo ngay sau đăng ký
        List<?> carts = cartDAO.findByCustomer(registered.getUserId());
        assertNotNull("findByCustomer không được null", carts);
        assertFalse("Cart phải được tạo cho customer mới", carts.isEmpty());
    }

    // =========================================================
    // Helpers
    // =========================================================

    private User buildRegistrationUser(String fullName, String email, String rawPassword, String phone) {
        User u = new User();
        u.setFullName(fullName);
        u.setEmail(email);
        // AuthService.register() thường nhận raw password rồi tự hash, hoặc nhận hash
        // Dựa theo pattern từ RegisterServlet — set passwordHash trực tiếp
        u.setPasswordHash(rawPassword); // service sẽ validate length trước khi hash
        u.setPhone(phone);
        u.setRole(AppConfig.ROLE_CUSTOMER);
        u.setStatus(AppConfig.ACCOUNT_STATUS_INACTIVE);
        u.setEmailVerified(false);
        return u;
    }

    private String buildPhone(int offset) {
        return "09" + String.format("%08d",
                Math.abs((System.nanoTime() + offset) % 100_000_000L));
    }
}
