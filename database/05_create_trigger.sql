-- AUDITLOG-TABELLE UND TRIGGER FÜR GERÄTESTATUS-ÄNDERUNGEN

USE EMM_Demo;
GO

-- 1. AuditLog-Tabelle erstellen (falls noch nicht vorhanden)
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[AuditLog]') AND type in (N'U'))
BEGIN
    CREATE TABLE AuditLog (
        id INT IDENTITY(1,1) PRIMARY KEY,
        tabelle NVARCHAR(100) NOT NULL,
        aktion NVARCHAR(100) NOT NULL,
        alt NVARCHAR(500) NULL,
        neu NVARCHAR(500) NULL,
        zeitpunkt DATETIME DEFAULT GETDATE(),
        benutzer NVARCHAR(128) DEFAULT SYSTEM_USER,
        ip_adresse NVARCHAR(50) NULL
    );
    
    PRINT 'AuditLog-Tabelle wurde erfolgreich erstellt.';
END
ELSE
BEGIN
    PRINT 'AuditLog-Tabelle existiert bereits.';
END
GO

-- 2. Trigger erstellen (falls noch nicht vorhanden)
IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'trg_GeräteStatusChange')
BEGIN
    DROP TRIGGER trg_GeräteStatusChange;
    PRINT 'Alter Trigger trg_GeräteStatusChange wurde gelöscht.';
END
GO

CREATE TRIGGER trg_GeräteStatusChange
ON Endgeraet
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Prüfen, ob die Status-Spalte geändert wurde
    IF UPDATE(status)
    BEGIN
        -- Audit-Einträge für alle geänderten Statuswerte erstellen
        INSERT INTO AuditLog (tabelle, aktion, alt, neu, zeitpunkt)
        SELECT 
            'Endgeraet', 
            'Statusänderung', 
            d.status, 
            i.status, 
            GETDATE()
        FROM inserted i
        JOIN deleted d ON i.id = d.id
        WHERE d.status <> i.status;  -- Nur bei tatsächlicher Änderung
        
        PRINT 'Statusänderungen wurden im AuditLog protokolliert.';
    END
END
GO

PRINT 'Trigger trg_GeräteStatusChange wurde erfolgreich erstellt.';
GO