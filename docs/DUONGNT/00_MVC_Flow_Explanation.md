# Giải thích Luồng MVC (Model - View - Controller) Dễ Hiểu Nhất

Mô hình MVC trong dự án Java Web cổ điển của bạn được chia vai trò rất rạch ròi. Dưới đây là cách giải thích thực tế nhất: **Truy vết một thao tác của người dùng từ lúc click chuột cho đến khi màn hình web hiện ra.**

## 1. Phân vai trong hệ thống (Ai làm việc nấy)
* **C - Controller (Điều phối viên - `Servlet`):** Nằm ở thư mục `src/java/servlet/`. Nhiệm vụ của nó là đứng gác cửa. Khi có HTTP Request gửi tới (ví dụ URL `/home` hoặc `/login`), Servlet nhận request, lấy dữ liệu người dùng nhập, sai bảo thằng Model đi làm việc, rồi mang kết quả ném cho thằng View.
* **M - Model (Cơ bắp & Xử lý - `Service` + `DAO` + `Entity`):** Nằm ở `src/java/service/` và `src/java/dao/`. Đây là não bộ.
    * **Service:** Chứa business logic (luật lệ). Ví dụ: *Chỉ lấy sản phẩm đang ACTIVE, không lấy hàng hết hạn.*
    * **DAO:** Chứa câu lệnh SQL (SELECT, INSERT) để móc nối trực tiếp với SQL Server.
    * **Entity:** Object Java (VD: class `Product`, `User`) đại diện cho cái bảng dưới Database.
* **V - View (Giao diện hiển thị - `JSP`):** Nằm ở `web/WEB-INF/jsp/`. Thằng này chả biết code Java xử lý phức tạp là gì. Nhiệm vụ của nó chỉ là nhận cái cục dữ liệu mà Controller ném sang, rồi dùng HTML/CSS/JS "đắp mặt nạ" lên và hiển thị cho người dùng xem.

---

## 2. Ví dụ thực tế: Khách hàng truy cập Trang Chủ (`/home`)

Hãy tưởng tượng bạn gõ URL `http://localhost:8080/Ban_Hoa_Qua_Online/home` vào trình duyệt. Luồng đi sẽ qua 5 bước sau:

**👉 Bước 1: Filter (Trạm kiểm soát vé)**
Trước khi vào hệ thống, Request đi qua các lớp Filter (`EncodingFilter`, `AuthFilter`...). Filter kiểm tra xem: *Ông này gõ tiếng việt UTF-8 chưa? Ông này URL này có cần bắt đăng nhập không?* URL `/home` là cho phép khách lạ (Guest) nên Filter mở cổng cho qua.

**👉 Bước 2: Controller nhận lệnh (`HomeServlet.java`)**
Server thấy URL là `/home`, liền gọi `HomeServlet` ra tiếp khách.
```java
// Servlet nhận request
String keyword = req.getParameter("keyword"); 
```

**👉 Bước 3: Gọi Model lấy dữ liệu (`CategoryDAO` & `ProductDAO`)**
Servlet không tự mình nói chuyện với Database. Nó gọi thằng DAO: *"Ê DAO, móc cho tao danh sách các Category đang bán và sản phẩm Flash Sale lên đây"*.
```java
// DAO chạy câu lệnh SQL
List<Category> categoriesList = categoryDAO.findAllActive();
List<Map<String, Object>> flashSale = productDAO.findFlashSaleProductsOptimized();
```

**👉 Bước 4: Controller gói hàng gửi cho View**
Sau khi DAO trả về 2 cục dữ liệu là `categoriesList` và `flashSale`, Controller sẽ "đóng gói" chúng vào một cái hộp gọi là `Request Scope` (đặt tên nhãn mác đàng hoàng) để gửi sang cho View.
```java
// Đóng gói dữ liệu
req.setAttribute("categories", categoriesList);
req.setAttribute("flashSaleProducts", flashSale);

// Bắn thẳng sang file home.jsp
req.getRequestDispatcher("/WEB-INF/jsp/guest/home.jsp").forward(req, resp);
```

**👉 Bước 5: View nhận hàng và vẽ HTML (`home.jsp`)**
File `home.jsp` hoàn toàn bị giấu kín trong thư mục `WEB-INF` (không ai gõ URL trực tiếp vào file này được). Nó nhận cái "hộp" dữ liệu, bóc ra và dùng thẻ **JSTL** (`<c:forEach>`) để vẽ lặp qua từng trái cây và in thành mã HTML.
```jsp
<!-- Đoạn mã trong JSP -->
<c:forEach items="${flashSaleProducts}" var="product">
    <div class="product-card">
        <h3>${product.name}</h3>
        <p>Giá: ${product.price} VNĐ</p>
    </div>
</c:forEach>
```
Cuối cùng, Tomcat gửi cục HTML thuần túy đó về cho trình duyệt. Trình duyệt vẽ lên màn hình. Kết thúc luồng!

---

## 3. Tóm tắt "Câu Thần Chú" khi bạn cần đọc hoặc sửa code

Nếu trưởng nhóm giao bạn sửa một tính năng cụ thể (Ví dụ: **Chi tiết sản phẩm**):
1. Đừng tìm file HTML trước. Hãy mở website lên, bấm vào tính năng đó xem **URL là gì**. (Ví dụ: `/product?id=5`)
2. Mở NetBeans, dùng tổ hợp phím **Ctrl + O**, tìm Class Servlet có map `@WebServlet("/product")`. Ở đó chứa luồng điều hướng (Controller).
3. Xem Servlet đó đang gọi hàm gì ở **Service/DAO** (Model) -> Nhảy vào DAO để xem hoặc sửa câu lệnh SQL.
4. Xem cuối hàm Servlet, chữ `forward` đang trỏ đến file `.jsp` nào -> Mở file JSP đó (View) ra để sửa giao diện HTML/CSS.
