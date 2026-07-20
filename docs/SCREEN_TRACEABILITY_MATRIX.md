# Screen traceability matrix

Ma trận này là kết quả đọc route annotation, JSP forward và domain flow hiện có. `UNVERIFIED` nghĩa là cần mở đúng Service/DAO/schema hoặc chạy DB evidence; không được coi là PASS chỉ vì trang render được.

## Guest và authentication

| ID | Route | Render source | Fields/input chính | Read / write transaction | Evidence cần thu |
|---|---|---|---|---|---|
| G01 | `/home` | `HomeServlet` → `guest/home.jsp` | search, category, CTA | Read home/catalog; no write | screenshot, HTTP, empty/error |
| G02 | `/products` | `ProductListServlet` → `guest/product-list.jsp` | keyword, categoryId, min/maxPrice, page | Read filtered products/categories; no write | query preservation, pagination, DB count |
| G03 | `/products/detail?id` | `ProductDetailServlet` → `guest/product-detail.jsp` | productId, variantId, quantity | Read product/variant/stock; session cart write on add | invalid ID, quantity/stock, cart before/after |
| G04 | `/shop-view?id` | `ShopViewServlet` → `shop/view.jsp` | shopId, page/filter | Read active shop/products; no write | ownership/data visibility, empty shop |
| G05 | `/cart` or `/guest/cart` | `CartServlet`/`GuestCartServlet` → cart JSP | variantId, quantity, remove | Session/local guest state; customer cart sync UNVERIFIED | reload, empty, stock check, state before/after |
| G06 | `/about` | `AboutServlet` → `guest/about.jsp` | none | No DB write; static/render read | screenshot, HTTP |
| G07 | `/contact` | `ContactServlet` → contact view | contact fields if form exists | Message write UNVERIFIED | validation, response, DB if applicable |
| A01 | `/auth/login` | `LoginServlet` → `auth/login.jsp` | identifier, password, redirect, CSRF | Read user/lock; write session/login audit | redirect safety, wrong password, session cookie |
| A02 | `/auth/register` | `RegisterServlet` → `auth/register.jsp` | fullName, phone, email, password, confirm, terms | Write user + verification token; transaction/rollback | invalid fields, duplicate email, DB before/after |
| A03 | `/auth/forgot` | `ForgotPasswordServlet` → `forgot-password.jsp` | email/identifier, CSRF | Read user; write reset token/send | no account enumeration, token expiry evidence |
| A17 | `/auth/verify` | `VerifyEmailServlet` → `auth/verify.jsp` | OTP/code, email/session | Write verified state; token single-use | invalid/expired/replay, user state before/after |
| A18 | `/auth/reset-password` | `ResetPasswordServlet` → `auth/reset-password.jsp` | token, password, confirm | Write password hash; invalidate token/sessions | token ownership/expiry, hash not plaintext |

## Customer

| ID | Route | Render source | Fields/input chính | Read / write transaction | Evidence cần thu |
|---|---|---|---|---|---|
| C01 | `/customer/dashboard` | `CustomerDashboardServlet` → `customer/dashboard.jsp` | date/filter if present | Read customer-scoped orders/vouchers | session owner predicate, empty/error |
| C02 | `/checkout` | `CheckoutServlet` → `customer/checkout.jsp` | address, delivery slot, voucher, paymentMethod, cart | Read cart/address/quote; no order until final submit | server recompute total, quote response, no write on refresh |
| C03 | `/orders` | `OrderServlet` → `customer/orders.jsp` | status, page, date | Read orders by authenticated customer | cross-user ID, stable pagination/count |
| C04 | `/orders/detail` | `OrderServlet` → `customer/order-detail.jsp` | orderId | Read order/items/payment/delivery by owner | ownership denial, status actions |
| C05 | `/notifications` | `NotificationServlet` → `customer/notification.jsp` | read/filter/page | Read notifications; mark read/delete writes | unread count before/after, authorization |
| C06 | `/chat` | `ChatServlet` → `customer/chat.jsp` | conversationId, message, upload | Read conversation; message/media writes | participant authorization, API response |
| C07 | `/profile` | `UserProfileServlet` → `common/profile.jsp` | profile fields, avatar | Read/write own user profile | ownership, validation, PRG |
| C08 | `/returns` | `ReturnRequestServlet` → `customer/return-request.jsp` | order/item, reason, evidence | Read eligible order; write return request | time/status eligibility, attachment ownership |
| C09 | `/customer/order-reviews` | `OrderReviewListServlet` → `customer/order-reviews.jsp` | order/page | Read delivered/unreviewed items | eligibility query, empty state |
| C10 | `/customer/shop-apply` | `ShopApplyServlet` → `customer/shop-apply.jsp` | store, businessEmail, address, category, document | Write shop/application/document; transaction boundary | duplicate submit, file validation, status before/after |
| C11 | `/auth/change-password` | `ChangePasswordServlet` → `auth/change-password.jsp` | current/new/confirm password | Write password hash and session invalidation policy | wrong current, CSRF, session behavior |
| C12 | `/profile/order-detail` | `ProfileOrderDetailServlet` → `profile-order-detail.jsp` | orderId | Read own order profile projection | ID tampering, data minimization |
| C13 | `/profile/order-detail?action=invoice` | `ProfileOrderDetailServlet` → `customer/invoice.jsp` | orderId/action | Read invoice/order snapshot; no write | ownership, totals match order |
| C14 | `/checkout?action=payment` | `CheckoutServlet` → `customer/order-payment.jsp` | order/payment reference | Read pending payment intent | amount/reference exactness, no false success |
| C15 | `/checkout?action=success` | `CheckoutServlet` → `customer/order-success.jsp` | order/payment reference | Read server-confirmed order/payment | webhook confirmation before success |
| C16 | `/reviews?action=submit` | `ReviewServlet` → `customer/review-submit.jsp` | orderId, itemId, rating, comment, media | Read eligibility; write review | one-review rule, delivered rule, moderation state |

## Shop owner

| ID | Route | Render source | Fields/input chính | Read / write transaction | Evidence cần thu |
|---|---|---|---|---|---|
| S01 | `/shop/status` | `ShopStatusServlet` → `shop/status.jsp` | status/reason, resubmit CTA | Read own shop/application; resubmit write if enabled | status state matrix, owner predicate |
| S02 | `/shop/dashboard` | `ShopDashboardServlet` → `shop/dashboard.jsp` | date/filter | Read shop-scoped KPI/orders/stock | shop isolation, KPI query evidence |
| S03 | `/shop/profile` | `ShopProfileServlet` → `shop/profile.jsp` | name, description, logo/banner, address | Read/write own shop profile | upload ownership, PRG, DB before/after |
| S04 | `/shop/products` | `ProductManageServlet` → `shop/product-list.jsp` | search/status/page | Read products by owner | cross-shop isolation, pagination |
| S05 | `/shop/product-create` | `ProductCreateServlet` → product form | product, variants, images, price/stock | Write product/variant/image/inventory | validation, transaction rollback, files |
| S06 | `/shop/inventory` | `InventoryServlet` → `shop/inventory.jsp` | product/variant, quantity/filter | Read inventory; adjustment write if enabled | no negative stock, audit |
| S07 | `/shop/orders` | `ShopOrderServlet` → `shop/orders.jsp` | status/page/action | Read own-shop order items; status write | legal transition, PRG, ownership |
| S08 | `/shop/promotions` | `PromotionServlet` → `shop/promotion.jsp` | code, dates, scope, limits | Read/write shop promotion | date/overlap/usage constraints |
| S09 | `/shop/settlement` | `SettlementServlet` → `shop/settlement.jsp` | period/page | Read ledger/balance; payout action if enabled | ledger reconciliation, no cross-shop data |
| S10 | `/shop/reports` | `ShopReportServlet` → `shop/report.jsp` | from/to, grouping | Read aggregate shop data | definitions, timezone, empty/error |
| S11 | `/shop/chat` | `ShopChatServlet` → `shop/chat.jsp` | conversation/message/media | Read/write authorized conversation | participant and upload checks |
| S12 | `/shop/settings` | `ShopSettingsServlet` → `shop/settings.jsp` | settings fields | Read/write shop settings | CSRF, confirmation, audit |
| S13 | `/returns` (shop actor) | `ReturnRequestServlet` → `shop/return-requests.jsp` | requestId, decision, reason | Read shop-owned return; write decision/refund side effect | item ownership, legal state, audit |
| S14 | `/shop/product-edit?id` | `ProductEditServlet` → product form | product/variant/image fields | Read/write shop-owned product | ID tampering, rollback, file ownership |

## Delivery

| ID | Route | Render source | Fields/input chính | Read / write transaction | Evidence cần thu |
|---|---|---|---|---|---|
| D01 | `/delivery/dashboard` | `DeliveryDashboardServlet` → `delivery/dashboard.jsp` | status/date | Read assigned delivery summary | assignment predicate |
| D02 | `/delivery/list` | `DeliveryDashboardServlet` → `delivery-list.jsp` | status/date/page | Read assigned deliveries | stable pagination, no other staff data |
| D03 | `/delivery/detail?id` | `DeliveryDetailServlet` → `delivery-detail.jsp` | deliveryId, evidence | Read assignment/order; update via API | recipient minimization, ownership |

## Admin

| ID | Route | Render source | Fields/input chính | Read / write transaction | Evidence cần thu |
|---|---|---|---|---|---|
| A04 | `/admin/dashboard` | `AdminDashboardServlet` → `admin/dashboard.jsp` | date/filter | Read platform KPI | KPI SQL/definition, admin guard |
| A05 | `/admin/users` | `AdminUserServlet` → `admin/user-list.jsp` | search/status/page | Read users | PII minimization, pagination |
| A06 | `/admin/shops` | `ShopApprovalServlet` → `admin/shop-approvals.jsp` | status/page/decision | Read queue; approve/reject write | reason, audit, state transition |
| A07 | `/admin/products` | `AdminProductServlet` → `admin/admin-products.jsp` | search/status/category/action | Read/moderate products | admin guard, reason/audit |
| A08 | `/admin/categories` | `CategoryServlet` → `admin/admin-categories.jsp` | name/status/action | Read/write categories | uniqueness, references, rollback |
| A09 | `/admin/orders` | `AdminOrderServlet` → `admin/orders.jsp` | status/page | Read operational orders | no unsafe mutation from list |
| A10 | `/admin/payments` | `AdminPaymentServlet` → `admin/payment-dashboard.jsp` | status/date/reference | Read payment ledger; manual review write if enabled | exact amount, idempotency |
| A11 | `/admin/settlements` | `AdminSettlementServlet` → `admin/admin-settlements.jsp` | shop/period/status | Read/write settlement review | ledger totals, audit |
| A12 | `/admin/refunds` | `AdminRefundServlet` → `admin/admin-refunds.jsp` | status/decision/reason | Read/write refund decision | legal state, financial side effects |
| A13 | `/admin/reviews` | `AdminReviewServlet` → `admin/review-management.jsp` | visibility/status/reason | Read/write moderation | audit, customer notification |
| A14 | `/admin/reports` | `AdminReportServlet` → `admin/report.jsp` | date/grouping | Read aggregate reports | revenue/fee/refund definitions |
| A15 | `/admin/config` | `AdminConfigServlet` → `admin/admin-config.jsp` | config key/value/effective date | Read/write config/history | validation, authorization, audit |
| A16 | `/admin/chat` | `AdminChatServlet` → `admin/chat.jsp` | conversation/message | Read/write support conversations | participant authorization |
| A19 | `/admin/users/view?id` | `AdminUserViewServlet` → `admin/user-view.jsp` | userId, status/action | Read user; status/session writes | self-action guard, audit |
| A20 | `/admin/shops/manage?id` | `AdminShopManageServlet` → `admin/admin-shops.jsp` | shopId, status/reason | Read/write shop management | ownership not inferred from query, audit |
| A21 | `/admin/notifications` | `AdminNotificationServlet` → `admin/admin-notifications.jsp` | read/filter/action | Read/write admin notifications | unread/read authorization |

## Evidence protocol

For every row, attach all applicable evidence:

1. Screenshot: `artifacts/screenshots/<project>/<actor>/<ID>-*.png`.
2. Route evidence: final URL, HTTP status, actor/session and redirect chain.
3. Source evidence: Servlet line, forward JSP, Service method, DAO query and schema table/constraint.
4. Write evidence: request fields, DB before/after, commit/rollback, affected row count and audit record.
5. Security evidence: unauthenticated, wrong-role, cross-owner ID and expired-session cases.

`PASS` chỉ được ghi khi đủ evidence tương ứng. `SKIPPED` = thiếu credential/runtime; `RUNTIME_BLOCKED` = deployment/DB không chạy; `UNVERIFIED` = chưa trace Service/DAO/schema; `GAP` = có bằng chứng sai.
