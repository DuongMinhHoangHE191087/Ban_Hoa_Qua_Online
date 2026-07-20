# UI Evidence Remediation Backlog

Danh sách này được sinh từ `docs/UI_EVIDENCE_VERIFICATION.md` sau lần chạy 122/122 capture. `Expected F` là baseline trong ma trận LOC; `Visible UI` là số field/nút DOM quan sát được. Chênh lệch UI không làm sai LOC khi `T×2` đã phủ `E`; vẫn cần đối chiếu field DB/hidden transaction trong traceability matrix để hoàn thiện evidence UI.

## Ưu tiên P0 - transaction/finance/security

| ID | Expected F | Visible UI desktop/mobile | Việc cần bổ sung | Evidence cần đạt |
|---|---:|---:|---|---|
| A06 | 12 | 7/8 | Xác định các field role/status/approval ẩn và action edit | UI, SEC, REQ, RESP, DBW, AUDIT, TEST |
| A07 | 10 | 3/4 | Bổ sung state approve/reject/lock và lý do xử lý | UI, SEC, REQ, DBW, ERR, TEST |
| A09 | 9 | 5/6 | Trace order filters/status actions còn thiếu | UI, REQ, DBR, SEC, TEST |
| A10 | 11 | 4/5 | Trace payment/transaction actions và exact amount | UI, REQ, DBR/DBW, ERR, TEST |
| A11 | 10 | 4/5 | Bổ sung settlement/detail actions và audit | UI, SEC, DBR/DBW, AUDIT, TEST |
| A12 | 11 | 5/6 | Đối chiếu refund lifecycle, idempotency và rollback | UI, REQ, DBW, ERR, TEST |
| A19 | 10 | 0/1 | Xác định màn manage shop thực tế và route redirect | UI, SEC, REQ, DBR/DBW, ERR, TEST |

## Ưu tiên P1 - role/CRUD/order flows

| ID | Expected F | Visible UI desktop/mobile | Việc cần bổ sung | Evidence cần đạt |
|---|---:|---:|---|---|
| A01 | 6 | 5/5 | Ghi nhận redirect/session/CSRF là hidden transaction evidence | UI, SEC, REQ, RESP, TEST |
| A03 | 6 | 2/2 | Ghi nhận forgot/reset state và token controls | UI, REQ, RESP, DBR/DBW, ERR, TEST |
| A18 | 6 | 2/2 | Bổ sung password confirmation/token expiry evidence | UI, SEC, DBW, ERR, TEST |
| A04 | 8 | 0/1 | Kiểm tra dashboard KPI widgets có render data hay redirect | UI, DBR, RESP, TEST |
| A14 | 9 | 8/8 | Xác định report filters/export action không hiện trên state hiện tại | UI, REQ, RESP, DBR, TEST |
| A21 | 7 | 4/5 | Bổ sung notification filter/read/delete states | UI, REQ, DBR/DBW, SEC, TEST |
| C01 | 6 | 3/2 | Bổ sung dashboard filter/date/KPI evidence nếu có | UI, DBR, RESP, TEST |
| S01 | 7 | 1/1 | Xác định dashboard KPI/action cards và shop ownership | UI, DBR, SEC, TEST |
| S02 | 8 | 0/1 | Kiểm tra product list data/filters và empty state | UI, DBR, REQ, RESP, TEST |
| S06 | 10 | 8/9 | Bổ sung inventory adjustment/stock audit controls | UI, REQ, DBR/DBW, ERR, TEST |
| S07 | 10 | 0/1 | Trace order status transitions và fulfillment actions | UI, SEC, REQ, DBW, ERR, TEST |
| S09 | 9 | 6/7 | Bổ sung settlement filters/export/reconciliation controls | UI, REQ, DBR, DBW, AUDIT, TEST |

## Quy trình đóng từng backlog item

- [ ] Mở đúng route bằng đúng actor và chụp loaded/empty/error/denied state.
- [ ] Đánh số field/nút thực tế; không đếm label, icon hoặc text không actionable.
- [ ] Nếu F lớn hơn DOM, ghi rõ field DB/hidden transaction trong `SCREEN_TRACEABILITY_MATRIX.md`.
- [ ] Gắn `REQ/RESP` cho mỗi action; gắn `DBR/DBW` cho mỗi seam persistence.
- [ ] Chạy negative/ownership/rollback case nếu màn có mutation.
- [ ] Chỉ cập nhật F/E/Level khi evidence mới đã được review.
- [ ] Chỉ chốt Normal khi đủ validation, permission, error và persistence; Best cần thêm tối ưu nghiệp vụ/UX và idempotency/concurrency khi domain yêu cầu.
