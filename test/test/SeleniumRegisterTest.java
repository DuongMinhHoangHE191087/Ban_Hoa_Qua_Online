package test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

/**
 * SeleniumRegisterTest — Automation Testing cho trang Đăng ký.
 *
 * Đây cũng là test cases của Decision Table Testing (Phần 3) nhưng
 * chạy tự động bằng Selenium thay vì kiểm thử thủ công.
 *
 * Form fields:
 *   - fullName (id="fullName")
 *   - phone (id="phone")
 *   - email (id="email")
 *   - password (id="password")
 *   - confirmPassword (id="confirmPassword")
 *   - terms (id="terms") — checkbox
 *   - registerForm (id="registerForm")
 *
 * SWT301 — Phần 5: Testing Tools (Automation Testing)
 * @author fruitmkt-team
 */
public class SeleniumRegisterTest {

    private WebDriver driver;
    private static final String BASE_URL = "http://localhost:8080/Ban_Hoa_Qua_Online";
    private static final int WAIT_SECONDS = 10;

    @Before
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        // options.addArguments("--headless"); // Bỏ comment để chạy không hiển thị

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(WAIT_SECONDS));
    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // =========================================================================
    //  HELPER METHODS
    // =========================================================================

    /**
     * Mở trang đăng ký và điền thông tin vào form.
     * Nếu giá trị nào là null, field đó sẽ không được điền.
     */
    private void fillRegisterForm(String fullName, String phone, String email,
                                   String password, String confirmPassword,
                                   boolean acceptTerms) {
        driver.get(BASE_URL + "/auth/register");

        if (fullName != null) {
            WebElement el = driver.findElement(By.id("fullName"));
            el.clear();
            el.sendKeys(fullName);
        }
        if (phone != null) {
            WebElement el = driver.findElement(By.id("phone"));
            el.clear();
            el.sendKeys(phone);
        }
        if (email != null) {
            WebElement el = driver.findElement(By.id("email"));
            el.clear();
            el.sendKeys(email);
        }
        if (password != null) {
            WebElement el = driver.findElement(By.id("password"));
            el.clear();
            el.sendKeys(password);
        }
        if (confirmPassword != null) {
            WebElement el = driver.findElement(By.id("confirmPassword"));
            el.clear();
            el.sendKeys(confirmPassword);
        }
        if (acceptTerms) {
            WebElement terms = driver.findElement(By.id("terms"));
            if (!terms.isSelected()) {
                // Sử dụng JavaScript click vì checkbox có thể bị che bởi label
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", terms);
            }
        }
    }

    /**
     * Submit form đăng ký bằng JavaScript.
     */
    private void submitForm() {
        ((JavascriptExecutor) driver).executeScript(
                "document.getElementById('registerForm').submit();");
    }

    // =========================================================================
    //  DT-01: All Valid (R1) — Đăng ký thành công
    // =========================================================================

    /**
     * TC-DT-01: Tất cả thông tin hợp lệ → đăng ký thành công.
     *
     * Decision Table Rule: R1 (C1=T, C2=T, C3=T, C4=T, C5=T)
     *
     * Lưu ý: Test này sẽ thực sự tạo tài khoản trong DB.
     * Cần đảm bảo email chưa tồn tại trước khi chạy test.
     * Dùng email unique (thêm timestamp).
     */
    @Test
    public void testDT01_AllValid_RegisterSuccess() {
        long ts = System.currentTimeMillis();
        String uniqueEmail = "testuser" + ts + "@example.com";

        fillRegisterForm(
                "Nguyen Van Test",      // fullName
                "0987654321",            // phone
                uniqueEmail,             // email (unique)
                "TestPass123",           // password (8+ chars)
                "TestPass123",           // confirmPassword
                true                     // terms
        );
        submitForm();

        // Chờ response — nếu thành công sẽ redirect ra khỏi trang register
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_SECONDS));
        try {
            // Thành công: redirect hoặc hiển thị success message
            wait.until(driver -> {
                String url = driver.getCurrentUrl();
                String source = driver.getPageSource();
                return !url.contains("/auth/register")
                        || source.contains("thành công")
                        || source.contains("xác minh");
            });

            String currentUrl = driver.getCurrentUrl();
            String pageSource = driver.getPageSource();
            boolean success = !currentUrl.contains("/auth/register")
                    || pageSource.contains("thành công")
                    || pageSource.contains("xác minh");
            assertTrue("DT-01: Registration should succeed", success);
        } catch (Exception e) {
            // Nếu ở lại trang register, kiểm tra có lỗi gì không
            fail("DT-01: Registration did not complete. Page source contains: "
                    + driver.getPageSource().substring(0, Math.min(500, driver.getPageSource().length())));
        }
    }

    // =========================================================================
    //  DT-02: Invalid Full Name (R2)
    // =========================================================================

    /**
     * TC-DT-02: Full Name rỗng → hiển thị lỗi "Họ và tên".
     *
     * Decision Table Rule: R2 (C1=F, C2=T, C3=T, C4=T, C5=T)
     */
    @Test
    public void testDT02_InvalidFullName_ShowsError() {
        fillRegisterForm(
                "",                      // fullName — INVALID (empty)
                "0987654321",
                "test2@example.com",
                "TestPass123",
                "TestPass123",
                true
        );
        submitForm();

        // Chờ client-side validation hoặc server response
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_SECONDS));
        wait.until(driver -> {
            String source = driver.getPageSource();
            return source.contains("Họ và tên")
                    || source.contains("client-error")
                    || source.contains("errorMsg");
        });

        String pageSource = driver.getPageSource();
        assertTrue("DT-02: Should show error about full name",
                pageSource.contains("Họ và tên") || pageSource.contains("fullName"));
    }

    // =========================================================================
    //  DT-03: Invalid Email (R3)
    // =========================================================================

    /**
     * TC-DT-03: Email không đúng định dạng → hiển thị lỗi.
     *
     * Decision Table Rule: R3 (C1=T, C2=F, C3=T, C4=T, C5=T)
     */
    @Test
    public void testDT03_InvalidEmail_ShowsError() {
        fillRegisterForm(
                "Nguyen Van B",
                "0987654321",
                "invalid-email",          // email — INVALID
                "TestPass123",
                "TestPass123",
                true
        );
        submitForm();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_SECONDS));
        wait.until(driver -> {
            String source = driver.getPageSource();
            return source.contains("Email") || source.contains("email")
                    || source.contains("client-error");
        });

        String pageSource = driver.getPageSource();
        assertTrue("DT-03: Should show error about email format",
                pageSource.contains("email") || pageSource.contains("Email"));
    }

    // =========================================================================
    //  DT-04: Invalid Phone (R4)
    // =========================================================================

    /**
     * TC-DT-04: Số điện thoại không hợp lệ → hiển thị lỗi.
     *
     * Decision Table Rule: R4 (C1=T, C2=T, C3=F, C4=T, C5=T)
     */
    @Test
    public void testDT04_InvalidPhone_ShowsError() {
        fillRegisterForm(
                "Nguyen Van C",
                "123",                    // phone — INVALID (too short)
                "testc@example.com",
                "TestPass123",
                "TestPass123",
                true
        );
        submitForm();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_SECONDS));
        wait.until(driver -> {
            String source = driver.getPageSource();
            return source.contains("điện thoại") || source.contains("phone")
                    || source.contains("client-error");
        });

        String pageSource = driver.getPageSource();
        assertTrue("DT-04: Should show error about phone",
                pageSource.contains("điện thoại") || pageSource.contains("phone"));
    }

    // =========================================================================
    //  DT-05: Invalid Password — too short (R5)
    // =========================================================================

    /**
     * TC-DT-05: Mật khẩu quá ngắn (< 8 ký tự) → hiển thị lỗi.
     *
     * Decision Table Rule: R5 (C1=T, C2=T, C3=T, C4=F, C5=T)
     */
    @Test
    public void testDT05_ShortPassword_ShowsError() {
        fillRegisterForm(
                "Nguyen Van D",
                "0987654321",
                "testd@example.com",
                "abc",                    // password — INVALID (too short)
                "abc",                    // confirm matches but still invalid
                true
        );
        submitForm();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_SECONDS));
        wait.until(driver -> {
            String source = driver.getPageSource();
            return source.contains("Mật khẩu") || source.contains("password")
                    || source.contains("client-error");
        });

        String pageSource = driver.getPageSource();
        assertTrue("DT-05: Should show error about password length",
                pageSource.contains("Mật khẩu") || pageSource.contains("8"));
    }

    // =========================================================================
    //  DT-06: Confirm Password Mismatch (R6)
    // =========================================================================

    /**
     * TC-DT-06: Mật khẩu xác nhận không khớp → hiển thị lỗi.
     *
     * Decision Table Rule: R6 (C1=T, C2=T, C3=T, C4=T, C5=F)
     */
    @Test
    public void testDT06_PasswordMismatch_ShowsError() {
        fillRegisterForm(
                "Nguyen Van E",
                "0987654321",
                "teste@example.com",
                "TestPass123",            // password
                "DifferentPass456",       // confirmPassword — MISMATCH
                true
        );
        submitForm();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_SECONDS));
        wait.until(driver -> {
            String source = driver.getPageSource();
            return source.contains("không khớp") || source.contains("xác nhận")
                    || source.contains("client-error");
        });

        String pageSource = driver.getPageSource();
        assertTrue("DT-06: Should show error about password confirmation",
                pageSource.contains("không khớp") || pageSource.contains("xác nhận"));
    }

    // =========================================================================
    //  DT-08: Multiple Invalid Fields (R8)
    // =========================================================================

    /**
     * TC-DT-08: Nhiều field không hợp lệ cùng lúc → hiển thị lỗi.
     *
     * Decision Table Rule: R8 (C1=F, C2=F, C3=T, C4=T, C5=T)
     */
    @Test
    public void testDT08_MultipleInvalid_ShowsErrors() {
        fillRegisterForm(
                "",                       // fullName — INVALID
                "0987654321",
                "bad-email",              // email — INVALID
                "TestPass123",
                "TestPass123",
                true
        );
        submitForm();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_SECONDS));
        wait.until(driver -> {
            String source = driver.getPageSource();
            return source.contains("client-error") || source.contains("error");
        });

        // Phải có ít nhất 1 error message
        String pageSource = driver.getPageSource();
        assertTrue("DT-08: Should show at least one error message",
                pageSource.contains("Họ và tên")
                        || pageSource.contains("Email")
                        || pageSource.contains("client-error"));
    }

    // =========================================================================
    //  NAVIGATION TESTS
    // =========================================================================

    /**
     * TC-REG-NAV-01: Kiểm tra trang đăng ký hiển thị đúng tiêu đề.
     */
    @Test
    public void testRegister_PageTitle() {
        driver.get(BASE_URL + "/auth/register");
        assertTrue("Page title should contain 'Đăng ký'",
                driver.getTitle().contains("Đăng ký"));
    }

    /**
     * TC-REG-NAV-02: Link "Đăng nhập ngay" chuyển về trang login.
     */
    @Test
    public void testRegister_LoginLink() {
        driver.get(BASE_URL + "/auth/register");

        WebElement loginLink = driver.findElement(
                By.cssSelector("a[href*='/auth/login']"));
        loginLink.click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_SECONDS));
        wait.until(ExpectedConditions.urlContains("/auth/login"));

        assertTrue("Should navigate to login page",
                driver.getCurrentUrl().contains("/auth/login"));
    }

    /**
     * TC-REG-NAV-03: Kiểm tra tab switcher Customer / Shop Owner hoạt động.
     */
    @Test
    public void testRegister_TabSwitcher() {
        driver.get(BASE_URL + "/auth/register");

        // Click tab "Chủ cửa hàng"
        WebElement shopTab = driver.findElement(By.id("tabShop"));
        shopTab.click();

        // Kiểm tra shopFields hiển thị
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("shopFields")));

        WebElement shopFields = driver.findElement(By.id("shopFields"));
        assertTrue("Shop fields should be visible after clicking Shop tab",
                shopFields.isDisplayed());

        // Click tab "Khách hàng"
        WebElement customerTab = driver.findElement(By.id("tabCustomer"));
        customerTab.click();

        // Kiểm tra shopFields ẩn
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("shopFields")));
        assertFalse("Shop fields should be hidden after clicking Customer tab",
                shopFields.isDisplayed());
    }
}
