package com.fruitmkt.config;

/**
 * AppConfig — Hằng số toàn cục của ứng dụng.
 *
 * Đây là nơi DUY NHẤT chứa các magic number và string literals dùng chung.
 * Khi cần thay đổi giá trị config, chỉ sửa ở đây.
 *
 * @author fruitmkt-team
 */
public final class AppConfig {
        // ——————————————————————————————————————————————————————————————————
        // Database
        // ------------------------------------------------------------------
        public static final String DB_HOST = "localhost";
        public static final String DB_PORT = "1433";
        public static final String DB_NAME = "OnlineFruitShopping";
        public static final String DB_USER = "sa";
        public static final String DB_PASSWORD = "123";
        public static final String DB_DRIVER_CLASS = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        public static final String DB_JDBC_URL = "jdbc:sqlserver://" + DB_HOST + ":" + DB_PORT
                        + ";databaseName=" + DB_NAME
                        + ";encrypt=false;trustServerCertificate=true";

        public static final String GOOGLE_CLIENT_ID = "710006759532-tnve0ctpc8d6m88qidm8g65in482rfnn.apps.googleusercontent.com";
        public static final String GOOGLE_CLIENT_SECRET = "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";
        // Domain của bạn. Nếu code ở localhost thì để HTTP
        public static final String GOOGLE_REDIRECT_URI = "http://localhost:8080/Ban_Hoa_Qua_Online/GoogleCallback";
        public static final String GOOGLE_LINK_GET_TOKEN = "https://oauth2.googleapis.com/token";
        public static final String GOOGLE_LINK_GET_USER_INFO = "https://www.googleapis.com/oauth2/v1/userinfo?access_token=";
        public static final String GOOGLE_GRANT_TYPE = "authorization_code";

        public static final String EMAIL_SMTP_HOST = "smtp.gmail.com";
        public static final String EMAIL_SMTP_PORT = "587";
        public static final String EMAIL_FROM = "duongminhhoanginwork@gmail.com";
        public static final String EMAIL_PASSWORD = "jkhg przg aohf pwla";
        public static final String APP_NAME = "MetaFruit";
        public static final String APP_SUPPORT_EMAIL = "duongminhhoanginwork@gmail.com";
        public static final String APP_BRAND_COLOR = "#14532d";
        public static final String APP_BASE_URL = "http://localhost:8080/Ban_Hoa_Qua_Online";

        // ------------------------------------------------------------------
        // Email verification
        // ------------------------------------------------------------------
        public static final int EMAIL_VERIFICATION_CODE_LENGTH = 6;
        public static final int EMAIL_VERIFICATION_TTL_MINUTES = 5;
        public static final int EMAIL_VERIFICATION_RESEND_SECONDS = 60;

        // ------------------------------------------------------------------
        // Account status
        // ------------------------------------------------------------------
        public static final String ACCOUNT_STATUS_ACTIVE = "ACTIVE";
        public static final String ACCOUNT_STATUS_INACTIVE = "INACTIVE";

        // ------------------------------------------------------------------
        // Phân trang (Pagination)
        // ------------------------------------------------------------------
        /** Số sản phẩm mỗi trang trên trang listing */
        public static final int PAGE_SIZE_PRODUCTS = 20;
        /** Số đơn hàng mỗi trang */
        public static final int PAGE_SIZE_ORDERS = 15;
        /** Số dòng admin table mỗi trang */
        public static final int PAGE_SIZE_ADMIN = 25;

        // ------------------------------------------------------------------
        // Upload File
        // ------------------------------------------------------------------
        /** Kích thước tối đa 1 file upload: 5 MB */
        public static final long MAX_UPLOAD_SIZE_BYTES = 5L * 1024 * 1024;
        /** Thư mục upload relative với webapp root — tạo bằng FileUploadUtil */
        public static final String UPLOAD_DIR = "uploads";
        /** Các đuôi file ảnh được phép */
        public static final String[] ALLOWED_IMAGE_EXTS = { "jpg", "jpeg", "png", "webp" };

        // ------------------------------------------------------------------
        // Session Keys — dùng thống nhất trong toàn bộ code
        // ------------------------------------------------------------------
        /** Key lưu User object trong session sau khi đăng nhập */
        public static final String SESSION_USER = "currentUser";
        /** Key lưu email chờ xác minh */
        public static final String SESSION_VERIFY_EMAIL = "verifyEmail";
        /** Key lưu flash message sau PRG redirect */
        public static final String SESSION_FLASH_MSG = "flashMsg";
        /** Key lưu loại flash (success / error / warning / info) */
        public static final String SESSION_FLASH_TYPE = "flashType";
        /** Key lưu CSRF token */
        public static final String SESSION_CSRF_TOKEN = "_csrfToken";

        // ------------------------------------------------------------------
        // Role Values — khớp với column role trong bảng users
        // ------------------------------------------------------------------
        public static final String ROLE_CUSTOMER = "CUSTOMER";
        public static final String ROLE_SHOP_OWNER = "SHOP_OWNER";
        public static final String ROLE_DELIVERY = "DELIVERY";
        public static final String ROLE_ADMIN = "ADMIN";

        // ------------------------------------------------------------------
        // Order Status — khớp CHECK constraint trong bảng orders
        // ------------------------------------------------------------------
        public static final String ORDER_PENDING_PAYMENT = "PENDING_PAYMENT";
        public static final String ORDER_CONFIRMED = "CONFIRMED";
        public static final String ORDER_PREPARING = "PREPARING";
        public static final String ORDER_DISPATCHED = "DISPATCHED";
        public static final String ORDER_DELIVERED = "DELIVERED";
        public static final String ORDER_CANCELLED = "CANCELLED";
        public static final String ORDER_PAYMENT_FAILED = "PAYMENT_FAILED";
        public static final String ORDER_EXPIRED = "EXPIRED";

        // ------------------------------------------------------------------
        // Delivery Status
        // ------------------------------------------------------------------
        public static final String DELIVERY_ASSIGNED = "ASSIGNED";
        public static final String DELIVERY_PICKED_UP = "PICKED_UP";
        public static final String DELIVERY_IN_TRANSIT = "IN_TRANSIT";
        public static final String DELIVERY_DELIVERED = "DELIVERED";
        public static final String DELIVERY_FAILED = "FAILED";

        // ------------------------------------------------------------------
        // Shop Approval Status
        // ------------------------------------------------------------------
        public static final String SHOP_PENDING = "PENDING";
        public static final String SHOP_APPROVED = "APPROVED";
        public static final String SHOP_REJECTED = "REJECTED";
        public static final String SHOP_SUSPENDED = "SUSPENDED";

        // ------------------------------------------------------------------
        // Payment Method
        // ------------------------------------------------------------------
        public static final String PAYMENT_CK = "CK"; // Chuyển khoản
        public static final String PAYMENT_COD = "COD"; // Thanh toán khi nhận

        // ------------------------------------------------------------------
        // Return Request
        // ------------------------------------------------------------------
        public static final String RETURN_REQUESTED = "REQUESTED";
        public static final String RETURN_APPROVED = "APPROVED";
        public static final String RETURN_REJECTED = "REJECTED";
        public static final String RETURN_PROCESSING = "PROCESSING";
        public static final String RETURN_COMPLETED = "COMPLETED";
        public static final String RETURN_CANCELLED = "CANCELLED";

        // ------------------------------------------------------------------
        // Notification Types
        // ------------------------------------------------------------------
        public static final String NOTIF_ORDER_UPDATE = "ORDER_UPDATE";
        public static final String NOTIF_PROMOTION = "PROMOTION";
        public static final String NOTIF_SYSTEM = "SYSTEM";
        public static final String NOTIF_INVENTORY_ALERT = "INVENTORY_ALERT";
        public static final String NOTIF_PAYMENT = "PAYMENT";

        // ------------------------------------------------------------------
        // Security
        // ------------------------------------------------------------------
        /** Số lần đăng nhập sai tối đa trước khi khóa tài khoản */
        public static final int MAX_FAILED_LOGIN = 5;
        /** Thời gian khóa tài khoản (phút) */
        public static final int LOCK_DURATION_MINUTES = 30;

        private AppConfig() {
                /* Utility class — không khởi tạo */ }

        /**
         * Đọc config theo ưu tiên: System property -> env variable -> default value.
         * Production: đặt JVM option (-Ddb.password=...) hoặc env variable
         * (DB_PASSWORD=...).
         */

}
