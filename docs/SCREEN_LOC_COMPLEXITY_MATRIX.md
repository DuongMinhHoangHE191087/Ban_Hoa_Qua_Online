# Screen/Function LOC Complexity Matrix

## 1. Chuẩn tính

Ma trận này áp dụng quy tắc từ bảng LOC được cung cấp:

`Effective fields (E) = max(actionable fields (F), transactions (T) × 2)`.

| Level | E | Base LOC | Failed 50% | Normal 75% | Best 100% |
|---|---:|---:|---:|---:|---:|
| L1 | 3–5 | 60 | 30 | 45 | 60 |
| L2 | 6–7 | 90 | 45 | 68 | 90 |
| L3 | 8–9 | 120 | 60 | 90 | 120 |
| L4 | 10–11 | 150 | 75 | 113 | 150 |
| L5 | 12–13 | 180 | 90 | 135 | 180 |
| L6 | 14–15 | 210 | 105 | 158 | 210 |
| L7 | >15 | 240 | 120 | 180 | 240 |

`Normal LOC` làm tròn đến số nguyên gần nhất. F/T là baseline đọc từ route, JSP, service/DAO và transaction evidence; cần cập nhật nếu DOM thực tế cho thấy khác biệt.

## 2. Ma trận toàn bộ màn hình

| ID | Actor | Màn hình/chức năng | F | T | E | Level | Normal LOC | Luồng dữ liệu/transaction chính |
|---|---|---|---:|---:|---:|---:|---:|---|
| G01 | Guest | Home | 5 | 2 | 5 | L1 | 45 | Đọc danh mục/sản phẩm |
| G02 | Guest | Product list/search | 8 | 3 | 8 | L3 | 90 | Đọc sản phẩm, lọc, phân trang |
| G03 | Guest | Product detail | 10 | 4 | 10 | L4 | 113 | Đọc chi tiết, tồn kho, đánh giá |
| G04 | Guest | Login | 6 | 2 | 6 | L2 | 68 | Xác thực tài khoản, tạo session |
| G05 | Guest | Register | 8 | 4 | 8 | L3 | 90 | Validate và ghi customer |
| G06 | Guest | Access denied/error | 0 | 0 | 0 | L1 | 45 | Render lỗi, không ghi DB |
| G07 | Guest | Static/help | 5 | 1 | 5 | L1 | 45 | Đọc nội dung tĩnh |
| A01 | Auth | Profile | 6 | 3 | 6 | L2 | 68 | Đọc/cập nhật profile |
| A02 | Auth | Change password | 10 | 4 | 10 | L4 | 113 | Kiểm tra mật khẩu, cập nhật credential |
| A03 | Auth | Logout | 6 | 3 | 6 | L2 | 68 | Hủy session |
| A17 | Auth | Forgot password | 5 | 3 | 6 | L2 | 68 | Tạo token, gửi/ghi reset state |
| A18 | Auth | Reset password | 6 | 3 | 6 | L2 | 68 | Kiểm tra token, cập nhật password |
| C01 | Customer | Dashboard | 6 | 3 | 6 | L2 | 68 | Đọc tổng quan customer |
| C02 | Customer | Cart checkout | 15 | 8 | 16 | L7 | 180 | Cart, voucher, shipping, order, payment |
| C03 | Customer | Cart | 7 | 3 | 7 | L2 | 68 | Đọc/cập nhật cart |
| C04 | Customer | Checkout address | 10 | 5 | 10 | L4 | 113 | Địa chỉ, shipping quote |
| C05 | Customer | Order list | 7 | 4 | 8 | L3 | 90 | Đọc orders, filter, paging |
| C06 | Customer | Order detail | 9 | 5 | 10 | L4 | 113 | Đọc order, payment, delivery |
| C07 | Customer | Payment | 9 | 3 | 9 | L3 | 90 | Tạo payment intent, callback state |
| C08 | Customer | Payment result | 12 | 5 | 12 | L5 | 135 | Đọc payment/order, reconcile result |
| C09 | Customer | Wishlist | 8 | 4 | 8 | L3 | 90 | Đọc/thêm/xóa wishlist |
| C10 | Customer | Product review | 13 | 6 | 13 | L5 | 135 | Validate và ghi review |
| C11 | Customer | Return request | 6 | 3 | 6 | L2 | 68 | Tạo return request |
| C12 | Customer | Return detail | 8 | 4 | 8 | L3 | 90 | Đọc return/refund state |
| C13 | Customer | Notifications | 7 | 3 | 7 | L2 | 68 | Đọc/đánh dấu notification |
| C14 | Customer | Address book | 8 | 5 | 10 | L4 | 113 | CRUD địa chỉ |
| C15 | Customer | Voucher | 5 | 4 | 8 | L3 | 90 | Đọc/validate voucher |
| C16 | Customer | Support/contact | 10 | 5 | 10 | L4 | 113 | Tạo yêu cầu hỗ trợ |
| S01 | Shop | Shop dashboard | 6 | 3 | 6 | L2 | 68 | Đọc KPI/shop data |
| S02 | Shop | Product list | 8 | 4 | 8 | L3 | 90 | Đọc/lọc sản phẩm shop |
| S03 | Shop | Product create | 11 | 5 | 11 | L4 | 113 | Validate và ghi product |
| S04 | Shop | Product detail/edit | 8 | 4 | 8 | L3 | 90 | Đọc/cập nhật product |
| S05 | Shop | Inventory | 14 | 7 | 14 | L6 | 158 | Ghi stock adjustment, audit |
| S06 | Shop | Order list | 10 | 5 | 10 | L4 | 113 | Đọc orders của shop |
| S07 | Shop | Order processing | 10 | 6 | 12 | L5 | 135 | Status transition, fulfillment |
| S08 | Shop | Order detail | 12 | 6 | 12 | L5 | 135 | Đọc order/items/payment |
| S09 | Shop | Return list | 9 | 5 | 10 | L4 | 113 | Đọc/lọc return |
| S10 | Shop | Return handling | 8 | 4 | 8 | L3 | 90 | Duyệt/từ chối return |
| S11 | Shop | Review moderation | 9 | 5 | 10 | L4 | 113 | Đọc và xử lý review |
| S12 | Shop | Voucher management | 8 | 4 | 8 | L3 | 90 | CRUD voucher |
| S13 | Shop | Settlement | 11 | 6 | 12 | L5 | 135 | Đọc/đối soát settlement |
| S14 | Shop | Shop settings | 14 | 7 | 14 | L6 | 158 | Cập nhật shop/config |
| D01 | Delivery | Delivery dashboard | 6 | 3 | 6 | L2 | 68 | Đọc shipment assignments |
| D02 | Delivery | Delivery list | 7 | 3 | 7 | L2 | 68 | Đọc/lọc delivery |
| D03 | Delivery | Delivery detail/update | 10 | 5 | 10 | L4 | 113 | Cập nhật trạng thái giao |
| A04 | Admin | Admin dashboard | 8 | 4 | 8 | L3 | 90 | Đọc KPI toàn hệ thống |
| A05 | Admin | User list | 8 | 4 | 8 | L3 | 90 | Đọc/lọc user |
| A06 | Admin | User detail/edit | 12 | 6 | 12 | L5 | 135 | Đọc/cập nhật user/role |
| A07 | Admin | Shop approval | 10 | 5 | 10 | L4 | 113 | Duyệt/từ chối shop |
| A08 | Admin | Product moderation | 10 | 5 | 10 | L4 | 113 | Duyệt/khóa product |
| A09 | Admin | Category management | 8 | 4 | 8 | L3 | 90 | CRUD category |
| A10 | Admin | Order management | 11 | 6 | 12 | L5 | 135 | Đọc/điều chỉnh order |
| A11 | Admin | Order detail | 10 | 5 | 10 | L4 | 113 | Đọc order/payment/audit |
| A12 | Admin | Transaction ledger | 11 | 6 | 12 | L5 | 135 | Đọc ledger, reconcile |
| A13 | Admin | Payment webhook/audit | 9 | 5 | 10 | L4 | 113 | Idempotency, webhook audit |
| A14 | Admin | Return/refund | 8 | 4 | 8 | L3 | 90 | Duyệt refund/return |
| A15 | Admin | Review moderation | 8 | 4 | 8 | L3 | 90 | Duyệt/ẩn review |
| A16 | Admin | Voucher management | 9 | 5 | 10 | L4 | 113 | CRUD/activate voucher |
| A19 | Admin | Settlement management | 10 | 5 | 10 | L4 | 113 | Đối soát/thanh toán shop |
| A20 | Admin | Audit logs | 12 | 6 | 12 | L5 | 135 | Đọc audit/filter/export |
| A21 | Admin | System settings | 7 | 4 | 8 | L3 | 90 | CRUD system settings |

## 3. Quy tắc nghiệm thu LOC

## 3. Tổng hợp hiện tại

| Chỉ số | Kết quả |
|---|---:|
| Số màn hình/chức năng | 61 |
| Tổng actionable fields (F) | 533 |
| Tổng transactions (T) | 262 |
| Tổng effective fields (E) | 552 |
| Tổng LOC quy đổi - Failed 50% | 4,065 |
| Tổng LOC quy đổi - Normal 75% | 6,113 |
| Tổng LOC quy đổi - Best 100% | 8,130 |

Các tổng trên là tổng LOC quy đổi theo màn hình, không phải số dòng thực tế trong repository. LOC vật lý production đã đo riêng trong báo cáo kiểm kê mã nguồn.

- `Failed LOC = Base LOC × 50%`.
- `Normal LOC = Base LOC × 75%`.
- `Best LOC = Base LOC × 100%`.
- Không cộng các dòng của bảng điểm dự án ở ảnh 1 vào tổng LOC màn hình; bảng đó là điểm đánh giá tổng thể theo số màn, chất lượng và non-LOC.
- F/T là chỉ số tính độ phức tạp, không phải physical LOC của Java/JSP/SQL.
- Khi chụp DOM thực tế, cập nhật F/T và giữ evidence screenshot, route, response, DAO/service, bảng DB và test ID trong `SCREEN_TRACEABILITY_MATRIX.md`.
