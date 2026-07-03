package servlet.admin.order;

import config.AppConfig;
import model.dto.common.PagedResultDTO;
import service.order.ReviewService;
import model.entity.order.Review;
import util.PaginationUtil;

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
            int page = PaginationUtil.parsePage(req.getParameter("page"));
            int pageSize = AppConfig.PAGE_SIZE_ADMIN;
            PagedResultDTO reviewPage = reviewService.getAllReviewsForAdminPaged(page, pageSize);
            req.setAttribute("reviewList", reviewPage.getItems());
            req.setAttribute("currentPage", reviewPage.getCurrentPage());
            req.setAttribute("totalPages", reviewPage.getTotalPages());
            req.setAttribute("totalItems", reviewPage.getTotalItems());
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
