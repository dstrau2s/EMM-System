use master
GO

-- Prüfen ob Datenbank existiert und löschen
IF DB_ID(N'EMM_Demo') IS NOT NULL
BEGIN
    ALTER DATABASE [EMM_Demo] SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE [EMM_Demo];
END
GO

-- Datenbank neu erstellen
CREATE DATABASE [EMM_Demo] 
ON Primary
(
    Name = N'EMM_Demo',
    Filename = N'C:\Program Files\Microsoft SQL Server\MSSQL16.SQLEXPRESS\MSSQL\DATA\EMM_Demo.mdf',
    Size = 100MB,
    Maxsize = UNLIMITED,
    Filegrowth = 5MB
)
LOG ON
(
    Name = N'EMM_Demo_Log',
    Filename = N'C:\Program Files\Microsoft SQL Server\MSSQL16.SQLEXPRESS\MSSQL\DATA\EMM_Demo.ldf',
    Size = 5MB,
    Maxsize = UNLIMITED,
    Filegrowth = 1MB
)
GO

use EMM_Demo
GO

-- 1. Tabelle: Endgeraet (mit Status als CHECK-Constraint)
CREATE TABLE Endgeraet (
    id INT IDENTITY(1,1) PRIMARY KEY,
    hersteller NVARCHAR(100) NOT NULL,
    modell NVARCHAR(100) NOT NULL,
    betriebssystem NVARCHAR(50) NOT NULL,
    osVersion NVARCHAR(50),
    imei NVARCHAR(20) UNIQUE,
    status NVARCHAR(20) CHECK (status IN ('LAGER', 'AKTIV', 'DEFEKT', 'AUSGESCHIEDEN')) DEFAULT 'LAGER',
    hinzugefuegtAm DATE DEFAULT CAST(GETDATE() AS DATE),
    entferntAm DATE NULL,
    entferntGrund NVARCHAR(200) NULL
);
GO

-- 2. Tabelle: Mitarbeiter
CREATE TABLE Mitarbeiter (
    id INT IDENTITY(1,1) PRIMARY KEY,
    personalnummer NVARCHAR(20) UNIQUE NOT NULL,
    vorname NVARCHAR(100) NOT NULL,
    nachname NVARCHAR(100) NOT NULL,
    abteilung NVARCHAR(100),
    email NVARCHAR(255)
);
GO

-- 3. Tabelle: Ausgabe
CREATE TABLE Ausgabe (
    id INT IDENTITY(1,1) PRIMARY KEY,
    endgeraetId INT FOREIGN KEY REFERENCES Endgeraet(id),
    mitarbeiterId INT FOREIGN KEY REFERENCES Mitarbeiter(id),
    ausgabedatum DATE NOT NULL,
    rueckgabedatum DATE NULL,
    ausgabeVon NVARCHAR(100),
    CONSTRAINT CHK_Rueckgabe CHECK (rueckgabedatum IS NULL OR rueckgabedatum >= ausgabedatum)
);
GO

-- 4. Tabelle: Mobilfunkvertrag
CREATE TABLE Mobilfunkvertrag (
    id INT IDENTITY(1,1) PRIMARY KEY,
    vertragsnummer NVARCHAR(50) UNIQUE NOT NULL,
    provider NVARCHAR(100) NOT NULL,
    datenvolumen INT NOT NULL, -- in GB
    kostenMonatlich DECIMAL(10,2) NOT NULL,
    laufzeitBis DATE NOT NULL,
    endgeraetId INT UNIQUE FOREIGN KEY REFERENCES Endgeraet(id), -- 1:1 Beziehung
    CONSTRAINT CHK_Datenvolumen CHECK (datenvolumen >= 0),
    CONSTRAINT CHK_Kosten CHECK (kostenMonatlich >= 0)
);
GO

-- 5. Tabelle: App
CREATE TABLE App (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(200) NOT NULL,
    hersteller NVARCHAR(100),
    version NVARCHAR(50),
    kategorie NVARCHAR(50)
);
GO

-- 6. Tabelle: Installation
CREATE TABLE Installation (
    id INT IDENTITY(1,1) PRIMARY KEY,
    endgeraetId INT FOREIGN KEY REFERENCES Endgeraet(id),
    appId INT FOREIGN KEY REFERENCES App(id),
    installiertAm DATE NULL,
    berechtigungen NVARCHAR(500),
    CONSTRAINT UQ_Endgeraet_App UNIQUE (endgeraetId, appId) -- Verhindert doppelte Installation
);
GO

-- 7. Tabelle: Richtlinie
CREATE TABLE Richtlinie (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(200) NOT NULL,
    beschreibung NVARCHAR(1000),
    regel NVARCHAR(500) -- SQL-basierte Regel oder Logikbeschreibung
);
GO

-- 8. Tabelle: CompliancePruefung
CREATE TABLE CompliancePruefung (
    id INT IDENTITY(1,1) PRIMARY KEY,
    endgeraetId INT FOREIGN KEY REFERENCES Endgeraet(id),
    policyId INT FOREIGN KEY REFERENCES Richtlinie(id),
    erfuellt BIT NOT NULL, -- 1 = erfüllt, 0 = nicht erfüllt
    geprueftAm DATE NOT NULL,
    bemerkung NVARCHAR(500)
);
GO

-- 9. Optional: Abteilungstabelle für Erweiterung
CREATE TABLE Abteilung (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL,
    budgetMobilfunk DECIMAL(12,2) DEFAULT 0
);
GO

-- 10. Optional: Update der Mitarbeiter-Tabelle mit Foreign Key
-- ZUERST muss die Spalte hinzugefügt werden
ALTER TABLE Mitarbeiter
ADD abteilungId INT;
GO

-- DANACH der Foreign Key
ALTER TABLE Mitarbeiter
ADD CONSTRAINT FK_Mitarbeiter_Abteilung FOREIGN KEY (abteilungId) REFERENCES Abteilung(id);
GO