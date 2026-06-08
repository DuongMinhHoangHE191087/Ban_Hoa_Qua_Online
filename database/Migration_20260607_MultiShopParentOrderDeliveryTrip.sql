USE [OnlineFruitShopping];
GO

SET NOCOUNT ON;
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET ARITHABORT ON;
SET NUMERIC_ROUNDABORT OFF;
GO

-- Multi-shop checkout needs an aggregate parent order and one child order per shop.
IF OBJECT_ID(N'dbo.orders', N'U') IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID(N'dbo.orders') AND name = N'parent_order_id')
        ALTER TABLE dbo.orders ADD parent_order_id INT NULL;

    IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID(N'dbo.orders') AND name = N'order_type')
        ALTER TABLE dbo.orders ADD order_type NVARCHAR(10) NOT NULL CONSTRAINT DF_orders_order_type DEFAULT 'CHILD';

    EXEC(N'UPDATE dbo.orders SET order_type = ''CHILD'' WHERE order_type IS NULL');

    IF EXISTS (
        SELECT 1
        FROM sys.columns
        WHERE object_id = OBJECT_ID(N'dbo.orders')
          AND name = N'owner_id'
          AND is_nullable = 0
    )
    BEGIN
        DECLARE @OwnerFkName NVARCHAR(255);
        SELECT @OwnerFkName = fk.name
        FROM sys.foreign_keys fk
        JOIN sys.foreign_key_columns fkc ON fk.object_id = fkc.constraint_object_id
        JOIN sys.columns c ON c.object_id = fkc.parent_object_id AND c.column_id = fkc.parent_column_id
        WHERE fk.parent_object_id = OBJECT_ID(N'dbo.orders') AND c.name = N'owner_id';

        IF @OwnerFkName IS NOT NULL
        BEGIN
            DECLARE @DropOwnerFkSql NVARCHAR(MAX);
            SET @DropOwnerFkSql = N'ALTER TABLE dbo.orders DROP CONSTRAINT ' + QUOTENAME(@OwnerFkName);
            EXEC sp_executesql @DropOwnerFkSql;
        END

        ALTER TABLE dbo.orders ALTER COLUMN owner_id INT NULL;

        IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_orders_owner')
            ALTER TABLE dbo.orders ADD CONSTRAINT FK_orders_owner FOREIGN KEY (owner_id) REFERENCES dbo.users(user_id);
    END

    IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_orders_parent')
        ALTER TABLE dbo.orders ADD CONSTRAINT FK_orders_parent FOREIGN KEY (parent_order_id) REFERENCES dbo.orders(order_id);

    IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'CK_orders_order_type')
        EXEC(N'ALTER TABLE dbo.orders ADD CONSTRAINT CK_orders_order_type CHECK (order_type IN (''PARENT'', ''CHILD''))');

    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_orders_parent_order_id')
        CREATE INDEX IX_orders_parent_order_id ON dbo.orders(parent_order_id, order_id);
END
GO

IF OBJECT_ID(N'dbo.delivery_trips', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.delivery_trips (
        trip_id INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_delivery_trips PRIMARY KEY,
        parent_order_id INT NOT NULL,
        shipper_id INT NULL,
        status NVARCHAR(20) NOT NULL CONSTRAINT DF_delivery_trips_status DEFAULT 'PLANNED'
            CONSTRAINT CK_delivery_trips_status CHECK (status IN ('PLANNED','ASSIGNED','PICKED_UP','IN_TRANSIT','DELIVERED','FAILED','CANCELLED')),
        estimated_start_time DATETIME NULL,
        estimated_end_time DATETIME NULL,
        created_at DATETIME NOT NULL CONSTRAINT DF_delivery_trips_created_at DEFAULT GETDATE(),
        updated_at DATETIME NOT NULL CONSTRAINT DF_delivery_trips_updated_at DEFAULT GETDATE(),
        CONSTRAINT FK_delivery_trips_parent_order FOREIGN KEY (parent_order_id) REFERENCES dbo.orders(order_id),
        CONSTRAINT FK_delivery_trips_shipper FOREIGN KEY (shipper_id) REFERENCES dbo.users(user_id)
    );
END
GO

IF OBJECT_ID(N'dbo.deliveries', N'U') IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID(N'dbo.deliveries') AND name = N'delivery_trip_id')
        ALTER TABLE dbo.deliveries ADD delivery_trip_id INT NULL;

    IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID(N'dbo.deliveries') AND name = N'trip_stop_seq')
        ALTER TABLE dbo.deliveries ADD trip_stop_seq INT NULL;

    IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_deliveries_delivery_trip')
        ALTER TABLE dbo.deliveries ADD CONSTRAINT FK_deliveries_delivery_trip FOREIGN KEY (delivery_trip_id) REFERENCES dbo.delivery_trips(trip_id);

    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_deliveries_delivery_trip_id')
        CREATE INDEX IX_deliveries_delivery_trip_id ON dbo.deliveries(delivery_trip_id, trip_stop_seq);
END
GO
