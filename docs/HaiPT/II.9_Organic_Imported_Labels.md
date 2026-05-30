# II.9 — Organic/Imported Labels

## Feature Overview
Introduces a classification label type for fruits, marking them as either **Organic** or **Imported**. These premium tags are rendered as eye-catching badges overlaying product image grids, allowing buyers to quickly distinguish quality categories while browsing.

---

## Domain Rules & Constraints
* **Database Check Constraint**: The product label must match the check constraint `CK_products_label_type` (only `'Organic'` or `'Imported'` are allowed, or `NULL`).
* **Visual Guidelines**: High-visibility glassmorphic CSS badges are overlaid onto product cards (`#34d399` green for Organic, `#3b82f6` blue for Imported).

---

## Workflow Details

### 1. Happy Path Flow
1. **Shop Owner** selects either "Organic" or "Imported" from a label dropdown when creating or editing a fruit listing in the shop portal.
2. The product is saved with the chosen label value.
3. The catalog list controller fetches the product with its `labelType` property populated.
4. The product list page (`product-list.jsp`) renders the overlay tag based on the label value.

### 2. Exception / Error Flow
1. If an incorrect value is sent (e.g., manipulated post data), the server-side validator detects the discrepancy and blocks the update.
2. If written directly to the database, the check constraint `CK_products_label_type` throws a SQL error, halting the insert/update transaction.

---

## Database Changes
Implemented in [database/Schema.sql](file:///c:/Users/WELCOME/Documents/GitHub/Ban_Hoa_Qua_Online/database/Schema.sql):
* Extended `dbo.products` table:
  ```sql
  ALTER TABLE dbo.products ADD label_type NVARCHAR(20) NULL;
  ALTER TABLE dbo.products ADD CONSTRAINT CK_products_label_type CHECK (label_type IN ('Organic', 'Imported'));
  ```

---

## Code Transformations

### 1. Model Layer
* Added `labelType` field in `Product.java`:
  ```java
  private String labelType;
  // Getters and setters
  ```

### 2. DAO Layer (`ProductDAO.java`)
* Extended the mapping method `mapRow(ResultSet rs)` and `save()` / `update()` statements to include `label_type`.

### 3. View Layer (`product-list.jsp`, `product-detail.jsp`)
* Dynamic JSTL checks render badge containers styled with custom CSS:
  ```html
  <c:if test="${not empty product.labelType}">
      <span class="badge badge-${fn:toLowerCase(product.labelType)}">${product.labelType}</span>
  </c:if>
  ```
