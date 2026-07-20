# LOC Evidence & Screen Upgrade Playbook

## 1. Mục đích

Tài liệu này quy định cách biến một field, nút hoặc transaction thành LOC được chứng minh. Screenshot chỉ chứng minh giao diện đã render; LOC xử lý chỉ được công nhận khi có trace đủ từ UI đến route/service/DAO/database hoặc external system.

## 2. Công thức chuẩn

Với mỗi màn hình/chức năng:

```text
E = max(F, T × 2)
Level = level của E
Base LOC = 60, 90, 120, 150, 180, 210 hoặc 240 theo Level 1..7
Evaluated LOC = Base LOC × Quality Rate
```

Quality Rate:

| Mức | Điều kiện bắt buộc | Rate |
|---|---|---:|
| Failed | Chỉ render/happy path lỗi hoặc thiếu transaction | 50% |
| Normal | Happy + validation + permission + error + persistence evidence | 75% |
| Best | Normal + tối ưu UX/business logic + concurrency/idempotency/audit khi có liên quan | 100% |

Không được tăng `F` chỉ bằng cách đếm label hoặc text. Một field được tính khi người dùng nhập/chọn/thay đổi dữ liệu hoặc khi hệ thống có actionable component gắn với xử lý. Một transaction được tính khi có read/write đến DB, session state, file store, API hoặc hệ thống ngoài.

## 3. Evidence ID bắt buộc

Đặt ID ổn định theo mẫu `SCREEN-STATE-TYPE-N`:

| Evidence | Nội dung | Bằng chứng tối thiểu |
|---|---|---|
| `UI` | Field/nút/hiển thị | Screenshot + selector/DOM label + viewport |
| `REQ` | Request từ UI | HTTP method, URL, status, sanitized payload |
| `RESP` | Response | Status, body schema, validation/error envelope |
| `DBR` | Database read | DAO/service, query signature, result/predicate |
| `DBW` | Database write | Before/after row, affected rows, transaction boundary |
| `EXT` | External/API/file | Request/response hoặc file hash/path đã redacted |
| `SEC` | Authorization | actor, owner predicate, denied case |
| `ERR` | Failure/rollback | invalid input, exception, rollback/no partial write |
| `TEST` | Reproduction | test class/method và command chạy |

Ví dụ: `C02-FIELD-05-UI`, `C02-CHECKOUT-REQ-01`, `C02-CHECKOUT-DBW-01`, `C02-CHECKOUT-TEST-01`.

## 4. Checklist chuẩn cho từng màn

Mỗi dòng trong ma trận LOC phải đạt các checkbox sau trước khi chốt Normal/Best:

- [ ] Chụp loaded, empty, validation error, server error và permission state nếu áp dụng.
- [ ] Đánh dấu từng field/nút trên screenshot bằng số; số khớp cột `F` trong ma trận.
- [ ] Với mỗi nút submit/action, ghi `REQ`, `RESP` và route/servlet thực tế.
- [ ] Ghi `DBR` cho dữ liệu đọc và `DBW` cho dữ liệu ghi; nếu chỉ session/local state ghi rõ `SESSION`.
- [ ] Kiểm tra input validation, CSRF nếu có, PRG sau POST và không lộ PII/secret trong evidence.
- [ ] Kiểm tra actor/ownership/role bằng một case được phép và một case bị từ chối.
- [ ] Với tiền, tồn kho, payment, webhook: kiểm tra transaction boundary, idempotency, concurrency và rollback.
- [ ] Gắn ít nhất một `TEST` runnable vào màn hoặc transaction chính.
- [ ] Chỉ chuyển Failed → Normal khi đủ evidence của happy, invalid, error, auth và persistence.
- [ ] Chỉ chuyển Normal → Best khi có bằng chứng tối ưu UX/business logic và thuộc tính an toàn phù hợp domain.

## 5. Checklist nâng cấp theo từng nhóm màn

| Nhóm màn | IDs | Field/nút cần đánh dấu | Evidence nâng cấp bắt buộc |
|---|---|---|---|
| Guest/catalog | G01–G07 | search, filter, product CTA, cart/contact | UI, REQ/RESP, DBR; invalid ID/empty; guest state |
| Authentication | A01–A03, A17–A18 | identifier, password, OTP/token, submit | SEC, ERR, DBR/DBW, session/cookie, replay/expiry |
| Customer order | C01–C06 | cart, address, voucher, payment, order actions | quote recompute, ownership, order/payment DBW, PRG |
| Customer after-sales | C08–C10, C12 | reason, evidence file, review/rating, return action | eligibility, file validation, moderation, DBW/rollback |
| Customer account | C07, C11, C13–C16 | profile/address/notification/chat fields | owner predicate, validation, unread/read or message before/after |
| Shop catalog/inventory | S01–S05 | product fields, stock, import/upload, save | DBW, stock concurrency, audit, invalid file/value |
| Shop order/after-sales | S06–S11 | status buttons, return/review actions | allowed transitions, ownership, rollback, notification |
| Shop finance/settings | S12–S14 | voucher, settlement, config fields | date/amount reconciliation, approval, audit, DBW |
| Delivery | D01–D03 | assignment, status, proof/confirm action | assignment ownership, transition, duplicate/retry, evidence file |
| Admin governance | A04–A11, A14–A16, A21 | filters, approve/disable/edit buttons | role/ownership, audit log, status transition, rollback |
| Admin finance/audit | A12–A13, A19–A20 | reconcile, webhook, export/filter | idempotency key, exact amount, ledger before/after, export audit |

## 6. Quy trình thực hiện cho một màn

```text
1. Route/JSP inventory
   ↓
2. Mark UI fields and buttons
   ↓
3. Capture GET/POST request and response
   ↓
4. Trace servlet → service → DAO → schema/external system
   ↓
5. Capture DB before/after and negative/rollback cases
   ↓
6. Attach TEST evidence
   ↓
7. Recalculate F/T/E and quality level
   ↓
8. Update SCREEN_LOC_COMPLEXITY_MATRIX.md and SCREEN_TRACEABILITY_MATRIX.md
```

## 7. Definition of done cho từng màn

- [ ] Có screenshot desktop/mobile và state checklist phù hợp.
- [ ] Mọi F đều có `UI` evidence; mọi T đều có `REQ/RESP` và seam xử lý.
- [ ] Transaction ghi DB có `DBW` before/after và cleanup.
- [ ] Protected action có `SEC`; lỗi có `ERR`; test có `TEST`.
- [ ] Không có dòng nào ghi `Best` nếu chỉ có screenshot hoặc happy path.
- [ ] Ma trận LOC, traceability matrix và test report dùng cùng Screen ID.

## 8. Lệnh kiểm tra hiện có

```powershell
npx.cmd playwright test playwright-tests/screen-capture.spec.ts
node tools/verify-screen-evidence.mjs
```

Sau khi chạy lại capture, mỗi JSON dưới `artifacts/screen-evidence/<project>/<actor>/` có thêm `uiEvidence[]`; mỗi phần tử có `evidenceId`, tag, type, name/id, label, visible và disabled. Lệnh `--list` đã xác nhận 122 test (61 màn × desktop/mobile). Transaction evidence vẫn phải chạy test/API/DB tương ứng, không suy ra từ screenshot.

Verifier `tools/verify-ui-evidence.mjs` tính coverage theo quy tắc chuẩn `max(visible UI fields, T × 2) >= E`. Vì vậy field UI ít hơn F không còn làm sai LOC nếu transaction conversion đã phủ E; báo cáo vẫn giữ `UI_REVIEW` riêng để yêu cầu bổ sung DOM evidence. F không tự tăng theo số element DOM.

Backlog 19 màn cần xử lý tiếp nằm tại [`UI_EVIDENCE_REMEDIATION_BACKLOG.md`](UI_EVIDENCE_REMEDIATION_BACKLOG.md).
