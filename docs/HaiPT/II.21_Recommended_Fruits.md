# II.21 — Recommended Fruits

## Feature Overview
Engages customers by displaying a **"Recommended Fruits"** grid on the product detail page. The recommendation engine displays fruits that belong to the same category and are sold by the same shop owner, excluding the current product, sorted by top-selling and highly-rated items.

---

## Domain Rules & Constraints
* **Recommendation Criteria**:
  * Match same category (`category_id = ?`).
  * Match same shop/owner (`owner_id = ?`).
  * Exclude currently viewed product (`product_id <> ?`).
  * Filter active listings only (`status = 'ACTIVE'`).
  * Sort by highest popularity (`sold_quantity DESC, rating DESC`).
* **Limit**: Recommends a maximum of **4 products** to prevent visual clutter and minimize database load.

---

## Workflow Details

### 1. Happy Path Flow
1. **Customer** opens the detail page for **Organic Honey Mango** (Category: Mangoes, Shop: GreenGarden, ID: 12).
2. The `ProductDetailServlet.java` catches the load event.
3. The controller calls `ProductDAO.findRecommendations(productId=12, categoryId=3, ownerId=4, limit=4)`.
4. The database queries:
   ```sql
   SELECT * FROM products 
   WHERE category_id = ? AND owner_id = ? AND product_id <> ? AND status = 'ACTIVE' 
   ORDER BY sold_quantity DESC, rating DESC 
   OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY;
   ```
5. The UI renders the recommendation section at the bottom of the page, showcasing up to 4 similar items from GreenGarden shop.

---

## Code Transformations

### 1. DAO Layer (`ProductDAO.java`)
* Implemented `findRecommendations(int productId, int categoryId, int ownerId, int limit)` returning matching products.

### 2. Controller Layer (`ProductDetailServlet.java`)
* Loads the recommended list and binds it to the request scope (`req.setAttribute("recommendations", list)`).

### 3. View Layer (`product-detail.jsp`)
* Renders a responsive slider or grid of recommended cards using a clean glassmorphic style.
