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
    private static final int RESTOCK_HISTORY_LIMIT = 50;
    private static final int ACTIVE_BATCH_LIMIT = 25;

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
            // 1. Fetch variants belonging to products of this shop owner in a single joined query (Optimized)
            variantsWithProduct = productVariantDAO.findVariantsWithOwnerDetails(currentUser.getUserId());

            // 2. Fetch past restock history logs
            history = inventoryService.getRestockHistory(currentUser.getUserId(), RESTOCK_HISTORY_LIMIT);

            // 3. Fetch active batches (lô hàng còn hạn) for batch/expiry panel
            List<InventoryLog> activeBatches = inventoryService.getActiveBatches(currentUser.getUserId(), ACTIVE_BATCH_LIMIT);
            req.setAttribute("activeBatches", activeBatches);

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
        String actionType = req.getParameter("actionType");
        String expiresAtStr = req.getParameter("expiresAt");
        if (actionType == null || actionType.trim().isEmpty()) {
            actionType = "RESTOCK";
        }

        // 2. Validation
        if (variantIdStr == null || variantIdStr.trim().isEmpty() ||
                quantityStr == null || quantityStr.trim().isEmpty()) {
            SessionUtil.flashError(session, "Vui lòng nhập đầy đủ các trường bắt buộc.");
            resp.sendRedirect(req.getContextPath() + "/shop/inventory");
            return;
        }

        int variantId;
        int quantity;
        // changedAt luôn lấy ngày hôm nay
        LocalDate changedAt = LocalDate.now();
        LocalDate expiresAt = null;

        if (expiresAtStr != null && !expiresAtStr.trim().isEmpty()) {
            try {
                expiresAt = LocalDate.parse(expiresAtStr.trim());
                if (!"REDUCE".equals(actionType) && expiresAt.isBefore(LocalDate.now())) {
                    SessionUtil.flashError(session, "Ngày hết hạn không được là ngày trong quá khứ.");
                    resp.sendRedirect(req.getContextPath() + "/shop/inventory");
                    return;
                }
            } catch (java.time.format.DateTimeParseException e) {
                SessionUtil.flashError(session, "Ngày hết hạn không đúng định dạng yyyy-MM-dd.");
                resp.sendRedirect(req.getContextPath() + "/shop/inventory");
                return;
            }
        }

        try {
            variantId = Integer.parseInt(variantIdStr);
            quantity = Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            SessionUtil.flashError(session, "Mã sản phẩm hoặc số lượng không hợp lệ.");
            resp.sendRedirect(req.getContextPath() + "/shop/inventory");
            return;
        }

        if (quantity <= 0) {
            SessionUtil.flashError(session, "Số lượng phải lớn hơn 0.");
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
                SessionUtil.flashError(session, "Bạn không có quyền thay đổi kho cho sản phẩm này.");
                resp.sendRedirect(req.getContextPath() + "/shop/inventory");
                return;
            }

            // 4. Call Service to execute transaction
            if ("REDUCE".equals(actionType)) {
                if (note == null || note.trim().isEmpty()) {
                    SessionUtil.flashError(session, "Vui lòng nhập lý do giảm kho vào ghi chú.");
                    resp.sendRedirect(req.getContextPath() + "/shop/inventory");
                    return;
                }
                inventoryService.manualAdjust(variantId, -quantity, note, currentUser.getUserId());
                SessionUtil.flashSuccess(session, "Cập nhật giảm tồn kho thành công!");
            } else {
                inventoryService.restockWithExpiry(variantId, quantity, note, changedAt, expiresAt,
                        currentUser.getUserId());
                SessionUtil.flashSuccess(session, "Nhập kho sản phẩm thành công!");
            }

        } catch (SQLException e) {
            LoggerUtil.error(log, "Lỗi cơ sở dữ liệu khi điều chỉnh kho", e);
            SessionUtil.flashError(session, "Lỗi cơ sở dữ liệu: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            SessionUtil.flashError(session, e.getMessage());
        } catch (Exception e) {
            LoggerUtil.error(log, "Lỗi không xác định khi điều chỉnh kho", e);
            SessionUtil.flashError(session, "Đã xảy ra lỗi không xác định.");
        }

        // 5. Redirect (PRG Pattern)
        resp.sendRedirect(req.getContextPath() + "/shop/inventory");
    }
}
