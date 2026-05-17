package com.fruitmkt.dao;

import com.fruitmkt.dao.base.BaseDAO;
import com.fruitmkt.model.entity.Order;
import java.sql.*;
import java.util.*;

/**
 * OrderDAO — DAO cho entity Order.
 *
 * QUY TẮC:
 *   - Chỉ chứa SQL, không chứa business logic
 *   - Dùng PreparedStatement, KHÔNG nối chuỗi SQL
 *   - Mỗi method ném SQLException để Service xử lý
 *   - Dùng try-with-resources cho Connection + PreparedStatement
 *
 * @author fruitmkt-team
 */
public class OrderDAO extends BaseDAO {

    /**
     * TODO: Implement — findById(int id)
     */
    public List<Order> findById(int id) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findById(int id)");
    }

    /**
     * TODO: Implement — findByCustomer(int customerId, int page, int pageSize)
     */
    public List<Order> findByCustomer(int customerId, int page, int pageSize) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findByCustomer(int customerId, int page, int pageSize)");
    }

    /**
     * TODO: Implement — findByOwner(int ownerId, String status, int page, int pageSize)
     */
    public List<Order> findByOwner(int ownerId, String status, int page, int pageSize) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findByOwner(int ownerId, String status, int page, int pageSize)");
    }

    /**
     * TODO: Implement — findAll(String status, int page, int pageSize)
     */
    public List<Order> findAll(String status, int page, int pageSize) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findAll(String status, int page, int pageSize)");
    }

    /**
     * TODO: Implement — save(Order order)
     */
    public int save(Order order) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: save(Order order)");
    }

    /**
     * TODO: Implement — updateStatus(int orderId, String status)
     */
    public void updateStatus(int orderId, String status) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: updateStatus(int orderId, String status)");
    }

    /**
     * TODO: Implement — cancel(int orderId, int cancelledBy, String reason)
     */
    public void cancel(int orderId, int cancelledBy, String reason) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: cancel(int orderId, int cancelledBy, String reason)");
    }

    /** Ánh xạ ResultSet -> Order — gọi trong mọi query SELECT */
    private Order mapRow(ResultSet rs) throws SQLException {
        // TODO: rs.getInt(), rs.getString()... theo Schema.sql
        throw new UnsupportedOperationException("mapRow not implemented");
    }
}
