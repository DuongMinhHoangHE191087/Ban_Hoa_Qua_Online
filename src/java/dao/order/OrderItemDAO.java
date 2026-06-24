package dao.order;

import dao.system.BaseDAO;
import model.entity.cart.CartItem;
import model.entity.order.OrderItem;
import model.entity.catalog.ProductVariant;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderItemDAO extends BaseDAO {

    public List<OrderItem> findByOrderId(int orderId) throws SQLException {
        List<OrderItem> list = new ArrayList<>();
        String sql = "SELECT oi.*, pi.file_path AS image_path "
                + "FROM order_items oi "
                + "LEFT JOIN product_variants pv ON pv.variant_id = oi.variant_id "
                + "LEFT JOIN product_images pi ON pi.product_id = pv.product_id AND pi.is_primary = 1 "
                + "WHERE oi.order_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private OrderItem mapRow(ResultSet rs) throws SQLException {
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
        item.setImagePath(rs.getString("image_path"));
        return item;
    }

    public void saveBatch(Connection conn, int orderId, List<CartItem> items, Map<Integer, ProductVariant> variantMap)
            throws SQLException {
        String sql = "INSERT INTO order_items (order_id, variant_id, product_name_snapshot, variant_label_snapshot, "
                + "quantity, unit_price, subtotal, packaging_label_snapshot, packaging_price_snapshot) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (CartItem item : items) {
                ProductVariant variant = variantMap.get(item.getVariantId());
                java.math.BigDecimal latestPrice = variant != null ? variant.getActivePrice() : item.getPrice();
                java.math.BigDecimal packagingPriceAdd = item.getPackagingPriceAdd() != null
                        ? item.getPackagingPriceAdd() : java.math.BigDecimal.ZERO;
                java.math.BigDecimal subtotal = latestPrice.add(packagingPriceAdd)
                        .multiply(new java.math.BigDecimal(item.getQuantity()));

                ps.setInt(1, orderId);
                ps.setInt(2, item.getVariantId());
                ps.setString(3, item.getProductName());
                ps.setString(4, item.getVariantLabel());
                ps.setInt(5, item.getQuantity());
                ps.setBigDecimal(6, latestPrice);
                ps.setBigDecimal(7, subtotal);
                if (item.getPackagingLabel() != null) {
                    ps.setString(8, item.getPackagingLabel());
                } else {
                    ps.setNull(8, java.sql.Types.NVARCHAR);
                }
                ps.setBigDecimal(9, packagingPriceAdd);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
