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
        phone NVARCHAR(15) NULL CONSTRAINT UQ_users_phone UNIQUE,
        role NVARCHAR(20) NOT NULL CONSTRAINT CK_users_role DEFAULT 'CUSTOMER' CHECK (role IN ('CUSTOMER', 'SHOP_OWNER', 'DELIVERY', 'ADMIN')),
        status NVARCHAR(20) NOT NULL CONSTRAINT CK_users_status DEFAULT 'INACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'LOCKED', 'SUSPENDED')),
        user_address NVARCHAR(500) NULL,
        is_email_verified BIT NOT NULL CONSTRAINT DF_users_is_email_verified DEFAULT 0,
        email_verification_code_hash NVARCHAR(255) NULL,
        email_verification_expires_at DATETIME NULL,
        email_verification_resend_at DATETIME NULL,
        email_verification_sent_at DATETIME NULL,
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
        review_image_url NVARCHAR(500) NULL,
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
-- Indexes for hot DAO paths
-- =========================================================

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_shop_owner_profiles_approval_status_profile_id' AND object_id = OBJECT_ID(N'dbo.shop_owner_profiles'))
BEGIN
    CREATE INDEX IX_shop_owner_profiles_approval_status_profile_id
        ON dbo.shop_owner_profiles (approval_status, profile_id DESC);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_products_owner_id_product_id_desc' AND object_id = OBJECT_ID(N'dbo.products'))
BEGIN
    CREATE INDEX IX_products_owner_id_product_id_desc
        ON dbo.products (owner_id, product_id DESC);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_products_category_id_product_id_desc' AND object_id = OBJECT_ID(N'dbo.products'))
BEGIN
    CREATE INDEX IX_products_category_id_product_id_desc
        ON dbo.products (category_id, product_id DESC);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_product_images_product_id_display_order' AND object_id = OBJECT_ID(N'dbo.product_images'))
BEGIN
    CREATE INDEX IX_product_images_product_id_display_order
        ON dbo.product_images (product_id, display_order ASC);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_product_variants_product_id_is_active_price' AND object_id = OBJECT_ID(N'dbo.product_variants'))
BEGIN
    CREATE INDEX IX_product_variants_product_id_is_active_price
        ON dbo.product_variants (product_id, is_active, price);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'UX_cart_items_cart_id_variant_id' AND object_id = OBJECT_ID(N'dbo.cart_items'))
BEGIN
    CREATE UNIQUE INDEX UX_cart_items_cart_id_variant_id
        ON dbo.cart_items (cart_id, variant_id);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_cart_items_cart_id_added_at' AND object_id = OBJECT_ID(N'dbo.cart_items'))
BEGIN
    CREATE INDEX IX_cart_items_cart_id_added_at
        ON dbo.cart_items (cart_id, added_at DESC);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_orders_customer_id_order_id_desc' AND object_id = OBJECT_ID(N'dbo.orders'))
BEGIN
    CREATE INDEX IX_orders_customer_id_order_id_desc
        ON dbo.orders (customer_id, order_id DESC);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_orders_owner_id_status_order_id_desc' AND object_id = OBJECT_ID(N'dbo.orders'))
BEGIN
    CREATE INDEX IX_orders_owner_id_status_order_id_desc
        ON dbo.orders (owner_id, status, order_id DESC);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_orders_owner_id_order_id_desc' AND object_id = OBJECT_ID(N'dbo.orders'))
BEGIN
    CREATE INDEX IX_orders_owner_id_order_id_desc
        ON dbo.orders (owner_id, order_id DESC);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_orders_status_order_id_desc' AND object_id = OBJECT_ID(N'dbo.orders'))
BEGIN
    CREATE INDEX IX_orders_status_order_id_desc
        ON dbo.orders (status, order_id DESC);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_order_items_order_id' AND object_id = OBJECT_ID(N'dbo.order_items'))
BEGIN
    CREATE INDEX IX_order_items_order_id
        ON dbo.order_items (order_id, order_item_id);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_order_items_variant_id' AND object_id = OBJECT_ID(N'dbo.order_items'))
BEGIN
    CREATE INDEX IX_order_items_variant_id
        ON dbo.order_items (variant_id, order_item_id);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_promotions_created_by_is_deleted_promo_id_desc' AND object_id = OBJECT_ID(N'dbo.promotions'))
BEGIN
    CREATE INDEX IX_promotions_created_by_is_deleted_promo_id_desc
        ON dbo.promotions (created_by, is_deleted, promo_id DESC);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_promotions_product_scope_active_validity' AND object_id = OBJECT_ID(N'dbo.promotions'))
BEGIN
    CREATE INDEX IX_promotions_product_scope_active_validity
        ON dbo.promotions (product_id, scope, is_active, is_deleted, valid_from, valid_until);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_inventory_logs_variant_id_changed_at_desc' AND object_id = OBJECT_ID(N'dbo.inventory_logs'))
BEGIN
    CREATE INDEX IX_inventory_logs_variant_id_changed_at_desc
        ON dbo.inventory_logs (variant_id, changed_at DESC);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_return_requests_order_id_created_at_desc' AND object_id = OBJECT_ID(N'dbo.return_requests'))
BEGIN
    CREATE INDEX IX_return_requests_order_id_created_at_desc
        ON dbo.return_requests (order_id, created_at DESC);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_return_requests_customer_id_created_at_desc' AND object_id = OBJECT_ID(N'dbo.return_requests'))
BEGIN
    CREATE INDEX IX_return_requests_customer_id_created_at_desc
        ON dbo.return_requests (customer_id, created_at DESC);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_return_requests_status_created_at_desc' AND object_id = OBJECT_ID(N'dbo.return_requests'))
BEGIN
    CREATE INDEX IX_return_requests_status_created_at_desc
        ON dbo.return_requests (status, created_at DESC);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_shop_settlements_owner_id_settlement_id_desc' AND object_id = OBJECT_ID(N'dbo.shop_settlements'))
BEGIN
    CREATE INDEX IX_shop_settlements_owner_id_settlement_id_desc
        ON dbo.shop_settlements (owner_id, settlement_id DESC);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_shop_settlements_status_settlement_id_desc' AND object_id = OBJECT_ID(N'dbo.shop_settlements'))
BEGIN
    CREATE INDEX IX_shop_settlements_status_settlement_id_desc
        ON dbo.shop_settlements (status, settlement_id DESC);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_shop_settlement_orders_settlement_id_order_id' AND object_id = OBJECT_ID(N'dbo.shop_settlement_orders'))
BEGIN
    CREATE INDEX IX_shop_settlement_orders_settlement_id_order_id
        ON dbo.shop_settlement_orders (settlement_id, order_id);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_deliveries_staff_id_status_delivery_id_desc' AND object_id = OBJECT_ID(N'dbo.deliveries'))
BEGIN
    CREATE INDEX IX_deliveries_staff_id_status_delivery_id_desc
        ON dbo.deliveries (staff_id, status, delivery_id DESC);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_deliveries_staff_id_delivery_id_desc' AND object_id = OBJECT_ID(N'dbo.deliveries'))
BEGIN
    CREATE INDEX IX_deliveries_staff_id_delivery_id_desc
        ON dbo.deliveries (staff_id, delivery_id DESC);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_reviews_order_item_id' AND object_id = OBJECT_ID(N'dbo.reviews'))
BEGIN
    CREATE INDEX IX_reviews_order_item_id
        ON dbo.reviews (order_item_id);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_chat_sessions_customer_owner' AND object_id = OBJECT_ID(N'dbo.chat_sessions'))
BEGIN
    CREATE INDEX IX_chat_sessions_customer_owner
        ON dbo.chat_sessions (customer_id, owner_id);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_chat_sessions_customer_id_updated_at_desc' AND object_id = OBJECT_ID(N'dbo.chat_sessions'))
BEGIN
    CREATE INDEX IX_chat_sessions_customer_id_updated_at_desc
        ON dbo.chat_sessions (customer_id, updated_at DESC);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_chat_sessions_owner_id_updated_at_desc' AND object_id = OBJECT_ID(N'dbo.chat_sessions'))
BEGIN
    CREATE INDEX IX_chat_sessions_owner_id_updated_at_desc
        ON dbo.chat_sessions (owner_id, updated_at DESC);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_chat_messages_session_id_is_read_created_at_desc' AND object_id = OBJECT_ID(N'dbo.chat_messages'))
BEGIN
    CREATE INDEX IX_chat_messages_session_id_is_read_created_at_desc
        ON dbo.chat_messages (session_id, is_read, created_at DESC);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_chat_messages_session_id_created_at_desc' AND object_id = OBJECT_ID(N'dbo.chat_messages'))
BEGIN
    CREATE INDEX IX_chat_messages_session_id_created_at_desc
        ON dbo.chat_messages (session_id, created_at DESC);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_notifications_user_id_is_read_created_at_desc' AND object_id = OBJECT_ID(N'dbo.notifications'))
BEGIN
    CREATE INDEX IX_notifications_user_id_is_read_created_at_desc
        ON dbo.notifications (user_id, is_read, created_at DESC);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_notifications_user_id_created_at_desc' AND object_id = OBJECT_ID(N'dbo.notifications'))
BEGIN
    CREATE INDEX IX_notifications_user_id_created_at_desc
        ON dbo.notifications (user_id, created_at DESC);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_payment_transactions_sepay_transaction_id' AND object_id = OBJECT_ID(N'dbo.payment_transactions'))
BEGIN
    CREATE INDEX IX_payment_transactions_sepay_transaction_id
        ON dbo.payment_transactions (sepay_transaction_id);
END
GO

-- =========================================================
-- Seed data
-- =========================================================

BEGIN TRY
    BEGIN TRAN;

    SET IDENTITY_INSERT dbo.users ON;
    INSERT INTO dbo.users (user_id, full_name, email, password_hash, phone, role, status, user_address, is_email_verified, email_verification_code_hash, email_verification_expires_at, email_verification_resend_at, email_verification_sent_at, failed_login_count, locked_until, created_at, updated_at)
    VALUES
        (1, N'Admin System', N'admin@fruitshop.local', N'hash_admin_demo', N'0900000001', N'ADMIN', N'ACTIVE', N'Central admin office', 1, NULL, NULL, NULL, NULL, 0, NULL, '2026-05-01T09:00:00', '2026-05-01T09:00:00'),
        (2, N'Delivery Nguyen', N'delivery@fruitshop.local', N'hash_delivery_demo', N'0900000002', N'DELIVERY', N'ACTIVE', N'Delivery hub, HCMC', 1, NULL, NULL, NULL, NULL, 0, NULL, '2026-05-01T09:05:00', '2026-05-01T09:05:00'),
        (3, N'An Phu Orchard Owner', N'owner1@fruitshop.local', N'hash_owner1_demo', N'0900000003', N'SHOP_OWNER', N'ACTIVE', N'12 Le Loi, District 1, HCMC', 1, NULL, NULL, NULL, NULL, 0, NULL, '2026-05-01T09:10:00', '2026-05-01T09:10:00'),
        (4, N'Mekong Fresh Owner', N'owner2@fruitshop.local', N'hash_owner2_demo', N'0900000004', N'SHOP_OWNER', N'ACTIVE', N'88 Nguyen Trai, District 5, HCMC', 1, NULL, NULL, NULL, NULL, 0, NULL, '2026-05-01T09:15:00', '2026-05-01T09:15:00'),
        (5, N'Tran Minh Customer', N'customer1@fruitshop.local', N'hash_customer1_demo', N'0900000005', N'CUSTOMER', N'ACTIVE', N'15 Pasteur, District 3, HCMC', 1, NULL, NULL, NULL, NULL, 0, NULL, '2026-05-01T09:20:00', '2026-05-01T09:20:00'),
        (6, N'Le Thu Customer', N'customer2@fruitshop.local', N'hash_customer2_demo', N'0900000006', N'CUSTOMER', N'ACTIVE', N'90 Truong Chinh, Tan Binh, HCMC', 1, NULL, NULL, NULL, NULL, 0, NULL, '2026-05-01T09:25:00', '2026-05-01T09:25:00'),
        (7, N'Klever Premium Owner', N'owner3@fruitshop.local', N'hash_owner3_demo', N'0900000007', N'SHOP_OWNER', N'ACTIVE', N'52 Vo Thi Sau, District 3, HCMC', 1, NULL, NULL, NULL, NULL, 0, NULL, '2026-05-01T09:30:00', '2026-05-01T09:30:00'),
        (10, N'Nguyễn Văn Hùng', N'hungnv@gmail.com', N'hash', N'0912345601', N'CUSTOMER', N'ACTIVE', N'12 Phố Cổ, Hà Nội', 1, NULL, NULL, NULL, NULL, 0, NULL, GETDATE(), GETDATE()),
        (11, N'Phạm Minh Tuấn', N'tuanpm@gmail.com', N'hash', N'0912345602', N'CUSTOMER', N'ACTIVE', N'85 Xuân Thủy, Cầu Giấy', 1, NULL, NULL, NULL, NULL, 0, NULL, GETDATE(), GETDATE()),
        (12, N'Trần Thị Mai', N'maitt@gmail.com', N'hash', N'0912345603', N'CUSTOMER', N'ACTIVE', N'45 Chùa Bộc, Đống Đa', 1, NULL, NULL, NULL, NULL, 0, NULL, GETDATE(), GETDATE()),
        (13, N'Lê Hoàng Nam', N'namlh@gmail.com', N'hash', N'0912345604', N'CUSTOMER', N'ACTIVE', N'102 Nguyễn Trãi, Thanh Xuân', 1, NULL, NULL, NULL, NULL, 0, NULL, GETDATE(), GETDATE()),
        (14, N'Đỗ Thùy Chi', N'chidt@gmail.com', N'hash', N'0912345605', N'CUSTOMER', N'ACTIVE', N'56 Bạch Mai, Hai Bà Trưng', 1, NULL, NULL, NULL, NULL, 0, NULL, GETDATE(), GETDATE()),
        (15, N'Vũ Quốc Anh', N'anhvq@gmail.com', N'hash', N'0912345606', N'CUSTOMER', N'ACTIVE', N'29 Lạc Long Quân, Tây Hồ', 1, NULL, NULL, NULL, NULL, 0, NULL, GETDATE(), GETDATE());
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
        (1, N'Cam, Bưởi, Quýt', N'citrus', 1, 1),
        (2, N'Trái Cây Nhiệt Đới', N'tropical', 2, 1),
        (3, N'Quả Mọng & Dâu Tây', N'berries', 3, 1),
        (4, N'Hộp Quà Trái Cây', N'gift-boxes', 4, 1),
        (5, N'Táo Cao Cấp', N'apples', 5, 1),
        (6, N'Nho Không Hạt', N'grapes', 6, 1),
        (7, N'Dưa Lưới & Dưa Hấu', N'melons', 7, 1),
        (8, N'Kiwi Tươi', N'kiwi', 8, 1),
        (9, N'Cherry Nhập Khẩu', N'cherries', 9, 1),
        (10, N'Trái Cây Hỗn Hợp', N'imported-mix', 10, 1);
    SET IDENTITY_INSERT dbo.categories OFF;

    SET IDENTITY_INSERT dbo.products ON;
    INSERT INTO dbo.products (product_id, owner_id, category_id, name, description, origin_country, origin_region, harvest_date, shelf_life_days, storage_instruction, status, view_count, rating, sold_quantity, created_at, updated_at)
    VALUES
        -- Category 1: Citrus
        (1, 3, 1, N'Cam Sành Cao Phong Hòa Bình', N'Cam sành Cao Phong nổi tiếng vỏ mỏng, mọng nước, vị ngọt thanh tự nhiên xen lẫn chua nhẹ thanh mát. Giàu Vitamin C, rất phù hợp làm nước ép giải nhiệt.', N'Việt Nam', N'Cao Phong, Hòa Bình', '2026-05-18', 12, N'Bảo quản mát ở nhiệt độ 6-10 độ C', N'ACTIVE', 1820, 4.80, 220, '2026-05-03T09:00:00', GETDATE()),
        (2, 3, 1, N'Bưởi Da Xanh Bến Tre loại đặc biệt', N'Bưởi da xanh ruột hồng tôm bưởi mọng nước, ráo nước, vị ngọt đậm đà không đắng, vỏ mỏng cực kỳ dễ bóc. Đạt tiêu chuẩn xuất khẩu.', N'Việt Nam', N'Chợ Lách, Bến Tre', '2026-05-06', 20, N'Bảo quản nơi khô ráo thoáng mát', N'ACTIVE', 1140, 4.92, 140, '2026-05-03T09:05:00', GETDATE()),
        -- Category 2: Tropical
        (3, 3, 2, N'Chuối Lùn Laba Đà Lạt', N'Chuối Laba trứ danh dẻo thơm, ngọt đậm, giàu chất xơ và kali. Sản xuất theo chuẩn hữu cơ không hóa chất, an toàn tuyệt đối cho trẻ nhỏ.', N'Việt Nam', N'Đức Trọng, Lâm Đồng', '2026-05-12', 7, N'Tránh ánh nắng trực tiếp, không để tủ lạnh khi chưa chín', N'ACTIVE', 980, 4.60, 260, '2026-05-03T09:10:00', GETDATE()),
        (4, 4, 2, N'Xoài Cát Hòa Lộc Tiền Giang', N'Đệ nhất xoài cát miền Tây, quả thon dài, khi chín vỏ vàng ươm, thịt quả vàng đậm, mịn không xơ, vị ngọt lịm thơm lừng khó quên.', N'Việt Nam', N'Cái Bè, Tiền Giang', '2026-05-07', 6, N'Bảo quản nhiệt độ phòng khi chín, giữ mát sau khi bổ', N'ACTIVE', 2150, 4.95, 180, '2026-05-03T09:15:00', GETDATE()),
        -- Category 3: Berries
        (5, 4, 3, N'Dâu Tây Đỏ Mỹ Nhập Khẩu Premium', N'Dâu tây đỏ nhập khẩu trực tiếp từ Mỹ, quả to đều, đỏ mọng nước, vị chua ngọt hài hòa đặc trưng, cùi dầy giòn ngọt thơm lừng.', N'Hoa Kỳ', N'California', '2026-05-10', 5, N'Giữ lạnh liên tục ở 2-4 độ C trong khay kín', N'ACTIVE', 760, 4.70, 96, '2026-05-03T09:20:00', GETDATE()),
        -- Category 7: Melons
        (6, 4, 7, N'Dưa Lưới Tươi Ruột Vàng VietGAP', N'Dưa lưới giống Nhật trồng nhà màng công nghệ cao, vỏ lưới dày đẹp mắt, cùi màu cam ruột vàng, giòn tan ngọt sắc mát lịm giải nhiệt.', N'Việt Nam', N'Đà Lạt, Lâm Đồng', '2026-05-09', 10, N'Giữ lạnh sau khi cắt để tăng độ giòn ngọt', N'ACTIVE', 840, 4.85, 72, '2026-05-03T09:25:00', GETDATE()),
        (7, 7, 7, N'Dưa Hấu Vuông Độc Lạ Tài Lộc', N'Quả dưa hấu được tạo hình vuông độc đáo mang ý nghĩa phong thủy may mắn tài lộc. Vỏ xanh thẫm mịn màng, ruột đỏ ngọt mát ít hạt.', N'Việt Nam', N'Vĩnh Long', '2026-05-11', 30, N'Trưng bày nơi khô ráo hoặc trữ lạnh ăn dần', N'ACTIVE', 1460, 4.78, 88, '2026-05-03T09:26:00', GETDATE()),
        -- Category 9: Cherries (Quả anh đào)
        (8, 7, 9, N'Cherry Đỏ Chile Nhập Khẩu Size Lớn', N'Cherry nhập khẩu chính ngạch từ nhà vườn Chile danh tiếng. Quả cứng trái màu đỏ sẫm óng ả, cuống tươi xanh, thịt ngọt giòn đậm đà.', N'Chile', N'O''Higgins', '2026-05-10', 7, N'Trữ lạnh ở 0-2 độ C, rửa sạch trước khi ăn', N'ACTIVE', 1325, 4.87, 64, '2026-05-03T09:27:00', GETDATE()),
        (9, 7, 9, N'Cherry Mỹ Premium Orchard View', N'Dòng Cherry Mỹ thượng hạng trứ danh thế giới từ thương hiệu Orchard View. Trái to vượt trội, giòn đôm đốp, ngọt lịm ngây ngất.', N'Hoa Kỳ', N'Washington', '2026-05-09', 6, N'Trữ tủ mát liên tục, tránh đè nén làm dập quả', N'ACTIVE', 1188, 4.90, 55, '2026-05-03T09:28:00', GETDATE()),
        -- Category 6: Grapes (Nho không hạt)
        (10, 7, 6, N'Nho Xanh Mẫu Đơn Không Hạt Chile', N'Nho xanh mẫu đơn không hạt Chile, chùm nho dày đặc quả tròn căng mọng, vỏ mỏng giòn sần sật, vị ngọt mát thơm hương sữa đặc trưng.', N'Chile', N'Maipo Valley', '2026-05-09', 12, N'Trữ mát tủ lạnh, không rửa trước khi cất trữ', N'ACTIVE', 1040, 4.83, 61, '2026-05-03T09:29:00', GETDATE()),
        -- Category 4: Gift Boxes (Hộp quà trái cây)
        (11, 7, 4, N'Hộp Quà Tết An Khang Phú Quý', N'Hộp quà tết sang trọng tinh tế kết hợp các loại trái cây nhập khẩu tươi ngon nhất. Món quà sức khỏe ý nghĩa kính tặng gia đình và đối tác.', N'Nhiều nước', N'Hộp quà cao cấp', '2026-05-08', 5, N'Bảo quản mát toàn bộ hộp quà tránh va đập', N'ACTIVE', 1575, 4.94, 44, '2026-05-03T09:30:00', GETDATE()),
        (12, 7, 4, N'Hộp Quà Trái Cây Tết Thịnh Vượng', N'Hộp quà tết rực rỡ màu sắc cát tường từ các loại quả cao cấp: dâu tây, táo, cam. Thiết kế hộp gỗ lót lụa sang trọng đẳng cấp.', N'Nhiều nước', N'Thiết kế VIP', '2026-05-09', 5, N'Giữ lạnh để giữ độ tươi ngon cao nhất', N'ACTIVE', 1210, 4.81, 73, '2026-05-03T09:31:00', GETDATE()),
        -- Category 10: Imported Mix (Trái cây hỗn hợp)
        (13, 7, 10, N'Giỏ Quả Trái Cây Sum Họp Ấm Áp', N'Giỏ trái cây đầy đặn sung túc tượng trưng cho gia đình đoàn viên ấm áp. Bao gồm lê Hàn Quốc, táo Mỹ, bưởi hồng đặc sản.', N'Nhiều nước', N'Lắp ráp thủ công', '2026-05-09', 6, N'Để nơi thoáng mát hoặc ngăn mát tủ lạnh', N'ACTIVE', 990, 4.79, 58, '2026-05-03T09:32:00', GETDATE()),
        (14, 7, 10, N'Giỏ Trái Cây Cát Tường Như Ý', N'Thiết kế giỏ quà kết hợp hoa tươi và trái cây nhập khẩu tinh tế. Thích hợp làm quà chúc mừng, khai trương, thăm hỏi cao cấp.', N'Nhiều nước', N'Giỏ hoa nghệ thuật', '2026-05-10', 4, N'Tránh nơi nhiệt độ cao, tưới ẩm hoa nhẹ nhàng', N'ACTIVE', 690, 4.88, 39, '2026-05-03T09:33:00', GETDATE()),
        (15, 7, 10, N'Giỏ Trái Cây Tài Lộc Thịnh Vượng', N'Giỏ quả được kết hợp tỉ mỉ từ nho đen ngón tay, lê vàng, hồng sấy thượng hạng đem lại tài lộc và phú quý cho người nhận.', N'Nhiều nước', N'Lắp ráp VIP', '2026-05-10', 6, N'Trữ mát để duy trì độ tươi giòn của quả', N'ACTIVE', 845, 4.84, 47, '2026-05-03T09:34:00', GETDATE()),
        -- Category 5: Apples (Táo cao cấp)
        (16, 7, 5, N'Táo Envy Mỹ Nhập Khẩu Premium', N'Táo Envy Mỹ giòn ngọt vượt trội, thịt táo trắng tinh khiết ít bị thâm khi cắt, hương thơm nồng nàn quyến rũ đặc trưng khó quên.', N'Hoa Kỳ', N'Washington', '2026-05-18', 15, N'Bảo quản mát ở nhiệt độ 2-4 độ C', N'ACTIVE', 1520, 4.88, 125, '2026-05-03T09:35:00', GETDATE()),
        -- Category 8: Kiwi (Kiwi tươi)
        (17, 7, 8, N'Kiwi Vàng New Zealand Zespri', N'Kiwi vàng Zespri thượng hạng từ New Zealand, vỏ mịn không lông, thịt quả màu vàng óng ả mọng nước, vị ngọt lịm như mật ong xen lẫn chua dịu thanh khiết.', N'New Zealand', N'Bay of Plenty', '2026-05-19', 14, N'Bảo quản mát tủ lạnh, ăn ngon hơn khi chín mềm tay', N'ACTIVE', 1380, 4.90, 110, '2026-05-03T09:36:00', GETDATE());
    SET IDENTITY_INSERT dbo.products OFF;

    SET IDENTITY_INSERT dbo.product_images ON;
    INSERT INTO dbo.product_images (image_id, product_id, file_path, display_order, is_primary, uploaded_at)
    VALUES
        (1, 1, N'https://images.unsplash.com/photo-1611080626919-7cf5a9dbab5b?w=600&auto=format&fit=crop&q=80', 1, 1, GETDATE()),
        (2, 2, N'https://images.unsplash.com/photo-1557800636-894a64c1696f?w=600&auto=format&fit=crop&q=80', 1, 1, GETDATE()),
        (3, 3, N'https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=600&auto=format&fit=crop&q=80', 1, 1, GETDATE()),
        (4, 4, N'https://images.unsplash.com/photo-1553279768-865429fa0078?w=600&auto=format&fit=crop&q=80', 1, 1, GETDATE()),
        (5, 5, N'https://images.unsplash.com/photo-1464965911861-746a04b4bca6?w=600&auto=format&fit=crop&q=80', 1, 1, GETDATE()),
        (6, 5, N'https://images.unsplash.com/photo-1518635017498-87f514b751ba?w=600&auto=format&fit=crop&q=80', 2, 0, GETDATE()),
        (7, 6, N'https://images.unsplash.com/photo-1595855759920-86582396756a?w=600&auto=format&fit=crop&q=80', 1, 1, GETDATE()),
        (8, 6, N'https://images.unsplash.com/photo-1571772996211-2f02c9727629?w=600&auto=format&fit=crop&q=80', 2, 0, GETDATE()),
        (9, 7, N'https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=600&auto=format&fit=crop&q=80', 1, 1, GETDATE()),
        (10, 8, N'https://images.unsplash.com/photo-1527661591475-527312dd65f5?w=600&auto=format&fit=crop&q=80', 1, 1, GETDATE()),
        (11, 9, N'https://images.unsplash.com/photo-1559181567-c3190ca9959b?w=600&auto=format&fit=crop&q=80', 1, 1, GETDATE()),
        (12, 10, N'https://images.unsplash.com/photo-1537640538966-79f369143f8f?w=600&auto=format&fit=crop&q=80', 1, 1, GETDATE()),
        (13, 11, N'https://images.unsplash.com/photo-1544816155-12df9643f363?w=600&auto=format&fit=crop&q=80', 1, 1, GETDATE()),
        (14, 11, N'https://images.unsplash.com/photo-1607344645866-009c320c5ab8?w=600&auto=format&fit=crop&q=80', 2, 0, GETDATE()),
        (15, 12, N'https://images.unsplash.com/photo-1607344645866-009c320c5ab8?w=600&auto=format&fit=crop&q=80', 1, 1, GETDATE()),
        (16, 12, N'https://images.unsplash.com/photo-1544816155-12df9643f363?w=600&auto=format&fit=crop&q=80', 2, 0, GETDATE()),
        (17, 13, N'https://images.unsplash.com/photo-1544816155-12df9643f363?w=600&auto=format&fit=crop&q=80', 1, 1, GETDATE()),
        (18, 14, N'https://images.unsplash.com/photo-1607344645866-009c320c5ab8?w=600&auto=format&fit=crop&q=80', 1, 1, GETDATE()),
        (19, 15, N'https://images.unsplash.com/photo-1607344645866-009c320c5ab8?w=600&auto=format&fit=crop&q=80', 1, 1, GETDATE()),
        (20, 15, N'https://images.unsplash.com/photo-1544816155-12df9643f363?w=600&auto=format&fit=crop&q=80', 2, 0, GETDATE()),
        -- Táo Envy Mỹ (16)
        (21, 16, N'https://images.unsplash.com/photo-1619546813926-a78fa6372cd2?w=600&auto=format&fit=crop&q=80', 1, 1, GETDATE()),
        -- Kiwi Vàng (17)
        (22, 17, N'https://images.unsplash.com/photo-1585052201332-b8c0ce30972f?w=600&auto=format&fit=crop&q=80', 1, 1, GETDATE());
    SET IDENTITY_INSERT dbo.product_images OFF;

    SET IDENTITY_INSERT dbo.product_variants ON;
    INSERT INTO dbo.product_variants (variant_id, product_id, sku, variant_label, price, stock_quantity, is_active, created_at, updated_at)
    VALUES
        (1, 1, N'CAM-SANH-1KG', N'Hộp 1kg', 35000.00, 150, 1, '2026-05-03T09:40:00', '2026-05-16T08:10:00'),
        (2, 1, N'CAM-SANH-3KG', N'Combo 3kg', 95000.00, 80, 1, '2026-05-03T09:40:00', '2026-05-16T08:10:00'),
        (3, 2, N'BUOI-DX-1P2KG', N'Quả 1.2kg - 1.4kg', 65000.00, 120, 1, '2026-05-03T09:41:00', '2026-05-16T08:10:00'),
        (4, 2, N'BUOI-DX-1P5KG', N'Quả VIP >1.5kg', 85000.00, 60, 1, '2026-05-03T09:41:00', '2026-05-16T08:10:00'),
        (5, 3, N'CHUOI-LABA-1KG', N'Nải 1kg', 28000.00, 200, 1, '2026-05-03T09:42:00', '2026-05-16T08:10:00'),
        (6, 4, N'XOAI-CAT-1KG', N'Hộp 1kg', 89000.00, 90, 1, '2026-05-03T09:43:00', '2026-05-16T08:10:00'),
        (7, 4, N'XOAI-CAT-2KG', N'Hộp 2kg VIP', 169000.00, 45, 1, '2026-05-03T09:43:00', '2026-05-16T08:10:00'),
        (8, 5, N'DAU-TAY-250G', N'Hộp 250g', 125000.00, 75, 1, '2026-05-03T09:44:00', '2026-05-16T08:10:00'),
        (9, 5, N'DAU-TAY-500G', N'Hộp 500g', 239000.00, 40, 1, '2026-05-03T09:44:00', '2026-05-16T08:10:00'),
        (10, 6, N'DUA-LUOI-1QT', N'Quả 1.5kg', 90000.00, 110, 1, '2026-05-03T09:45:00', '2026-05-16T08:10:00'),
        (11, 7, N'DUA-HAU-VUONG', N'Quả vuông vẽ chữ', 299000.00, 30, 1, '2026-05-03T09:46:00', '2026-05-16T08:10:00'),
        (12, 8, N'CHERRY-CL-250G', N'Hộp 250g', 149000.00, 85, 1, '2026-05-03T09:47:00', '2026-05-16T08:10:00'),
        (13, 8, N'CHERRY-CL-500G', N'Hộp 500g', 289000.00, 50, 1, '2026-05-03T09:47:00', '2026-05-16T08:10:00'),
        (14, 8, N'CHERRY-CL-1KG', N'Hộp 1kg Premium', 550000.00, 25, 1, '2026-05-03T09:47:00', '2026-05-16T08:10:00'),
        (15, 9, N'CHERRY-US-500G', N'Hộp 500g Orchard', 320000.00, 60, 1, '2026-05-03T09:48:00', '2026-05-16T08:10:00'),
        (16, 9, N'CHERRY-US-1KG', N'Thùng 1kg VIP', 620000.00, 30, 1, '2026-05-03T09:48:00', '2026-05-16T08:10:00'),
        (17, 10, N'NHO-MD-500G', N'Khay 500g', 179000.00, 95, 1, '2026-05-03T09:49:00', '2026-05-16T08:10:00'),
        (18, 10, N'NHO-MD-1KG', N'Hộp 1kg', 345000.00, 50, 1, '2026-05-03T09:49:00', '2026-05-16T08:10:00'),
        (19, 11, N'HQ-AN-KHANG', N'Hộp gỗ Premium', 680000.00, 35, 1, '2026-05-03T09:50:00', '2026-05-16T08:10:00'),
        (20, 12, N'HQ-THINH-VUONG', N'Hộp lụa VIP', 880000.00, 28, 1, '2026-05-03T09:51:00', '2026-05-16T08:10:00'),
        (21, 13, N'GQ-SUM-HOP', N'Giỏ lục bình lớn', 550000.00, 40, 1, '2026-05-03T09:52:00', '2026-05-16T08:10:00'),
        (22, 14, N'GQ-CAT-TUONG', N'Giỏ tre hoa tươi', 750000.00, 25, 1, '2026-05-03T09:53:00', '2026-05-16T08:10:00'),
        (23, 15, N'GQ-TAI-LOC', N'Giỏ VIP đặc biệt', 1250000.00, 20, 1, '2026-05-03T09:54:00', '2026-05-16T08:10:00'),
        -- Táo Envy Mỹ (16)
        (24, 16, N'TAO-ENVY-1KG', N'Hộp 1kg', 119000.00, 120, 1, GETDATE(), GETDATE()),
        (25, 16, N'TAO-ENVY-3KG', N'Combo 3kg', 329000.00, 50, 1, GETDATE(), GETDATE()),
        -- Kiwi Vàng (17)
        (26, 17, N'KIWI-VANG-1KG', N'Hộp 1kg', 145000.00, 90, 1, GETDATE(), GETDATE()),
        (27, 17, N'KIWI-VANG-3KG', N'Combo 3kg', 399000.00, 40, 1, GETDATE(), GETDATE());
    SET IDENTITY_INSERT dbo.product_variants OFF;

    SET IDENTITY_INSERT dbo.inventory_logs ON;
    INSERT INTO dbo.inventory_logs (log_id, variant_id, changed_by, change_type, quantity_delta, quantity_after, note, changed_at)
    VALUES
        (1, 1, 3, N'MANUAL_ADJUST', 150, 150, N'Nhập kho ban đầu', '2026-05-12T08:00:00'),
        (2, 3, 3, N'MANUAL_ADJUST', 120, 120, N'Nhập kho đầu mùa bưởi', '2026-05-15T09:05:00'),
        (3, 8, 4, N'MANUAL_ADJUST', 75, 75, N'Nhập lô dâu tây Mỹ kiểm dịch', '2026-05-16T08:20:00'),
        (4, 12, 7, N'MANUAL_ADJUST', 85, 85, N'Nhập container Cherry Chile', '2026-05-15T10:00:00'),
        (5, 19, 7, N'MANUAL_ADJUST', 35, 35, N'Đóng hộp quà tết', '2026-05-14T18:00:00'),
        (6, 24, 7, N'MANUAL_ADJUST', 120, 120, N'Nhập kho táo Envy Mỹ', '2026-05-18T09:00:00'),
        (7, 26, 7, N'MANUAL_ADJUST', 90, 90, N'Nhập kho Kiwi Vàng', '2026-05-19T09:00:00');
    SET IDENTITY_INSERT dbo.inventory_logs OFF;

    SET IDENTITY_INSERT dbo.promotions ON;
    INSERT INTO dbo.promotions (promo_id, code, discount_type, discount_scope, discount_max, discount_value, min_order_value, scope, product_id, max_uses, used_count, can_stack, valid_from, valid_until, created_by, created_at, updated_at, is_deleted, is_active)
    VALUES
        (1, N'FLASHSALE-DAUTAY', N'FIXED', N'ALL', 0.00, 30000.00, 100000.00, N'PRODUCT', 5, 200, 12, 1, '2026-01-01T00:00:00', '2026-12-31T23:59:59', 1, '2026-05-01T10:00:00', '2026-05-16T08:00:00', 0, 1),
        (2, N'FLASHSALE-CHERRYCL', N'PERCENT', N'ALL', 100000.00, 20.00, 150000.00, N'PRODUCT', 8, 150, 8, 1, '2026-01-01T00:00:00', '2026-12-31T23:59:59', 1, '2026-05-01T10:05:00', '2026-05-16T08:00:00', 0, 1),
        (3, N'FLASHSALE-DUAVUONG', N'PERCENT', N'ALL', 50000.00, 15.00, 200000.00, N'PRODUCT', 7, 100, 3, 1, '2026-01-01T00:00:00', '2026-12-31T23:59:59', 1, '2026-05-01T10:10:00', '2026-05-16T08:00:00', 0, 1),
        (4, N'FLASHSALE-NHOMD', N'FIXED', N'ALL', 0.00, 50000.00, 150000.00, N'PRODUCT', 10, 120, 5, 1, '2026-01-01T00:00:00', '2026-12-31T23:59:59', 1, '2026-05-01T10:15:00', '2026-05-16T08:00:00', 0, 1),
        (5, N'WELCOME10', N'PERCENT', N'ALL', 50000.00, 10.00, 120000.00, N'ORDER', NULL, 1000, 36, 0, '2026-01-01T00:00:00', '2026-12-31T23:59:59', 1, '2026-05-01T10:20:00', '2026-05-16T08:00:00', 0, 1),
        (6, N'FREESHIP50', N'FIXED', N'ALL', 0.00, 20000.00, 300000.00, N'ORDER', NULL, 500, 45, 0, '2026-01-01T00:00:00', '2026-12-31T23:59:59', 1, '2026-05-01T10:25:00', '2026-05-16T08:00:00', 0, 1),
        (7, N'FLASHSALE-CAMSANH', N'PERCENT', N'ALL', 20000.00, 20.00, 50000.00, N'PRODUCT', 1, 300, 0, 1, '2026-01-01T00:00:00', '2026-12-31T23:59:59', 1, '2026-05-01T10:30:00', '2026-05-16T08:00:00', 0, 1),
        (8, N'FLASHSALE-BUOI', N'FIXED', N'ALL', 0.00, 15000.00, 80000.00, N'PRODUCT', 2, 200, 0, 1, '2026-01-01T00:00:00', '2026-12-31T23:59:59', 1, '2026-05-01T10:35:00', '2026-05-16T08:00:00', 0, 1),
        (9, N'FLASHSALE-XOAI', N'PERCENT', N'ALL', 30000.00, 10.00, 100000.00, N'PRODUCT', 4, 150, 0, 1, '2026-01-01T00:00:00', '2026-12-31T23:59:59', 1, '2026-05-01T10:40:00', '2026-05-16T08:00:00', 0, 1),
        (10, N'FLASHSALE-TAOENVY', N'PERCENT', N'ALL', 40000.00, 15.00, 120000.00, N'PRODUCT', 16, 250, 0, 1, '2026-01-01T00:00:00', '2026-12-31T23:59:59', 1, '2026-05-01T10:45:00', '2026-05-16T08:00:00', 0, 1),
        (11, N'FLASHSALE-KIWI', N'FIXED', N'ALL', 0.00, 25000.00, 100000.00, N'PRODUCT', 17, 180, 0, 1, '2026-01-01T00:00:00', '2026-12-31T23:59:59', 1, '2026-05-01T10:50:00', '2026-05-16T08:00:00', 0, 1),
        -- Voucher của Shop (discount_scope = 'SHOP') — hiển thị trên trang chi tiết sản phẩm
        (12, N'ANPHU-GIAM30K', N'FIXED', N'SHOP', 0.00, 30000.00, 200000.00, N'ORDER', NULL, 500, 17, 0, '2026-01-01T00:00:00', '2026-12-31T23:59:59', 3, '2026-05-02T08:00:00', '2026-05-16T08:00:00', 0, 1),
        (13, N'ANPHU-GIAM15P', N'PERCENT', N'SHOP', 50000.00, 15.00, 350000.00, N'ORDER', NULL, 300, 8, 0, '2026-01-01T00:00:00', '2026-12-31T23:59:59', 3, '2026-05-02T08:05:00', '2026-05-16T08:00:00', 0, 1),
        (14, N'MEKONG-GIAM20K', N'FIXED', N'SHOP', 0.00, 20000.00, 150000.00, N'ORDER', NULL, 400, 12, 0, '2026-01-01T00:00:00', '2026-12-31T23:59:59', 4, '2026-05-03T08:00:00', '2026-05-16T08:00:00', 0, 1),
        (15, N'MEKONG-GIAM10P', N'PERCENT', N'SHOP', 40000.00, 10.00, 250000.00, N'ORDER', NULL, 350, 5, 0, '2026-01-01T00:00:00', '2026-12-31T23:59:59', 4, '2026-05-03T08:05:00', '2026-05-16T08:00:00', 0, 1),
        (16, N'KLEVER-GIAM50K', N'FIXED', N'SHOP', 0.00, 50000.00, 400000.00, N'ORDER', NULL, 250, 3, 0, '2026-01-01T00:00:00', '2026-12-31T23:59:59', 7, '2026-05-04T08:00:00', '2026-05-16T08:00:00', 0, 1),
        (17, N'KLEVER-GIAM20P', N'PERCENT', N'SHOP', 80000.00, 20.00, 500000.00, N'ORDER', NULL, 200, 1, 0, '2026-01-01T00:00:00', '2026-12-31T23:59:59', 7, '2026-05-04T08:05:00', '2026-05-16T08:00:00', 0, 1);
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
        (3, 2, 6, 1, '2026-05-15T09:12:00'),
        (4, 2, 8, 1, '2026-05-15T09:12:00');
    SET IDENTITY_INSERT dbo.cart_items OFF;

    SET IDENTITY_INSERT dbo.orders ON;
    INSERT INTO dbo.orders (order_id, customer_id, owner_id, delivery_address, user_address, delivery_time_slot, notes, cancelled_at, cancelled_by, cancellation_reason, status, total_amount, delivery_fee, discount_amount, system_discount_amount, shop_discount_amount, platform_fee, final_amount, payment_method, refund_status, created_at, updated_at)
    VALUES
        (1, 5, 3, N'15 Pasteur, District 3, HCMC', N'15 Pasteur, District 3, HCMC', N'08:00-12:00', N'Leave at reception', NULL, NULL, NULL, N'DELIVERED', 130000.00, 15000.00, 13000.00, 10000.00, 3000.00, 6500.00, 132000.00, N'CK', N'NONE', '2026-05-15T09:10:00', '2026-05-16T12:30:00'),
        (2, 6, 4, N'90 Truong Chinh, Tan Binh, HCMC', N'90 Truong Chinh, Tan Binh, HCMC', N'14:00-18:00', N'Call on arrival', NULL, NULL, NULL, N'DELIVERED', 214000.00, 20000.00, 15000.00, 0.00, 15000.00, 10700.00, 219000.00, N'COD', N'NONE', '2026-05-15T10:20:00', '2026-05-16T13:10:00'),
        (3, 5, 3, N'15 Pasteur, District 3, HCMC', N'15 Pasteur, District 3, HCMC', N'18:00-21:00', N'Ring the bell twice', NULL, NULL, NULL, N'DELIVERED', 142000.00, 12000.00, 14200.00, 14200.00, 0.00, 7100.00, 139800.00, N'CK', N'PENDING', '2026-05-16T08:00:00', '2026-05-16T18:00:00'),
        (10, 10, 3, N'12 Phố Cổ, Hà Nội', N'12 Phố Cổ, Hà Nội', NULL, NULL, NULL, NULL, NULL, N'DELIVERED', 35000.00, 15000.00, 0.00, 0.00, 0.00, 1750.00, 50000.00, N'COD', N'NONE', '2026-05-18T08:00:00', '2026-05-18T14:00:00'),
        (11, 11, 3, N'85 Xuân Thủy, Cầu Giấy', N'85 Xuân Thủy, Cầu Giấy', NULL, NULL, NULL, NULL, NULL, N'DELIVERED', 35000.00, 15000.00, 0.00, 0.00, 0.00, 1750.00, 50000.00, N'COD', N'NONE', '2026-05-19T09:00:00', '2026-05-19T15:00:00'),
        (12, 12, 3, N'45 Chùa Bộc, Đống Đa', N'45 Chùa Bộc, Đống Đa', NULL, NULL, NULL, NULL, NULL, N'DELIVERED', 95000.00, 20000.00, 0.00, 0.00, 0.00, 4750.00, 115000.00, N'CK', N'NONE', '2026-05-20T10:00:00', '2026-05-20T16:00:00'),
        (13, 13, 3, N'102 Nguyễn Trãi, Thanh Xuân', N'102 Nguyễn Trãi, Thanh Xuân', NULL, NULL, NULL, NULL, NULL, N'DELIVERED', 35000.00, 15000.00, 0.00, 0.00, 0.00, 1750.00, 50000.00, N'COD', N'NONE', '2026-05-21T11:00:00', '2026-05-21T17:00:00'),
        (14, 14, 3, N'56 Bạch Mai, Hai Bà Trưng', N'56 Bạch Mai, Hai Bà Trưng', NULL, NULL, NULL, NULL, NULL, N'DELIVERED', 35000.00, 15000.00, 0.00, 0.00, 0.00, 1750.00, 50000.00, N'CK', N'NONE', '2026-05-22T13:00:00', '2026-05-22T19:00:00'),
        (15, 15, 3, N'29 Lạc Long Quân, Tây Hồ', N'29 Lạc Long Quân, Tây Hồ', NULL, NULL, NULL, NULL, NULL, N'DELIVERED', 95000.00, 20000.00, 0.00, 0.00, 0.00, 4750.00, 115000.00, N'COD', N'NONE', '2026-05-23T08:00:00', '2026-05-23T12:00:00');
    SET IDENTITY_INSERT dbo.orders OFF;

    SET IDENTITY_INSERT dbo.order_items ON;
    INSERT INTO dbo.order_items (order_item_id, order_id, variant_id, product_name_snapshot, variant_label_snapshot, quantity, unit_price, subtotal)
    VALUES
        (1, 1, 1, N'Cam Sành Cao Phong Hòa Bình', N'Hộp 1kg', 1, 35000.00, 35000.00),
        (2, 1, 2, N'Cam Sành Cao Phong Hòa Bình', N'Combo 3kg', 1, 95000.00, 95000.00),
        (3, 2, 6, N'Xoài Cát Hòa Lộc Tiền Giang', N'Hộp 1kg', 1, 89000.00, 89000.00),
        (4, 2, 8, N'Dâu Tây Đỏ Mỹ Nhập Khẩu Premium', N'Hộp 250g', 1, 125000.00, 125000.00),
        (5, 3, 3, N'Bưởi Da Xanh Bến Tre loại đặc biệt', N'Quả 1.2kg - 1.4kg', 1, 86000.00, 86000.00),
        (6, 3, 5, N'Chuối Lùn Laba Đà Lạt', N'Nải 1kg', 2, 28000.00, 56000.00),
        (10, 10, 1, N'Cam Sành Cao Phong Hòa Bình', N'Hộp 1kg', 1, 35000.00, 35000.00),
        (11, 11, 1, N'Cam Sành Cao Phong Hòa Bình', N'Hộp 1kg', 1, 35000.00, 35000.00),
        (12, 12, 2, N'Cam Sành Cao Phong Hòa Bình', N'Combo 3kg', 1, 95000.00, 95000.00),
        (13, 13, 1, N'Cam Sành Cao Phong Hòa Bình', N'Hộp 1kg', 1, 35000.00, 35000.00),
        (14, 14, 1, N'Cam Sành Cao Phong Hòa Bình', N'Hộp 1kg', 1, 35000.00, 35000.00),
        (15, 15, 2, N'Cam Sành Cao Phong Hòa Bình', N'Combo 3kg', 1, 95000.00, 95000.00);
    SET IDENTITY_INSERT dbo.order_items OFF;

    SET IDENTITY_INSERT dbo.order_promotions ON;
    INSERT INTO dbo.order_promotions (usage_id, order_id, promo_id, customer_id, discount_applied, used_at)
    VALUES
        (1, 1, 5, 5, 13000.00, '2026-05-15T09:20:00'),
        (2, 2, 1, 6, 15000.00, '2026-05-15T10:30:00'),
        (3, 3, 5, 5, 14200.00, '2026-05-16T08:10:00');
    SET IDENTITY_INSERT dbo.order_promotions OFF;

    SET IDENTITY_INSERT dbo.return_requests ON;
    INSERT INTO dbo.return_requests (return_request_id, order_id, order_item_id, customer_id, request_type, reason_code, description, evidence_url, requested_quantity, resolution_type, replacement_variant_id, refund_amount, status, decided_by, decision_reason, resolved_at, created_at, updated_at)
    VALUES
        (1, 3, 6, 5, N'RETURN', N'DAMAGED', N'Một nải chuối laba bị dập nát khi vận chuyển', N'/evidence/returns/rr-001.jpg', 1, N'REFUND', NULL, 28000.00, N'REQUESTED', NULL, NULL, NULL, '2026-05-16T19:00:00', '2026-05-16T19:00:00');
    SET IDENTITY_INSERT dbo.return_requests OFF;

    SET IDENTITY_INSERT dbo.shop_settlements ON;
    INSERT INTO dbo.shop_settlements (settlement_id, owner_id, period_start, period_end, gross_amount, platform_fee_amount, refund_amount, adjustment_amount, net_amount, status, calculated_at, confirmed_at, paid_at, created_by, note)
    VALUES
        (1, 3, '2026-05-01', '2026-05-15', 272000.00, 13600.00, 0.00, 0.00, 231200.00, N'PENDING', '2026-05-16T20:00:00', NULL, NULL, 1, N'Pending because order 3 has an open return request'),
        (2, 4, '2026-05-01', '2026-05-15', 214000.00, 10700.00, 0.00, 0.00, 188300.00, N'PAID', '2026-05-16T20:00:00', '2026-05-16T20:30:00', '2026-05-16T22:00:00', 1, N'Includes order 2');
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
    INSERT INTO dbo.reviews (review_id, order_item_id, customer_id, rating, review_text, review_image_url, is_hidden, created_at)
    VALUES
        (1, 1, 5, 5, N'Cam Cao Phong cực kỳ nhiều nước, vị ngọt thanh tự nhiên xen chua nhẹ ăn cực đã. Giao hàng nhanh và đóng gói chuyên nghiệp!', N'https://images.unsplash.com/photo-1611080626919-7cf5a9dbab5b?w=600&auto=format&fit=crop&q=80', 0, '2026-05-15T12:00:00'),
        (2, 3, 6, 4, N'Xoài cát chất lượng chín ngọt thơm ngon, đóng gói rất cẩn thận.', NULL, 0, '2026-05-15T17:00:00'),
        (10, 10, 10, 5, N'Quả cam tươi rói, vỏ mỏng, nước nhiều. Vắt nước uống cho các bé ở nhà rất thích, sẽ tiếp tục ủng hộ shop lâu dài.', N'https://images.unsplash.com/photo-1547514701-42782101795e?w=600&auto=format&fit=crop&q=80', 0, '2026-05-18T15:00:00'),
        (11, 11, 11, 4, N'Cam ngon, thơm ngọt thanh. Giao hàng trong vòng 2 tiếng rất đúng hẹn. Tuy nhiên có vài quả hơi nhỏ hơn so với mô tả một chút.', NULL, 0, '2026-05-19T16:30:00'),
        (12, 12, 12, 5, N'Combo 3kg rẻ hơn nhiều so với mua lẻ. Trái cây tươi sạch sẽ, vỏ xanh bóng bẩy cực bắt mắt. Khuyên mọi người nên mua nha!', N'https://images.unsplash.com/photo-1618897996318-5a901fa6ca71?w=600&auto=format&fit=crop&q=80', 0, '2026-05-20T17:45:00'),
        (13, 13, 13, 3, N'Cam ăn cũng tạm được, nước vừa phải chứ không nhiều lắm. Giao hàng trễ mất 30 phút nên trừ 2 sao.', NULL, 0, '2026-05-21T18:20:00'),
        (14, 14, 14, 2, N'Quả cam nhận được bị dập 2 quả ở dưới đáy hộp do khâu vận chuyển xếp đè lên. Vị ngọt bình thường không đặc sắc.', N'https://images.unsplash.com/photo-1590080875515-8a3a8dc5735e?w=600&auto=format&fit=crop&q=80', 0, '2026-05-22T20:10:00'),
        (15, 15, 15, 1, N'Giao hàng quá chậm, cam thì bị héo vỏ và rụng cuống hết cả chùm. Quá thất vọng về trải nghiệm mua hàng lần này.', NULL, 0, '2026-05-23T10:15:00');
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

