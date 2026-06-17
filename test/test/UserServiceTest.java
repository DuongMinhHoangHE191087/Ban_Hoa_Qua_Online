package test;

import service.auth.UserService;
import model.entity.auth.User;
import org.junit.Test;
import org.junit.Before;

import java.sql.SQLException;

import static org.junit.Assert.*;

public class UserServiceTest {

    private UserService userService;

    @Before
    public void setUp() {
        userService = new UserService();
    }

    @Test(expected = IllegalArgumentException.class)
    public void findById_negativeId_throws() throws SQLException {
        userService.findById(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findById_zeroId_throws() throws SQLException {
        userService.findById(0);
    }

    @Test
    public void getAllUsers_succeeds() throws SQLException {
        assertNotNull(userService.getAllUsers());
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateUser_nullUser_throws() throws SQLException {
        userService.updateUser(null);
    }

    @Test
    public void searchUsers_succeeds() throws SQLException {
        assertNotNull(userService.searchUsers("CUSTOMER", "test", 0, 20));
    }

    @Test
    public void countUsers_succeeds() throws SQLException {
        assertTrue(userService.countUsers("CUSTOMER", "test") >= 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateUserStatus_negativeId_throws() throws SQLException {
        userService.updateUserStatus(-1, "ACTIVE");
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateUserStatus_nullStatus_throws() throws SQLException {
        userService.updateUserStatus(1, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void isPhoneTakenByAnother_nullPhone_throws() throws SQLException {
        userService.isPhoneTakenByAnother(null, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void isPhoneTakenByAnother_negativeUserId_throws() throws SQLException {
        userService.isPhoneTakenByAnother("0123456789", -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteSessionsByUserId_negativeId_throws() throws SQLException {
        userService.deleteSessionsByUserId(-1);
    }

    @Test(expected = Exception.class)
    public void saveOrUpdateCheckoutContactInfo_nullUser_throws() throws SQLException {
        userService.saveOrUpdateCheckoutContactInfo(null, "Recipient", "0123456789", "Address", false);
    }

    @Test(expected = Exception.class)
    public void saveOrUpdateCheckoutContactInfo_blankRecipientName_throws() throws SQLException {
        User user = new User();
        user.setUserId(1);
        userService.saveOrUpdateCheckoutContactInfo(user, "", "0123456789", "Address", false);
    }

    @Test(expected = Exception.class)
    public void saveOrUpdateCheckoutContactInfo_blankPhone_throws() throws SQLException {
        User user = new User();
        user.setUserId(1);
        userService.saveOrUpdateCheckoutContactInfo(user, "Recipient", "", "Address", false);
    }

    @Test(expected = Exception.class)
    public void saveOrUpdateCheckoutContactInfo_blankAddress_throws() throws SQLException {
        User user = new User();
        user.setUserId(1);
        userService.saveOrUpdateCheckoutContactInfo(user, "Recipient", "0123456789", "", false);
    }
}
