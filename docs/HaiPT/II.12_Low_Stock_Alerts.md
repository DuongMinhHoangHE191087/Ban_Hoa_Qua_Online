# II.12 — Low Stock Alerts

## Feature Overview
Allows the **Shop Owner** to proactively monitor stock levels by displaying instant, red-themed warning panels on their dashboard. Any active product variant that falls to or below **5 units** is flagged immediately, helping them prevent stockouts and coordinate restock operations.

---

## Domain Rules & Constraints
* **Low Stock Threshold**: Defined strictly at **5 units** (`stock_quantity <= 5`).
* **Filtering**: Only displays active variants (`is_active = 1`) belonging to products owned by the currently logged-in Shop Owner.
* **UI Design**: Modern, glassmorphic red alerts (`bg-red-500/10` with red border and micro-animations) rendered in `dashboard.jsp`.

---

## Workflow Details

### 1. Happy Path Flow
1. A checkout transaction occurs, bringing an Apple variant's stock down to 4 units.
2. The **Shop Owner** logs into their dashboard.
3. The dashboard controller calls `ProductVariantDAO.findLowStock(ownerId)`.
4. The database queries:
   ```sql
   SELECT pv.*, p.name AS product_name 
   FROM product_variants pv 
   JOIN products p ON pv.product_id = p.product_id 
   WHERE p.owner_id = ? AND pv.stock_quantity <= 5 AND pv.is_active = 1;
   ```
5. The view displays a red alert banner saying: **"Warning: Apple - 1kg is low on stock (4 items left)!"**

### 2. Resolution Flow
1. The **Shop Owner** clicks on the warning alert.
2. They are redirected to the Inventory page where they can initiate a restock.
3. Once restocked, the stock level rises above 5, and the warning banner automatically clears.

---

## Database Changes
No direct schema changes are needed since this is a read-only query mapping existing tables, but it leverages the `dbo.product_variants` and `dbo.products` schemas.

---

## Code Transformations

### 1. DAO Layer (`ProductVariantDAO.java`)
* Added `findLowStock(int ownerId)` returning a list of low-stock `ProductVariant` objects:
  ```java
  public List<ProductVariant> findLowStock(int ownerId) throws SQLException {
      // Executes query with stock_quantity <= 5 and maps rows
  }
  ```

### 2. Controller Layer (`ShopDashboardServlet.java`)
* Extracts the logged-in user session, calls `ProductService`, and forwards the collections to the request scope (`req.setAttribute("lowStockVariants", list)`).

### 3. View Layer (`dashboard.jsp`)
* Formats the list using JSTL tags. If the collection is not empty, a prominent red warn box is shown.
