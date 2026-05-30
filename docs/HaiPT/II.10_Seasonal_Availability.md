# II.10 — Seasonal Availability

## Feature Overview
Protects agricultural freshness by restricting purchases of fresh fruits to their natural seasonal periods (e.g., Lychee is only available from June to August). If the current date is outside a product's defined seasonal window, order creation is blocked.

---

## Domain Rules & Constraints
* **Month Range Validation**: Months are represented as integers (1 = Jan, 12 = Dec).
* **Seasonal Wraparound Support**: If a season begins in a late month of the year and ends in an early month of the next year (e.g., `season_start = 11` (November) and `season_end = 2` (February)), the month range wraps around. The product is active if the current month matches `(currentMonth >= 11 || currentMonth <= 2)`.
* **Behavior**: Buyers cannot add out-of-season products to their cart. If they manipulate post data, the checkout servlet rejects the order submission.

---

## Workflow Details

### 1. Happy Path Flow
1. **Shop Owner** defines a fruit's seasonal window (e.g., Mangoes from March (3) to July (7)).
2. A **Customer** visits the site in April (current month is 4, which is in range).
3. The "Add to Cart" button is active. The customer completes their order cleanly.

### 2. Out-of-Season Flow
1. A **Customer** visits the same product page in October (current month is 10, which is outside the range [3, 7]).
2. The product detail page displays an **"Out of Season"** warning bar, and the **"Add to Cart"** button is disabled.
3. If an unauthorized client bypasses the disabled UI and submits a checkout request, the `OrderService` transaction intercepts the checkout request, matches the current month against the product season months, detects it is out-of-season, rolls back the transaction, and returns a user-friendly error.

---

## Database Changes
Implemented in [database/Schema.sql](file:///c:/Users/WELCOME/Documents/GitHub/Ban_Hoa_Qua_Online/database/Schema.sql):
* Extended `dbo.products` table:
  ```sql
  ALTER TABLE dbo.products ADD season_start INT NULL;
  ALTER TABLE dbo.products ADD season_end INT NULL;
  ```

---

## Code Transformations

### 1. Model Layer
* Added `seasonStart` and `seasonEnd` fields in `Product.java`.

### 2. Service Layer (`OrderService.java` — Checkout Validation)
* Extracted the current month using `java.time.LocalDate.now().getMonthValue()`.
* Implemented seasonal checker logic:
  ```java
  public boolean isProductInSeason(Product p) {
      if (p.getSeasonStart() == null || p.getSeasonEnd() == null) return true;
      int start = p.getSeasonStart();
      int end = p.getSeasonEnd();
      int cur = LocalDate.now().getMonthValue();
      if (start <= end) {
          return (cur >= start && cur <= end);
      } else {
          return (cur >= start || cur <= end); // Wraparound Nov to Feb
      }
  }
  ```

### 3. View Layer (`product-detail.jsp`)
* Renders a seasonal warning and disables purchasing buttons dynamically.
