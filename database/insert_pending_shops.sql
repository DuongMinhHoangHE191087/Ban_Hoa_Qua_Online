USE [OnlineFruitShopping];
GO

INSERT INTO dbo.users (email, password_hash, full_name, phone, role, status)
VALUES ('pending_shop1@fruitshop.local', '$2a$10$wTfA2q1E1Hk08vU9g2aQ1OWf8gXz6H1A2pZ9vT3yXyT1A2bC3D4E5', 'Nguyễn Văn Đăng Ký', '0911223344', 'CUSTOMER', 'ACTIVE');
DECLARE @UserId1 INT = SCOPE_IDENTITY();

INSERT INTO dbo.shop_owner_profiles (user_id, shop_name, shop_description, approval_status, delivery_address, doc_paths)
VALUES (@UserId1, 'Trái Cây Sạch Hữu Cơ', 'Cung cấp trái cây chuẩn VietGAP', 'PENDING', '123 Đường VietGAP, Quận 1', '["uploads/docs/doc1.pdf"]');


INSERT INTO dbo.users (email, password_hash, full_name, phone, role, status)
VALUES ('pending_shop2@fruitshop.local', '$2a$10$wTfA2q1E1Hk08vU9g2aQ1OWf8gXz6H1A2pZ9vT3yXyT1A2bC3D4E5', 'Trần Thị Chờ Duyệt', '0988776655', 'CUSTOMER', 'ACTIVE');
DECLARE @UserId2 INT = SCOPE_IDENTITY();

INSERT INTO dbo.shop_owner_profiles (user_id, shop_name, shop_description, approval_status, delivery_address, doc_paths)
VALUES (@UserId2, 'Đặc Sản Trái Cây Vùng Miền', 'Tất cả đặc sản trái cây 3 miền', 'PENDING', '456 Vùng Miền, Quận 3', '["uploads/docs/doc2.pdf"]');
GO
