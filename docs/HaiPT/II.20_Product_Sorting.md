# II.20 — Product Sorting

## Feature Overview
Allows customers to sort the catalog grid based on their preferences: **Price: Low to High**, **Price: High to Low**, **Top Rated**, or **Newest** (default).

---

## Domain Rules & Constraints
* **SQL Server `SELECT DISTINCT` Sorting Constraint**: In SQL Server, if a query uses `SELECT DISTINCT`, any column specified in the `ORDER BY` clause must also appear in the select list. However, because our price is bound to the `product_variants` table (and we want to avoid duplicate parent rows), sorting directly by `pv.price` causes a SQL error.
* **Scalar Subquery Solution**: To solve this constraint, the DAO executes a dependent scalar subquery inside the `ORDER BY` clause, finding the minimum price (or discounted price) of the product's active variants:
  ```sql
  ORDER BY (SELECT MIN(COALESCE(pv2.discount_price, pv2.price)) 
            FROM product_variants pv2 
            WHERE pv2.product_id = p.product_id 
              AND pv2.is_active = 1) ASC
  ```
  This is 100% compliant with SQL Server constraints and runs efficiently.

---

## Workflow Details

### 1. Happy Path Flow
1. **Customer** selects "Price: Low to High" from the sorting dropdown menu.
2. JavaScript captures the dropdown value change and calls the backend: `/products?ajax=true&sortBy=price_asc`.
3. The DAO executes the search using the price-ascending scalar subquery.
4. The grid refreshes instantly with the cheapest fruits displayed first.

---

## Code Transformations

### 1. DAO Layer (`ProductDAO.java`)
* Handles the sorting options dynamically inside `searchAdvanced`:
  ```java
  if ("price_asc".equals(sortBy)) {
      sql.append("ORDER BY (SELECT MIN(COALESCE(pv2.discount_price, pv2.price)) FROM product_variants pv2 WHERE pv2.product_id = p.product_id AND pv2.is_active = 1) ASC ");
  } else if ("price_desc".equals(sortBy)) {
      sql.append("ORDER BY (SELECT MIN(COALESCE(pv2.discount_price, pv2.price)) FROM product_variants pv2 WHERE pv2.product_id = p.product_id AND pv2.is_active = 1) DESC ");
  } else if ("rating".equals(sortBy)) {
      sql.append("ORDER BY p.rating DESC ");
  } else {
      sql.append("ORDER BY p.product_id DESC ");
  }
  ```

### 2. View Layer (`product-list.jsp`)
* Renders a sorting dropdown element:
  ```html
  <select name="sortBy" id="sort-select">
      <option value="newest">Newest</option>
      <option value="price_asc">Price: Low to High</option>
      <option value="price_desc">Price: High to Low</option>
      <option value="rating">Top Rated</option>
  </select>
  ```
