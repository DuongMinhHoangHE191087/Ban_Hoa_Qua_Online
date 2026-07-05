# Báo cáo tổng hợp: Feature Product Customization (Tùy chọn đóng gói & Nhãn sản phẩm)

## 1. Flow của chức năng
Chức năng "Product Customization" cho phép chủ cửa hàng cấu hình các nhãn đặc biệt cho sản phẩm (Hữu cơ, Nhập khẩu), thiết lập các tháng mùa vụ và cung cấp các tùy chọn đóng gói đi kèm (ví dụ: Hộp quà gỗ, Túi giấy sinh học) với phụ phí cộng thêm. Khách hàng sẽ xem được các nhãn này và chọn đóng gói tùy ý khi xem chi tiết sản phẩm.

**Các bước (Data Flow):**
1. **Chủ shop thiết lập (Create/Edit Product):** Khi tạo mới hoặc cập nhật sản phẩm, chủ cửa hàng đánh dấu cờ `isOrganic`, `isImported`, chỉ định `seasonStartMonth`, `seasonEndMonth`, và cấu hình danh sách các gói đóng gói cùng `priceAdd` (phụ thu). Dữ liệu này được xử lý bởi `ProductCreateServlet` và `ProductEditServlet`, lưu xuống database qua DAO.
2. **Lưu trữ database:** Các nhãn cờ và mùa vụ được lưu vào bảng `products`. Các tùy chọn đóng gói được lưu vào bảng `product_packaging_options` thông qua entity `ProductPackagingOption` và DAO tương ứng.
3. **Hiển thị sản phẩm (Product Detail View):** Khi khách hàng truy cập `/products/detail`, `ProductDetailServlet` truy xuất dữ liệu sản phẩm, kiểm tra tính khả dụng theo mùa vụ, lấy danh sách `packagingOptions` qua `ProductPackagingOptionDAO` và gửi sang view (JSP).
4. **Trải nghiệm khách hàng (UI):** Trang `product-detail.jsp` hiển thị các tag nổi bật (Hữu cơ, Nhập khẩu), cảnh báo nếu ngoài mùa vụ, và cung cấp một dropdown cho phép khách chọn tùy chọn đóng gói. Khi chọn, giá phụ thu được tính ngay vào tổng tiền (Subtotal).

---

## 2. Các Class liên quan

1. **`model.entity.catalog.ProductPackagingOption` (Entity):** Ánh xạ bảng `product_packaging_options`.
2. **`dao.catalog.ProductPackagingOptionDAO` (DAO):** Chứa các hàm CRUD để làm việc với tùy chọn đóng gói.
3. **`servlet.shop.product.ProductCreateServlet` (Servlet):** Nhận tham số từ form thêm mới sản phẩm và gọi DAO để lưu thông tin tùy chọn đóng gói, nhãn sản phẩm.
4. **`servlet.shop.product.ProductEditServlet` (Servlet):** Xử lý cập nhật thông tin sản phẩm, bao gồm việc sửa đổi, thêm mới hoặc xóa các tùy chọn đóng gói.
5. **`servlet.guest.product.ProductDetailServlet` (Servlet):** Truy vấn và chuẩn bị danh sách tùy chọn đóng gói của sản phẩm, cờ mùa vụ để hiển thị ra trang chi tiết.
6. **`web/WEB-INF/jsp/guest/product-detail.jsp` (View - JSP):** Render các tag "Hữu cơ", "Nhập khẩu", cảnh báo "Trái mùa", và thẻ select cho tùy chọn đóng gói phụ thu.

---

## 3. Từng dòng code liên quan (Code Snippets)

### A. Entity: `ProductPackagingOption.java`
```java
package model.entity.catalog;

import java.math.BigDecimal;

public class ProductPackagingOption {
    private int packagingId;
    private int productId;
    private String label;
    private BigDecimal priceAdd;
    private boolean isActive;

    public ProductPackagingOption() {}

    public int getPackagingId() { return packagingId; }
    public void setPackagingId(int packagingId) { this.packagingId = packagingId; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public BigDecimal getPriceAdd() { return priceAdd; }
    public void setPriceAdd(BigDecimal priceAdd) { this.priceAdd = priceAdd; }
    public boolean getIsActive() { return isActive; }
    public void setIsActive(boolean isActive) { this.isActive = isActive; }
}
```

### B. DAO: `ProductPackagingOptionDAO.java`
```java
// Lấy danh sách bao bì đóng gói theo productId
public List<ProductPackagingOption> findByProduct(int productId) throws SQLException {
    List<ProductPackagingOption> list = new ArrayList<>();
    String sql = "SELECT * FROM product_packaging_options WHERE product_id = ? AND is_active = 1";
    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, productId);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
    }
    return list;
}

// Lưu mới tùy chọn đóng gói
public int save(ProductPackagingOption option) throws SQLException {
    String sql = "INSERT INTO product_packaging_options (product_id, label, price_add, is_active) VALUES (?, ?, ?, ?)";
    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        ps.setInt(1, option.getProductId());
        ps.setString(2, option.getLabel());
        ps.setBigDecimal(3, option.getPriceAdd());
        ps.setBoolean(4, option.getIsActive());
        ps.executeUpdate();
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) return rs.getInt(1);
        }
    }
    throw new SQLException("Failed to save packaging option.");
}

// Các hàm khác: update, deleteByProductExcept, findById...
```

### C. Servlet xử lý tạo mới: `ProductCreateServlet.java`
```java
// 1. Nhận dữ liệu checkbox và dropdown từ UI
boolean isOrganic = req.getParameter("isOrganic") != null;
boolean isImported = req.getParameter("isImported") != null;
String seasonStartMonthStr = req.getParameter("seasonStartMonth");
String seasonEndMonthStr = req.getParameter("seasonEndMonth");

// Mảng các tùy chọn đóng gói gửi lên
String[] packagingLabels = req.getParameterValues("packagingLabel");
String[] packagingPriceAdds = req.getParameterValues("packagingPriceAdd");

// 2. Gán vào Product entity và lưu
p.setIsOrganic(isOrganic);
p.setIsImported(isImported);
if (seasonStartMonthStr != null && !seasonStartMonthStr.trim().isEmpty()) {
    p.setSeasonStartMonth(Integer.parseInt(seasonStartMonthStr.trim()));
}
if (seasonEndMonthStr != null && !seasonEndMonthStr.trim().isEmpty()) {
    p.setSeasonEndMonth(Integer.parseInt(seasonEndMonthStr.trim()));
}
int productId = productDAO.save(p);

// 3. Xử lý lưu danh sách bao bì tùy chọn liên kết với product id
if (packagingLabels != null) {
    dao.catalog.ProductPackagingOptionDAO ppoDAO = new dao.catalog.ProductPackagingOptionDAO();
    for (int i = 0; i < packagingLabels.length; i++) {
        String pLabel = packagingLabels[i];
        if (pLabel != null && !pLabel.trim().isEmpty()) {
            BigDecimal priceAdd = BigDecimal.ZERO;
            if (packagingPriceAdds != null && packagingPriceAdds.length > i && packagingPriceAdds[i] != null) {
                try {
                    priceAdd = new BigDecimal(packagingPriceAdds[i].trim());
                } catch (NumberFormatException e) { ... }
            }
            model.entity.catalog.ProductPackagingOption option = new model.entity.catalog.ProductPackagingOption();
            option.setProductId(productId);
            option.setLabel(pLabel.trim());
            option.setPriceAdd(priceAdd);
            option.setIsActive(true);
            ppoDAO.save(option);
        }
    }
}
```

### D. Servlet xử lý chi tiết sản phẩm: `ProductDetailServlet.java`
```java
// 1. Kiểm tra tính trái mùa của sản phẩm
int currentMonth = java.time.LocalDate.now().getMonthValue();
boolean isOutOfSeason = false;
if (product.getSeasonStartMonth() != null && product.getSeasonEndMonth() != null) {
    int start = product.getSeasonStartMonth();
    int end = product.getSeasonEndMonth();
    if (start <= end) {
        isOutOfSeason = (currentMonth < start || currentMonth > end);
    } else {
        isOutOfSeason = (currentMonth < start && currentMonth > end);
    }
}
req.setAttribute("isOutOfSeason", isOutOfSeason);

// 2. Đọc danh sách bao bì đóng gói chọn thêm
dao.catalog.ProductPackagingOptionDAO packagingOptionDAO = new dao.catalog.ProductPackagingOptionDAO();
List<model.entity.catalog.ProductPackagingOption> packagingOptions = packagingOptionDAO.findByProduct(productId);
req.setAttribute("packagingOptions", packagingOptions);

// Đẩy dữ liệu ra view
req.setAttribute("product", product);
// ... chuyển hướng sang product-detail.jsp
```

### E. View: `product-detail.jsp`
```jsp
<!-- Hiển thị tag Hữu cơ / Nhập khẩu -->
<c:if test="${product.isOrganic}">
    <span class="badge-stock" style="background: linear-gradient(120deg, #dcfce7, #86efac); color: #14532d; border: 1px solid rgba(34,197,94,0.4);">
        <i class="fa-solid fa-leaf mr-1"></i> Hữu Cơ
    </span>
</c:if>
<c:if test="${product.isImported}">
    <span class="badge-stock" style="background: linear-gradient(120deg, #dbeafe, #93c5fd); color: #1e3a8a; border: 1px solid rgba(59,130,246,0.4);">
        <i class="fa-solid fa-globe mr-1"></i> Nhập Khẩu
    </span>
</c:if>

<!-- Cảnh báo trái mùa (nếu isOutOfSeason) -->
<c:if test="${isOutOfSeason && !isExpiredProduct}">
    <div class="out-of-season-banner" style="...">
        <!-- icon cloud-sun-rain -->
        <div>
            <h4>Sản phẩm đã hết mùa vụ gieo trồng</h4>
            <p>Mùa vụ của sản phẩm này từ tháng ${product.seasonStartMonth} đến tháng ${product.seasonEndMonth}. Vui lòng quay lại khi đến mùa vụ hoặc liên hệ shop.</p>
        </div>
    </div>
</c:if>

<!-- Lựa chọn quy cách đóng gói (Packaging Options) -->
<c:if test="${not empty packagingOptions}">
    <div class="variant-section my-5">
        <div class="section-sub-title">Chọn quy cách đóng gói (Tùy chọn):</div>
        <div class="mt-2">
            <select id="packaging-selector" onchange="calculateSubtotal()" class="w-full md:max-w-xs px-4 py-2.5 rounded-full border-2 border-gray-200 focus:border-primary focus:ring-0 text-sm font-semibold text-gray-700 bg-white transition-all shadow-sm">
                <option value="" data-price-add="0">Mặc định (Không thêm phụ phí)</option>
                <c:forEach var="po" items="${packagingOptions}">
                    <option value="${po.packagingId}" data-price-add="${po.priceAdd}">
                        <c:out value="${po.label}"/> (+<ft:currency value="${po.priceAdd}"/>)
                    </option>
                </c:forEach>
            </select>
        </div>
    </div>
</c:if>
```

---

## 4. Giao diện (JSP) tương tác (Mapping UI to Servlet)

Chức năng tùy chỉnh và đóng gói sản phẩm có sự tương tác chặt chẽ giữa giao diện (frontend) và các Servlet (backend):

1. **Trang Quản lý / Thêm / Sửa Sản Phẩm (`web/WEB-INF/jsp/shop/product-list.jsp`)**:
   - Chứa form HTML (thường được đặt trong modal/popup) cho phép nhập thông tin nhãn (`isOrganic`, `isImported`), chọn mùa vụ (`seasonStartMonth`, `seasonEndMonth`). Ngoài ra có một vùng giao diện cho phép **thêm động** (add row) các quy cách đóng gói bằng Javascript. Các ô input sinh ra sẽ có `name="packagingLabel"` và `name="packagingPriceAdd"`.
   - **Tương tác API**: Khi chủ shop bấm lưu, dữ liệu form được gom lại bằng đối tượng `FormData` trong JS và gửi đi (submit) thông qua Fetch API/AJAX.
   - **Đích đến (Servlet)**: Dữ liệu được gửi theo phương thức POST tới URL `/shop/product-create` (do `ProductCreateServlet` bắt) hoặc `/shop/product-edit` (do `ProductEditServlet` bắt). Các servlet này dùng `req.getParameterValues()` để đọc mảng giá trị đóng gói và ghi xuống Database.

2. **Trang Chi Tiết Sản Phẩm (`web/WEB-INF/jsp/guest/product-detail.jsp`)**:
   - **Chuẩn bị dữ liệu**: Khi có truy cập vào `/products/detail?id=...`, `ProductDetailServlet` sẽ truy vấn Database lấy chi tiết danh sách gói phụ thu. Sau khi `setAttribute`, request được forward về trang JSP này.
   - **Tương tác UI**: Thẻ `<select id="packaging-selector" onchange="calculateSubtotal()">` dùng vòng lặp JSTL `<c:forEach>` để in ra từng loại bao bì. Javascript ở file này sẽ lắng nghe sự kiện đổi thẻ Select, tự động đọc giá trị `data-price-add` (số tiền phụ thu) và cộng ngay vào giá Tạm tính (Subtotal) cho người dùng thấy.
   - **Thêm vào giỏ hàng**: Giá trị `packagingId` đã chọn cuối cùng sẽ được kẹp vào payload gửi tới `CartServlet` (hoặc API xử lý Add to Cart) để ghi nhận đơn hàng chứa quy cách đóng gói kèm theo.
