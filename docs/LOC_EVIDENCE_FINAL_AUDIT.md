# LOC Evidence Final Audit

## Trạng thái kiểm chứng

| Hạng mục | Kết quả | Bằng chứng |
|---|---|---|
| Manifest màn hình | PASS - 61 màn | `playwright-tests/screen-capture.spec.ts` |
| Desktop/mobile capture | PASS - 122/122 | `node tools/verify-screen-evidence.mjs` |
| Ảnh đánh dấu field/nút | PASS - 122/122 | `artifacts/screenshots/**/**-annotated.png` |
| DOM UI evidence | PASS - JSON có `uiEvidence[]` | `artifacts/screen-evidence/**` |
| LOC complexity matrix | PASS - 61 dòng | `docs/SCREEN_LOC_COMPLEXITY_MATRIX.md` |
| Checklist nâng cấp | PASS | `docs/LOC_EVIDENCE_AND_SCREEN_UPGRADE_PLAYBOOK.md` |
| Remediation backlog | PASS - 19 màn cần review | `docs/UI_EVIDENCE_REMEDIATION_BACKLOG.md` |
| Transaction test evidence | PARTIAL - nhóm transaction chính | `docs/TRANSACTION_TEST_RUN_2026-07-20.md` |

## Cách chốt LOC

1. Dùng `F` và `T` trong ma trận, không dùng số element DOM thô để tự động tăng LOC.
2. Tính `E = max(F, T × 2)` rồi lấy Level tương ứng.
3. Dùng Base LOC theo Level.
4. Chọn rate 50%/75%/100% chỉ sau khi đủ evidence chất lượng tương ứng.
5. Với mỗi transaction, phải gắn route/request/response/service/DAO/schema và test.

## Gaps không được che giấu

- `UI_REVIEW` nghĩa là số field DOM visible thấp hơn baseline F; LOC vẫn `LOC_READY` nếu `T × 2` phủ E theo quy tắc ảnh. Cần review field ẩn/database để hoàn thiện UI evidence.
- Screenshot annotated không chứng minh transaction hoặc database write.
- Không chốt Best cho màn chỉ có happy-path screenshot.
- Các màn P0 trong backlog phải được ưu tiên vì liên quan payment, refund, settlement, approval và audit.

## Lệnh xác minh lại

```powershell
npx.cmd playwright test playwright-tests/screen-capture.spec.ts
node tools/verify-screen-evidence.mjs
node tools/verify-ui-evidence.mjs
```
