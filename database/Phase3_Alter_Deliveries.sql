-- Phase 3: Thêm các cột cho tính năng Giao hàng
ALTER TABLE deliveries ADD proof_image_url NVARCHAR(500) NULL;
ALTER TABLE deliveries ADD estimated_delivery_time DATETIME NULL;
