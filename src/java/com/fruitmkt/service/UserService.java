package com.fruitmkt.service;

import com.fruitmkt.dao.UserDAO;
import com.fruitmkt.model.entity.User;
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
    public boolean updateUserStatus(int userId, String status) throws SQLException {
        return userDAO.updateUserStatus(userId, status);
    }
}
