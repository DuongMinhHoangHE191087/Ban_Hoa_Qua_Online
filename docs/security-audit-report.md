# Ban_hoa_qua_online - Security, Logic, and OOP Audit Report

## Scope

Audited the current codebase for:

- Guest, Customer, Shop, Admin, and Delivery flows
- Servlet / Service / DAO / JSP boundaries
- CSRF, authorization, input validation, secret handling, and webhook handling
- OOP duplication and logic drift across shared flows

Webhook SePay is assessed under the explicit project constraint that no authentic/signature layer is added in this pass.

## Current Status After This Pass

The repository now compiles successfully with the NetBeans Ant build and the narrower compile-test pass.

Verified fixes applied in this pass:

- Removed the duplicate method block in `CheckoutPricingEngine`
- Narrowed the `/cart` CSRF exemption to `syncOnUnload` only
- Added CSRF headers to cart sync / mutation client calls
- Split `AddressAPIServlet` GET from POST mutation flow
- Validated that `ChatAPI.createShopSession` only accepts real `SHOP_OWNER`
- Normalized phone validation in checkout and address flows
- Aligned QR expiry with `AppConfig.QR_EXPIRE_MINUTES`
- Switched auto-cancel job to the system cancel path
- Externalized config reads for app environment, URLs, and secret values with production validation
- Removed hardcoded DB credential defaults from `AppConfig` and `docker-compose.yml`; test harnesses now resolve DB credentials from env/system properties
- Aligned legacy docs/examples with the env-based config model
- Removed the dead `autoConfirmDeliveredOrders()` seam from `OrderService`
- Consolidated shop registration profile creation/upsert into `ShopService`
- Routed `RegisterServlet` shop profile checks through `ShopService` instead of direct DAO access
- Routed `ShopApplyServlet` shop profile checks through `ShopService` instead of direct DAO access
- Routed `ShopStatusServlet` shop profile lookup through `ShopService` instead of direct DAO access
- Replaced hardcoded Google OAuth, email password, and token signing secrets with placeholder-backed config lookup
- Sanitized SePay webhook failure logs so raw payloads are no longer emitted
- Confirmed the SePay webhook endpoint stays unauthenticated by design and always returns HTTP 200 while relying on deduplication, reference matching, amount matching, account matching, and payment-status gates
- Sanitized auth-facing error propagation in login, forgot-password, reset-password, change-password, verify-email, Google callback, and register flows so they no longer echo raw exception text to JSPs
- Sanitized customer notification API error responses so client-facing exception text is no longer leaked there
- Sanitized checkout quote validation errors so the public quote API no longer returns raw exception text
- Sanitized shop/admin/customer operational handlers including settlement, settings, inventory, admin config/report/category, shop approval, shop profile upload, and customer dashboard so they no longer echo raw exception details back to clients
- Sanitized chat bootstrap / chat API internal errors, the address API, and guest product restock errors so they no longer echo raw exception details back to clients
- Tightened the `/api/*` public allowlist to keep guest AI search reachable while still protecting authenticated API routes
- Centralized user-facing database error mapping in `ErrorMessageUtil` so shared servlet helpers no longer append raw `SQLException` details
- Converted the remaining cart validation flow to code-bearing `BusinessException` responses and sanitized the websocket chat, JSON writer, connection-pool shutdown, and order retry heuristics so no direct `e.getMessage()` paths remain in production source
- Routed `/api/*` login gating through `AuthFilter` with public allowlist for `coupon/validate`, `ai/search`, and `payment/webhook`, so API authentication no longer depends solely on per-servlet session checks
- Tightened `RoleFilter` so checkout, chat, and review routes are customer-only, returns now include admin/shop/customer, and the `/customer/*` namespace stays customer-only
- Routed shop status redirect helper through `ShopService` and cached the resolved shop profile in session instead of querying the DAO directly from the helper
- Reused the shop status redirect helper from `ShopApplyServlet` so the onboarding path has one fewer bespoke redirect branch
- Aligned the QR countdown fallback in `order-payment.jsp` to 10 minutes
- Moved chat `markRead` from GET to CSRF-protected POST in both shop and customer chat screens

## High-Risk Findings Still Requiring Follow-Up

### 1. Secrets and production config

- `src/java/config/AppConfig.java`
- Config values now read from system properties or environment variables when present.
- `validateSecretsForProduction()` now fails fast in production when default fallback secrets or localhost URLs are still in use.
- Hardcoded Google OAuth secret, email password, and token signing key were replaced with placeholder-backed lookup values.
- DB credentials are now externalized instead of hardcoded, and the regression tests resolve them from env/system properties.
- Chat read markers now use POST, so the previous GET mutation path is closed.

Impact:

- Source leak equals credential leak.
- Production startup now fails fast for placeholder Google/email/signing secrets and localhost URLs.
- Deployment still needs explicit environment provisioning for DB credentials, but the source no longer bakes in default username/password values.

### 2. Central auth model is partially centralized

- `web/WEB-INF/web.xml`
- `src/java/filter/AuthFilter.java`
- `src/java/filter/RoleFilter.java`
- Some `/api/*` servlets still keep action-local role checks for defense in depth, but the shared route policy now matches the actual checkout/chat/returns/customer surfaces more closely.

Impact:

- Lower risk than before because `/api/*` login is now filter-based.
- Role rules can still drift if new route-local checks are added without a shared policy helper.

### 3. Shop onboarding is split across multiple entry points

- `src/java/servlet/auth/RegisterServlet.java`
- `src/java/servlet/customer/shop/ShopApplyServlet.java`
- `src/java/servlet/shop/shop/ShopStatusServlet.java`
- `src/java/service/shop/ShopService.java`

Impact:

- Same business flow is still exposed through more than one servlet path.
- The profile persistence path is centralized now, but route-level duplication remains.

### 4. Webhook logging is too verbose for sensitive payloads

- `src/java/service/shop/PaymentService.java`

Impact:

- Raw webhook payload logging was sanitized in this pass.
- The webhook remains signature-less by explicit project instruction, so the remaining control surface is deduplication, reference matching, amount matching, account matching, and order status validation.

## Actor-by-Actor Review

### Guest

- Guest cart remains session/local-storage based, which matches the project rule.
- Guest-facing product pages and shop view still drive chat session creation through `ChatAPI`; ownership validation is now tightened.

### Customer

- Checkout validation is centralized more cleanly now.
- CSRF on cart mutation paths is now enforced, including `/cart` actions except the unload beacon case.
- Address mutation via GET has been removed.

### Shop

- Product status mutations and promotion management remain isolated to shop-owner flows.
- Shop onboarding still has split entry points, but the profile create/update path now runs through `ShopService` and no longer directly manipulates DAO state from the servlet.

### Admin

- Admin settlement and refund routes are role-guarded.
- Main remaining admin risk is policy consistency, not an obvious exposed mutation bypass.

### Delivery

- Delivery update routes are properly role-guarded.
- Delivery state machine and proof requirements are structurally sound in the current code.

## Coverage Matrix

| Actor | Main code surfaces audited | Status | Notes |
| --- | --- | --- | --- |
| Guest | `src/java/servlet/guest/product/ProductListServlet.java`, `ProductDetailServlet.java`, `ShopViewServlet.java`, `GuestCartServlet.java`, `AiSearchServlet.java`, `HomeServlet.java`, `LoginServlet.java`, `RegisterServlet.java` | Mostly verified | Guest catalog and cart remain read-only/local-state where intended. Guest checkout still requires authenticated customer flow. |
| Customer | `src/java/servlet/customer/cart/CartServlet.java`, `CheckoutServlet.java`, `AddressAPIServlet.java`, `OrderServlet.java`, `ReviewServlet.java`, `ReturnRequestServlet.java`, `NotificationServlet.java`, `ChatServlet.java`, `PaymentService.java`, `PaymentWebhookServlet.java` | Hardened | CSRF, phone validation, chat ownership, and customer error surfaces are now normalized with code-based cart validation. |
| Shop | `src/java/servlet/shop/product/ProductCreateServlet.java`, `ProductEditServlet.java`, `ProductStatusServlet.java`, `InventoryServlet.java`, `PromotionServlet.java`, `SettlementServlet.java`, `ShopOrderServlet.java`, `ShopProfileServlet.java`, `ShopSettingsServlet.java`, `ShopApplyServlet.java`, `ShopService.java` | Partially consolidated | Shop onboarding still has multiple entry points, but the profile create/update logic is centralized in `ShopService` and the redirect helper now resolves profiles through that service. Product/order/settlement seams are functionally sound but still contain duplicated error-handling patterns. |
| Admin | `src/java/servlet/admin/shop/ShopApprovalServlet.java`, `AdminShopManageServlet.java`, `AdminProductServlet.java`, `AdminOrderServlet.java`, `AdminRefundServlet.java`, `AdminSettlementServlet.java`, `AdminUserServlet.java`, `AdminConfigServlet.java`, `AdminNotificationServlet.java` | Mostly verified | Role gates and mutation paths are in place. Main gap is consistent response sanitization and policy normalization across admin APIs. |
| Delivery | `src/java/servlet/delivery/DeliveryServlet.java`, `DeliveryUpdateAPI.java`, `DeliveryDetailServlet.java`, `DeliveryDashboardServlet.java`, `DeliveryConfirmSuccessServlet.java`, `service/order/DeliveryService.java` | Verified at route seam | Delivery role checks and sequential status handling are structurally okay. Remaining gap is harmonized error messaging, not a known bypass. |

## Residual Risk Ledger

- `src/java/servlet/auth/RegisterServlet.java`, `src/java/servlet/customer/shop/ShopApplyServlet.java`, `src/java/service/shop/ShopService.java`
  - Shop onboarding still has multiple entry points, but the profile create/update logic is now centralized in `ShopService` and `ShopApplyServlet` now reuses the redirect helper for the existing-profile case.
- `web/WEB-INF/web.xml`, `src/java/filter/AuthFilter.java`, `src/java/filter/RoleFilter.java`
  - Login policy for `/api/*` is now centralized in `AuthFilter`, and the shared role filter now mirrors the main customer/shop/admin route split. Some API servlets still keep action-level checks.

## Notable Logic / OOP Standardization Improvements

- Centralized phone validation instead of inline regex drift
- Reduced duplicate checkout validation logic by removing the duplicated methods
- Aligned payment expiry between server/service/UI
- Used the system cancel path for background order cancellation
- Tightened chat ownership checks to match actual shop ownership

## SePay Note

Per project instruction, no authentic/signature verification was added for SePay webhook handling in this pass.

The webhook flow currently relies on:

- idempotency / deduplication
- reference matching
- amount matching
- beneficiary account matching
- pending-payment status gate

## Validation

- `ant -q compile-test` completed successfully after the fixes in this pass.

## Recommendation Order

1. Consolidate remaining shop onboarding entry points into one canonical flow
2. Normalize API auth/CSRF policy into a single route strategy
3. Normalize response/error surfaces across customer/admin/shop handlers
