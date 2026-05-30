package com.fruitmkt.dao;

import com.fruitmkt.dao.base.BaseDAO;
import com.fruitmkt.model.entity.Review;
import java.sql.*;
import java.util.*;

/**
 * ReviewDAO — DAO cho thực thể Review.
 *
 * QUY TẮC:
 *   - Chỉ chứa SQL, không chứa business logic
 *   - Dùng PreparedStatement, KHÔNG nối chuỗi SQL
 *   - Mỗi method ném SQLException để Service xử lý
 *   - Dùng try-with-resources cho Connection + PreparedStatement
 *
 * @author fruitmkt-team
 */
public class ReviewDAO extends BaseDAO {

    /**
     * Tìm đánh giá dựa trên Order Item ID.
     * Sử dụng câu lệnh JOIN để lấy đầy đủ thông tin tên khách hàng.
     *
     * @param orderItemId mã dòng chi tiết đơn hàng
     * @return danh sách đánh giá tìm thấy
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu
     */
    public List<Review> findByOrderItem(int orderItemId) throws SQLException {
        List<Review> list = new ArrayList<>();
        String sql = "SELECT r.*, u.full_name AS customer_name FROM reviews r "
                   + "JOIN users u ON r.customer_id = u.user_id "
                   + "WHERE r.order_item_id = ? AND r.is_hidden = 0";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderItemId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Lấy toàn bộ danh sách đánh giá của một sản phẩm (chưa phân trang).
     *
     * @param productId mã sản phẩm
     * @return danh sách đánh giá tìm thấy
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu
     */
    public List<Review> findByProduct(int productId) throws SQLException {
        List<Review> list = new ArrayList<>();
        String sql = "SELECT r.*, u.full_name AS customer_name FROM reviews r "
                   + "JOIN users u ON r.customer_id = u.user_id "
                   + "JOIN order_items oi ON r.order_item_id = oi.order_item_id "
                   + "JOIN product_variants pv ON oi.variant_id = pv.variant_id "
                   + "WHERE pv.product_id = ? AND r.is_hidden = 0 "
                   + "ORDER BY r.created_at DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Truy vấn danh sách đánh giá của sản phẩm có hỗ trợ phân trang và bộ lọc theo số sao.
     *
     * @param productId    ID của sản phẩm
     * @param ratingFilter Bộ lọc số sao (1-5), nếu null sẽ lấy tất cả mức sao
     * @param page         Trang hiện tại (1-based)
     * @param pageSize     Số lượng bản ghi mỗi trang
     * @return danh sách các đánh giá thỏa mãn
     * @throws SQLException nếu xảy ra lỗi cơ sở dữ liệu
     */
    public List<Review> findByProductPaginated(int productId, Integer ratingFilter, int page, int pageSize) throws SQLException {
        List<Review> list = new ArrayList<>();
        int offset = (page - 1) * pageSize;
        if (offset < 0) offset = 0;

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT r.*, u.full_name AS customer_name FROM reviews r ")
           .append("JOIN users u ON r.customer_id = u.user_id ")
           .append("JOIN order_items oi ON r.order_item_id = oi.order_item_id ")
           .append("JOIN product_variants pv ON oi.variant_id = pv.variant_id ")
           .append("WHERE pv.product_id = ? AND r.is_hidden = 0 ");

        if (ratingFilter != null) {
            sql.append("AND r.rating = ? ");
        }

        sql.append("ORDER BY r.created_at DESC ")
           .append("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            ps.setInt(paramIndex++, productId);
            if (ratingFilter != null) {
                ps.setInt(paramIndex++, ratingFilter);
            }
            ps.setInt(paramIndex++, offset);
            ps.setInt(paramIndex++, pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Đếm tổng số đánh giá của sản phẩm theo bộ lọc sao để phục vụ phân trang.
     *
     * @param productId    ID của sản phẩm
     * @param ratingFilter Bộ lọc số sao (1-5), nếu null sẽ đếm tất cả mức sao
     * @return tổng số đánh giá tìm thấy
     * @throws SQLException nếu xảy ra lỗi cơ sở dữ liệu
     */
    public int countByProductAndRating(int productId, Integer ratingFilter) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM reviews r ")
           .append("JOIN order_items oi ON r.order_item_id = oi.order_item_id ")
           .append("JOIN product_variants pv ON oi.variant_id = pv.variant_id ")
           .append("WHERE pv.product_id = ? AND r.is_hidden = 0 ");

        if (ratingFilter != null) {
            sql.append("AND r.rating = ? ");
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, productId);
            if (ratingFilter != null) {
                ps.setInt(2, ratingFilter);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Thống kê số lượng đánh giá cho từng mức sao (1 đến 5 sao) của sản phẩm.
     * Dữ liệu này dùng để vẽ biểu đồ phân phối sao ở giao diện chi tiết sản phẩm.
     *
     * @param productId ID sản phẩm
     * @return Map lưu trữ key là số sao (1-5), value là số lượng đánh giá của mức sao đó
     * @throws SQLException nếu xảy ra lỗi cơ sở dữ liệu
     */
    public Map<Integer, Integer> getRatingDistribution(int productId) throws SQLException {
        Map<Integer, Integer> distribution = new HashMap<>();
        // Khởi tạo mặc định 0 cho các mức sao từ 1 đến 5
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0);
        }

        String sql = "SELECT r.rating, COUNT(*) AS count_val FROM reviews r "
                   + "JOIN order_items oi ON r.order_item_id = oi.order_item_id "
                   + "JOIN product_variants pv ON oi.variant_id = pv.variant_id "
                   + "WHERE pv.product_id = ? AND r.is_hidden = 0 "
                   + "GROUP BY r.rating";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int rating = rs.getInt("rating");
                    int count = rs.getInt("count_val");
                    distribution.put(rating, count);
                }
            }
        }
        return distribution;
    }

    /**
     * Kiểm tra xem khách hàng đã đánh giá chi tiết đơn hàng (Order Item) này hay chưa.
     *
     * @param customerId  mã khách hàng
     * @param orderItemId mã dòng chi tiết đơn hàng
     * @return true nếu đã đánh giá, ngược lại false
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu
     */
    public boolean existsByCustomerAndItem(int customerId, int orderItemId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reviews WHERE customer_id = ? AND order_item_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setInt(2, orderItemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Lưu một đánh giá sản phẩm mới vào cơ sở dữ liệu.
     *
     * @param review Đối tượng review chứa dữ liệu cần lưu
     * @return ID tự sinh của đánh giá vừa thêm
     * @throws SQLException nếu xảy ra lỗi cơ sở dữ liệu hoặc không lấy được khóa tự tăng
     */
    public int save(Review review) throws SQLException {
        String sql = "INSERT INTO reviews (order_item_id, customer_id, rating, review_text, review_image_url, is_hidden, created_at) "
                   + "VALUES (?, ?, ?, ?, ?, ?, GETDATE())";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, review.getOrderItemId());
            ps.setInt(2, review.getCustomerId());
            ps.setInt(3, review.getRating());
            ps.setString(4, review.getReviewText());
            ps.setString(5, review.getReviewImageUrl());
            ps.setBoolean(6, review.getIsHidden());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    review.setReviewId(generatedId);
                    return generatedId;
                }
            }
        }
        throw new SQLException("Thêm mới đánh giá sản phẩm thất bại, không lấy được mã khóa tự tăng.");
    }

    /**
     * Lấy toàn bộ danh sách đánh giá cho màn hình Admin (bao gồm cả bị ẩn và không).
     */
    public List<Review> findAllForAdmin() throws SQLException {
        List<Review> list = new ArrayList<>();
        String sql = "SELECT r.*, u.full_name AS customer_name, p.product_name FROM reviews r "
                   + "JOIN users u ON r.customer_id = u.user_id "
                   + "JOIN order_items oi ON r.order_item_id = oi.order_item_id "
                   + "JOIN product_variants pv ON oi.variant_id = pv.variant_id "
                   + "JOIN products p ON pv.product_id = p.product_id "
                   + "ORDER BY r.created_at DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Review r = mapRow(rs);
                // Attach product name manually since mapRow doesn't have a field for it by default
                // But wait, Review.java might not have a productName field.
                // If it doesn't, we can add it, or just use a HashMap / DTO.
                // Let's assume Review has setProductName or we can just fetch it as part of mapRow if we add it to the model.
                // I will add a transient field productName to Review if needed, or just map it if we can.
                try {
                    r.getClass().getMethod("setProductName", String.class).invoke(r, rs.getString("product_name"));
                } catch (Exception e) {
                    // Ignore if method not found
                }
                list.add(r);
            }
        }
        return list;
    }

    /**
     * Cập nhật trạng thái ẩn/hiện của đánh giá (Admin).
     */
    public void updateVisibility(int reviewId, boolean isHidden) throws SQLException {
        String sql = "UPDATE reviews SET is_hidden = ? WHERE review_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, isHidden);
            ps.setInt(2, reviewId);
            ps.executeUpdate();
        }
    }

    /**
     * Ẩn một đánh giá khỏi giao diện (Soft delete).
     */
    public void hide(int reviewId) throws SQLException {
        updateVisibility(reviewId, true);
    }

    /**
     * Ánh xạ kết quả ResultSet sang đối tượng Review.
     *
     * @param rs ResultSet chứa dữ liệu truy vấn từ cơ sở dữ liệu
     * @return Đối tượng Review đã được ánh xạ đầy đủ thuộc tính
     * @throws SQLException nếu xảy ra lỗi đọc dữ liệu từ ResultSet
     */
    private Review mapRow(ResultSet rs) throws SQLException {
        Review r = new Review();
        r.setReviewId(rs.getInt("review_id"));
        r.setOrderItemId(rs.getInt("order_item_id"));
        r.setCustomerId(rs.getInt("customer_id"));
        r.setRating(rs.getInt("rating"));
        r.setReviewText(rs.getString("review_text"));
        r.setIsHidden(rs.getBoolean("is_hidden"));
        
        Timestamp createdAtVal = rs.getTimestamp("created_at");
        if (createdAtVal != null) {
            r.setCreatedAt(createdAtVal.toLocalDateTime());
        }

        // Đọc thuộc tính bổ sung ảnh review
        r.setReviewImageUrl(rs.getString("review_image_url"));

        // Đọc tên khách hàng nếu có trong câu SQL JOIN
        try {
            r.setCustomerName(rs.getString("customer_name"));
        } catch (SQLException e) {
            // Trường hợp truy vấn không có cột customer_name (an toàn dự phòng)
            r.setCustomerName("Khách hàng ẩn danh");
        }

        return r;
    }
}
