package com.fruitmkt.service;

import com.fruitmkt.dao.InventoryDAO;
import com.fruitmkt.dao.ProductVariantDAO;
import com.fruitmkt.model.entity.InventoryLog;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

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

    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final ProductVariantDAO productVariantDAO = new ProductVariantDAO();

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
                log.setChangedAt(changedAt.atTime(java.time.LocalTime.now()));

                inventoryDAO.save(conn, log);

                // 3. Cập nhật tồn kho thực tế
                productVariantDAO.updateStock(conn, variantId, quantity);

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
        return inventoryDAO.findByOwner(ownerId);
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public void reserve(int variantId, int qty, int orderId) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: reserve(int variantId, int qty, int orderId)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public void release(int variantId, int qty, int orderId) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: release(int variantId, int qty, int orderId)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public void confirm(int variantId, int qty, int orderId) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: confirm(int variantId, int qty, int orderId)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public void manualAdjust(int variantId, int delta, String note, int userId) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: manualAdjust(int variantId, int delta, String note, int userId)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public java.util.List getLogs(int variantId) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: getLogs(int variantId)");
    }

}
