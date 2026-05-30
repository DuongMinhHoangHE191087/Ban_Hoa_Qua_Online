# II.19 — Availability Filters

## Feature Overview
Allows customers to filter out out-of-stock items using an **"In Stock Only"** checkbox. This improves user satisfaction by hiding products that are temporarily unavailable.

---

## Domain Rules & Constraints
* **Availability Threshold**: A product is considered "in stock" if it has *at least one* active variant with a stock quantity greater than zero (`stock_quantity > 0`).
* **Subquery Constraint**: The SQL query uses an optimized `EXISTS` subquery to check for in-stock variants, avoiding duplicate product row records in the result set.

---

## Workflow Details

### 1. Happy Path Flow
1. **Customer** toggles the **"In Stock Only"** checkbox.
2. JavaScript captures the event and appends `inStockOnly=true` to the AJAX query.
3. The DAO executes the search:
   ```sql
   SELECT DISTINCT p.* FROM products p 
   WHERE p.status = 'ACTIVE' 
     AND EXISTS (
         SELECT 1 FROM product_variants 
         WHERE product_id = p.product_id 
           AND stock_quantity > 0 
           AND is_active = 1
     );
   ```
4. Out-of-stock fruits are hidden from the grid.

---

## Code Transformations

### 1. DAO Layer (`ProductDAO.java`)
* Incorporates the dynamic availability filter clause inside `searchAdvanced` and `countSearchAdvanced`:
  ```java
  if (inStockOnly != null && inStockOnly) {
      sql.append("AND EXISTS (SELECT 1 FROM product_variants WHERE product_id = p.product_id AND stock_quantity > 0 AND is_active = 1) ");
  }
  ```

### 2. View Layer (`product-list.jsp`)
* Embeds a custom checkbox labeled "Show In Stock Only". Clicking it triggers a smooth AJAX refresh on the product container grid.
