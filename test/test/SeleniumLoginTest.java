package test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

/**
 * SeleniumLoginTest — Automation Testing cho trang Đăng nhập.
 *
 * Sử dụng Selenium WebDriver + ChromeDriver + JUnit 4.
 * Cần cài đặt:
 *   - selenium-java-4.x.jar trong classpath
 *   - ChromeDriver tương thích phiên bản Chrome trên máy
 *
 * SWT301 — Phần 5: Testing Tools (Automation Testing)
 * @author fruitmkt-team
 */
public class SeleniumLoginTest {

    private WebDriver driver;
    private static final String BASE_URL = "http://localhost:8080/Ban_Hoa_Qua_Online";
    private static final int WAIT_SECONDS = 10;

    @Before
    public void setUp() {
        // Cấu hình ChromeOptions
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        // Bỏ comment dòng dưới nếu muốn chạy headless (không hiển thị trình duyệt)
        // options.addArguments("--headless");

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
    //  TEST CASE 1: Đăng nhập thành công
    // =========================================================================

    /**
     * TC-LOGIN-01: Đăng nhập thành công với email và mật khẩu hợp lệ.
     *
     * Pre-condition: Tài khoản test tồn tại trong DB, status ACTIVE.
     * Steps:
     *   1. Truy cập trang login
     *   2. Nhập email hợp lệ
     *   3. Nhập mật khẩu đúng
     *   4. Nhấn nút "Đăng nhập"
     * Expected: Redirect sang trang Home hoặc Dashboard
     */
    @Test
    public void testLogin_Success() {
        // 1. Mở trang đăng nhập
        driver.get(BASE_URL + "/auth/login");

        // 2. Nhập thông tin đăng nhập
        WebElement identifierField = driver.findElement(By.id("identifier"));
        identifierField.clear();
        identifierField.sendKeys("customer@metafruit.com"); // Thay bằng email test có sẵn trong DB

        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.clear();
        passwordField.sendKeys("12345678"); // Thay bằng mật khẩu test tương ứng

        // 3. Nhấn nút đăng nhập
        WebElement loginForm = driver.findElement(By.id("loginForm"));
        loginForm.submit();

        // 4. Chờ redirect và kiểm tra URL
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_SECONDS));
        wait.until(ExpectedConditions.not(
                ExpectedConditions.urlContains("/auth/login")));

        // 5. Assert: URL không còn ở trang login
        String currentUrl = driver.getCurrentUrl();
        assertFalse("Should redirect away from login page after success",
                currentUrl.contains("/auth/login"));
    }

    // =========================================================================
    //  TEST CASE 2: Đăng nhập thất bại — sai mật khẩu
    // =========================================================================

    /**
     * TC-LOGIN-02: Đăng nhập thất bại khi nhập sai mật khẩu.
     *
     * Steps:
     *   1. Truy cập trang login
     *   2. Nhập email hợp lệ
     *   3. Nhập mật khẩu sai
     *   4. Nhấn nút "Đăng nhập"
     * Expected: Hiển thị thông báo lỗi, vẫn ở trang login
     */
    @Test
    public void testLogin_WrongPassword_ShowsError() {
        driver.get(BASE_URL + "/auth/login");

        // Nhập email hợp lệ nhưng mật khẩu sai
        driver.findElement(By.id("identifier")).sendKeys("customer@metafruit.com");
        driver.findElement(By.id("password")).sendKeys("wrongpassword123");
        driver.findElement(By.id("loginForm")).submit();

        // Chờ trang load lại
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_SECONDS));
        wait.until(ExpectedConditions.urlContains("/auth/login"));

        // Assert: vẫn ở trang login
        assertTrue("Should stay on login page after failed login",
                driver.getCurrentUrl().contains("/auth/login"));

        // Assert: có hiển thị thông báo lỗi
        String pageSource = driver.getPageSource();
        assertTrue("Should display error message",
                pageSource.contains("không chính xác") || pageSource.contains("error"));
    }

    // =========================================================================
    //  TEST CASE 3: Đăng nhập thất bại — email rỗng
    // =========================================================================

    /**
     * TC-LOGIN-03: Không cho phép submit khi email rỗng.
     *
     * Steps:
     *   1. Truy cập trang login
     *   2. Bỏ trống email
     *   3. Nhập mật khẩu
     *   4. Nhấn nút "Đăng nhập"
     * Expected: Form không submit (HTML5 required validation)
     */
    @Test
    public void testLogin_EmptyIdentifier_NotSubmitted() {
        driver.get(BASE_URL + "/auth/login");

        // Bỏ trống identifier, chỉ nhập password
        driver.findElement(By.id("password")).sendKeys("somepassword");
        driver.findElement(By.id("loginForm")).submit();

        // Vẫn ở trang login (HTML5 required chặn submit)
        assertTrue("Should stay on login page when email is empty",
                driver.getCurrentUrl().contains("/auth/login"));
    }

    // =========================================================================
    //  TEST CASE 4: Kiểm tra nút "Quên mật khẩu" hoạt động
    // =========================================================================

    /**
     * TC-LOGIN-04: Link "Quên mật khẩu" chuyển đúng trang.
     *
     * Steps:
     *   1. Truy cập trang login
     *   2. Click link "Quên mật khẩu?"
     * Expected: Chuyển sang trang /auth/forgot
     */
    @Test
    public void testLogin_ForgotPasswordLink() {
        driver.get(BASE_URL + "/auth/login");

        // Tìm và click link "Quên mật khẩu?"
        WebElement forgotLink = driver.findElement(
                By.cssSelector("a[href*='/auth/forgot']"));
        forgotLink.click();

        // Chờ redirect
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_SECONDS));
        wait.until(ExpectedConditions.urlContains("/auth/forgot"));

        // Assert
        assertTrue("Should navigate to forgot password page",
                driver.getCurrentUrl().contains("/auth/forgot"));
    }

    // =========================================================================
    //  TEST CASE 5: Kiểm tra link "Đăng ký ngay" hoạt động
    // =========================================================================

    /**
     * TC-LOGIN-05: Link "Đăng ký ngay" chuyển đúng trang.
     *
     * Steps:
     *   1. Truy cập trang login
     *   2. Click link "Đăng ký ngay"
     * Expected: Chuyển sang trang /auth/register
     */
    @Test
    public void testLogin_RegisterLink() {
        driver.get(BASE_URL + "/auth/login");

        // Tìm và click link đăng ký
        WebElement registerLink = driver.findElement(
                By.cssSelector("a[href*='/auth/register']"));
        registerLink.click();

        // Chờ redirect
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_SECONDS));
        wait.until(ExpectedConditions.urlContains("/auth/register"));

        // Assert
        assertTrue("Should navigate to register page",
                driver.getCurrentUrl().contains("/auth/register"));
    }

    // =========================================================================
    //  TEST CASE 6: Kiểm tra nút toggle hiển thị mật khẩu
    // =========================================================================

    /**
     * TC-LOGIN-06: Nút toggle visibility mật khẩu hoạt động đúng.
     *
     * Steps:
     *   1. Truy cập trang login
     *   2. Kiểm tra type ban đầu của field password = "password"
     *   3. Click nút toggle
     *   4. Kiểm tra type đã đổi thành "text"
     */
    @Test
    public void testLogin_PasswordToggleVisibility() {
        driver.get(BASE_URL + "/auth/login");

        WebElement passwordField = driver.findElement(By.id("password"));

        // Ban đầu phải là type="password" (ẩn)
        assertEquals("Initial type should be password",
                "password", passwordField.getAttribute("type"));

        // Click nút toggle visibility
        WebElement toggleBtn = passwordField.findElement(
                By.xpath("./following-sibling::button | ../button"));
        toggleBtn.click();

        // Sau khi click, type phải chuyển thành "text"
        assertEquals("After toggle, type should be text",
                "text", passwordField.getAttribute("type"));
    }

    // =========================================================================
    //  TEST CASE 7: Kiểm tra trang login hiển thị đúng title
    // =========================================================================

    /**
     * TC-LOGIN-07: Trang đăng nhập hiển thị đúng tiêu đề.
     */
    @Test
    public void testLogin_PageTitle() {
        driver.get(BASE_URL + "/auth/login");

        String title = driver.getTitle();
        assertTrue("Page title should contain 'Đăng nhập'",
                title.contains("Đăng nhập"));
    }
}
