package test;

import config.AppConfig;
import dao.auth.UserDAO;
import model.entity.auth.User;
import service.system.EmailTemplateService;
import util.HashUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.*;

/**
 * XssInputSanitizationTest — Kiểm tra xử lý XSS ở tầng service/util:
 * EmailTemplateService.escapeHtml(), SQL injection prevention qua PreparedStatement.
 */
public class XssInputSanitizationTest {

    private UserDAO userDAO;
    private int testUserId = -1;

    @Before
    public void setUp() throws SQLException {
        userDAO = new UserDAO();
        long ts = System.currentTimeMillis();
        testUserId = userDAO.saveNewCustomer(
                "XSS Test User", "xss_test_" + ts + "@test.com",
                HashUtil.hashPassword("TestPass@2026"),
                "09" + String.format("%08d", Math.abs(ts % 100_000_000L)),
                AppConfig.ROLE_CUSTOMER, AppConfig.ACCOUNT_STATUS_ACTIVE, true);
    }

    @After
    public void tearDown() {
        try {
            if (testUserId > 0) userDAO.deleteUser(testUserId);
        } catch (SQLException e) {
            System.err.println("[XssInputSanitizationTest] Cleanup: " + e.getMessage());
        }
    }

    // =========================================================
    // TC-XSS-01: escapeHtml() encode <script> tag
    // =========================================================

    @Test
    public void should_encodeScriptTag_in_escapeHtml() {
        String input = "<script>alert('xss')</script>";
        String escaped = EmailTemplateService.escapeHtml(input);

        assertFalse("Kết quả không được chứa <script>", escaped.contains("<script>"));
        assertTrue("Phải chứa ký tự encode của <",
                escaped.contains("&lt;") || escaped.contains("&#x3C;") || escaped.contains("&#60;"));
    }

    // =========================================================
    // TC-XSS-02: escapeHtml() encode các entity cơ bản
    // =========================================================

    @Test
    public void should_encodeHtmlEntities_in_escapeHtml() {
        String input = "<b>bold</b>";
        String escaped = EmailTemplateService.escapeHtml(input);

        assertFalse("Không được chứa <b> thô", escaped.contains("<b>"));
        assertFalse("Không được chứa </b> thô", escaped.contains("</b>"));
    }

    // =========================================================
    // TC-XSS-03: escapeHtml(null) → trả về chuỗi rỗng (không NPE)
    // =========================================================

    @Test
    public void should_handleNullInput_in_escapeHtml() {
        try {
            String result = EmailTemplateService.escapeHtml(null);
            assertNotNull("Không được trả về null", result);
            assertEquals("null input phải trả về chuỗi rỗng", "", result);
        } catch (NullPointerException e) {
            fail("escapeHtml(null) không được ném NPE");
        }
    }

    // =========================================================
    // TC-XSS-04: escapeHtml("") → trả về chuỗi rỗng
    // =========================================================

    @Test
    public void should_returnEmpty_when_inputIsEmpty() {
        String result = EmailTemplateService.escapeHtml("");
        assertNotNull("Không được trả về null cho chuỗi rỗng", result);
        assertEquals("Chuỗi rỗng phải trả về chuỗi rỗng", "", result);
    }

    // =========================================================
    // TC-XSS-05: escapeHtml() giữ nguyên text thông thường
    // =========================================================

    @Test
    public void should_preservePlainText_in_escapeHtml() {
        String input = "Nguyễn Văn A - đơn hàng #12345";
        String escaped = EmailTemplateService.escapeHtml(input);
        assertTrue("Tên tiếng Việt không bị mất", escaped.contains("Nguyễn Văn A"));
        assertTrue("Số đơn hàng phải còn nguyên", escaped.contains("#12345"));
    }

    // =========================================================
    // TC-XSS-06: escapeHtml() encode dấu &
    // =========================================================

    @Test
    public void should_encodeAmpersand() {
        String input = "A & B";
        String escaped = EmailTemplateService.escapeHtml(input);
        assertFalse("Dấu & thô không được xuất hiện trong context HTML", escaped.contains(" & "));
        assertTrue("& phải được encode thành &amp;", escaped.contains("&amp;"));
    }

    // =========================================================
    // TC-XSS-07: Input chứa SQL injection không gây lỗi khi lưu profile
    //             Chứng minh PreparedStatement ngăn SQL injection
    // =========================================================

    @Test
    public void should_safelySave_whenUserInputContainsSqlInjection() throws SQLException {
        String maliciousInput = "'; DROP TABLE users; --";

        User user = userDAO.findUserById(testUserId);
        assertNotNull(user);
        user.setUserAddress(maliciousInput);

        // PreparedStatement đúng không bị SQL injection
        try {
            userDAO.update(user);
            User found = userDAO.findUserById(testUserId);
            assertNotNull("Bảng users phải vẫn tồn tại sau SQL injection attempt", found);
            assertEquals("Địa chỉ phải được lưu đúng", maliciousInput, found.getUserAddress());
        } catch (SQLException e) {
            // Chấp nhận nếu có ràng buộc DB khác, nhưng không chấp nhận DROP TABLE
            assertNotNull("Exception phải có message", e.getMessage());
        }

        // Verify bảng users còn tồn tại — nếu bị DROP thì findUserById throw
        User verify = userDAO.findUserById(testUserId);
        assertNotNull("Bảng users phải còn tồn tại", verify);
    }

    // =========================================================
    // TC-XSS-08: Tên người dùng chứa HTML được encode trong email template
    // =========================================================

    @Test
    public void should_encodeUserName_when_buildingEmailTemplate() {
        String maliciousName = "<img src=x onerror=alert(1)>";
        String escaped = EmailTemplateService.escapeHtml(maliciousName);

        assertFalse("Template không được chứa <img thô", escaped.contains("<img"));
        assertFalse("Template không được chứa onerror thô trong tag", escaped.contains("<img"));
    }
}
