package com.fruitmkt.service;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.security.SecureRandom;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.dao.UserDAO;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.service.EmailService;
import com.fruitmkt.util.HashUtil;
import com.fruitmkt.util.ValidationUtil;
/**
 * AuthService — Tầng business logic cho nghiệp vụ tương ứng.
 *
 * QUY TẮC:
 *   - Chỉ gọi DAO, không viết SQL ở đây
 *   - Chứa tất cả validation và business rule
 *   - Ném RuntimeException hoặc custom exception cho Servlet xử lý
 *   - Không tương tác trực tiếp với HttpRequest/Response
 *
 * @author fruitmkt-team
 */
public class AuthService {
    private final UserDAO userDAO = new UserDAO();
    private final EmailService emailService = new EmailService();
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static class VerificationRequiredException extends Exception {
        private final String email;

        public VerificationRequiredException(String email, String message) {
            super(message);
            this.email = email;
        }

        public String getEmail() {
            return email;
        }
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public User register(com.fruitmkt.model.entity.User user) throws SQLException, Exception {
        return register(user, null, null);
    }

    public User register(com.fruitmkt.model.entity.User user, String shopName, String shopAddress) throws SQLException, Exception {
        // Validate input
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            throw new Exception("Họ và tên không được để trống!");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new Exception("Email không được để trống!");
        }
        if (user.getPasswordHash() == null || user.getPasswordHash().trim().isEmpty()) {
            throw new Exception("Mật khẩu không được để trống!");
        }
        if (!ValidationUtil.isValidPhone(user.getPhone())) {
            throw new Exception("Số điện thoại không hợp lệ!");
        }

        User existingUser = userDAO.findByEmail(user.getEmail());
        if (existingUser != null) {
            throw new Exception("Tài khoản hoặc số điện thoại đã được đăng ký, vui lòng đăng nhập!");
        }

        User existingPhoneUser = userDAO.findByPhone(user.getPhone());
        if (existingPhoneUser != null) {
            throw new Exception("Tài khoản hoặc số điện thoại đã được đăng ký, vui lòng đăng nhập!");
        }

        // Băm mật khẩu để bảo mật trước khi đưa xuống DAO
        String hashedPass = HashUtil.hashPassword(user.getPasswordHash()); 

        // Hàm save hoặc insert của DAO
        int insertedId = userDAO.saveNewCustomer(user.getFullName(), user.getEmail(), hashedPass, user.getPhone(), user.getRole(), AppConfig.ACCOUNT_STATUS_INACTIVE, false);
        if (insertedId > 0) {
            User createdUser = userDAO.findByEmail(user.getEmail());
            if (createdUser == null) {
                throw new Exception("Không thể tải lại thông tin tài khoản vừa tạo.");
            }

            issueVerificationCode(createdUser);

            // Tự động khởi tạo giỏ hàng hoặc profile cửa hàng dựa trên vai trò
            if ("CUSTOMER".equals(user.getRole())) {
                com.fruitmkt.dao.CartDAO cartDAO = new com.fruitmkt.dao.CartDAO();
                cartDAO.createForCustomer(insertedId);
            } else if ("SHOP_OWNER".equals(user.getRole())) {
                com.fruitmkt.model.entity.ShopProfile profile = new com.fruitmkt.model.entity.ShopProfile();
                profile.setUserId(insertedId);
                profile.setShopName(shopName != null && !shopName.trim().isEmpty() ? shopName : "Cửa hàng của " + user.getFullName());
                profile.setShopDescription("Chào mừng tới cửa hàng của chúng tôi!");
                profile.setApprovalStatus("PENDING");
                profile.setDeliveryAddress(shopAddress != null ? shopAddress : user.getUserAddress());
                profile.setRating(java.math.BigDecimal.ZERO);

                com.fruitmkt.dao.ShopProfileDAO shopProfileDAO = new com.fruitmkt.dao.ShopProfileDAO();
                shopProfileDAO.save(profile);
            }

            // Lấy lại user vừa tạo để Set vào session
            return createdUser; 
        }
        throw new Exception("Lỗi hệ thống khi tạo tài khoản.");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public com.fruitmkt.model.entity.User login(String identifier, String password) throws SQLException, Exception {
        // TODO: Validate input → gọi DAO → business rule → return result
        if (!ValidationUtil.notBlank(identifier)) {
            throw new Exception("Email hoặc số điện thoại không được để trống.");
        }

        User user = userDAO.findByLoginIdentifier(identifier.trim());
        if (user == null) {
            throw new Exception("Tài khoản hoặc mật khẩu không chính xác.");
        }

        // Kiểm tra đối chiếu hash (Tuỳ vào HashUtil bạn đang viết)
        if (!HashUtil.verify(password, user.getPasswordHash())) {
            // Có thể thêm logic: userDAO.incrementFailedLogin(user.getUserId());
            throw new Exception("Tài khoản hoặc mật khẩu không chính xác.");
        }

        if (!AppConfig.ACCOUNT_STATUS_ACTIVE.equals(user.getStatus()) || !user.isEmailVerified()) {
            throw new VerificationRequiredException(user.getEmail(), "Tài khoản chưa được xác minh. Vui lòng nhập mã code để kích hoạt tài khoản.");
        }

        // Thành công: Xóa biến đếm số lần sai mật khẩu
        // userDAO.resetFailedLogin(user.getUserId());
        return user;
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public void logout(int userId) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: logout(int userId)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public void handleFailedLogin(String email) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: handleFailedLogin(String email)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public void resetPassword(String email, String newPassword) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: resetPassword(String email, String newPassword)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public boolean isEmailTaken(String email) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: isEmailTaken(String email)");
    }
    public User processGoogleLogin(String email, String fullName) throws Exception {
        User existingUser = userDAO.findByEmail(email);
        if (existingUser != null) {
            if (!AppConfig.ACCOUNT_STATUS_ACTIVE.equals(existingUser.getStatus()) || !existingUser.isEmailVerified()) {
                userDAO.activateVerifiedEmail(existingUser.getUserId());
                existingUser = userDAO.findByEmail(email);
            }
            return existingUser; 
        } else {
            // Sinh mật khẩu random an toàn vì Oauth không cung cấp pass
            String randomPass = java.util.UUID.randomUUID().toString();
            String hashedPass = HashUtil.hashPassword(randomPass);

            // Insert role mặc định CUSTOMER qua DAO
            int newId = userDAO.saveNewCustomer(fullName, email, hashedPass, null, "CUSTOMER", AppConfig.ACCOUNT_STATUS_ACTIVE, true);
            
            // Tự động khởi tạo giỏ hàng cho tài khoản Google mới
            com.fruitmkt.dao.CartDAO cartDAO = new com.fruitmkt.dao.CartDAO();
            cartDAO.createForCustomer(newId);

            return userDAO.findByEmail(email);
        }
    }

    public User verifyEmailCode(String email, String code) throws Exception {
        if (!ValidationUtil.notBlank(email) || !ValidationUtil.isValidEmail(email)) {
            throw new Exception("Email không hợp lệ.");
        }
        if (!ValidationUtil.notBlank(code)) {
            throw new Exception("Mã xác minh không được để trống.");
        }

        User user = userDAO.findByEmail(email);
        if (user == null) {
            throw new Exception("Không tìm thấy tài khoản cần xác minh.");
        }
        if (AppConfig.ACCOUNT_STATUS_ACTIVE.equals(user.getStatus()) && user.isEmailVerified()) {
            return user;
        }

        if (user.getEmailVerificationExpiresAt() == null || LocalDateTime.now().isAfter(user.getEmailVerificationExpiresAt())) {
            throw new Exception("Mã xác minh đã hết hạn. Vui lòng gửi lại mã mới.");
        }

        if (!HashUtil.verify(code.trim(), user.getEmailVerificationCodeHash())) {
            throw new Exception("Mã xác minh không chính xác.");
        }

        userDAO.activateVerifiedEmail(user.getUserId());
        return userDAO.findByEmail(email);
    }

    public User resendVerificationCode(String email) throws Exception {
        if (!ValidationUtil.notBlank(email) || !ValidationUtil.isValidEmail(email)) {
            throw new Exception("Email không hợp lệ.");
        }

        User user = userDAO.findByEmail(email);
        if (user == null) {
            throw new Exception("Không tìm thấy tài khoản cần xác minh.");
        }
        if (AppConfig.ACCOUNT_STATUS_ACTIVE.equals(user.getStatus()) && user.isEmailVerified()) {
            throw new Exception("Tài khoản này đã được xác minh.");
        }
        if (user.getEmailVerificationResendAt() != null && LocalDateTime.now().isBefore(user.getEmailVerificationResendAt())) {
            throw new Exception("Vui lòng chờ 1 phút rồi mới gửi lại mã.");
        }

        issueVerificationCode(user);
        return userDAO.findByEmail(email);
    }

    private void issueVerificationCode(User user) throws Exception {
        String verificationCode = generateVerificationCode();
        String codeHash = HashUtil.hashPassword(verificationCode);
        LocalDateTime now = LocalDateTime.now();
        Timestamp expiresAt = Timestamp.valueOf(now.plusMinutes(AppConfig.EMAIL_VERIFICATION_TTL_MINUTES));
        Timestamp resendAt = Timestamp.valueOf(now.plusSeconds(AppConfig.EMAIL_VERIFICATION_RESEND_SECONDS));

        userDAO.saveEmailVerificationCode(user.getUserId(), codeHash, expiresAt, resendAt);
        boolean sent = emailService.sendVerificationCodeEmail(user.getEmail(), user.getFullName(), verificationCode);
        if (!sent) {
            throw new Exception("Không thể gửi email xác minh. Vui lòng thử lại sau.");
        }
    }

    private String generateVerificationCode() {
        int bound = (int) Math.pow(10, AppConfig.EMAIL_VERIFICATION_CODE_LENGTH);
        int min = bound / 10;
        int code = min + SECURE_RANDOM.nextInt(bound - min);
        return String.format("%0" + AppConfig.EMAIL_VERIFICATION_CODE_LENGTH + "d", code);
    }

    public void saveUserSession(int userId, String token, java.sql.Timestamp expiresAt) throws SQLException {
        userDAO.saveUserSession(userId, token, expiresAt);
    }

    public void deleteUserSession(String token) throws SQLException {
        userDAO.deleteUserSession(token);
    }
}
