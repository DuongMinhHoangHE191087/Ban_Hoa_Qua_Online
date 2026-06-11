# Phân tích Thiết kế Hệ thống Wishlist (Danh sách yêu thích)

Báo cáo phân tích chi tiết về tính năng Wishlist cho dự án **Ban_Hoa_Qua_Online**.

---

## 1. Wishlist là gì?
**Wishlist (Danh sách yêu thích)** là tính năng cho phép người dùng (Customer) lưu lại các sản phẩm họ quan tâm nhưng chưa muốn mua ngay vào một danh sách riêng. 

---

## 2. Có nên xây dựng Wishlist trong dự án Bán Hoa Quả Online không?
**Rất nên xây dựng**. Mặc dù hoa quả là sản phẩm có vòng đời ngắn (tiêu dùng nhanh), tính năng này vẫn mang lại lợi ích lớn:
* **Tăng tỷ lệ chuyển đổi (Conversion Rate)**: Khách hàng dễ dàng lưu lại các loại trái cây yêu thích theo mùa để mua lại hoặc mua sau khi có lương.
* **Cải thiện trải nghiệm khách hàng (UX)**: Hỗ trợ tính năng "Move to Cart" giúp khách hàng thanh toán nhanh chóng mà không mất công tìm kiếm lại.
* **Hỗ trợ Marketing & Inventory**: Shop owner có thể theo dõi sản phẩm nào được yêu thích nhiều nhất để chuẩn bị nguồn hàng (phù hợp với quy tắc quản lý kho proximity/FEFO).

---

## 3. Phân tích các phần cần sửa đổi và thêm mới

### A. Cơ sở dữ liệu (Database Schema)
Cần tạo thêm bảng `wishlists` để liên kết người dùng và sản phẩm yêu thích.

```sql
-- Thêm bảng wishlists vào Schema.sql
CREATE TABLE wishlists (
    wishlist_id INT IDENTITY(1,1) PRIMARY KEY,
    customer_id INT NOT NULL FOREIGN KEY REFERENCES users(user_id) ON DELETE CASCADE,
    product_id INT NOT NULL FOREIGN KEY REFERENCES products(product_id) ON DELETE CASCADE,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT UQ_wishlist_customer_product UNIQUE (customer_id, product_id)
);
```

### B. Lớp Dữ liệu & Nghiệp vụ (Backend Java)

#### 1. Lớp Model
* **Thêm mới**: `WishlistItem.java` chứa thông tin về sản phẩm yêu thích (gồm `wishlistId`, `customerId`, `productId`, đối tượng `Product`, và thời gian tạo).

#### 2. Lớp DAO (Data Access Object)
* **Thêm mới**: `WishlistDAO.java` chứa các phương thức tương tác với DB:
  * `addWishlistItem(int customerId, int productId)`: Thêm sản phẩm vào danh sách yêu thích.
  * `removeWishlistItem(int customerId, int productId)`: Xóa sản phẩm khỏi danh sách yêu thích.
  * `getWishlistByCustomerId(int customerId)`: Lấy danh sách sản phẩm yêu thích của khách hàng.
  * `isInWishlist(int customerId, int productId)`: Kiểm tra sản phẩm đã nằm trong wishlist chưa.

#### 3. Lớp Service
* **Thêm mới**: `WishlistService.java` điều phối nghiệp vụ:
  * Thêm/Xóa sản phẩm.
  * `moveWishlistToCart(int customerId, int productId)`: Lấy biến thể mặc định (default variant) của sản phẩm, thêm vào giỏ hàng (`cart_items`), sau đó xóa khỏi wishlist (`wishlists`) trong một giao dịch (transaction) để đảm bảo toàn vẹn dữ liệu.

#### 4. Lớp Controller / Servlets
* **Thêm mới**: `WishlistServlet.java` (URL mapping: `/customer/wishlist` hoặc `/api/wishlist`) xử lý các request AJAX POST/GET:
  * `action=add`: Gọi `WishlistService.add`.
  * `action=remove`: Gọi `WishlistService.remove`.
  * `action=moveToCart`: Gọi `WishlistService.moveWishlistToCart`.

### C. Giao diện (Frontend JSP/JS)

#### 1. [product-detail.jsp](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/web/WEB-INF/jsp/product-detail.jsp) & [product-list.jsp](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/web/WEB-INF/jsp/product-list.jsp)
* Thêm nút "Trái tim" (Heart Icon) trên thẻ sản phẩm và trang chi tiết sản phẩm.
* Viết sự kiện JavaScript (AJAX) để gọi API thêm/xóa wishlist mà không tải lại trang.

#### 2. Trang cá nhân [wishlist.jsp](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/web/WEB-INF/jsp/customer/wishlist.jsp) [NEW]
* Hiển thị danh sách các sản phẩm đã yêu thích.
* Hiển thị trạng thái tồn kho (nếu hết hàng, hiển thị "Hết hàng" và khóa nút chuyển sang giỏ hàng theo quy tắc `INV-03`).
* Cung cấp nút "Xóa khỏi danh sách" và "Thêm vào giỏ hàng" (Move to Cart).

---

## 4. Kế hoạch Kiểm thử & Xác thực (Verification Plan)
1. **Kiểm thử Luồng Thêm/Xóa**: Khách hàng nhấn tim -> icon chuyển đỏ -> F5 trang danh sách vẫn lưu trạng thái -> nhấn lại -> icon trở lại bình thường.
2. **Kiểm thử Luồng Move to Cart**: Chọn sản phẩm trong wishlist -> nhấn Move to Cart -> sản phẩm biến mất khỏi wishlist và xuất hiện trong giỏ hàng với biến thể mặc định.
3. **Kiểm thử Ràng buộc Hết hàng (`INV-03`)**: Sản phẩm hết hàng trong kho -> hiển thị tag "Hết hàng" trên giao diện wishlist -> Nút "Move to Cart" bị disabled.
