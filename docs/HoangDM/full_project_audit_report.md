# Full Project Audit Report — Ban_Hoa_Qua_Online

| Field | Value |
| --- | --- |
| **Date** | 2026-07-11 |
| **Mode** | **SCAN-ONLY** (no production code fixes in this pass) |
| **Branch** | `HoangDMHE191087` |
| **Stack** | Java 17, Jakarta Servlet 6, JSP/JSTL, Tomcat 10, Ant/NetBeans, MSSQL |
| **Skip** | Hardcoded/placeholder secrets in `AppConfig` — **not remediated** (user request) |
| **Compile check** | `ant -q compile` → **BUILD SUCCESSFUL** |

---

## 1. Scope / Mode / Skip list

### In scope

1. Project understanding (layers, actors, feature surfaces).
2. Security scan of all major feature groups (AuthZ, CSRF, IDOR, upload, webhook residual risk, XSS samples, SQLi patterns).
3. Business documents vs code vs schema (principle violations / system-error risks).
4. Syntax / IDE resolve / compile diagnosis (including `ActorAccessPolicy` diagnostics).

### Explicit skips

| Item | Status |
| --- | --- |
| AppConfig secrets / env keys / OAuth-email-signing placeholders | **SKIPPED** — not reviewed for remediation |
| Production code changes | **NONE** this pass |
| Dependency CVE scan (Maven/npm) | N/A primary path (Ant + JARs in `web/WEB-INF/lib`) |

### Deliverable

This report only. Recommendations are **text backlog** for a future fix phase.

---

## 2. Project map (read understanding)

### 2.1 Architecture

```text
Browser/JSP  →  Filters (web.xml)  →  Servlet (@WebServlet)
                                      → Service
                                      → DAO (PreparedStatement)
                                      → MSSQL (Schema.sql)
Background: listener/* (auto-cancel, settlement, expiry, payment reconcile)
Realtime: websocket/ChatEndpoint
```

### 2.2 Filter chain (order)

`GlobalExceptionFilter` → `SecurityHeadersFilter` → `RateLimitFilter` → `EncodingFilter` → `LoggingFilter` → `SessionRestoreFilter` → `CsrfFilter` → `AuthFilter` → `RoleFilter`

### 2.3 Actors × surfaces

| Actor | Primary URLs | Guard |
| --- | --- | --- |
| Guest | `/home`, `/products`, `/guest/cart`, `/api/ai/search`, `/api/coupon/validate` | Public |
| Customer | `/checkout`, `/orders`, `/customer/*`, `/chat`, `/reviews`, `/returns`, `/profile` | Auth + Role (+ servlet) |
| Shop Owner | `/shop/*` (APPROVED), `/shop/status` | RoleFilter + profile status |
| Delivery | `/delivery/*` | RoleFilter delivery |
| Admin | `/admin/*` | RoleFilter admin |
| System | Listeners, SePay webhook | No session (webhook public by design) |

### 2.4 Shared policy

- `util.ActorAccessPolicy` — centralized role/area gates used by `RoleFilter` and multiple customer/chat servlets.
- Constants: `config.AppConfig` (roles, order/payment statuses, return window hours, QR expiry).

### 2.5 Inventory (approx.)

| Area | Count / note |
| --- | --- |
| Production Java | ~231 `.java` under `src/java` |
| Servlets | ~67 `@WebServlet` mappings |
| Tests | ~67 under `test/test` |
| JSP | ~75 under `web/WEB-INF/jsp` |
| Docs | SRS Full, Business Rules Catalog, 50 UCs, Feature Tree (103 leaves), prior security reports |

### 2.6 Business doc sources used

| Priority | Document |
| --- | --- |
| P0 | `docs/SRS_Full/SRS_Section_3_Functional_Requirements_FruitShop.docx.md` |
| P0 | `docs/Modules_Features/Business Rules Catalog - Nền Tảng Hoa Quả.md` |
| P0 | `docs/Use_Cases/Use_Case_Specification_Master.md` |
| P0 | `docs/Modules_Features/SRS_Feature_Tree_FruitShop.md` |
| P0 | `database/Schema.sql` |
| P1 | `docs/security-audit-report.md`, `docs/actor_access_hardening_report.html` |
| Domain | `Agents.md` (one-order-one-shop, guest cart, delivery separation, returns/settlement) |

---

## 3. Phase 1 — Compile & IDE (`ActorAccessPolicy`)

### 3.1 User-reported diagnostics

IDE reported on `src/java/filter/RoleFilter.java`:

- `The import util.ActorAccessPolicy cannot be resolved` (line 7)
- `ActorAccessPolicy cannot be resolved` (multiple use sites ~96–135)

### 3.2 Evidence

| Check | Result |
| --- | --- |
| Source file exists | **Yes** — `src/java/util/ActorAccessPolicy.java` (`package util`) |
| Usages consistent | **Yes** — RoleFilter + Checkout/Order/Chat/Return servlets import `util.ActorAccessPolicy` |
| Ant compile | **PASS** (`BUILD SUCCESSFUL`) |
| Real output dir | `build.classes.dir` = `${build.web.dir}/WEB-INF/classes` → `build/web/WEB-INF/classes/` |
| Class present after compile | **Yes** — `build/web/WEB-INF/classes/util/ActorAccessPolicy.class` |
| `build/classes/util/...` | **Missing / stale** — **not** the NetBeans compile target (do not treat as failure) |
| Pollution | **~38 `.class` files mixed into `src/java/**`** next to `.java` |

### 3.3 Verdict

| Classification | Detail |
| --- | --- |
| **IDE / Language Server false-positive (or index hygiene)** | Not a missing type. Source and deploy compile output are valid. |
| **Not a syntax error in RoleFilter** | Imports and call sites match an existing public API. |
| Contributing factors | (1) Dual web facet in `.iml` (`web` + `build/web`); (2) `.class` files under source roots; (3) IDE may index wrong output root `build/classes`. |

### 3.4 Recommendations (fix phase later — not done now)

1. Clean Java LS workspace / reimport NetBeans or IntelliJ module; ensure source root = `src/java` only.
2. Remove `*.class` from `src/java` (keep only in `build/`); add ignore rule if needed.
3. Ignore or exclude stale `build/classes` if tools still emit there.
4. Optional: run `ant clean compile` once in a fix session to fully resync deploy tree.

### 3.5 Other compile/syntax notes

| Item | Finding |
| --- | --- |
| Full production compile | **Green** |
| Stale package paths from old reports (`com.fruitmkt.*`) | Not observed as current compile blockers |
| Duplicate Jackson / multi-version MSSQL JARs on classpath | **INFO/MEDIUM hygiene** — risk of runtime version skew; not compile-fail |
| JSP compile | Not run (would need Tomcat Jasper deploy); separate smoke recommended |

---

## 4. Phase 2 — Security scan (all features)

**AppConfig secret values: SKIPPED.**

### 4.1 Feature-group review status

| Feature group | Surfaces | Status |
| --- | --- | --- |
| FE-01 Auth & profile | Login/Register/OAuth/Forgot/Reset/ChangePassword/Profile/Address | Reviewed |
| FE-02 Product mgmt | Shop product CRUD, inventory, images | Reviewed (ownership via shop APIs) |
| FE-03 Discovery | List/detail/AI search | Reviewed (public) |
| FE-04 Cart | Guest cart, Cart, sync API | Reviewed |
| FE-05 Checkout/order | Checkout, quote, multi-shop, orders | Reviewed |
| FE-06 Payment | QR, webhook, reconcile | Reviewed |
| FE-07 Delivery | Dashboard/detail/update/confirm | Reviewed |
| FE-08 Return/refund | Returns, admin refund | Reviewed |
| FE-09 Chat/notify | ChatAPI, WS, notifications | Reviewed |
| FE-010 Settlement/report | Shop/admin settlement | Reviewed |
| FE-011 Admin | Users, shops, categories, config | Reviewed |

### 4.2 Findings (severity-ranked)

#### CRITICAL

| ID | Title | Evidence | Risk | Recommendation |
| --- | --- | --- | --- | --- |
| **S-C1** | Public debug JSP mutates production DB | `web/test-db.jsp` — unauthenticated page under web root runs `INSERT`/`DELETE` on `orders` via `ConnectionPool` | Anyone who can reach the app can corrupt orders | Remove from deployable `web/` or move under `WEB-INF` + admin + non-prod gate |
| **S-C2** | Payment webhook unauthenticated + weak matching residual | `AuthFilter` public allowlist `/api/payment/webhook`; `PaymentService` no HMAC/signature (project design); account check skippable if both account fields null; reference entropy limited; response may echo order/customer ids | Fake payment confirmation → free goods | In prod: require SePay API key/HMAC; always enforce expected account; stronger refs; rate-limit; no PII in response. *Signature was intentionally out of prior pass — residual remains HIGH/CRITICAL depending on exposure.* |

#### HIGH

| ID | Title | Evidence | Risk | Recommendation |
| --- | --- | --- | --- | --- |
| **S-H1** | `/uploads/*` public serve of persistent upload tree | `UploadsServlet` — only blocks `..`; no canonical-path sandbox; can serve `shop-docs` / chat media if path known | KYC/docs leak; path abuse | Canonical root check; never serve private docs via public servlet; signed URLs or auth for private media |
| **S-H2** | CSRF bypass on `/auth/*` leaves reset/forgot-verify unprotected | `CsrfFilter` skips all `/auth/*`; `ResetPasswordServlet` / `ForgotVerifyServlet` have **no** `_csrf` checks (unlike Login/Register/Forgot) | CSRF password set after OTP session | Manual CSRF on those servlets **or** narrow CSRF exemptions |
| **S-H3** | Chat `markRead` IDOR | `ChatAPI` POST `markRead` → `chatDAO.markRead(sessionId, userId)` without session membership check | Mark others’ threads read | Reuse chat access authorization before mark |
| **S-H4** | Return video evidence not actually stored | `ReturnRequestServlet` counts video parts but sets `evidence_url` from **client filename**, does not `FileUploadUtil.save` for video | Policy gameable; weak evidence trail | Save validated video files; enforce in service layer |

#### MEDIUM

| ID | Title | Evidence | Recommendation |
| --- | --- | --- | --- |
| **S-M1** | `/profile`, `/profile/order-detail` not in AuthFilter/RoleFilter maps | Servlet-local session/role checks only | Map `/profile/*` in filters for defense in depth |
| **S-M2** | Chat `getOnlineStatus` without membership | Any authed user can probe online for sessionId/userId | Require participant/admin |
| **S-M3** | Arbitrary `mediaUrl` in chat | Stored/broadcast without same-origin allowlist | Allow only upload servlet URLs |
| **S-M4** | `500.jsp` may show log file path | `resolvedLogFilePath` / app log path | User-facing: errorId only |
| **S-M5** | Session cookie `Secure=false` in `web.xml` | Comment says enable on HTTPS prod | Force Secure + HSTS in production |
| **S-M6** | CSP `unsafe-inline` | `SecurityHeadersFilter` | Nonces/hashes long-term |
| **S-M7** | Forgot/OTP not rate-limited like login | `RateLimitFilter` scopes | Rate-limit forgot + verify |
| **S-M8** | Admin refund `orderId` not bound to return request | Possible wrong order refund on param mismatch | Derive orderId from return request only |
| **S-M9** | `OrderService.cancelOrder` privileged path not explicit allowlist | Non-customer/non-shop may cancel any if they reach service | Explicit ADMIN (and system) allowlist |
| **S-M10** | `/shop/docs` RoleFilter allows any authed user | Servlet enforces admin/owner (OK if solid) | Optionally restrict filter roles; keep servlet checks |

#### LOW / INFO

| ID | Title | Note |
| --- | --- | --- |
| **S-L1** | XSS residual in attributes | Many pages use `c:out`; some product/shop attributes use raw `${}` |
| **S-L2** | SQL injection | DAOs use `PreparedStatement`; dynamic `IN (?,?,?)` OK — **no classic SQLi found** |
| **S-L3** | IDOR positives | Order detail ownership, shop order mutate, address API, delivery staffId, chat load/WS mostly solid |
| **S-L4** | CSRF general | POST CSRF global; GET mutations largely cleaned; chat markRead rejects GET |

### 4.3 Positive security controls (keep)

- Layered filters + `ActorAccessPolicy` RBAC hardening.
- Guest must login for checkout (`AuthFilter` + `RoleFilter` customer-only).
- CSRF on most mutations; cart unload no longer broad-exempt.
- Error message sanitization via `ErrorMessageUtil` (prior audit).
- Shop docs download ownership + path traversal string checks in `ShopDocDownloadServlet`.
- Delivery state transitions and staff assignment checks.

---

## 5. Phase 3 — Business rules ↔ code ↔ schema

Legend: **ALIGNED** | **PARTIAL** | **VIOLATION** | **DOC-AHEAD** | **CODE-AHEAD**

### 5.1 Core / Agents.md invariants

| Rule | Verdict | Evidence |
| --- | --- | --- |
| One order → one shop owner | **ALIGNED** | `CheckoutService` splits multi-shop; child orders have single `owner_id`; parent shell for group payment |
| Guest cart session/localStorage | **ALIGNED** | Guest cart JS + sync; not treated as customer DB cart until login |
| Login required for checkout | **ALIGNED** | Auth + Role on `/checkout` |
| Delivery actor separate | **ALIGNED** | `/delivery/*` role gate + staffId checks |
| Returns / refunds / settlement consistent with schema | **ALIGNED** (with partials below) | Tables + services wired; evidence multi-file weak |

### 5.2 Business Rules Catalog / SRS focus matrix

| RuleID | Topic | Verdict | Evidence / note |
| --- | --- | --- | --- |
| **ORD-SHOP-01** | Multi-shop split | **ALIGNED** | Parent/CHILD + per-owner reserve |
| **USR-05** | Guest → login for checkout | **ALIGNED** | Filters + checkout servlet policy |
| **INV-01** | Stock hold + release / auto-cancel unpaid | **ALIGNED** | Reserve on checkout; release on cancel; auto-cancel unpaid ~15 min |
| **ORD-01** | Cancel only before processing | **PARTIAL** | Code allows `PENDING_PAYMENT` **and** `CONFIRMED`; blocks from `PREPARING`+ — stricter catalog says Pending only |
| **PAY-01** | COD max 2,000,000 | **ALIGNED** | Checkout COD cap |
| **SEC-01** | COD lock after 3 failed deliveries / 30d | **ALIGNED** | `OrderService.isCodEligible` path |
| **PAY-02** | Online payment timeout 10 min | **PARTIAL** | `QR_EXPIRE_MINUTES=10`; stock/order auto-cancel still **15 min** — dual clocks |
| **REF-01** | Return window ~6h after DELIVERED | **ALIGNED** | Config + `ReturnService` from delivered_at |
| **REF-02** | ≥1 video or 2 photos | **PARTIAL** | Servlet enforces counts; service does not; video not saved; single `evidence_url` |
| **REV-01** | Review only after delivered | **PARTIAL** | Servlet checks; `ReviewService` does not re-check delivered |
| **PAY-03** | Settlement hold ~12h + open returns excluded | **ALIGNED** | Auto settlement + DAO filters |
| **USR-01** | Shop pending until admin approve | **ALIGNED** | RoleFilter APPROVED gate; reject/suspend side-effects |
| **PRD-01** | Weight variants required | **ALIGNED** (soft) | Create path requires variants; blank weight defaults to 1.000 kg |
| **PRD-03** | Seasonal availability | **ALIGNED** | SQL month window on public product queries |
| **INV-02** | Low stock alerts | **ALIGNED** | Threshold + notification on stock change |
| **DEL-01** | 20 km delivery radius | **DOC-AHEAD** | No georadius enforcement found in services |
| **USR-03** | Google Maps lat/long required on address | **DOC-AHEAD** | No Maps geocode gate found |
| **USR-04** | Shop cannot self-order own products | **PARTIAL / light** | Checkout blocks same `userId` as owner for items; not full device/IP anti-fraud |
| **PRO-01..03** | Coupon stacking / flash / budget | **PARTIAL** | Coupon engine exists; full enterprise stacking not exhaustively proven in this pass |
| **DEL-02/03** | 30 kg express / ETA maps | **DOC-AHEAD** | Not fully found as hard gates |

### 5.3 Schema spot-check

| Area | Supports rules? |
| --- | --- |
| `orders` parent/child, `owner_id` | Yes multi-shop model |
| `payment_transactions.expires_at` | Yes QR window |
| `return_requests` | Window/status; single evidence URL limits REF-02 |
| `shop_settlements` / settlement orders | Hold/settlement uniqueness |
| `product_variants.weight_kg` | DB constraint > 0 |
| `inventory_logs` reserve/release | Audit trail present |

### 5.4 Principle-risk summary (no hard VIOLATION on core 14)

Highest residual **business** risks:

1. Review / return evidence enforced mainly at servlet boundary (**PARTIAL**).
2. QR 10 min vs stock release 15 min (**operational mismatch**).
3. Catalog enterprise logistics (20 km, Maps) still **DOC-AHEAD**.
4. Weight default 1 kg softens PRD-01 intent.

**No CRITICAL domain VIOLATION** found against Agents.md one-order-one-shop / guest cart / delivery separation.

---

## 6. Prioritized fix backlog (recommendations only)

| Priority | IDs | Action (future fix phase) |
| --- | --- | --- |
| **P0** | S-C1 | Delete or hard-gate `web/test-db.jsp` before any shared/public deploy |
| **P0** | S-C2 | Harden webhook for production exposure (signature/API key + mandatory account match) |
| **P1** | S-H1 | Harden `UploadsServlet` + isolate private docs |
| **P1** | S-H2 | CSRF on reset password / forgot-verify |
| **P1** | S-H3, S-H4 | Chat markRead authz; real return video storage + service-level REF-02 |
| **P2** | S-M1–S-M10 | Profile filter maps, chat online/mediaUrl, 500 path leak, Secure cookie, rate-limit forgot, refund orderId bind, cancel allowlist |
| **P2** | Business PARTIAL | Align QR vs auto-cancel clocks; push REV/REF checks into services |
| **P3** | IDE hygiene | Remove `src/java/**/*.class`; reindex IDE; dual-facet cleanup |
| **P3** | DOC-AHEAD | Decide: implement Maps/radius later or mark catalog as future roadmap |
| **Skip** | AppConfig secrets | Explicitly out of this audit’s remediation scope |

---

## 7. Suggested commands for a future fix / verify phase

```bat
REM Compile
"C:\Program Files\Apache NetBeans\extide\ant\bin\ant.bat" -q clean compile
"C:\Program Files\Apache NetBeans\extide\ant\bin\ant.bat" -q compile-test

REM Security-related tests (if DB/env available)
REM RoleFilterAccessControlTest, CsrfProtectionTest, XssInputSanitizationTest,
REM LoginServletSecurityTest, ShopApprovalAPISecurityTest, PaymentWebhookRaceTest,
REM CheckoutMultiShopTest, SettlementAndReturnRulesTest, InventoryConcurrencyTest
```

Smoke paths after fixes: login × 4 roles → guest cart → checkout COD/QR → shop product → delivery update → admin approve shop → return request → settlement job dry-run.

---

## 8. Compliance with scan-only mode

| Gate | Result |
| --- | --- |
| Compile diagnosis documented | **PASS** (ant SUCCESS + ActorAccessPolicy class in real output) |
| IDE sample verdict | **IDE hygiene / false-positive**, not missing source |
| Security matrix all feature groups | **Reviewed** |
| Business matrix core + catalog sections | **Documented** (ALIGNED/PARTIAL/DOC-AHEAD) |
| AppConfig secrets | **Skipped as requested** |
| Production code modified | **No** (report file only) |

---

## 9. Closing note

The project has a coherent layered architecture, centralized actor policy, and solid coverage on core marketplace invariants (multi-shop split, guest cart, COD rules, return window, settlement hold, delivery RBAC). The highest immediate operational risks are **debug JSP with live DB writes**, **payment webhook residual trust model**, and **public upload serving**, not the IDE red squiggles on `ActorAccessPolicy`.

When ready for a **fix phase**, start with P0 items in section 6; keep AppConfig secret handling as a separate, explicit security task if desired later.
