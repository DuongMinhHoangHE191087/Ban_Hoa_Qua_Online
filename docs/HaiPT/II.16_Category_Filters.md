# II.16 — Category Filters

## Feature Overview
Provides a visual sidebar checkbox category filter in the product catalog page (`product-list.jsp`). Clicking on categories dynamically refreshes the product grid via instant **AJAX** requests, without page reloads, using pre-compiled CSS transitions.

---

## Domain Rules & Constraints
* **Multi-Select Filtering**: Allows filtering by multiple categories concurrently.
* **Database Isolation**: The SQL SELECT query dynamically constructs safe JDBC parameter arrays using PreparedStatement variables to prevent SQL injection.

---

## Workflow Details

### 1. Happy Path Flow
1. **Customer** loads `/products`.
2. On the sidebar filter, they check the **"Citrus Fruits"** and **"Berries"** checkboxes.
3. JavaScript listens for the state change, aggregates the selected category IDs in an array (e.g., `[2, 5]`), and triggers a GET request: `/products?ajax=true&categoryId=2&categoryId=5`.
4. `ProductListServlet.java` parses the request parameters, calls `ProductDAO.searchAdvanced(...)`.
5. The DAO executes the dynamic SQL query:
   ```sql
   SELECT DISTINCT p.* FROM products p WHERE p.category_id IN (?, ?) AND p.status = 'ACTIVE';
   ```
6. The servlet returns a JSON payload or a rendered JSP fragment containing the matching fruits.
7. JavaScript updates the product grid instantly with a smooth fade-in animation.

---

## Code Transformations

### 1. DAO Layer (`ProductDAO.java` — `searchAdvanced`)
* Dynamically constructs the SQL clause `AND p.category_id IN (?, ?, ...)` based on the size of the category IDs array, and binds each element safely using parameterized JDBC index inputs.

### 2. Controller Layer (`ProductListServlet.java`)
* Parses multi-value parameters `req.getParameterValues("categoryId")` into lists of integers and feeds them into the advanced searching service interface.

### 3. View Layer (`product-list.jsp` / AJAX script)
* Binds events to category checkboxes. On click, it queries the backend API and updates the catalog grid container dynamically using jQuery.
