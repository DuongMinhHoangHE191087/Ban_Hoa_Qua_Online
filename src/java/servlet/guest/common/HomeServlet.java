package servlet.guest.common;
import dao.shop.PromotionDAO;

import dao.catalog.CategoryDAO;
import dao.catalog.ProductDAO;
import dao.catalog.ProductImageDAO;
import dao.catalog.ProductVariantDAO;

import model.entity.catalog.Category;
import model.entity.catalog.Product;
import model.entity.catalog.ProductImage;
import model.entity.catalog.ProductVariant;
import model.entity.Promotion;
import util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HomeServlet — Controller cho chức năng: Trang chủ hiển thị danh mục, sản phẩm
 * nổi bật, và Flash Sale.
 * 
 * URL: /home
 * GET : Trang chủ: sản phẩm nổi bật, danh mục (kết nối DB hoặc tự động nạp Mock
 * Data Việt Nam)
 *
 * @author fruitmkt-team
 */
@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final ProductImageDAO productImageDAO = new ProductImageDAO();
    private final ProductVariantDAO productVariantDAO = new ProductVariantDAO();
    private final PromotionDAO promotionDAO = new PromotionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // 1. Lọc tham số tìm kiếm & danh mục từ Request
        String keyword = req.getParameter("keyword");
        String categoryIdParam = req.getParameter("categoryId");
        Integer categoryId = null;
        if (categoryIdParam != null && !categoryIdParam.trim().isEmpty()) {
            try {
                categoryId = Integer.parseInt(categoryIdParam);
            } catch (NumberFormatException e) {
                // Bỏ qua định dạng không hợp lệ
            }
        }

        List<Category> categoriesList = new ArrayList<>();
        List<Map<String, Object>> flashSaleProducts = new ArrayList<>();
        List<Map<String, Object>> seasonalProducts = new ArrayList<>();
        List<Map<String, Object>> organicProducts = new ArrayList<>();
        List<Map<String, Object>> importedProducts = new ArrayList<>();
        List<Map<String, Object>> normalProducts = new ArrayList<>();
        boolean isSearchOrFilterActive = (keyword != null && !keyword.trim().isEmpty()) || categoryId != null;

        int page = 1;
        int totalPages = 1;
        int totalProducts = 0;

        try {
            // 2. Lấy danh sách Categories từ Database thực tế
            categoriesList = categoryDAO.findAllActive();

            // 3. Lấy tổng số sản phẩm để tính số trang hỗ trợ phân trang
            totalProducts = productDAO.countSearch(keyword, categoryId, null, null);
            int pageSize = 8; // 8 sản phẩm mỗi trang giúp bố cục gọn gàng, cân đối
            totalPages = (int) Math.ceil((double) totalProducts / pageSize);
            if (totalPages < 1) totalPages = 1;

            String pageParam = req.getParameter("page");
            if (pageParam != null && !pageParam.trim().isEmpty()) {
                try {
                    page = Integer.parseInt(pageParam);
                    if (page < 1) page = 1;
                    if (page > totalPages) page = totalPages;
                } catch (NumberFormatException e) {
                    // Giữ trang mặc định
                }
            }

            // 2a. Lấy sản phẩm Flash Sale thực tế hoạt động độc lập (không phụ thuộc trang hiện tại)
            flashSaleProducts = productDAO.findFlashSaleProductsOptimized(req.getContextPath());

            // 2b. Lấy 10 sản phẩm bán chạy nhất (Best Sellers) cho slider
            List<Map<String, Object>> bestSellersProducts = productDAO.findBestSellersOptimized(10, req.getContextPath());
            req.setAttribute("bestSellersProducts", bestSellersProducts);

            // 2c. Lấy 10 sản phẩm theo mùa (Seasonal Products) cho slider
            seasonalProducts = productDAO.findSeasonalProductsOptimized(10, req.getContextPath());
            req.setAttribute("seasonalProducts", seasonalProducts);

            // 2d. Lấy 10 sản phẩm hữu cơ (Organic Products) cho slider
            organicProducts = productDAO.findOrganicProductsOptimized(10, req.getContextPath());
            req.setAttribute("organicProducts", organicProducts);

            // 2e. Lấy 10 sản phẩm nhập khẩu (Imported Products) cho slider
            importedProducts = productDAO.findImportedProductsOptimized(10, req.getContextPath());
            req.setAttribute("importedProducts", importedProducts);

            // 4. Lấy sản phẩm từ Database thực tế theo trang hiện tại (lưới catalog chính) - Tối ưu tránh N+1
            normalProducts = productDAO.searchProductsOptimized(keyword, categoryId, page, pageSize, req.getContextPath());

        } catch (SQLException e) {
            req.getServletContext().log("Không kết nối được database hoặc truy vấn lỗi: " + e.getMessage(), e);
        }

        // 4. Gán dữ liệu vào Request scope
        req.setAttribute("categories", categoriesList);
        req.setAttribute("flashSaleProducts", flashSaleProducts);
        req.setAttribute("seasonalProducts", seasonalProducts);
        req.setAttribute("organicProducts", organicProducts);
        req.setAttribute("importedProducts", importedProducts);
        req.setAttribute("normalProducts", normalProducts);
        req.setAttribute("keyword", keyword);
        req.setAttribute("selectedCategoryId", categoryId);
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("totalProducts", totalProducts);

        // 5. Forward đến JSP Trang chủ an toàn sau WEB-INF
        req.getServletContext().getRequestDispatcher("/WEB-INF/jsp/guest/home.jsp").forward(req, resp);
    }

    /**
     * Ánh xạ productId sang một URL ảnh Unsplash cao cấp, rực rỡ và ổn định 100%
     * Tránh hoàn toàn lỗi 404 tài nguyên tĩnh trên các máy local của người dùng.
     */

}
