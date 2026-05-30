# II.13 — Restock Management

## Feature Overview
Provides the **Shop Owner** with a manual warehouse restock form (`inventory.jsp` / `ShopRestockServlet.java`). Selecting an active variant allows them to replenish its inventory. The action adjusts stock levels and automatically records an audit log to maintain absolute traceablity.

---

## Domain Rules & Constraints
* **Negative Stock Input Blocked**: The restock quantity must be a strictly positive integer (`quantity > 0`). Negative numbers or empty submissions are blocked.
* **Transactional Logging**: Modifying a variant's inventory level and inserting an entry into `dbo.inventory_logs` must be done in a single database transaction. If logging fails, the stock increase is rolled back.

---

## Workflow Details

### 1. Happy Path Flow
1. **Shop Owner** navigates to the `/shop/inventory` page.
2. In the restock panel, they select a product variant and input a quantity (e.g., +100 units) and a note (e.g., "New summer harvest delivery").
3. They submit the form.
4. The servlet receives the POST request, validates the positive quantity input, and runs a transactional update.
5. In the database, the stock level increases by 100, and a new log row with type `'MANUAL_ADJUST'` is recorded.
6. The page reloads with a success alert, displaying the updated stock level and showing the new audit trail record in the logs list.

### 2. Exception / Error Flow
1. **Shop Owner** inputs an invalid amount (e.g., `-50` or `abc`).
2. The servlet intercepts the request, runs validation checks, and returns a detailed validation error message without modifying the database.

---

## Database Changes
No new tables are required, as this leverages `dbo.product_variants` and `dbo.inventory_logs`.

---

## Code Transformations

### 1. DAO Layer (`ProductVariantDAO.java`)
* Added a transactional restock method:
  ```java
  public void restockVariant(int variantId, int quantity, int userId, String note) throws SQLException {
      // Connects, disables auto-commit, locks/updates stock_quantity, records insert in inventory_logs, then commits.
  }
  ```

### 2. Controller Layer (`ShopRestockServlet.java` or `InventoryServlet.java`)
* Serves the GET request by populating active items and the current audit log history.
* Parses and validates the POST arguments, displays alert status messages, and prevents negative values.

### 3. View Layer (`inventory.jsp`)
* Designed a clean, glassmorphic layout detailing active stock lists, a quick-restock modal/form, and a history table rendering all database logs.
