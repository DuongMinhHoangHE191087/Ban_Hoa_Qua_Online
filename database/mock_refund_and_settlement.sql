USE OnlineFruitShopping;
GO
SET QUOTED_IDENTIFIER ON;
SET ANSI_NULLS ON;
GO

-- 1. Thêm data Khiếu nại đổi trả đa dạng
INSERT INTO return_requests (order_id, order_item_id, customer_id, request_type, reason_code, description, requested_quantity, refund_amount, status)
VALUES (1, 1, 5, 'RETURN', 'WRONG_ITEM', N'Giao nhầm Táo Mỹ thay vì Táo Úc', 1, 35000.00, 'REQUESTED');

INSERT INTO return_requests (order_id, order_item_id, customer_id, request_type, reason_code, description, requested_quantity, refund_amount, status)
VALUES (2, 3, 6, 'EXCHANGE', 'DAMAGED', N'Dưa hấu bị dập nát do vận chuyển', 1, 89000.00, 'REQUESTED');

INSERT INTO return_requests (order_id, order_item_id, customer_id, request_type, reason_code, description, requested_quantity, refund_amount, status)
VALUES (3, 5, 5, 'RETURN', 'MISSING_ITEM', N'Thiếu 1 hộp Nho đen', 1, 86000.00, 'REQUESTED');

INSERT INTO return_requests (order_id, order_item_id, customer_id, request_type, reason_code, description, requested_quantity, refund_amount, status)
VALUES (10, NULL, 10, 'CANCEL', 'LATE_DELIVERY', N'Giao hàng trễ quá 3 ngày', 1, 150000.00, 'REQUESTED');

INSERT INTO return_requests (order_id, order_item_id, customer_id, request_type, reason_code, description, requested_quantity, refund_amount, status)
VALUES (11, NULL, 11, 'RETURN', 'NOT_AS_DESCRIBED', N'Sầu riêng bị sượng, không giống mô tả trên web', 1, 300000.00, 'REQUESTED');

-- 2. Thêm đơn hàng mới cho Shop #3 (owner_id = 3) đã giao thành công
INSERT INTO orders (customer_id, owner_id, order_type, delivery_address, status, total_amount, delivery_fee, final_amount, payment_method)
VALUES (5, 3, 'CHILD', N'123 Đường ABC', 'DELIVERED', 500000.00, 20000.00, 520000.00, 'COD');
DECLARE @NewOrder1 INT = SCOPE_IDENTITY();

INSERT INTO order_items (order_id, product_name_snapshot, variant_label_snapshot, quantity, unit_price, subtotal)
VALUES (@NewOrder1, N'Sầu riêng Ri6', N'Loại 1', 2, 250000.00, 500000.00);

INSERT INTO payment_transactions (order_id, payment_method, amount, currency, status, completed_at)
VALUES (@NewOrder1, 'COD', 520000.00, 'VND', 'completed', GETDATE());

-- 3. Thêm đơn hàng mới cho Shop #4 (owner_id = 4) đã giao thành công
INSERT INTO orders (customer_id, owner_id, order_type, delivery_address, status, total_amount, delivery_fee, final_amount, payment_method)
VALUES (6, 4, 'CHILD', N'456 Đường XYZ', 'DELIVERED', 800000.00, 30000.00, 830000.00, 'CK');
DECLARE @NewOrder2 INT = SCOPE_IDENTITY();

INSERT INTO order_items (order_id, product_name_snapshot, variant_label_snapshot, quantity, unit_price, subtotal)
VALUES (@NewOrder2, N'Cherry Úc', N'Thùng 2kg', 1, 800000.00, 800000.00);

INSERT INTO payment_transactions (order_id, payment_method, amount, currency, status, completed_at)
VALUES (@NewOrder2, 'SEPAY', 830000.00, 'VND', 'completed', GETDATE());

-- 4. Th�m don h�ng m?u (thanh to�n CK b? CANCELLED) d? test lu?ng d?i so�t kh�ng t�nh doanh thu
INSERT INTO orders (customer_id, owner_id, delivery_address, total_amount, final_amount, payment_method, status, created_at, updated_at)
VALUES (6, 3, N'456 Test Street', 200000.00, 200000.00, 'CK', 'CANCELLED', DATEADD(hour, -25, GETDATE()), DATEADD(hour, -18, GETDATE()));

