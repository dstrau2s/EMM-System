-- ANSICHTEN FÜR BESSERE ÜBERSICHT

USE EMM_Demo;
GO

-- View 1: Aktive Geräte mit Besitzern
CREATE VIEW V_AktiveGeräte AS
SELECT 
    e.id AS GeräteID,
    e.hersteller,
    e.modell,
    e.betriebssystem + ' ' + e.osVersion AS Betriebssystem,
    e.imei,
    m.personalnummer,
    m.vorname + ' ' + m.nachname AS Mitarbeiter,
    m.abteilung,
    a.ausgabedatum,
    DATEDIFF(DAY, a.ausgabedatum, GETDATE()) AS Tage_in_Nutzung
FROM Endgeraet e
LEFT JOIN Ausgabe a ON e.id = a.endgeraetId AND a.rueckgabedatum IS NULL
LEFT JOIN Mitarbeiter m ON a.mitarbeiterId = m.id
WHERE e.status = 'AKTIV';
GO

-- View 2: Kostenübersicht pro Abteilung
CREATE VIEW V_KostenProAbteilung AS
SELECT 
    a.name AS Abteilung,
    COUNT(DISTINCT e.id) AS Anzahl_Geräte,
    SUM(mv.kostenMonatlich) AS Monatliche_Kosten,
    SUM(mv.datenvolumen) AS Gesamt_Datenvolumen_GB,
    AVG(ab.budgetMobilfunk) AS Budget
FROM Abteilung a
LEFT JOIN Mitarbeiter m ON a.id = m.abteilungId
LEFT JOIN Ausgabe au ON m.id = au.mitarbeiterId AND au.rueckgabedatum IS NULL
LEFT JOIN Endgeraet e ON au.endgeraetId = e.id
LEFT JOIN Mobilfunkvertrag mv ON e.id = mv.endgeraetId
LEFT JOIN Abteilung ab ON a.id = ab.id
GROUP BY a.name;
GO

-- View 3: Vertragsübersicht mit Ablaufdatum
CREATE VIEW V_Vertragsuebersicht AS
SELECT 
    mv.vertragsnummer,
    mv.provider,
    mv.datenvolumen,
    mv.kostenMonatlich,
    mv.laufzeitBis,
    DATEDIFF(DAY, GETDATE(), mv.laufzeitBis) AS Tage_bis_Ablauf,
    e.hersteller + ' ' + e.modell AS Gerät,
    CASE 
        WHEN DATEDIFF(DAY, GETDATE(), mv.laufzeitBis) < 30 THEN 'BALD ABLAUFEND'
        WHEN DATEDIFF(DAY, GETDATE(), mv.laufzeitBis) < 90 THEN 'MITTLERE LAUFZEIT'
        ELSE 'LANG LAUFEND'
    END AS Status
FROM Mobilfunkvertrag mv
LEFT JOIN Endgeraet e ON mv.endgeraetId = e.id;
GO

-- View 4: Compliance-Report
CREATE VIEW V_ComplianceReport AS
SELECT 
    e.id AS GeräteID,
    e.hersteller + ' ' + e.modell AS Gerät,
    r.name AS Richtlinie,
    cp.erfuellt,
    cp.geprueftAm,
    cp.bemerkung,
    m.vorname + ' ' + m.nachname AS Verantwortlicher
FROM CompliancePruefung cp
JOIN Endgeraet e ON cp.endgeraetId = e.id
JOIN Richtlinie r ON cp.policyId = r.id
LEFT JOIN Ausgabe a ON e.id = a.endgeraetId AND a.rueckgabedatum IS NULL
LEFT JOIN Mitarbeiter m ON a.mitarbeiterId = m.id;
GO

-- View 5: App-Installationsübersicht
CREATE VIEW V_AppInstallationen AS
SELECT 
    app.name AS App_Name,
    app.kategorie,
    COUNT(i.id) AS Installationsanzahl,
    STRING_AGG(e.modell, ', ') AS Gerätemodelle
FROM App app
LEFT JOIN Installation i ON app.id = i.appId
LEFT JOIN Endgeraet e ON i.endgeraetId = e.id
GROUP BY app.name, app.kategorie;
GO