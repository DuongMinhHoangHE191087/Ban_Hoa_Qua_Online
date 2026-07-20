# Screen capture checklist

Quy tắc đánh dấu field/nút, evidence ID và điều kiện nâng cấp Failed/Normal/Best xem tại [`LOC_EVIDENCE_AND_SCREEN_UPGRADE_PLAYBOOK.md`](LOC_EVIDENCE_AND_SCREEN_UPGRADE_PLAYBOOK.md).

Bảng HTML nối màn hình, ảnh annotated, JSON evidence và tổng LOC: [`loc-evidence-dashboard.html`](loc-evidence-dashboard.html).

Mục tiêu: chụp ảnh hiện trạng từng màn bằng Playwright và dùng ảnh làm bằng chứng UI, không coi ảnh là bằng chứng đủ cho transaction. Nguồn route phải đối chiếu với `@WebServlet`, `web.xml`, JSP và filter.

## Cách chạy

```powershell
npm install
npx playwright test playwright-tests/screen-capture.spec.ts --project=chromium
```

Ảnh được ghi vào `artifacts/screenshots/<project>/<actor>/`, với project `chromium` cho desktop và `mobile-screens` cho iPhone 12. Network/console evidence tương ứng được ghi vào `artifacts/screen-evidence/<project>/<actor>/`. Màn public chạy được ngay. Màn protected tự skip nếu chưa cung cấp credential qua biến môi trường phiên hiện tại:

Nếu một trang quá dài vượt giới hạn ảnh của Chromium, test tạo ảnh `*-viewport.png` và ghi cảnh báo; đó là bằng chứng viewport, cần bổ sung ảnh cuộn/chunk thủ công nếu cần toàn bộ nội dung.

```powershell
$env:PW_CUSTOMER_EMAIL='...'; $env:PW_CUSTOMER_PASSWORD='...'
$env:PW_SHOP_EMAIL='...'; $env:PW_SHOP_PASSWORD='...'
$env:PW_DELIVERY_EMAIL='...'; $env:PW_DELIVERY_PASSWORD='...'
$env:PW_ADMIN_EMAIL='...'; $env:PW_ADMIN_PASSWORD='...'
npx playwright test playwright-tests/screen-capture.spec.ts
```

Không ghi mật khẩu vào file test, `.env`, report hoặc ảnh.

## Inventory cần chụp

| ID | Actor | Route | Màn | Public/role | Ảnh | Logic/DB analysis |
|---|---|---|---|---|---|---|
| G01-G07 | Guest | `/home`, `/products`, `/products/detail`, `/shop-view`, `/cart`, `/about`, `/contact` | Khám phá sản phẩm và thông tin | Public | [ ] | [ ] |
| A01-A03, A17-A18 | Guest | `/auth/login`, `/auth/register`, `/auth/forgot`, `/auth/verify`, `/auth/reset-password` | Đăng nhập, đăng ký, xác minh, khôi phục | Public/flow-state | [ ] | [ ] |
| C01-C16 | Customer | dashboard, checkout/order/payment/success/invoice, profile, return, review, notification, chat, shop apply, change password | Mua hàng, đơn hàng, hậu mãi, tài khoản | Customer | [ ] | [ ] |
| S01-S13 | Shop | status, dashboard, profile, products/editor, inventory, orders, promotions, settlement, reports, chat, settings, returns | Vận hành shop | Shop owner | [ ] | [ ] |
| D01-D03 | Delivery | dashboard/list/detail | Nhận và giao đơn | Delivery | [ ] | [ ] |
| A04-A16, A19-A21 | Admin | dashboard, users/detail, shops/manage, products/categories, orders/monitor, payments/settlements, refunds/reviews, reports/config/profile, chat/notifications | Quản trị và kiểm soát nền tảng | Admin | [ ] | [ ] |

## Trạng thái phải chụp thêm cho từng màn chính

- [ ] Loaded/data
- [ ] Empty/no data
- [ ] Validation error giữ lại input hợp lệ
- [ ] Server error/retry
- [ ] Permission denied/unauthenticated
- [ ] Success sau POST/PRG
- [ ] Destructive confirm/disabled đang submit
- [ ] 1280px desktop và 390px mobile

## Action/API không phải màn độc lập

Các mapping sau cần trace trong test flow hoặc API test, không tạo ảnh như một màn: `/api/checkout/quote`, `/api/coupon/validate`, `/api/cart/sync`, `/api/payment/webhook`, `/delivery/api/update`, `/delivery/confirm-success` (POST multipart), `/admin/shops/approve`, `/admin/users/status`, `/admin/users/revoke-sessions`, notification/chat/address/upload APIs. Bằng chứng đúng là request/response, DB before/after, authorization và idempotency.

## Definition of done

- [ ] Mỗi route trong manifest trả status < 400 ở đúng actor.
- [ ] Ảnh có tên ID ổn định, không chứa secret/token/PII ngoài dữ liệu seed cần thiết.
- [ ] Mỗi màn có dòng trace trong `docs/SCREEN_PROCESS_ANALYSIS.md`.
- [ ] POST/action được kiểm tra PRG, permission, ownership và transaction; ảnh chỉ là bằng chứng render.
