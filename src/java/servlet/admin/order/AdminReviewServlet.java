package servlet.admin.order;

import service.order.ReviewService;
import model.entity.order.Review;

import util.LoggerUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

@WebServlet("/admin/reviews")
public class AdminReviewServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(AdminReviewServlet.class.getName());

    private final ReviewService reviewService = new ReviewService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            List<Review> reviews = reviewService.getAllReviewsForAdmin();
            req.setAttribute("reviewList", reviews);
            req.getRequestDispatcher("/WEB-INF/jsp/admin/review-management.jsp").forward(req, resp);
        } catch (Exception e) {
            util.ServletUtil.sendPageInternalServerError(
                    req,
                    resp,
                    java.util.logging.Logger.getLogger(AdminReviewServlet.class.getName()),
                    "AdminReviewServlet#doGet",
                    "Lỗi tải danh sách đánh giá",
                    e);
        }
    }
}
