package dao.catalog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import model.entity.catalog.InventoryBatch;

public class InventoryBatchDAO {

    public int createBatch(Connection conn, InventoryBatch batch) throws SQLException {
        String sql = "INSERT INTO dbo.inventory_batches (variant_id, initial_quantity, remaining_quantity, expires_at, is_expired) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, batch.getVariantId());
            pstmt.setInt(2, batch.getInitialQuantity());
            pstmt.setInt(3, batch.getRemainingQuantity());
            if (batch.getExpiresAt() != null) {
                pstmt.setDate(4, java.sql.Date.valueOf(batch.getExpiresAt()));
            } else {
                pstmt.setNull(4, Types.DATE);
            }
            pstmt.setBoolean(5, batch.isExpired());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int batchId = rs.getInt(1);
                    batch.setBatchId(batchId);
                    return batchId;
                }
            }
        }
        return -1;
    }

    public List<InventoryBatch> getActiveBatchesByVariant(Connection conn, int variantId) throws SQLException {
        String sql = "SELECT * FROM dbo.inventory_batches " +
                     "WHERE variant_id = ? " +
                     "  AND remaining_quantity > 0 " +
                     "  AND is_expired = 0 " +
                     "  AND (expires_at IS NULL OR expires_at >= CAST(GETDATE() AS DATE)) " +
                     "ORDER BY CASE WHEN expires_at IS NULL THEN 1 ELSE 0 END, expires_at ASC, created_at ASC";
        
        List<InventoryBatch> batches = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, variantId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    InventoryBatch b = new InventoryBatch();
                    b.setBatchId(rs.getInt("batch_id"));
                    b.setVariantId(rs.getInt("variant_id"));
                    b.setInitialQuantity(rs.getInt("initial_quantity"));
                    b.setRemainingQuantity(rs.getInt("remaining_quantity"));
                    
                    java.sql.Date exp = rs.getDate("expires_at");
                    if (exp != null) {
                        b.setExpiresAt(exp.toLocalDate());
                    }
                    b.setExpired(rs.getBoolean("is_expired"));
                    b.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    
                    batches.add(b);
                }
            }
        }
        return batches;
    }

    public InventoryBatch getBatchById(Connection conn, int batchId) throws SQLException {
        String sql = "SELECT * FROM dbo.inventory_batches WHERE batch_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, batchId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    InventoryBatch b = new InventoryBatch();
                    b.setBatchId(rs.getInt("batch_id"));
                    b.setVariantId(rs.getInt("variant_id"));
                    b.setInitialQuantity(rs.getInt("initial_quantity"));
                    b.setRemainingQuantity(rs.getInt("remaining_quantity"));
                    
                    java.sql.Date exp = rs.getDate("expires_at");
                    if (exp != null) {
                        b.setExpiresAt(exp.toLocalDate());
                    }
                    b.setExpired(rs.getBoolean("is_expired"));
                    b.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    return b;
                }
            }
        }
        return null;
    }

    public void updateRemainingQuantity(Connection conn, int batchId, int newQuantity) throws SQLException {
        String sql = "UPDATE dbo.inventory_batches SET remaining_quantity = ? WHERE batch_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newQuantity);
            pstmt.setInt(2, batchId);
            pstmt.executeUpdate();
        }
    }

    public void incrementRemainingQuantity(Connection conn, int batchId, int amount) throws SQLException {
        String sql = "UPDATE dbo.inventory_batches SET remaining_quantity = remaining_quantity + ? WHERE batch_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, amount);
            pstmt.setInt(2, batchId);
            pstmt.executeUpdate();
        }
    }
}
