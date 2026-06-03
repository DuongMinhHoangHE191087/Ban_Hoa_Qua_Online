# II.13 — Restock Management: Class & Method Reference

> Giải thích chi tiết **mọi attribute và function** trong từng class liên quan đến chức năng Restock (II.13), đánh dấu rõ cái nào ảnh hưởng trực tiếp đến logic flow.

**Quy ước đánh dấu:**
- 🔴 **TRỰC TIẾP** = attribute/method được gọi/dùng trực tiếp trong logic flow của Restock
- ⚪ **GIÁN TIẾP** = tồn tại trong class nhưng KHÔNG được gọi bởi logic Restock

---

## Mục Lục

1. [AppConfig.java — Hằng số cấu hình](#1-appconfigjava--hằng-số-cấu-hình)
2. [User.java — Entity người dùng](#2-userjava--entity-người-dùng)
3. [ProductVariant.java — Entity biến thể sản phẩm](#3-productvariantjava--entity-biến-thể-sản-phẩm)
4. [InventoryLog.java — Entity nhật ký kho](#4-inventorylogjava--entity-nhật-ký-kho)
5. [BaseDAO.java — Lớp cha của mọi DAO](#5-basedaojava--lớp-cha-của-mọi-dao)
6. [ProductVariantDAO.java — DAO biến thể sản phẩm](#6-productvariantdaojava--dao-biến-thể-sản-phẩm)
7. [InventoryDAO.java — DAO nhật ký kho](#7-inventorydaojava--dao-nhật-ký-kho)
8. [ProductService.java — Service xử lý nghiệp vụ](#8-productservicejava--service-xử-lý-nghiệp-vụ)
9. [SessionUtil.java — Tiện ích session](#9-sessionutiljava--tiện-ích-session)
10. [CsrfFilter.java — Bộ lọc bảo mật CSRF](#10-csrffilterjava--bộ-lọc-bảo-mật-csrf)
11. [InventoryServlet.java — Controller chính](#11-inventoryservletjava--controller-chính)

---

## 1. AppConfig.java — Hằng số cấu hình

**Package:** `com.fruitmkt.config`
**Vai trò:** Chứa TẤT CẢ hằng số (magic number/string) dùng chung trong toàn bộ dự án. Khi cần thay đổi giá trị, chỉ sửa duy nhất file này.

### Attributes được dùng bởi Restock:

| Attribute | Kiểu | Giá trị | Vai trò trong Restock |
|-----------|------|---------|----------------------|
| 🔴 `DB_JDBC_URL` | `String` | `"jdbc:sqlserver://localhost:1433;..."` | BaseDAO dùng để mở connection tới SQL Server |
| 🔴 `DB_USER` | `String` | `"sa"` | Username kết nối database |
| 🔴 `DB_PASSWORD` | `String` | `"123"` | Password kết nối database |
| 🔴 `DB_DRIVER_CLASS` | `String` | `"com.microsoft.sqlserver.jdbc.SQLServerDriver"` | BaseDAO load driver này trong static block |
| 🔴 `SESSION_USER` | `String` | `"currentUser"` | Key để lấy User object từ session. SessionUtil dùng key này |
| 🔴 `SESSION_FLASH_MSG` | `String` | `"flashMsg"` | Key lưu flash message sau PRG redirect |
| 🔴 `SESSION_FLASH_TYPE` | `String` | `"flashType"` | Key lưu loại flash: `"success"` hoặc `"error"` |
| 🔴 `SESSION_CSRF_TOKEN` | `String` | `"_csrfToken"` | Key lưu CSRF token trong session. CsrfFilter đối chiếu với form |
| 🔴 `ROLE_SHOP_OWNER` | `String` | `"SHOP_OWNER"` | InventoryServlet kiểm tra role này trong auth check |

### Attributes KHÔNG dùng bởi Restock (tham khảo):

| Attribute | Lý do không dùng |
|-----------|------------------|
| ⚪ `PAGE_SIZE_*` | Restock không phân trang |
| ⚪ `ROLE_CUSTOMER`, `ROLE_ADMIN`, `ROLE_DELIVERY` | Chỉ SHOP_OWNER mới dùng inventory |
| ⚪ `ORDER_*`, `DELIVERY_*`, `RETURN_*` | Thuộc use case khác |
| ⚪ `GOOGLE_*`, `EMAIL_*` | Thuộc auth/email |
| ⚪ `MAX_UPLOAD_SIZE_BYTES`, `UPLOAD_DIR` | Restock không upload file |

---

## 2. User.java — Entity người dùng

**Package:** `com.fruitmkt.model.entity`
**Vai trò:** POJO ánh xạ bảng `users` trong DB. Được lưu trong session sau khi login, servlet đọc từ session để lấy thông tin shop owner.

### Attributes:

| Attribute | Kiểu | DB Column | Vai trò trong Restock |
|-----------|------|-----------|----------------------|
| 🔴 `userId` | `int` | `user_id` | Truyền vào DAO để: (1) lọc variants theo owner, (2) ghi `changed_by` vào `inventory_logs` |
| 🔴 `role` | `String` | `role` | Servlet kiểm tra `role == "SHOP_OWNER"` trước khi cho truy cập |
| ⚪ `fullName` | `String` | `full_name` | Không dùng trong Restock logic |
| ⚪ `email` | `String` | `email` | Không dùng |
| ⚪ `passwordHash` | `String` | `password_hash` | Chỉ dùng khi login |
| ⚪ `phone` | `String` | `phone` | Không dùng |
| ⚪ `status` | `String` | `status` | Đã kiểm tra khi login, không check lại |
| ⚪ `userAddress` | `String` | `user_address` | Không dùng |
| ⚪ `isEmailVerified` | `boolean` | `is_email_verified` | Đã check khi login |
| ⚪ `failedLoginCount` | `int` | `failed_login_count` | Thuộc auth |
| ⚪ `lockedUntil` | `LocalDateTime` | `locked_until` | Thuộc auth |
| ⚪ `createdAt` | `LocalDateTime` | `created_at` | Metadata |
| ⚪ `updatedAt` | `LocalDateTime` | `updated_at` | Metadata |

### Methods:

| Method | Vai trò trong Restock |
|--------|----------------------|
| 🔴 `getUserId()` | Servlet gọi để lấy ID truyền vào DAO queries |
| 🔴 `getRole()` | SessionUtil.hasRole() gọi để so sánh với `"SHOP_OWNER"` |
| ⚪ `isLocked()` | Không gọi trong Restock |
| ⚪ `isActive()` | Không gọi trong Restock |

---

## 3. ProductVariant.java — Entity biến thể sản phẩm

**Package:** `com.fruitmkt.model.entity`
**Vai trò:** POJO ánh xạ bảng `product_variants`. Mỗi sản phẩm (Product) có nhiều biến thể (theo trọng lượng, đóng gói). Restock thay đổi `stockQuantity` của 1 biến thể.

### Attributes:

| Attribute | Kiểu | DB Column | Vai trò trong Restock |
|-----------|------|-----------|----------------------|
| 🔴 `variantId` | `int` | `variant_id` | PK — form gửi lên, servlet parse, truyền vào DAO để UPDATE |
| 🔴 `productId` | `int` | `product_id` | FK tới `products` — dùng trong JOIN để lọc theo owner |
| 🔴 `sku` | `String` | `sku` | Hiển thị trong JSP dropdown: `<option>${v.sku} (${v.variantLabel})</option>` |
| 🔴 `variantLabel` | `String` | `variant_label` | Hiển thị trong JSP dropdown cùng SKU |
| 🔴 `stockQuantity` | `int` | `stock_quantity` | **CỐT LÕI** — đây là giá trị mà Restock TĂNG lên bằng `stock_quantity + ?`. Hiển thị trong bảng tồn kho và badge cảnh báo |
| 🔴 `price` | `BigDecimal` | `price` | Hiển thị trong bảng tồn kho JSP (`<ft:currency value="${v.price}"/>`) |
| ⚪ `weightGrams` | `Integer` | `weight_grams` | Không dùng trực tiếp trong Restock logic |
| ⚪ `discountPrice` | `BigDecimal` | `discount_price` | Không dùng |
| ⚪ `packagingOption` | `String` | `packaging_option` | Không dùng |
| 🔴 `isActive` | `boolean` | `is_active` | DAO lọc `WHERE is_active = 1` — biến thể đã bị deactivate không hiện |
| ⚪ `createdAt` | `LocalDateTime` | `created_at` | Metadata |
| ⚪ `updatedAt` | `LocalDateTime` | `updated_at` | DAO UPDATE SET `updated_at = GETDATE()` nhưng không đọc lại |

### Methods:

Tất cả là getter/setter thuần túy. Getter được đánh dấu 🔴 tương ứng với attribute 🔴 ở trên.

| Method | Nơi gọi |
|--------|---------|
| 🔴 `getVariantId()` | Servlet so sánh khi check ownership: `ov.getVariantId() == variantId` |
| 🔴 `getSku()` | JSP: `${v.sku}` |
| 🔴 `getVariantLabel()` | JSP: `${v.variantLabel}` |
| 🔴 `getStockQuantity()` | JSP: `${v.stockQuantity}` để hiển thị tồn kho + low-stock badge |
| 🔴 `getPrice()` | JSP: `<ft:currency value="${v.price}"/>` |
| 🔴 `getIsActive()` | DAO mapRow() set giá trị, SQL WHERE lọc |

---

## 4. InventoryLog.java — Entity nhật ký kho

**Package:** `com.fruitmkt.model.entity`
**Vai trò:** POJO ánh xạ bảng `inventory_logs`. Mỗi lần restock tạo 1 record mới. JSP hiển thị list các record này trong bảng audit trail.

### Attributes — TẤT CẢ đều 🔴 (mọi field đều hiển thị trên JSP):

| Attribute | Kiểu | DB Column | Vai trò trong Restock |
|-----------|------|-----------|----------------------|
| 🔴 `logId` | `int` | `log_id` | PK — JSP hiển thị `#${log.logId}` |
| 🔴 `variantId` | `int` | `variant_id` | FK — JSP hiển thị `${log.variantId}`, DAO INSERT set khi restock |
| 🔴 `changedBy` | `int` | `changed_by` | FK tới `users.user_id` — DAO INSERT set = userId của shop owner đang login |
| 🔴 `changeType` | `String` | `change_type` | DAO hardcode `'MANUAL_ADJUST'` khi restock. JSP dùng để tô màu (xanh/vàng/đỏ) |
| 🔴 `quantityDelta` | `int` | `quantity_delta` | Số lượng thay đổi (+50, -3). DAO INSERT set = quantity. JSP hiển thị `${log.quantityDelta} quả` |
| 🔴 `quantityAfter` | `int` | `quantity_after` | Tồn kho SAU khi thay đổi. DAO lấy bằng subquery `(SELECT stock_quantity FROM product_variants WHERE variant_id = ?)` |
| 🔴 `note` | `String` | `note` | Ghi chú từ form hoặc default. DAO INSERT set. JSP hiển thị `${log.note}` |
| 🔴 `changedAt` | `LocalDateTime` | `changed_at` | Thời gian. DAO set `GETDATE()`. JSP format thành `dd/MM/yyyy HH:mm` |

### Methods:

Tất cả là getter/setter thuần túy. Getter được JSP gọi qua EL expression `${log.xxx}`, setter được DAO `mapRow()` gọi.

---

## 5. BaseDAO.java — Lớp cha của mọi DAO

**Package:** `com.fruitmkt.dao.base`
**Vai trò:** Abstract class, cung cấp `getConnection()` cho tất cả DAO con kế thừa. Tập trung logic kết nối database.

### Static Block (chạy 1 lần khi class được load):

```java
static {
    Class.forName(AppConfig.DB_DRIVER_CLASS);  // Load SQL Server JDBC driver
}
```
- 🔴 **Tác động:** Nếu driver không tìm thấy → `ExceptionInInitializerError` → toàn bộ app crash. Chạy trước mọi DAO operation.

### Methods:

| Method | Signature | Vai trò trong Restock |
|--------|-----------|----------------------|
| 🔴 `getConnection()` | `protected Connection getConnection() throws SQLException` | **Được gọi bởi MỌI method trong ProductVariantDAO và InventoryDAO.** Tạo JDBC connection mới tới SQL Server bằng `DriverManager.getConnection(URL, USER, PASS)`. Caller phải đóng connection trong try-with-resources. |

**Chi tiết `getConnection()`:**
```java
protected Connection getConnection() throws SQLException {
    return DriverManager.getConnection(
        AppConfig.DB_JDBC_URL,    // "jdbc:sqlserver://localhost:1433;databaseName=OnlineFruitShopping;..."
        AppConfig.DB_USER,        // "sa"
        AppConfig.DB_PASSWORD     // "123"
    );
}
```
- Mỗi lần gọi tạo **connection MỚI** (không pooling)
- Connection phải được đóng bởi caller → tất cả DAO method dùng `try (Connection conn = getConnection()) { ... }`

---

## 6. ProductVariantDAO.java — DAO biến thể sản phẩm

**Package:** `com.fruitmkt.dao`
**Extends:** `BaseDAO`
**Vai trò:** Chứa TẤT CẢ SQL liên quan đến bảng `product_variants`. Restock dùng 3 method chính từ class này.

### Methods:

| # | Method | Signature | Restock? | Giải thích |
|---|--------|-----------|----------|------------|
| 1 | 🔴 `findById` | `ProductVariant findById(int id)` | **CÓ — Step 10d** | Servlet gọi khi POST để kiểm tra biến thể có tồn tại không trước khi restock |
| 2 | ⚪ `findByProduct` | `List<ProductVariant> findByProduct(int productId)` | KHÔNG | Dùng bởi product-detail, không liên quan restock |
| 3 | 🔴 `findByOwner` | `List<ProductVariant> findByOwner(int ownerId)` | **CÓ — Step 4 + Step 10d** | **(1)** doGet gọi để load danh sách variants hiển thị trên JSP. **(2)** doPost gọi để check ownership |
| 4 | ⚪ `findBySku` | `ProductVariant findBySku(String sku)` | KHÔNG | Dùng khi tạo/sửa product |
| 5 | ⚪ `save` | `int save(ProductVariant variant)` | KHÔNG | Dùng khi tạo product mới |
| 6 | ⚪ `update` | `void update(ProductVariant variant)` | KHÔNG | Dùng khi sửa product |
| 7 | ⚪ `updateStock` | `void updateStock(int variantId, int delta)` | KHÔNG | Method đơn giản không ghi log — dùng bởi Order flow (II.11) |
| 8 | ⚪ `deactivate` | `void deactivate(int variantId)` | KHÔNG | Soft delete 1 biến thể |
| 9 | ⚪ `deactivateAllByProduct` | `void deactivateAllByProduct(int productId)` | KHÔNG | Dùng khi sửa đè variants (II.7) |
| 10 | ⚪ `findLowStock` | `List<ProductVariant> findLowStock(int ownerId)` | KHÔNG | Dùng bởi II.12 Low Stock Alert |
| 11 | 🔴 `restockVariant(4 params)` | `void restockVariant(int variantId, int quantity, int userId, String note)` | **CÓ — Step 12** | **CORE METHOD** — thực hiện transaction UPDATE stock + INSERT log |
| 12 | ⚪ `restockVariant(3 params)` | `void restockVariant(int variantId, int quantity, int userId)` | KHÔNG trực tiếp | Overload backward-compatible, gọi lại method 4 params với note mặc định |
| 13 | 🔴 `mapRow` | `private ProductVariant mapRow(ResultSet rs)` | **CÓ** | Chuyển đổi 1 dòng ResultSet → ProductVariant object. Được gọi bởi findById, findByOwner |

### Chi tiết 3 method chính:

#### 🔴 `findById(int id)` — Kiểm tra biến thể tồn tại

```sql
SELECT * FROM product_variants WHERE variant_id = ? AND is_active = 1
```
- **Input:** `variantId` từ form POST
- **Output:** `ProductVariant` object hoặc `null`
- **Trong Restock:** Servlet kiểm tra `target == null` → reject nếu biến thể không tồn tại
- **Lưu ý:** Chỉ tìm biến thể `is_active = 1` → biến thể đã bị deactivate sẽ không tìm thấy

#### 🔴 `findByOwner(int ownerId)` — Lấy danh sách biến thể của shop owner

```sql
SELECT pv.*
FROM product_variants pv
JOIN products p ON pv.product_id = p.product_id
WHERE p.owner_id = ?          -- ← chỉ lấy product thuộc owner này
  AND pv.is_active = 1        -- ← chỉ lấy biến thể đang hoạt động
ORDER BY p.name ASC, pv.variant_label ASC
```
- **Input:** `user.getUserId()` từ session
- **Output:** `List<ProductVariant>` — có thể rỗng nếu shop chưa có sản phẩm
- **Trong Restock dùng 2 chỗ:**
  1. **doGet (Step 4):** Load danh sách để hiển thị trên JSP (dropdown + bảng tồn kho)
  2. **doPost (Step 10d):** Load danh sách để verify ownership — loop qua xem variantId từ form có nằm trong list không

#### 🔴 `restockVariant(int variantId, int quantity, int userId, String note)` — CORE

```java
conn.setAutoCommit(false);  // BẮT ĐẦU TRANSACTION

// SQL 1: Tăng stock
UPDATE product_variants
SET stock_quantity = stock_quantity + ?,   -- ? = quantity (ví dụ 50)
    updated_at = GETDATE()
WHERE variant_id = ?;                     -- ? = variantId

// Kiểm tra rows affected
if (rows == 0) → rollback + throw  // Biến thể không tồn tại

// SQL 2: Ghi audit log
INSERT INTO inventory_logs
    (variant_id, changed_by, change_type, quantity_delta, quantity_after, note, changed_at)
VALUES (?, ?, 'MANUAL_ADJUST', ?,
    (SELECT stock_quantity FROM product_variants WHERE variant_id = ?),  -- subquery lấy stock SAU update
    ?, GETDATE());

conn.commit();  // CẢ 2 SQL thành công → COMMIT
// Nếu bất kỳ SQL nào fail → catch → rollback → throw
```

**Tại sao dùng transaction?**
- Nếu INSERT log fail mà UPDATE stock đã thành công → stock tăng nhưng không có log → mất audit trail
- Transaction đảm bảo: hoặc CẢ HAI thành công, hoặc CẢ HAI bị hủy

**Tại sao `quantity_after` dùng subquery?**
- Subquery chạy SAU UPDATE (trong cùng transaction) → lấy được giá trị stock MỚI
- Ví dụ: stock cũ = 20, restock +50 → subquery trả về 70 → `quantity_after = 70` ✅

#### 🔴 `mapRow(ResultSet rs)` — Ánh xạ dữ liệu

```java
private ProductVariant mapRow(ResultSet rs) throws SQLException {
    ProductVariant pv = new ProductVariant();
    pv.setVariantId(rs.getInt("variant_id"));       // Lấy cột variant_id → set vào object
    pv.setProductId(rs.getInt("product_id"));       // Lấy cột product_id
    pv.setSku(rs.getString("sku"));                 // Lấy cột sku
    pv.setVariantLabel(rs.getString("variant_label"));
    pv.setPrice(rs.getBigDecimal("price"));
    pv.setStockQuantity(rs.getInt("stock_quantity"));
    int weight = rs.getInt("weight_grams");
    pv.setWeightGrams(rs.wasNull() ? null : weight);  // Handle NULL column
    pv.setDiscountPrice(rs.getBigDecimal("discount_price"));
    pv.setPackagingOption(rs.getString("packaging_option"));
    pv.setIsActive(rs.getBoolean("is_active"));
    // Timestamp → LocalDateTime conversion
    Timestamp createdAtVal = rs.getTimestamp("created_at");
    if (createdAtVal != null) pv.setCreatedAt(createdAtVal.toLocalDateTime());
    Timestamp updatedAtVal = rs.getTimestamp("updated_at");
    if (updatedAtVal != null) pv.setUpdatedAt(updatedAtVal.toLocalDateTime());
    return pv;
}
```

---

## 7. InventoryDAO.java — DAO nhật ký kho

**Package:** `com.fruitmkt.dao`
**Extends:** `BaseDAO`
**Vai trò:** Chứa SQL liên quan đến bảng `inventory_logs`. Restock chỉ dùng 1 method từ class này (cho GET).

### Methods:

| # | Method | Signature | Restock? | Giải thích |
|---|--------|-----------|----------|------------|
| 1 | 🔴 `findLogsByOwner` | `List<InventoryLog> findLogsByOwner(int ownerId)` | **CÓ — Step 5** | doGet gọi để load audit trail hiển thị trên JSP |
| 2 | ⚪ `findByVariant` | `List<InventoryLog> findByVariant(int variantId)` | KHÔNG | Có thể dùng cho detail view |
| 3 | ⚪ `save` | `int save(InventoryLog log)` | KHÔNG | Restock ghi log trực tiếp trong `restockVariant()` transaction, không qua method này |
| 4 | 🔴 `mapRow` | `private InventoryLog mapRow(ResultSet rs)` | **CÓ** | Chuyển đổi ResultSet → InventoryLog |

### Chi tiết method chính:

#### 🔴 `findLogsByOwner(int ownerId)` — Load lịch sử kho

```sql
SELECT il.*
FROM inventory_logs il
JOIN product_variants pv ON il.variant_id = pv.variant_id
JOIN products p ON pv.product_id = p.product_id
WHERE p.owner_id = ?             -- chỉ lấy log của shop owner này
ORDER BY il.changed_at DESC      -- mới nhất lên đầu
```
- **Input:** `user.getUserId()`
- **Output:** `List<InventoryLog>` — tất cả log (MANUAL_ADJUST, ORDER_RESERVE, v.v.)
- **JOIN 3 bảng:** `inventory_logs` → `product_variants` → `products` để lọc theo `owner_id`

#### 🔴 `mapRow(ResultSet rs)` — Ánh xạ dữ liệu

```java
private InventoryLog mapRow(ResultSet rs) throws SQLException {
    InventoryLog log = new InventoryLog();
    log.setLogId(rs.getInt("log_id"));
    log.setVariantId(rs.getInt("variant_id"));
    log.setChangedBy(rs.getInt("changed_by"));
    log.setChangeType(rs.getString("change_type"));
    log.setQuantityDelta(rs.getInt("quantity_delta"));
    log.setQuantityAfter(rs.getInt("quantity_after"));
    log.setNote(rs.getString("note"));
    Timestamp changedAtVal = rs.getTimestamp("changed_at");
    if (changedAtVal != null) log.setChangedAt(changedAtVal.toLocalDateTime());
    return log;
}
```

> **Lưu ý:** `InventoryDAO.save()` KHÔNG được dùng bởi Restock. Restock ghi log trực tiếp bên trong `ProductVariantDAO.restockVariant()` vì cần chung 1 transaction với UPDATE stock.

---

## 8. ProductService.java — Service xử lý nghiệp vụ

**Package:** `com.fruitmkt.service`
**Vai trò:** Tầng trung gian giữa Servlet và DAO. Validate business rule, delegate xuống DAO. KHÔNG viết SQL, KHÔNG tương tác HTTP.

### Attributes:

| Attribute | Kiểu | Vai trò trong Restock |
|-----------|------|----------------------|
| ⚪ `productDAO` | `ProductDAO` | Không dùng bởi Restock |
| ⚪ `categoryDAO` | `CategoryDAO` | Không dùng |
| 🔴 `productVariantDAO` | `ProductVariantDAO` | **restock() gọi `productVariantDAO.restockVariant()`** |

### Methods:

| # | Method | Restock? | Giải thích |
|---|--------|----------|------------|
| 1 | 🔴 `restock(4 params)` | **CÓ — Step 11** | Validate `quantity > 0`, rồi delegate xuống DAO |
| 2 | ⚪ `restock(3 params)` | Overload | Gọi lại method 4 params với note mặc định |
| 3 | ⚪ `getProductList` | KHÔNG | Dùng bởi product listing |
| 4 | ⚪ `getProductListAdvanced` | KHÔNG | Dùng bởi II.16-II.20 |
| 5 | ⚪ `getRecommendations` | KHÔNG | II.21 |
| 6 | ⚪ `getBestSellers` | KHÔNG | II.22 |
| 7 | ⚪ `getRecentlyViewed` | KHÔNG | II.23 |
| 8 | ⚪ `getLowStockAlerts` | KHÔNG | II.12 |
| 9 | ⚪ `getProductDetail` | KHÔNG | Product detail page |
| 10 | ⚪ `createProduct` | KHÔNG | Tạo sản phẩm |
| 11 | ⚪ `updateProduct` | KHÔNG | Sửa sản phẩm |
| 12 | ⚪ `toggleStatus` | KHÔNG | Bật/tắt sản phẩm |

### Chi tiết method chính:

#### 🔴 `restock(int variantId, int quantity, int userId, String note)`

```java
public void restock(int variantId, int quantity, int userId, String note) throws SQLException {
    if (quantity <= 0) {
        throw new IllegalArgumentException("Số lượng nhập kho phải lớn hơn 0.");
    }
    productVariantDAO.restockVariant(variantId, quantity, userId, note);
}
```

- **Validation:** Double-check `quantity > 0` (servlet đã check, service check lại cho defense-in-depth)
- **Delegation:** Gọi thẳng `restockVariant()` — không thêm business logic nào khác
- **Exception flow:** `IllegalArgumentException` nếu quantity invalid, `SQLException` nếu DB error → cả hai bubble up cho Servlet catch

---

## 9. SessionUtil.java — Tiện ích session

**Package:** `com.fruitmkt.util`
**Vai trò:** Tiện ích đọc/ghi HTTP Session. Mọi Servlet dùng class này thay vì trực tiếp `session.getAttribute()`.

### Methods:

| # | Method | Signature | Restock? | Giải thích |
|---|--------|-----------|----------|------------|
| 1 | 🔴 `getCurrentUser` | `static User getCurrentUser(HttpSession)` | **CÓ** | Lấy User object từ session bằng key `"currentUser"`. Trả null nếu chưa login |
| 2 | 🔴 `isLoggedIn` | `static boolean isLoggedIn(HttpSession)` | **CÓ** | Gọi `getCurrentUser()`, return `!= null`. Servlet dùng trong auth check |
| 3 | 🔴 `hasRole` | `static boolean hasRole(HttpSession, String role)` | **CÓ** | Lấy User, so sánh `role.equals(user.getRole())`. Servlet check `hasRole(session, "SHOP_OWNER")` |
| 4 | ⚪ `setCurrentUser` | `static void setCurrentUser(HttpSession, User)` | KHÔNG | Chỉ gọi khi login |
| 5 | 🔴 `setFlashMessage` | `static void setFlashMessage(HttpSession, String msg, String type)` | **CÓ** | Set 2 session attributes: `flashMsg` + `flashType` |
| 6 | 🔴 `flashSuccess` | `static void flashSuccess(HttpSession, String msg)` | **CÓ** | Shortcut: `setFlashMessage(session, msg, "success")` — Servlet gọi khi restock thành công |
| 7 | 🔴 `flashError` | `static void flashError(HttpSession, String msg)` | **CÓ** | Shortcut: `setFlashMessage(session, msg, "error")` — Servlet gọi khi có lỗi |
| 8 | ⚪ `clearSession` | `static void clearSession(HttpSession)` | KHÔNG | Chỉ gọi khi logout |

### Cách flash message hoạt động:

```
POST (Servlet) → flashSuccess(session, "Thành công!")
    ↓ session lưu: {flashMsg: "Thành công!", flashType: "success"}
302 Redirect → GET /shop/inventory
    ↓ servlet forward → inventory.jsp
JSP đọc: ${sessionScope.flashMsg} → hiển thị banner xanh
JSP xóa: session.removeAttribute("flashMsg") → lần sau không hiện nữa
```

---

## 10. CsrfFilter.java — Bộ lọc bảo mật CSRF

**Package:** `com.fruitmkt.filter`
**Implements:** `jakarta.servlet.Filter`
**Annotation:** `@WebFilter("/*")` — chạy trước MỌI request
**Vai trò:** Bảo vệ tất cả form POST khỏi tấn công Cross-Site Request Forgery.

### Method duy nhất:

#### 🔴 `doFilter(ServletRequest, ServletResponse, FilterChain)`

**Logic flow:**

```
1. Đảm bảo session có CSRF token:
   if (session.getAttribute("_csrfToken") == null)
       session.setAttribute("_csrfToken", UUID.randomUUID().toString());

2. Nếu là POST (và không phải /api/* hoặc /auth/*):
   - Lấy sessionToken = session.getAttribute("_csrfToken")
   - Lấy requestToken = req.getParameter("_csrf")      ← từ hidden field trong form
   - So sánh: nếu KHÔNG khớp → HTTP 403 → DỪNG
   
3. Nếu token OK → chain.doFilter() → tiếp tục tới Servlet
```

**Tác động lên Restock:**
- Restock form PHẢI có: `<input type="hidden" name="_csrf" value="${sessionScope._csrfToken}" />`
- Nếu thiếu hoặc sai token → POST bị chặn, không bao giờ đến InventoryServlet

---

## 11. InventoryServlet.java — Controller chính

**Package:** `com.fruitmkt.servlet.shop`
**Extends:** `HttpServlet`
**Annotation:** `@WebServlet("/shop/inventory")`
**Vai trò:** Controller (C trong MVC) — nhận HTTP request, gọi Service/DAO, trả response. KHÔNG chứa SQL hay business logic phức tạp.

### Attributes:

| Attribute | Kiểu | Khởi tạo | Vai trò |
|-----------|------|----------|---------|
| 🔴 `productService` | `ProductService` | `new ProductService()` | doPost gọi `productService.restock()` |
| 🔴 `productVariantDAO` | `ProductVariantDAO` | `new ProductVariantDAO()` | doGet gọi `findByOwner()`, doPost gọi `findById()` + `findByOwner()` |
| 🔴 `inventoryDAO` | `InventoryDAO` | `new InventoryDAO()` | doGet gọi `findLogsByOwner()` |

> **Lưu ý:** Cả 3 được khởi tạo 1 lần khi Servlet được tạo (Servlet là singleton trong Tomcat). Nghĩa là cùng 1 instance được dùng lại cho mọi request.

### Methods:

#### 🔴 `doGet(HttpServletRequest, HttpServletResponse)` — Load trang

```
Step 1: Auth check → redirect /auth/login nếu chưa login hoặc không phải SHOP_OWNER
Step 2: Lấy User từ session
Step 3: Gọi 2 DAO query:
        - productVariantDAO.findByOwner(userId)  → List<ProductVariant>
        - inventoryDAO.findLogsByOwner(userId)    → List<InventoryLog>
Step 4: Set vào request scope:
        - req.setAttribute("variants", variants)
        - req.setAttribute("logs", logs)
Step 5: Forward → /WEB-INF/jsp/shop/inventory.jsp
```

#### 🔴 `doPost(HttpServletRequest, HttpServletResponse)` — Xử lý restock

```
Step 1: Auth check (giống doGet)
Step 2: Parse parameters từ form:
        - variantId = Integer.parseInt(req.getParameter("variantId"))
        - quantity  = Integer.parseInt(req.getParameter("quantity"))
Step 3: Validate quantity > 0
Step 4: Security — check ownership:
        - findById(variantId) → null check
        - findByOwner(userId) → loop kiểm tra variantId có trong list
Step 5: Parse note (default nếu trống, trim + truncate 300 ký tự)
Step 6: Gọi productService.restock(variantId, quantity, userId, note)
Step 7: flashSuccess("Cập nhật số lượng tồn kho thành công!")
Step 8: 302 Redirect → /shop/inventory (PRG pattern)

Error handling:
- NumberFormatException → flashError("Số lượng không hợp lệ.")
- IllegalArgumentException → flashError(e.getMessage())
- SQLException → flashError("Cập nhật tồn kho thất bại: Lỗi cơ sở dữ liệu.")
- Redirect luôn xảy ra (L129) bất kể thành công hay lỗi
```

---

## Tổng kết: Call Chain hoàn chỉnh

```
Browser POST
    │
    ▼
CsrfFilter.doFilter()
    │ validates _csrf token
    ▼
InventoryServlet.doPost()
    │ SessionUtil.isLoggedIn()      → User.getRole() checked
    │ SessionUtil.hasRole("SHOP_OWNER")
    │ SessionUtil.getCurrentUser()  → User.getUserId()
    │ Integer.parseInt(req.getParameter("variantId"))
    │ Integer.parseInt(req.getParameter("quantity"))
    │ quantity <= 0 check
    │
    │ ProductVariantDAO.findById(variantId)      → SQL SELECT
    │ ProductVariantDAO.findByOwner(userId)       → SQL SELECT + loop
    │ ownership check
    │
    │ req.getParameter("note") → trim/truncate
    │
    ▼
ProductService.restock(variantId, quantity, userId, note)
    │ quantity <= 0 check (defense-in-depth)
    │
    ▼
ProductVariantDAO.restockVariant(variantId, quantity, userId, note)
    │ conn.setAutoCommit(false)       ← START TRANSACTION
    │
    │ SQL 1: UPDATE product_variants SET stock_quantity += quantity
    │ rows check (rollback if 0)
    │
    │ SQL 2: INSERT INTO inventory_logs (MANUAL_ADJUST, +quantity, ...)
    │
    │ conn.commit()                   ← COMMIT
    │ (or conn.rollback() on error)
    │
    ▼
InventoryServlet.doPost() (continued)
    │ SessionUtil.flashSuccess(session, "Cập nhật...!")
    │
    ▼
resp.sendRedirect("/shop/inventory")   ← 302 redirect (PRG)
    │
    ▼
Browser GET (automatic)
    │
    ▼
InventoryServlet.doGet()
    │ ProductVariantDAO.findByOwner(userId)  → updated stock
    │ InventoryDAO.findLogsByOwner(userId)   → includes new log entry
    │ req.setAttribute("variants", ...)
    │ req.setAttribute("logs", ...)
    │
    ▼
inventory.jsp renders
    │ ${sessionScope.flashMsg}  → green success banner
    │ ${variants}               → dropdown + stock table (updated)
    │ ${logs}                   → audit trail (new entry at top)
```
