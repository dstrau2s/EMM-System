-- Füge eine Spalte für die Geräte-ID in der AuditLog-Tabelle hinzu

use EMM_Demo
GO

-- SQL Server: Spalte zur AuditLog-Tabelle hinzufügen
ALTER TABLE AuditLog 
ADD endgeraet_id INT NULL;
GO

-- Optional: Foreign Key Constraint hinzufügen
ALTER TABLE AuditLog 
ADD CONSTRAINT FK_AuditLog_Endgeraet 
FOREIGN KEY (endgeraet_id) REFERENCES Endgeraet(id);
GO