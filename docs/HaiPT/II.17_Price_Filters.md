# II.17 — Price Filters

## Feature Overview
Enables buyers to filter the fruit catalog based on a price budget (minimum and maximum price). It uses a dual range slider UI in the sidebar, refreshing the catalog grid via AJAX.

---

## Domain Rules & Constraints
* **Discount-Aware Pricing**: The SQL query must evaluate the active price of the variant. If a promotional discount price is configured, it overrides the original price:
  ```sql
  COALESCE(pv.discount_price, pv.price) BETWEEN ? AND ?
  ```
* **Price Swapping Logic**: If minPrice is greater than maxPrice (due to input manipulation or slider edge cases), the servlet dynamically swaps the values (`minPrice <-> maxPrice`) before executing the DB search, avoiding empty result errors.

---

## Workflow Details

### 1. Happy Path Flow
1. **Customer** slides the price budget from 50,000 VNĐ to 150,000 VNĐ.
2. JavaScript triggers the filter query: `/products?ajax=true&minPrice=50000&maxPrice=150000`.
3. The servlet parses the parameters and delegates them to the search query.
4. The DAO executes the search:
   ```sql
   SELECT DISTINCT p.* FROM products p 
   JOIN product_variants pv ON p.product_id = pv.product_id 
   WHERE COALESCE(pv.discount_price, pv.price) >= 50000 
     AND COALESCE(pv.discount_price, pv.price) <= 150000;
   ```
5. The frontend displays the matching fruits (e.g., Apple 1kg at a discounted 120,000 VNĐ price).

### 2. Exception Flow (Bound Swapping)
1. An input error sends `minPrice=200000` and `maxPrice=50000`.
2. The servlet detects that `minPrice > maxPrice`.
3. It swaps them to `minPrice=50000` and `maxPrice=200000` and executes the query safely, returning the correct results.

---

## Code Transformations

### 1. DAO Layer (`ProductDAO.java`)
* Incorporates variant price logic:
  ```sql
  AND COALESCE(pv.discount_price, pv.price) >= ?
  AND COALESCE(pv.discount_price, pv.price) <= ?
  ```

### 2. Service/Controller Layer (`ProductListServlet.java`)
* Validates inputs and implements price-swapping:
  ```java
  if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
      BigDecimal temp = minPrice;
      minPrice = maxPrice;
      maxPrice = temp;
  }
  ```

### 3. View Layer (`product-list.jsp`)
* Integrates a dual price range slider. Sliding updates min/max input numbers and alerts the AJAX listener.
