package com.fruitmkt.dao;

import com.fruitmkt.dao.base.BaseDAO;
import com.fruitmkt.model.entity.OrderItem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderItemDAO extends BaseDAO {

    public List<OrderItem> findByOrderId(int orderId) throws SQLException {
        List<OrderItem> list = new ArrayList<>();
        String sql = "SELECT * FROM order_items WHERE order_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setOrderItemId(rs.getInt("order_item_id"));
                    item.setOrderId(rs.getInt("order_id"));
                    item.setVariantId(rs.getObject("variant_id") != null ? rs.getInt("variant_id") : null);
                    item.setProductNameSnapshot(rs.getString("product_name_snapshot"));
                    item.setVariantLabelSnapshot(rs.getString("variant_label_snapshot"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setSubtotal(rs.getBigDecimal("subtotal"));
                    item.setPackagingLabelSnapshot(rs.getString("packaging_label_snapshot"));
                    item.setPackagingPriceSnapshot(rs.getBigDecimal("packaging_price_snapshot"));
                    list.add(item);
                }
            }
        }
        return list;
    }
}
