package com.fruitmkt.dao;

import com.fruitmkt.dao.base.BaseDAO;
import com.fruitmkt.model.entity.Notification;
import java.sql.*;
import java.util.*;

/**
 * NotificationDAO — DAO cho entity Notification.
 *
 * QUY TẮC:
 *   - Chỉ chứa SQL, không chứa business logic
 *   - Dùng PreparedStatement, KHÔNG nối chuỗi SQL
 *   - Mỗi method ném SQLException để Service xử lý
 *   - Dùng try-with-resources cho Connection + PreparedStatement
 *
 * @author fruitmkt-team
 */
public class NotificationDAO extends BaseDAO {

    /**
     * TODO: Implement — findByUser(int userId, boolean unreadOnly)
     */
    public List<Notification> findByUser(int userId, boolean unreadOnly) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findByUser(int userId, boolean unreadOnly)");
    }

    /**
     * TODO: Implement — save(Notification notif)
     */
    public int save(Notification notif) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: save(Notification notif)");
    }

    /**
     * TODO: Implement — markRead(int notifId)
     */
    public void markRead(int notifId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: markRead(int notifId)");
    }

    /**
     * TODO: Implement — markAllRead(int userId)
     */
    public void markAllRead(int userId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: markAllRead(int userId)");
    }

    /** Ánh xạ ResultSet -> Notification — gọi trong mọi query SELECT */
    private Notification mapRow(ResultSet rs) throws SQLException {
        // TODO: rs.getInt(), rs.getString()... theo Schema.sql
        throw new UnsupportedOperationException("mapRow not implemented");
    }
}
