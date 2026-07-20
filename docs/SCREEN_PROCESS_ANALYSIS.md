# Screen processing and transaction analysis

Tài liệu này là khung phân tích code-derived. Không kết luận transaction chỉ từ UI: phải đọc Servlet → Service → DAO → schema và kiểm tra commit/rollback, ownership, status transition, response/redirect.

## Quy chuẩn phân tích cho mỗi màn

| Trường | Cần ghi nhận |
|---|---|
| Screen/route | ID, actor, URL, query/path params, public/protected |
| Render | Servlet/controller, forward JSP, model/session attributes, loading/empty/error/success |
| Input | Form/API fields, type, required, length/range, file constraints, CSRF |
| Output | PRG/redirect, HTTP status, flash/session, JSON contract, visible fields |
| Write transaction | Service method, DAO writes, tables, transaction boundary, commit/rollback |
| Read transaction | DAO queries, joins, filters, pagination/order, tenant/owner predicate |
| Invariants | Money/stock/order status, duplicate submission, webhook idempotency |
| Evidence | Screenshot path + Playwright result + logs/DB before-after + source line |

## Baseline by screen group

| Group | Read path cần chứng minh | Write/action cần chứng minh | Invariants |
|---|---|---|---|
| Guest catalog/cart | Product/category/shop DAO → listing/detail JSP; guest cart session/local state | Add/update/remove cart, cart sync | Chỉ active product/variant; quantity > 0; không cross-shop leakage |
| Auth/account | Auth servlet/filter → user/session/profile DAO | Register, verify, login/logout, password/address/profile | Password hash; session renewal/expiry; redirect target safe; ownership by session |
| Checkout/payment | Cart + address + quote service → checkout JSP | Quote, create order, payment intent, webhook/reconciliation | Server recomputes total; stock guarded; exact amount; webhook idempotent; commit atomic |
| Customer order/return/review | Order/return/review DAO filtered by customer ID | Return/review/status actions | Customer owns order/item; legal state/time window; one review per item |
| Shop operations | Shop owner derived from session → product/order/inventory DAO | Product CRUD, stock, order transitions, promotion, settlement | Shop ownership; prepared SQL; legal transitions; no negative stock; PRG |
| Delivery | Assignment DAO filtered by delivery staff ID | Delivery status/evidence update | Assigned order only; legal transition; duplicate-safe audit timestamp |
| Admin | Admin-only query/service and audit | User/shop/product/payment/refund moderation | ADMIN guard at route/service; reason/audit; no unsafe read-only mutation |

## Màn trạng thái và chi tiết phải phân tích riêng

| Màn | Đầu vào cần kiểm tra | Đầu ra/transaction cần chứng minh |
|---|---|---|
| Verify/reset/change password | OTP/token, password mới, confirm password, CSRF | Token hết hạn dùng một lần; password hash; không lộ token; PRG/error giữ field hợp lệ |
| Order payment/success | order ID, payment reference, amount, payment method | Payment page chỉ hiện intent hợp lệ; success chỉ sau server-confirmed state; duplicate callback không tạo order/payment thứ hai |
| Invoice/profile order detail | order ID, action invoice | Customer ownership; dữ liệu invoice lấy từ server; không tin ID từ query; không lộ order khác |
| Review submit | order/item ID, rating, comment, evidence | Đã giao/đủ điều kiện; một review/item; moderation status và notification |
| Shop return requests | request/order/item IDs, decision, reason | Shop chỉ xử lý item thuộc shop; legal return state; audit và refund/stock side effect rõ |
| Delivery confirmation | delivery/order ID, result, failure reason/evidence | Chỉ assignment của delivery staff; legal transition; timestamp/audit; idempotent retry |
| Admin user/shop detail | user/shop ID, status/reason, document action | ADMIN + object existence; self-action guard; approval/status audit; download không lộ path vật lý |

## Chứng minh đạt chuẩn

Một màn chỉ được đánh `PASS` khi có đủ: (1) ảnh desktop/mobile và trạng thái liên quan, (2) route đúng actor, (3) source path của render và input, (4) DAO/schema mapping, (5) kiểm tra success/error/rollback hoặc bằng chứng không có write, (6) ownership/authorization, (7) test hoặc manual evidence có thể lặp lại.

## Mẫu ghi từng màn

```text
ID / route:
Actor + guard:
Servlet -> JSP:
Inputs + validation:
Reads: DAO -> tables/filters:
Writes: Service -> DAO -> tables:
Transaction boundary + rollback:
Output/PRG/API:
Security/ownership:
Screenshot evidence:
Playwright/API/DB evidence:
Finding: PASS | GAP | PLANNED
```

## Gaps cần xử lý sau khi chạy capture

- Route có thể tồn tại nhưng JSP/forward không chạy do Tomcat context/deployment; phải đánh `RUNTIME_BLOCKED`, không đánh pass.
- Route detail cần ID/seed phù hợp; `id=1` chỉ là probe, phải thay bằng fixture hợp lệ khi phân tích nghiệp vụ.
- Màn protected thiếu credential sẽ là `SKIPPED`, không phải pass.
- Webhook, upload, notification và các API action cần evidence request/response + DB, không thể chứng minh bằng screenshot.
