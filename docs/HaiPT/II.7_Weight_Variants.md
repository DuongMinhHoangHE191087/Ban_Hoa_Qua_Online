# II.7 — Weight Variants

## Feature Overview
Allows the **Shop Owner** to configure different weight variants (e.g., 500g, 1kg, 2kg) for a single product. Each variant maintains its own pricing, SKU, and warehouse stock levels. This introduces a flexible product inventory model compared to simple single-pricing products.

---

## Domain Rules & Constraints
* **Duplicate Weights Blocked**: A unique filtered index constraint `UQ_product_variants_product_weight` is defined on `dbo.product_variants` so that a product *cannot* have duplicate active weight values (e.g., two variants with 1000g for Apple).
* **Validation**: Input weight (grams), price, and stock must be positive integers/decimals. Duplicate weights submitted from the frontend UI will be rejected on both client-side and server-side.

---

## Workflow Details

### 1. Happy Path Flow
1. **Shop Owner** navigates to the Add/Edit Product page.
2. In the dynamic variant builder table, they click **"Add Variant"** and enter weight (e.g., 1000g), price (e.g., 150,000 VNĐ), and stock (e.g., 50 units).
3. They submit the form.
4. The servlet parses the array of variants, validates that there are no duplicate weight values, and transactionally saves the variants.
5. The product page displays the successfully configured weight chips to the buyer.

### 2. Exception / Error Flow
1. **Shop Owner** attempts to save two variants with the same weight (e.g., two entries of 500g) for a single product.
2. **Client-side JS** detects the duplication and alerts the user, blocking the form submission.
3. If the request bypasses JS, the **Servlet controller** performs duplicate checks on the input list and rejects the request with an error message.
4. If a concurrent database write attempts to insert duplicate weights, the SQL Server database throws a unique constraint violation on index `UQ_product_variants_product_weight`, causing the transaction to rollback and safely protecting data integrity.

---

## Database Changes
Implemented in [database/Schema.sql](file:///c:/Users/WELCOME/Documents/GitHub/Ban_Hoa_Qua_Online/database/Schema.sql):
* Extended `dbo.product_variants` table:
  ```sql
  ALTER TABLE dbo.product_variants ADD weight_grams INT NULL;
  ```
* Created Unique Index:
  ```sql
  CREATE UNIQUE INDEX UQ_product_variants_product_weight
  ON dbo.product_variants(product_id, weight_grams)
  WHERE is_active = 1 AND weight_grams IS NOT NULL;
  ```

---

## Code Transformations

### 1. Model Layer
* Added `weightGrams` field in `ProductVariant.java`:
  ```java
  private Integer weightGrams;
  public Integer getWeightGrams() { return weightGrams; }
  public void setWeightGrams(Integer weightGrams) { this.weightGrams = weightGrams; }
  ```

### 2. DAO Layer (`ProductVariantDAO.java`)
* Parsed and persisted `weight_grams` column in mapping and inserts:
  ```java
  ps.setInt(colIndex, variant.getWeightGrams());
  ```

### 3. Controller & View Layers
* **Servlet**: `ProductManageServlet.java` extracts `weightGrams` arrays from form submission, checks for duplicates, and executes a transactional batch update.
* **JSP**: `product-form.jsp` includes a dynamic Javascript-driven variant grid builder with live duplication checks.
