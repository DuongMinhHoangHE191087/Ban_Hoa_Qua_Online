-- Create the database (Optional: uncomment to execute)
-- CREATE DATABASE OnlineFruitShopping;
-- GO
-- USE OnlineFruitShopping;
-- GO

-- 1. users [cite: 36]
CREATE TABLE users (
    user_id INT IDENTITY(1,1) PRIMARY KEY,
    full_name NVARCHAR(100) NOT NULL,
    email NVARCHAR(255) NOT NULL UNIQUE,
    password_hash NVARCHAR(255) NULL,
    phone NVARCHAR(15) NULL UNIQUE,
    role NVARCHAR(20) NOT NULL DEFAULT 'CUSTOMER' CHECK (role IN ('CUSTOMER','SHOP_OWNER','DELIVERY','ADMIN')),
    status NVARCHAR(20) NOT NULL DEFAULT 'INACTIVE' CHECK (status IN ('ACTIVE','INACTIVE','LOCKED','SUSPENDED')),
    user_address NVARCHAR(500) NULL,

    is_email_verified BIT NOT NULL DEFAULT 0,
    email_verification_code_hash NVARCHAR(255) NULL,
    email_verification_expires_at DATETIME NULL,
    email_verification_resend_at DATETIME NULL,
    email_verification_sent_at DATETIME NULL,
    failed_login_count INT NOT NULL DEFAULT 0,
    locked_until DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT GETDATE(), -- [cite: 29]
    updated_at DATETIME NOT NULL DEFAULT GETDATE()  -- [cite: 29]
);

-- 2. user_sessions [cite: 40]
CREATE TABLE user_sessions (
    session_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL FOREIGN KEY REFERENCES users(user_id) ON DELETE CASCADE,
    token NVARCHAR(100) NOT NULL UNIQUE,
    expires_at DATETIME NOT NULL
);



-- 4. shop_owner_profiles [cite: 47]
CREATE TABLE shop_owner_profiles (
    profile_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL UNIQUE FOREIGN KEY REFERENCES users(user_id),
    shop_name NVARCHAR(150) NOT NULL,
    shop_description NVARCHAR(MAX) NULL,
    approval_status NVARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (approval_status IN ('PENDING','APPROVED','REJECTED','SUSPENDED')),
    rejection_reason NVARCHAR(500) NULL,
    approved_at DATETIME NULL,
    delivery_address NVARCHAR(500) NULL,
    rating DECIMAL(3,2) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT GETDATE(), -- [cite: 29]
    updated_at DATETIME NOT NULL DEFAULT GETDATE()  -- [cite: 29]
);

-- 5. categories [cite: 50]
CREATE TABLE categories (
    category_id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL UNIQUE,
    slug NVARCHAR(100) NOT NULL UNIQUE,
    display_order INT NOT NULL DEFAULT 0,
    is_active BIT NOT NULL DEFAULT 1

);

-- 6. products [cite: 54]
CREATE TABLE products (
    product_id INT IDENTITY(1,1) PRIMARY KEY,
    owner_id INT NOT NULL FOREIGN KEY REFERENCES users(user_id),
    category_id INT NOT NULL FOREIGN KEY REFERENCES categories(category_id),
    name NVARCHAR(200) NOT NULL,
    description NVARCHAR(MAX) NULL,
    origin_country NVARCHAR(100) NULL,
    origin_region NVARCHAR(150) NULL,
    harvest_date DATE NULL,
    shelf_life_days INT NULL,
    storage_instruction NVARCHAR(300) NULL,
    status NVARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE')),
    view_count INT NOT NULL DEFAULT 0,
    rating DECIMAL(3,2) NOT NULL DEFAULT 0,
    sold_quantity INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT GETDATE(), -- [cite: 29]
    updated_at DATETIME NOT NULL DEFAULT GETDATE()  -- [cite: 29]
);

-- 7. product_images [cite: 57]
CREATE TABLE product_images (
    image_id INT IDENTITY(1,1) PRIMARY KEY,
    product_id INT NOT NULL FOREIGN KEY REFERENCES products(product_id) ON DELETE CASCADE,
    file_path NVARCHAR(500) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    is_primary BIT NOT NULL DEFAULT 0,
    uploaded_at DATETIME NOT NULL DEFAULT GETDATE()
);

-- 8. product_variants [cite: 61]
CREATE TABLE product_variants (
    variant_id INT IDENTITY(1,1) PRIMARY KEY,
    product_id INT NOT NULL FOREIGN KEY REFERENCES products(product_id) ON DELETE CASCADE,
    sku NVARCHAR(50) NOT NULL UNIQUE,
    variant_label NVARCHAR(100) NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
   
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT GETDATE(), -- [cite: 29]
    updated_at DATETIME NOT NULL DEFAULT GETDATE()  -- [cite: 29]
);

-- 9. inventory_logs [cite: 65]
CREATE TABLE inventory_logs (
    log_id INT IDENTITY(1,1) PRIMARY KEY,
    variant_id INT NOT NULL FOREIGN KEY REFERENCES product_variants(variant_id),
    changed_by INT NOT NULL FOREIGN KEY REFERENCES users(user_id),
    change_type NVARCHAR(20) NOT NULL CHECK (change_type IN ('MANUAL_ADJUST','ORDER_RESERVE','ORDER_RELEASE','ORDER_CONFIRM','RETURN')),
    quantity_delta INT NOT NULL,
    quantity_after INT NOT NULL,
    note NVARCHAR(300) NULL,
    changed_at DATETIME NOT NULL DEFAULT GETDATE()
);

-- 10. promotions [cite: 93]
CREATE TABLE promotions (
    promo_id INT IDENTITY(1,1) PRIMARY KEY,
    code NVARCHAR(50) NOT NULL UNIQUE,
    discount_type NVARCHAR(10) NOT NULL CHECK (discount_type IN ('PERCENT','FIXED')),
    discount_scope NVARCHAR(50) NOT NULL CHECK (discount_scope IN ('SHOP','ALL')),
    discount_max DECIMAL(10,2) NOT NULL DEFAULT 0,
    discount_value DECIMAL(10,2) NOT NULL,
    min_order_value DECIMAL(14,2) NOT NULL DEFAULT 0,
    scope NVARCHAR(15) NOT NULL CHECK (scope IN ('ORDER','PRODUCT')),
    product_id INT NULL FOREIGN KEY REFERENCES products(product_id),
    max_uses INT NULL,
    used_count INT NOT NULL DEFAULT 0,
    can_stack BIT NOT NULL DEFAULT 0,
    valid_from DATETIME NOT NULL,
    valid_until DATETIME NOT NULL,
    created_by INT NOT NULL FOREIGN KEY REFERENCES users(user_id),
    created_at DATETIME NOT NULL DEFAULT GETDATE(), -- [cite: 29]
    updated_at DATETIME NOT NULL DEFAULT GETDATE(),  -- [cite: 29]
    is_deleted BIT NOT NULL DEFAULT 0,
    is_active BIT NOT NULL DEFAULT 1
);

-- 11. cart [cite: 69]
CREATE TABLE cart (
    cart_id INT IDENTITY(1,1) PRIMARY KEY,
    customer_id INT NOT NULL UNIQUE FOREIGN KEY REFERENCES users(user_id),
    created_at DATETIME NOT NULL DEFAULT GETDATE(), -- [cite: 29]
    updated_at DATETIME NOT NULL DEFAULT GETDATE()  -- [cite: 29]
);

-- 12. cart_items [cite: 69]
CREATE TABLE cart_items (
    cart_item_id INT IDENTITY(1,1) PRIMARY KEY,
    cart_id INT NOT NULL FOREIGN KEY REFERENCES cart(cart_id) ON DELETE CASCADE,
    variant_id INT NOT NULL FOREIGN KEY REFERENCES product_variants(variant_id),
    quantity INT NOT NULL CHECK (quantity >= 1),
    added_at DATETIME NOT NULL DEFAULT GETDATE()
);

-- 13. orders [cite: 75]
CREATE TABLE orders (
    order_id INT IDENTITY(1,1) PRIMARY KEY,
    customer_id INT NOT NULL FOREIGN KEY REFERENCES users(user_id),
    owner_id INT NOT NULL FOREIGN KEY REFERENCES users(user_id),
    delivery_address NVARCHAR(500) NOT NULL,
    user_address NVARCHAR(500) NOT NULL,
    delivery_time_slot NVARCHAR(100) NULL,
    notes NVARCHAR(300) NULL,
    cancelled_at DATETIME NULL,
    cancelled_by INT NULL FOREIGN KEY REFERENCES users(user_id),
    cancellation_reason NVARCHAR(500) NULL,
    status NVARCHAR(25) NOT NULL DEFAULT 'PENDING_PAYMENT' CHECK (status IN 
    ('PENDING_PAYMENT','CONFIRMED','PREPARING','DISPATCHED','DELIVERED','CANCELLED','PAYMENT_FAILED','EXPIRED')),
    total_amount DECIMAL(14,2) NOT NULL,
    delivery_fee DECIMAL(10,2) NOT NULL DEFAULT 0,
    
    discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    system_discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    shop_discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    platform_fee DECIMAL(12,2) NOT NULL DEFAULT 0,
    final_amount DECIMAL(14,2) NOT NULL,
    payment_method NVARCHAR(20) NOT NULL CHECK (payment_method IN ('CK','COD')),
    refund_status NVARCHAR(20) NOT NULL DEFAULT 'NONE' CHECK (refund_status IN ('NONE','PENDING','APPROVED','REJECTED','PROCESSING','REFUNDED','FAILED')),
    created_at DATETIME NOT NULL DEFAULT GETDATE(), -- [cite: 29]
    updated_at DATETIME NOT NULL DEFAULT GETDATE()  -- [cite: 29]
);

-- 14. order_items [cite: 79]
CREATE TABLE order_items (
    order_item_id INT IDENTITY(1,1) PRIMARY KEY,
    order_id INT NOT NULL FOREIGN KEY REFERENCES orders(order_id) ON DELETE CASCADE,
    variant_id INT NULL FOREIGN KEY REFERENCES product_variants(variant_id) ON DELETE SET NULL,
    product_name_snapshot NVARCHAR(200) NOT NULL,
    variant_label_snapshot NVARCHAR(100) NOT NULL,
    quantity INT NOT NULL CHECK (quantity >= 1),
    unit_price DECIMAL(12,2) NOT NULL,
    subtotal DECIMAL(14,2) NOT NULL
);

CREATE TABLE order_promotions (
    usage_id INT IDENTITY(1,1) PRIMARY KEY,
    order_id INT NOT NULL FOREIGN KEY REFERENCES orders(order_id) ON DELETE CASCADE,
    promo_id INT NOT NULL FOREIGN KEY REFERENCES promotions(promo_id),
    customer_id INT NOT NULL FOREIGN KEY REFERENCES users(user_id), -- Thêm để dễ check giới hạn per user
    discount_applied DECIMAL(12,2) NOT NULL, -- Lưu số tiền giảm thực tế từ mã này
    used_at DATETIME NOT NULL DEFAULT GETDATE()
);

CREATE TABLE return_requests (
    return_request_id INT IDENTITY(1,1) PRIMARY KEY,
    order_id INT NOT NULL FOREIGN KEY REFERENCES orders(order_id) ON DELETE CASCADE,
    order_item_id INT NULL FOREIGN KEY (order_item_id) REFERENCES dbo.order_items(order_item_id),
    customer_id INT NOT NULL FOREIGN KEY REFERENCES users(user_id),
    request_type NVARCHAR(20) NOT NULL CHECK (request_type IN ('CANCEL','RETURN','EXCHANGE')),
    reason_code NVARCHAR(50) NOT NULL CHECK (reason_code IN ('WRONG_ITEM','DAMAGED','MISSING_ITEM','LATE_DELIVERY','NOT_AS_DESCRIBED','OTHER')),
    description NVARCHAR(1000) NULL,
    evidence_url NVARCHAR(500) NULL,
    requested_quantity INT NOT NULL DEFAULT 1 CHECK (requested_quantity >= 1),
    resolution_type NVARCHAR(20) NULL CHECK (resolution_type IN ('REFUND','REPLACE','DISCOUNT','REJECT')),
    replacement_variant_id INT NULL FOREIGN KEY REFERENCES product_variants(variant_id),
    refund_amount DECIMAL(14,2) NOT NULL DEFAULT 0,
    status NVARCHAR(20) NOT NULL DEFAULT 'REQUESTED' CHECK (status IN ('REQUESTED','APPROVED','REJECTED','PROCESSING','COMPLETED','CANCELLED')),
    decided_by INT NULL FOREIGN KEY REFERENCES users(user_id),
    decision_reason NVARCHAR(500) NULL,
    resolved_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME NOT NULL DEFAULT GETDATE()
     
);

CREATE TABLE shop_settlements (
    settlement_id INT IDENTITY(1,1) PRIMARY KEY,
    owner_id INT NOT NULL FOREIGN KEY REFERENCES users(user_id),
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    gross_amount DECIMAL(14,2) NOT NULL DEFAULT 0,
    platform_fee_amount DECIMAL(14,2) NOT NULL DEFAULT 0,
    refund_amount DECIMAL(14,2) NOT NULL DEFAULT 0,
    adjustment_amount DECIMAL(14,2) NOT NULL DEFAULT 0,
    net_amount DECIMAL(14,2) NOT NULL DEFAULT 0,
    status NVARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','CONFIRMED','PAID','CANCELLED')),
    calculated_at DATETIME NOT NULL DEFAULT GETDATE(),
    confirmed_at DATETIME NULL,
    paid_at DATETIME NULL,
    created_by INT NOT NULL FOREIGN KEY REFERENCES users(user_id),
    note NVARCHAR(500) NULL
);

CREATE TABLE shop_settlement_orders (
    settlement_order_id INT IDENTITY(1,1) PRIMARY KEY,
    settlement_id INT NOT NULL FOREIGN KEY REFERENCES shop_settlements(settlement_id) ON DELETE CASCADE,
    order_id INT NOT NULL UNIQUE FOREIGN KEY REFERENCES orders(order_id),
    order_amount DECIMAL(14,2) NOT NULL,
    platform_fee_amount DECIMAL(14,2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(14,2) NOT NULL DEFAULT 0,
    refund_amount DECIMAL(14,2) NOT NULL DEFAULT 0,
    net_amount DECIMAL(14,2) NOT NULL
);

-- 15. payments [cite: 83]
CREATE TABLE payment_transactions (
    transaction_id INT IDENTITY(1,1) PRIMARY KEY,
    order_id INT NOT NULL UNIQUE FOREIGN KEY REFERENCES orders(order_id), -- Đảm bảo 1 đơn hàng 1 giao dịch active
    payment_method NVARCHAR(30) NOT NULL DEFAULT 'SEPAY', -- Lưu 'SEPAY' hoặc 'COD'
    sepay_transaction_id NVARCHAR(100) NULL, -- Mã GD thực tế trên ngân hàng do SePay trả về
    sepay_reference NVARCHAR(100) NULL,      -- Nội dung chuyển khoản (VD: DH101)
    sepay_qr_code NVARCHAR(500) NULL,        -- Link ảnh QR Code để hiển thị lên UI
    amount DECIMAL(14,2) NOT NULL,           -- Khớp với orders.final_amount
    currency NVARCHAR(3) NOT NULL DEFAULT 'VND',
    status NVARCHAR(20) NOT NULL DEFAULT 'pending' CHECK (status IN (
        'pending', 'processing', 'completed', 'failed', 'cancelled', 'refunded', 'expired'
    )),
    initiated_at DATETIME NOT NULL DEFAULT GETDATE(),
    completed_at DATETIME NULL,
    expires_at DATETIME NULL,                -- Thời hạn quét mã (VD: sau 15 phút)
    provider_response NVARCHAR(MAX) NULL,    -- Lưu log cục JSON webhook của SePay để đối soát
    error_code NVARCHAR(50) NULL,
    error_message NVARCHAR(500) NULL,
    ip_address NVARCHAR(45) NULL
);

CREATE TABLE sepay_webhook_dedup (
    dedup_id INT IDENTITY(1,1) PRIMARY KEY,
    sepay_transaction_id NVARCHAR(100) NOT NULL UNIQUE, -- Đảm bảo mã GD ngân hàng này chỉ được xử lý 1 lần
    order_code NVARCHAR(100) NOT NULL,                  -- Nội dung CK (VD: DH101)
    process_result NVARCHAR(30) NOT NULL DEFAULT 'processed',
    created_at DATETIME NOT NULL DEFAULT GETDATE()
);

-- 16. deliveries [cite: 86]
CREATE TABLE deliveries (
    delivery_id INT IDENTITY(1,1) PRIMARY KEY,
    order_id INT NOT NULL UNIQUE FOREIGN KEY REFERENCES orders(order_id),
    staff_id INT NULL FOREIGN KEY REFERENCES users(user_id),
    status NVARCHAR(20) NOT NULL DEFAULT 'ASSIGNED' CHECK (status IN ('ASSIGNED','PICKED_UP','IN_TRANSIT','DELIVERED','FAILED')),
    picked_up_at DATETIME NULL,
    delivered_at DATETIME NULL,
    failure_reason NVARCHAR(300) NULL,
    created_at DATETIME NOT NULL DEFAULT GETDATE(), -- [cite: 29]
    updated_at DATETIME NOT NULL DEFAULT GETDATE()  -- [cite: 29]
);

-- 17. reviews [cite: 89]
CREATE TABLE reviews (
    review_id INT IDENTITY(1,1) PRIMARY KEY,
    order_item_id INT NOT NULL FOREIGN KEY REFERENCES order_items(order_item_id),
    customer_id INT NOT NULL FOREIGN KEY REFERENCES users(user_id),
    rating TINYINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    review_text NVARCHAR(1000) NULL,
    review_image_url NVARCHAR(500) NULL,
    is_hidden BIT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT GETDATE(), -- [cite: 29]
    CONSTRAINT UQ_review_customer_item UNIQUE (customer_id, order_item_id)
);

CREATE TABLE chat_sessions (
    session_id INT IDENTITY(1,1) PRIMARY KEY,
    customer_id INT NOT NULL FOREIGN KEY REFERENCES users(user_id),
    owner_id INT NOT NULL FOREIGN KEY REFERENCES users(user_id),
    
    -- Trạng thái để biết khung chat đang mở hay đã bị khóa/đóng
    status NVARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','CLOSED')),
    
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME NOT NULL DEFAULT GETDATE(), -- Dùng để sort các phiên chat mới nhất lên đầu
    closed_at DATETIME NULL
);

CREATE TABLE chat_messages (
    message_id INT IDENTITY(1,1) PRIMARY KEY,
    session_id INT NOT NULL FOREIGN KEY REFERENCES chat_sessions(session_id) ON DELETE CASCADE,
    sender_id INT NOT NULL FOREIGN KEY REFERENCES users(user_id), -- Ai là người gửi (Customer hay Owner)
    
    content NVARCHAR(MAX) NOT NULL, -- Dùng MAX để hỗ trợ tin nhắn dài
    is_read BIT NOT NULL DEFAULT 0, -- Cờ đánh dấu đã đọc chưa (rất quan trọng cho UI)
    
    created_at DATETIME NOT NULL DEFAULT GETDATE()
);

CREATE TABLE notifications (
    notification_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL FOREIGN KEY REFERENCES users(user_id) ON DELETE CASCADE,
    
    -- Phân loại thông báo để Frontend có thể hiển thị icon tương ứng (Ví dụ: 🔔, 📦, 💰)
    type NVARCHAR(50) NOT NULL CHECK (type IN ('ORDER_UPDATE', 'PROMOTION', 'SYSTEM', 'INVENTORY_ALERT', 'PAYMENT')),
    
    title NVARCHAR(200) NOT NULL,
    message NVARCHAR(MAX) NOT NULL, -- Dùng MAX để linh hoạt độ dài nội dung
    
    -- Đổi 'link' thành 'action_url' cho rõ nghĩa. Chứa đường dẫn khi click vào thông báo.
    action_url NVARCHAR(300) NULL, 
    
    is_read BIT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT GETDATE()
);

-- Optional: Create Full-Text Search configuration [cite: 19]
-- CREATE FULLTEXT CATALOG ftCatalog AS DEFAULT;
-- CREATE UNIQUE INDEX UX_products_product_id ON products(product_id);
-- CREATE FULLTEXT INDEX ON products(name, description) KEY INDEX UX_products_product_id;