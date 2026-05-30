# II.18 — Rating Filters

## Feature Overview
Allows buyers to screen the catalog for high-quality fruits by filtering products based on their average star ratings (e.g., "4 Stars & Up", "5 Stars only"). This leverages previous buyer reviews to elevate trusted sellers and quality products.

---

## Domain Rules & Constraints
* **Precision Rating Bounds**: Star ratings are stored as fractional decimals (e.g., `4.5` or `4.8`) in the products table.
* **SQL Filter Constraint**: The query matches products where `avg_rating >= inputStarRating`.

---

## Workflow Details

### 1. Happy Path Flow
1. **Customer** checks the "4 Stars & Up" rating option on the sidebar.
2. JavaScript sends the query parameter: `/products?ajax=true&rating=4.0`.
3. The DAO executes the search:
   ```sql
   SELECT DISTINCT p.* FROM products p WHERE p.rating >= 4.0 AND p.status = 'ACTIVE';
   ```
4. The catalog updates to show only highly-rated items.

---

## Code Transformations

### 1. DAO Layer (`ProductDAO.java`)
* Appends the rating filter condition:
  ```java
  if (rating != null) {
      sql.append("AND p.rating >= ? ");
      params.add(rating);
  }
  ```

### 2. View Layer (`product-list.jsp`)
* Renders a rating filter selector with star indicators:
  ```html
  <div class="rating-filter">
      <input type="radio" name="rating" value="4.0" id="rate-4">
      <label for="rate-4">★★★★☆ & Up</label>
  </div>
  ```
  Selecting a star rating fires the global AJAX grid-update query.
