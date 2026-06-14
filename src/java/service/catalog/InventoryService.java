package service.catalog;
import dao.catalog.InventoryDAO;


import dao.catalog.ProductVariantDAO;
import dao.catalog.ProductDAO;
import model.entity.catalog.InventoryLog;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import java.util.logging.Logger;
import util.LoggerUtil;

/**
 * InventoryService — Tầng business logic cho nghiệp vụ tương ứng.
 *
 * QUY TẮC:
 *   - Chỉ gọi DAO, không viết SQL ở đây
 *   - Chứa tất cả validation và business rule
 *   - Ném RuntimeException hoặc custom exception cho Servlet xử lý
 *   - Không tương tác trực tiếp với HttpRequest/Response
 *
 * @author fruitmkt-team
 */
public class InventoryService {

    private static final Logger log = LoggerUtil.getLogger(InventoryService.class);

    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final ProductVariantDAO productVariantDAO = new ProductVariantDAO();
    private final ProductDAO productDAO = new ProductDAO();

    /**
     * Thực hiện nghiệp vụ nhập kho (Restock) cho một biến thể sản phẩm.
     * Quản lý giao dịch để đảm bảo ghi log và cập nhật số lượng tồn kho đồng thời.
     */
    public void restock(int variantId, int quantity, String note, LocalDate changedAt, int userId) throws SQLException {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng nhập kho phải lớn hơn 0.");
        }
        if (changedAt == null) {
            throw new IllegalArgumentException("Ngày nhập kho không được để trống.");
        }
        if (changedAt.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày nhập kho không thể là ngày trong tương lai.");
        }

        try (Connection conn = inventoryDAO.openConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Lấy số lượng tồn kho hiện tại (trong transaction)
                int currentStock = productVariantDAO.getStockQuantity(conn, variantId);
                int stockAfter = currentStock + quantity;

                // 2. Ghi nhận lịch sử thay đổi tồn kho
                InventoryLog log = new InventoryLog();
                log.setVariantId(variantId);
                log.setChangedBy(userId);
                log.setChangeType("MANUAL_ADJUST");
                log.setQuantityDelta(quantity);
                log.setQuantityAfter(stockAfter);
                
                log.setNote(note != null && !note.trim().isEmpty() ? note.trim() : "Nhập kho");
                log.setChangedAt(changedAt.atTime(LocalTime.now()));

                inventoryDAO.save(conn, log);

                // 3. Cập nhật tồn kho thực tế
                productVariantDAO.updateStock(conn, variantId, quantity);

                // 4. Lấy productId từ variantId và cập nhật ngày thu hoạch cùng trạng thái sản phẩm sang ACTIVE
                int productId = productVariantDAO.getProductId(conn, variantId);
                productDAO.updateHarvestDateAndStatus(conn, productId, changedAt, "ACTIVE");

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Lấy toàn bộ lịch sử biến động kho của Shop Owner.
     */
    public List<InventoryLog> getRestockHistory(int ownerId) throws SQLException {
        if (ownerId <= 0) {
            throw new IllegalArgumentException("Owner ID không hợp lệ.");
        }
        return inventoryDAO.findByOwner(ownerId);
    }

    public void reserve(Connection conn, int variantId, int qty, int orderId, int userId) throws SQLException {
        if (qty <= 0) return;
        int currentStock = productVariantDAO.getStockQuantity(conn, variantId);
        if (currentStock < qty) {
            throw new RuntimeException("Không đủ số lượng hàng tồn kho cho sản phẩm.");
        }
        int stockAfter = currentStock - qty;

        InventoryLog log = new InventoryLog();
        log.setVariantId(variantId);
        log.setChangedBy(userId);
        log.setChangeType("ORDER_RESERVE");
        log.setQuantityDelta(-qty);
        log.setQuantityAfter(stockAfter);
        log.setNote("Giữ hàng cho đơn hàng #" + orderId);
        log.setChangedAt(LocalDateTime.now());
        
        inventoryDAO.save(conn, log);
        productVariantDAO.updateStock(conn, variantId, -qty);
        checkAndSendLowStockAlert(conn, variantId, stockAfter);
    }

    public void reserve(int variantId, int qty, int orderId) throws SQLException {
        try (Connection conn = inventoryDAO.openConnection()) {
            conn.setAutoCommit(false);
            try {
                reserve(conn, variantId, qty, orderId, 1);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public void release(Connection conn, int variantId, int qty, int orderId, int userId) throws SQLException {
        if (qty <= 0) return;
        int currentStock = productVariantDAO.getStockQuantity(conn, variantId);
        int stockAfter = currentStock + qty;

        InventoryLog log = new InventoryLog();
        log.setVariantId(variantId);
        log.setChangedBy(userId);
        log.setChangeType("ORDER_RELEASE");
        log.setQuantityDelta(qty);
        log.setQuantityAfter(stockAfter);
        log.setNote("Hoàn kho từ đơn hàng #" + orderId);
        log.setChangedAt(LocalDateTime.now());

        inventoryDAO.save(conn, log);
        productVariantDAO.updateStock(conn, variantId, qty);
    }

    public void release(int variantId, int qty, int orderId) throws SQLException {
        try (Connection conn = inventoryDAO.openConnection()) {
            conn.setAutoCommit(false);
            try {
                release(conn, variantId, qty, orderId, 1);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public void confirm(Connection conn, int variantId, int qty, int orderId, int userId) throws SQLException {
        int currentStock = productVariantDAO.getStockQuantity(conn, variantId);
        InventoryLog log = new InventoryLog();
        log.setVariantId(variantId);
        log.setChangedBy(userId);
        log.setChangeType("ORDER_CONFIRM");
        log.setQuantityDelta(0);
        log.setQuantityAfter(currentStock);
        log.setNote("Xác nhận bán hàng cho đơn hàng #" + orderId);
        log.setChangedAt(LocalDateTime.now());
        inventoryDAO.save(conn, log);
    }

    public void confirm(int variantId, int qty, int orderId) throws SQLException {
        try (Connection conn = inventoryDAO.openConnection()) {
            confirm(conn, variantId, qty, orderId, 1);
        }
    }

    public void manualAdjust(int variantId, int delta, String note, int userId) throws SQLException {
        if (delta == 0) return;
        try (Connection conn = inventoryDAO.openConnection()) {
            conn.setAutoCommit(false);
            try {
                int currentStock = productVariantDAO.getStockQuantity(conn, variantId);
                int stockAfter = currentStock + delta;
                if (stockAfter < 0) {
                    throw new IllegalArgumentException("Số lượng tồn kho sau điều chỉnh không được âm.");
                }

                InventoryLog log = new InventoryLog();
                log.setVariantId(variantId);
                log.setChangedBy(userId);
                log.setChangeType("MANUAL_ADJUST");
                log.setQuantityDelta(delta);
                log.setQuantityAfter(stockAfter);
                log.setNote(note != null ? note : "Điều chỉnh kho thủ công");
                log.setChangedAt(LocalDateTime.now());

                inventoryDAO.save(conn, log);
                productVariantDAO.updateStock(conn, variantId, delta);
                checkAndSendLowStockAlert(conn, variantId, stockAfter);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private void checkAndSendLowStockAlert(Connection conn, int variantId, int stockAfter) {
        if (stockAfter < 5) {
            try {
                int ownerId = productVariantDAO.getProductOwnerId(conn, variantId);
                java.util.Map<String, Object> info = productVariantDAO.getVariantAndProductName(conn, variantId);
                String sku = (String) info.get("sku");
                String productName = (String) info.get("name");
                
                service.chat.NotificationService notificationService = new service.chat.NotificationService();
                String title = "Cảnh báo tồn kho thấp";
                String message = "Sản phẩm \"" + productName + "\" (SKU: " + sku + ") sắp hết hàng. Chỉ còn " + stockAfter + " sản phẩm trong kho.";
                notificationService.send(ownerId, config.AppConfig.NOTIF_INVENTORY_ALERT, title, message, "/shop/products");
            } catch (Exception ex) {
                LoggerUtil.warn(log, "Không thể gửi cảnh báo tồn kho thấp cho variantId=" + variantId, ex);
            }
        }
    }

    public List<InventoryLog> getLogs(int variantId) throws SQLException {
        if (variantId <= 0) {
            throw new IllegalArgumentException("Variant ID không hợp lệ.");
        }
        return inventoryDAO.findByVariant(variantId);
    }

}
