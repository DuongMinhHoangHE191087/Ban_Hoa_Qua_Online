-- Create user_addresses table
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[user_addresses]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[user_addresses] (
        [address_id] INT IDENTITY(1,1) PRIMARY KEY,
        [user_id] INT NOT NULL FOREIGN KEY REFERENCES users(user_id) ON DELETE CASCADE,
        [recipient_name] NVARCHAR(100) NOT NULL,
        [recipient_phone] NVARCHAR(15) NOT NULL,
        [address_detail] NVARCHAR(500) NOT NULL,
        [is_default] BIT NOT NULL DEFAULT 0,
        [created_at] DATETIME NOT NULL DEFAULT GETDATE()
    );
END
GO

-- Add recipient_name and recipient_phone to orders table
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[orders]') AND name = 'recipient_name')
BEGIN
    ALTER TABLE dbo.orders ADD recipient_name NVARCHAR(100) NULL;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[orders]') AND name = 'recipient_phone')
BEGIN
    ALTER TABLE dbo.orders ADD recipient_phone NVARCHAR(15) NULL;
END
GO
