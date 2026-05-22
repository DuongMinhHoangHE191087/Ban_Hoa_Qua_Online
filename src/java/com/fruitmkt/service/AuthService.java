package com.fruitmkt.service;

import java.sql.SQLException;

import com.fruitmkt.dao.UserDAO;
import com.fruitmkt.model.entity.User;
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

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public User register(com.fruitmkt.model.entity.User user) throws SQLException, Exception {
        // TODO: Validate input → gọi DAO → business rule → return result
        if (userDAO.findByEmail(user.getEmail()) != null) {
            throw new Exception("Email này đã tồn tại trong hệ thống, Hãy đăng nhập!");
        }

        // Băm mật khẩu để bảo mật trước khi đưa xuống DAO
        // Lưu ý: Nếu HashUtil có hàm hashPassword, hãy dùng nó.
        String hashedPass = HashUtil.hashPassword(user.getPasswordHash()); 

        // Hàm save hoặc insert của DAO
        int insertedId = userDAO.saveNewCustomer(user.getFullName(), user.getEmail(), hashedPass, user.getPhone(), user.getRole());
        if (insertedId > 0) {
            // Lấy lại user vừa tạo để Set vào session
            return userDAO.findByEmail(user.getEmail()); 
        }
        throw new Exception("Lỗi hệ thống khi tạo tài khoản.");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public com.fruitmkt.model.entity.User login(String email, String password) throws SQLException, Exception {
        // TODO: Validate input → gọi DAO → business rule → return result
        User user = userDAO.findByEmail(email);
        if (user == null) {
            throw new Exception("Tài khoản hoặc mật khẩu không chính xác.");
        }

        if ("INACTIVE".equals(user.getStatus())) {
            throw new Exception("Tài khoản của bạn đã bị vô hiệu hoá.");
        }

        // Kiểm tra đối chiếu hash (Tuỳ vào HashUtil bạn đang viết)
        if (!HashUtil.verify(password, user.getPasswordHash())) {
            // Có thể thêm logic: userDAO.incrementFailedLogin(user.getUserId());
            throw new Exception("Tài khoản hoặc mật khẩu không chính xác.");
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
            if ("INACTIVE".equals(existingUser.getStatus())) {
                throw new Exception("Tài khoản của bạn đã bị vô hiệu hoá.");
            }
            return existingUser; 
        } else {
            // Sinh mật khẩu random an toàn vì Oauth không cung cấp pass
            String randomPass = java.util.UUID.randomUUID().toString();
            String hashedPass = HashUtil.hashPassword(randomPass);

            // Insert role mặc định CUSTOMER qua DAO
            int newId = userDAO.saveNewCustomer(fullName, email, hashedPass, null, "CUSTOMER");
            return userDAO.findByEmail(email);
        }
    }

    public void saveUserSession(int userId, String token, java.sql.Timestamp expiresAt) throws SQLException {
        userDAO.saveUserSession(userId, token, expiresAt);
    }

    public void deleteUserSession(String token) throws SQLException {
        userDAO.deleteUserSession(token);
    }
}
