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

    public List<User> getAllUsers() throws SQLException {
        return userDAO.findAll();
    }

    public boolean updateUserStatus(int userId, String status) throws SQLException {
        return userDAO.updateUserStatus(userId, status);
    }
}
