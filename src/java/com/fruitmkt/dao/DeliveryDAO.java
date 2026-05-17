package com.fruitmkt.dao;

import com.fruitmkt.dao.base.BaseDAO;
import com.fruitmkt.model.entity.Delivery;
import java.sql.*;
import java.util.*;

/**
 * DeliveryDAO — DAO cho entity Delivery.
 *
 * QUY TẮC:
 *   - Chỉ chứa SQL, không chứa business logic
 *   - Dùng PreparedStatement, KHÔNG nối chuỗi SQL
 *   - Mỗi method ném SQLException để Service xử lý
 *   - Dùng try-with-resources cho Connection + PreparedStatement
 *
 * @author fruitmkt-team
 */
public class DeliveryDAO extends BaseDAO {

    /**
     * TODO: Implement — findById(int id)
     */
    public List<Delivery> findById(int id) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findById(int id)");
    }

    /**
     * TODO: Implement — findByOrder(int orderId)
     */
    public List<Delivery> findByOrder(int orderId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findByOrder(int orderId)");
    }

    /**
     * TODO: Implement — findByStaff(int staffId, String status)
     */
    public List<Delivery> findByStaff(int staffId, String status) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findByStaff(int staffId, String status)");
    }

    /**
     * TODO: Implement — save(Delivery delivery)
     */
    public int save(Delivery delivery) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: save(Delivery delivery)");
    }

    /**
     * TODO: Implement — updateStatus(int deliveryId, String status, String failureReason)
     */
    public void updateStatus(int deliveryId, String status, String failureReason) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: updateStatus(int deliveryId, String status, String failureReason)");
    }

    /** Ánh xạ ResultSet -> Delivery — gọi trong mọi query SELECT */
    private Delivery mapRow(ResultSet rs) throws SQLException {
        // TODO: rs.getInt(), rs.getString()... theo Schema.sql
        throw new UnsupportedOperationException("mapRow not implemented");
    }
}
