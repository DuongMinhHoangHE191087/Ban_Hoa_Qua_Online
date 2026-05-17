package com.fruitmkt.dao;

import com.fruitmkt.dao.base.BaseDAO;
import com.fruitmkt.model.entity.User;
import java.sql.*;
import java.util.*;

/**
 * UserDAO — DAO cho entity User.
 *
 * QUY TẮC:
 *   - Chỉ chứa SQL, không chứa business logic
 *   - Dùng PreparedStatement, KHÔNG nối chuỗi SQL
 *   - Mỗi method ném SQLException để Service xử lý
 *   - Dùng try-with-resources cho Connection + PreparedStatement
 *
 * @author fruitmkt-team
 */
public class UserDAO extends BaseDAO {

    /**
     * TODO: Implement — findById(int id)
     */
    public List<User> findById(int id) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findById(int id)");
    }

    /**
     * TODO: Implement — findByEmail(String email)
     */
    public List<User> findByEmail(String email) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findByEmail(String email)");
    }

    /**
     * TODO: Implement — findAll()
     */
    public List<User> findAll() throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findAll()");
    }

    /**
     * TODO: Implement — save(User user)
     */
    public int save(User user) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: save(User user)");
    }

    /**
     * TODO: Implement — update(User user)
     */
    public void update(User user) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: update(User user)");
    }

    /**
     * TODO: Implement — updatePassword(int userId, String newHash)
     */
    public void updatePassword(int userId, String newHash) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: updatePassword(int userId, String newHash)");
    }

    /**
     * TODO: Implement — incrementFailedLogin(int userId)
     */
    public void incrementFailedLogin(int userId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: incrementFailedLogin(int userId)");
    }

    /**
     * TODO: Implement — resetFailedLogin(int userId)
     */
    public void resetFailedLogin(int userId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: resetFailedLogin(int userId)");
    }

    /**
     * TODO: Implement — lockAccount(int userId, java.time.LocalDateTime until)
     */
    public void lockAccount(int userId, java.time.LocalDateTime until) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: lockAccount(int userId, java.time.LocalDateTime until)");
    }

    /** Ánh xạ ResultSet -> User — gọi trong mọi query SELECT */
    private User mapRow(ResultSet rs) throws SQLException {
        // TODO: rs.getInt(), rs.getString()... theo Schema.sql
        throw new UnsupportedOperationException("mapRow not implemented");
    }
}
