package service.auth;

import config.AppConfig;
import dao.auth.UserAddressDAO;
import dao.auth.UserDAO;
import dao.auth.UserSessionDAO;
import model.entity.auth.User;
import model.entity.auth.UserAddress;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class UserService {
    private final UserDAO userDAO;
    private final UserSessionDAO userSessionDAO;

    public UserService() {
        this.userDAO = new UserDAO();
        this.userSessionDAO = new UserSessionDAO();
    }

    public List<User> searchUsers(String role, String keyword, int offset, int limit) throws SQLException {
        return userDAO.searchUsers(role, keyword, offset, limit);
    }

    public int countUsers(String role, String keyword) throws SQLException {
        return userDAO.countUsers(role, keyword);
    }

    public User findById(int id) throws SQLException { 
        if (id <= 0) {
            throw new IllegalArgumentException("ID không hợp lệ.");
        }
        List<User> users = userDAO.findById(id); 
        return users.isEmpty() ? null : users.get(0); 
    }

    public List<User> getAllUsers() throws SQLException {
        return userDAO.findAll();
    }

    public void updateUser(User user) throws SQLException { 
        if (user == null) {
            throw new IllegalArgumentException("User không được null.");
        }
        userDAO.update(user); 
    }

    public void saveOrUpdateCheckoutContactInfo(User user, String recipientName, String phone, String addressDetail, boolean saveAddressToBook) throws SQLException {
        if (user == null) {
            throw new IllegalArgumentException("User không được null.");
        }
        if (recipientName == null || recipientName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên người nhận không được để trống.");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Số điện thoại không được để trống.");
        }
        if (addressDetail == null || addressDetail.trim().isEmpty()) {
            throw new IllegalArgumentException("Địa chỉ không được để trống.");
        }

        UserAddressDAO addressDAO = new UserAddressDAO();
        User persistedUser = userDAO.findUserById(user.getUserId());
        if (persistedUser == null) {
            throw new IllegalArgumentException("User không tồn tại.");
        }

        // Merge from the persisted snapshot so a partial session user does not
        // overwrite required columns like full_name during checkout.
        user.setFullName(persistedUser.getFullName());
        user.setEmail(persistedUser.getEmail());
        user.setPasswordHash(persistedUser.getPasswordHash());
        user.setPhone(persistedUser.getPhone());
        user.setRole(persistedUser.getRole());
        user.setStatus(persistedUser.getStatus());
        user.setUserAddress(persistedUser.getUserAddress());
        user.setAvatarUrl(persistedUser.getAvatarUrl());
        
        boolean profileUpdated = false;
        if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
            user.setPhone(phone.trim());
            profileUpdated = true;
        }
        
        if (user.getUserAddress() == null || user.getUserAddress().trim().isEmpty()) {
            user.setUserAddress(addressDetail.trim());
            profileUpdated = true;
        }
        
        if (profileUpdated) {
            userDAO.update(user);
        }
        
        List<UserAddress> existingAddresses = addressDAO.findByUser(user.getUserId());
        boolean hasNoAddresses = existingAddresses.isEmpty();
        
        if (saveAddressToBook || hasNoAddresses) {
            boolean exists = false;
            for (UserAddress existing : existingAddresses) {
                if (existing.getAddressDetail() != null && existing.getAddressDetail().trim().equalsIgnoreCase(addressDetail.trim())) {
                    exists = true;
                    break;
                }
            }
            
            if (!exists) {
                UserAddress newAddr = new UserAddress();
                newAddr.setUserId(user.getUserId());
                newAddr.setRecipientName(recipientName.trim());
                newAddr.setRecipientPhone(phone.trim());
                newAddr.setAddressDetail(addressDetail.trim());
                if (hasNoAddresses) {
                    newAddr.setDefault(true);
                } else {
                    newAddr.setDefault(false);
                }
                addressDAO.save(newAddr);
            }
        }
    }

    public boolean updateUserStatus(int userId, String status) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID không hợp lệ.");
        }
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Trạng thái không được để trống.");
        }
        String normalizedStatus = status.trim().toUpperCase(Locale.ROOT);
        if (!AppConfig.ACCOUNT_STATUS_ACTIVE.equals(normalizedStatus)
                && !AppConfig.ACCOUNT_STATUS_SUSPENDED.equals(normalizedStatus)) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ. Chỉ chấp nhận ACTIVE hoặc SUSPENDED.");
        }
        return userDAO.updateUserStatus(userId, normalizedStatus);
    }

    public boolean isPhoneTakenByAnother(String phone, int userId) throws SQLException {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ.");
        }
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID không hợp lệ.");
        }
        return userDAO.isPhoneExists(phone, userId);
    }

    public void deleteSessionsByUserId(int userId) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID không hợp lệ.");
        }
        userSessionDAO.deleteSessionsByUserId(userId);
    }

    public void deleteAllSessions() throws SQLException {
        userSessionDAO.deleteAllSessions();
    }
}
