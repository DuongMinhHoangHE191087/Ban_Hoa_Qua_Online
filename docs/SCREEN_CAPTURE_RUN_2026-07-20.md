# Screen capture runtime evidence — 2026-07-20

## Environment

- Base URL: `http://localhost:8080/Ban_Hoa_Qua_Online/`
- Browser project: `chromium` (desktop, 1280px) and `mobile-screens` (iPhone 12)
- Accounts: existing local E2E seed accounts supplied to the process through `PW_*` environment variables; no credentials are stored in source or artifacts.
- Scope: GET/read-only screen capture. No order creation, payment, webhook, upload, status mutation or other business POST was executed.

## Executed evidence

| Scope | Result | Evidence |
|---|---:|---|
| Guest public desktop smoke | 7/7 pass | `artifacts/screenshots/chromium/guest/` |
| Guest public mobile smoke | 7/7 pass | `artifacts/screenshots/mobile-screens/guest/` |
| Customer protected desktop | 16/16 pass | `artifacts/screenshots/chromium/customer/` |
| Shop protected desktop | 14/14 pass | `artifacts/screenshots/chromium/shop/` |
| Delivery GET screens desktop | 3/3 pass | `artifacts/screenshots/chromium/delivery/` |
| Admin protected desktop | 16/16 pass | `artifacts/screenshots/chromium/admin/` |
| Customer protected mobile | 16/16 pass | `artifacts/screenshots/mobile-screens/customer/` |
| Shop protected mobile | 14/14 pass | `artifacts/screenshots/mobile-screens/shop/` |
| Delivery GET screens mobile | 3/3 pass | `artifacts/screenshots/mobile-screens/delivery/` |
| Admin protected mobile | 16/16 pass | `artifacts/screenshots/mobile-screens/admin/` |

The Playwright manifest currently contains 61 renderable screens and 122 tests (desktop + mobile project). The role runs above cover all 61 screens in both projects; the full suite can be listed with:

```powershell
npx.cmd playwright test playwright-tests/screen-capture.spec.ts --list
```

## Findings from runtime, not assumptions

1. `C09 /customer/order-reviews` requires `orderId`; the corrected probe `?orderId=1` rendered successfully. Calling it without that field caused the Servlet's redirect target to return 404, so the field is part of the screen contract.
2. `D04 /delivery/confirm-success` implements only `doPost` multipart upload and redirects to the delivery dashboard. It is action-only, not a GET-rendered screen, and is therefore excluded from the screenshot manifest. Its evidence must be request/response + upload validation + DB/status before/after.
3. Mobile product listing exceeded Chromium's 32,767px image dimension. The capture harness records a `*-viewport.png` fallback and warning instead of falsely claiming a full-page image.

## Status semantics

- `PASS`: route rendered for the intended actor and screenshot was written.
- `SKIPPED`: required credential/runtime fixture was not supplied.
- `RUNTIME_BLOCKED`: server/context/database prevented a valid probe.
- `UNVERIFIED`: screenshot exists, but Service/DAO/schema or mutation evidence is not yet attached.
- `GAP`: runtime contradicted the documented route/contract.

Screenshot PASS does not imply transaction PASS. Use `SCREEN_TRACEABILITY_MATRIX.md` to attach source, DB and security evidence per screen.

Each successful capture also writes a sanitized route/status/console JSON under `artifacts/screen-evidence/`; query strings are reduced to `?…` so tokens and identifiers are not copied into the evidence file.
