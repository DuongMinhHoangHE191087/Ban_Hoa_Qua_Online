# Transaction evidence map

Đây là kết quả trace code hiện tại, bổ sung cho screenshot matrix. Màn hình chỉ chứng minh render/HTTP; các dòng dưới đây chỉ được đánh `PASS` khi có thêm DB before/after hoặc integration test tương ứng.

## Checkout → order → payment

| Flow | Entry/fields | Code evidence | DB boundary/tables | Required proof |
|---|---|---|---|---|
| Checkout quote | cart/variant IDs, address, slot, voucher, payment method | `CheckoutServlet` → `CheckoutService.buildCheckoutView`; `CheckoutQuoteServlet` | Reads product variants, inventory, promotions, address; no write | Same server-calculated total in UI/API; invalid/expired/stock cases |
| Place order | address, slot, notes, voucher, paymentMethod, cart items | `CheckoutServlet` → `CheckoutService.placeOrder` | `CheckoutService` explicitly disables auto-commit and commits/rolls back; `orders`, `order_items`, `inventory_logs`, promotions/payment setup | DB snapshot proves no partial order/stock on failure; exact total and stock decrement |
| Payment page | `orderId`, customer session | `CheckoutServlet` → `PaymentService.getCustomerPaymentSummary/getPaymentByOrder/renewQr` | `payment_transactions`, `orders`, system config | Customer ownership; reference/amount/status match; expired QR renewal |
| Manual payment confirm | order/payment ID, customer session | `CheckoutServlet` → `PaymentService.confirmManualPayment` | Payment status and order state in transaction | Legal state only; repeat request idempotent |
| SePay webhook | provider transaction ID, reference/code, amount, direction, account | `PaymentWebhookServlet` → `PaymentService.processWebhook` | Explicit transaction; dedup record, `payment_transactions`, `orders`, parent orders | Exact amount/reference/account; duplicate ID; wrong account; mismatch; rollback |

Important code finding: `OrderService.placeOrder(int, CheckoutDTO)` is explicitly `UnsupportedOperationException`; the live checkout path must remain on `CheckoutService.placeOrder`, and tests/docs must not claim the unused method is the order transaction.

## Order lifecycle

| Flow | Fields | Code evidence | DB effects | Required proof |
|---|---|---|---|---|
| Customer order view | orderId, status, page | `OrderServlet` → `OrderViewService`/`OrderDAO` | Read `orders`, `order_items`, payment/delivery joins | Query derives customer from session; ID tampering denied |
| Shop confirm/cancel/dispatch | orderId, reason, estimated time | `ShopOrderServlet` → `OrderService.confirmOrder/cancelOrder/dispatchOrder` | `orders`; dispatch path has explicit commit/rollback | Valid status transition, shop ownership, PRG, no duplicate submit |
| Customer receive/not received | orderId, reason | `OrderServlet` → `OrderService.customerConfirmDelivery/reportNotReceived` | `orders.received_status`; possible refund/payment side effects | Legal delivery state/time window; audit |
| Delivery update | deliveryId, status, failure reason, proof URL | `DeliveryUpdateAPI` → `DeliveryService.updateStatusAndProof/markAsDelivered` | Explicit transaction; `deliveries`, `orders`, parent order cascade | Staff assignment, legal state, proof validation, rollback |

## Return, review and settlement

| Flow | Fields | Code evidence | DB effects | Required proof |
|---|---|---|---|---|
| Return request | order/item, request type, reason, description, evidence, quantity, resolution | `ReturnRequestServlet` → `ReturnService.createRequest` | `return_requests`; order refund status may become `PENDING` | Customer/item ownership, eligibility window, file ownership, no duplicate request |
| Return decision | requestId, status, reason, decidedBy | `ReturnRequestServlet` → `ReturnService.decide` | `return_requests`, `orders.refund_status`, possible payment/refund records | Shop/admin scope, legal transition, financial side effect and audit |
| Review submit/update/delete | orderId, orderItemId, rating, comment, media | `ReviewServlet` → `ReviewService` | `reviews` and media/moderation fields | Delivered order, one review/item, owner predicate, moderation status |
| Settlement/report | period, shop, status | `SettlementServlet`/report servlets → service/DAO | `shop_settlements`, order/payment/refund aggregates | Gross − fee − refund + adjustment = net; date/timezone and ownership |

## Schema invariants to verify

From `database/Setup_OnlineFruitShopping.sql`:

- `orders.status` is constrained to the documented lifecycle; `payment_method` is `CK`/`COD`.
- `orders.refund_status` and `received_status` have constrained state values.
- `order_items.quantity >= 1`; foreign keys connect orders, variants and items.
- `return_requests` constrains request type, reason, quantity, resolution and status.
- `shop_settlements` stores gross, platform fee, refund, adjustment and net values plus audit actors.
- `inventory_logs` records `ORDER_RESERVE`, `ORDER_RELEASE`, `ORDER_CONFIRM`, `RETURN` and manual changes.

## Evidence status

| Evidence class | Current state |
|---|---|
| Route/JSP/fields and GET render | Captured for the current 61-screen manifest; see `SCREEN_CAPTURE_RUN_2026-07-20.md`. |
| HTTP/network/console | Harness writes one JSON per capture under `artifacts/screen-evidence/`. |
| Service/DAO/schema trace | Mapped above and in `SCREEN_TRACEABILITY_MATRIX.md`; source review evidence exists. |
| Mutation DB before/after | Not executed in screenshot run; requires an isolated test database/fixture and explicit mutation-flow run. |
| Rollback/idempotency/webhook | Must be covered by dedicated integration tests; screenshot cannot prove these properties. |

## Existing JUnit evidence candidates

The repository already contains DB-backed tests under `test/test/`; these are the correct next evidence source because they create timestamped fixtures and generally clean them in `@After`. They must still be run in an isolated/local test database and their final output must be recorded before marking a flow `PASS`.

| Screen/flow | Existing test candidate | What it can prove |
|---|---|---|
| C02/C14/C15 checkout/payment | `CheckoutPaymentAuditTest`, `CheckoutServiceExceptionHandlingTest` | order/payment creation, status transitions, amount/reference and failure cleanup |
| C14/C15 webhook | `PaymentWebhookRaceTest`, `PaymentServiceTest`, `PaymentServiceExceptionHandlingTest` | deduplication, concurrent webhook behavior, mismatch/error handling |
| S07 shop orders | `ShopOrderServletRegressionTest`, `OrderServiceExceptionHandlingTest` | ownership, legal transition and exception behavior |
| D01-D03 delivery | `DeliveryServiceTest`, `DeliveryServiceExceptionHandlingTest`, `DeliveryN1QueryRegressionTest` | assignment, delivery status/proof, rollback and query shape |
| C08/S13 returns | `ReturnServiceTest`, `SettlementAndReturnRulesTest` | request/decision rules and refund status side effects |
| C16/A13 reviews | `ReviewModerationValidationTest` | review eligibility/validation and moderation visibility |
| S06 inventory | `InventoryConcurrencyTest`, `InventoryLossTest` | stock reservation concurrency and no-loss invariant |
| A06 shop approval | `ShopApprovalAPISecurityTest`, `ShopServiceTransactionRollbackTest` | role/ownership guard and rollback across user/shop profile writes |
| S09/A11 settlement | `SettlementServiceTest` | settlement calculation and state rules |

The selected list was executed successfully on 2026-07-20: 35 JUnit tests passed. See `TRANSACTION_TEST_RUN_2026-07-20.md`. The remaining repository-wide tests are not included in that PASS claim because the full harness stops at compilation on Selenium dependency gaps and `AdminOrderAndProductEditRegressionTest` errors.
