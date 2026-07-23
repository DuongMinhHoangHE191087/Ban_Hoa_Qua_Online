USE OnlineFruitShopping;
GO

SET QUOTED_IDENTIFIER ON;
GO

-- Insert users
INSERT INTO users (full_name, email, password_hash, role, status)
VALUES 
('Nông trại Dâu Tây Đà Lạt', 'dautaydalat@example.com', 'dummyhash', 'CUSTOMER', 'ACTIVE'),
('Vựa Bưởi Da Xanh Bến Tre', 'buoibentre@example.com', 'dummyhash', 'CUSTOMER', 'ACTIVE'),
('Sầu Riêng Chín Hóa', 'sauriengchinhoa@example.com', 'dummyhash', 'CUSTOMER', 'ACTIVE');
GO

-- Insert shop profiles for those users
INSERT INTO shop_owner_profiles (user_id, shop_name, shop_description, approval_status, doc_paths, business_email, logo_url)
SELECT user_id, full_name, N'Cửa hàng chuyên cung cấp trái cây tươi ngon, đạt chuẩn VietGAP', 'PENDING', 'docs/giay_phep_kinh_doanh.pdf', email, 'logo.png'
FROM users
WHERE email IN ('dautaydalat@example.com', 'buoibentre@example.com', 'sauriengchinhoa@example.com');
GO
