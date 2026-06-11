package com.fruitmkt.service;

import com.fruitmkt.dao.UserDAO;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.dao.UserAddressDAO;
import com.fruitmkt.model.entity.UserAddress;
import java.sql.SQLException;
import java.util.List;

public class UserService {
    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    public List<User> searchUsers(String role, String keyword, int offset, int limit) throws SQLException { return userDAO.searchUsers(role, keyword, offset, limit); }
    public int countUsers(String role, String keyword) throws SQLException { return userDAO.countUsers(role, keyword); }
    public User findById(int id) throws SQLException { 
        List<User> users = userDAO.findById(id); 
        return users.isEmpty() ? null : users.get(0); 
    }

    public List<User> getAllUsers() throws SQLException {
        return userDAO.findAll();
    }

    public void updateUser(User user) throws SQLException { userDAO.update(user); }

    public void saveOrUpdateCheckoutContactInfo(User user, String recipientName, String phone, String addressDetail, boolean saveAddressToBook) throws SQLException {
        UserAddressDAO addressDAO = new UserAddressDAO();
        
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
        return userDAO.updateUserStatus(userId, status);
    }

    public boolean isPhoneTakenByAnother(String phone, int userId) throws SQLException {
        return userDAO.isPhoneExists(phone, userId);
    }

    public void deleteSessionsByUserId(int userId) throws SQLException {
        userDAO.deleteSessionsByUserId(userId);
    }
}
