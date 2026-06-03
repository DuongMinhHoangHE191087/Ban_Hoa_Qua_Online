package com.fruitmkt.service;

import com.fruitmkt.dao.ReplenishmentLogDAO;
import com.fruitmkt.model.entity.ReplenishmentLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * ReplenishmentService — Business logic service for handling Restock Management.
 * Manages JDBC transactions for stock update and logging.
 */
public class ReplenishmentService {

    private final ReplenishmentLogDAO replenishmentLogDAO = new ReplenishmentLogDAO();

    /**
     * Executes the restocking transaction.
     * Inserts a replenishment log and updates the stock of the product variant.
     */
    public void replenish(int variantId, int quantity, String supplierDetails, LocalDate replenishmentDate, int userId) throws SQLException {
        // Business logic validation
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng nhập kho phải lớn hơn 0.");
        }
        if (replenishmentDate == null) {
            throw new IllegalArgumentException("Ngày nhập kho không được để trống.");
        }
        if (replenishmentDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày nhập kho không thể là ngày trong tương lai.");
        }

        ReplenishmentLog log = new ReplenishmentLog();
        log.setVariantId(variantId);
        log.setReplenishedBy(userId);
        log.setQuantity(quantity);
        log.setSupplierDetails(supplierDetails);
        log.setReplenishmentDate(replenishmentDate);

        // Transaction orchestration
        try (Connection conn = replenishmentLogDAO.openConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Insert replenishment log record
                replenishmentLogDAO.save(conn, log);

                // 2. Update stock quantity in product_variants
                String updateStockSql = "UPDATE product_variants SET stock_quantity = stock_quantity + ?, updated_at = GETDATE() WHERE variant_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateStockSql)) {
                    ps.setInt(1, quantity);
                    ps.setInt(2, variantId);
                    int affectedRows = ps.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Cập nhật tồn kho thất bại. Biến thể sản phẩm không tồn tại hoặc đã bị xóa.");
                    }
                }

                // Commit transaction if both succeeded
                conn.commit();
            } catch (Exception e) {
                // Rollback on any failure
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Retrieves all replenishment history logs for a specific Shop Owner.
     */
    public List<ReplenishmentLog> getReplenishmentHistory(int ownerId) throws SQLException {
        return replenishmentLogDAO.findByOwner(ownerId);
    }
}
