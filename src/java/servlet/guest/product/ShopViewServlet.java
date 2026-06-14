package servlet.guest.product;
import dao.shop.PromotionDAO;

import model.entity.shop.ShopProfile;
import model.entity.catalog.Product;
import model.entity.catalog.ProductImage;
import model.entity.catalog.ProductVariant;
import model.entity.Promotion;
import service.shop.ShopService;
import dao.catalog.ProductDAO;
import dao.catalog.ProductImageDAO;
import dao.catalog.ProductVariantDAO;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ShopViewServlet — Xem trang thông tin công khai của Shop dành cho khách hàng.
 * URL: /shop-view
 */
@WebServlet("/shop-view")
public class ShopViewServlet extends HttpServlet {

    private final ShopService shopService = new ShopService();
    private final ProductDAO productDAO = new ProductDAO();
    private final ProductImageDAO productImageDAO = new ProductImageDAO();
    private final ProductVariantDAO productVariantDAO = new ProductVariantDAO();
    private final PromotionDAO promotionDAO = new PromotionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        String idStr = req.getParameter("id");
        if (idStr == null || idStr.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu tham số ID cửa hàng");
            return;
        }

        try {
            int profileId = Integer.parseInt(idStr.trim());
            ShopProfile profile = shopService.getShopById(profileId);
            if (profile == null || !"APPROVED".equals(profile.getApprovalStatus())) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy gian hàng này hoặc gian hàng chưa được duyệt.");
                return;
            }

            req.setAttribute("shopProfile", profile);

            // Tải danh sách sản phẩm của Shop
            List<Product> products = productDAO.findByOwner(profile.getUserId());
            List<Map<String, Object>> mappedProducts = new ArrayList<>();

            for (Product p : products) {
                if (!"ACTIVE".equals(p.getStatus())) continue;

                Map<String, Object> item = new HashMap<>();
                item.put("productId", p.getProductId());
                item.put("name", p.getName());
                item.put("description", p.getDescription());
                item.put("rating", p.getRating() != null ? p.getRating() : new BigDecimal("4.8"));
                item.put("soldQuantity", p.getSoldQuantity());

                // Lấy ảnh chính thực tế
                ProductImage pi = productImageDAO.findPrimary(p.getProductId());
                String imagePath = null;
                if (pi != null && pi.getFilePath() != null && !pi.getFilePath().trim().isEmpty()) {
                    imagePath = pi.getFilePath().trim().replace('\\', '/');
                }
                if (imagePath == null) {
                    imagePath = req.getContextPath() + "/assets/img/placeholder.png";
                } else if (!imagePath.startsWith("http://") && !imagePath.startsWith("https://")) {
                    if (!imagePath.startsWith("/")) {
                        imagePath = "/" + imagePath;
                    }
                    imagePath = req.getContextPath() + imagePath;
                }
                item.put("image", imagePath);

                // Lấy giá và đơn vị
                List<ProductVariant> variants = productVariantDAO.findByProduct(p.getProductId());
                BigDecimal basePrice = new BigDecimal("45000");
                String unit = "kg";
                if (variants != null && !variants.isEmpty()) {
                    ProductVariant cheapestVariant = variants.get(0);
                    basePrice = cheapestVariant.getPrice();
                    unit = cheapestVariant.getVariantLabel();
                }
                item.put("unit", unit);

                // Kiểm tra khuyến mãi đang hoạt động
                List<Promotion> activePromos = promotionDAO.findActivePromotionsByProduct(p.getProductId());
                if (activePromos != null && !activePromos.isEmpty()) {
                    Promotion promo = activePromos.get(0);
                    BigDecimal discountValue = promo.getDiscountValue();
                    BigDecimal finalPrice = basePrice;
                    if ("PERCENT".equals(promo.getDiscountType())) {
                        BigDecimal discountAmount = basePrice.multiply(discountValue).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                        finalPrice = basePrice.subtract(discountAmount);
                        item.put("discountPercent", discountValue.intValue());
                    } else if ("FIXED".equals(promo.getDiscountType())) {
                        finalPrice = basePrice.subtract(discountValue);
                        item.put("discountPercent", 15); // Default badge display
                    }
                    item.put("price", finalPrice);
                    item.put("oldPrice", basePrice);
                } else {
                    item.put("price", basePrice);
                }
                mappedProducts.add(item);
            }

            List<Promotion> promotions = promotionDAO.findByOwner(profile.getUserId());
            req.setAttribute("promotions", promotions);
            req.setAttribute("products", mappedProducts);
            req.getRequestDispatcher("/WEB-INF/jsp/shop/view.jsp").forward(req, resp);

        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID cửa hàng không đúng định dạng");
        } catch (SQLException e) {
            getServletContext().log("Lỗi tải thông tin shop: " + e.getMessage(), e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi kết nối cơ sở dữ liệu");
        }
    }
}
