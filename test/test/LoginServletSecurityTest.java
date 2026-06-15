package test;

import config.AppConfig;
import dao.auth.UserDAO;
import model.entity.auth.User;
import service.auth.AuthService;
import util.HashUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.Assert.*;

/**
 * LoginServletSecurityTest — Kiểm tra bảo mật luồng đăng nhập:
 * brute-force lockout, tài khoản bị khóa, đăng nhập thành công reset counter.
 *
 * Test trực tiếp AuthService.login() — không cần mock servlet.
 */
public class LoginServletSecurityTest {

    private UserDAO userDAO;
    private AuthService authService;

    private int testUserId = -1;
    private String testEmail;
    private static final String RAW_PASSWORD = "TestPass@2026";

    @Before
    public void setUp() throws SQLException {
        userDAO = new UserDAO();
        authService = new AuthService();
        testEmail = "junit_login_sec_" + System.currentTimeMillis() + "@test.com";

        testUserId = userDAO.saveNewCustomer(
                "Login Security Test User",
                testEmail,
                HashUtil.hashPassword(RAW_PASSWORD),
                buildUniquePhone(0),
                AppConfig.ROLE_CUSTOMER,
                AppConfig.ACCOUNT_STATUS_ACTIVE,
                true
        );
        assertTrue("User phải được tạo", testUserId > 0);
    }

    @After
    public void tearDown() {
        try {
            if (testUserId != -1) {
                resetFailedLogin(testUserId);
                userDAO.deleteUser(testUserId);
                testUserId = -1;
            }
        } catch (SQLException e) {
            System.err.println("[LoginServletSecurityTest] Cleanup failed: " + e.getMessage());
        }
    }

    // =========================================================
    // TC-LSS-01: Mật khẩu sai tăng failed_login_count
    // =========================================================

    @Test
    public void should_incrementFailedCount_on_wrongPassword() throws Exception {
        try {
            authService.login(testEmail, "WrongPassword!");
            fail("Phải ném exception khi sai mật khẩu");
        } catch (Exception e) {
            // expected
        }
        int failedCount = getFailedLoginCount(testUserId);
        assertTrue("failed_login_count phải >= 1 sau khi sai mật khẩu", failedCount >= 1);
    }

    // =========================================================
    // TC-LSS-02: Sai liên tiếp nhiều lần tăng đúng counter
    // =========================================================

    @Test
    public void should_incrementCountForEachWrongAttempt() throws Exception {
        int attempts = 3;
        for (int i = 0; i < attempts; i++) {
            try {
                authService.login(testEmail, "Wrong" + i);
            } catch (Exception ignored) { }
        }
        int failedCount = getFailedLoginCount(testUserId);
        assertTrue("failed_login_count phải >= " + attempts, failedCount >= attempts);
    }

    // =========================================================
    // TC-LSS-03: Khóa tài khoản sau MAX_FAILED_LOGIN lần sai
    // =========================================================

    @Test
    public void should_lockAccount_after_maxFailedAttempts() {
        Exception lastException = null;
        for (int i = 0; i <= AppConfig.MAX_FAILED_LOGIN; i++) {
            try {
                authService.login(testEmail, "WrongPass" + i);
            } catch (Exception e) {
                lastException = e;
            }
        }
        assertNotNull("Phải ném exception", lastException);
        assertNotNull("Exception phải có message", lastException.getMessage());

        try {
            LocalDateTime lockedUntil = getLockedUntil(testUserId);
            assertNotNull("locked_until phải được set", lockedUntil);
            assertTrue("locked_until phải sau thời điểm hiện tại",
                    lockedUntil.isAfter(LocalDateTime.now().minusMinutes(1)));
        } catch (SQLException e) {
            fail("Không thể kiểm tra locked_until: " + e.getMessage());
        }
    }

    // =========================================================
    // TC-LSS-04: Tài khoản bị khóa — từ chối đăng nhập dù đúng mật khẩu
    // =========================================================

    @Test
    public void should_rejectLogin_when_accountIsLocked() throws SQLException {
        lockAccount(testUserId, LocalDateTime.now().plusMinutes(AppConfig.LOCK_DURATION_MINUTES));

        Exception ex = null;
        try {
            authService.login(testEmail, RAW_PASSWORD);
        } catch (Exception e) {
            ex = e;
        }
        assertNotNull("Phải ném exception khi tài khoản bị khóa", ex);
        assertNotNull("Exception phải có message", ex.getMessage());
    }

    // =========================================================
    // TC-LSS-05: Đăng nhập thành công reset failed_login_count
    // =========================================================

    @Test
    public void should_resetFailedCount_after_successfulLogin() throws Exception {
        for (int i = 0; i < 2; i++) {
            try { authService.login(testEmail, "WrongPass"); } catch (Exception ignored) { }
        }
        assertTrue("failed_login_count phải >= 2", getFailedLoginCount(testUserId) >= 2);

        User loggedIn = authService.login(testEmail, RAW_PASSWORD);
        assertNotNull("Phải trả về user khi đăng nhập đúng", loggedIn);

        assertEquals("failed_login_count phải về 0 sau login thành công", 0, getFailedLoginCount(testUserId));
    }

    // =========================================================
    // TC-LSS-06: Tài khoản INACTIVE bị từ chối đăng nhập
    // =========================================================

    @Test
    public void should_rejectLogin_when_accountIsInactive() throws SQLException {
        setStatus(testUserId, AppConfig.ACCOUNT_STATUS_INACTIVE);
        Exception ex = null;
        try {
            authService.login(testEmail, RAW_PASSWORD);
        } catch (Exception e) {
            ex = e;
        }
        assertNotNull("Phải ném exception khi tài khoản INACTIVE", ex);
    }

    // =========================================================
    // TC-LSS-07: Email trống / null bị từ chối
    // =========================================================

    @Test
    public void should_rejectLogin_when_emailIsBlank() {
        assertLoginFails("", RAW_PASSWORD, "email trống");
        assertLoginFails("   ", RAW_PASSWORD, "email toàn khoảng trắng");
    }

    // =========================================================
    // Helpers
    // =========================================================

    private void assertLoginFails(String identifier, String password, String scenario) {
        try {
            authService.login(identifier, password);
            fail("Phải ném exception khi " + scenario);
        } catch (Exception e) {
            // expected
        }
    }

    private int getFailedLoginCount(int userId) throws SQLException {
        try (Connection conn = userDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT failed_login_count FROM users WHERE user_id = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("failed_login_count");
            }
        }
        return 0;
    }

    private LocalDateTime getLockedUntil(int userId) throws SQLException {
        try (Connection conn = userDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT locked_until FROM users WHERE user_id = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    java.sql.Timestamp ts = rs.getTimestamp("locked_until");
                    return ts != null ? ts.toLocalDateTime() : null;
                }
            }
        }
        return null;
    }

    private void lockAccount(int userId, LocalDateTime until) throws SQLException {
        try (Connection conn = userDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE users SET status = 'LOCKED', locked_until = ? WHERE user_id = ?")) {
            ps.setTimestamp(1, java.sql.Timestamp.valueOf(until));
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    private void resetFailedLogin(int userId) throws SQLException {
        try (Connection conn = userDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE users SET failed_login_count = 0, locked_until = NULL, status = 'ACTIVE' WHERE user_id = ?")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    private void setStatus(int userId, String status) throws SQLException {
        try (Connection conn = userDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE users SET status = ? WHERE user_id = ?")) {
            ps.setString(1, status);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    private String buildUniquePhone(int offset) {
        return "09" + String.format("%08d",
                Math.abs((System.nanoTime() + offset) % 100_000_000L));
    }
}
