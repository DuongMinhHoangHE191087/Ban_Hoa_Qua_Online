# II.23 — Recently Viewed

## Feature Overview
Improves navigation and personalization by displaying a **"Recently Viewed Products"** slider at the bottom of the fruit catalog. It tracks product detail pages visited by the user using a client-side cookie and displays the results in chronological order.

---

## Domain Rules & Constraints
* **Cookie-Based Persistence**: Visited product IDs are stored in a client-side cookie named `recently_viewed_ids`.
* **Zero Database writes for Tracking**: Avoids database overhead. Tracking is done entirely in the browser using cookies, and the details are queried on-demand.
* **Size Limit**: Restricts the tracked history to the last **5 unique product IDs**.
* **Ordering Preservation**: When querying details for the tracked IDs, the DAO explicitly maintains the exact chronological order of the IDs in the cookie, rather than default SQL Server sorting.

---

## Workflow Details

### 1. Happy Path Flow
1. **Customer** opens the detail page for Orange (ID = 3), then Apple (ID = 8), then Grape (ID = 2).
2. On each visit, browser JavaScript appends the product ID to the `recently_viewed_ids` cookie (so it contains `"2,8,3"`).
3. The customer returns to the product catalog list page.
4. The servlet detects the `recently_viewed_ids` cookie, parses the comma-separated IDs into a list of integers, and fetches details via `ProductDAO.findRecentlyViewed([2, 8, 3])`.
5. In `ProductDAO.findRecentlyViewed()`, the sql is executed:
   ```sql
   SELECT * FROM products WHERE status = 'ACTIVE' AND product_id IN (2, 8, 3);
   ```
6. The DAO maps the results into a HashMap and constructs a list in the *exact input order* `[2, 8, 3]`.
7. The view renders the products in chronological order.

---

## Code Transformations

### 1. DAO Layer (`ProductDAO.java`)
* Implemented `findRecentlyViewed(List<Integer> productIds)` with input-order preservation:
  ```java
  public List<Product> findRecentlyViewed(List<Integer> productIds) throws SQLException {
      // Maps ResultSet rows into a Map<Integer, Product>
      // Iterates through input list and populates the returned List in exact chronological sequence
  }
  ```

### 2. View Layer (Javascript in `product-detail.jsp`)
* A script reads the existing `recently_viewed_ids` cookie, filters out the current ID if already present, prepends the current product ID, trims the size to a maximum of 5, and writes the updated comma-separated list back to the cookie path.
