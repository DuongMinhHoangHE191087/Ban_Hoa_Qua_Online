# **BUSINESS RULES CATALOG (DANH MỤC QUY TẮC NGHIỆP VỤ)**

**Dự án:** Nền tảng Mua sắm Hoa quả Trực tuyến (Online Fruit Shopping)

**Mức độ:** Enterprise (Bao gồm Vận hành, Kế toán, Pháp lý & Marketing)

**Tham chiếu:** Feature Tree Document (FE-01 đến FE-011)

## **1\. User & Account Management (Phân quyền & Rủi ro)**

*Quản lý vòng đời tài khoản, bảo mật và kiểm soát rủi ro nền tảng.*

| ID | Rule definition (Định nghĩa quy tắc) | Type of rule | Static/Dynamic | Source |
| :---- | :---- | :---- | :---- | :---- |
| **USR-01** | **(Phê duyệt Shop):** Tài khoản "Shop Owner" đăng ký mới mặc định ở trạng thái Pending. Chỉ được kích hoạt (Active) sau khi Admin duyệt (Approve) Giấy phép ĐKKD hoặc CCCD và Giấy chứng nhận VSATTP. | Constraint | Static | Platform Policy / Pháp luật |
| **USR-02** | **(Bảo vệ dữ liệu):** Một tài khoản Customer chỉ được phép lưu tối đa 5 địa chỉ nhận hàng (Delivery Address). Bắt buộc phải có 1 địa chỉ được set là Default. | Constraint | Static | System Architect |
| **USR-03** | **(Xác thực tọa độ):** Mọi địa chỉ được thêm mới (Manage Addresses) phải được xác thực qua Google Maps API để lấy chính xác kinh độ/vĩ độ (Lat/Long), phục vụ cho giao hàng hỏa tốc. | Constraint | Dynamic | Logistics Standard |
| **USR-04** | **(Chống thao túng):** Tài khoản Shop Owner không được phép tự đặt hàng các sản phẩm thuộc sở hữu của chính Shop mình (kiểm tra qua Device ID và IP) để chống buff đơn/đánh giá ảo. | Constraint | Dynamic | Risk Management |
| **USR-05** | **(Khách vãng lai \- Guest):** Người dùng Guest (chưa đăng nhập) được phép duyệt sản phẩm và thêm vào giỏ hàng (Add to Cart), nhưng **bắt buộc** phải Đăng nhập/Đăng ký (Login/Registration) mới được phép tiến hành Checkout. | Action Enabler | Static | Security Policy |
| **SEC-01** | **(Chống bom hàng):** Nếu Customer có lịch sử từ chối nhận hàng (Failed Delivery do lỗi người mua) quá 3 lần trong 30 ngày, hệ thống tự động khóa vĩnh viễn tính năng thanh toán COD đối với tài khoản đó. | Constraint | Dynamic | Risk Management |
| **SEC-02** | **(Xử lý vi phạm):** Khi Admin thực hiện "Block Account" một Shop Owner, toàn bộ sản phẩm của Shop tự động chuyển sang trạng thái Hidden (Ẩn) và hệ thống tự động hủy (Cancel & Refund) mọi đơn hàng đang ở trạng thái Pending của Shop đó. | Action Enabler | Dynamic | Security Policy |

## **2\. Product & Inventory (Sản phẩm & Tồn kho)**

*Đặc thù trái cây tươi sống, truy xuất nguồn gốc và kiểm soát chống sai sót.*

| ID | Rule definition (Định nghĩa quy tắc) | Type of rule | Static/Dynamic | Source |
| :---- | :---- | :---- | :---- | :---- |
| **PRD-01** | **(Biến thể bán hàng):** Mọi sản phẩm trái cây bắt buộc phải tạo kèm ít nhất 1 biến thể khối lượng (Weight Variants \- VD: hộp 500g, thùng 3kg). Không cho phép bán theo "Số lượng quả" nếu không có trọng lượng tham chiếu. | Constraint | Static | Product Manager |
| **PRD-02** | **(Truy xuất nguồn gốc):** Nếu sản phẩm được gắn tag "Hoa quả nhập khẩu" hoặc "Hữu cơ (Organic)", bắt buộc phải nhập mã số lô hàng (Batch Number) và upload file chứng nhận. | Constraint | Dynamic | Food Safety Law |
| **PRD-03** | **(Mùa vụ tự động \- Seasonal):** Các sản phẩm được cài đặt "Thời gian mùa vụ" (Seasonal Availability) sẽ tự động chuyển trạng thái thành Out of Season (Ẩn nút mua) khi ngày hiện tại hệ thống nằm ngoài khoảng thời gian đã cấu hình. | System Decision | Dynamic | Product Logic |
| **PRD-04** | **(Bảo vệ giá sai sót):** Để tránh việc Shop nhập sai giá (VD: 10k/1kg cherry), hệ thống chặn việc thiết lập Giá khuyến mãi (Discount Pricing) thấp hơn 80% so với Giá gốc (Base Pricing) trừ khi được duyệt bởi Admin. | Constraint | Static | Pricing Policy |
| **INV-01** | **(Giữ kho Real-time):** "Stock Quantity" (Tồn kho) phải được giữ (Hold) ngay lập tức khi khách bấm thanh toán (Checkout) trong vòng 15 phút. Quá 15 phút không thanh toán, tồn kho tự động nhả (Release). | System Decision | Dynamic | Inventory Manager |
| **INV-02** | **(Cảnh báo hết hàng):** Sự kiện "Low Stock Alerts" tự động gửi Notification cho Shop Owner khi tổng tồn kho của một biến thể giảm xuống dưới mức tối thiểu (VD: \< 5kg). | Fact | Dynamic | Inventory Manager |
| **INV-03** | **(Ẩn khi hết hàng):** Nếu tất cả các biến thể khối lượng (Weight Variants) của một sản phẩm đều có Stock Quantity \= 0, sản phẩm đó sẽ không được hiển thị trên Recommendation và mục Best Sellers. | Action Enabler | Dynamic | UX / Marketing |

## **3\. Order & Instant Delivery (Đơn hàng & Giao vận)**

*Yếu tố cốt lõi: Giao hàng hỏa tốc tránh dập nát.*

| ID | Rule definition (Định nghĩa quy tắc) | Type of rule | Static/Dynamic | Source |
| :---- | :---- | :---- | :---- | :---- |
| **DEL-01** | **(Giới hạn bán kính):** Hệ thống chỉ cho phép tiến hành Checkout nếu "Delivery Address" của khách hàng nằm trong bán kính tối đa 20km so với kho của Shop Owner. | Constraint | Dynamic | Logistics Policy |
| **DEL-02** | **(Giới hạn trọng tải):** Tổng khối lượng giỏ hàng sử dụng dịch vụ Giao hàng hỏa tốc không được vượt quá 30kg để đảm bảo khả năng chuyên chở bằng xe máy. | Constraint | Dynamic | Logistics API |
| **DEL-03** | **(Tính thời gian giao):** "Estimated Delivery Time" \= *\[Thời gian chuẩn bị hàng mặc định của Shop (vd: 30p)\] \+ \[Thời gian di chuyển dự kiến theo API Bản đồ\]*. | Computation | Dynamic | Logistics API |
| **ORD-01** | **(Hủy đơn):** Khách hàng chỉ được "Cancel Order" khi đơn ở trạng thái Pending. Nếu Shop đã cập nhật trạng thái sang Processing (Đã cắt/đóng gói hàng), khách không thể tự hủy. | Constraint | Dynamic | Shop Policy |

## **4\. Payment, Promotions & Taxes (Thanh toán, Khuyến mãi & Kế toán)**

*Tuân thủ pháp luật, chống thất thoát doanh thu từ mã giảm giá.*

| ID | Rule definition (Định nghĩa quy tắc) | Type of rule | Static/Dynamic | Source |
| :---- | :---- | :---- | :---- | :---- |
| **PRO-01** | **(Luật cộng dồn mã \- Stacking):** Khách hàng KHÔNG được dùng 2 "Discount Coupons" (giảm giá đơn hàng) cùng lúc. Tuy nhiên, được phép dùng 1 "Discount Coupon" \+ 1 "Free-shipping Coupon" cho một hóa đơn. | Constraint | Static | Marketing Policy |
| **PRO-02** | **(Flash Sale Timeout):** Nếu sản phẩm trong giỏ hàng thuộc chương trình Flash Sale, khách hàng phải hoàn tất thanh toán trước khi Countdown của Flash Sale kết thúc. Nếu lố giờ, giá sẽ tự động revert (quay về) giá Base Pricing. | Computation | Dynamic | Marketing Policy |
| **PRO-03** | **(Ngân sách Khuyến mãi):** Mỗi "Discount Coupon" đều phải có Giới hạn ngân sách (Max Budget \- VD: 50 triệu) hoặc Số lượt dùng tối đa (Max Usages). Khi đạt ngưỡng, mã tự động chuyển trạng thái Expired. | System Decision | Dynamic | Financial Policy |
| **PAY-01** | **(Giới hạn COD):** Phương thức "Cash on Delivery (COD)" chỉ khả dụng với đơn hàng có tổng giá trị (Invoice Total) dưới 2.000.000 VNĐ. Trên mức này bắt buộc thanh toán trước (Thẻ/E-wallet). | Constraint | Static | Financial Policy |
| **PAY-02** | **(Timeout Thanh toán Online):** Đối với thanh toán Credit Card / E-wallet, phiên giao dịch (Payment Processing) phải hoàn tất trong vòng 10 phút. Hết giờ, giao dịch bị hủy và tồn kho được Release. | Fact | Dynamic | Payment Gateway API |
| **PAY-03** | **(Chu kỳ đối soát Shop):** Tiền bán hàng sẽ bị giữ (Hold) trong ví hệ thống của Shop và chỉ chuyển sang số dư khả dụng (Available Balance) sau 12 giờ kể từ khi giao hàng thành công. | Constraint | Static | Platform Finance |
| **ORD-02** | **(Tính tổng đơn):** Invoice Total \= ![][image1] (Base Price \- Discount Price) \* Qty \+ Shipping Fee \- Promotional Coupon. Giá trị Invoice Total tối thiểu là 0 VNĐ (Không được ra số âm). | Computation | Dynamic | Accounting Code |

## **5\. Review & Refund System (Đánh giá & Khiếu nại)**

*Xử lý đặc thù hàng hóa hao hụt.*

| ID | Rule definition (Định nghĩa quy tắc) | Type of rule | Static/Dynamic | Source |
| :---- | :---- | :---- | :---- | :---- |
| **REF-01** | **(Cửa sổ khiếu nại):** Đối với ngành hàng trái cây, Customer chỉ có thể tạo "Refund Request" (Báo dập nát, hỏng) trong thời gian tối đa **06 giờ** kể từ khi nhận hàng. Quá 06 giờ, hệ thống khóa nút khiếu nại. | Constraint | Dynamic | Refund Policy |
| **REF-02** | **(Bằng chứng hoàn tiền):** Mọi Refund Request bắt buộc phải đính kèm ít nhất 1 Video hoặc 2 Ảnh rõ nét chụp sản phẩm hỏng. Áp dụng chính sách "Hoàn tiền một phần/toàn phần, không thu hồi hàng hóa" để tiết kiệm chi phí Logistics. | Constraint | Static | Customer Service |
| **REF-03** | **(Hoàn tiền điện tử):** Nếu khiếu nại được Admin "Approve", tiền sẽ được hoàn về Ví E-wallet/Thẻ tín dụng ban đầu đối với thanh toán Online, hoặc hoàn vào Ví nền tảng (Platform Wallet) đối với thanh toán COD. | Action Enabler | Dynamic | Finance Policy |
| **REV-01** | **(Điều kiện đánh giá):** Tính năng "Write Review" chỉ mở khóa cho sản phẩm trong đơn hàng đã Delivered thành công. Người dùng không thể rate 1 sao cho sản phẩm mà họ chưa từng mua. | Constraint | Dynamic | System Logic |
| **REV-02** | **(Công thức Rating):** Điểm đánh giá (Average Rating) \= Tổng số sao / Tổng số lượt review hợp lệ (Loại trừ các review đã bị Admin Reject). | Computation | Dynamic | System Logic |

[image1]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABMAAAAXCAYAAADpwXTaAAABdklEQVR4Xs1UsUrEQBRMOAQFRaKJISTZpLBRK5v7Am0t9OBs7AS/QMHS0k5QLE79AAULCxu5L7BSsBILsbOQ64VTZ3J7YW/xVjbXODDk9s2+ufdeduM4/xluHMezWZZFVZnn+XjhFATBpBDiDsFvPN/AM7A1jNh3AT6An8yRPGFRhSEWy2CHTJKkrlRtQg3mq+AL8l6RF5dKmqZNCF/gUxiGc0qSETCZQU4b+ZtqfEy2wXZbXKuiCdi/hLzTgSArYmWskJUOiGa4yPH0IMuu9+fHWeq6Lfgve7Ldtud50/oGW3B+V9Jw3+m/9qqIoiiD0TPYxfzWdN0aqGyDZvJQ1nTdCnwZbHfkuck2b/jUNSuwElZkcbWGgm/yEFzRBQN+PbQ0OsLA13XBBHQwL3pXsIQLk12Sv1XBBBhNIOccZjtlkMcA5+nYsbvgi+A9+IH8hSIo7+MjuA2h8QcPwEvsfecNkbwtvra+709hcS16X9mq3NKKHh0/lBF4Q4P/bkAAAAAASUVORK5CYII=>