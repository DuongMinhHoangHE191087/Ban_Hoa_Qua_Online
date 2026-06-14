package test;

import service.auth.AuthService;
import service.auth.AuthService.VerificationRequiredException;
import model.entity.auth.User;
import config.AppConfig;
import util.HashUtil;
import org.junit.Test;
import org.junit.Before;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

public class AuthServiceTest {

    private AuthService authService;

    @Before
    public void setUp() {
        authService = new AuthService();
    }

    @Test(expected = Exception.class)
    public void login_blankIdentifier_throws() throws Exception {
        authService.login("", "password123");
    }

    @Test(expected = Exception.class)
    public void login_invalidEmail_throws() throws Exception {
        authService.login("invalid-email", "password123");
    }

    @Test
    public void register_invalidFullName_throws() {
        User user = new User();
        user.setFullName("");
        user.setEmail("test@example.com");
        user.setPasswordHash("password123");
        user.setPhone("0123456789");

        try {
            authService.register(user);
            fail("Should throw exception for blank fullName");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Họ và tên"));
        }
    }

    @Test
    public void register_invalidEmail_throws() {
        User user = new User();
        user.setFullName("John Doe");
        user.setEmail("not-an-email");
        user.setPasswordHash("password123");
        user.setPhone("0123456789");

        try {
            authService.register(user);
            fail("Should throw exception for invalid email");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Email"));
        }
    }

    @Test
    public void register_invalidPassword_throws() {
        User user = new User();
        user.setFullName("John Doe");
        user.setEmail("john@example.com");
        user.setPasswordHash("short");
        user.setPhone("0123456789");

        try {
            authService.register(user);
            fail("Should throw exception for short password");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Mật khẩu"));
        }
    }

    @Test
    public void register_invalidPhone_throws() {
        User user = new User();
        user.setFullName("John Doe");
        user.setEmail("john@example.com");
        user.setPasswordHash("validpass123");
        user.setPhone("invalid");

        try {
            authService.register(user);
            fail("Should throw exception for invalid phone");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("điện thoại") || e.getMessage().contains("email"));
        }
    }

    @Test(expected = Exception.class)
    public void changePassword_blankCurrentPassword_throws() throws Exception {
        authService.changePassword(1, "", "newpass123");
    }

    @Test(expected = Exception.class)
    public void changePassword_invalidNewPassword_throws() throws Exception {
        authService.changePassword(1, "validpass123", "short");
    }

    @Test(expected = Exception.class)
    public void sendForgotPasswordCode_invalidEmail_throws() throws Exception {
        authService.sendForgotPasswordCode("not-an-email");
    }

    @Test(expected = Exception.class)
    public void verifyEmailCode_blankEmail_throws() throws Exception {
        authService.verifyEmailCode("", "123456");
    }

    @Test(expected = Exception.class)
    public void verifyEmailCode_blankCode_throws() throws Exception {
        authService.verifyEmailCode("test@example.com", "");
    }

    @Test(expected = Exception.class)
    public void verifyForgotCode_invalidEmail_throws() throws Exception {
        authService.verifyForgotCode("not-an-email", "123456");
    }

    @Test(expected = Exception.class)
    public void verifyForgotCode_blankCode_throws() throws Exception {
        authService.verifyForgotCode("test@example.com", "");
    }

    @Test(expected = Exception.class)
    public void resendVerificationCode_invalidEmail_throws() throws Exception {
        authService.resendVerificationCode("not-an-email");
    }

    @Test(expected = Exception.class)
    public void processGoogleLogin_blankEmail_throws() throws Exception {
        authService.processGoogleLogin("", "John Doe");
    }

    @Test(expected = Exception.class)
    public void processGoogleLogin_blankFullName_throws() throws Exception {
        authService.processGoogleLogin("test@example.com", "");
    }

    @Test(expected = Exception.class)
    public void resetPassword_invalidEmail_throws() throws Exception {
        authService.resetPassword("not-an-email", "newpass123");
    }

    @Test(expected = Exception.class)
    public void resetPassword_invalidPassword_throws() throws Exception {
        authService.resetPassword("test@example.com", "short");
    }
}
