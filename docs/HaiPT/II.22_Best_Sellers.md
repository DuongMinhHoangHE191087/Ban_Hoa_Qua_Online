# II.22 — Best Sellers

## Feature Overview
Highlights the most popular products on the home and product catalog pages through a **"Best Sellers"** panel. This increases conversions by pointing users directly to trending, high-volume products.

---

## Domain Rules & Constraints
* **Fulfillment-Based Popularity**: The sales metrics are compiled exclusively from items in successfully completed/delivered orders (`o.status = 'DELIVERED'`). Draft, pending, or canceled orders are completely ignored.
* **Database Aggregation**: Uses a database `JOIN` and `SUM` query grouping by product properties to guarantee real-time accuracy.
* **Limit**: Displays a maximum of **5 best-selling items**.

---

## Workflow Details

### 1. Happy Path Flow
1. A **Customer** lands on the main fruit catalog page.
2. The controller calls `ProductDAO.findBestSellers(limit=5)`.
3. The database executes the aggregation:
   ```sql
   SELECT TOP (5) p.*, SUM(oi.quantity) as sold 
   FROM products p 
   JOIN order_items oi ON p.product_id = oi.product_id 
   JOIN orders o ON oi.order_id = o.order_id 
   WHERE o.status = 'DELIVERED' 
   GROUP BY p.product_id, ... 
   ORDER BY sold DESC;
   ```
4. The catalog page showcases the Top 5 products inside a beautifully styled "Best Sellers" carousel or card grid with custom badges.

---

## Code Transformations

### 1. DAO Layer (`ProductDAO.java`)
* Implemented `findBestSellers(int limit)` retrieving high-sales listings.

### 2. Controller Layer (`ProductListServlet.java`)
* Pre-fetches best-selling products and passes them to the template (`req.setAttribute("bestSellers", list)`).

### 3. View Layer (`product-list.jsp`)
* Incorporates a dedicated carousel block styled with premium dark/glassmorphic backgrounds.
