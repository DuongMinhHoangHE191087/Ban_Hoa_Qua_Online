# LOC Quality Assessment

## 1. Quy tắc chốt phần trăm

| Mức | Rate | Chỉ được dùng khi | Không được dùng khi |
|---|---:|---|---|
| Failed | 50% | Chỉ chứng minh render/happy path hoặc còn thiếu validation/error/authorization | Không dùng để mô tả flow đã đủ test transaction |
| Normal | 75% | Có route/JSP, input validation, quyền, lỗi, đọc/ghi dữ liệu hoặc chứng minh rõ màn read-only | Không gọi Normal chỉ vì ảnh đẹp |
| Best | 100% | Normal + tối ưu UX/nghiệp vụ và test/evidence transaction safety, audit, idempotency/concurrency khi phù hợp | Không dùng nếu chỉ có screenshot/DOM |

Công thức: `E = max(F, T × 2)` → Level → `Evaluated LOC = Base LOC × Quality Rate`. `Normal` làm tròn theo bảng Level.

## 2. Đánh giá từng màn theo code/evidence hiện có

| ID | Actor | Màn hình | F | T | E | Level | Base LOC | Quality | Rate | Evaluated LOC | Lý do kiểm tra |
|---|---|---|---:|---:|---:|---:|---:|---|---:|---:|---|
| G01 | Guest | Home | 5 | 2 | 5 | L1 | 60 | Failed | 50% | 30 | Màn public/static hoặc chưa có đủ unhappy-path/transaction evidence để chốt Normal. |
| G02 | Guest | Product list/search | 8 | 3 | 8 | L3 | 120 | Normal | 75% | 90 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| G03 | Guest | Product detail | 10 | 4 | 10 | L4 | 150 | Normal | 75% | 113 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| G04 | Guest | Login | 6 | 2 | 6 | L2 | 90 | Failed | 50% | 45 | Màn public/static hoặc chưa có đủ unhappy-path/transaction evidence để chốt Normal. |
| G05 | Guest | Register | 8 | 4 | 8 | L3 | 120 | Normal | 75% | 90 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| G06 | Guest | Access denied/error | 0 | 0 | 0 | L1 | 60 | Failed | 50% | 30 | Màn public/static hoặc chưa có đủ unhappy-path/transaction evidence để chốt Normal. |
| G07 | Guest | Static/help | 5 | 1 | 5 | L1 | 60 | Failed | 50% | 30 | Màn public/static hoặc chưa có đủ unhappy-path/transaction evidence để chốt Normal. |
| A01 | Auth | Login | 4 | 2 | 4 | L1 | 60 | Normal | 75% | 45 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| A02 | Auth | Register | 13 | 4 | 13 | L5 | 180 | Normal | 75% | 135 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| A03 | Auth | Logout | 6 | 3 | 6 | L2 | 90 | Normal | 75% | 68 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| A17 | Auth | Forgot password | 5 | 3 | 6 | L2 | 90 | Normal | 75% | 68 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| A18 | Auth | Reset password | 6 | 3 | 6 | L2 | 90 | Normal | 75% | 68 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| C01 | Customer | Dashboard | 6 | 3 | 6 | L2 | 90 | Normal | 75% | 68 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| C02 | Customer | Cart checkout | 15 | 8 | 16 | L7 | 240 | Best | 100% | 240 | Flow rủi ro cao có transaction/security test hoặc evidence rollback/idempotency/concurrency/audit. |
| C03 | Customer | Cart | 7 | 3 | 7 | L2 | 90 | Normal | 75% | 68 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| C04 | Customer | Checkout address | 10 | 5 | 10 | L4 | 150 | Normal | 75% | 113 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| C05 | Customer | Order list | 7 | 4 | 8 | L3 | 120 | Normal | 75% | 90 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| C06 | Customer | Order detail | 9 | 5 | 10 | L4 | 150 | Normal | 75% | 113 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| C07 | Customer | Payment | 9 | 3 | 9 | L3 | 120 | Best | 100% | 120 | Flow rủi ro cao có transaction/security test hoặc evidence rollback/idempotency/concurrency/audit. |
| C08 | Customer | Payment result | 12 | 5 | 12 | L5 | 180 | Best | 100% | 180 | Flow rủi ro cao có transaction/security test hoặc evidence rollback/idempotency/concurrency/audit. |
| C09 | Customer | Wishlist | 8 | 4 | 8 | L3 | 120 | Normal | 75% | 90 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| C10 | Customer | Product review | 13 | 6 | 13 | L5 | 180 | Normal | 75% | 135 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| C11 | Customer | Return request | 6 | 3 | 6 | L2 | 90 | Normal | 75% | 68 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| C12 | Customer | Return detail | 8 | 4 | 8 | L3 | 120 | Normal | 75% | 90 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| C13 | Customer | Notifications | 7 | 3 | 7 | L2 | 90 | Normal | 75% | 68 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| C14 | Customer | Address book | 8 | 5 | 10 | L4 | 150 | Normal | 75% | 113 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| C15 | Customer | Voucher | 5 | 4 | 8 | L3 | 120 | Normal | 75% | 90 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| C16 | Customer | Support/contact | 10 | 5 | 10 | L4 | 150 | Normal | 75% | 113 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| S01 | Shop | Shop dashboard | 6 | 3 | 6 | L2 | 90 | Normal | 75% | 68 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| S02 | Shop | Product list | 8 | 4 | 8 | L3 | 120 | Normal | 75% | 90 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| S03 | Shop | Product create | 11 | 5 | 11 | L4 | 150 | Normal | 75% | 113 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| S04 | Shop | Product detail/edit | 8 | 4 | 8 | L3 | 120 | Normal | 75% | 90 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| S05 | Shop | Inventory | 14 | 7 | 14 | L6 | 210 | Best | 100% | 210 | Flow rủi ro cao có transaction/security test hoặc evidence rollback/idempotency/concurrency/audit. |
| S06 | Shop | Order list | 10 | 5 | 10 | L4 | 150 | Normal | 75% | 113 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| S07 | Shop | Order processing | 10 | 6 | 12 | L5 | 180 | Normal | 75% | 135 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| S08 | Shop | Order detail | 12 | 6 | 12 | L5 | 180 | Normal | 75% | 135 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| S09 | Shop | Return list | 9 | 5 | 10 | L4 | 150 | Normal | 75% | 113 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| S10 | Shop | Return handling | 8 | 4 | 8 | L3 | 120 | Best | 100% | 120 | Flow rủi ro cao có transaction/security test hoặc evidence rollback/idempotency/concurrency/audit. |
| S11 | Shop | Review moderation | 9 | 5 | 10 | L4 | 150 | Best | 100% | 150 | Flow rủi ro cao có transaction/security test hoặc evidence rollback/idempotency/concurrency/audit. |
| S12 | Shop | Voucher management | 8 | 4 | 8 | L3 | 120 | Normal | 75% | 90 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| S13 | Shop | Settlement | 11 | 6 | 12 | L5 | 180 | Best | 100% | 180 | Flow rủi ro cao có transaction/security test hoặc evidence rollback/idempotency/concurrency/audit. |
| S14 | Shop | Shop settings | 14 | 7 | 14 | L6 | 210 | Normal | 75% | 158 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| D01 | Delivery | Delivery dashboard | 6 | 3 | 6 | L2 | 90 | Normal | 75% | 68 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| D02 | Delivery | Delivery list | 7 | 3 | 7 | L2 | 90 | Normal | 75% | 68 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| D03 | Delivery | Delivery detail/update | 10 | 5 | 10 | L4 | 150 | Best | 100% | 150 | Flow rủi ro cao có transaction/security test hoặc evidence rollback/idempotency/concurrency/audit. |
| A04 | Admin | Admin dashboard | 8 | 4 | 8 | L3 | 120 | Normal | 75% | 90 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| A05 | Admin | User list | 8 | 4 | 8 | L3 | 120 | Normal | 75% | 90 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| A06 | Admin | User detail/edit | 12 | 6 | 12 | L5 | 180 | Normal | 75% | 135 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| A07 | Admin | Shop approval | 10 | 5 | 10 | L4 | 150 | Best | 100% | 150 | Flow rủi ro cao có transaction/security test hoặc evidence rollback/idempotency/concurrency/audit. |
| A08 | Admin | Product moderation | 10 | 5 | 10 | L4 | 150 | Normal | 75% | 113 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| A09 | Admin | Category management | 8 | 4 | 8 | L3 | 120 | Normal | 75% | 90 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| A10 | Admin | Order management | 11 | 6 | 12 | L5 | 180 | Normal | 75% | 135 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| A11 | Admin | Order detail | 10 | 5 | 10 | L4 | 150 | Normal | 75% | 113 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| A12 | Admin | Transaction ledger | 11 | 6 | 12 | L5 | 180 | Normal | 75% | 135 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| A13 | Admin | Payment webhook/audit | 9 | 5 | 10 | L4 | 150 | Best | 100% | 150 | Flow rủi ro cao có transaction/security test hoặc evidence rollback/idempotency/concurrency/audit. |
| A14 | Admin | Return/refund | 8 | 4 | 8 | L3 | 120 | Normal | 75% | 90 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| A15 | Admin | Review moderation | 8 | 4 | 8 | L3 | 120 | Normal | 75% | 90 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| A16 | Admin | Voucher management | 9 | 5 | 10 | L4 | 150 | Normal | 75% | 113 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| A19 | Admin | Settlement management | 10 | 5 | 10 | L4 | 150 | Normal | 75% | 113 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| A20 | Admin | Audit logs | 12 | 6 | 12 | L5 | 180 | Normal | 75% | 135 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |
| A21 | Admin | System settings | 7 | 4 | 8 | L3 | 120 | Normal | 75% | 90 | Có render, input/route và xử lý dữ liệu; cần giữ evidence error/permission theo checklist. |

## 3. Tổng hợp báo cáo

| Chỉ số | Giá trị |
|---|---:|
| Tổng màn | 61 |
| Failed screens | 4 |
| Normal screens | 47 |
| Best screens | 10 |
| Failed LOC nếu toàn bộ dự án ở 50% | 4065 |
| Normal LOC nếu toàn bộ dự án ở 75% | 6112 |
| Best LOC nếu toàn bộ dự án ở 100% | 8130 |
| Evaluated LOC theo quality assessment hiện tại | 6454 |

## 4. Kết luận dùng để báo cáo

Số nên báo cáo là **Evaluated LOC theo quality assessment hiện tại**, vì đây là kết quả sau khi đọc code và đối chiếu evidence. Không lấy Best LOC toàn bộ nếu các màn chưa có đủ bằng chứng Best. Khi bổ sung test/evidence cho một màn, chỉ nâng rate của màn đó và chạy lại script để cập nhật tổng.

Evidence nền: `SCREEN_PROCESS_ANALYSIS.md`, `TRANSACTION_TEST_RUN_2026-07-20.md`, `TRANSACTION_EVIDENCE_MAP.md`, `JSP_FUNCTION_CATALOG.md`, ảnh/JSON trong `artifacts/`.
