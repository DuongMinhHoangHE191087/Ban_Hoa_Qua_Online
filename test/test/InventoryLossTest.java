package test;

import service.catalog.InventoryService;
import model.entity.catalog.InventoryLog;
import org.junit.Test;
import org.junit.Before;
import java.sql.SQLException;
import java.util.List;
import static org.junit.Assert.*;

public class InventoryLossTest {

    private InventoryService inventoryService;

    @Before
    public void setUp() {
        inventoryService = new InventoryService();
    }

    @Test
    public void adjustStockForExpiry_validInputs_succeeds() throws Exception {
        // Lấy số lượng ban đầu của variant 1 (phần tử đầu tiên là mới nhất vì sắp xếp DESC)
        List<InventoryLog> initialLogs = inventoryService.getLogs(1);
        int initialStock = initialLogs.isEmpty() ? 0 : initialLogs.get(0).getQuantityAfter();

        // restock trước nếu tồn kho bằng 0 hoặc quá ít
        if (initialStock < 50) {
            inventoryService.restock(1, 50, "Restock for test", java.time.LocalDate.now(), 1);
            initialLogs = inventoryService.getLogs(1);
            initialStock = initialLogs.get(0).getQuantityAfter();
        }

        // Thực hiện giảm 10 sản phẩm do hết hạn
        int qtyToReduce = 10;
        inventoryService.adjustStockForExpiry(1, qtyToReduce, "Hết hạn test", 1);

        // Kiểm tra log mới nhất (nằm ở vị trí đầu tiên do sắp xếp DESC)
        List<InventoryLog> logs = inventoryService.getLogs(1);
        assertFalse(logs.isEmpty());
        InventoryLog latestLog = logs.get(0);

        assertEquals("EXPIRED", latestLog.getChangeType());
        assertEquals(-qtyToReduce, latestLog.getQuantityDelta());
        assertEquals(initialStock - qtyToReduce, latestLog.getQuantityAfter());
        assertEquals("Hết hạn test", latestLog.getNote());
    }

    @Test
    public void adjustStockForSpoilage_validInputs_succeeds() throws Exception {
        // Lấy số lượng ban đầu của variant 1
        List<InventoryLog> initialLogs = inventoryService.getLogs(1);
        int initialStock = initialLogs.isEmpty() ? 0 : initialLogs.get(0).getQuantityAfter();

        // restock trước nếu tồn kho bằng 0 hoặc quá ít
        if (initialStock < 50) {
            inventoryService.restock(1, 50, "Restock for test", java.time.LocalDate.now(), 1);
            initialLogs = inventoryService.getLogs(1);
            initialStock = initialLogs.get(0).getQuantityAfter();
        }

        // Thực hiện giảm 5 sản phẩm do thối hỏng
        int qtyToReduce = 5;
        inventoryService.adjustStockForSpoilage(1, qtyToReduce, "Thối hỏng test", 1);

        // Kiểm tra log mới nhất
        List<InventoryLog> logs = inventoryService.getLogs(1);
        assertFalse(logs.isEmpty());
        InventoryLog latestLog = logs.get(0);

        assertEquals("SPOILED", latestLog.getChangeType());
        assertEquals(-qtyToReduce, latestLog.getQuantityDelta());
        assertEquals(initialStock - qtyToReduce, latestLog.getQuantityAfter());
        assertEquals("Thối hỏng test", latestLog.getNote());
    }

    @Test(expected = IllegalArgumentException.class)
    public void adjustStockForExpiry_negativeQuantity_throws() throws Exception {
        inventoryService.adjustStockForExpiry(1, -5, "Invalid qty", 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void adjustStockForExpiry_zeroQuantity_throws() throws Exception {
        inventoryService.adjustStockForExpiry(1, 0, "Invalid qty", 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void adjustStockForExpiry_insufficientStock_throws() throws Exception {
        // Lấy tồn kho hiện tại để tính số lượng lớn hơn
        List<InventoryLog> logs = inventoryService.getLogs(1);
        int currentStock = logs.isEmpty() ? 0 : logs.get(0).getQuantityAfter();
        inventoryService.adjustStockForExpiry(1, currentStock + 999999, "Exceed stock", 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void adjustStockForSpoilage_negativeQuantity_throws() throws Exception {
        inventoryService.adjustStockForSpoilage(1, -5, "Invalid qty", 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void adjustStockForSpoilage_zeroQuantity_throws() throws Exception {
        inventoryService.adjustStockForSpoilage(1, 0, "Invalid qty", 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void adjustStockForSpoilage_insufficientStock_throws() throws Exception {
        // Lấy tồn kho hiện tại để tính số lượng lớn hơn
        List<InventoryLog> logs = inventoryService.getLogs(1);
        int currentStock = logs.isEmpty() ? 0 : logs.get(0).getQuantityAfter();
        inventoryService.adjustStockForSpoilage(1, currentStock + 999999, "Exceed stock", 1);
    }

    @Test
    public void autoExpiryBatch_pastExpiryDate_deductsStock() throws Exception {
        // 1. Nhập kho một lô hàng có ngày hết hạn là ngày hôm qua (đã hết hạn)
        dao.catalog.ProductVariantDAO variantDAO = new dao.catalog.ProductVariantDAO();
        int variantId = 1;
        int restockQty = 15;
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate fiveDaysAgo = today.minusDays(5);
        java.time.LocalDate yesterday = today.minusDays(1);

        // Lấy tồn kho hiện tại trước khi nhập
        int stockBeforeRestock = variantDAO.getStockQuantity(variantId);

        inventoryService.restockWithExpiry(variantId, restockQty, "Lô hàng test hết hạn tự động", fiveDaysAgo, yesterday, 1);

        // Tồn kho sau khi nhập
        int stockAfterRestock = variantDAO.getStockQuantity(variantId);
        assertEquals(stockBeforeRestock + restockQty, stockAfterRestock);

        // 2. Chạy tiến trình xử lý hết hạn tự động
        int processed = inventoryService.processExpiredBatches();
        assertTrue(processed >= 1); // Phải xử lý ít nhất lô hàng vừa nhập hết hạn

        // 3. Kiểm tra xem tồn kho đã bị trừ tương ứng
        int stockAfterExpiry = variantDAO.getStockQuantity(variantId);
        assertEquals(stockAfterRestock - restockQty, stockAfterExpiry); // Trừ đi đúng lượng hết hạn

        // 4. Kiểm tra log biến động kho mới nhất
        List<InventoryLog> finalLogs = inventoryService.getLogs(variantId);
        assertFalse(finalLogs.isEmpty());
        InventoryLog expiryLog = finalLogs.get(0);
        assertEquals("EXPIRED", expiryLog.getChangeType());
        assertEquals(-restockQty, expiryLog.getQuantityDelta());
        assertTrue(expiryLog.getNote().contains("Tự động trừ kho"));
    }
}
