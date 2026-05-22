SET NOCOUNT ON;
GO

USE [master];
GO

IF DB_ID(N'OnlineFruitShopping') IS NOT NULL
BEGIN
    ALTER DATABASE [OnlineFruitShopping] SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE [OnlineFruitShopping];
END
GO

CREATE DATABASE [OnlineFruitShopping];
GO

USE [OnlineFruitShopping];
GO

SET NOCOUNT ON;
GO

-- =========================================================
-- Schema creation
-- =========================================================

IF OBJECT_ID(N'dbo.users', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.users (
        user_id INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_users PRIMARY KEY,
        full_name NVARCHAR(100) NOT NULL,
        email NVARCHAR(255) NOT NULL CONSTRAINT UQ_users_email UNIQUE,
        password_hash NVARCHAR(255) NULL,
        phone NVARCHAR(15) NULL,
        role NVARCHAR(20) NOT NULL CONSTRAINT CK_users_role DEFAULT 'CUSTOMER' CHECK (role IN ('CUSTOMER', 'SHOP_OWNER', 'DELIVERY', 'ADMIN')),
        status NVARCHAR(20) NOT NULL CONSTRAINT CK_users_status DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
        user_address NVARCHAR(500) NULL,
        is_email_verified BIT NOT NULL CONSTRAINT DF_users_is_email_verified DEFAULT 0,
        failed_login_count INT NOT NULL CONSTRAINT DF_users_failed_login_count DEFAULT 0,
        locked_until DATETIME NULL,
        created_at DATETIME NOT NULL CONSTRAINT DF_users_created_at DEFAULT GETDATE(),
        updated_at DATETIME NOT NULL CONSTRAINT DF_users_updated_at DEFAULT GETDATE()
    );
END
GO

IF OBJECT_ID(N'dbo.user_sessions', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.user_sessions (
        session_id INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_user_sessions PRIMARY KEY,
        user_id INT NOT NULL,
        token NVARCHAR(100) NOT NULL CONSTRAINT UQ_user_sessions_token UNIQUE,
        expires_at DATETIME NOT NULL,
        CONSTRAINT FK_user_sessions_users FOREIGN KEY (user_id) REFERENCES dbo.users(user_id) ON DELETE CASCADE
    );
END
GO

IF OBJECT_ID(N'dbo.shop_owner_profiles', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.shop_owner_profiles (
        profile_id INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_shop_owner_profiles PRIMARY KEY,
        user_id INT NOT NULL CONSTRAINT UQ_shop_owner_profiles_user_id UNIQUE,
        shop_name NVARCHAR(150) NOT NULL,
        shop_description NVARCHAR(MAX) NULL,
        approval_status NVARCHAR(20) NOT NULL CONSTRAINT DF_shop_owner_profiles_approval_status DEFAULT 'PENDING' CONSTRAINT CK_shop_owner_profiles_approval_status CHECK (approval_status IN ('PENDING', 'APPROVED', 'REJECTED', 'SUSPENDED')),
        rejection_reason NVARCHAR(500) NULL,
        approved_at DATETIME NULL,
        delivery_address NVARCHAR(500) NULL,
        rating DECIMAL(3,2) NOT NULL CONSTRAINT DF_shop_owner_profiles_rating DEFAULT 0,
        created_at DATETIME NOT NULL CONSTRAINT DF_shop_owner_profiles_created_at DEFAULT GETDATE(),
        updated_at DATETIME NOT NULL CONSTRAINT DF_shop_owner_profiles_updated_at DEFAULT GETDATE(),
        CONSTRAINT FK_shop_owner_profiles_users FOREIGN KEY (user_id) REFERENCES dbo.users(user_id)
    );
END
GO

IF OBJECT_ID(N'dbo.categories', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.categories (
        category_id INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_categories PRIMARY KEY,
        name NVARCHAR(100) NOT NULL CONSTRAINT UQ_categories_name UNIQUE,
        slug NVARCHAR(100) NOT NULL CONSTRAINT UQ_categories_slug UNIQUE,
        display_order INT NOT NULL CONSTRAINT DF_categories_display_order DEFAULT 0,
        is_active BIT NOT NULL CONSTRAINT DF_categories_is_active DEFAULT 1
    );
END
GO

IF OBJECT_ID(N'dbo.products', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.products (
        product_id INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_products PRIMARY KEY,
        owner_id INT NOT NULL,
        category_id INT NOT NULL,
        name NVARCHAR(200) NOT NULL,
        description NVARCHAR(MAX) NULL,
        origin_country NVARCHAR(100) NULL,
        origin_region NVARCHAR(150) NULL,
        harvest_date DATE NULL,
        shelf_life_days INT NULL,
        storage_instruction NVARCHAR(300) NULL,
        status NVARCHAR(20) NOT NULL CONSTRAINT CK_products_status DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
        view_count INT NOT NULL CONSTRAINT DF_products_view_count DEFAULT 0,
        rating DECIMAL(3,2) NOT NULL CONSTRAINT DF_products_rating DEFAULT 0,
        sold_quantity INT NOT NULL CONSTRAINT DF_products_sold_quantity DEFAULT 0,
        created_at DATETIME NOT NULL CONSTRAINT DF_products_created_at DEFAULT GETDATE(),
        updated_at DATETIME NOT NULL CONSTRAINT DF_products_updated_at DEFAULT GETDATE(),
        CONSTRAINT FK_products_users FOREIGN KEY (owner_id) REFERENCES dbo.users(user_id),
        CONSTRAINT FK_products_categories FOREIGN KEY (category_id) REFERENCES dbo.categories(category_id)
    );
END
GO

IF OBJECT_ID(N'dbo.product_images', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.product_images (
        image_id INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_product_images PRIMARY KEY,
        product_id INT NOT NULL,
        file_path NVARCHAR(500) NOT NULL,
        display_order INT NOT NULL CONSTRAINT DF_product_images_display_order DEFAULT 0,
        is_primary BIT NOT NULL CONSTRAINT DF_product_images_is_primary DEFAULT 0,
        uploaded_at DATETIME NOT NULL CONSTRAINT DF_product_images_uploaded_at DEFAULT GETDATE(),
        CONSTRAINT FK_product_images_products FOREIGN KEY (product_id) REFERENCES dbo.products(product_id) ON DELETE CASCADE
    );
END
GO

IF OBJECT_ID(N'dbo.product_variants', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.product_variants (
        variant_id INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_product_variants PRIMARY KEY,
        product_id INT NOT NULL,
        sku NVARCHAR(50) NOT NULL CONSTRAINT UQ_product_variants_sku UNIQUE,
        variant_label NVARCHAR(100) NOT NULL,
        price DECIMAL(12,2) NOT NULL,
        stock_quantity INT NOT NULL CONSTRAINT DF_product_variants_stock_quantity DEFAULT 0,
        is_active BIT NOT NULL CONSTRAINT DF_product_variants_is_active DEFAULT 1,
        created_at DATETIME NOT NULL CONSTRAINT DF_product_variants_created_at DEFAULT GETDATE(),
        updated_at DATETIME NOT NULL CONSTRAINT DF_product_variants_updated_at DEFAULT GETDATE(),
        CONSTRAINT FK_product_variants_products FOREIGN KEY (product_id) REFERENCES dbo.products(product_id) ON DELETE CASCADE
    );
END
GO

IF OBJECT_ID(N'dbo.inventory_logs', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.inventory_logs (
        log_id INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_inventory_logs PRIMARY KEY,
        variant_id INT NOT NULL,
        changed_by INT NOT NULL,
        change_type NVARCHAR(20) NOT NULL CONSTRAINT CK_inventory_logs_change_type CHECK (change_type IN ('MANUAL_ADJUST', 'ORDER_RESERVE', 'ORDER_RELEASE', 'ORDER_CONFIRM', 'RETURN')),
        quantity_delta INT NOT NULL,
        quantity_after INT NOT NULL,
        note NVARCHAR(300) NULL,
        changed_at DATETIME NOT NULL CONSTRAINT DF_inventory_logs_changed_at DEFAULT GETDATE(),
        CONSTRAINT FK_inventory_logs_variants FOREIGN KEY (variant_id) REFERENCES dbo.product_variants(variant_id),
        CONSTRAINT FK_inventory_logs_users FOREIGN KEY (changed_by) REFERENCES dbo.users(user_id)
    );
END
GO

IF OBJECT_ID(N'dbo.promotions', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.promotions (
        promo_id INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_promotions PRIMARY KEY,
        code NVARCHAR(50) NOT NULL CONSTRAINT UQ_promotions_code UNIQUE,
        discount_type NVARCHAR(10) NOT NULL CONSTRAINT CK_promotions_discount_type CHECK (discount_type IN ('PERCENT', 'FIXED')),
        discount_scope NVARCHAR(50) NOT NULL CONSTRAINT CK_promotions_discount_scope CHECK (discount_scope IN ('SHOP', 'ALL')),
        discount_max DECIMAL(10,2) NOT NULL CONSTRAINT DF_promotions_discount_max DEFAULT 0,
        discount_value DECIMAL(10,2) NOT NULL,
        min_order_value DECIMAL(14,2) NOT NULL CONSTRAINT DF_promotions_min_order_value DEFAULT 0,
        scope NVARCHAR(15) NOT NULL CONSTRAINT CK_promotions_scope CHECK (scope IN ('ORDER', 'PRODUCT')),
        product_id INT NULL,
        max_uses INT NULL,
        used_count INT NOT NULL CONSTRAINT DF_promotions_used_count DEFAULT 0,
        can_stack BIT NOT NULL CONSTRAINT DF_promotions_can_stack DEFAULT 0,
        valid_from DATETIME NOT NULL,
        valid_until DATETIME NOT NULL,
        created_by INT NOT NULL,
        created_at DATETIME NOT NULL CONSTRAINT DF_promotions_created_at DEFAULT GETDATE(),
        updated_at DATETIME NOT NULL CONSTRAINT DF_promotions_updated_at DEFAULT GETDATE(),
        is_deleted BIT NOT NULL CONSTRAINT DF_promotions_is_deleted DEFAULT 0,
        is_active BIT NOT NULL CONSTRAINT DF_promotions_is_active DEFAULT 1,
        CONSTRAINT FK_promotions_products FOREIGN KEY (product_id) REFERENCES dbo.products(product_id),
        CONSTRAINT FK_promotions_users FOREIGN KEY (created_by) REFERENCES dbo.users(user_id)
    );
END
GO

IF OBJECT_ID(N'dbo.cart', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.cart (
        cart_id INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_cart PRIMARY KEY,
        customer_id INT NOT NULL CONSTRAINT UQ_cart_customer_id UNIQUE,
        created_at DATETIME NOT NULL CONSTRAINT DF_cart_created_at DEFAULT GETDATE(),
        updated_at DATETIME NOT NULL CONSTRAINT DF_cart_updated_at DEFAULT GETDATE(),
        CONSTRAINT FK_cart_users FOREIGN KEY (customer_id) REFERENCES dbo.users(user_id)
    );
END
GO

IF OBJECT_ID(N'dbo.cart_items', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.cart_items (
        cart_item_id INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_cart_items PRIMARY KEY,
        cart_id INT NOT NULL,
        variant_id INT NOT NULL,
        quantity INT NOT NULL CONSTRAINT CK_cart_items_quantity CHECK (quantity >= 1),
        added_at DATETIME NOT NULL CONSTRAINT DF_cart_items_added_at DEFAULT GETDATE(),
        CONSTRAINT FK_cart_items_cart FOREIGN KEY (cart_id) REFERENCES dbo.cart(cart_id) ON DELETE CASCADE,
        CONSTRAINT FK_cart_items_variants FOREIGN KEY (variant_id) REFERENCES dbo.product_variants(variant_id)
    );
END
GO

IF OBJECT_ID(N'dbo.orders', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.orders (
        order_id INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_orders PRIMARY KEY,
        customer_id INT NOT NULL,
        owner_id INT NOT NULL,
        delivery_address NVARCHAR(500) NOT NULL,
        user_address NVARCHAR(500) NOT NULL,
        delivery_time_slot NVARCHAR(100) NULL,
        notes NVARCHAR(300) NULL,
        cancelled_at DATETIME NULL,
        cancelled_by INT NULL,
        cancellation_reason NVARCHAR(500) NULL,
        status NVARCHAR(25) NOT NULL CONSTRAINT DF_orders_status DEFAULT 'PENDING_PAYMENT' CONSTRAINT CK_orders_status CHECK (status IN ('PENDING_PAYMENT', 'CONFIRMED', 'PREPARING', 'DISPATCHED', 'DELIVERED', 'CANCELLED', 'PAYMENT_FAILED', 'EXPIRED')),
        total_amount DECIMAL(14,2) NOT NULL,
        delivery_fee DECIMAL(10,2) NOT NULL CONSTRAINT DF_orders_delivery_fee DEFAULT 0,
        discount_amount DECIMAL(12,2) NOT NULL CONSTRAINT DF_orders_discount_amount DEFAULT 0,
        system_discount_amount DECIMAL(12,2) NOT NULL CONSTRAINT DF_orders_system_discount_amount DEFAULT 0,
        shop_discount_amount DECIMAL(12,2) NOT NULL CONSTRAINT DF_orders_shop_discount_amount DEFAULT 0,
        platform_fee DECIMAL(12,2) NOT NULL CONSTRAINT DF_orders_platform_fee DEFAULT 0,
        final_amount DECIMAL(14,2) NOT NULL,
        payment_method NVARCHAR(20) NOT NULL CONSTRAINT CK_orders_payment_method  CHECK (payment_method IN ('CK', 'COD')),
        refund_status NVARCHAR(20) NOT NULL CONSTRAINT DF_orders_refund_status DEFAULT 'NONE' CONSTRAINT CK_orders_refund_status CHECK (refund_status IN ('NONE', 'PENDING', 'APPROVED', 'REJECTED', 'PROCESSING', 'REFUNDED', 'FAILED')),
        created_at DATETIME NOT NULL CONSTRAINT DF_orders_created_at DEFAULT GETDATE(),
        updated_at DATETIME NOT NULL CONSTRAINT DF_orders_updated_at DEFAULT GETDATE(),
        CONSTRAINT FK_orders_customer FOREIGN KEY (customer_id) REFERENCES dbo.users(user_id),
        CONSTRAINT FK_orders_owner FOREIGN KEY (owner_id) REFERENCES dbo.users(user_id),
        CONSTRAINT FK_orders_cancelled_by FOREIGN KEY (cancelled_by) REFERENCES dbo.users(user_id)
    );
END
GO

IF OBJECT_ID(N'dbo.order_items', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.order_items (
        order_item_id INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_order_items PRIMARY KEY,
        order_id INT NOT NULL,
        variant_id INT NULL,
        product_name_snapshot NVARCHAR(200) NOT NULL,
        variant_label_snapshot NVARCHAR(100) NOT NULL,
        quantity INT NOT NULL CONSTRAINT CK_order_items_quantity CHECK (quantity >= 1),
        unit_price DECIMAL(12,2) NOT NULL,
        subtotal DECIMAL(14,2) NOT NULL,
        CONSTRAINT FK_order_items_orders FOREIGN KEY (order_id) REFERENCES dbo.orders(order_id) ON DELETE CASCADE,
        CONSTRAINT FK_order_items_variants FOREIGN KEY (variant_id) REFERENCES dbo.product_variants(variant_id) ON DELETE SET NULL
    );
END
GO

IF OBJECT_ID(N'dbo.order_promotions', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.order_promotions (
        usage_id INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_order_promotions PRIMARY KEY,
        order_id INT NOT NULL,
        promo_id INT NOT NULL,
        customer_id INT NOT NULL,
        discount_applied DECIMAL(12,2) NOT NULL,
        used_at DATETIME NOT NULL CONSTRAINT DF_order_promotions_used_at DEFAULT GETDATE(),
        CONSTRAINT FK_order_promotions_orders FOREIGN KEY (order_id) REFERENCES dbo.orders(order_id) ON DELETE CASCADE,
        CONSTRAINT FK_order_promotions_promotions FOREIGN KEY (promo_id) REFERENCES dbo.promotions(promo_id),
        CONSTRAINT FK_order_promotions_users FOREIGN KEY (customer_id) REFERENCES dbo.users(user_id)
    );
END
GO

IF OBJECT_ID(N'dbo.return_requests', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.return_requests (
        return_request_id INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_return_requests PRIMARY KEY,
        order_id INT NOT NULL,
        order_item_id INT NULL,
        customer_id INT NOT NULL,
        request_type NVARCHAR(20) NOT NULL CONSTRAINT CK_return_requests_request_type CHECK (request_type IN ('CANCEL', 'RETURN', 'EXCHANGE')),
        reason_code NVARCHAR(50) NOT NULL CONSTRAINT CK_return_requests_reason_code CHECK (reason_code IN ('WRONG_ITEM', 'DAMAGED', 'MISSING_ITEM', 'LATE_DELIVERY', 'NOT_AS_DESCRIBED', 'OTHER')),
        description NVARCHAR(1000) NULL,
        evidence_url NVARCHAR(500) NULL,
        requested_quantity INT NOT NULL CONSTRAINT DF_return_requests_requested_quantity DEFAULT 1 CONSTRAINT CK_return_requests_requested_quantity CHECK (requested_quantity >= 1),
        resolution_type NVARCHAR(20) NULL CONSTRAINT CK_return_requests_resolution_type CHECK (resolution_type IN ('REFUND', 'REPLACE', 'DISCOUNT', 'REJECT')),
        replacement_variant_id INT NULL,
        refund_amount DECIMAL(14,2) NOT NULL CONSTRAINT DF_return_requests_refund_amount DEFAULT 0,
        status NVARCHAR(20) NOT NULL CONSTRAINT DF_return_requests_status DEFAULT 'REQUESTED' CONSTRAINT CK_return_requests_status CHECK (status IN ('REQUESTED', 'APPROVED', 'REJECTED', 'PROCESSING', 'COMPLETED', 'CANCELLED')),
        decided_by INT NULL,
        decision_reason NVARCHAR(500) NULL,
        resolved_at DATETIME NULL,
        created_at DATETIME NOT NULL CONSTRAINT DF_return_requests_created_at DEFAULT GETDATE(),
        updated_at DATETIME NOT NULL CONSTRAINT DF_return_requests_updated_at DEFAULT GETDATE(),
        CONSTRAINT FK_return_requests_orders FOREIGN KEY (order_id) REFERENCES dbo.orders(order_id),
        CONSTRAINT FK_return_requests_order_items FOREIGN KEY (order_item_id) REFERENCES dbo.order_items(order_item_id),
        CONSTRAINT FK_return_requests_customers FOREIGN KEY (customer_id) REFERENCES dbo.users(user_id),
        CONSTRAINT FK_return_requests_variants FOREIGN KEY (replacement_variant_id) REFERENCES dbo.product_variants(variant_id),
        CONSTRAINT FK_return_requests_decided_by FOREIGN KEY (decided_by) REFERENCES dbo.users(user_id)
    );
END
GO

IF OBJECT_ID(N'dbo.shop_settlements', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.shop_settlements (
        settlement_id INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_shop_settlements PRIMARY KEY,
        owner_id INT NOT NULL,
        period_start DATE NOT NULL,
        period_end DATE NOT NULL,
        gross_amount DECIMAL(14,2) NOT NULL CONSTRAINT DF_shop_settlements_gross_amount DEFAULT 0,
        platform_fee_amount DECIMAL(14,2) NOT NULL CONSTRAINT DF_shop_settlements_platform_fee_amount DEFAULT 0,
        refund_amount DECIMAL(14,2) NOT NULL CONSTRAINT DF_shop_settlements_refund_amount DEFAULT 0,
        adjustment_amount DECIMAL(14,2) NOT NULL CONSTRAINT DF_shop_settlements_adjustment_amount DEFAULT 0,
        net_amount DECIMAL(14,2) NOT NULL CONSTRAINT DF_shop_settlements_net_amount DEFAULT 0,
        status NVARCHAR(20) NOT NULL CONSTRAINT DF_shop_settlements_status DEFAULT 'PENDING' CONSTRAINT CK_shop_settlements_status CHECK (status IN ('PENDING', 'CONFIRMED', 'PAID', 'CANCELLED')),
        calculated_at DATETIME NOT NULL CONSTRAINT DF_shop_settlements_calculated_at DEFAULT GETDATE(),
        confirmed_at DATETIME NULL,
        paid_at DATETIME NULL,
        created_by INT NOT NULL,
        note NVARCHAR(500) NULL,
        CONSTRAINT FK_shop_settlements_owner FOREIGN KEY (owner_id) REFERENCES dbo.users(user_id),
        CONSTRAINT FK_shop_settlements_created_by FOREIGN KEY (created_by) REFERENCES dbo.users(user_id)
    );
END
GO

IF OBJECT_ID(N'dbo.shop_settlement_orders', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.shop_settlement_orders (
        settlement_order_id INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_shop_settlement_orders PRIMARY KEY,
        settlement_id INT NOT NULL,
        order_id INT NOT NULL CONSTRAINT UQ_shop_settlement_orders_order_id UNIQUE,
        order_amount DECIMAL(14,2) NOT NULL,
        platform_fee_amount DECIMAL(14,2) NOT NULL CONSTRAINT DF_shop_settlement_orders_platform_fee_amount DEFAULT 0,
        discount_amount DECIMAL(14,2) NOT NULL CONSTRAINT DF_shop_settlement_orders_discount_amount DEFAULT 0,
        refund_amount DECIMAL(14,2) NOT NULL CONSTRAINT DF_shop_settlement_orders_refund_amount DEFAULT 0,
        net_amount DECIMAL(14,2) NOT NULL,
        CONSTRAINT FK_shop_settlement_orders_settlement FOREIGN KEY (settlement_id) REFERENCES dbo.shop_settlements(settlement_id) ON DELETE CASCADE,
        CONSTRAINT FK_shop_settlement_orders_order FOREIGN KEY (order_id) REFERENCES dbo.orders(order_id)
    );
END
GO

IF OBJECT_ID(N'dbo.payment_transactions', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.payment_transactions (
        transaction_id INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_payment_transactions PRIMARY KEY,
        order_id INT NOT NULL CONSTRAINT UQ_payment_transactions_order_id UNIQUE,
        payment_method NVARCHAR(30) NOT NULL CONSTRAINT DF_payment_transactions_payment_method DEFAULT 'SEPAY',
        sepay_transaction_id NVARCHAR(100) NULL,
        sepay_reference NVARCHAR(100) NULL,
        sepay_qr_code NVARCHAR(500) NULL,
        amount DECIMAL(14,2) NOT NULL,
        currency NVARCHAR(3) NOT NULL CONSTRAINT DF_payment_transactions_currency DEFAULT 'VND',
        status NVARCHAR(20) NOT NULL CONSTRAINT DF_payment_transactions_status DEFAULT 'pending' CONSTRAINT CK_payment_transactions_status CHECK (status IN ('pending', 'processing', 'completed', 'failed', 'cancelled', 'refunded', 'expired')),
        initiated_at DATETIME NOT NULL CONSTRAINT DF_payment_transactions_initiated_at DEFAULT GETDATE(),
        completed_at DATETIME NULL,
        expires_at DATETIME NULL,
        provider_response NVARCHAR(MAX) NULL,
        error_code NVARCHAR(50) NULL,
        error_message NVARCHAR(500) NULL,
        ip_address NVARCHAR(45) NULL,
        CONSTRAINT FK_payment_transactions_order FOREIGN KEY (order_id) REFERENCES dbo.orders(order_id)
    );
END
GO

IF OBJECT_ID(N'dbo.sepay_webhook_dedup', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.sepay_webhook_dedup (
        dedup_id INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_sepay_webhook_dedup PRIMARY KEY,
        sepay_transaction_id NVARCHAR(100) NOT NULL CONSTRAINT UQ_sepay_webhook_dedup_transaction_id UNIQUE,
        order_code NVARCHAR(100) NOT NULL,
        process_result NVARCHAR(30) NOT NULL CONSTRAINT DF_sepay_webhook_dedup_process_result DEFAULT 'processed',
        created_at DATETIME NOT NULL CONSTRAINT DF_sepay_webhook_dedup_created_at DEFAULT GETDATE()
    );
END
GO

IF OBJECT_ID(N'dbo.deliveries', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.deliveries (
        delivery_id INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_deliveries PRIMARY KEY,
        order_id INT NOT NULL CONSTRAINT UQ_deliveries_order_id UNIQUE,
        staff_id INT NULL,
        status NVARCHAR(20) NOT NULL CONSTRAINT DF_deliveries_status DEFAULT 'ASSIGNED' CONSTRAINT CK_deliveries_status CHECK (status IN ('ASSIGNED', 'PICKED_UP', 'IN_TRANSIT', 'DELIVERED', 'FAILED')),
        picked_up_at DATETIME NULL,
        delivered_at DATETIME NULL,
        failure_reason NVARCHAR(300) NULL,
        created_at DATETIME NOT NULL CONSTRAINT DF_deliveries_created_at DEFAULT GETDATE(),
        updated_at DATETIME NOT NULL CONSTRAINT DF_deliveries_updated_at DEFAULT GETDATE(),
        CONSTRAINT FK_deliveries_order FOREIGN KEY (order_id) REFERENCES dbo.orders(order_id),
        CONSTRAINT FK_deliveries_staff FOREIGN KEY (staff_id) REFERENCES dbo.users(user_id)
    );
END
GO

IF OBJECT_ID(N'dbo.reviews', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.reviews (
        review_id INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_reviews PRIMARY KEY,
        order_item_id INT NOT NULL,
        customer_id INT NOT NULL,
        rating TINYINT NOT NULL CONSTRAINT CK_reviews_rating CHECK (rating BETWEEN 1 AND 5),
        review_text NVARCHAR(1000) NULL,
        is_hidden BIT NOT NULL CONSTRAINT DF_reviews_is_hidden DEFAULT 0,
        created_at DATETIME NOT NULL CONSTRAINT DF_reviews_created_at DEFAULT GETDATE(),
        CONSTRAINT UQ_review_customer_item UNIQUE (customer_id, order_item_id),
        CONSTRAINT FK_reviews_order_items FOREIGN KEY (order_item_id) REFERENCES dbo.order_items(order_item_id),
        CONSTRAINT FK_reviews_customers FOREIGN KEY (customer_id) REFERENCES dbo.users(user_id)
    );
END
GO

IF OBJECT_ID(N'dbo.chat_sessions', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.chat_sessions (
        session_id INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_chat_sessions PRIMARY KEY,
        customer_id INT NOT NULL,
        owner_id INT NOT NULL,
        status NVARCHAR(20) NOT NULL CONSTRAINT DF_chat_sessions_status DEFAULT 'ACTIVE' CONSTRAINT CK_chat_sessions_status CHECK (status IN ('ACTIVE', 'CLOSED')),
        created_at DATETIME NOT NULL CONSTRAINT DF_chat_sessions_created_at DEFAULT GETDATE(),
        updated_at DATETIME NOT NULL CONSTRAINT DF_chat_sessions_updated_at DEFAULT GETDATE(),
        closed_at DATETIME NULL,
        CONSTRAINT FK_chat_sessions_customer FOREIGN KEY (customer_id) REFERENCES dbo.users(user_id),
        CONSTRAINT FK_chat_sessions_owner FOREIGN KEY (owner_id) REFERENCES dbo.users(user_id)
    );
END
GO

IF OBJECT_ID(N'dbo.chat_messages', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.chat_messages (
        message_id INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_chat_messages PRIMARY KEY,
        session_id INT NOT NULL,
        sender_id INT NOT NULL,
        content NVARCHAR(MAX) NOT NULL,
        is_read BIT NOT NULL CONSTRAINT DF_chat_messages_is_read DEFAULT 0,
        created_at DATETIME NOT NULL CONSTRAINT DF_chat_messages_created_at DEFAULT GETDATE(),
        CONSTRAINT FK_chat_messages_session FOREIGN KEY (session_id) REFERENCES dbo.chat_sessions(session_id) ON DELETE CASCADE,
        CONSTRAINT FK_chat_messages_sender FOREIGN KEY (sender_id) REFERENCES dbo.users(user_id)
    );
END
GO

IF OBJECT_ID(N'dbo.notifications', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.notifications (
        notification_id INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_notifications PRIMARY KEY,
        user_id INT NOT NULL,
        type NVARCHAR(50) NOT NULL CONSTRAINT CK_notifications_type CHECK (type IN ('ORDER_UPDATE', 'PROMOTION', 'SYSTEM', 'INVENTORY_ALERT', 'PAYMENT')),
        title NVARCHAR(200) NOT NULL,
        message NVARCHAR(MAX) NOT NULL,
        action_url NVARCHAR(300) NULL,
        is_read BIT NOT NULL CONSTRAINT DF_notifications_is_read DEFAULT 0,
        created_at DATETIME NOT NULL CONSTRAINT DF_notifications_created_at DEFAULT GETDATE(),
        CONSTRAINT FK_notifications_users FOREIGN KEY (user_id) REFERENCES dbo.users(user_id) ON DELETE CASCADE
    );
END
GO

-- =========================================================
-- Seed data
-- =========================================================

BEGIN TRY
    BEGIN TRAN;

    SET IDENTITY_INSERT dbo.users ON;
    INSERT INTO dbo.users (user_id, full_name, email, password_hash, phone, role, status, user_address, is_email_verified, failed_login_count, locked_until, created_at, updated_at)
    VALUES
        (1, N'Admin System', N'admin@fruitshop.local', N'hash_admin_demo', N'0900000001', N'ADMIN', N'ACTIVE', N'Central admin office', 1, 0, NULL, '2026-05-01T09:00:00', '2026-05-01T09:00:00'),
        (2, N'Delivery Nguyen', N'delivery@fruitshop.local', N'hash_delivery_demo', N'0900000002', N'DELIVERY', N'ACTIVE', N'Delivery hub, HCMC', 1, 0, NULL, '2026-05-01T09:05:00', '2026-05-01T09:05:00'),
        (3, N'An Phu Orchard Owner', N'owner1@fruitshop.local', N'hash_owner1_demo', N'0900000003', N'SHOP_OWNER', N'ACTIVE', N'12 Le Loi, District 1, HCMC', 1, 0, NULL, '2026-05-01T09:10:00', '2026-05-01T09:10:00'),
        (4, N'Mekong Fresh Owner', N'owner2@fruitshop.local', N'hash_owner2_demo', N'0900000004', N'SHOP_OWNER', N'ACTIVE', N'88 Nguyen Trai, District 5, HCMC', 1, 0, NULL, '2026-05-01T09:15:00', '2026-05-01T09:15:00'),
        (5, N'Tran Minh Customer', N'customer1@fruitshop.local', N'hash_customer1_demo', N'0900000005', N'CUSTOMER', N'ACTIVE', N'15 Pasteur, District 3, HCMC', 1, 0, NULL, '2026-05-01T09:20:00', '2026-05-01T09:20:00'),
        (6, N'Le Thu Customer', N'customer2@fruitshop.local', N'hash_customer2_demo', N'0900000006', N'CUSTOMER', N'ACTIVE', N'90 Truong Chinh, Tan Binh, HCMC', 1, 0, NULL, '2026-05-01T09:25:00', '2026-05-01T09:25:00'),
        (7, N'Klever Premium Owner', N'owner3@fruitshop.local', N'hash_owner3_demo', N'0900000007', N'SHOP_OWNER', N'ACTIVE', N'52 Vo Thi Sau, District 3, HCMC', 1, 0, NULL, '2026-05-01T09:30:00', '2026-05-01T09:30:00');
    SET IDENTITY_INSERT dbo.users OFF;

    SET IDENTITY_INSERT dbo.user_sessions ON;
    INSERT INTO dbo.user_sessions (session_id, user_id, token, expires_at)
    VALUES
        (1, 1, N'sess-admin-001', '2026-05-17T23:59:00'),
        (2, 5, N'sess-customer-001', '2026-05-17T23:59:00'),
        (3, 3, N'sess-owner-001', '2026-05-10T23:59:00'),
        (4, 6, N'sess-customer-002', '2026-05-17T23:59:00');
    SET IDENTITY_INSERT dbo.user_sessions OFF;

    SET IDENTITY_INSERT dbo.shop_owner_profiles ON;
    INSERT INTO dbo.shop_owner_profiles (profile_id, user_id, shop_name, shop_description, approval_status, rejection_reason, approved_at, delivery_address, rating, created_at, updated_at)
    VALUES
        (1, 3, N'An Phu Orchard', N'Premium citrus and banana supplier', N'APPROVED', NULL, '2026-05-02T10:00:00', N'12 Le Loi, District 1, HCMC', 4.88, '2026-05-02T10:00:00', '2026-05-16T08:00:00'),
        (2, 4, N'Mekong Fresh Farm', N'Mango, berries, and grapes specialist', N'APPROVED', NULL, '2026-05-03T10:00:00', N'88 Nguyen Trai, District 5, HCMC', 4.76, '2026-05-03T10:00:00', '2026-05-16T08:00:00'),
        (3, 7, N'Klever Premium Fruits', N'Imported fruits, gift boxes, and seasonal premium selections', N'APPROVED', NULL, '2026-05-04T10:00:00', N'52 Vo Thi Sau, District 3, HCMC', 4.91, '2026-05-04T10:00:00', '2026-05-16T08:00:00');
    SET IDENTITY_INSERT dbo.shop_owner_profiles OFF;

    SET IDENTITY_INSERT dbo.categories ON;
    INSERT INTO dbo.categories (category_id, name, slug, display_order, is_active)
    VALUES
        (1, N'Citrus', N'citrus', 1, 1),
        (2, N'Tropical', N'tropical', 2, 1),
        (3, N'Berries', N'berries', 3, 1),
        (4, N'Gift Boxes', N'gift-boxes', 4, 1),
        (5, N'Apples', N'apples', 5, 1),
        (6, N'Grapes', N'grapes', 6, 1),
        (7, N'Melons', N'melons', 7, 1),
        (8, N'Kiwi', N'kiwi', 8, 1),
        (9, N'Cherries', N'cherries', 9, 1),
        (10, N'Imported Mix', N'imported-mix', 10, 1);
    SET IDENTITY_INSERT dbo.categories OFF;

    SET IDENTITY_INSERT dbo.products ON;
    INSERT INTO dbo.products (product_id, owner_id, category_id, name, description, origin_country, origin_region, harvest_date, shelf_life_days, storage_instruction, status, view_count, rating, sold_quantity, created_at, updated_at)
    VALUES
        (1, 3, 1, N'Cam Sanh Cao Phong', N'Sweet Cao Phong oranges with thin peel', N'Vietnam', N'Cao Phong, Hoa Binh', '2026-05-08', 12, N'Keep chilled at 6-10C', N'ACTIVE', 1820, 4.80, 220, '2026-05-03T09:00:00', '2026-05-16T08:00:00'),
        (2, 3, 1, N'Buoi Da Xanh', N'Large green pomelo from Ben Tre', N'Vietnam', N'Ben Tre', '2026-05-06', 18, N'Store in a cool dry place', N'ACTIVE', 1140, 4.92, 140, '2026-05-03T09:05:00', '2026-05-16T08:00:00'),
        (3, 3, 2, N'Chuoi Lun Da Lat', N'Fresh small bananas for daily snack', N'Vietnam', N'Da Lat', '2026-05-12', 8, N'Avoid direct sun', N'ACTIVE', 980, 4.60, 260, '2026-05-03T09:10:00', '2026-05-16T08:00:00'),
        (4, 4, 2, N'Xoai Cat Hoa Loc', N'Premium ripe mango', N'Vietnam', N'Tien Giang', '2026-05-07', 6, N'Ripen at room temperature', N'ACTIVE', 2150, 4.95, 180, '2026-05-03T09:15:00', '2026-05-16T08:00:00'),
        (5, 4, 3, N'Dau Tay New Zealand', N'Imported berries for premium gift', N'New Zealand', N'Canterbury', '2026-05-10', 5, N'Keep refrigerated', N'ACTIVE', 760, 4.70, 96, '2026-05-03T09:20:00', '2026-05-16T08:00:00'),
        (6, 4, 4, N'Nho Xanh Mau Don', N'Seedless green grapes box', N'Chile', N'Central Valley', '2026-05-09', 10, N'Store at 2-4C', N'ACTIVE', 840, 4.85, 72, '2026-05-03T09:25:00', '2026-05-16T08:00:00'),
        (7, 7, 7, N'Dua Vang Han Quoc Size 11', N'Sweet yellow melon with crisp flesh', N'Korea', N'Gyeongsang', '2026-05-11', 7, N'Refrigerate after cutting', N'ACTIVE', 1460, 4.78, 88, '2026-05-03T09:26:00', '2026-05-16T08:00:00'),
        (8, 7, 8, N'Kiwi Vang New Zealand 3.5kg', N'Golden kiwi with bright aroma', N'New Zealand', N'Bay of Plenty', '2026-05-10', 9, N'Keep chilled and dry', N'ACTIVE', 1325, 4.87, 64, '2026-05-03T09:27:00', '2026-05-16T08:00:00'),
        (9, 7, 6, N'Nho Den Khong Hat Ngon Tay Oliver UC', N'Crunchy seedless black grapes in premium pack', N'Australia', N'South Australia', '2026-05-09', 11, N'Store in a ventilated cold room', N'ACTIVE', 1188, 4.90, 55, '2026-05-03T09:28:00', '2026-05-16T08:00:00'),
        (10, 7, 6, N'Nho Xanh Autumn Nam Phi', N'Green grapes imported from South Africa', N'South Africa', N'Western Cape', '2026-05-09', 11, N'Store at 0-4C', N'ACTIVE', 1040, 4.83, 61, '2026-05-03T09:29:00', '2026-05-16T08:00:00'),
        (11, 7, 9, N'Cherry Do Orchard View My', N'Premium red cherries from the United States', N'USA', N'Washington', '2026-05-08', 6, N'Keep refrigerated and avoid crushing', N'ACTIVE', 1575, 4.94, 44, '2026-05-03T09:30:00', '2026-05-16T08:00:00'),
        (12, 7, 5, N'Tao Cosmic Crisp My', N'Crisp and juicy apples with balanced sweetness', N'USA', N'Washington State', '2026-05-09', 20, N'Store in a cool dry place', N'ACTIVE', 1210, 4.81, 73, '2026-05-03T09:31:00', '2026-05-16T08:00:00'),
        (13, 7, 5, N'Tao Dazzle New Zealand', N'Bright red apples with a refreshing finish', N'New Zealand', N'Hawke''s Bay', '2026-05-09', 18, N'Refrigerate for best crunch', N'ACTIVE', 990, 4.79, 58, '2026-05-03T09:32:00', '2026-05-16T08:00:00'),
        (14, 7, 10, N'Hop Qua Cherry My 3kg', N'Gift box for premium fruit gifting', N'USA', N'Imported mix', '2026-05-10', 5, N'Keep chilled until gifting', N'ACTIVE', 690, 4.88, 39, '2026-05-03T09:33:00', '2026-05-16T08:00:00'),
        (15, 7, 10, N'Fruit Mix Premium Season', N'Assorted imported fruit box for family and office', N'Mixed', N'Imported selection', '2026-05-10', 7, N'Refrigerate immediately after delivery', N'ACTIVE', 845, 4.84, 47, '2026-05-03T09:34:00', '2026-05-16T08:00:00');
    SET IDENTITY_INSERT dbo.products OFF;

    SET IDENTITY_INSERT dbo.product_images ON;
    INSERT INTO dbo.product_images (image_id, product_id, file_path, display_order, is_primary, uploaded_at)
    VALUES
        (1, 1, N'/assets/images/products/cam-sanh-cao-phong/main.jpg', 1, 1, '2026-05-03T09:30:00'),
        (2, 1, N'/assets/images/products/cam-sanh-cao-phong/side.jpg', 2, 0, '2026-05-03T09:30:00'),
        (3, 2, N'/assets/images/products/buoi-da-xanh/main.jpg', 1, 1, '2026-05-03T09:31:00'),
        (4, 3, N'/assets/images/products/chuoi-lun-da-lat/main.jpg', 1, 1, '2026-05-03T09:32:00'),
        (5, 4, N'/assets/images/products/xoai-cat-hoa-loc/main.jpg', 1, 1, '2026-05-03T09:33:00'),
        (6, 4, N'/assets/images/products/xoai-cat-hoa-loc/box.jpg', 2, 0, '2026-05-03T09:33:00'),
        (7, 5, N'/assets/images/products/dau-tay-new-zealand/main.jpg', 1, 1, '2026-05-03T09:34:00'),
        (8, 6, N'/assets/images/products/nho-xanh-mau-don/main.jpg', 1, 1, '2026-05-03T09:35:00'),
        (9, 7, N'/assets/images/products/dua-vang-han-quoc/main.jpg', 1, 1, '2026-05-03T09:36:00'),
        (10, 8, N'/assets/images/products/kiwi-vang-new-zealand/main.jpg', 1, 1, '2026-05-03T09:37:00'),
        (11, 9, N'/assets/images/products/nho-den-ngon-tay-oliver/main.jpg', 1, 1, '2026-05-03T09:38:00'),
        (12, 10, N'/assets/images/products/nho-xanh-autumn/main.jpg', 1, 1, '2026-05-03T09:39:00'),
        (13, 11, N'/assets/images/products/cherry-do-orchard-view/main.jpg', 1, 1, '2026-05-03T09:40:00'),
        (14, 12, N'/assets/images/products/tao-cosmic-crisp/main.jpg', 1, 1, '2026-05-03T09:41:00'),
        (15, 13, N'/assets/images/products/tao-dazzle-new-zealand/main.jpg', 1, 1, '2026-05-03T09:42:00'),
        (16, 14, N'/assets/images/products/hop-qua-cherry-my/main.jpg', 1, 1, '2026-05-03T09:43:00'),
        (17, 15, N'/assets/images/products/fruit-mix-premium-season/main.jpg', 1, 1, '2026-05-03T09:44:00');
    SET IDENTITY_INSERT dbo.product_images OFF;

    SET IDENTITY_INSERT dbo.product_variants ON;
    INSERT INTO dbo.product_variants (variant_id, product_id, sku, variant_label, price, stock_quantity, is_active, created_at, updated_at)
    VALUES
        (1, 1, N'CAM-SANH-1KG', N'1kg', 45000.00, 118, 1, '2026-05-03T09:40:00', '2026-05-16T08:10:00'),
        (2, 1, N'CAM-SANH-2KG', N'2kg', 85000.00, 57, 1, '2026-05-03T09:40:00', '2026-05-16T08:10:00'),
        (3, 2, N'BUOI-DA-XANH-1P5KG', N'1.5kg', 78000.00, 48, 1, '2026-05-03T09:41:00', '2026-05-16T08:10:00'),
        (4, 3, N'CHUOI-LUN-1KG', N'1kg', 32000.00, 148, 1, '2026-05-03T09:42:00', '2026-05-16T08:10:00'),
        (5, 4, N'XOAI-CAT-1KG', N'1kg', 125000.00, 39, 1, '2026-05-03T09:43:00', '2026-05-16T08:10:00'),
        (6, 4, N'XOAI-CAT-2KG', N'2kg', 238000.00, 24, 1, '2026-05-03T09:43:00', '2026-05-16T08:10:00'),
        (7, 5, N'DAU-TAY-500G', N'500g', 89000.00, 34, 1, '2026-05-03T09:44:00', '2026-05-16T08:10:00'),
        (8, 6, N'NHO-XANH-1KG', N'1kg', 155000.00, 29, 1, '2026-05-03T09:45:00', '2026-05-16T08:10:00'),
        (9, 7, N'DUA-VANG-KR-1QT', N'1 qua', 99000.00, 52, 1, '2026-05-03T09:46:00', '2026-05-16T08:10:00'),
        (10, 8, N'KIWI-VANG-NZ-1KG', N'1kg', 265000.00, 31, 1, '2026-05-03T09:47:00', '2026-05-16T08:10:00'),
        (11, 9, N'NHO-DEN-UC-9KG', N'9kg box', 690000.00, 12, 1, '2026-05-03T09:48:00', '2026-05-16T08:10:00'),
        (12, 10, N'NHO-AUTUMN-ZA-4P5', N'4.5kg box', 420000.00, 16, 1, '2026-05-03T09:49:00', '2026-05-16T08:10:00'),
        (13, 11, N'CHERRY-RED-US-500G', N'500g', 249000.00, 27, 1, '2026-05-03T09:50:00', '2026-05-16T08:10:00'),
        (14, 12, N'TAO-COSMIC-CRISP-1KG', N'1kg', 95000.00, 46, 1, '2026-05-03T09:51:00', '2026-05-16T08:10:00'),
        (15, 13, N'TAO-DAZZLE-NZ-1KG', N'1kg', 175000.00, 44, 1, '2026-05-03T09:52:00', '2026-05-16T08:10:00'),
        (16, 14, N'CHERRY-GIFT-BOX-3KG', N'3kg gift box', 799000.00, 9, 1, '2026-05-03T09:53:00', '2026-05-16T08:10:00'),
        (17, 15, N'FRUIT-MIX-PREMIUM', N'Assorted box', 499000.00, 14, 1, '2026-05-03T09:54:00', '2026-05-16T08:10:00');
    SET IDENTITY_INSERT dbo.product_variants OFF;

    SET IDENTITY_INSERT dbo.inventory_logs ON;
    INSERT INTO dbo.inventory_logs (log_id, variant_id, changed_by, change_type, quantity_delta, quantity_after, note, changed_at)
    VALUES
        (1, 1, 3, N'MANUAL_ADJUST', 18, 118, N'Top up before campaign', '2026-05-12T08:00:00'),
        (2, 2, 3, N'ORDER_RESERVE', -3, 57, N'Reserved for demo orders', '2026-05-15T09:05:00'),
        (3, 4, 3, N'ORDER_CONFIRM', -2, 148, N'Confirmed order pick', '2026-05-16T08:20:00'),
        (4, 5, 4, N'ORDER_RESERVE', -1, 39, N'Reserved for delivery', '2026-05-15T10:00:00'),
        (5, 6, 4, N'MANUAL_ADJUST', 4, 24, N'Stock audit correction', '2026-05-14T18:00:00'),
        (6, 8, 1, N'MANUAL_ADJUST', 2, 29, N'Warehouse correction', '2026-05-14T19:00:00');
    SET IDENTITY_INSERT dbo.inventory_logs OFF;

    SET IDENTITY_INSERT dbo.promotions ON;
    INSERT INTO dbo.promotions (promo_id, code, discount_type, discount_scope, discount_max, discount_value, min_order_value, scope, product_id, max_uses, used_count, can_stack, valid_from, valid_until, created_by, created_at, updated_at, is_deleted, is_active)
    VALUES
        (1, N'WELCOME10', N'PERCENT', N'ALL', 50000.00, 10.00, 120000.00, N'ORDER', NULL, 1000, 36, 0, '2026-01-01T00:00:00', '2026-12-31T23:59:59', 1, '2026-05-01T10:00:00', '2026-05-16T08:00:00', 0, 1),
        (2, N'SHOP3SAVE20', N'FIXED', N'SHOP', 0.00, 20000.00, 180000.00, N'ORDER', NULL, 200, 18, 0, '2026-04-01T00:00:00', '2026-12-31T23:59:59', 3, '2026-05-01T10:05:00', '2026-05-16T08:00:00', 0, 1),
        (3, N'MANGO15', N'FIXED', N'SHOP', 0.00, 15000.00, 100000.00, N'PRODUCT', 4, 50, 9, 1, '2026-04-15T00:00:00', '2026-12-31T23:59:59', 4, '2026-05-01T10:10:00', '2026-05-16T08:00:00', 0, 1),
        (4, N'OLDSPRING5', N'PERCENT', N'ALL', 20000.00, 5.00, 100000.00, N'ORDER', NULL, 50, 2, 0, '2026-01-01T00:00:00', '2026-04-01T23:59:59', 1, '2026-04-01T10:00:00', '2026-04-01T10:00:00', 1, 0);
    SET IDENTITY_INSERT dbo.promotions OFF;

    SET IDENTITY_INSERT dbo.cart ON;
    INSERT INTO dbo.cart (cart_id, customer_id, created_at, updated_at)
    VALUES
        (1, 5, '2026-05-15T08:10:00', '2026-05-16T08:00:00'),
        (2, 6, '2026-05-15T09:10:00', '2026-05-16T08:00:00');
    SET IDENTITY_INSERT dbo.cart OFF;

    SET IDENTITY_INSERT dbo.cart_items ON;
    INSERT INTO dbo.cart_items (cart_item_id, cart_id, variant_id, quantity, added_at)
    VALUES
        (1, 1, 1, 1, '2026-05-15T08:12:00'),
        (2, 1, 3, 1, '2026-05-15T08:12:00'),
        (3, 2, 5, 1, '2026-05-15T09:12:00'),
        (4, 2, 7, 1, '2026-05-15T09:12:00');
    SET IDENTITY_INSERT dbo.cart_items OFF;

    SET IDENTITY_INSERT dbo.orders ON;
    INSERT INTO dbo.orders (order_id, customer_id, owner_id, delivery_address, user_address, delivery_time_slot, notes, cancelled_at, cancelled_by, cancellation_reason, status, total_amount, delivery_fee, discount_amount, system_discount_amount, shop_discount_amount, platform_fee, final_amount, payment_method, refund_status, created_at, updated_at)
    VALUES
        (1, 5, 3, N'15 Pasteur, District 3, HCMC', N'15 Pasteur, District 3, HCMC', N'08:00-12:00', N'Leave at reception', NULL, NULL, NULL, N'DELIVERED', 130000.00, 15000.00, 13000.00, 10000.00, 3000.00, 6500.00, 132000.00, N'CK', N'NONE', '2026-05-15T09:10:00', '2026-05-16T12:30:00'),
        (2, 6, 4, N'90 Truong Chinh, Tan Binh, HCMC', N'90 Truong Chinh, Tan Binh, HCMC', N'14:00-18:00', N'Call on arrival', NULL, NULL, NULL, N'DELIVERED', 214000.00, 20000.00, 15000.00, 0.00, 15000.00, 10700.00, 219000.00, N'COD', N'NONE', '2026-05-15T10:20:00', '2026-05-16T13:10:00'),
        (3, 5, 3, N'15 Pasteur, District 3, HCMC', N'15 Pasteur, District 3, HCMC', N'18:00-21:00', N'Ring the bell twice', NULL, NULL, NULL, N'DELIVERED', 142000.00, 12000.00, 14200.00, 14200.00, 0.00, 7100.00, 139800.00, N'CK', N'PENDING', '2026-05-16T08:00:00', '2026-05-16T18:00:00');
    SET IDENTITY_INSERT dbo.orders OFF;

    SET IDENTITY_INSERT dbo.order_items ON;
    INSERT INTO dbo.order_items (order_item_id, order_id, variant_id, product_name_snapshot, variant_label_snapshot, quantity, unit_price, subtotal)
    VALUES
        (1, 1, 1, N'Cam Sanh Cao Phong', N'1kg', 1, 45000.00, 45000.00),
        (2, 1, 2, N'Cam Sanh Cao Phong', N'2kg', 1, 85000.00, 85000.00),
        (3, 2, 5, N'Xoai Cat Hoa Loc', N'1kg', 1, 125000.00, 125000.00),
        (4, 2, 7, N'Dau Tay New Zealand', N'500g', 1, 89000.00, 89000.00),
        (5, 3, 3, N'Buoi Da Xanh', N'1.5kg', 1, 78000.00, 78000.00),
        (6, 3, 4, N'Chuoi Lun Da Lat', N'1kg', 2, 32000.00, 64000.00);
    SET IDENTITY_INSERT dbo.order_items OFF;

    SET IDENTITY_INSERT dbo.order_promotions ON;
    INSERT INTO dbo.order_promotions (usage_id, order_id, promo_id, customer_id, discount_applied, used_at)
    VALUES
        (1, 1, 1, 5, 13000.00, '2026-05-15T09:20:00'),
        (2, 2, 3, 6, 15000.00, '2026-05-15T10:30:00'),
        (3, 3, 1, 5, 14200.00, '2026-05-16T08:10:00');
    SET IDENTITY_INSERT dbo.order_promotions OFF;

    SET IDENTITY_INSERT dbo.return_requests ON;
    INSERT INTO dbo.return_requests (return_request_id, order_id, order_item_id, customer_id, request_type, reason_code, description, evidence_url, requested_quantity, resolution_type, replacement_variant_id, refund_amount, status, decided_by, decision_reason, resolved_at, created_at, updated_at)
    VALUES
        (1, 3, 6, 5, N'RETURN', N'DAMAGED', N'One box arrived with bruised bananas', N'/evidence/returns/rr-001.jpg', 1, N'REFUND', NULL, 32000.00, N'REQUESTED', NULL, NULL, NULL, '2026-05-16T19:00:00', '2026-05-16T19:00:00');
    SET IDENTITY_INSERT dbo.return_requests OFF;

    SET IDENTITY_INSERT dbo.shop_settlements ON;
    INSERT INTO dbo.shop_settlements (settlement_id, owner_id, period_start, period_end, gross_amount, platform_fee_amount, refund_amount, adjustment_amount, net_amount, status, calculated_at, confirmed_at, paid_at, created_by, note)
    VALUES
        (1, 3, '2026-05-01', '2026-05-15', 244800.00, 13600.00, 0.00, 0.00, 231200.00, N'PENDING', '2026-05-16T20:00:00', NULL, NULL, 1, N'Pending because order 3 has an open return request'),
        (2, 4, '2026-05-01', '2026-05-15', 199000.00, 10700.00, 0.00, 0.00, 188300.00, N'PAID', '2026-05-16T20:00:00', '2026-05-16T20:30:00', '2026-05-16T22:00:00', 1, N'Includes order 2');
    SET IDENTITY_INSERT dbo.shop_settlements OFF;

    SET IDENTITY_INSERT dbo.shop_settlement_orders ON;
    INSERT INTO dbo.shop_settlement_orders (settlement_order_id, settlement_id, order_id, order_amount, platform_fee_amount, discount_amount, refund_amount, net_amount)
    VALUES
        (1, 1, 1, 130000.00, 6500.00, 13000.00, 0.00, 110500.00),
        (2, 1, 3, 142000.00, 7100.00, 14200.00, 0.00, 120700.00),
        (3, 2, 2, 214000.00, 10700.00, 15000.00, 0.00, 188300.00);
    SET IDENTITY_INSERT dbo.shop_settlement_orders OFF;

    SET IDENTITY_INSERT dbo.payment_transactions ON;
    INSERT INTO dbo.payment_transactions (transaction_id, order_id, payment_method, sepay_transaction_id, sepay_reference, sepay_qr_code, amount, currency, status, initiated_at, completed_at, expires_at, provider_response, error_code, error_message, ip_address)
    VALUES
        (1, 1, N'SEPAY', N'SP20260516001', N'DH-0001', N'/payments/qr/DH-0001.png', 132000.00, N'VND', N'completed', '2026-05-15T09:11:00', '2026-05-15T09:13:00', '2026-05-15T09:26:00', N'success', NULL, NULL, N'127.0.0.1'),
        (2, 2, N'COD', NULL, NULL, NULL, 219000.00, N'VND', N'completed', '2026-05-15T10:21:00', '2026-05-16T13:15:00', NULL, NULL, NULL, NULL, N'127.0.0.1'),
        (3, 3, N'SEPAY', N'SP20260516003', N'DH-0003', N'/payments/qr/DH-0003.png', 139800.00, N'VND', N'completed', '2026-05-16T08:01:00', '2026-05-16T08:03:00', '2026-05-16T08:16:00', N'success', NULL, NULL, N'127.0.0.1');
    SET IDENTITY_INSERT dbo.payment_transactions OFF;

    SET IDENTITY_INSERT dbo.sepay_webhook_dedup ON;
    INSERT INTO dbo.sepay_webhook_dedup (dedup_id, sepay_transaction_id, order_code, process_result, created_at)
    VALUES
        (1, N'SP20260516001', N'DH-0001', N'processed', '2026-05-15T09:14:00'),
        (2, N'SP20260516003', N'DH-0003', N'processed', '2026-05-16T08:04:00');
    SET IDENTITY_INSERT dbo.sepay_webhook_dedup OFF;

    SET IDENTITY_INSERT dbo.deliveries ON;
    INSERT INTO dbo.deliveries (delivery_id, order_id, staff_id, status, picked_up_at, delivered_at, failure_reason, created_at, updated_at)
    VALUES
        (1, 1, 2, N'DELIVERED', '2026-05-15T11:00:00', '2026-05-15T11:45:00', NULL, '2026-05-15T09:15:00', '2026-05-15T11:45:00'),
        (2, 2, 2, N'DELIVERED', '2026-05-15T14:10:00', '2026-05-15T16:00:00', NULL, '2026-05-15T10:25:00', '2026-05-15T16:00:00'),
        (3, 3, 2, N'DELIVERED', '2026-05-16T10:00:00', '2026-05-16T10:40:00', NULL, '2026-05-16T08:20:00', '2026-05-16T10:40:00');
    SET IDENTITY_INSERT dbo.deliveries OFF;

    SET IDENTITY_INSERT dbo.reviews ON;
    INSERT INTO dbo.reviews (review_id, order_item_id, customer_id, rating, review_text, is_hidden, created_at)
    VALUES
        (1, 1, 5, 5, N'Sweet and fresh, fast delivery', 0, '2026-05-15T12:00:00'),
        (2, 3, 6, 4, N'Good mango quality and careful packing', 0, '2026-05-15T17:00:00');
    SET IDENTITY_INSERT dbo.reviews OFF;

    SET IDENTITY_INSERT dbo.chat_sessions ON;
    INSERT INTO dbo.chat_sessions (session_id, customer_id, owner_id, status, created_at, updated_at, closed_at)
    VALUES
        (1, 5, 3, N'ACTIVE', '2026-05-15T08:30:00', '2026-05-16T18:15:00', NULL),
        (2, 6, 4, N'CLOSED', '2026-05-15T09:00:00', '2026-05-15T17:30:00', '2026-05-15T17:30:00');
    SET IDENTITY_INSERT dbo.chat_sessions OFF;

    SET IDENTITY_INSERT dbo.chat_messages ON;
    INSERT INTO dbo.chat_messages (message_id, session_id, sender_id, content, is_read, created_at)
    VALUES
        (1, 1, 5, N'Hello shop, can you deliver before noon?', 1, '2026-05-15T08:35:00'),
        (2, 1, 3, N'Yes, we will prioritize your delivery slot.', 1, '2026-05-15T08:40:00'),
        (3, 2, 6, N'My order has arrived, thanks.', 1, '2026-05-15T17:00:00'),
        (4, 2, 4, N'Thank you for your purchase.', 1, '2026-05-15T17:05:00');
    SET IDENTITY_INSERT dbo.chat_messages OFF;

    SET IDENTITY_INSERT dbo.notifications ON;
    INSERT INTO dbo.notifications (notification_id, user_id, type, title, message, action_url, is_read, created_at)
    VALUES
        (1, 5, N'ORDER_UPDATE', N'Order 1 confirmed', N'Your order has been confirmed and is being prepared.', N'/orders/1', 1, '2026-05-15T09:12:00'),
        (2, 5, N'PAYMENT', N'Payment received', N'Payment for order 1 was completed successfully.', N'/payments/1', 1, '2026-05-15T09:14:00'),
        (3, 6, N'ORDER_UPDATE', N'Delivery update', N'Your order 2 is out for delivery.', N'/orders/2', 0, '2026-05-15T14:05:00'),
        (4, 3, N'INVENTORY_ALERT', N'Stock notice', N'Variant 2 is below the preferred threshold.', N'/shop/inventory', 0, '2026-05-16T07:00:00'),
        (5, 4, N'PROMOTION', N'New promotion active', N'MANGO15 is ready for your product page.', N'/shop/promotions', 0, '2026-05-16T07:10:00'),
        (6, 1, N'SYSTEM', N'Settlement batch complete', N'Daily settlement snapshots were created successfully.', N'/admin/settlements', 0, '2026-05-16T20:05:00');
    SET IDENTITY_INSERT dbo.notifications OFF;

    COMMIT;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK;
    THROW;
END CATCH;
GO
