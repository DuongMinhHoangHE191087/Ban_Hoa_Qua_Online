package service.order;

import dao.order.ReviewDAO;
import dao.catalog.ProductDAO;
import model.dto.common.PagedResultDTO;
import model.entity.order.Review;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import util.PaginationUtil;

/**
 * ReviewService — Tầng business logic cho nghiệp vụ đánh giá sản phẩm.
 *
 * QUY TẮC:
 *   - Chỉ gọi DAO, không viết SQL ở đây
 *   - Chứa tất cả validation và business rule
 *   - Ném RuntimeException hoặc custom exception cho Servlet xử lý
 *   - Không tương tác trực tiếp với HttpRequest/Response
 *
 * @author fruitmkt-team
 */
public class ReviewService {

    private final ReviewDAO reviewDAO = new ReviewDAO();
    private final ProductDAO productDAO = new ProductDAO();

    /**
     * Gửi một đánh giá mới từ khách hàng sau khi đã xác thực đầy đủ điều kiện.
     *
     * @param review Đối tượng review cần gửi
     * @throws IllegalArgumentException nếu các tham số review không hợp lệ
     * @throws SQLException             nếu xảy ra lỗi trong quá trình tương tác cơ sở dữ liệu
     */
    public void submitReview(Review review) throws SQLException {
        if (review == null) {
            throw new IllegalArgumentException("Dữ liệu đánh giá không được để trống.");
        }
        if (review.getCustomerId() <= 0) {
            throw new IllegalArgumentException("Mã khách hàng không hợp lệ.");
        }
        if (review.getOrderItemId() <= 0) {
            throw new IllegalArgumentException("Mã dòng chi tiết đơn hàng không hợp lệ.");
        }
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("Điểm đánh giá sao phải nằm trong khoảng từ 1 đến 5 sao.");
        }
        if (review.getReviewText() != null && review.getReviewText().length() > 1000) {
            throw new IllegalArgumentException("Nội dung đánh giá không được vượt quá 1000 ký tự.");
        }

        // Kiểm tra xem khách hàng này đã viết đánh giá cho order item này chưa
        if (reviewDAO.existsByCustomerAndItem(review.getCustomerId(), review.getOrderItemId())) {
            throw new IllegalArgumentException("Bạn đã gửi đánh giá cho sản phẩm này trong đơn hàng này rồi.");
        }

        // Đảm bảo đánh giá mới mặc định không bị ẩn
        review.setIsHidden(false);

        // Lưu đánh giá xuống database
        reviewDAO.save(review);
        recalculateRatingAfterReviewChange(review.getReviewId());
    }

    /**
     * Cập nhật một đánh giá hiện có.
     *
     * @param review đánh giá cần cập nhật
     * @throws SQLException nếu xảy ra lỗi cơ sở dữ liệu
     */
    public void updateReview(Review review) throws SQLException {
        if (review == null) {
            throw new IllegalArgumentException("Dữ liệu đánh giá không được để trống.");
        }
        if (review.getReviewId() <= 0) {
            throw new IllegalArgumentException("Mã đánh giá không hợp lệ.");
        }
        if (review.getCustomerId() <= 0) {
            throw new IllegalArgumentException("Mã khách hàng không hợp lệ.");
        }
        if (review.getOrderItemId() <= 0) {
            throw new IllegalArgumentException("Mã dòng chi tiết đơn hàng không hợp lệ.");
        }
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("Điểm đánh giá sao phải nằm trong khoảng từ 1 đến 5 sao.");
        }
        if (review.getReviewText() != null && review.getReviewText().length() > 1000) {
            throw new IllegalArgumentException("Nội dung đánh giá không được vượt quá 1000 ký tự.");
        }

        reviewDAO.update(review);
        recalculateRatingAfterReviewChange(review.getReviewId());
    }

    /**
     * Xóa một đánh giá và cập nhật lại rating của sản phẩm liên quan.
     *
     * @param reviewId mã đánh giá
     * @throws SQLException nếu xảy ra lỗi cơ sở dữ liệu
     */
    public void deleteReview(int reviewId) throws SQLException {
        if (reviewId <= 0) {
            throw new IllegalArgumentException("Mã đánh giá không hợp lệ.");
        }

        int productId = productDAO.getProductIdByReview(reviewId);
        reviewDAO.delete(reviewId);
        if (productId != -1) {
            productDAO.recalculateRating(productId);
        }
    }

    /**
     * Lấy toàn bộ danh sách đánh giá của sản phẩm (chưa phân trang).
     *
     * @param productId mã sản phẩm
     * @return danh sách đánh giá của sản phẩm
     * @throws SQLException nếu xảy ra lỗi cơ sở dữ liệu
     */
    public List<Review> getReviews(int productId) throws SQLException {
        if (productId <= 0) {
            throw new IllegalArgumentException("Mã sản phẩm không hợp lệ.");
        }
        return reviewDAO.findByProduct(productId);
    }

    /**
     * Lấy danh sách đánh giá của sản phẩm có phân trang và bộ lọc số sao.
     *
     * @param productId    ID của sản phẩm
     * @param ratingFilter Bộ lọc số sao (1-5), nếu null sẽ lấy tất cả mức sao
     * @param page         Trang hiện tại (1-based)
     * @param pageSize     Số lượng đánh giá hiển thị trên mỗi trang
     * @return Đối tượng PagedResultDTO chứa danh sách đánh giá trang hiện tại và các thông tin phân trang
     * @throws SQLException nếu xảy ra lỗi cơ sở dữ liệu
     */
    public PagedResultDTO getReviewsPaginated(int productId, Integer ratingFilter, int page, int pageSize) throws SQLException {
        if (productId <= 0) {
            throw new IllegalArgumentException("Mã sản phẩm không hợp lệ.");
        }
        if (page < 1) {
            page = 1;
        }
        if (pageSize <= 0) {
            pageSize = 5; // Kích thước mặc định cho trang review là 5 để hiển thị cân đối
        }

        // 1. Đếm tổng số lượng đánh giá thỏa mãn điều kiện bộ lọc
        int total = reviewDAO.countByProductAndRating(productId, ratingFilter);

        // 2. Tính toán tổng số trang
        int totalPages = Math.max(1, (int) Math.ceil((double) total / pageSize));

        // 3. Khống chế trang hiện tại không vượt quá tổng số trang
        if (page > totalPages) {
            page = totalPages;
        }

        // 4. Lấy danh sách đánh giá của trang hiện tại
        List<Review> items = reviewDAO.findByProductPaginated(productId, ratingFilter, page, pageSize);

        // 5. Trả về đối tượng PagedResult DTO chuẩn hóa
        return new PagedResultDTO(items, page, totalPages, total, pageSize);
    }

    /**
     * Lấy thống kê số lượng đánh giá theo từng mức sao (1-5★) của sản phẩm.
     *
     * @param productId ID sản phẩm
     * @return Map lưu trữ phân phối sao
     * @throws SQLException nếu xảy ra lỗi cơ sở dữ liệu
     */
    public Map<Integer, Integer> getRatingDistribution(int productId) throws SQLException {
        if (productId <= 0) {
            throw new IllegalArgumentException("Mã sản phẩm không hợp lệ.");
        }
        return reviewDAO.getRatingDistribution(productId);
    }

    /**
     * Kiểm tra xem khách hàng có quyền gửi đánh giá cho chi tiết đơn hàng này hay không.
     * Điều kiện: Khách hàng chưa đánh giá dòng đơn hàng này trước đó.
     *
     * @param customerId  ID khách hàng
     * @param orderItemId ID chi tiết dòng đơn hàng
     * @return true nếu khách hàng có thể đánh giá, ngược lại false
     * @throws SQLException nếu xảy ra lỗi cơ sở dữ liệu
     */
    public boolean canReview(int customerId, int orderItemId) throws SQLException {
        if (customerId <= 0 || orderItemId <= 0) {
            return false;
        }
        return !reviewDAO.existsByCustomerAndItem(customerId, orderItemId);
    }

    /**
     * Lấy toàn bộ danh sách đánh giá cho admin.
     */
    public List<Review> getAllReviewsForAdmin() throws SQLException {
        return reviewDAO.findAllForAdmin();
    }

    public PagedResultDTO getAllReviewsForAdminPaged(int page, int pageSize) throws SQLException {
        int validatedPage = PaginationUtil.validatePage(page);
        int validatedPageSize = PaginationUtil.validatePageSize(pageSize);
        List<Review> reviews = reviewDAO.findAllForAdmin(validatedPage, validatedPageSize);
        int totalCount = reviewDAO.countAllForAdmin();
        return PaginationUtil.buildPagedResult(reviews, validatedPage, validatedPageSize, totalCount);
    }

    /**
     * Duyệt hoặc từ chối một đánh giá theo workflow rõ ràng.
     */
    public void moderateReview(int reviewId, String action) throws SQLException {
        if (reviewId <= 0) {
            throw new IllegalArgumentException("Mã đánh giá không hợp lệ.");
        }
        if (action == null || action.trim().isEmpty()) {
            throw new IllegalArgumentException("Hành động kiểm duyệt không hợp lệ.");
        }

        String normalized = action.trim().toLowerCase();
        if ("approve".equals(normalized) || "show".equals(normalized)) {
            updateReviewVisibility(reviewId, false);
        } else if ("reject".equals(normalized) || "hide".equals(normalized)) {
            updateReviewVisibility(reviewId, true);
        } else {
            throw new IllegalArgumentException("Hành động kiểm duyệt không hợp lệ.");
        }
    }

    /**
     * Cập nhật trạng thái ẩn/hiện của đánh giá.
     */
    public void updateReviewVisibility(int reviewId, boolean isHidden) throws SQLException {
        reviewDAO.updateVisibility(reviewId, isHidden);
        recalculateRatingAfterReviewChange(reviewId);
    }

    private void recalculateRatingAfterReviewChange(int reviewId) throws SQLException {
        int productId = productDAO.getProductIdByReview(reviewId);
        if (productId != -1) {
            productDAO.recalculateRating(productId);
        }
    }
}
