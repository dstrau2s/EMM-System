# EMM-Java-Client - Enterprise Mobility Management System

## 📋 Projektübersicht
Ein Java-basiertes Konsolen-Programm zur Verwaltung von mobilen Endgeräten in Unternehmen.
Das System ermöglicht die vollständige Verwaltung von Smartphones, Tablets und Laptops inklusive Compliance-Checks,
Reporting und Audit-Logging.

## 🎯 Hauptfunktionen

### 1. **Gerätemanagement**
- **Neue Geräte erfassen** - Komplette Erfassung mit Hersteller, Modell, OS-Version, IMEI
- **Geräte ausgeben** - Zuordnung zu Mitarbeitern mit Nachverfolgung
- **Geräte zurücknehmen** - Rückgabe ins Lager
- **Geräte entfernen** - Soft-Delete mit Nachverfolgung

### 2. **Reporting & Monitoring**
- **Monatsreport** - Kostenübersicht pro Abteilung
- **Verfügbare Geräte** - Übersicht über Lagerbestand
- **Mitarbeiter-Geräte** - Alle Geräte eines Mitarbeiters anzeigen

### 3. **Compliance & Security**
- **Compliance-Checks** - Automatische Prüfung gegen Sicherheitsrichtlinien
- **Audit-Log** - Vollständiges Änderungsprotokoll aller Aktionen
- **Trigger-Tests** - Test der automatischen Audit-Log-Erstellung

### 4. **Datenbank-Verwaltung**
- **Alle Tabellen anzeigen** - Datenbank-Struktur
- **Views anzeigen** - Vordefinierte Abfragen
- **Datenbank-Info** - Technische Details

## 🖥️ Konsolen-Oberfläche - Hauptmenu:

	=== EMM DATENBANK-MANAGER ===

    1. sp_NeuesGerätErfassen - Neues Gerät erfassen
	2. sp_GerätEntfernen_Einfach - Gerät entfernen (Soft-Delete)
    3. sp_GerätAusgeben - Gerät an Mitarbeiter ausgeben
    4. sp_GerätZuruecknehmen - Gerät zurücknehmen
    5. sp_GetMitarbeiterGeräte - Geräte eines Mitarbeiters
    6. sp_GetVerfügbareGeräte - Verfügbare Geräte im Lager
    7. sp_Monatsreport - Monatlichen Kostenreport
    8. sp_DemoComplianceCheck - Compliance-Check für Richtlinie
    9. sp_AlleComplianceChecks - Alle Compliance-Checks
    10 .Views anzeigen (V_AktiveGeräte, V_KostenProAbteilung, etc.)
    11. Tabellen anzeigen (Endgeraet, Mitarbeiter, etc.)
    12. AUDITLOG - Änderungsprotokoll anzeigen
    13. AUDITLOG TEST - Trigger testen
    14. Datenbank-Informationen
    15. Beenden
	

## 📊 Beispiel-Interaktionen

## Beispiel 1: Neues Gerät erfassen

	SP_NEUESGERÄTERFASSEN - Neues Gerät erfassen
	=============================================
	Hersteller (z.B. Apple, Samsung, Dell): Apple
	Modell (z.B. iPhone 15, Galaxy S24, XPS 13): iPhone 15 Pro
	Betriebssystem (Windows, iOS, Android, macOS): iOS
	OS Version (z.B. 17.2, 14, 23H2): 17.2
	IMEI/Seriennummer: 123456789012345
	Status (LAGER/AKTIV/DEFEKT/AUSGESCHIEDEN) [LAGER]: LAGER

	Erfasse neues Gerät...

	✓ Erfolg! Neues Gerät erfasst mit ID: 42
	Hersteller: Apple
	Modell: iPhone 15 Pro
	Status: LAGER



## Beispiel 2: Gerät an Mitarbeiter ausgeben

	SP_GERÄTAUSGEBEN - Gerät an Mitarbeiter ausgeben
	=================================================
	Geräte-ID: 42
	Mitarbeiter-ID: 101
	Ausgegeben von (z.B. IT-Admin, IT-Support): IT-Admin

	✓ Erfolg: Gerät erfolgreich an Mitarbeiter ausgegeben
	text


## Beispiel 3: Compliance-Check durchführen

	SP_DEMOCOMPLIANCECHECK - Compliance-Check für Richtlinie
	=========================================================

	Verfügbare Richtlinien:
	ID   Name                     Beschreibung
	--------------------------------------------------
	1    OS-Version-Richtlinie    Mindestanforderung iOS 17.0 / Android 13
	2    Antivirus-Policy         Antivirus muss installiert und aktiv sein
	3    Encryption-Policy        Vollständige Geräteverschlüsselung erforderlich
	4    Passwort-Policy          Mindestpasswortlänge 8 Zeichen, Komplexität
	5    App-Whitelist           Nur genehmigte Business-Apps erlaubt
	6    MDM-Enrollment          Gerät muss im MDM registriert sein

	Richtlinien-ID für Compliance-Check: 1

	Führe Compliance-Check durch...

	COMPLIANCE-CHECK ERGEBNIS
	==========================================
	Geprüfte Richtlinie: OS-Version-Richtlinie
	Anzahl Geräte: 187
	Erfüllt: 172
	Nicht erfüllt: 15
	Erfüllungsquote: 92.0%
	Prüfungszeitpunkt: 2024-02-15 10:45:22.350

	Details der Compliance-Prüfungen:
	GerätID  Gerät                 Erfüllt    Geprüft am   Bemerkung
	---------------------------------------------------------------
	127      iPhone 15 Pro         ✓          2024-02-15   iOS 17.3.1 - konform
	118      Samsung Galaxy S23    ✓          2024-02-15   Android 14 - konform
	95       Google Pixel 7        ✓          2024-02-15   Android 14 - konform
	83       iPhone 14             ✓          2024-02-15   iOS 17.2.1 - konform
	76       Samsung Galaxy S22    ✗          2024-02-15   Android 12 - Update erforderlich
	64       iPhone 13             ✓          2024-02-15   iOS 17.0   - minimal konform
	52       OnePlus 9             ✗          2024-02-15   Android 11 - Sicherheitsupdate dringend
	41       iPhone 12             ✓          2024-02-15   iOS 17.1.2 - konform
	33       Samsung Galaxy A54    ✓          2024-02-15   Android 14 - konform
	22       Google Pixel 6a       ✓          2024-02-15   Android 14 - konform
	19       iPhone 11             ✗          2024-02-15   iOS 15.7   - Update nicht verfügbar
	14       Samsung Galaxy S21    ✗          2024-02-15   Android 13 - Update blockiert
	9        iPhone SE (2020)      ✓          2024-02-15   iOS 17.2   - konform
	5        Motorola Edge 30      ✓          2024-02-15   Android 13 - konform
	2        iPhone XR             ✗          2024-02-15   iOS 16.7   - Hardware-Limit

	Angezeigt: 15 von 187 Prüfungen

## Beispiel 4: Audit-Log anzeigen

	AUDITLOG - ÄNDERUNGSPROTOKOLL
	==========================================

	ID Tabelle Aktion Alt Neu Zeitpunkt Benutzer

	42 Endgeraet Statusänderung LAGER AKTIV 2024-12-09 14:30:15 IT-Admin
	41 Endgeraet Statusänderung AKTIV LAGER 2024-12-09 14:25:10 System
	40 Endgeraet Statusänderung LAGER AKTIV 2024-12-09 14:20:05 IT-Admin

	AUDITLOG STATISTIK

	Gesamteinträge: 42
	Überwachte Tabellen: 3
	Erster Eintrag: 2024-11-01 09:00:00.000
	Letzter Eintrag: 2024-12-09 14:30:15.123
	Überwachte Benutzer: 2
	Statusänderungen (Endgeräte): 28

	Top 5 Aktionen:
	Statusänderung: 28
	INSERT: 8
	UPDATE: 6

	SP_MONATSREPORT - Monatlicher Kostenreport
	===========================================
	Monat (1-12, leer für aktuellen Monat): 02
	Jahr (z.B. 2024, leer für aktuelles Jahr): 2024

	Generiere Report für 2/2024...

	MONATSREPORT 02/2024
	================================================================================

	Abteilung           Geräte        Kosten (€)    Durchschn. Datenvol.    Budget (€)    Budget-Rest (€)
	---------------------------------------------------------------------------------------
	IT                  38            8,120.00      4.5 GB                 9,500.00      1,380.00
	Vertrieb            25            5,875.30      7.2 GB                 6,500.00      624.70
	Entwicklung         22            4,840.00      8.1 GB                 5,000.00      160.00
	Marketing           18            3,780.45      5.3 GB                 4,200.00      419.55
	HR                  12            2,520.00      2.8 GB                 3,000.00      480.00
	Support             8             1,680.00      3.6 GB                 2,000.00      320.00

	================================================================================
	SUMME:              6             26,815.75                             30,200.00     3,384.25
	================================================================================


## Features

    - JDBC-Verbindung - Sichere Verbindung zu SQL Server
    - Parametrisierte Queries - Schutz vor SQL-Injection
    - Transaktionssicher - Fehlerbehandlung mit Rollback
    - Logging - Umfassende Fehlerprotokollierung
    - Audit-Trail - Vollständige Änderungsnachverfolgung

## Sicherheitsfeatures
	Audit-Logging

    - Automatische Protokollierung aller Änderungen
    - Trigger-basiert für Endgeräte-Statusänderungen
    - Filterbare Anzeige im Client
    - Statistik und Berichte

	Compliance-Checks

    - Automatische Prüfung gegen Richtlinien
    - Detailierte Ergebnisberichte
    - Historische Nachverfolgung
    - Erfüllungsquote-Berechnung

	Datenintegrität

    - Foreign Key Constraints
    - Check Constraints für Statuswerte
    - Transaction Handling
    - Rollback bei Fehlern

## Reporting-Features
	Verfügbare Reports

    - Echtzeit-Lagerbestand - Verfügbare Geräte im Lager
    - Mitarbeiter-Equipment - Alle Geräte pro Mitarbeiter
    - Monatliche Kosten - Budget-Übersicht pro Abteilung
    - Compliance-Status - Sicherheitsrichtlinien-Erfüllung
    - Audit-Trail - Kompletter Änderungsverlauf

	Filteroptionen

    - Zeitraum-Filter (Datum, Monat, Jahr)
    - Abteilungs-Filter
    - Status-Filter
    - Benutzer-Filter

## Setup & Konfiguration
	Voraussetzungen

    SQL Server mit EMM_Demo Datenbank
    JDBC Driver für SQL Server
    Java 8+ Runtime
    Datenbank-Benutzer mit entsprechenden Rechten

## Konfigurationsdatei

Die Verbindungsparameter sind hartcodiert in der Hauptklasse:

	// In EMMDatabaseManager.java
	private static final String SERVER = "localhost\\SQLEXPRESS";
	private static final String DATABASE = "EMM_Demo";
	private static final String USERNAME = "emm_user";
	private static final String PASSWORD = "emm_x123";

## Fehlerbehandlung

Das System bietet umfassende Fehlerbehandlung:

Verbindungsfehler
	✗ SQL FEHLER:
	  Message: Login failed for user 'emm_user'
	  SQL State: 28000
	  Error Code: 18456

	=== FEHLERBEHEBUNG ===
	LOGIN FEHLGESCHLAGEN
	Überprüfe:
	1. SQL Server Authentication aktiviert?
	2. Login 'emm_user' existiert?
	3. Passwort korrekt?
	
Datenbankfehler
	✗ FEHLER: Das Gerät ist nicht verfügbar oder nicht im Lager.
	
Validierungsfehler
	✗ Ungültiger Status! Erlaubt: LAGER, AKTIV, DEFEKT, AUSGESCHIEDEN

## Lizenz & Hinweise

Dieses Projekt ist Teil eines Enterprise Mobility Management Systems. Es demonstriert:

    - Enterprise-Grade Datenbankanbindung
    - Umfassendes Audit-Logging
    - Compliance-Management
    - Reporting-Funktionen

Die Software ist für Demonstrationszwecke konzipiert und zeigt Best Practices für:

    - Sichere Datenbankanbindung
    - Transaktionsmanagement
    - Benutzerfreundliche Konsolenschnittstelle
    - Umfassende Fehlerbehandlung

Hinweis: Dieses Projekt erfordert eine entsprechende SQL Server Datenbank-Struktur mit Tabellen, 
Views und Stored Procedures. 