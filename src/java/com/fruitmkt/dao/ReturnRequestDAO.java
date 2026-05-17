package com.fruitmkt.dao;

import com.fruitmkt.dao.base.BaseDAO;
import com.fruitmkt.model.entity.ReturnRequest;
import java.sql.*;
import java.util.*;

/**
 * ReturnRequestDAO — DAO cho entity ReturnRequest.
 *
 * QUY TẮC:
 *   - Chỉ chứa SQL, không chứa business logic
 *   - Dùng PreparedStatement, KHÔNG nối chuỗi SQL
 *   - Mỗi method ném SQLException để Service xử lý
 *   - Dùng try-with-resources cho Connection + PreparedStatement
 *
 * @author fruitmkt-team
 */
public class ReturnRequestDAO extends BaseDAO {

    /**
     * TODO: Implement — findById(int id)
     */
    public List<ReturnRequest> findById(int id) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findById(int id)");
    }

    /**
     * TODO: Implement — findByOrder(int orderId)
     */
    public List<ReturnRequest> findByOrder(int orderId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findByOrder(int orderId)");
    }

    /**
     * TODO: Implement — findByCustomer(int customerId)
     */
    public List<ReturnRequest> findByCustomer(int customerId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findByCustomer(int customerId)");
    }

    /**
     * TODO: Implement — findPending()
     */
    public ReturnRequest findPending() throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findPending()");
    }

    /**
     * TODO: Implement — save(ReturnRequest req)
     */
    public int save(ReturnRequest req) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: save(ReturnRequest req)");
    }

    /**
     * TODO: Implement — updateStatus(int id, String status, String decisionReason, int decidedBy)
     */
    public void updateStatus(int id, String status, String decisionReason, int decidedBy) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: updateStatus(int id, String status, String decisionReason, int decidedBy)");
    }

    /** Ánh xạ ResultSet -> ReturnRequest — gọi trong mọi query SELECT */
    private ReturnRequest mapRow(ResultSet rs) throws SQLException {
        // TODO: rs.getInt(), rs.getString()... theo Schema.sql
        throw new UnsupportedOperationException("mapRow not implemented");
    }
}
