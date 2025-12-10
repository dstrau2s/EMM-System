# EMM-Java-Client - Enterprise Mobility Management System (Objektorientierte Version)


Dieser Branch enthält eine komplett überarbeitete, objektorientierte Version des ursprünglichen monolithischen EMM-Clients.
Die Anwendung wurde in eine moderne, wartbare Architektur umgewandelt, die Best Practices für Software-Design folgt.

## Was hat sich geändert?

### **Von Monolith zu Modularer Architektur**

| Vorher (Main-Branch)  			| Nachher (OO-Branch)   				|
|-----------------------------------|---------------------------------------|
| Eine große Klasse 			 	| 10+ spezialisierte Klassen 			|
| Hohe Kopplung, schwer testbar 	| Geringe Kopplung, einfach testbar 	|
| Mix von Logik & UI 				| Saubere Trennung (Repository Pattern) |
| Direkte SQL-Aufrufe 				| Abstrahierte Datenzugriffsschicht 	|

## Neue Struktur

	src/main/java/de/emm/demo/
	├── 📁 database/
	│ └── EMMDatabaseConnection.java # Verbindungsmanagement
	├── 📁 model/ # Datenmodelle
	│ ├── Device.java
	│ ├── AuditLogEntry.java
	│ ├── AuditStatistic.java
	│ ├── AuditTableInfo.java
	│ ├── DeviceAssignment.java
	│ ├── MonthlyReport.java
	│ └── ComplianceCheck.java
	├── 📁 repository/ # Datenzugriff
	│ ├── DeviceRepository.java # Alle Geräte-Operationen
	│ └── AuditRepository.java # Alle AuditLog-Operationen
	├── 📁 ui/
	│ └── MenuManager.java # Benutzeroberfläche
	└── EMMDatabaseManager.java # Hauptklasse
	
## Neue Features in dieser Version

## **1. Vollständige Objektorientierung**
- Starke Typisierung mit Java-Klassen
- Saubere Trennung von Datenzugriff und Business-Logik
- Lose Kopplung zwischen Komponenten

## **2. Verbessertes Audit-Logging** 

// Vorher: Nur Statusänderung
"Aktueller Status: LAGER → AKTIV"

// Nachher: Mit Geräte-Referenz
"Gerät ID:42 (iPhone 15 Pro) - Status: LAGER → AKTIV"

3. Erweiterte Filterung

- Geräte-basierte Filter: Zeige nur Änderungen für bestimmte Geräte
- Flexible Zeitfilter: Nach Datumsbereichen filtern
- Benutzer-spezifisch: Alle Aktionen eines bestimmten Users

4. Detaillierte Statistiken

	AuditStatistic stats = auditRepo.getAuditStatistics();
	System.out.println("Aktivste Geräte: " + stats.getMostAuditedDevices());
	System.out.println("Durchschnittl. Änderungen/Tag: " + stats.getAverageEntriesPerDay());

## Vorteile der neuen Architektur
Für Entwickler:

- Single Responsibility Principle: Jede Klasse hat eine klare Aufgabe
- Einfach testbar: Repositories können mit Mocks getestet werden
- Wiederverwendbar: Code kann in anderen Projekten verwendet werden
- Erweiterbar: Neue Features durch neue Klassen, nicht Änderungen

Für Wartung:

- Einfache Fehlerbehebung: Klare Trennung vereinfacht Debugging
- Bessere Skalierbarkeit: Neue Module einfach hinzufügbar
- Refactoring-freundlich: Änderungen isoliert in einzelnen Klassen

Für Performance:

- Connection Pooling: Effizientes Verbindungsmanagement
- Optimierte Queries: Repository-Layer ermöglicht Query-Optimierung
- Lazy Loading: Geräteinformationen nur bei Bedarf laden

## Vergleich der Implementierungen
	Vorher (Main Branch):

	// Alles in einer Methode
	private static void call_sp_NeuesGerätErfassen(Scanner scanner, Connection conn) {
		// UI-Logik
		System.out.print("Hersteller: ");
		String hersteller = scanner.nextLine();
		
		// Datenbank-Logik
		try (CallableStatement cstmt = conn.prepareCall("{call sp_NeuesGerätErfassen(...)}")) {
			cstmt.setString(1, hersteller);
			// ...
		}
		// Fehlerbehandlung
		catch (SQLException e) {
			System.err.println("✗ Fehler: " + e.getMessage());
    }
}

	Nachher - OO Version (Dieser Branch):

	// MenuManager.java - Nur UI-Logik
	private void handleNewDevice() {
		System.out.print("Hersteller: ");
		String hersteller = scanner.nextLine();
		
		try {
			// Repository für Datenzugriff
			Device device = deviceRepo.createDevice(hersteller, modell, os, version, imei, status);
			System.out.println("✓ Erfolg! Neues Gerät: " + device);
			
		} catch (SQLException e) {
			System.err.println("✗ Fehler: " + e.getMessage());
		}
	}

	// DeviceRepository.java - Nur Datenzugriff
	public Device createDevice(String hersteller, String modell, ...) throws SQLException {
		String sql = "{call sp_NeuesGerätErfassen(?, ?, ?, ?, ?, ?)}";
		try (CallableStatement cstmt = connection.prepareCall(sql)) {
			cstmt.setString(1, hersteller);
			// ...
			return new Device(id, hersteller, modell, ...);
		}
	}

🚀 Schnellstart
Voraussetzungen

# Gleiche wie Main-Branch, plus:
- Java 11+ (für moderne OO-Features)
- Maven 3.6+ (für Dependency Management)
- SQL Server mit EMM_Demo Datenbank

Installation

# Branch wechseln
git checkout objektorientiert

# Kompilieren
mvn clean compile

# Ausführen
mvn exec:java -Dexec.mainClass="de.emm.demo.EMMDatabaseManager"

## Testing (Neu in dieser Version)
	Unit Tests einfach möglich:

	@Test
	public void testCreateDevice() {
		// Mock Connection
		Connection mockConn = Mockito.mock(Connection.class);
		DeviceRepository repo = new DeviceRepository(mockConn);
		
		// Test Logik ohne echte Datenbank
		// ...
	}

	Integration Tests:
	java

	@Test
	public void testAuditLogIntegration() {
		// Repository mit echter DB testen
		AuditRepository auditRepo = new AuditRepository(realConnection);
		List<AuditLogEntry> entries = auditRepo.getRecentAuditLogs(10);
		
		assertNotNull(entries);
		assertTrue(entries.size() <= 10);
	}

## Migration vom Main Branch
Für bestehende Benutzer:

    Datenbank-Schema erweitern (optional):

	ALTER TABLE AuditLog ADD endgeraet_id INT NULL;
		Keine Änderungen an Stored Procedures nötig
		Alle Funktionen erhalten - 1:1 Kompatibilität

## Neue Features nutzen:

	// Alte Nutzung (kompatibel)
	List<AuditLogEntry> entries = auditRepo.getAllAuditLogs();

	// Neue Features
	List<AuditLogEntry> entries = auditRepo.getAllAuditLogsWithDevices();
	List<AuditLogEntry> deviceHistory = auditRepo.getDeviceHistory(42);
	AuditStatistic stats = auditRepo.getAuditStatistics();
	
	
## Roadmap & Erweiterungen

Geplant für nächste Version:

- Spring Framework Integration für Dependency Injection
- REST API für Web-Zugriff
- Docker Container für einfache Deployment
- Grafische Benutzeroberfläche (JavaFX/Web)
- Export-Funktionen (PDF, Excel, CSV)

Mögliche Erweiterungen:

- Notification-System: E-Mail-Benachrichtigungen bei Compliance-Verstößen
- Batch-Processing: Massen-Updates von Geräten
- API-Gateway: Integration mit anderen Systemen
- Mobile App: Verwaltung per Smartphone

## API-Dokumentation:

	/**
	 * Holt den kompletten Änderungsverlauf eines Geräts
	 * @param deviceId Die ID des Geräts
	 * @return Liste aller AuditLog-Einträge für das Gerät
	 * @throws SQLException Bei Datenbankfehlern
	 */
	public List<AuditLogEntry> getDeviceHistory(int deviceId) throws SQLException {
		// Implementation
	}

Architektur-Diagramme:

	[UI Layer] → [Service Layer] → [Repository Layer] → [Database]
	   ↑              ↑                  ↑
	MenuManager    Business Logic    Data Access

## Best Practices in dieser Implementierung

- SOLID Principles umgesetzt
- Design Patterns: Repository, MVC, Dependency Injection
- Lesbarer, wartbarer Code
- Defensive Programming: Robust gegen Fehler

