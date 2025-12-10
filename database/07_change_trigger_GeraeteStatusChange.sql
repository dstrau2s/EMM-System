-- Trigger anpassen, um die Geräte-ID mit aufzunehmen

use EMM_Demo
GO

ALTER TRIGGER trg_GeräteStatusChange
ON Endgeraet
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    
    INSERT INTO AuditLog (tabelle, aktion, alt, neu, benutzer, zeitpunkt, endgeraet_id)
    SELECT 
        'Endgeraet',
        'Statusänderung',
        d.status,
        i.status,
        SUSER_SNAME(),
        GETDATE(),
        i.id  -- Geräte-ID hinzufügen
    FROM inserted i
    JOIN deleted d ON i.id = d.id
    WHERE i.status != d.status;
END;