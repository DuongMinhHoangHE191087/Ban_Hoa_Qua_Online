# II.8 — Packaging Options

## Feature Overview
Allows buyers to customize the presentation of their fruit purchases (e.g., selecting standard packaging, a high-end **Gift Box**, or a protective **Foam Tray**). This enhances the shopping experience for users buying fruit as corporate or holiday gifts.

---

## Domain Rules & Constraints
* **Packaging Costs**:
  * **Gift Box**: Adds **50,000 VNĐ** to the order item base price.
  * **Foam Tray**: Adds **15,000 VNĐ** to the order item base price.
* **Constraints**: The packaging option values must match the database CHECK constraint: `'Gift Box'` or `'Foam Tray'`.

---

## Workflow Details

### 1. Happy Path Flow
1. **Customer** opens a product's details page.
2. In the configuration panel, they select their preferred packaging option from a dropdown (e.g., "Gift Box (+50,000 VNĐ)").
3. The page dynamically displays the added price update using interactive JavaScript.
4. The customer adds the product to their cart.
5. During checkout, the system calculates the additional cost and adds it to the order item total, storing the selected packaging snapshot in the database.

### 2. Exception / Error Flow
1. If an invalid packaging option value is manipulated on the client side and sent to the server, the server-side validator in `OrderService` rejects the order, or the database throws a check constraint failure (`CK_product_variants_packaging`), safely aborting the operation.

---

## Database Changes
Implemented in [database/Schema.sql](file:///c:/Users/WELCOME/Documents/GitHub/Ban_Hoa_Qua_Online/database/Schema.sql):
* Extended `dbo.product_variants` table:
  ```sql
  ALTER TABLE dbo.product_variants ADD packaging_option NVARCHAR(50) NULL;
  ALTER TABLE dbo.product_variants ADD CONSTRAINT CK_product_variants_packaging 
  CHECK (packaging_option IN ('Gift Box', 'Foam Tray'));
  ```

---

## Code Transformations

### 1. Model Layer
* Added `packagingOption` field in `ProductVariant.java`:
  ```java
  private String packagingOption;
  // Getters and setters
  ```

### 2. Service Layer (`OrderService.java`)
* During checkout execution, the system reads the chosen packaging option from the cart/request, matches it against constants, adds the respective fee (**50k** for Gift Box, **15k** for Foam Tray) to the line-item calculations, and commits the snapshot into the `order_items` audit schema.

### 3. View Layer (`product-detail.jsp`)
* Renders a dropdown list of available packaging add-ons. Selecting an option triggers a Javascript cost calculation that instantly updates the displayed price.
