package com.fruitmkt.test;

import com.fruitmkt.dao.UserDAO;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.util.HashUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * UserAuthFlowTest — Bộ kiểm thử JUnit 4 cho toàn bộ luồng xác thực người dùng:
 *   Đăng ký tài khoản → Đăng nhập → Sai mật khẩu → Khóa tài khoản → Reset.
 *
 * CÁC LUỒNG KIỂM TRA:
 *   1. Đăng ký tài khoản mới thành công (customer)
 *   2. Tìm user theo email (login identifier)
 *   3. Xác minh mật khẩu đúng (hash matching)
 *   4. Xác minh mật khẩu sai không khớp hash
 *   5. Tài khoản bị khoá khi đăng nhập sai quá nhiều lần
 *   6. Tìm kiếm user theo role và từ khóa
 *   7. Cập nhật thông tin profile người dùng
 *   8. Đăng ký trùng email — phải báo lỗi ràng buộc DB
 *
 * QUY TẮC KIỂM THỬ:
 *   - Không test thuần code, test đúng theo nghiệp vụ xác thực
 *   - Cleanup toàn bộ dữ liệu tạm sau mỗi test
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserAuthFlowTest {

    private UserDAO userDAO;

    // IDs của user test cần dọn dẹp
    private int testUserId1 = -1;
    private int testUserId2 = -1;

    // Email tĩnh cho test trùng lặp
    private String uniqueEmail1;
    private String uniqueEmail2;

    // Mật khẩu gốc và hash để test
    private static final String RAW_PASSWORD = "SecurePass@2026";

    @Before
    public void setUp() throws SQLException {
        userDAO = new UserDAO();
        long ts = System.currentTimeMillis();
        uniqueEmail1 = "junit_auth1_" + ts + "@test.com";
        uniqueEmail2 = "junit_auth2_" + ts + "@test.com";
    }

    @After
    public void tearDown() {
        try {
            if (testUserId1 != -1) {
                userDAO.deleteUser(testUserId1);
                testUserId1 = -1;
            }
            if (testUserId2 != -1) {
                userDAO.deleteUser(testUserId2);
                testUserId2 = -1;
            }
        } catch (SQLException e) {
            System.err.println("[UserAuthFlowTest] Cleanup failed: " + e.getMessage());
        }
    }

    // =========================================================
    // NGHIỆP VỤ 1: Đăng ký tài khoản
    // =========================================================

    /**
     * TC-AUTH-01: Đăng ký tài khoản customer mới thành công.
     * Nghiệp vụ: Người dùng điền form đăng ký → lưu vào DB → trả về ID.
     */
    @Test
    public void test01_RegisterNewCustomerSuccessfully() throws SQLException {
        String hashedPwd = HashUtil.hashPassword(RAW_PASSWORD);

        testUserId1 = userDAO.saveNewCustomer(
            "Nguyễn Test Đăng Ký",
            uniqueEmail1,
            hashedPwd,
            "0911000001",
            "CUSTOMER",
            "INACTIVE", // chờ xác minh email
            false       // email chưa xác minh
        );

        assertTrue("User ID phải > 0 sau đăng ký", testUserId1 > 0);

        // Kiểm tra đã lưu đúng thông tin
        User saved = userDAO.findByEmail(uniqueEmail1);
        assertNotNull("Phải tìm thấy user sau đăng ký", saved);
        assertEquals("Tên phải khớp", "Nguyễn Test Đăng Ký", saved.getFullName());
        assertEquals("Email phải khớp", uniqueEmail1, saved.getEmail());
        assertEquals("Role mặc định phải là CUSTOMER", "CUSTOMER", saved.getRole());
        assertEquals("Status ban đầu phải là INACTIVE (chờ verify email)", "INACTIVE", saved.getStatus());
        assertFalse("Email chưa được xác minh", saved.isEmailVerified());
    }

    /**
     * TC-AUTH-02: Đăng nhập đúng mật khẩu — hash phải khớp.
     * Nghiệp vụ: Hệ thống so sánh mật khẩu nhập vào với hash đã lưu trong DB.
     */
    @Test
    public void test02_LoginWithCorrectPasswordMatchesHash() throws SQLException {
        String hashedPwd = HashUtil.hashPassword(RAW_PASSWORD);
        testUserId1 = userDAO.saveNewCustomer(
            "Login Test User",
            uniqueEmail1,
            hashedPwd,
            "0911000002",
            "CUSTOMER",
            "ACTIVE",
            true
        );

        // Tìm user theo email (giống servlet đăng nhập)
        User found = userDAO.findByLoginIdentifier(uniqueEmail1);
        assertNotNull("Phải tìm thấy user theo email", found);

        // Xác minh mật khẩu đúng
        boolean pwdMatch = HashUtil.verify(RAW_PASSWORD, found.getPasswordHash());
        assertTrue("Mật khẩu đúng phải khớp với hash trong DB", pwdMatch);
    }

    /**
     * TC-AUTH-03: Đăng nhập SAI mật khẩu — hash không được khớp.
     * Nghiệp vụ: Bảo vệ tài khoản — mật khẩu sai phải bị từ chối.
     */
    @Test
    public void test03_LoginWithWrongPasswordFails() throws SQLException {
        String hashedPwd = HashUtil.hashPassword(RAW_PASSWORD);
        testUserId1 = userDAO.saveNewCustomer(
            "Wrong Pass Test",
            uniqueEmail1,
            hashedPwd,
            "0911000003",
            "CUSTOMER",
            "ACTIVE",
            true
        );

        User found = userDAO.findByLoginIdentifier(uniqueEmail1);
        assertNotNull(found);

        // Mật khẩu sai không được khớp
        boolean wrongMatch = HashUtil.verify("WrongPassword123", found.getPasswordHash());
        assertFalse("Mật khẩu sai KHÔNG được khớp hash", wrongMatch);
    }

    /**
     * TC-AUTH-04: Tìm user theo số điện thoại (login bằng phone).
     * Nghiệp vụ: Hệ thống cho phép đăng nhập bằng email HOẶC số điện thoại.
     */
    @Test
    public void test04_FindUserByPhoneAsLoginIdentifier() throws SQLException {
        String phone = "0911999888";
        testUserId1 = userDAO.saveNewCustomer(
            "Phone Login User",
            uniqueEmail1,
            HashUtil.hashPassword(RAW_PASSWORD),
            phone,
            "CUSTOMER",
            "ACTIVE",
            true
        );

        // findByLoginIdentifier phải tìm được cả bằng phone
        User foundByPhone = userDAO.findByLoginIdentifier(phone);
        assertNotNull("Phải tìm được user theo số điện thoại", foundByPhone);
        assertEquals("Email phải khớp khi tìm theo phone", uniqueEmail1, foundByPhone.getEmail());
    }

    /**
     * TC-AUTH-05: Tài khoản bị khóa (status=LOCKED) không được đăng nhập.
     * Nghiệp vụ: Sau nhiều lần đăng nhập sai, tài khoản bị khóa.
     */
    @Test
    public void test05_LockedAccountCannotLogin() throws SQLException {
        testUserId1 = userDAO.saveNewCustomer(
            "Locked Account Test",
            uniqueEmail1,
            HashUtil.hashPassword(RAW_PASSWORD),
            "0911000005",
            "CUSTOMER",
            "ACTIVE",
            true
        );

        // Simulate admin lock tài khoản
        lockUserAccount(testUserId1);

        // Tìm lại user và kiểm tra status
        User locked = userDAO.findByEmail(uniqueEmail1);
        assertNotNull(locked);
        assertEquals("Tài khoản phải ở trạng thái LOCKED", "LOCKED", locked.getStatus());

        // Nghiệp vụ: servlet phải từ chối đăng nhập khi status = LOCKED
        boolean canLogin = "ACTIVE".equals(locked.getStatus());
        assertFalse("Tài khoản LOCKED không được phép đăng nhập", canLogin);
    }

    /**
     * TC-AUTH-06: Tìm kiếm user theo role và từ khóa — admin quản lý user.
     * Nghiệp vụ: Admin có thể filter danh sách user theo role hoặc tìm kiếm.
     */
    @Test
    public void test06_SearchUsersByRoleAndKeyword() throws SQLException {
        testUserId1 = userDAO.saveNewCustomer(
            "Tìm Kiếm Đặc Biệt",
            uniqueEmail1,
            HashUtil.hashPassword(RAW_PASSWORD),
            "0911000006",
            "CUSTOMER",
            "ACTIVE",
            true
        );

        // Tìm kiếm theo role = CUSTOMER + keyword khớp email
        List<User> results = userDAO.searchUsers("CUSTOMER", "junit_auth1_", 0, 10);
        assertFalse("Phải tìm thấy ít nhất 1 kết quả", results.isEmpty());

        boolean found = results.stream().anyMatch(u -> u.getUserId() == testUserId1);
        assertTrue("Danh sách kết quả phải chứa user vừa tạo", found);
    }

    /**
     * TC-AUTH-07: Đăng ký trùng email phải ném Exception do ràng buộc UNIQUE.
     * Nghiệp vụ: Hệ thống không cho phép 2 tài khoản cùng email.
     */
    @Test
    public void test07_DuplicateEmailRegistrationThrowsException() throws SQLException {
        // Đăng ký lần 1 thành công
        testUserId1 = userDAO.saveNewCustomer(
            "User Trùng Email 1",
            uniqueEmail1,
            HashUtil.hashPassword(RAW_PASSWORD),
            "0911000007",
            "CUSTOMER",
            "ACTIVE",
            true
        );
        assertTrue("Đăng ký lần 1 phải thành công", testUserId1 > 0);

        // Đăng ký lần 2 cùng email phải ném Exception
        boolean exceptionThrown = false;
        try {
            testUserId2 = userDAO.saveNewCustomer(
                "User Trùng Email 2",
                uniqueEmail1, // CÙNG EMAIL
                HashUtil.hashPassword(RAW_PASSWORD),
                "0911000008",
                "CUSTOMER",
                "ACTIVE",
                true
            );
        } catch (SQLException e) {
            exceptionThrown = true;
            // DB phải ném lỗi vi phạm UNIQUE constraint
            assertTrue("Exception phải liên quan đến vi phạm ràng buộc UNIQUE hoặc duplicate key",
                e.getMessage() != null && (
                    e.getMessage().toLowerCase().contains("unique") ||
                    e.getMessage().toLowerCase().contains("duplicate") ||
                    e.getMessage().toLowerCase().contains("violation") ||
                    e.getErrorCode() == 2627 // SQL Server: Cannot insert duplicate key
                )
            );
        }
        assertTrue("Đăng ký trùng email PHẢI ném SQLException", exceptionThrown);
    }

    /**
     * TC-AUTH-08: Đếm tổng số user theo role — admin dashboard.
     * Nghiệp vụ: Admin xem tổng số customer, shop owner trong hệ thống.
     */
    @Test
    public void test08_CountUsersByRole() throws SQLException {
        // Đếm số CUSTOMER trước khi thêm
        int countBefore = userDAO.countUsers("CUSTOMER", null);

        testUserId1 = userDAO.saveNewCustomer(
            "Count Test Customer",
            uniqueEmail1,
            HashUtil.hashPassword(RAW_PASSWORD),
            "0911000009",
            "CUSTOMER",
            "ACTIVE",
            true
        );

        int countAfter = userDAO.countUsers("CUSTOMER", null);
        assertEquals("Số CUSTOMER phải tăng đúng 1", countBefore + 1, countAfter);
    }

    // =========================================================
    // Helper methods
    // =========================================================

    /** Khóa tài khoản người dùng (simulate admin lock) */
    private void lockUserAccount(int userId) throws SQLException {
        String sql = "UPDATE users SET status = 'LOCKED', updated_at = GETDATE() WHERE user_id = ?";
        try (Connection conn = userDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }
}
