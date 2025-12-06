-- TESTDATEN EINFÜGEN

use EMM_Demo
GO

-- 1. Abteilungen 
INSERT INTO Abteilung (name, budgetMobilfunk)
VALUES
('IT', 5000.00),
('Vertrieb', 3000.00),
('HR', 2000.00),
('Marketing', 2500.00),
('Entwicklung', 8000.00),
('Finanzen', 2000.00),
('Einkauf', 1800.00),
('Produktion', 3500.00);
GO

-- 2. Mitarbeiter hinzufügen
INSERT INTO Mitarbeiter (personalnummer, vorname, nachname, abteilung, email, abteilungId)
VALUES
('EMP001', 'Max', 'Mustermann', 'IT', 'max.mustermann@firma.de', 1),
('EMP002', 'Anna', 'Musterfrau', 'Vertrieb', 'anna.musterfrau@firma.de', 2),
('EMP003', 'Thomas', 'Schmidt', 'IT', 'thomas.schmidt@firma.de', 1),
('EMP004', 'Sarah', 'Müller', 'Marketing', 'sarah.mueller@firma.de', 4),
('EMP005', 'David', 'Schneider', 'Entwicklung', 'david.schneider@firma.de', 5),
('EMP006', 'Lisa', 'Fischer', 'Finanzen', 'lisa.fischer@firma.de', 6),
('EMP007', 'Markus', 'Weber', 'Einkauf', 'markus.weber@firma.de', 7),
('EMP008', 'Julia', 'Hoffmann', 'Produktion', 'julia.hoffmann@firma.de', 8),
('EMP009', 'Alexander', 'Bauer', 'IT', 'alexander.bauer@firma.de', 1),
('EMP010', 'Nina', 'Schulz', 'Vertrieb', 'nina.schulz@firma.de', 2),
('EMP011', 'Kevin', 'Klein', 'HR', 'kevin.klein@firma.de', 3),
('EMP012', 'Sophie', 'Richter', 'Marketing', 'sophie.richter@firma.de', 4),
('EMP013', 'Daniel', 'Lange', 'Entwicklung', 'daniel.lange@firma.de', 5);
GO

-- 3. Endgeräte hinzufügen (aktive und inaktive)
INSERT INTO Endgeraet (hersteller, modell, betriebssystem, osVersion, imei, status)
VALUES 
-- Laptops
('Dell', 'Latitude 7440', 'Windows 11', '23H2', 'DELL7440XYZ001', 'LAGER'),
('Apple', 'MacBook Pro 16"', 'macOS', 'Sonoma 14.3', 'MBP16M3ABC001', 'AKTIV'),
('Lenovo', 'ThinkPad X1 Carbon', 'Windows 11', '23H2', 'LENX1CARBON001', 'LAGER'),
('HP', 'EliteBook 840 G9', 'Windows 11', '22H2', 'HPELITE840001', 'AKTIV'),
('Microsoft', 'Surface Laptop 5', 'Windows 11', '23H2', 'SURFACE5001', 'LAGER'),
('Apple', 'MacBook Air M2', 'macOS', 'Sonoma 14.2', 'MBAM2XYZ002', 'AKTIV'),
('Dell', 'XPS 13', 'Windows 11', '23H2', 'DELLXPS13001', 'AKTIV'),
('Lenovo', 'ThinkPad T14', 'Windows 11', '22H2', 'LENOVOT14001', 'LAGER'),

-- Smartphones
('Apple', 'iPhone 14', 'iOS', '17.2', '123456789012345', 'LAGER'),
('Samsung', 'Galaxy S23', 'Android', '14', '987654321098765', 'AKTIV'),
('Google', 'Pixel 7', 'Android', '14', '555555555555555', 'LAGER'),
('Apple', 'iPhone 15 Pro', 'iOS', '17.3', '354876590123456', 'AKTIV'),
('Samsung', 'Galaxy S24 Ultra', 'Android', '14', '35111222333444', 'AKTIV'),
('Google', 'Pixel 8 Pro', 'Android', '14', '35666777888899', 'LAGER'),
('Apple', 'iPhone 14', 'iOS', '17.2', '35555666777788', 'AKTIV'),
('Samsung', 'Galaxy A54', 'Android', '13', '35999888777666', 'DEFEKT'),
('OnePlus', '11 5G', 'Android', '14', '35222333444555', 'LAGER'),
('Apple', 'iPhone SE', 'iOS', '17.1', '35444555666777', 'AKTIV'),
('Xiaomi', 'Redmi Note 13', 'Android', '13', '35888999000111', 'LAGER'),

-- Tablets
('Apple', 'iPad Pro 12.9"', 'iPadOS', '17.2', 'IPADPRO12001', 'AKTIV'),
('Samsung', 'Galaxy Tab S9', 'Android', '14', 'GALTABS9001', 'LAGER'),
('Microsoft', 'Surface Pro 9', 'Windows 11', '23H2', 'SURFPRO9001', 'AKTIV'),
('Apple', 'iPad Air', 'iPadOS', '17.1', 'IPADAIR5001', 'LAGER'),

-- Desktop-PCs
('HP', 'EliteDesk 800 G9', 'Windows 11', '23H2', 'DESKTOP001', 'AKTIV'),
('Dell', 'OptiPlex 7010', 'Windows 10', '22H2', 'DESKTOP002', 'AUSGESCHIEDEN'),
('Apple', 'Mac Studio', 'macOS', 'Sonoma 14.3', 'MACSTUDIO01', 'AKTIV'),
('Lenovo', 'ThinkCentre M70s', 'Windows 11', '23H2', 'DESKTOP003', 'LAGER');
GO

-- 4. Ausgaben (Aktive Zuordnungen)
INSERT INTO Ausgabe (endgeraetId, mitarbeiterId, ausgabedatum, ausgabeVon)
VALUES 
-- Max Mustermann (EMP001) - IT
(2, 1, '2024-01-15', 'IT-Admin'), -- Samsung Galaxy S23
(6, 1, '2024-02-01', 'IT-Admin'), -- Apple MacBook Pro 16"

-- Anna Musterfrau (EMP002) - Vertrieb
(9, 2, '2024-01-20', 'IT-Admin'), -- iPhone 15 Pro

-- Thomas Schmidt (EMP003) - IT
(10, 3, '2024-02-10', 'IT-Admin'), -- Samsung S24 Ultra
(22, 3, '2023-12-01', 'IT-Admin'), -- Apple Mac Studio

-- Sarah Müller (EMP004) - Marketing
(13, 4, '2024-01-25', 'IT-Support'), -- iPhone 14
(18, 4, '2024-02-05', 'IT-Support'), -- iPad Pro 12.9"

-- David Schneider (EMP005) - Entwicklung
(7, 5, '2024-01-10', 'IT-Admin'), -- MacBook Air M2
(21, 5, '2024-01-12', 'IT-Admin'), -- Surface Pro 9

-- Lisa Fischer (EMP006) - Finanzen
(23, 6, '2024-01-05', 'IT-Support'), -- HP EliteDesk

-- Markus Weber (EMP007) - Einkauf
(16, 7, '2024-02-15', 'IT-Support'), -- iPhone SE

-- Julia Hoffmann (EMP008) - Produktion
(4, 8, '2024-01-30', 'IT-Support'), -- HP EliteBook 840 G9

-- Alexander Bauer (EMP009) - IT
(8, 9, '2024-02-20', 'IT-Admin'), -- Dell XPS 13

-- Nina Schulz (EMP010) - Vertrieb
(11, 10, '2024-02-01', 'IT-Support'); -- Google Pixel 8 Pro
GO

-- 5. Mobilfunkverträge für aktive Smartphones
INSERT INTO Mobilfunkvertrag (vertragsnummer, provider, datenvolumen, kostenMonatlich, laufzeitBis, endgeraetId)
VALUES
('TEL-2024-001', 'Telekom', 50, 49.99, '2025-12-31', 2),
('TEL-2024-002', 'Vodafone', 100, 69.99, '2026-01-31', 9),
('TEL-2024-003', 'O2', 30, 29.99, '2025-06-30', 10),
('TEL-2024-004', '1&1', 40, 39.99, '2025-09-15', 13),
('TEL-2024-005', 'Telekom', 20, 24.99, '2024-12-31', 16),
('TEL-2024-006', 'Vodafone', 60, 54.99, '2025-08-31', 11);
GO

-- 6. Apps definieren
INSERT INTO App (name, hersteller, version, kategorie)
VALUES
('Microsoft 365', 'Microsoft', '16.0', 'Produktivität'),
('SAP Fiori', 'SAP', '2308', 'Business'),
('Salesforce', 'Salesforce', 'Winter 24', 'CRM'),
('Cisco AnyConnect', 'Cisco', '4.10', 'Sicherheit'),
('McAfee Endpoint Security', 'McAfee', '10.7', 'Sicherheit'),
('Adobe Creative Cloud', 'Adobe', '2024', 'Kreativ'),
('Slack', 'Slack', '4.35', 'Kommunikation'),
('Zoom', 'Zoom', '5.17', 'Kommunikation'),
('Tableau', 'Salesforce', '2023.3', 'Analytics'),
('Jira', 'Atlassian', '9.12', 'Projektmanagement'),
('VPN Client', 'Company Internal', '2.5', 'Sicherheit'),
('Anti-Virus Corporate', 'Company Internal', '3.1', 'Sicherheit'),
('BI Dashboard', 'Company Internal', '1.2', 'Business');
GO

-- 7. Installationen auf aktiven Geräten
INSERT INTO Installation (endgeraetId, appId, installiertAm, berechtigungen)
VALUES
-- Max Mustermanns Geräte
(2, 4, '2024-01-16', 'Vollzugriff'),
(2, 5, '2024-01-16', 'Standard'),
(6, 1, '2024-02-02', 'Vollzugriff'),
(6, 11, '2024-02-02', 'Admin'),

-- Anna Musterfraus Smartphone
(9, 3, '2024-01-21', 'Standard'),
(9, 7, '2024-01-21', 'Standard'),

-- David Schneiders MacBook
(7, 1, '2024-01-11', 'Vollzugriff'),
(7, 6, '2024-01-11', 'Standard'),
(7, 10, '2024-01-11', 'Admin');
GO

-- 8. Richtlinien definieren
INSERT INTO Richtlinie (name, beschreibung, regel)
VALUES
('Passwortrichtlinie', 'Mindestanforderungen für Passwörter', 'Passwort >= 8 Zeichen, Groß-/Kleinbuchstaben, Zahlen, Sonderzeichen'),
('Verschlüsselung', 'Volle Festplattenverschlüsselung erforderlich', 'BitLocker/FileVault aktiviert'),
('OS-Version', 'Mindest-Betriebssystemversion', 'Windows >= 10 22H2, iOS >= 16, Android >= 12'),
('Antivirus', 'Aktiver Virenschutz erforderlich', 'Antivirus installiert und aktuell'),
('VPN-Pflicht', 'VPN für externen Zugriff', 'VPN Client installiert und konfiguriert'),
('App-Whitelist', 'Nur genehmigte Apps installieren', 'Nur Apps aus Unternehmens-App-Store');
GO

-- 9. Compliance-Prüfungen
INSERT INTO CompliancePruefung (endgeraetId, policyId, erfuellt, geprueftAm, bemerkung)
VALUES
(2, 1, 1, '2024-02-28', 'Passwortrichtlinie erfüllt'),
(2, 3, 1, '2024-02-28', 'Android 14 - aktuell'),
(2, 4, 1, '2024-02-28', 'McAfee aktiv'),
(6, 1, 1, '2024-02-28', 'FileVault aktiviert'),
(6, 2, 1, '2024-02-28', 'Verschlüsselung aktiv'),
(9, 3, 1, '2024-02-28', 'iOS 17.3 - aktuell'),
(9, 4, 0, '2024-02-28', 'Kein Antivirus auf iOS'),
(15, 3, 0, '2024-02-28', 'Android 13 - Update erforderlich');
GO

-- Status der Endgeräte aktualisieren basierend auf Ausgaben
UPDATE Endgeraet 
SET status = 'AKTIV' 
WHERE id IN (
    SELECT DISTINCT endgeraetId 
    FROM Ausgabe 
    WHERE rueckgabedatum IS NULL
);
GO