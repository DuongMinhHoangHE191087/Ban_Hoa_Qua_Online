# II.11 — Automatic Stock Deduction & Restore

## Feature Overview
Provides automatic inventory level updates (reductions and restorations) to ensure that fruit stock is never oversold or lost. This must run in a thread-safe environment with strong transactional isolation to handle high concurrent purchase events (e.g., flash sales).

---

## Domain Rules & Constraints
* **Pessimistic Row Locking**: During checkout, the variant records are queried using a pessimistic lock (`WITH (UPDLOCK)`) inside a SQL Transaction to prevent race conditions or double-deductions.
* **Rollback Safeguard**: If any variant stock quantity falls below zero during deduction, the transaction rolls back, and the order is rejected.
* **Restoration**: If a Customer or Shop Owner cancels an order, the system automatically returns the subtracted quantities to their respective variant inventory pools.
* **Audit Logging**: Every single stock change (order reserve, manual adjustment, cancellation release) must be logged into `dbo.inventory_logs`.

---

## Workflow Details

### 1. Checkout Deduction (Happy Path)
1. **Customer** completes checkout submission.
2. `OrderService.placeOrder()` opens a database transaction.
3. For each order item, the query locks the variant row:
   ```sql
   SELECT stock_quantity FROM product_variants WITH (UPDLOCK) WHERE variant_id = ?;
   ```
4. Stock is verified (e.g., 10 items in stock, ordering 2).
5. The system performs the deduction:
   ```sql
   UPDATE product_variants SET stock_quantity = stock_quantity - 2 WHERE variant_id = ?;
   ```
6. An entry is recorded in `dbo.inventory_logs` with type `'ORDER_RESERVE'`.
7. Transaction commits successfully.

### 2. Order Cancellation (Restoration Flow)
1. **Shop Owner** cancels the order.
2. `OrderService.cancelOrder()` opens a transaction.
3. Locked rows are updated to add back the canceled items.
4. A release entry is recorded in `dbo.inventory_logs` with type `'ORDER_RELEASE'`.
5. Transaction commits, and stock becomes immediately available to other buyers.

---

## Database Changes
Implemented in [database/Schema.sql](file:///c:/Users/WELCOME/Documents/GitHub/Ban_Hoa_Qua_Online/database/Schema.sql):
* Created the `inventory_logs` table:
  ```sql
  CREATE TABLE inventory_logs (
      log_id INT IDENTITY(1,1) PRIMARY KEY,
      variant_id INT NOT NULL FOREIGN KEY REFERENCES product_variants(variant_id),
      changed_by INT NOT NULL FOREIGN KEY REFERENCES users(user_id),
      change_type NVARCHAR(20) NOT NULL CHECK (change_type IN ('MANUAL_ADJUST','ORDER_RESERVE','ORDER_RELEASE','ORDER_CONFIRM','RETURN')),
      quantity_delta INT NOT NULL,
      quantity_after INT NOT NULL,
      note NVARCHAR(300) NULL,
      changed_at DATETIME NOT NULL DEFAULT GETDATE()
  );
  ```

---

## Code Transformations

### 1. DAO Layer (`InventoryDAO.java`)
* Added methods to retrieve inventory audit trails by owner or variant.

### 2. Service Layer (`OrderService.java`)
* Fully implemented transactional JDBC blocks setting `conn.setAutoCommit(false)` and managing row locks and log entries inside `try-catch-rollback` patterns.
