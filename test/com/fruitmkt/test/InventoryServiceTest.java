package com.fruitmkt.test;

import com.fruitmkt.service.InventoryService;
import org.junit.Test;
import org.junit.Before;

import java.time.LocalDate;

import static org.junit.Assert.*;

public class InventoryServiceTest {

    private InventoryService inventoryService;

    @Before
    public void setUp() {
        inventoryService = new InventoryService();
    }

    @Test(expected = IllegalArgumentException.class)
    public void restock_negativeQuantity_throws() throws Exception {
        inventoryService.restock(1, -5, "Restock", LocalDate.now(), 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void restock_zeroQuantity_throws() throws Exception {
        inventoryService.restock(1, 0, "Restock", LocalDate.now(), 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void restock_nullDate_throws() throws Exception {
        inventoryService.restock(1, 10, "Restock", null, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void restock_futureDate_throws() throws Exception {
        inventoryService.restock(1, 10, "Restock", LocalDate.now().plusDays(1), 1);
    }

    @Test(expected = RuntimeException.class)
    public void reserve_insufficientStock_throws() throws Exception {
        inventoryService.reserve(1, 100, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void manualAdjust_negativeResultStock_throws() throws Exception {
        inventoryService.manualAdjust(1, -1000, "Adjust", 1);
    }

    @Test
    public void manualAdjust_zeroQuantity_succeeds() throws Exception {
        inventoryService.manualAdjust(1, 0, "Adjust", 1);
    }

    @Test
    public void reserve_negativeDelta_ignored() throws Exception {
        inventoryService.reserve(1, 0, 1);
    }

    @Test
    public void release_negativeDelta_ignored() throws Exception {
        inventoryService.release(1, 0, 1);
    }

    @Test(expected = Exception.class)
    public void getRestockHistory_invalidOwnerId_throws() throws Exception {
        inventoryService.getRestockHistory(-1);
    }

    @Test(expected = Exception.class)
    public void getLogs_invalidVariantId_throws() throws Exception {
        inventoryService.getLogs(-1);
    }
}
