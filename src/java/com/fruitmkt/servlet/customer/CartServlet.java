package com.fruitmkt.servlet.customer;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.model.dto.CartSummaryDTO;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.service.CartService;
import com.fruitmkt.util.JsonUtil;
import com.fruitmkt.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CartServlet — Controller cho chức năng: Giỏ hàng
 *
 * URL: /cart
 * GET : Giỏ hàng: hiển thị items và tổng tiền
 * POST: Thêm/sửa/xóa item, sync guest cart, check stock
 *
 * @author fruitmkt-team
 */
@WebServlet("/cart")
public class CartServlet extends HttpServlet {

    private final CartService cartService = new CartService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        HttpSession session = req.getSession();
        User user = SessionUtil.getCurrentUser(session);

        String format = req.getParameter("format");
        boolean isJson = "json".equals(format) || "XMLHttpRequest".equals(req.getHeader("X-Requested-With"));

        try {
            if (user != null) {
                // Đã đăng nhập -> lấy dữ liệu giỏ hàng từ database
                CartSummaryDTO cartSummary = cartService.getCart(user.getUserId());
                
                if (isJson) {
                    JsonUtil.writeJson(resp, Map.of("success", true, "cartSummary", cartSummary, "isLoggedIn", true));
                } else {
                    req.setAttribute("cartSummary", cartSummary);
                    req.getRequestDispatcher("/WEB-INF/jsp/customer/cart.jsp").forward(req, resp);
                }
            } else {
                // Khách vãng lai -> Dữ liệu giỏ hàng sẽ được Client-side JS tự render từ Local Storage
                if (isJson) {
                    JsonUtil.writeJson(resp, Map.of("success", true, "isLoggedIn", false));
                } else {
                    req.setAttribute("cartSummary", null);
                    req.getRequestDispatcher("/WEB-INF/jsp/customer/cart.jsp").forward(req, resp);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (isJson) {
                JsonUtil.writeJson(resp, Map.of("success", false, "error", "Lỗi kết nối cơ sở dữ liệu."));
            } else {
                SessionUtil.flashError(session, "Không thể tải giỏ hàng của bạn lúc này. Vui lòng thử lại sau.");
                resp.sendRedirect(req.getContextPath() + "/home");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        HttpSession session = req.getSession();
        User user = SessionUtil.getCurrentUser(session);
        String action = req.getParameter("action");

        if (action == null) {
            JsonUtil.writeJson(resp, Map.of("success", false, "error", "Yêu cầu không hợp lệ. Thiếu action."));
            return;
        }

        try {
            switch (action) {
                case "add": {
                    // Thêm sản phẩm vào giỏ
                    if (user == null) {
                        JsonUtil.writeJson(resp, Map.of("success", true, "message", "Đã thêm vào giỏ hàng cục bộ."));
                        return;
                    }

                    int variantId = Integer.parseInt(req.getParameter("variantId"));
                    int quantity = Integer.parseInt(req.getParameter("quantity"));
                    String packagingIdStr = req.getParameter("packagingId");
                    Integer packagingId = (packagingIdStr != null && !packagingIdStr.trim().isEmpty()) 
                        ? Integer.parseInt(packagingIdStr.trim()) 
                        : null;

                    cartService.addToCart(user.getUserId(), variantId, quantity, packagingId);
                    CartSummaryDTO updatedSummary = cartService.getCart(user.getUserId());
                    JsonUtil.writeJson(resp, Map.of("success", true, "message", "Đã thêm vào giỏ hàng thành công.", "cartSummary", updatedSummary));
                    break;
                }
                case "update": {
                    // Cập nhật số lượng
                    if (user == null) {
                        JsonUtil.writeJson(resp, Map.of("success", true, "message", "Đã cập nhật giỏ hàng cục bộ."));
                        return;
                    }

                    int cartItemId = Integer.parseInt(req.getParameter("cartItemId"));
                    int quantity = Integer.parseInt(req.getParameter("quantity"));

                    cartService.updateQuantity(user.getUserId(), cartItemId, quantity);
                    CartSummaryDTO updatedSummary = cartService.getCart(user.getUserId());
                    JsonUtil.writeJson(resp, Map.of("success", true, "message", "Cập nhật thành công.", "cartSummary", updatedSummary));
                    break;
                }
                case "remove": {
                    // Xóa sản phẩm
                    if (user == null) {
                        JsonUtil.writeJson(resp, Map.of("success", true, "message", "Đã xóa khỏi giỏ hàng cục bộ."));
                        return;
                    }

                    int cartItemId = Integer.parseInt(req.getParameter("cartItemId"));

                    cartService.removeItem(user.getUserId(), cartItemId);
                    CartSummaryDTO updatedSummary = cartService.getCart(user.getUserId());
                    JsonUtil.writeJson(resp, Map.of("success", true, "message", "Đã xóa sản phẩm khỏi giỏ hàng.", "cartSummary", updatedSummary));
                    break;
                }
                case "changeVariant": {
                    if (user == null) {
                        JsonUtil.writeJson(resp, Map.of("success", true, "message", "Đã cập nhật biến thể giỏ hàng cục bộ."));
                        return;
                    }

                    int cartItemId = Integer.parseInt(req.getParameter("cartItemId"));
                    int newVariantId = Integer.parseInt(req.getParameter("newVariantId"));

                    cartService.changeVariant(user.getUserId(), cartItemId, newVariantId);
                    CartSummaryDTO updatedSummary = cartService.getCart(user.getUserId());
                    JsonUtil.writeJson(resp, Map.of("success", true, "message", "Đã cập nhật biến thể thành công.", "cartSummary", updatedSummary));
                    break;
                }
                case "sync": {
                    // Đồng bộ gộp giỏ hàng guest khi đăng nhập
                    if (user == null) {
                        JsonUtil.writeJson(resp, Map.of("success", false, "error", "Chưa đăng nhập."));
                        return;
                    }

                    String guestCartJson = req.getParameter("guestCart");
                    if (guestCartJson != null && !guestCartJson.trim().isEmpty()) {
                        cartService.syncGuestCart(user.getUserId(), guestCartJson);
                    }
                    
                    CartSummaryDTO updatedSummary = cartService.getCart(user.getUserId());
                    JsonUtil.writeJson(resp, Map.of("success", true, "message", "Đồng bộ thành công.", "cartSummary", updatedSummary));
                    break;
                }
                case "syncOnUnload": {
                    // Nhận Beacon API đồng bộ ghi đè khi tắt tab
                    if (user == null) {
                        JsonUtil.writeJson(resp, Map.of("success", false, "error", "Chưa đăng nhập."));
                        return;
                    }

                    String bodyJson = readRequestBody(req);
                    Map<String, Object> parsedBody = JsonUtil.fromJson(bodyJson, Map.class);
                    if (parsedBody != null && parsedBody.containsKey("items")) {
                        String itemsJson = JsonUtil.toJson(parsedBody.get("items"));
                        cartService.syncOnUnload(user.getUserId(), itemsJson);
                    }
                    
                    JsonUtil.writeJson(resp, Map.of("success", true));
                    break;
                }
                case "checkStock": {
                    // Kiểm tra tồn kho trước khi thanh toán
                    if (user == null) {
                        JsonUtil.writeJson(resp, Map.of("success", false, "error", "Bạn vui lòng đăng nhập để tiến hành thanh toán."));
                        return;
                    }

                    List<Integer> variantIds = parseVariantIds(req.getParameter("variantIds"));
                    List<String> errors = cartService.checkCartStockBeforeCheckout(user.getUserId(), variantIds);
                    if (errors.isEmpty()) {
                        JsonUtil.writeJson(resp, Map.of("success", true));
                    } else {
                        JsonUtil.writeJson(resp, Map.of("success", false, "errors", errors));
                    }
                    break;
                }
                default:
                    JsonUtil.writeJson(resp, Map.of("success", false, "error", "Hành động không được hỗ trợ."));
                    break;
            }
        } catch (IllegalArgumentException e) {
            JsonUtil.writeJson(resp, Map.of("success", false, "error", e.getMessage(), "errorCode", "out_of_stock"));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeJson(resp, Map.of("success", false, "error", "Lỗi hệ thống khi xử lý giỏ hàng: " + e.getMessage()));
        }
    }

    private List<Integer> parseVariantIds(String variantIdsParam) {
        List<Integer> variantIds = new ArrayList<>();
        if (variantIdsParam == null || variantIdsParam.trim().isEmpty()) {
            return variantIds;
        }
        for (String part : variantIdsParam.split(",")) {
            try {
                variantIds.add(Integer.parseInt(part.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        return variantIds;
    }

    private String readRequestBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        try (java.io.BufferedReader reader = req.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}
