package com.fruitmkt.dao;

import com.fruitmkt.dao.base.BaseDAO;
import com.fruitmkt.model.entity.Cart;
import java.sql.*;
import java.util.*;

/**
 * CartDAO — DAO cho entity Cart.
 *
 * QUY TẮC:
 *   - Chỉ chứa SQL, không chứa business logic
 *   - Dùng PreparedStatement, KHÔNG nối chuỗi SQL
 *   - Mỗi method ném SQLException để Service xử lý
 *   - Dùng try-with-resources cho Connection + PreparedStatement
 *
 * @author fruitmkt-team
 */
public class CartDAO extends BaseDAO {

    /**
     * TODO: Implement — findByCustomer(int customerId)
     */
    public List<Cart> findByCustomer(int customerId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findByCustomer(int customerId)");
    }

    /**
     * TODO: Implement — createForCustomer(int customerId)
     */
    public int createForCustomer(int customerId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: createForCustomer(int customerId)");
    }

    /**
     * TODO: Implement — addItem(int cartId, int variantId, int quantity)
     */
    public Cart addItem(int cartId, int variantId, int quantity) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: addItem(int cartId, int variantId, int quantity)");
    }

    /**
     * TODO: Implement — updateItemQuantity(int cartItemId, int quantity)
     */
    public void updateItemQuantity(int cartItemId, int quantity) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: updateItemQuantity(int cartItemId, int quantity)");
    }

    /**
     * TODO: Implement — removeItem(int cartItemId)
     */
    public Cart removeItem(int cartItemId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: removeItem(int cartItemId)");
    }

    /**
     * TODO: Implement — clearCart(int cartId)
     */
    public Cart clearCart(int cartId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: clearCart(int cartId)");
    }

    /**
     * TODO: Implement — findItems(int cartId)
     */
    public List<Cart> findItems(int cartId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findItems(int cartId)");
    }

    /** Ánh xạ ResultSet -> Cart — gọi trong mọi query SELECT */
    private Cart mapRow(ResultSet rs) throws SQLException {
        // TODO: rs.getInt(), rs.getString()... theo Schema.sql
        throw new UnsupportedOperationException("mapRow not implemented");
    }
}
