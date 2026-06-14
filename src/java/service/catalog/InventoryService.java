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
 * @author fruitmkt-team
 */
public class InventoryService {

    private static final Logger log = LoggerUtil.getLogger(InventoryService.class);

    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final ProductVariantDAO productVariantDAO = new ProductVariantDAO();
    private final ProductDAO productDAO = new ProductDAO();

    /**
     * Thực hiện nghiệp vụ nhập kho (Restock) cho một biến thể sản phẩm.
     */
    public void restock(int variantId, int quantity, String note, LocalDate changedAt, int userId) throws SQLException {
        restockWithExpiry(variantId, quantity, note, changedAt, null, userId);
    }

    /**
     * Nhập kho kèm theo ngày hết hạn (Expires At) tùy chọn.
     */
    public void restockWithExpiry(int variantId, int quantity, String note, LocalDate changedAt, LocalDate expiresAt, int userId) throws SQLException {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng nhập kho phải lớn hơn 0.");
        }
        if (changedAt == null) {
            throw new IllegalArgumentException("Ngày nhập kho không được để trống.");
        }
        if (changedAt.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày nhập kho không thể là ngày trong tương lai.");
        }
        if (expiresAt != null && expiresAt.isBefore(changedAt)) {
            throw new IllegalArgumentException("Ngày hết hạn không thể trước ngày nhập kho.");
        }

        try (Connection conn = inventoryDAO.openConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Lấy số lượng tồn kho hiện tại (trong transaction)
                int currentStock = productVariantDAO.getStockQuantity(conn, variantId);
                int stockAfter = currentStock + quantity;

                // 2. Ghi nhận lịch sử thay đổi tồn kho kèm hạn dùng
                InventoryLog logEntry = new InventoryLog();
                logEntry.setVariantId(variantId);
                logEntry.setChangedBy(userId);
                logEntry.setChangeType("MANUAL_ADJUST");
                logEntry.setQuantityDelta(quantity);
                logEntry.setQuantityAfter(stockAfter);
                logEntry.setExpiresAt(expiresAt);
                logEntry.setExpired(false);
                logEntry.setNote(note != null && !note.trim().isEmpty() ? note.trim() : "Nhập kho");
                logEntry.setChangedAt(changedAt.atTime(LocalTime.now()));

                inventoryDAO.save(conn, logEntry);

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

        InventoryLog logEntry = new InventoryLog();
        logEntry.setVariantId(variantId);
        logEntry.setChangedBy(userId);
        logEntry.setChangeType("ORDER_RESERVE");
        logEntry.setQuantityDelta(-qty);
        logEntry.setQuantityAfter(stockAfter);
        logEntry.setNote("Giữ hàng cho đơn hàng #" + orderId);
        logEntry.setChangedAt(LocalDateTime.now());
        
        inventoryDAO.save(conn, logEntry);
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

        InventoryLog logEntry = new InventoryLog();
        logEntry.setVariantId(variantId);
        logEntry.setChangedBy(userId);
        logEntry.setChangeType("ORDER_RELEASE");
        logEntry.setQuantityDelta(qty);
        logEntry.setQuantityAfter(stockAfter);
        logEntry.setNote("Hoàn kho từ đơn hàng #" + orderId);
        logEntry.setChangedAt(LocalDateTime.now());

        inventoryDAO.save(conn, logEntry);
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
        InventoryLog logEntry = new InventoryLog();
        logEntry.setVariantId(variantId);
        logEntry.setChangedBy(userId);
        logEntry.setChangeType("ORDER_CONFIRM");
        logEntry.setQuantityDelta(0);  // Kho đã bị trừ từ bước ORDER_RESERVE, đây chỉ đánh dấu audit
        logEntry.setQuantityAfter(currentStock);
        logEntry.setNote("Giao hàng thành công - Đã bán " + qty + " sản phẩm từ đơn hàng #" + orderId);
        logEntry.setChangedAt(LocalDateTime.now());
        inventoryDAO.save(conn, logEntry);
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

                InventoryLog logEntry = new InventoryLog();
                logEntry.setVariantId(variantId);
                logEntry.setChangedBy(userId);
                logEntry.setChangeType("MANUAL_ADJUST");
                logEntry.setQuantityDelta(delta);
                logEntry.setQuantityAfter(stockAfter);
                logEntry.setNote(note != null ? note : "Điều chỉnh kho thủ công");
                logEntry.setChangedAt(LocalDateTime.now());

                inventoryDAO.save(conn, logEntry);
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

    /**
     * Nghiệp vụ hao hụt tồn kho do sản phẩm hết hạn (EXPIRED).
     * Sẽ giảm tồn kho và ghi log loại EXPIRED.
     */
    public void adjustStockForExpiry(int variantId, int qty, String note, int userId) throws SQLException {
        if (qty <= 0) {
            throw new IllegalArgumentException("Số lượng hao hụt do hết hạn phải lớn hơn 0.");
        }
        try (Connection conn = inventoryDAO.openConnection()) {
            conn.setAutoCommit(false);
            try {
                int currentStock = productVariantDAO.getStockQuantity(conn, variantId);
                int stockAfter = currentStock - qty;
                if (stockAfter < 0) {
                    throw new IllegalArgumentException("Số lượng hao hụt vượt quá số lượng tồn kho hiện tại.");
                }

                InventoryLog logEntry = new InventoryLog();
                logEntry.setVariantId(variantId);
                logEntry.setChangedBy(userId);
                logEntry.setChangeType(config.AppConfig.INVENTORY_CHANGE_EXPIRED);
                logEntry.setQuantityDelta(-qty);
                logEntry.setQuantityAfter(stockAfter);
                logEntry.setNote(note != null && !note.trim().isEmpty() ? note.trim() : "Hao hụt do hết hạn");
                logEntry.setChangedAt(LocalDateTime.now());

                inventoryDAO.save(conn, logEntry);
                productVariantDAO.updateStock(conn, variantId, -qty);
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

    /**
     * Nghiệp vụ hao hụt tồn kho do sản phẩm thối/hỏng (SPOILED).
     * Sẽ giảm tồn kho và ghi log loại SPOILED.
     */
    public void adjustStockForSpoilage(int variantId, int qty, String note, int userId) throws SQLException {
        if (qty <= 0) {
            throw new IllegalArgumentException("Số lượng hao hụt do thối hỏng phải lớn hơn 0.");
        }
        try (Connection conn = inventoryDAO.openConnection()) {
            conn.setAutoCommit(false);
            try {
                int currentStock = productVariantDAO.getStockQuantity(conn, variantId);
                int stockAfter = currentStock - qty;
                if (stockAfter < 0) {
                    throw new IllegalArgumentException("Số lượng hao hụt vượt quá số lượng tồn kho hiện tại.");
                }

                InventoryLog logEntry = new InventoryLog();
                logEntry.setVariantId(variantId);
                logEntry.setChangedBy(userId);
                logEntry.setChangeType(config.AppConfig.INVENTORY_CHANGE_SPOILED);
                logEntry.setQuantityDelta(-qty);
                logEntry.setQuantityAfter(stockAfter);
                logEntry.setNote(note != null && !note.trim().isEmpty() ? note.trim() : "Hao hụt do thối hỏng");
                logEntry.setChangedAt(LocalDateTime.now());

                inventoryDAO.save(conn, logEntry);
                productVariantDAO.updateStock(conn, variantId, -qty);
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

    /**
     * Tự động quét các lô hàng nhập kho đã hết hạn và tiến hành trừ kho,
     * sau đó lưu lịch sử biến động là EXPIRED.
     */
    public int processExpiredBatches() throws SQLException {
        int processedCount = 0;
        try (Connection conn = inventoryDAO.openConnection()) {
            conn.setAutoCommit(false);
            try {
                List<InventoryLog> expiredLogs = inventoryDAO.findExpiredLogs(conn);
                for (InventoryLog logEntry : expiredLogs) {
                    int variantId = logEntry.getVariantId();
                    int batchQty = logEntry.getQuantityDelta(); // Số lượng nhập ban đầu

                    // Lấy số lượng tồn kho hiện tại
                    int currentStock = productVariantDAO.getStockQuantity(conn, variantId);
                    if (currentStock <= 0) {
                        // Kho đã bằng 0, chỉ đánh dấu là đã xử lý
                        inventoryDAO.markLogExpired(conn, logEntry.getLogId());
                        processedCount++;
                        continue;
                    }

                    // Số lượng trừ đi không vượt quá tồn kho thực tế
                    int deductQty = Math.min(currentStock, batchQty);
                    int stockAfter = currentStock - deductQty;

                    // Ghi log biến động kho
                    InventoryLog expiryEntry = new InventoryLog();
                    expiryEntry.setVariantId(variantId);
                    expiryEntry.setChangedBy(logEntry.getChangedBy());
                    expiryEntry.setChangeType(config.AppConfig.INVENTORY_CHANGE_EXPIRED);
                    expiryEntry.setQuantityDelta(-deductQty);
                    expiryEntry.setQuantityAfter(stockAfter);
                    expiryEntry.setNote("Tự động trừ kho: Lô nhập #" + logEntry.getLogId() + " hết hạn ngày " + logEntry.getExpiresAt());
                    expiryEntry.setChangedAt(LocalDateTime.now());

                    inventoryDAO.save(conn, expiryEntry);

                    // Cập nhật số lượng tồn kho của variant
                    productVariantDAO.updateStock(conn, variantId, -deductQty);

                    // Đánh dấu log nhập kho đã xử lý hết hạn
                    inventoryDAO.markLogExpired(conn, logEntry.getLogId());
                    processedCount++;

                    // Gửi cảnh báo nếu tồn kho xuống thấp
                    checkAndSendLowStockAlert(conn, variantId, stockAfter);
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
        return processedCount;
    }

    public List<InventoryLog> getLogs(int variantId) throws SQLException {
        if (variantId <= 0) {
            throw new IllegalArgumentException("Variant ID không hợp lệ.");
        }
        return inventoryDAO.findByVariant(variantId);
    }
}
