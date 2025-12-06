-- STORED PROCEDURES FÜR HÄUFIGE OPERATIONEN

USE EMM_Demo;
GO

-- Procedure 1: Neues Gerät erfassen
CREATE PROCEDURE sp_NeuesGerätErfassen
    @hersteller NVARCHAR(100),
    @modell NVARCHAR(100),
    @betriebssystem NVARCHAR(50),
    @osVersion NVARCHAR(50) = NULL,
    @imei NVARCHAR(20),
    @status NVARCHAR(20) = 'LAGER'
AS
BEGIN
    INSERT INTO Endgeraet (hersteller, modell, betriebssystem, osVersion, imei, status)
    VALUES (@hersteller, @modell, @betriebssystem, @osVersion, @imei, @status);
    
    SELECT SCOPE_IDENTITY() AS NeueGeräteID;
END
GO

-- Procedure 1b: Gerät entfernen (nur Soft-Delete)
CREATE PROCEDURE sp_GerätEntfernen_Einfach
    @geraeteID INT,
    @entfernungsGrund NVARCHAR(200) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Prüfen ob Gerät existiert
    IF NOT EXISTS (SELECT 1 FROM Endgeraet WHERE id = @geraeteID)
    BEGIN
        RAISERROR('Gerät mit der angegebenen ID existiert nicht.', 16, 1);
        RETURN;
    END
    
    -- Status auf "AUSGESCHIEDEN" setzen
    UPDATE Endgeraet 
    SET status = 'AUSGESCHIEDEN',
        entferntAm = CAST(GETDATE() AS DATE),
        entferntGrund = @entfernungsGrund
    WHERE id = @geraeteID;
    
    SELECT 'Gerät erfolgreich als entfernt markiert' AS Meldung,
           @geraeteID AS EntfernteGeräteID;
END
GO

-- Procedure 2: Gerät an Mitarbeiter ausgeben
CREATE PROCEDURE sp_GerätAusgeben
    @geräteId INT,
    @mitarbeiterId INT,
    @ausgegebenVon NVARCHAR(100)
AS
BEGIN
    BEGIN TRANSACTION;
    
    -- Prüfen ob Gerät verfügbar ist
    IF NOT EXISTS (SELECT 1 FROM Endgeraet WHERE id = @geräteId AND status = 'LAGER')
    BEGIN
        ROLLBACK;
        RAISERROR('Gerät ist nicht verfügbar oder nicht im Lager.', 16, 1);
        RETURN;
    END
    
    -- Alte Ausgabe schließen falls vorhanden
    UPDATE Ausgabe 
    SET rueckgabedatum = GETDATE() 
    WHERE endgeraetId = @geräteId AND rueckgabedatum IS NULL;
    
    -- Neue Ausgabe erfassen
    INSERT INTO Ausgabe (endgeraetId, mitarbeiterId, ausgabeVon, ausgabedatum)
    VALUES (@geräteId, @mitarbeiterId, @ausgegebenVon, CAST(GETDATE() AS DATE));
    
    -- Gerätestatus aktualisieren
    UPDATE Endgeraet SET status = 'AKTIV' WHERE id = @geräteId;
    
    COMMIT;
    
    SELECT 'Gerät erfolgreich ausgegeben' AS Meldung;
END
GO

-- Procedure 3: Gerät zurücknehmen
CREATE PROCEDURE sp_GerätZuruecknehmen
    @geräteId INT
AS
BEGIN
    BEGIN TRANSACTION;
    
    -- Aktuelle Ausgabe schließen
    UPDATE Ausgabe 
    SET rueckgabedatum = GETDATE() 
    WHERE endgeraetId = @geräteId AND rueckgabedatum IS NULL;
    
    -- Gerätestatus aktualisieren
    UPDATE Endgeraet SET status = 'LAGER' WHERE id = @geräteId;
    
    COMMIT;
    
    SELECT 'Gerät erfolgreich zurückgenommen' AS Meldung;
END
GO


-- Procedure 4: Ausgegebene Geräte
CREATE PROCEDURE sp_GetMitarbeiterGeräte
    @MitarbeiterId INT
AS
BEGIN
    SELECT 
        e.hersteller + ' ' + e.modell AS Gerät,
        e.status,
        e.betriebssystem + ' ' + e.osVersion AS OS_Version,
        au.ausgabedatum,
        DATEDIFF(day, au.ausgabedatum, GETDATE()) AS Tage_im_Einsatz
    FROM Ausgabe au
    JOIN Endgeraet e ON au.endgeraetId = e.id
    WHERE au.mitarbeiterId = @MitarbeiterId 
    AND au.rueckgabedatum IS NULL;
END
GO


-- Procedure 5: Verfügbare Geräte
CREATE PROCEDURE sp_GetVerfügbareGeräte
    @Gerätetyp NVARCHAR(50) = NULL
AS
BEGIN
    SELECT 
        e.id,
        e.hersteller + ' ' + e.modell AS Gerät,
        e.betriebssystem + ' ' + e.osVersion AS Betriebssystem,
        e.hinzugefuegtAm AS Im_Lager_seit,
        DATEDIFF(day, e.hinzugefuegtAm, GETDATE()) AS Lagerdauer_Tage
    FROM Endgeraet e
    LEFT JOIN Ausgabe a ON e.id = a.endgeraetId AND a.rueckgabedatum IS NULL
    WHERE a.id IS NULL 
    AND e.status = 'LAGER'
    AND (@Gerätetyp IS NULL OR e.modell LIKE '%' + @Gerätetyp + '%');
END
GO


-- Procedure 6: Monatlichen Kostenreport erstellen
CREATE PROCEDURE sp_Monatsreport
    @monat INT = NULL,
    @jahr INT = NULL
AS
BEGIN
    IF @monat IS NULL SET @monat = MONTH(GETDATE());
    IF @jahr IS NULL SET @jahr = YEAR(GETDATE());
    
    SELECT 
        a.name AS Abteilung,
        COUNT(DISTINCT e.id) AS Anzahl_Geräte,
        SUM(mv.kostenMonatlich) AS Gesamtkosten,
        AVG(mv.datenvolumen) AS Durchschnitts_Datenvolumen,
        ab.budgetMobilfunk AS Budget,
        ab.budgetMobilfunk - SUM(mv.kostenMonatlich) AS Budget_Rest
    FROM Abteilung a
    LEFT JOIN Mitarbeiter m ON a.id = m.abteilungId
    LEFT JOIN Ausgabe au ON m.id = au.mitarbeiterId 
        AND au.rueckgabedatum IS NULL
        AND MONTH(au.ausgabedatum) = @monat
        AND YEAR(au.ausgabedatum) = @jahr
    LEFT JOIN Endgeraet e ON au.endgeraetId = e.id
    LEFT JOIN Mobilfunkvertrag mv ON e.id = mv.endgeraetId
    LEFT JOIN Abteilung ab ON a.id = ab.id
    GROUP BY a.name, ab.budgetMobilfunk
    ORDER BY Gesamtkosten DESC;
END
GO

-- Procedure 7: Compliance-Check für einzelne Richtlinie

-- Zuerst die Prozedur löschen
IF OBJECT_ID('sp_DemoComplianceCheck', 'P') IS NOT NULL
    DROP PROCEDURE sp_DemoComplianceCheck;
GO

-- Korrigierte Version
CREATE PROCEDURE sp_DemoComplianceCheck
    @richtlinienId INT
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Prüfen ob Richtlinie existiert
    IF NOT EXISTS (SELECT 1 FROM Richtlinie WHERE id = @richtlinienId)
    BEGIN
        PRINT 'FEHLER: Richtlinie mit ID ' + CAST(@richtlinienId AS NVARCHAR(10)) + ' existiert nicht.';
        PRINT 'Verfügbare Richtlinien:';
        SELECT id, name FROM Richtlinie ORDER BY id;
        RETURN;
    END
    
    DECLARE @richtlinienName NVARCHAR(200);
    DECLARE @beschreibung NVARCHAR(1000);
    DECLARE @regelText NVARCHAR(500);
    
    -- Richtlinien-Daten holen
    SELECT 
        @richtlinienName = name,
        @beschreibung = beschreibung,
        @regelText = regel
    FROM Richtlinie 
    WHERE id = @richtlinienId;
    
    PRINT 'Starte Compliance-Check für Richtlinie: ' + @richtlinienName;
    PRINT 'Beschreibung: ' + @beschreibung;
    PRINT 'Regel: ' + @regelText;
    PRINT '---';
    
    -- Alte Einträge für diese Richtlinie löschen (für frische Ergebnisse)
    DELETE FROM CompliancePruefung WHERE policyId = @richtlinienId;
    
    -- Einfache Demologik basierend auf Geräteeigenschaften
    INSERT INTO CompliancePruefung (endgeraetId, policyId, erfuellt, bemerkung, geprueftAm)
    SELECT 
        e.id,
        @richtlinienId,
        CASE 
            -- 1: Passwortrichtlinie - Gerät aktiv + Apple/Windows = erfüllt
            WHEN @richtlinienId = 1 AND e.status = 'AKTIV' 
                 AND e.betriebssystem IN ('iOS', 'Windows') THEN 1
            
            -- 2: Verschlüsselung - Apple/Microsoft Geräte = erfüllt
            WHEN @richtlinienId = 2 AND e.hersteller IN ('Apple', 'Microsoft') THEN 1
            
            -- 3: OS-Version - Aktuelle Versionen = erfüllt
            WHEN @richtlinienId = 3 AND (
                (e.betriebssystem = 'iOS' AND e.osVersion >= '17.0') OR
                (e.betriebssystem = 'Android' AND e.osVersion >= '14.0') OR
                (e.betriebssystem = 'Windows' AND e.osVersion >= '10.0')
            ) THEN 1
            
            -- 4: Antivirus - Alle außer iOS (braucht keinen) = erfüllt
            WHEN @richtlinienId = 4 AND e.betriebssystem != 'iOS' THEN 1
            
            -- 5: VPN-Pflicht - Nur für aktive Geräte = erfüllt
            WHEN @richtlinienId = 5 AND e.status = 'AKTIV' THEN 1
            
            -- 6: App-Whitelist - iOS und Google Geräte = erfüllt
            WHEN @richtlinienId = 6 AND e.hersteller IN ('Apple', 'Google') THEN 1
            
            ELSE 0 -- Standard: nicht erfüllt
        END AS erfuellt,
        
        -- Detaillierte Bemerkung (auf 500 Zeichen begrenzen)
        LEFT('Demo-Check für ''' + @richtlinienName + ''' - ' +
        'Gerät: ' + e.hersteller + ' ' + e.modell + ' (' + e.betriebssystem + ' ' + 
        ISNULL(e.osVersion, 'unbekannt') + '), Status: ' + e.status, 500),
        
        GETDATE()
        
    FROM Endgeraet e;
    
    -- Statistik berechnen mit Fehlerbehandlung
    DECLARE @AnzahlGeprueft INT;
    DECLARE @Erfuellt INT;
    DECLARE @NichtErfuellt INT;
    DECLARE @Erfuellungsquote DECIMAL(5,2);
    
    SELECT @AnzahlGeprueft = COUNT(*) 
    FROM CompliancePruefung 
    WHERE policyId = @richtlinienId;
    
    SELECT @Erfuellt = COUNT(*) 
    FROM CompliancePruefung 
    WHERE policyId = @richtlinienId AND erfuellt = 1;
    
    SELECT @NichtErfuellt = COUNT(*) 
    FROM CompliancePruefung 
    WHERE policyId = @richtlinienId AND erfuellt = 0;
    
    -- Erfüllungsquote sicher berechnen (Division durch Null vermeiden)
    IF @AnzahlGeprueft > 0
        SET @Erfuellungsquote = ROUND(@Erfuellt * 100.0 / @AnzahlGeprueft, 1);
    ELSE
        SET @Erfuellungsquote = 0.0;
    
    -- Ergebnisse ausgeben (sicher mit CAST)
    PRINT 'ERGEBNISSE:';
    PRINT '===========';
    PRINT 'Anzahl geprüfter Geräte: ' + CAST(@AnzahlGeprueft AS NVARCHAR(10));
    PRINT 'Richtlinie erfüllt: ' + CAST(@Erfuellt AS NVARCHAR(10));
    PRINT 'Richtlinie nicht erfüllt: ' + CAST(@NichtErfuellt AS NVARCHAR(10));
    PRINT 'Erfüllungsquote: ' + CAST(@Erfuellungsquote AS NVARCHAR(10)) + '%';
    PRINT '';
    
    -- Detailergebnisse als SELECT zurückgeben
    SELECT 
        @richtlinienName AS 'Geprüfte Richtlinie',
        @AnzahlGeprueft AS 'Anzahl Geräte',
        @Erfuellt AS 'Erfüllt',
        @NichtErfuellt AS 'Nicht erfüllt',
        @Erfuellungsquote AS 'Erfüllungsquote (%)',
        GETDATE() AS 'Prüfungszeitpunkt';
    
END
GO


-- Procedure 8: Compliance-Check für alle Richtlinien
IF OBJECT_ID('sp_AlleComplianceChecks', 'P') IS NOT NULL
    DROP PROCEDURE sp_AlleComplianceChecks;
GO

CREATE PROCEDURE sp_AlleComplianceChecks
AS
BEGIN
    SET NOCOUNT ON;
    
    PRINT 'Starte umfassenden Compliance-Check für alle Richtlinien...';
    PRINT '==========================================================';
    PRINT '';
    
    -- Temporäre Tabelle für Gesamtergebnisse
    CREATE TABLE #Gesamtergebnisse (
        RichtlinieID INT PRIMARY KEY,
        RichtlinienName NVARCHAR(200),
        AnzahlGeräte INT DEFAULT 0,
        Erfuellt INT DEFAULT 0,
        NichtErfuellt INT DEFAULT 0,
        Erfuellungsquote DECIMAL(5,2) DEFAULT 0,
        Pruefungszeitpunkt DATETIME
    );
    
    -- Initial mit allen Richtlinien füllen
    INSERT INTO #Gesamtergebnisse (RichtlinieID, RichtlinienName, Pruefungszeitpunkt)
    SELECT id, name, GETDATE() FROM Richtlinie ORDER BY id;
    
    -- Variablen für die Schleife
    DECLARE @maxId INT = (SELECT MAX(id) FROM Richtlinie);
    DECLARE @currentId INT = 1;
    
    -- Durch alle Richtlinien-IDs iterieren
    WHILE @currentId <= @maxId
    BEGIN
        -- Prüfen ob Richtlinie existiert
        IF EXISTS (SELECT 1 FROM Richtlinie WHERE id = @currentId)
        BEGIN
            DECLARE @name NVARCHAR(200);
            SELECT @name = name FROM Richtlinie WHERE id = @currentId;
            
            PRINT 'Prüfe Richtlinie: ' + @name + ' (ID: ' + CAST(@currentId AS NVARCHAR) + ')';
            
            -- Prozedur aufrufen und Ergebnisse in Variablen speichern
            DECLARE @AnzahlGeräte INT, @Erfuellt INT, @NichtErfuellt INT;
            DECLARE @Quote DECIMAL(5,2);
            
            -- Temporäre Tabelle für Ergebnisse
            CREATE TABLE #TempResult (
                Richtlinie NVARCHAR(200),
                Anzahl INT,
                Erf INT,
                NichtErf INT,
                Quote DECIMAL(5,2),
                Zeit DATETIME
            );
            
            INSERT INTO #TempResult
            EXEC sp_DemoComplianceCheck @richtlinienId = @currentId;
            
            -- Werte extrahieren
            SELECT 
                @AnzahlGeräte = Anzahl,
                @Erfuellt = Erf,
                @NichtErfuellt = NichtErf,
                @Quote = Quote
            FROM #TempResult;
            
            -- In Gesamttabelle aktualisieren
            UPDATE #Gesamtergebnisse
            SET 
                AnzahlGeräte = @AnzahlGeräte,
                Erfuellt = @Erfuellt,
                NichtErfuellt = @NichtErfuellt,
                Erfuellungsquote = @Quote,
                Pruefungszeitpunkt = GETDATE()
            WHERE RichtlinieID = @currentId;
            
            DROP TABLE #TempResult;
            
            PRINT '';
        END
        
        SET @currentId = @currentId + 1;
    END
    
    -- Gesamtübersicht
    PRINT '==========================================================';
    PRINT 'GESAMTÜBERSICHT ALLER COMPLIANCE-PRÜFUNGEN:';
    PRINT '==========================================================';
    
    SELECT 
        RichtlinienName AS 'Richtlinie',
        AnzahlGeräte AS 'Geräte',
        Erfuellt AS '✓ Erfüllt',
        NichtErfuellt AS '✗ Nicht erfüllt',
        Erfuellungsquote AS 'Quote (%)',
        Pruefungszeitpunkt AS 'Geprüft am'
    FROM #Gesamtergebnisse
    WHERE AnzahlGeräte > 0  -- Nur Richtlinien mit Ergebnissen
    ORDER BY Erfuellungsquote DESC;
    
    -- Zusammenfassung
    SELECT 
        'ZUSAMMENFASSUNG' AS 'Statistik',
        COUNT(*) AS 'Anzahl geprüfter Richtlinien',
        SUM(AnzahlGeräte) AS 'Gesamte Geräteprüfungen',
        AVG(Erfuellungsquote) AS 'Durchschnittl. Erfüllungsquote (%)'
    FROM #Gesamtergebnisse
    WHERE AnzahlGeräte > 0;
    
    DROP TABLE #Gesamtergebnisse;
    
END
GO