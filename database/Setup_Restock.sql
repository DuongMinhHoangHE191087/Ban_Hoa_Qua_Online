-- Script to create the replenishment_logs table for Restock Management

IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[replenishment_logs]') AND type in (N'U'))
BEGIN
    CREATE TABLE replenishment_logs (
        log_id INT IDENTITY(1,1) PRIMARY KEY,
        variant_id INT NOT NULL FOREIGN KEY REFERENCES product_variants(variant_id) ON DELETE CASCADE,
        replenished_by INT NOT NULL FOREIGN KEY REFERENCES users(user_id),
        quantity INT NOT NULL CHECK (quantity > 0),
        supplier_details NVARCHAR(500) NULL,
        replenishment_date DATE NOT NULL,
        created_at DATETIME NOT NULL DEFAULT GETDATE()
    );
END
GO
