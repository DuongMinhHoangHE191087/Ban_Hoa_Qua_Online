# II.14 — Base Pricing

## Feature Overview
Establishes the foundational price for each product variant in the database. Rather than keeping a single pricing field on the product parent table, pricing is decoupled and bound to each weight variant (e.g., Apple 500g is 60,000 VNĐ, Apple 1kg is 110,000 VNĐ).

---

## Domain Rules & Constraints
* **Precision Decimal Storage**: Price is stored as a high-precision decimal `DECIMAL(12,2)` in SQL Server to ensure accurate arithmetic without rounding errors.
* **Validation**: Price values must be positive and non-zero (`price > 0`).

---

## Workflow Details

### 1. Happy Path Flow
1. **Shop Owner** enters a base price (e.g., `120000`) for a 1kg variant in the product creation form.
2. The servlet validates that the price is greater than zero.
3. The DAO executes the SQL insert.
4. The product detail page displays the currency correctly formatted in Vietnamese đồng (e.g., `120.000 VNĐ`).

### 2. Exception / Error Flow
1. **Shop Owner** enters a negative or zero value for a variant price.
2. JavaScript or server-side servlet validation detects the error, displays **"Price must be greater than 0"**, and prevents saving.

---

## Database Changes
Implemented in [database/Schema.sql](file:///c:/Users/WELCOME/Documents/GitHub/Ban_Hoa_Qua_Online/database/Schema.sql):
* Configured column in `dbo.product_variants`:
  ```sql
  price DECIMAL(12,2) NOT NULL
  ```

---

## Code Transformations

### 1. Model Layer
* `ProductVariant.java` maintains price as a `java.math.BigDecimal` (or `double` mapped to BigDecimal) to preserve decimal precision:
  ```java
  private java.math.BigDecimal price;
  // Getters and setters
  ```

### 2. View Layer
* JSP lists format price numbers to standard Vietnamese currency formats:
  ```html
  <fmt:formatNumber value="${variant.price}" type="currency" currencySymbol="đ" />
  ```
