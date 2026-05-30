# II.15 — Discount Pricing

## Feature Overview
Allows the **Shop Owner** to apply promotional prices to specific product variants. The buyer sees a dynamic price presentation: the original base price is shown with a **strikethrough**, and the active promotional price is highlighted in **bold red text**. This encourages buying decisions.

---

## Domain Rules & Constraints
* **Discount Constraint**: The discount price must be strictly less than the original base price (`discount_price < price`).
* **Availability**: If a discount price is set, it is used for all total checkout calculations and cart item pricing updates.

---

## Workflow Details

### 1. Happy Path Flow
1. **Shop Owner** sets a base price of 100,000 VNĐ and a discount price of 80,000 VNĐ.
2. The servlet validates that the discount price is lower than the base price and saves it.
3. On the product detail page, the user sees `100.000 VNĐ` crossed out, and `80.000 VNĐ` highlighted next to it.
4. If added to the cart, the system uses the discounted 80,000 VNĐ rate for all subtotal calculations.

### 2. Exception / Error Flow
1. **Shop Owner** sets a discount price of 120,000 VNĐ (higher than base price).
2. The servlet validator flags the error: **"Discount price must be lower than base price"**, and halts saving.

---

## Database Changes
Implemented in [database/Schema.sql](file:///c:/Users/WELCOME/Documents/GitHub/Ban_Hoa_Qua_Online/database/Schema.sql):
* Extended `dbo.product_variants` table:
  ```sql
  ALTER TABLE dbo.product_variants ADD discount_price DECIMAL(12,2) NULL;
  ```

---

## Code Transformations

### 1. Model & DAO Layers
* Added `discountPrice` (`BigDecimal`) in `ProductVariant.java`.
* Extended row-mapping in `ProductVariantDAO.java` to extract and update the `discount_price` column.

### 2. View Layer
* JSP templates use conditional logic to render the prices dynamically:
  ```html
  <c:choose>
      <c:when test="${not empty variant.discountPrice && variant.discountPrice lt variant.price}">
          <span class="original-price line-through text-gray-400 mr-2"><fmt:formatNumber value="${variant.price}"/> đ</span>
          <span class="discount-price text-red-500 font-bold"><fmt:formatNumber value="${variant.discountPrice}"/> đ</span>
      </c:when>
      <c:otherwise>
          <span class="price text-green-600 font-bold"><fmt:formatNumber value="${variant.price}"/> đ</span>
      </c:otherwise>
  </c:choose>
  ```
