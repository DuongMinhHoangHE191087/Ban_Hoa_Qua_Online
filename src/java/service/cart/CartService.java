package service.cart;

import dao.cart.CartDAO;
import dao.catalog.ProductDAO;
import dao.catalog.ProductVariantDAO;
import dao.shop.PromotionDAO;
import model.dto.product.CartSummaryDTO;
import model.entity.cart.Cart;
import model.entity.cart.CartItem;
import model.entity.catalog.Product;
import model.entity.catalog.ProductVariant;
import model.entity.Promotion;
import util.JsonUtil;
import util.LoggerUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * CartService — Tầng business logic cho nghiệp vụ tương ứng.
 *
 * QUY TẮC:
 *   - Chỉ gọi DAO, không viết SQL ở đây
 *   - Chứa tất cả validation và business rule
 *   - Ném RuntimeException hoặc custom exception cho Servlet xử lý
 *   - Không tương tác trực tiếp với HttpRequest/Response
 *
 * @author fruitmkt-team
 */
public class CartService {

    private static final Logger log = LoggerUtil.getLogger(CartService.class);

    private final CartDAO cartDAO = new CartDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final ProductVariantDAO productVariantDAO = new ProductVariantDAO();

    private String resolveProductName(Product product, String fallbackName) {
        if (product != null && product.getName() != null && !product.getName().trim().isEmpty()) {
            return product.getName().trim();
        }
        if (fallbackName != null && !fallbackName.trim().isEmpty()) {
            return fallbackName.trim();
        }
        return "Sản phẩm này";
    }

    private String validatePurchasableProduct(Product product, String fallbackName) {
        String productName = resolveProductName(product, fallbackName);
        if (product == null) {
            return productName + " hiện không còn tồn tại.";
        }

        String status = product.getStatus();
        if ("DELETED".equals(status)) {
            return productName + " đã bị gỡ khỏi gian hàng.";
        }
        if ("INACTIVE".equals(status)) {
            return productName + " đã ngừng kinh doanh.";
        }
        if ("OUT_OF_SEASON".equals(status) || !product.isInSeason()) {
            return productName + " đã hết mùa. Vui lòng quay lại khi có vụ mới.";
        }
        return null;
    }

    private Product loadProductForVariant(ProductVariant variant) throws SQLException {
        if (variant == null) {
            return null;
        }
        return productDAO.findOneById(variant.getProductId());
    }

    /**
     * Lấy hoặc khởi tạo giỏ hàng cho khách hàng.
     */
    public CartSummaryDTO getCart(int customerId) throws SQLException {
        List<Cart> carts = cartDAO.findByCustomer(customerId);
        int cartId;
        if (carts.isEmpty()) {
            cartId = cartDAO.createForCustomer(customerId);
        } else {
            cartId = carts.get(0).getCartId();
        }

        List<CartItem> items = cartDAO.findItems(cartId);
        long accumulativeSubtotal = 0;
        long accumulativeGrams = 0;

        for (CartItem item : items) {
            BigDecimal price = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
            BigDecimal packagingPriceAdd = item.getPackagingPriceAdd() != null ? item.getPackagingPriceAdd() : BigDecimal.ZERO;
            BigDecimal totalItemUnitPrice = price.add(packagingPriceAdd);
            BigDecimal weight = item.getWeightKg() != null ? item.getWeightKg() : new BigDecimal("1.000");
            
            // 1. Tính toán tiền tệ trên số nguyên Long (VND không lẻ thập phân)
            long unitPrice = totalItemUnitPrice.setScale(0, RoundingMode.HALF_UP).longValue();
            long itemSubtotal = unitPrice * item.getQuantity();
            accumulativeSubtotal += itemSubtotal;

            // 2. Tính toán trọng lượng quy đổi ra Grams (Kg * 1000) để triệt tiêu hoàn toàn sai số dấu phẩy động ở CPU
            long weightGrams = weight.multiply(new BigDecimal("1000")).setScale(0, RoundingMode.HALF_UP).longValue();
            long itemTotalGrams = weightGrams * item.getQuantity();
            accumulativeGrams += itemTotalGrams;
        }

        // 3. Dịch ngược lại sang BigDecimal để lưu trữ hiển thị
        BigDecimal subtotal = new BigDecimal(accumulativeSubtotal).setScale(0, RoundingMode.HALF_UP);
        BigDecimal totalWeight = new BigDecimal(accumulativeGrams).divide(new BigDecimal("1000"), 3, RoundingMode.HALF_UP);

        BigDecimal discountAmount = BigDecimal.ZERO; // Sẽ xử lý sau nếu có voucher
        BigDecimal deliveryFee = BigDecimal.ZERO;    // Tương tự phí ship
        BigDecimal total = subtotal.subtract(discountAmount).add(deliveryFee);
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        return new CartSummaryDTO(items, subtotal, discountAmount, deliveryFee, total, totalWeight);
    }

    /**
     * Thêm sản phẩm vào giỏ hàng. Kiểm tra giới hạn số lượng tồn kho.
     */
    public void addToCart(int customerId, int variantId, int qty) throws SQLException {
        addToCart(customerId, variantId, qty, null);
    }

    public void addToCart(int customerId, int variantId, int qty, Integer packagingId) throws SQLException {
        if (qty <= 0) {
            throw new IllegalArgumentException("Số lượng thêm vào giỏ hàng phải lớn hơn 0.");
        }

        ProductVariant variant = productVariantDAO.findById(variantId);
        if (variant == null || !variant.getIsActive()) {
            throw new IllegalArgumentException("Sản phẩm hoặc biến thể này không tồn tại hoặc đã ngừng kinh doanh.");
        }
        Product product = loadProductForVariant(variant);
        String productError = validatePurchasableProduct(product, null);
        if (productError != null) {
            throw new IllegalArgumentException(productError);
        }

        List<Cart> carts = cartDAO.findByCustomer(customerId);
        int cartId;
        if (carts.isEmpty()) {
            cartId = cartDAO.createForCustomer(customerId);
        } else {
            cartId = carts.get(0).getCartId();
        }

        // Kiểm tra xem đã có sản phẩm này trong giỏ với cùng tùy chọn bao bì chưa
        List<CartItem> items = cartDAO.findItems(cartId);
        int existingQty = 0;
        for (CartItem item : items) {
            if (item.getVariantId() == variantId && 
                ((packagingId == null && item.getPackagingId() == null) || (packagingId != null && packagingId.equals(item.getPackagingId())))) {
                existingQty = item.getQuantity();
                break;
            }
        }

        int totalRequested = existingQty + qty;
        if (totalRequested > variant.getStockQuantity()) {
            throw new IllegalArgumentException(resolveProductName(product, null)
                    + " đã hết số lượng bạn cần mua, hiện chỉ còn " + variant.getStockQuantity() + " sản phẩm.");
        }

        cartDAO.addItem(cartId, variantId, qty, packagingId);
    }

    /**
     * Cập nhật số lượng của một CartItem trong giỏ hàng.
     */
    public void updateQuantity(int customerId, int cartItemId, int qty) throws SQLException {
        if (qty <= 0) {
            throw new IllegalArgumentException("Số lượng sản phẩm phải lớn hơn 0.");
        }

        CartItem item = cartDAO.findItemById(cartItemId);
        if (item == null) {
            throw new IllegalArgumentException("Không tìm thấy sản phẩm này trong giỏ hàng.");
        }

        // Kiểm tra quyền sở hữu giỏ hàng (IDOR Prevention)
        List<Cart> carts = cartDAO.findByCustomer(customerId);
        if (carts.isEmpty() || item.getCartId() != carts.get(0).getCartId()) {
            throw new IllegalArgumentException("Sản phẩm không thuộc giỏ hàng của bạn!");
        }

        ProductVariant variant = productVariantDAO.findById(item.getVariantId());
        if (variant == null || !variant.getIsActive()) {
            throw new IllegalArgumentException("Biến thể sản phẩm này không còn tồn tại hoặc đã ngừng kinh doanh.");
        }
        Product product = loadProductForVariant(variant);
        String productError = validatePurchasableProduct(product, item.getProductName());
        if (productError != null) {
            throw new IllegalArgumentException(productError);
        }

        if (qty > variant.getStockQuantity()) {
            throw new IllegalArgumentException(resolveProductName(product, item.getProductName())
                    + " đã hết số lượng bạn cần mua, hiện chỉ còn " + variant.getStockQuantity() + " sản phẩm.");
        }

        cartDAO.updateItemQuantity(cartItemId, qty);
    }

    /**
     * Xóa một CartItem khỏi giỏ hàng.
     */
    public void removeItem(int customerId, int cartItemId) throws SQLException {
        CartItem item = cartDAO.findItemById(cartItemId);
        if (item == null) {
            return;
        }

        // Kiểm tra quyền sở hữu giỏ hàng (IDOR Prevention)
        List<Cart> carts = cartDAO.findByCustomer(customerId);
        if (carts.isEmpty() || item.getCartId() != carts.get(0).getCartId()) {
            throw new IllegalArgumentException("Sản phẩm không thuộc giỏ hàng của bạn!");
        }

        cartDAO.removeItem(cartItemId);
    }

    /**
     * Thay đổi biến thể của một CartItem trong giỏ hàng.
     * Tự động gộp nếu trùng biến thể có sẵn và giới hạn theo tồn kho tối đa.
     */
    public void changeVariant(int customerId, int cartItemId, int newVariantId) throws SQLException {
        CartItem item = cartDAO.findItemById(cartItemId);
        if (item == null) {
            throw new IllegalArgumentException("Không tìm thấy sản phẩm này trong giỏ hàng.");
        }

        // Kiểm tra quyền sở hữu giỏ hàng (IDOR Prevention)
        List<Cart> carts = cartDAO.findByCustomer(customerId);
        if (carts.isEmpty() || item.getCartId() != carts.get(0).getCartId()) {
            throw new IllegalArgumentException("Sản phẩm không thuộc giỏ hàng của bạn!");
        }

        ProductVariant newVariant = productVariantDAO.findById(newVariantId);
        if (newVariant == null || !newVariant.getIsActive()) {
            throw new IllegalArgumentException("Biến thể mới không tồn tại hoặc đã ngừng kinh doanh.");
        }
        Product newProduct = loadProductForVariant(newVariant);
        String productError = validatePurchasableProduct(newProduct, item.getProductName());
        if (productError != null) {
            throw new IllegalArgumentException(productError);
        }

        if (newVariant.getStockQuantity() <= 0) {
            throw new IllegalArgumentException("Sản phẩm này đã hết số lượng bạn cần mua, hiện chỉ còn 0 sản phẩm.");
        }

        int cartId = item.getCartId();
        List<CartItem> items = cartDAO.findItems(cartId);
        CartItem existingNewVariantItem = null;
        for (CartItem ci : items) {
            if (ci.getVariantId() == newVariantId && ci.getCartItemId() != cartItemId) {
                existingNewVariantItem = ci;
                break;
            }
        }

        if (existingNewVariantItem != null) {
            int mergedQty = item.getQuantity() + existingNewVariantItem.getQuantity();
            if (mergedQty > newVariant.getStockQuantity()) {
                mergedQty = newVariant.getStockQuantity();
            }
            cartDAO.updateItemQuantity(existingNewVariantItem.getCartItemId(), mergedQty);
            cartDAO.removeItem(cartItemId);
        } else {
            int qty = item.getQuantity();
            if (qty > newVariant.getStockQuantity()) {
                qty = newVariant.getStockQuantity();
                cartDAO.updateItemQuantity(cartItemId, qty);
            }
            cartDAO.updateItemVariant(cartItemId, newVariantId);
        }
    }

    /**
     * Đồng bộ giỏ hàng khách vãng lai khi đăng nhập (Gộp giỏ hàng).
     */
    public void syncGuestCart(int customerId, String guestCartJson) throws SQLException {
        if (guestCartJson == null || guestCartJson.trim().isEmpty()) {
            return;
        }

        try {
            // guestCartJson có cấu trúc: [{"variantId": 1, "quantity": 2}, ...]
            // Sử dụng JsonUtil để parse thành List of Map
            List<Map<String, Object>> guestItems = JsonUtil.fromJson(guestCartJson, List.class);
            if (guestItems == null || guestItems.isEmpty()) {
                return;
            }

            for (Map<String, Object> gItem : guestItems) {
                if (gItem.get("variantId") == null || gItem.get("quantity") == null) {
                    continue;
                }
                int variantId = ((Number) gItem.get("variantId")).intValue();
                int qty = ((Number) gItem.get("quantity")).intValue();
                if (qty <= 0) {
                    continue;
                }

                try {
                    addToCart(customerId, variantId, qty);
                } catch (IllegalArgumentException e) {
                    LoggerUtil.warn(log, "[CartSync] Không thể gộp variantId %d: %s", variantId, e.getMessage());
                }
            }
        } catch (Exception e) {
            LoggerUtil.warn(log, "[CartSync] Lỗi giải mã dữ liệu giỏ hàng khách", e);
        }
    }

    /**
     * Ghi đè đồng bộ toàn bộ giỏ hàng từ Local Storage gửi lên (Beacon Sync).
     */
    public void syncOnUnload(int customerId, String guestCartJson) throws SQLException {
        if (guestCartJson == null) return;

        List<Cart> carts = cartDAO.findByCustomer(customerId);
        int cartId;
        if (carts.isEmpty()) {
            cartId = cartDAO.createForCustomer(customerId);
        } else {
            cartId = carts.get(0).getCartId();
        }

        try {
            List<Map<String, Object>> parsedItems = JsonUtil.fromJson(guestCartJson, List.class);
            List<CartItem> itemsToReplace = new ArrayList<>();
            
            if (parsedItems != null) {
                // [FIX] Giới hạn tối đa 100 items để tránh DoS
                int limit = Math.min(parsedItems.size(), 100);
                for (int i = 0; i < limit; i++) {
                    Map<String, Object> pItem = parsedItems.get(i);
                    if (pItem.get("variantId") == null || pItem.get("quantity") == null) {
                        continue;
                    }
                    int variantId = ((Number) pItem.get("variantId")).intValue();
                    int qty = ((Number) pItem.get("quantity")).intValue();
                    if (qty <= 0) {
                        continue;
                    }
                    
                    // Validate stock nhanh
                    ProductVariant variant = productVariantDAO.findById(variantId);
                    if (variant == null || !variant.getIsActive()) {
                        continue;
                    }
                    Product product = loadProductForVariant(variant);
                    if (validatePurchasableProduct(product, null) != null) {
                        LoggerUtil.warn(log, "[UnloadSync] Bỏ qua variantId %d vì sản phẩm không còn khả dụng", variantId);
                        continue;
                    }
                    int stock = variant.getStockQuantity();
                    if (stock > 0) {
                        CartItem ci = new CartItem();
                        ci.setVariantId(variantId);
                        ci.setQuantity(Math.min(qty, stock));
                        itemsToReplace.add(ci);
                    }
                }
            }
            
            cartDAO.replaceCartItems(cartId, itemsToReplace);
        } catch (Exception e) {
            LoggerUtil.warn(log, "[UnloadSync] Lỗi ghi đè giỏ hàng", e);
        }
    }

    /**
     * Kiểm tra tồn kho trước khi Checkout (Thanh toán) chống xung đột đồng thời.
     * Trả về danh sách các thông báo lỗi nếu có sản phẩm hết hàng hoặc không đủ tồn kho.
     */
    public List<String> checkCartStockBeforeCheckout(int customerId) throws SQLException {
        return checkCartStockBeforeCheckout(customerId, null);
    }

    /**
     * Kiểm tra tồn kho trước khi Checkout (Thanh toán) cho danh sách variant được chọn.
     * Trả về danh sách các thông báo lỗi nếu có sản phẩm hết hàng hoặc không đủ tồn kho.
     */
    public List<String> checkCartStockBeforeCheckout(int customerId, List<Integer> variantIds) throws SQLException {
        List<String> errors = new ArrayList<>();
        List<Cart> carts = cartDAO.findByCustomer(customerId);
        if (carts.isEmpty()) {
            return errors;
        }

        int cartId = carts.get(0).getCartId();
        List<CartItem> items = cartDAO.findItems(cartId);
        if (items.isEmpty()) {
            return errors;
        }

        Set<Integer> selectedIds = null;
        if (variantIds != null) {
            selectedIds = new HashSet<>();
            for (Integer variantId : variantIds) {
                if (variantId != null && variantId > 0) {
                    selectedIds.add(variantId);
                }
            }
            if (selectedIds.isEmpty()) {
                errors.add("Vui lòng chọn ít nhất một sản phẩm để thanh toán.");
                return errors;
            }
        }

        boolean matchedAnySelectedItem = selectedIds == null;
        for (CartItem item : items) {
            if (selectedIds != null && !selectedIds.contains(item.getVariantId())) {
                continue;
            }
            matchedAnySelectedItem = true;
            // Lấy trực tiếp stock từ DB để có giá trị mới nhất
            ProductVariant variant = productVariantDAO.findById(item.getVariantId());
            if (variant == null || !variant.getIsActive()) {
                errors.add("Sản phẩm '" + item.getProductName() + "' (" + item.getVariantLabel() + ") hiện không còn bán.");
                continue;
            }
            Product product = loadProductForVariant(variant);
            String productError = validatePurchasableProduct(product, item.getProductName());
            if (productError != null) {
                errors.add(productError + " (" + item.getVariantLabel() + ")");
                continue;
            } else if (item.getQuantity() > variant.getStockQuantity()) {
                errors.add("Sản phẩm '" + item.getProductName() + "' (" + item.getVariantLabel() + ") đã hết số lượng bạn cần mua, hiện chỉ còn " + variant.getStockQuantity() + " sản phẩm.");
            }
        }
        if (!matchedAnySelectedItem) {
            errors.add("Không tìm thấy sản phẩm nào đã chọn trong giỏ hàng.");
        }
        return errors;
    }

    /**
     * Xóa sạch toàn bộ sản phẩm trong giỏ hàng.
     */
    public void clearCart(int customerId) throws SQLException {
        List<Cart> carts = cartDAO.findByCustomer(customerId);
        if (!carts.isEmpty()) {
            cartDAO.clearCart(carts.get(0).getCartId());
        }
    }

    /**
     * Áp dụng voucher cho giỏ hàng — validate coupon và tính discount
     * Lưu ý: Cart table không có field `applied_promotion_id`, nên discount được lưu trong session/request attribute.
     *
     * @param customerId ID khách hàng
     * @param code Mã coupon
     * @return Số tiền giảm (BigDecimal), hoặc BigDecimal.ZERO nếu invalid
     * @throws SQLException nếu lỗi DB
     */
    public BigDecimal applyVoucher(int customerId, String code) throws SQLException {
        if (customerId <= 0 || code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID hoặc mã voucher không hợp lệ.");
        }

        PromotionDAO promotionDAO = new PromotionDAO();
        Promotion promo = promotionDAO.findByCode(code.trim());

        if (promo == null) {
            throw new IllegalArgumentException("Mã voucher không tồn tại hoặc đã hết hạn.");
        }

        // Validate expire time
        if (promo.getValidUntil() != null && LocalDateTime.now().isAfter(promo.getValidUntil())) {
            throw new IllegalArgumentException("Mã voucher đã hết hạn.");
        }

        CartSummaryDTO cartSummary = getCart(customerId);
        BigDecimal subtotal = cartSummary.getSubtotal();

        // Validate minimum order value
        if (promo.getMinOrderValue() != null && subtotal.compareTo(promo.getMinOrderValue()) < 0) {
            throw new IllegalArgumentException("Mã voucher chỉ áp dụng cho đơn hàng từ " + promo.getMinOrderValue() + "đ trở lên.");
        }

        // Tính discount
        BigDecimal discount = BigDecimal.ZERO;
        if ("PERCENT".equalsIgnoreCase(promo.getDiscountType())) {
            discount = subtotal.multiply(promo.getDiscountValue()).divide(new BigDecimal("100"), 2, RoundingMode.DOWN);
        } else if ("FIXED".equalsIgnoreCase(promo.getDiscountType())) {
            discount = promo.getDiscountValue();
        }

        // Cap discount với max value nếu có
        if (promo.getDiscountMax() != null && discount.compareTo(promo.getDiscountMax()) > 0) {
            discount = promo.getDiscountMax();
        }

        return discount.max(BigDecimal.ZERO);
    }
}
