package servlet.shop.product;
import service.catalog.InventoryService;

import config.AppConfig;
import util.SessionUtil;

import dao.catalog.ProductDAO;
import dao.catalog.ProductVariantDAO;
import model.entity.catalog.Product;
import model.entity.catalog.ProductVariant;
import model.entity.catalog.InventoryLog;
import model.entity.auth.User;

import util.LoggerUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * InventoryServlet — Controller for Restock Management and inventory logs.
 * URL: /shop/inventory
 */
@WebServlet("/shop/inventory")
public class InventoryServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(InventoryServlet.class.getName());

    private final InventoryService inventoryService = new InventoryService();
    private final ProductDAO productDAO = new ProductDAO();
    private final ProductVariantDAO productVariantDAO = new ProductVariantDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        HttpSession session = req.getSession();
        User currentUser = SessionUtil.getCurrentUser(session);
        if (currentUser == null || !AppConfig.ROLE_SHOP_OWNER.equals(currentUser.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        List<Map<String, Object>> variantsWithProduct = new ArrayList<>();
        List<InventoryLog> history = new ArrayList<>();
        String errorMsg = null;
        try {
            // 1. Fetch variants belonging to products of this shop owner
            List<Product> products = productDAO.findByOwner(currentUser.getUserId());
            for (Product p : products) {
                List<ProductVariant> variants = productVariantDAO.findByProduct(p.getProductId());
                for (ProductVariant v : variants) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("productId", p.getProductId());
                    map.put("productName", p.getName());
                    map.put("variantId", v.getVariantId());
                    map.put("variantLabel", v.getVariantLabel());
                    map.put("stockQuantity", v.getStockQuantity());
                    map.put("sku", v.getSku());
                    variantsWithProduct.add(map);
                }
            }

            // 2. Fetch past restock history logs
            history = inventoryService.getRestockHistory(currentUser.getUserId());

        } catch (SQLException e) {
            LoggerUtil.error(log, "Không thể tải danh sách sản phẩm hoặc lịch sử nhập kho", e);
            errorMsg = "Không thể tải danh sách sản phẩm hoặc lịch sử: " + e.getMessage();
        }

        if (errorMsg != null) {
            req.setAttribute("inventoryError", errorMsg);
        }

        // 3. Set request attributes
        req.setAttribute("variants", variantsWithProduct);
        req.setAttribute("restockLogs", history);

        // 4. Forward to inventory JSP page
        req.getRequestDispatcher("/WEB-INF/jsp/shop/inventory.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession();
        User currentUser = SessionUtil.getCurrentUser(session);
        if (currentUser == null || !AppConfig.ROLE_SHOP_OWNER.equals(currentUser.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        // 1. Read parameters
        String variantIdStr = req.getParameter("variantId");
        String quantityStr = req.getParameter("quantity");
        String note = req.getParameter("note");
        String changedAtStr = req.getParameter("changedAt");

        // 2. Validation
        if (variantIdStr == null || variantIdStr.trim().isEmpty() ||
                quantityStr == null || quantityStr.trim().isEmpty() ||
                changedAtStr == null || changedAtStr.trim().isEmpty()) {
            SessionUtil.flashError(session, "Vui lòng nhập đầy đủ các trường bắt buộc.");
            resp.sendRedirect(req.getContextPath() + "/shop/inventory");
            return;
        }

        int variantId;
        int quantity;
        LocalDate changedAt;

        try {
            variantId = Integer.parseInt(variantIdStr);
            quantity = Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            SessionUtil.flashError(session, "Mã sản phẩm hoặc số lượng nhập kho không hợp lệ.");
            resp.sendRedirect(req.getContextPath() + "/shop/inventory");
            return;
        }

        try {
            changedAt = LocalDate.parse(changedAtStr);
        } catch (DateTimeParseException e) {
            SessionUtil.flashError(session, "Định dạng ngày nhập kho không hợp lệ.");
            resp.sendRedirect(req.getContextPath() + "/shop/inventory");
            return;
        }

        if (quantity <= 0) {
            SessionUtil.flashError(session, "Số lượng nhập kho phải lớn hơn 0.");
            resp.sendRedirect(req.getContextPath() + "/shop/inventory");
            return;
        }

        if (changedAt.isAfter(LocalDate.now())) {
            SessionUtil.flashError(session, "Ngày nhập kho không được lớn hơn ngày hiện tại.");
            resp.sendRedirect(req.getContextPath() + "/shop/inventory");
            return;
        }

        try {
            // 3. Security check: Verify that this variant belongs to a product owned by the
            // current Shop Owner
            ProductVariant pv = productVariantDAO.findById(variantId);
            if (pv == null) {
                SessionUtil.flashError(session, "Biến thể sản phẩm không tồn tại.");
                resp.sendRedirect(req.getContextPath() + "/shop/inventory");
                return;
            }

            List<Product> products = productDAO.findById(pv.getProductId());
            if (products.isEmpty() || products.get(0).getOwnerId() != currentUser.getUserId()) {
                SessionUtil.flashError(session, "Bạn không có quyền nhập kho cho sản phẩm này.");
                resp.sendRedirect(req.getContextPath() + "/shop/inventory");
                return;
            }

            // 4. Call Service to execute transaction
            inventoryService.restock(variantId, quantity, note, changedAt, currentUser.getUserId());
            SessionUtil.flashSuccess(session, "Nhập kho sản phẩm thành công!");

        } catch (SQLException e) {
            LoggerUtil.error(log, "Lỗi cơ sở dữ liệu khi nhập kho", e);
            SessionUtil.flashError(session, "Lỗi cơ sở dữ liệu: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            SessionUtil.flashError(session, e.getMessage());
        } catch (Exception e) {
            LoggerUtil.error(log, "Lỗi không xác định khi nhập kho", e);
            SessionUtil.flashError(session, "Đã xảy ra lỗi không xác định.");
        }

        // 5. Redirect (PRG Pattern)
        resp.sendRedirect(req.getContextPath() + "/shop/inventory");
    }
}
