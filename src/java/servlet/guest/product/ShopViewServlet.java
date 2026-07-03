package servlet.guest.product;
import dao.shop.PromotionDAO;

import config.AppConfig;
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
            int parsedId = Integer.parseInt(idStr.trim());
            model.entity.auth.User currentUser = (model.entity.auth.User) req.getSession().getAttribute(AppConfig.SESSION_USER);
            boolean isAdminPreview = currentUser != null && AppConfig.ROLE_ADMIN.equals(currentUser.getRole());
            // Ưu tiên tìm theo userId của chủ cửa hàng (owner_id) trước vì các liên kết từ giỏ hàng/đơn hàng truyền owner_id/userId
            ShopProfile profile = shopService.getShopByUserId(parsedId);
            if (profile == null || (!isAdminPreview && !"APPROVED".equals(profile.getApprovalStatus()))) {
                // Nếu không có hoặc chưa duyệt theo userId, thử tìm theo profileId
                ShopProfile profileById = shopService.getShopById(parsedId);
                if (profileById != null && (isAdminPreview || "APPROVED".equals(profileById.getApprovalStatus()))) {
                    profile = profileById;
                }
            }
            if (profile == null || (!isAdminPreview && !"APPROVED".equals(profile.getApprovalStatus()))) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy gian hàng này hoặc gian hàng chưa được duyệt.");
                return;
            }

            req.setAttribute("shopProfile", profile);
            req.setAttribute("isAdminPreview", isAdminPreview);

            // Tải danh sách sản phẩm của Shop
            List<Product> products = isAdminPreview
                    ? productDAO.findByOwner(profile.getUserId())
                    : new ArrayList<>(productDAO.findActiveByOwner(profile.getUserId()).values());
            List<Integer> productIds = new ArrayList<>();
            for (Product product : products) {
                productIds.add(product.getProductId());
            }
            Map<Integer, ProductImage> primaryImages = productIds.isEmpty()
                    ? new HashMap<>()
                    : productImageDAO.findPrimaryByProductIds(productIds);
            Map<Integer, List<ProductVariant>> variantsByProduct = productIds.isEmpty()
                    ? new HashMap<>()
                    : productVariantDAO.findByProductIds(productIds);
            Map<Integer, Promotion> promotionsByProduct = productIds.isEmpty()
                    ? new HashMap<>()
                    : promotionDAO.findActivePromotionsByProductIds(productIds);
            List<Map<String, Object>> mappedProducts = new ArrayList<>();

            for (Product p : products) {
                mappedProducts.add(buildShopProductCard(req, p,
                        primaryImages.get(p.getProductId()),
                        variantsByProduct.get(p.getProductId()),
                        promotionsByProduct.get(p.getProductId())));
            }

            List<Promotion> promotions = promotionDAO.findShopActivePromotions(profile.getUserId());
            req.setAttribute("promotions", promotions);
            req.setAttribute("products", mappedProducts);
            req.getRequestDispatcher("/WEB-INF/jsp/shop/view.jsp").forward(req, resp);

        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID cửa hàng không đúng định dạng");
        } catch (SQLException e) {
            util.ServletUtil.sendPageInternalServerError(
                    req,
                    resp,
                    java.util.logging.Logger.getLogger(ShopViewServlet.class.getName()),
                    "ShopViewServlet#doGet",
                    "Lỗi kết nối cơ sở dữ liệu",
                    e);
        }
    }

    private Map<String, Object> buildShopProductCard(HttpServletRequest req, Product product,
                                                     ProductImage primaryImage,
                                                     List<ProductVariant> variants,
                                                     Promotion promo) {
        Map<String, Object> item = new HashMap<>();
        item.put("productId", product.getProductId());
        item.put("name", product.getName());
        item.put("description", product.getDescription());
        item.put("rating", product.getRating() != null ? product.getRating() : BigDecimal.ZERO);
        item.put("soldQuantity", product.getSoldQuantity());
        item.put("image", resolveImagePath(req, primaryImage));

        BigDecimal basePrice = new BigDecimal("45000");
        String unit = "kg";
        if (variants != null && !variants.isEmpty()) {
            ProductVariant cheapestVariant = variants.get(0);
            basePrice = cheapestVariant.getPrice();
            unit = cheapestVariant.getVariantLabel();
        }

        item.put("unit", unit);
        if (promo != null) {
            BigDecimal finalPrice = basePrice;
            BigDecimal discountValue = promo.getDiscountValue();
            if ("PERCENT".equals(promo.getDiscountType())) {
                BigDecimal discountAmount = basePrice.multiply(discountValue).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                finalPrice = basePrice.subtract(discountAmount);
                item.put("discountPercent", discountValue.intValue());
            } else if ("FIXED".equals(promo.getDiscountType())) {
                finalPrice = basePrice.subtract(discountValue);
                item.put("discountPercent", 15);
            }
            if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
                finalPrice = BigDecimal.ZERO;
            }
            item.put("price", finalPrice);
            item.put("oldPrice", basePrice);
        } else {
            item.put("price", basePrice);
        }
        return item;
    }

    private String resolveImagePath(HttpServletRequest req, ProductImage image) {
        String imagePath = null;
        if (image != null && image.getFilePath() != null && !image.getFilePath().trim().isEmpty()) {
            imagePath = image.getFilePath().trim().replace('\\', '/');
        }
        if (imagePath == null) {
            return req.getContextPath() + "/assets/img/placeholder.png";
        }
        if (!imagePath.startsWith("http://") && !imagePath.startsWith("https://")) {
            if (!imagePath.startsWith("/")) {
                imagePath = "/" + imagePath;
            }
            imagePath = req.getContextPath() + imagePath;
        }
        return imagePath;
    }
}
