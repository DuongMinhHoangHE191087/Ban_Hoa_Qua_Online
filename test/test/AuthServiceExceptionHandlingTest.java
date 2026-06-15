package test;

import service.auth.AuthService;
import service.auth.AuthService.VerificationRequiredException;
import model.entity.auth.User;
import config.AppConfig;
import util.HashUtil;
import dao.auth.UserDAO;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.Assert.*;

/**
 * Comprehensive exception handling tests for AuthService.
 * Covers: validation errors, business logic violations, database errors,
 * authorization failures, state transitions, and resource not found scenarios.
 */
public class AuthServiceExceptionHandlingTest {

    private AuthService authService;

    @Before
    public void setUp() {
        authService = new AuthService();
    }

    @After
    public void tearDown() {
        // Cleanup test data
    }

    // ============= INPUT VALIDATION ERRORS =============

    @Test
    public void register_nullUser_throws() {
        try {
            authService.register(null);
            fail("Should throw IllegalArgumentException for null user");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("tài khoản"));
        } catch (Exception e) {
            fail("Should throw IllegalArgumentException, got " + e.getClass().getName());
        }
    }

    @Test
    public void register_nullFullName_throws() {
        User user = new User();
        user.setFullName(null);
        user.setEmail("test@example.com");
        user.setPasswordHash("validpass123");
        user.setPhone("0123456789");

        try {
            authService.register(user);
            fail("Should throw exception for null fullName");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Họ và tên"));
        }
    }

    @Test
    public void register_blankFullName_throws() {
        User user = new User();
        user.setFullName("   ");
        user.setEmail("test@example.com");
        user.setPasswordHash("validpass123");
        user.setPhone("0123456789");

        try {
            authService.register(user);
            fail("Should throw exception for blank fullName");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Họ và tên"));
        }
    }

    @Test
    public void register_fullNameTooLong_throws() {
        User user = new User();
        user.setFullName("a".repeat(256)); // Exceeds typical max length
        user.setEmail("test@example.com");
        user.setPasswordHash("validpass123");
        user.setPhone("0123456789");

        try {
            authService.register(user);
            fail("Should throw exception for name too long");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void register_nullEmail_throws() {
        User user = new User();
        user.setFullName("John Doe");
        user.setEmail(null);
        user.setPasswordHash("validpass123");
        user.setPhone("0123456789");

        try {
            authService.register(user);
            fail("Should throw exception for null email");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Email"));
        }
    }

    @Test
    public void register_blankEmail_throws() {
        User user = new User();
        user.setFullName("John Doe");
        user.setEmail("   ");
        user.setPasswordHash("validpass123");
        user.setPhone("0123456789");

        try {
            authService.register(user);
            fail("Should throw exception for blank email");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Email"));
        }
    }

    @Test
    public void register_invalidEmailFormat_throws() {
        User user = new User();
        user.setFullName("John Doe");
        user.setEmail("not-an-email");
        user.setPasswordHash("validpass123");
        user.setPhone("0123456789");

        try {
            authService.register(user);
            fail("Should throw exception for invalid email format");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Email"));
        }
    }

    @Test
    public void register_emailMissingAtSign_throws() {
        User user = new User();
        user.setFullName("John Doe");
        user.setEmail("testexample.com");
        user.setPasswordHash("validpass123");
        user.setPhone("0123456789");

        try {
            authService.register(user);
            fail("Should throw exception for email missing @");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Email"));
        }
    }

    @Test
    public void register_emailMissingDomain_throws() {
        User user = new User();
        user.setFullName("John Doe");
        user.setEmail("test@");
        user.setPasswordHash("validpass123");
        user.setPhone("0123456789");

        try {
            authService.register(user);
            fail("Should throw exception for email missing domain");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Email"));
        }
    }

    @Test
    public void register_nullPassword_throws() {
        User user = new User();
        user.setFullName("John Doe");
        user.setEmail("john@example.com");
        user.setPasswordHash(null);
        user.setPhone("0123456789");

        try {
            authService.register(user);
            fail("Should throw exception for null password");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Mật khẩu"));
        }
    }

    @Test
    public void register_blankPassword_throws() {
        User user = new User();
        user.setFullName("John Doe");
        user.setEmail("john@example.com");
        user.setPasswordHash("   ");
        user.setPhone("0123456789");

        try {
            authService.register(user);
            fail("Should throw exception for blank password");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Mật khẩu"));
        }
    }

    @Test
    public void register_passwordTooShort_throws() {
        User user = new User();
        user.setFullName("John Doe");
        user.setEmail("john@example.com");
        user.setPasswordHash("short");
        user.setPhone("0123456789");

        try {
            authService.register(user);
            fail("Should throw exception for password too short");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Mật khẩu"));
        }
    }

    @Test
    public void register_nullPhone_throws() {
        User user = new User();
        user.setFullName("John Doe");
        user.setEmail("john@example.com");
        user.setPasswordHash("validpass123");
        user.setPhone(null);

        try {
            authService.register(user);
            fail("Should throw exception for null phone");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("điện thoại") || e.getMessage().contains("Email"));
        }
    }

    @Test
    public void register_blankPhone_throws() {
        User user = new User();
        user.setFullName("John Doe");
        user.setEmail("john@example.com");
        user.setPasswordHash("validpass123");
        user.setPhone("   ");

        try {
            authService.register(user);
            fail("Should throw exception for blank phone");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("điện thoại") || e.getMessage().contains("Email"));
        }
    }

    @Test
    public void register_invalidPhoneFormat_throws() {
        User user = new User();
        user.setFullName("John Doe");
        user.setEmail("john@example.com");
        user.setPasswordHash("validpass123");
        user.setPhone("abc-def-ghij");

        try {
            authService.register(user);
            fail("Should throw exception for invalid phone format");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= BUSINESS LOGIC VIOLATIONS =============

    @Test
    public void register_duplicateEmail_throws() {
        // First registration
        User user1 = new User();
        user1.setFullName("John Doe");
        user1.setEmail("unique@example.com");
        user1.setPasswordHash("validpass123");
        user1.setPhone("0123456789");

        try {
            authService.register(user1);
        } catch (Exception e) {
            // First registration may fail due to DB, that's ok for this test
        }

        // Second registration with same email should fail
        User user2 = new User();
        user2.setFullName("Jane Doe");
        user2.setEmail("unique@example.com");
        user2.setPasswordHash("validpass456");
        user2.setPhone("0987654321");

        try {
            authService.register(user2);
            fail("Should throw exception for duplicate email");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Email") || e.getMessage().contains("đã được đăng ký"));
        }
    }

    @Test
    public void register_duplicatePhone_throws() {
        // First registration
        User user1 = new User();
        user1.setFullName("John Doe");
        user1.setEmail("john1@example.com");
        user1.setPasswordHash("validpass123");
        user1.setPhone("0123456789");

        try {
            authService.register(user1);
        } catch (Exception e) {
            // First registration may fail due to DB
        }

        // Second registration with same phone should fail
        User user2 = new User();
        user2.setFullName("Jane Doe");
        user2.setEmail("jane1@example.com");
        user2.setPasswordHash("validpass456");
        user2.setPhone("0123456789");

        try {
            authService.register(user2);
            fail("Should throw exception for duplicate phone");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("điện thoại") || e.getMessage().contains("đã được đăng ký"));
        }
    }

    // ============= LOGIN EXCEPTION SCENARIOS =============

    @Test
    public void login_nullIdentifier_throws() {
        try {
            authService.login(null, "password123");
            fail("Should throw exception for null identifier");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void login_blankIdentifier_throws() {
        try {
            authService.login("", "password123");
            fail("Should throw exception for blank identifier");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Email") || e.getMessage().contains("điện thoại"));
        }
    }

    @Test
    public void login_whitespaceOnlyIdentifier_throws() {
        try {
            authService.login("   ", "password123");
            fail("Should throw exception for whitespace-only identifier");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Email") || e.getMessage().contains("điện thoại"));
        }
    }

    @Test
    public void login_nonexistentEmail_throws() {
        try {
            authService.login("nonexistent@example.com", "password123");
            fail("Should throw exception for non-existent user");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("không chính xác") || e.getMessage().contains("không tìm thấy"));
        }
    }

    @Test
    public void login_nonexistentPhone_throws() {
        try {
            authService.login("0999999999", "password123");
            fail("Should throw exception for non-existent phone");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("không chính xác") || e.getMessage().contains("không tìm thấy"));
        }
    }

    @Test
    public void login_nullPassword_throws() {
        try {
            authService.login("test@example.com", null);
            fail("Should throw exception for null password");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void login_blankPassword_throws() {
        try {
            authService.login("test@example.com", "");
            fail("Should throw exception for blank password");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void login_wrongPassword_throws() {
        // Register a user first
        User user = new User();
        user.setFullName("Test User");
        user.setEmail("correct@example.com");
        user.setPasswordHash("correctpass123");
        user.setPhone("0123456789");

        try {
            authService.register(user);

            // Try to login with wrong password
            authService.login("correct@example.com", "wrongpassword");
            fail("Should throw exception for wrong password");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("không chính xác") ||
                      e.getMessage().contains("mật khẩu"));
        }
    }

    // ============= LOCKED ACCOUNT SCENARIOS =============

    @Test
    public void login_lockedAccount_throws() {
        // This test assumes there's a mechanism to lock accounts after failed attempts
        // and that the service properly checks and reports locked status
        try {
            // Create a user with a locked account time in future
            User user = new User();
            user.setFullName("Locked User");
            user.setEmail("locked@example.com");
            user.setPasswordHash(HashUtil.hashPassword("password123"));
            user.setPhone("0199999999");
            user.setLockedUntil(LocalDateTime.now().plusHours(1));

            // Try to login - should fail with locked message
            authService.login("locked@example.com", "password123");
            fail("Should throw exception for locked account");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("khóa") || e.getMessage().contains("tạm thời"));
        }
    }

    // ============= PASSWORD CHANGE EXCEPTIONS =============

    @Test
    public void changePassword_negativeUserId_throws() {
        try {
            authService.changePassword(-1, "oldpass", "newpass123");
            fail("Should throw exception for negative user ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void changePassword_zeroUserId_throws() {
        try {
            authService.changePassword(0, "oldpass", "newpass123");
            fail("Should throw exception for zero user ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void changePassword_nullCurrentPassword_throws() {
        try {
            authService.changePassword(1, null, "newpass123");
            fail("Should throw exception for null current password");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void changePassword_blankCurrentPassword_throws() {
        try {
            authService.changePassword(1, "", "newpass123");
            fail("Should throw exception for blank current password");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void changePassword_nullNewPassword_throws() {
        try {
            authService.changePassword(1, "oldpass", null);
            fail("Should throw exception for null new password");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void changePassword_blankNewPassword_throws() {
        try {
            authService.changePassword(1, "oldpass", "");
            fail("Should throw exception for blank new password");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void changePassword_newPasswordTooShort_throws() {
        try {
            authService.changePassword(1, "oldpass123", "short");
            fail("Should throw exception for new password too short");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void changePassword_wrongCurrentPassword_throws() {
        try {
            authService.changePassword(1, "wrongoldpass", "newpass123");
            fail("Should throw exception for wrong current password");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("không chính xác") ||
                      e.getMessage().contains("mật khẩu"));
        }
    }

    // ============= VERIFICATION CODE EXCEPTIONS =============

    @Test
    public void sendForgotPasswordCode_nullEmail_throws() {
        try {
            authService.sendForgotPasswordCode(null);
            fail("Should throw exception for null email");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Email"));
        }
    }

    @Test
    public void sendForgotPasswordCode_blankEmail_throws() {
        try {
            authService.sendForgotPasswordCode("");
            fail("Should throw exception for blank email");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Email"));
        }
    }

    @Test
    public void sendForgotPasswordCode_invalidEmail_throws() {
        try {
            authService.sendForgotPasswordCode("not-an-email");
            fail("Should throw exception for invalid email");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Email"));
        }
    }

    @Test
    public void sendForgotPasswordCode_nonexistentEmail_throws() {
        try {
            authService.sendForgotPasswordCode("nonexistent@example.com");
            fail("Should throw exception for non-existent email");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("không tìm thấy") ||
                      e.getMessage().contains("không tồn tại"));
        }
    }

    @Test
    public void verifyEmailCode_nullEmail_throws() {
        try {
            authService.verifyEmailCode(null, "123456");
            fail("Should throw exception for null email");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Email"));
        }
    }

    @Test
    public void verifyEmailCode_blankEmail_throws() {
        try {
            authService.verifyEmailCode("", "123456");
            fail("Should throw exception for blank email");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Email"));
        }
    }

    @Test
    public void verifyEmailCode_nullCode_throws() {
        try {
            authService.verifyEmailCode("test@example.com", null);
            fail("Should throw exception for null code");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void verifyEmailCode_blankCode_throws() {
        try {
            authService.verifyEmailCode("test@example.com", "");
            fail("Should throw exception for blank code");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void verifyEmailCode_invalidCodeFormat_throws() {
        try {
            authService.verifyEmailCode("test@example.com", "not-a-code");
            fail("Should throw exception for invalid code format");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void verifyEmailCode_wrongCode_throws() {
        try {
            authService.verifyEmailCode("test@example.com", "000000");
            fail("Should throw exception for wrong code");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("không đúng") ||
                      e.getMessage().contains("không hợp lệ"));
        }
    }

    // ============= FORGOT PASSWORD CODE EXCEPTIONS =============

    @Test
    public void verifyForgotCode_nullEmail_throws() {
        try {
            authService.verifyForgotCode(null, "123456");
            fail("Should throw exception for null email");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Email"));
        }
    }

    @Test
    public void verifyForgotCode_blankEmail_throws() {
        try {
            authService.verifyForgotCode("", "123456");
            fail("Should throw exception for blank email");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Email"));
        }
    }

    @Test
    public void verifyForgotCode_nullCode_throws() {
        try {
            authService.verifyForgotCode("test@example.com", null);
            fail("Should throw exception for null code");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void verifyForgotCode_blankCode_throws() {
        try {
            authService.verifyForgotCode("test@example.com", "");
            fail("Should throw exception for blank code");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void verifyForgotCode_expiredCode_throws() {
        try {
            authService.verifyForgotCode("test@example.com", "000000");
            fail("Should throw exception for expired code");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("hết hạn") ||
                      e.getMessage().contains("không hợp lệ"));
        }
    }

    // ============= RESEND VERIFICATION CODE EXCEPTIONS =============

    @Test
    public void resendVerificationCode_nullEmail_throws() {
        try {
            authService.resendVerificationCode(null);
            fail("Should throw exception for null email");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Email"));
        }
    }

    @Test
    public void resendVerificationCode_blankEmail_throws() {
        try {
            authService.resendVerificationCode("");
            fail("Should throw exception for blank email");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Email"));
        }
    }

    @Test
    public void resendVerificationCode_invalidEmail_throws() {
        try {
            authService.resendVerificationCode("not-an-email");
            fail("Should throw exception for invalid email");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Email"));
        }
    }

    // ============= OAUTH LOGIN EXCEPTIONS =============

    @Test
    public void processGoogleLogin_nullEmail_throws() {
        try {
            authService.processGoogleLogin(null, "John Doe");
            fail("Should throw exception for null email");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Email"));
        }
    }

    @Test
    public void processGoogleLogin_blankEmail_throws() {
        try {
            authService.processGoogleLogin("", "John Doe");
            fail("Should throw exception for blank email");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Email"));
        }
    }

    @Test
    public void processGoogleLogin_nullFullName_throws() {
        try {
            authService.processGoogleLogin("test@example.com", null);
            fail("Should throw exception for null full name");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void processGoogleLogin_blankFullName_throws() {
        try {
            authService.processGoogleLogin("test@example.com", "");
            fail("Should throw exception for blank full name");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= RESET PASSWORD EXCEPTIONS =============

    @Test
    public void resetPassword_nullEmail_throws() {
        try {
            authService.resetPassword(null, "newpass123");
            fail("Should throw exception for null email");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Email"));
        }
    }

    @Test
    public void resetPassword_blankEmail_throws() {
        try {
            authService.resetPassword("", "newpass123");
            fail("Should throw exception for blank email");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Email"));
        }
    }

    @Test
    public void resetPassword_invalidEmail_throws() {
        try {
            authService.resetPassword("not-an-email", "newpass123");
            fail("Should throw exception for invalid email");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Email"));
        }
    }

    @Test
    public void resetPassword_nullPassword_throws() {
        try {
            authService.resetPassword("test@example.com", null);
            fail("Should throw exception for null password");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Mật khẩu"));
        }
    }

    @Test
    public void resetPassword_blankPassword_throws() {
        try {
            authService.resetPassword("test@example.com", "");
            fail("Should throw exception for blank password");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Mật khẩu"));
        }
    }

    @Test
    public void resetPassword_passwordTooShort_throws() {
        try {
            authService.resetPassword("test@example.com", "short");
            fail("Should throw exception for password too short");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Mật khẩu"));
        }
    }

    @Test
    public void resetPassword_nonexistentEmail_throws() {
        try {
            authService.resetPassword("nonexistent@example.com", "newpass123");
            fail("Should throw exception for non-existent email");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("không tìm thấy") ||
                      e.getMessage().contains("không tồn tại"));
        }
    }
}
