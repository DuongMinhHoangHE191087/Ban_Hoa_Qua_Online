USE [OnlineFruitShopping];
GO

-- Drop existing tables
IF OBJECT_ID('dbo.inventory_logs', 'U') IS NOT NULL 
    DROP TABLE dbo.inventory_logs;

IF OBJECT_ID('dbo.inventory_batches', 'U') IS NOT NULL 
    DROP TABLE dbo.inventory_batches;
GO

-- Create new inventory_batches table
CREATE TABLE dbo.inventory_batches (
    batch_id INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_inventory_batches PRIMARY KEY,
    variant_id INT NOT NULL,
    initial_quantity INT NOT NULL,
    remaining_quantity INT NOT NULL CONSTRAINT CK_inventory_batches_remaining CHECK (remaining_quantity >= 0),
    expires_at DATE NULL,
    is_expired BIT NOT NULL CONSTRAINT DF_inventory_batches_is_expired DEFAULT 0,
    created_at DATETIME NOT NULL CONSTRAINT DF_inventory_batches_created_at DEFAULT GETDATE(),
    CONSTRAINT FK_inventory_batches_variants FOREIGN KEY (variant_id) REFERENCES dbo.product_variants(variant_id)
);
GO

-- Create new inventory_logs table with batch_id
CREATE TABLE dbo.inventory_logs (
    log_id INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_inventory_logs PRIMARY KEY,
    variant_id INT NOT NULL,
    batch_id INT NULL,
    changed_by INT NOT NULL,
    change_type NVARCHAR(20) NOT NULL CONSTRAINT CK_inventory_logs_change_type CHECK (change_type IN ('MANUAL_ADJUST', 'ORDER_RESERVE', 'ORDER_RELEASE', 'ORDER_CONFIRM', 'RETURN', 'EXPIRED', 'SPOILED')),
    quantity_delta INT NOT NULL,
    quantity_after INT NOT NULL,
    note NVARCHAR(300) NULL,
    expires_at DATE NULL,
    is_expired BIT NOT NULL CONSTRAINT DF_inventory_logs_is_expired DEFAULT 0,
    changed_at DATETIME NOT NULL CONSTRAINT DF_inventory_logs_changed_at DEFAULT GETDATE(),
    CONSTRAINT FK_inventory_logs_variants FOREIGN KEY (variant_id) REFERENCES dbo.product_variants(variant_id),
    CONSTRAINT FK_inventory_logs_batches FOREIGN KEY (batch_id) REFERENCES dbo.inventory_batches(batch_id),
    CONSTRAINT FK_inventory_logs_users FOREIGN KEY (changed_by) REFERENCES dbo.users(user_id)
);
GO

-- Update product_variants to reset stock to 0 since we wiped inventory
UPDATE dbo.product_variants SET stock_quantity = 0;
GO

PRINT 'Inventory tables migrated successfully. All existing inventory data has been cleared.';
