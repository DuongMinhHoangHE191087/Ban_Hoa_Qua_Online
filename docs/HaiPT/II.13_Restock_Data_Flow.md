# II.13 — Restock Management: Complete Data Flow

> Step-by-step explanation of how the Restock use case works, tracing every interaction between **Browser ↔ JSP ↔ Servlet ↔ Service ↔ DAO ↔ Database**.

---

## All Files Involved (11 files)

| # | Layer | File | Role |
|---|-------|------|------|
| 1 | **DB Schema** | `database/Schema.sql` (L97-L129) | `product_variants` + `inventory_logs` table definitions |
| 2 | **Model** | `model/entity/ProductVariant.java` | Entity POJO — maps `product_variants` table |
| 3 | **Model** | `model/entity/InventoryLog.java` | Entity POJO — maps `inventory_logs` table |
| 4 | **DAO Base** | `dao/base/BaseDAO.java` | Provides `getConnection()` via JDBC |
| 5 | **DAO** | `dao/ProductVariantDAO.java` (L220-L250) | `restockVariant()` — transactional UPDATE + INSERT |
| 6 | **DAO** | `dao/InventoryDAO.java` | `findLogsByOwner()` — reads audit trail for GET |
| 7 | **Service** | `service/ProductService.java` (L99-L115) | `restock()` — validates `quantity > 0`, delegates to DAO |
| 8 | **Servlet** | `servlet/shop/InventoryServlet.java` | Controller — GET loads page, POST handles restock |
| 9 | **View** | `WEB-INF/jsp/shop/inventory.jsp` | JSP page — restock form, stock table, audit log table |
| 10 | **Config** | `config/AppConfig.java` | Constants: `ROLE_SHOP_OWNER`, session keys |
| 11 | **Util** | `util/SessionUtil.java` | `flashSuccess()`, `flashError()`, `isLoggedIn()`, `hasRole()` |

**Supporting infrastructure** (not HaiPT-specific, but touched at runtime):

| File | Role |
|------|------|
| `filter/CsrfFilter.java` | Validates `_csrf` token on POST |
| `WEB-INF/jsp/common/header.jsp` / `footer.jsp` | Page chrome |
| `WEB-INF/tld/fruitmkt.tld` | `<ft:currency>` tag used for price formatting |

---

## Phase 1: Loading the Page (GET Request)

### Step 1 — Browser sends GET request
```
User clicks "Quản lý kho" link → Browser sends:  GET /shop/inventory
```

### Step 2 — CsrfFilter intercepts (before servlet)
**File:** `filter/CsrfFilter.java` (L32-35)

Since this is a GET (not POST), the filter only ensures a CSRF token exists in session, then passes through:
```java
// Creates token if not present
if (session.getAttribute("_csrfToken") == null) {
    session.setAttribute("_csrfToken", UUID.randomUUID().toString());
}
chain.doFilter(request, response);  // → pass to InventoryServlet
```

### Step 3 — InventoryServlet.doGet() runs
**File:** `servlet/shop/InventoryServlet.java` (L37-62)

**3a. Auth check** (L40-44):
```java
if (!SessionUtil.isLoggedIn(session) || !SessionUtil.hasRole(session, "SHOP_OWNER")) {
    resp.sendRedirect("/auth/login");  // ← kick out if not shop owner
    return;
}
```

**3b. Get current user** (L46):
```java
User user = SessionUtil.getCurrentUser(session);  // gets User object from session
```

**3c. Load data from database** (L50-52) — TWO database queries happen:
```java
// Query 1: Get all product variants owned by this shop owner
variants = productVariantDAO.findByOwner(user.getUserId());

// Query 2: Get all inventory change logs for this shop owner
logs = inventoryDAO.findLogsByOwner(user.getUserId());
```

### Step 4 — ProductVariantDAO.findByOwner() hits the database
**File:** `dao/ProductVariantDAO.java` (L59-72)
```sql
SELECT pv.*
FROM product_variants pv
JOIN products p ON pv.product_id = p.product_id
WHERE p.owner_id = ?        ← binds current user's ID
  AND pv.is_active = 1
ORDER BY p.name ASC, pv.variant_label ASC
```
Returns → `List<ProductVariant>` (each has: variantId, sku, variantLabel, price, stockQuantity, etc.)

### Step 5 — InventoryDAO.findLogsByOwner() hits the database
**File:** `dao/InventoryDAO.java` (L24-41)
```sql
SELECT il.*
FROM inventory_logs il
JOIN product_variants pv ON il.variant_id = pv.variant_id
JOIN products p ON pv.product_id = p.product_id
WHERE p.owner_id = ?        ← binds current user's ID
ORDER BY il.changed_at DESC
```
Returns → `List<InventoryLog>` (each has: logId, variantId, changeType, quantityDelta, quantityAfter, note, changedAt)

### Step 6 — Servlet sets request attributes and forwards to JSP
**File:** `servlet/shop/InventoryServlet.java` (L58-61)
```java
req.setAttribute("variants", variants);  // → ${variants} in JSP
req.setAttribute("logs", logs);          // → ${logs} in JSP

req.getRequestDispatcher("/WEB-INF/jsp/shop/inventory.jsp").forward(req, resp);
```
> **Key point:** `forward()` is an internal server-side redirect. The browser URL stays `/shop/inventory`. The JSP is behind WEB-INF so it can't be accessed directly.

### Step 7 — inventory.jsp renders HTML using the data

**7a. Flash message** (L20-30) — shows success/error from previous POST:
```jsp
<c:if test="${not empty sessionScope.flashMsg}">
    <!-- Green box for success, Red box for error -->
    <c:out value="${sessionScope.flashMsg}"/>
</c:if>
```

**7b. Restock form** (L41-72) — the `<select>` dropdown is populated from `${variants}`:
```jsp
<form action="${pageContext.request.contextPath}/shop/inventory" method="POST">
    <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}" />

    <select name="variantId">
        <c:forEach var="v" items="${variants}">     ← loops over List<ProductVariant>
            <option value="${v.variantId}">          ← sends variant_id on submit
                ${v.sku} (${v.variantLabel}) - Tồn: ${v.stockQuantity} quả
            </option>
        </c:forEach>
    </select>

    <input type="number" name="quantity" min="1" />
    <textarea name="note"></textarea>
</form>
```

**7c. Stock table** (L86-118) — loops over same `${variants}`:
```jsp
<c:forEach var="v" items="${variants}">
    <tr>
        <td>${v.sku}</td>
        <td>${v.variantLabel}</td>
        <td><ft:currency value="${v.price}"/></td>     ← custom tag formats VND
        <td>${v.stockQuantity} quả</td>
        <!-- Low stock badge: red if 0, orange if ≤5, green otherwise -->
    </tr>
</c:forEach>
```

**7d. Audit log table** (L149-168) — loops over `${logs}`:
```jsp
<c:forEach var="log" items="${logs}">
    <tr>
        <td>#${log.logId}</td>
        <td>${log.changeType}</td>        ← "MANUAL_ADJUST", "ORDER_RESERVE", etc.
        <td>${log.quantityDelta} quả</td> ← +50, -3, etc.
        <td>${log.quantityAfter} quả</td>
        <td>${log.note}</td>
        <td>formatted date</td>
    </tr>
</c:forEach>
```

**At this point**, the browser shows the fully rendered page. The user sees their variants, stock levels, and history.

---

## Phase 2: Submitting the Restock Form (POST Request)

### Step 8 — User fills the form and clicks submit

The form sends:
```
POST /shop/inventory
Body: _csrf=abc123&variantId=5&quantity=50&note=Nhập+hàng+mùa+hè
```

**But first**, the client-side JS validates (L182-196):
```javascript
restockForm.addEventListener("submit", function(e) {
    const qty = parseInt(quantityInput.value);
    if (isNaN(qty) || qty <= 0) {
        alert("Lỗi: Số lượng nhập kho phải lớn hơn 0 quả!");
        e.preventDefault();  // ← BLOCKS the form submission
        return;
    }
    // If valid → form submits normally
});
```

### Step 9 — CsrfFilter validates the CSRF token
**File:** `filter/CsrfFilter.java` (L38-47)
```java
// This IS a POST, so filter checks token
String sessionToken = session.getAttribute("_csrfToken");  // from session
String requestToken = req.getParameter("_csrf");           // from form hidden field
if (!sessionToken.equals(requestToken)) {
    resp.sendError(403, "CSRF token không hợp lệ.");       // ← BLOCKED
    return;
}
chain.doFilter(request, response);  // ← token OK, pass to servlet
```

### Step 10 — InventoryServlet.doPost() processes the restock
**File:** `servlet/shop/InventoryServlet.java` (L64-130)

**10a. Auth check** (same as GET)

**10b. Parse form parameters** (L77-78):
```java
int variantId = Integer.parseInt(req.getParameter("variantId"));  // → 5
int quantity  = Integer.parseInt(req.getParameter("quantity"));   // → 50
```

**10c. Server-side validation** (L81-83):
```java
if (quantity <= 0) {
    throw new IllegalArgumentException("Số lượng nhập kho phải lớn hơn 0.");
}
```

**10d. Ownership check** (L85-105) — verifies this variant belongs to the logged-in shop:
```java
ProductVariant target = productVariantDAO.findById(variantId);
// Then checks if variantId exists in the owner's list
List<ProductVariant> ownedVariants = productVariantDAO.findByOwner(user.getUserId());
// If not found → throws "Bạn không có quyền nhập kho cho biến thể này."
```

**10e. Parse note** (L107-115):
```java
String note = req.getParameter("note");
if (note == null || note.trim().isEmpty()) {
    note = "Manual restock of 50 units.";  // default
}
```

**10f. Call service** (L117):
```java
productService.restock(variantId, quantity, user.getUserId(), note);
```

### Step 11 — ProductService.restock() validates and delegates
**File:** `service/ProductService.java` (L100-108)
```java
public void restock(int variantId, int quantity, int userId, String note) {
    if (quantity <= 0) throw ...;  // double-check
    productVariantDAO.restockVariant(variantId, quantity, userId, note);
}
```

### Step 12 — ProductVariantDAO.restockVariant() — THE CORE (single transaction)
**File:** `dao/ProductVariantDAO.java` (L220-250)

```java
conn.setAutoCommit(false);  // ← START TRANSACTION

// SQL 1: Increase stock
UPDATE product_variants
SET stock_quantity = stock_quantity + 50, updated_at = GETDATE()
WHERE variant_id = 5;
// → checks rows affected, rollback if 0

// SQL 2: Write audit log
INSERT INTO inventory_logs
    (variant_id, changed_by, change_type, quantity_delta, quantity_after, note, changed_at)
VALUES
    (5, 3, 'MANUAL_ADJUST', 50,
     (SELECT stock_quantity FROM product_variants WHERE variant_id = 5),  ← reads AFTER update
     'Nhập hàng mùa hè', GETDATE());

conn.commit();  // ← COMMIT (both succeed or both fail)
```

> **Why single transaction?** If the log INSERT fails, the stock UPDATE is rolled back. The audit trail is always consistent with actual stock levels.

### Step 13 — Success flows back up the call chain
```
DAO returns void → Service returns void → Servlet sets flash message:
```
```java
SessionUtil.flashSuccess(session, "Cập nhật số lượng tồn kho thành công!");
// This stores in session:  flashMsg = "...",  flashType = "success"
```

### Step 14 — PRG Redirect (Post-Redirect-Get pattern)
**File:** `servlet/shop/InventoryServlet.java` (L129)
```java
resp.sendRedirect(req.getContextPath() + "/shop/inventory");
// Browser receives: HTTP 302 → Location: /shop/inventory
```

> **Why redirect instead of forward?** The **PRG pattern** prevents duplicate submissions. If the user refreshes the page after a forward, the browser would re-POST the form. With redirect, refresh only re-does the GET.

### Step 15 — Browser follows the redirect → back to Step 1
```
Browser sends:  GET /shop/inventory  (automatic, from the 302)
```

The whole **Phase 1** repeats. This time:
- The stock table shows **updated quantity** (e.g., was 20, now 70)
- The audit log table shows the **new MANUAL_ADJUST entry**
- The flash message shows **green success banner** (then gets cleared from session on L29)

---

## Visual Summary

```
┌──────────────────────────────────────────────────────────────┐
│                    PHASE 1: GET (Load Page)                  │
│                                                              │
│  Browser ──GET──→ CsrfFilter ──→ InventoryServlet.doGet()   │
│                                        │                     │
│                                   ┌────┴────┐               │
│                                   ▼         ▼               │
│                          ProductVariant  InventoryDAO        │
│                          DAO.findByOwner .findLogsByOwner    │
│                                   │         │               │
│                                   ▼         ▼               │
│                              SQL Server (2 SELECT queries)   │
│                                   │         │               │
│                                   └────┬────┘               │
│                                        ▼                     │
│                         req.setAttribute("variants", ...)    │
│                         req.setAttribute("logs", ...)        │
│                                        │                     │
│                              forward → inventory.jsp         │
│                                        │                     │
│                                        ▼                     │
│  Browser ←── rendered HTML ────────────┘                     │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│                   PHASE 2: POST (Restock)                    │
│                                                              │
│  Browser ──POST──→ CsrfFilter ──→ InventoryServlet.doPost() │
│  (variantId=5,       (validates      │                       │
│   quantity=50,        _csrf token)   ├─ parse params         │
│   note=...,                          ├─ validate qty > 0     │
│   _csrf=abc)                         ├─ check ownership      │
│                                      │                       │
│                                      ▼                       │
│                              ProductService.restock()        │
│                                      │                       │
│                                      ▼                       │
│                          ProductVariantDAO.restockVariant()   │
│                                      │                       │
│                              ┌───────┴───────┐              │
│                              ▼               ▼              │
│                      UPDATE stock    INSERT audit log        │
│                         +50              MANUAL_ADJUST       │
│                              └───────┬───────┘              │
│                                   COMMIT                     │
│                                      │                       │
│                                      ▼                       │
│                         flashSuccess("Cập nhật...!")         │
│                                      │                       │
│                          302 Redirect → /shop/inventory      │
│                                      │                       │
│  Browser ──GET──→ (Phase 1 repeats with updated data)        │
└──────────────────────────────────────────────────────────────┘
```

---

## Error Handling Flows

### Error Flow A: Invalid quantity (negative or zero)
```
Browser → JS blocks form submission (alert shown)
If JS bypassed → Servlet catches → throws IllegalArgumentException
  → flashError("Số lượng nhập kho phải lớn hơn 0.")
  → 302 redirect → page reloads with red error banner
```

### Error Flow B: Invalid CSRF token
```
Browser → CsrfFilter → token mismatch → HTTP 403 error page
  → form never reaches servlet
```

### Error Flow C: Variant doesn't belong to this shop owner
```
Browser → Servlet → ownership check fails
  → throws IllegalArgumentException("Bạn không có quyền nhập kho cho biến thể này.")
  → flashError → 302 redirect → red error banner
```

### Error Flow D: Database error
```
Browser → Servlet → Service → DAO → SQL error
  → DAO: conn.rollback() (stock NOT changed, log NOT written)
  → throws SQLException → Servlet catches
  → flashError("Cập nhật tồn kho thất bại: Lỗi cơ sở dữ liệu.")
  → 302 redirect → red error banner
```

---

## Key Design Patterns Used

| Pattern | Where | Why |
|---------|-------|-----|
| **PRG** (Post-Redirect-Get) | Servlet L129 | Prevents form resubmission on browser refresh |
| **Flash Messages** | SessionUtil → JSP L20-29 | Shows success/error after redirect (survives the redirect in session, cleared on display) |
| **CSRF Protection** | Hidden field L42 + CsrfFilter | Prevents cross-site form forgery |
| **3-Layer Validation** | JS → Servlet → DAO | Defense in depth: client, server, and database level |
| **Transaction** | DAO L225: `setAutoCommit(false)` | Ensures stock + log are always consistent |
| **Forward vs Redirect** | GET=forward, POST=redirect | Forward shares request data; redirect prevents re-POST |

---

## Database Tables Used

### `product_variants` (Schema.sql L97-111)
```sql
CREATE TABLE product_variants (
    variant_id INT IDENTITY(1,1) PRIMARY KEY,
    product_id INT NOT NULL FOREIGN KEY REFERENCES products(product_id),
    sku NVARCHAR(50) NOT NULL UNIQUE,
    variant_label NVARCHAR(100) NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,    -- ← Restock modifies this
    weight_grams INT NULL,
    discount_price DECIMAL(12,2) NULL,
    packaging_option NVARCHAR(50) NULL,
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME NOT NULL DEFAULT GETDATE()
);
```

### `inventory_logs` (Schema.sql L119-129)
```sql
CREATE TABLE inventory_logs (
    log_id INT IDENTITY(1,1) PRIMARY KEY,
    variant_id INT NOT NULL FOREIGN KEY REFERENCES product_variants(variant_id),
    changed_by INT NOT NULL FOREIGN KEY REFERENCES users(user_id),
    change_type NVARCHAR(20) NOT NULL CHECK (change_type IN
        ('MANUAL_ADJUST','ORDER_RESERVE','ORDER_RELEASE','ORDER_CONFIRM','RETURN')),
    quantity_delta INT NOT NULL,         -- ← +50 for restock
    quantity_after INT NOT NULL,         -- ← stock level AFTER the change
    note NVARCHAR(300) NULL,            -- ← user's note or default
    changed_at DATETIME NOT NULL DEFAULT GETDATE()
);
```
