package com.fruitmkt.dao;

import com.fruitmkt.dao.base.BaseDAO;
import com.fruitmkt.model.entity.PaymentTransaction;
import java.sql.*;
import java.util.*;

/**
 * PaymentDAO — DAO cho entity PaymentTransaction.
 *
 * QUY TẮC:
 *   - Chỉ chứa SQL, không chứa business logic
 *   - Dùng PreparedStatement, KHÔNG nối chuỗi SQL
 *   - Mỗi method ném SQLException để Service xử lý
 *   - Dùng try-with-resources cho Connection + PreparedStatement
 *
 * @author fruitmkt-team
 */
public class PaymentDAO extends BaseDAO {

    /**
     * TODO: Implement — findByOrder(int orderId)
     */
    public List<PaymentTransaction> findByOrder(int orderId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findByOrder(int orderId)");
    }

    /**
     * TODO: Implement — findBySepayTxId(String txId)
     */
    public List<PaymentTransaction> findBySepayTxId(String txId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findBySepayTxId(String txId)");
    }

    /**
     * TODO: Implement — save(PaymentTransaction tx)
     */
    public int save(PaymentTransaction tx) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: save(PaymentTransaction tx)");
    }

    /**
     * TODO: Implement — updateStatus(int txId, String status)
     */
    public void updateStatus(int txId, String status) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: updateStatus(int txId, String status)");
    }

    /**
     * TODO: Implement — isDuplicate(String sepayTxId)
     */
    public boolean isDuplicate(String sepayTxId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: isDuplicate(String sepayTxId)");
    }

    /** Ánh xạ ResultSet -> PaymentTransaction — gọi trong mọi query SELECT */
    private PaymentTransaction mapRow(ResultSet rs) throws SQLException {
        // TODO: rs.getInt(), rs.getString()... theo Schema.sql
        throw new UnsupportedOperationException("mapRow not implemented");
    }
}
