-- Kịch bản SQL chèn dữ liệu mẫu đánh giá (Reviews Seed Data)
USE OnlineFruitShopping;
GO

BEGIN TRANSACTION;
BEGIN TRY
    -- 1. Xóa các đánh giá mẫu cũ nếu có
    DELETE FROM dbo.reviews WHERE order_item_id IN (1, 2) OR order_item_id >= 10;
    DELETE FROM dbo.order_items WHERE order_item_id >= 10;
    DELETE FROM dbo.orders WHERE order_id >= 10;
    DELETE FROM dbo.users WHERE user_id >= 10;

    -- 2. Thêm một số khách hàng giả lập mới để viết review
    SET IDENTITY_INSERT dbo.users ON;
    INSERT INTO dbo.users (user_id, full_name, email, password_hash, phone, role, status, user_address, is_email_verified, created_at, updated_at)
    VALUES
        (10, N'Nguyễn Văn Hùng', N'hungnv@gmail.com', N'hash', N'0912345601', N'CUSTOMER', N'ACTIVE', N'Hoàn Kiếm, Hà Nội', 1, GETDATE(), GETDATE()),
        (11, N'Phạm Minh Tuấn', N'tuanpm@gmail.com', N'hash', N'0912345602', N'CUSTOMER', N'ACTIVE', N'Cầu Giấy, Hà Nội', 1, GETDATE(), GETDATE()),
        (12, N'Trần Thị Mai', N'maitt@gmail.com', N'hash', N'0912345603', N'CUSTOMER', N'ACTIVE', N'Đống Đa, Hà Nội', 1, GETDATE(), GETDATE()),
        (13, N'Lê Hoàng Nam', N'namlh@gmail.com', N'hash', N'0912345604', N'CUSTOMER', N'ACTIVE', N'Thanh Xuân, Hà Nội', 1, GETDATE(), GETDATE()),
        (14, N'Đỗ Thùy Chi', N'chidt@gmail.com', N'hash', N'0912345605', N'CUSTOMER', N'ACTIVE', N'Hai Bà Trưng, Hà Nội', 1, GETDATE(), GETDATE()),
        (15, N'Vũ Quốc Anh', N'anhvq@gmail.com', N'hash', N'0912345606', N'CUSTOMER', N'ACTIVE', N'Tây Hồ, Hà Nội', 1, GETDATE(), GETDATE());
    SET IDENTITY_INSERT dbo.users OFF;

    -- 3. Thêm các đơn hàng giả lập đã hoàn thành cho các khách hàng này mua Cam Sành (product_id = 1, variant_id = 1)
    SET IDENTITY_INSERT dbo.orders ON;
    INSERT INTO dbo.orders (order_id, customer_id, owner_id, delivery_address, user_address, status, total_amount, delivery_fee, discount_amount, final_amount, payment_method, refund_status, created_at, updated_at)
    VALUES
        (10, 10, 3, N'12 Phố Cổ, Hà Nội', N'12 Phố Cổ, Hà Nội', N'DELIVERED', 35000.00, 15000.00, 0.00, 50000.00, N'COD', N'NONE', '2026-05-18T08:00:00', '2026-05-18T14:00:00'),
        (11, 11, 3, N'85 Xuân Thủy, Cầu Giấy', N'85 Xuân Thủy, Cầu Giấy', N'DELIVERED', 35000.00, 15000.00, 0.00, 50000.00, N'COD', N'NONE', '2026-05-19T09:00:00', '2026-05-19T15:00:00'),
        (12, 12, 3, N'45 Chùa Bộc, Đống Đa', N'45 Chùa Bộc, Đống Đa', N'DELIVERED', 95000.00, 20000.00, 0.00, 115000.00, N'CK', N'NONE', '2026-05-20T10:00:00', '2026-05-20T16:00:00'),
        (13, 13, 3, N'102 Nguyễn Trãi, Thanh Xuân', N'102 Nguyễn Trãi, Thanh Xuân', N'DELIVERED', 35000.00, 15000.00, 0.00, 50000.00, N'COD', N'NONE', '2026-05-21T11:00:00', '2026-05-21T17:00:00'),
        (14, 14, 3, N'56 Bạch Mai, Hai Bà Trưng', N'56 Bạch Mai, Hai Bà Trưng', N'DELIVERED', 35000.00, 15000.00, 0.00, 50000.00, N'CK', N'NONE', '2026-05-22T13:00:00', '2026-05-22T19:00:00'),
        (15, 15, 3, N'29 Lạc Long Quân, Tây Hồ', N'29 Lạc Long Quân, Tây Hồ', N'DELIVERED', 95000.00, 20000.00, 0.00, 115000.00, N'COD', N'NONE', '2026-05-23T08:00:00', '2026-05-23T12:00:00');
    SET IDENTITY_INSERT dbo.orders OFF;

    -- 4. Thêm các dòng order items tương ứng
    SET IDENTITY_INSERT dbo.order_items ON;
    INSERT INTO dbo.order_items (order_item_id, order_id, variant_id, product_name_snapshot, variant_label_snapshot, quantity, unit_price, subtotal)
    VALUES
        (10, 10, 1, N'Cam Sành Cao Phong Hòa Bình', N'Hộp 1kg', 1, 35000.00, 35000.00),
        (11, 11, 1, N'Cam Sành Cao Phong Hòa Bình', N'Hộp 1kg', 1, 35000.00, 35000.00),
        (12, 12, 2, N'Cam Sành Cao Phong Hòa Bình', N'Combo 3kg', 1, 95000.00, 95000.00),
        (13, 13, 1, N'Cam Sành Cao Phong Hòa Bình', N'Hộp 1kg', 1, 35000.00, 35000.00),
        (14, 14, 1, N'Cam Sành Cao Phong Hòa Bình', N'Hộp 1kg', 1, 35000.00, 35000.00),
        (15, 15, 2, N'Cam Sành Cao Phong Hòa Bình', N'Combo 3kg', 1, 95000.00, 95000.00);
    SET IDENTITY_INSERT dbo.order_items OFF;

    -- 5. Thêm các đánh giá đa dạng số sao và có kèm ảnh
    SET IDENTITY_INSERT dbo.reviews ON;
    INSERT INTO dbo.reviews (review_id, order_item_id, customer_id, rating, review_text, is_hidden, created_at, review_image_url)
    VALUES
        -- Đã có review_id = 1 từ khách hàng 5 cho order_item = 1.
        (1, 1, 5, 5, N'Cam Cao Phong cực kỳ nhiều nước, vị ngọt thanh tự nhiên xen chua nhẹ ăn cực đã. Giao hàng nhanh và đóng gói chuyên nghiệp!', 0, '2026-05-15T12:00:00', N'https://images.unsplash.com/photo-1611080626919-7cf5a9dbab5b?w=600&auto=format&fit=crop&q=80'),
        
        -- Các review mới
        (10, 10, 10, 5, N'Quả cam tươi rói, vỏ mỏng, nước nhiều. Vắt nước uống cho các bé ở nhà rất thích, sẽ tiếp tục ủng hộ shop lâu dài.', 0, '2026-05-18T15:00:00', N'https://images.unsplash.com/photo-1547514701-42782101795e?w=600&auto=format&fit=crop&q=80'),
        (11, 11, 11, 4, N'Cam ngon, thơm ngọt thanh. Giao hàng trong vòng 2 tiếng rất đúng hẹn. Tuy nhiên có vài quả hơi nhỏ hơn so với mô tả một chút.', 0, '2026-05-19T16:30:00', NULL),
        (12, 12, 12, 5, N'Combo 3kg rẻ hơn nhiều so với mua lẻ. Trái cây tươi sạch sẽ, vỏ xanh bóng bẩy cực bắt mắt. Khuyên mọi người nên mua nha!', 0, '2026-05-20T17:45:00', N'https://images.unsplash.com/photo-1618897996318-5a901fa6ca71?w=600&auto=format&fit=crop&q=80'),
        (13, 13, 13, 3, N'Cam ăn cũng tạm được, nước vừa phải chứ không nhiều lắm. Giao hàng trễ mất 30 phút nên trừ 2 sao.', 0, '2026-05-21T18:20:00', NULL),
        (14, 14, 14, 2, N'Quả cam nhận được bị dập 2 quả ở dưới đáy hộp do khâu vận chuyển xếp đè lên. Vị ngọt bình thường không đặc sắc.', 0, '2026-05-22T20:10:00', N'https://images.unsplash.com/photo-1590080875515-8a3a8dc5735e?w=600&auto=format&fit=crop&q=80'),
        (15, 15, 15, 1, N'Giao hàng quá chậm, cam thì bị héo vỏ và rụng cuống hết cả chùm. Quá thất vọng về trải nghiệm mua hàng lần này.', 0, '2026-05-23T10:15:00', NULL);
    SET IDENTITY_INSERT dbo.reviews OFF;

    COMMIT TRANSACTION;
    PRINT 'Seed data for reviews added successfully!';
END TRY
BEGIN CATCH
    ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO
