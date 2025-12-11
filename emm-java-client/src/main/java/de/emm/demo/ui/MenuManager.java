// MenuManager.java (vollständig mit Services)
package de.emm.demo.ui;

import de.emm.demo.dto.CreateDeviceRequest;
import de.emm.demo.dto.DeviceResponse;
import de.emm.demo.exception.EMMException;
import de.emm.demo.service.DeviceService;
import de.emm.demo.service.ComplianceService;
import de.emm.demo.service.ReportService;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class MenuManager {
    
    private final Scanner scanner;
    private final DeviceService deviceService;
    private final ComplianceService complianceService;
    private final ReportService reportService;
    private final Connection connection; // Für direkte DB-Abfragen
    private boolean running = true;
    
    public MenuManager(Scanner scanner, DeviceService deviceService, 
                      ComplianceService complianceService, ReportService reportService,
                      Connection connection) {
        this.scanner = scanner;
        this.deviceService = deviceService;
        this.complianceService = complianceService;
        this.reportService = reportService;
        this.connection = connection;
    }
    
    public void run() {
        while (running) {
            showMainMenu();
            String choice = scanner.nextLine();
            handleMenuChoice(choice);
        }
    }
    
    private void showMainMenu() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("=== EMM DATENBANK-MANAGER (Service-Version) ===");
        System.out.println("=".repeat(60));
        System.out.println(" 1. Neues Gerät erfassen");
        System.out.println(" 2. Gerät entfernen (Soft-Delete)");
        System.out.println(" 3. Gerät an Mitarbeiter ausgeben");
        System.out.println(" 4. Gerät zurücknehmen");
        System.out.println(" 5. Geräte eines Mitarbeiters anzeigen");
        System.out.println(" 6. Verfügbare Geräte im Lager anzeigen");
        System.out.println(" 7. Monatlichen Kostenreport generieren");
        System.out.println(" 8. Compliance-Check für Richtlinie");
        System.out.println(" 9. Alle Compliance-Checks durchführen");
        System.out.println("10. Views anzeigen (V_AktiveGeräte, V_KostenProAbteilung, etc.)");
        System.out.println("11. Tabellen anzeigen (Endgeraet, Mitarbeiter, etc.)");
        System.out.println("12. AUDITLOG - Änderungsprotokoll anzeigen");
        System.out.println("13. AUDITLOG TEST - Trigger testen");
        System.out.println("14. Datenbank-Informationen");
        System.out.println("15. Gerätestatistiken");
        System.out.println("16. Beenden");
        System.out.print("\nWähle eine Option (1-16): ");
    }
    
    private void handleMenuChoice(String choice) {
        switch (choice) {
            case "1":
                createNewDevice();
                break;
            case "2":
                removeDevice();
                break;
            case "3":
                assignDeviceToEmployee();
                break;
            case "4":
                returnDevice();
                break;
            case "5":
                getEmployeeDevices();
                break;
            case "6":
                getAvailableDevices();
                break;
            case "7":
                generateMonthlyReport();
                break;
            case "8":
                performComplianceCheck();
                break;
            case "9":
                performAllComplianceChecks();
                break;
            case "10":
                showAllViews();
                break;
            case "11":
                showAllTables();
                break;
            case "12":
                showAuditLog();
                break;
            case "13":
                testAuditLogTrigger();
                break;
            case "14":
                showDatabaseInfo();
                break;
            case "15":
                showDeviceStatistics();
                break;
            case "16":
                System.out.println("\nProgramm wird beendet. Auf Wiedersehen!");
                running = false;
                break;
            default:
                System.out.println("Ungültige Eingabe! Bitte 1-16 wählen.");
        }
    }
    
    // ============================================================
    // 1. Neues Gerät erfassen (mit Service)
    // ============================================================
    private void createNewDevice() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("NEUES GERÄT ERFASSEN");
        System.out.println("=".repeat(60));
        
        try {
            CreateDeviceRequest request = new CreateDeviceRequest();
            
            System.out.print("Hersteller (z.B. Apple, Samsung, Dell): ");
            request.setManufacturer(scanner.nextLine());
            
            System.out.print("Modell (z.B. iPhone 15, Galaxy S24, XPS 13): ");
            request.setModel(scanner.nextLine());
            
            System.out.print("Betriebssystem (Windows, iOS, Android, macOS): ");
            request.setOs(scanner.nextLine());
            
            System.out.print("OS Version (z.B. 17.2, 14, 23H2): ");
            request.setOsVersion(scanner.nextLine());
            
            System.out.print("IMEI/Seriennummer: ");
            request.setImei(scanner.nextLine());
            
            System.out.print("Status (LAGER/AKTIV/DEFEKT/AUSGESCHIEDEN) [LAGER]: ");
            String status = scanner.nextLine();
            if (!status.isEmpty()) {
                request.setStatus(status.toUpperCase());
            }
            
            System.out.println("\nErfasse neues Gerät...");
            DeviceResponse response = deviceService.createDevice(request);
            
            System.out.println("\n✓ Erfolg! Neues Gerät erfasst:");
            System.out.println("  ID: " + response.getId());
            System.out.println("  Gerät: " + response.getFullName());
            System.out.println("  Betriebssystem: " + response.getOs() + " " + response.getOsVersion());
            System.out.println("  Status: " + response.getStatus());
            System.out.println("  IMEI: " + response.getImei());
            if (response.getFormattedCreatedAt() != null) {
                System.out.println("  Erstellt: " + response.getFormattedCreatedAt());
            }
            
        } catch (EMMException e) {
            System.err.println("\n✗ Fehler [" + e.getErrorCode() + "]: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("\n✗ Allgemeiner Fehler: " + e.getMessage());
        }
    }
    
    // ============================================================
    // 2. Gerät entfernen (Soft-Delete) (mit Service)
    // ============================================================
    private void removeDevice() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("GERÄT ENTFERNEN (SOFT-DELETE)");
        System.out.println("=".repeat(60));
        
        try {
            System.out.print("Geräte-ID zum Entfernen: ");
            int deviceId = Integer.parseInt(scanner.nextLine());
            
            System.out.print("Entfernungsgrund (optional): ");
            String reason = scanner.nextLine();
            
            System.out.println("\nEntferne Gerät...");
            String result = deviceService.removeDevice(deviceId, reason);
            
            System.out.println("\n✓ " + result);
            
        } catch (NumberFormatException e) {
            System.out.println("✗ Ungültige Geräte-ID!");
        } catch (EMMException e) {
            System.err.println("\n✗ Fehler [" + e.getErrorCode() + "]: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("\n✗ Allgemeiner Fehler: " + e.getMessage());
        }
    }
    
    // ============================================================
    // 3. Gerät an Mitarbeiter ausgeben (mit Service)
    // ============================================================
    private void assignDeviceToEmployee() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("GERÄT AN MITARBEITER AUSGEBEN");
        System.out.println("=".repeat(60));
        
        try {
            System.out.print("Geräte-ID: ");
            int deviceId = Integer.parseInt(scanner.nextLine());
            
            System.out.print("Mitarbeiter-ID: ");
            int employeeId = Integer.parseInt(scanner.nextLine());
            
            System.out.print("Ausgegeben von (z.B. IT-Admin, IT-Support): ");
            String issuedBy = scanner.nextLine();
            
            System.out.println("\nGebe Gerät aus...");
            String result = deviceService.assignDeviceToEmployee(deviceId, employeeId, issuedBy);
            
            System.out.println("\n✓ " + result);
            
        } catch (NumberFormatException e) {
            System.out.println("✗ Ungültige ID!");
        } catch (EMMException e) {
            System.err.println("\n✗ Fehler [" + e.getErrorCode() + "]: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("\n✗ Allgemeiner Fehler: " + e.getMessage());
        }
    }
    
    // ============================================================
    // 4. Gerät zurücknehmen (mit Service)
    // ============================================================
    private void returnDevice() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("GERÄT ZURÜCKNEHMEN");
        System.out.println("=".repeat(60));
        
        try {
            System.out.print("Geräte-ID zur Rücknahme: ");
            int deviceId = Integer.parseInt(scanner.nextLine());
            
            System.out.println("\nNehme Gerät zurück...");
            String result = deviceService.returnDevice(deviceId);
            
            System.out.println("\n✓ " + result);
            
        } catch (NumberFormatException e) {
            System.out.println("✗ Ungültige Geräte-ID!");
        } catch (EMMException e) {
            System.err.println("\n✗ Fehler [" + e.getErrorCode() + "]: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("\n✗ Allgemeiner Fehler: " + e.getMessage());
        }
    }
    
    // ============================================================
    // 5. Geräte eines Mitarbeiters anzeigen (über DB)
    // ============================================================
    private void getEmployeeDevices() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("GERÄTE EINES MITARBEITERS");
        System.out.println("=".repeat(60));
        
        System.out.print("Mitarbeiter-ID: ");
        String employeeIdStr = scanner.nextLine();
        
        try {
            int employeeId = Integer.parseInt(employeeIdStr);
            
            try (CallableStatement cstmt = connection.prepareCall("{call sp_GetMitarbeiterGeräte(?)}")) {
                cstmt.setInt(1, employeeId);
                
                ResultSet rs = cstmt.executeQuery();
                
                System.out.println("\nGeräte von Mitarbeiter ID " + employeeId + ":");
                System.out.printf("\n%-25s %-15s %-20s %-15s %-15s%n", 
                    "Gerät", "Status", "OS Version", "Ausgabedatum", "Tage im Einsatz");
                System.out.println("-".repeat(90));
                
                int count = 0;
                while (rs.next()) {
                    System.out.printf("%-25s %-15s %-20s %-15s %-15d%n",
                        rs.getString("Gerät"),
                        rs.getString("status"),
                        rs.getString("OS_Version"),
                        rs.getDate("ausgabedatum"),
                        rs.getInt("Tage_im_Einsatz"));
                    count++;
                }
                
                if (count == 0) {
                    System.out.println("Keine Geräte für diesen Mitarbeiter gefunden.");
                } else {
                    System.out.println("\nGesamt: " + count + " Gerät(e)");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("✗ Ungültige Mitarbeiter-ID!");
        } catch (SQLException e) {
            System.err.println("✗ Fehler: " + e.getMessage());
        }
    }
    
    // ============================================================
    // 6. Verfügbare Geräte im Lager anzeigen (mit Service)
    // ============================================================
    private void getAvailableDevices() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("VERFÜGBARE GERÄTE IM LAGER");
        System.out.println("=".repeat(60));
        
        System.out.print("Gerätetyp-Filter (z.B. iPhone, Galaxy, leer für alle): ");
        String filter = scanner.nextLine();
        
        try {
            List<DeviceResponse> devices = deviceService.getAvailableDevices(filter);
            
            System.out.println("\nVerfügbare Geräte im Lager" + 
                (filter.isEmpty() ? "" : " (Filter: " + filter + ")") + ":");
            System.out.printf("\n%-5s %-25s %-20s %-15s%n", 
                "ID", "Gerät", "Betriebssystem", "Status");
            System.out.println("-".repeat(65));
            
            int count = 0;
            for (DeviceResponse device : devices) {
                System.out.printf("%-5d %-25s %-20s %-15s%n",
                    device.getId(),
                    device.getFullName(),
                    device.getOs() + " " + device.getOsVersion(),
                    device.getStatus());
                count++;
            }
            
            if (count == 0) {
                System.out.println("Keine verfügbaren Geräte gefunden.");
            } else {
                System.out.println("\nGesamt: " + count + " verfügbare Gerät(e)");
            }
        } catch (EMMException e) {
            System.err.println("\n✗ Fehler [" + e.getErrorCode() + "]: " + e.getMessage());
        }
    }
    
    // ============================================================
    // 7. Monatlichen Kostenreport generieren (über DB)
    // ============================================================
    private void generateMonthlyReport() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("MONATLICHER KOSTENREPORT");
        System.out.println("=".repeat(60));
        
        System.out.print("Monat (1-12, leer für aktuellen Monat): ");
        String monthStr = scanner.nextLine();
        
        System.out.print("Jahr (z.B. 2024, leer für aktuelles Jahr): ");
        String yearStr = scanner.nextLine();
        
        int month, year;
        
        if (monthStr.isEmpty()) {
            month = LocalDate.now().getMonthValue();
        } else {
            month = Integer.parseInt(monthStr);
        }
        
        if (yearStr.isEmpty()) {
            year = LocalDate.now().getYear();
        } else {
            year = Integer.parseInt(yearStr);
        }
        
        System.out.println("\nGeneriere Report für " + month + "/" + year + "...");
        
        try (CallableStatement cstmt = connection.prepareCall("{call sp_Monatsreport(?, ?)}")) {
            cstmt.setInt(1, month);
            cstmt.setInt(2, year);
            
            ResultSet rs = cstmt.executeQuery();
            
            System.out.println("\n" + "=".repeat(80));
            System.out.println("MONATSREPORT " + month + "/" + year);
            System.out.println("=".repeat(80));
            
            System.out.printf("\n%-20s %-15s %-15s %-25s %-15s %-15s%n", 
                "Abteilung", "Geräte", "Kosten (€)", "Durchschn. Datenvol.", "Budget (€)", "Budget-Rest (€)");
            System.out.println("-".repeat(105));
            
            double totalKosten = 0;
            double totalBudget = 0;
            int count = 0;
            
            while (rs.next()) {
                String abteilung = rs.getString("Abteilung");
                int anzahl = rs.getInt("Anzahl_Geräte");
                double kosten = rs.getDouble("Gesamtkosten");
                double datenvolumen = rs.getDouble("Durchschnitts_Datenvolumen");
                double budget = rs.getDouble("Budget");
                double rest = rs.getDouble("Budget_Rest");
                
                System.out.printf("%-20s %-15d %-15.2f %-25.1f %-15.2f %-15.2f%n",
                    abteilung, anzahl, kosten, datenvolumen, budget, rest);
                
                totalKosten += kosten;
                totalBudget += budget;
                count++;
            }
            
            System.out.println("\n" + "=".repeat(105));
            System.out.printf("%-20s %-15d %-15.2f %-25s %-15.2f %-15.2f%n",
                "SUMME:", count, totalKosten, "", totalBudget, totalBudget - totalKosten);
            System.out.println("=".repeat(105));
            
        } catch (NumberFormatException e) {
            System.out.println("✗ Ungültige Zahleneingabe!");
        } catch (SQLException e) {
            System.err.println("✗ Fehler: " + e.getMessage());
        }
    }
    
    // ============================================================
    // 8. Compliance-Check für Richtlinie (über DB)
    // ============================================================
    private void performComplianceCheck() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("COMPLIANCE-CHECK FÜR RICHTLINIE");
        System.out.println("=".repeat(60));
        
        System.out.println("\nVerfügbare Richtlinien:");
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, beschreibung FROM Richtlinie ORDER BY id")) {
            
            System.out.printf("\n%-5s %-25s %-50s%n", "ID", "Name", "Beschreibung");
            System.out.println("-".repeat(85));
            
            while (rs.next()) {
                System.out.printf("%-5d %-25s %-50s%n",
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("beschreibung").substring(0, Math.min(50, rs.getString("beschreibung").length())));
            }
        } catch (SQLException e) {
            System.err.println("✗ Fehler beim Abrufen der Richtlinien: " + e.getMessage());
            return;
        }
        
        System.out.print("\nRichtlinien-ID für Compliance-Check: ");
        String policyIdStr = scanner.nextLine();
        
        try {
            int policyId = Integer.parseInt(policyIdStr);
            
            try (CallableStatement cstmt = connection.prepareCall("{call sp_DemoComplianceCheck(?)}")) {
                cstmt.setInt(1, policyId);
                
                System.out.println("\nFühre Compliance-Check durch...");
                
                boolean hasResultSet = cstmt.execute();
                
                if (hasResultSet) {
                    ResultSet rs = cstmt.getResultSet();
                    if (rs.next()) {
                        System.out.println("\n" + "=".repeat(60));
                        System.out.println("COMPLIANCE-CHECK ERGEBNIS");
                        System.out.println("=".repeat(60));
                        
                        System.out.println("Geprüfte Richtlinie: " + rs.getString("Geprüfte Richtlinie"));
                        System.out.println("Anzahl Geräte: " + rs.getInt("Anzahl Geräte"));
                        System.out.println("Erfüllt: " + rs.getInt("Erfüllt"));
                        System.out.println("Nicht erfüllt: " + rs.getInt("Nicht erfüllt"));
                        System.out.println("Erfüllungsquote: " + rs.getDouble("Erfüllungsquote (%)") + "%");
                        System.out.println("Prüfungszeitpunkt: " + rs.getTimestamp("Prüfungszeitpunkt"));
                        
                        showComplianceDetails(policyId);
                    }
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("✗ Ungültige Richtlinien-ID!");
        } catch (SQLException e) {
            System.err.println("✗ Fehler: " + e.getMessage());
        }
    }
    
    // ============================================================
    // 9. Alle Compliance-Checks durchführen (über DB)
    // ============================================================
    private void performAllComplianceChecks() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("ALLE COMPLIANCE-CHECKS DURCHFÜHREN");
        System.out.println("=".repeat(60));
        
        System.out.println("\nStarte umfassenden Compliance-Check für alle Richtlinien...");
        System.out.println("Dies kann einen Moment dauern...");
        
        try (CallableStatement cstmt = connection.prepareCall("{call sp_AlleComplianceChecks}")) {
            boolean hasResultSet = cstmt.execute();
            int resultSetCount = 0;
            
            do {
                if (hasResultSet) {
                    ResultSet rs = cstmt.getResultSet();
                    resultSetCount++;
                    
                    if (resultSetCount == 1) {
                        System.out.println("\n" + "=".repeat(80));
                        System.out.println("GESAMTÜBERSICHT ALLER COMPLIANCE-PRÜFUNGEN");
                        System.out.println("=".repeat(80));
                        
                        System.out.printf("\n%-25s %-10s %-15s %-20s %-15s %-20s%n", 
                            "Richtlinie", "Geräte", "✓ Erfüllt", "✗ Nicht erfüllt", "Quote (%)", "Geprüft am");
                        System.out.println("-".repeat(105));
                        
                        while (rs.next()) {
                            System.out.printf("%-25s %-10d %-15d %-20d %-15.1f %-20s%n",
                                rs.getString("Richtlinie"),
                                rs.getInt("Geräte"),
                                rs.getInt("✓ Erfüllt"),
                                rs.getInt("✗ Nicht erfüllt"),
                                rs.getDouble("Quote (%)"),
                                rs.getTimestamp("Geprüft am"));
                        }
                    } else if (resultSetCount == 2) {
                        System.out.println("\n" + "=".repeat(60));
                        System.out.println("ZUSAMMENFASSUNG");
                        System.out.println("=".repeat(60));
                        
                        while (rs.next()) {
                            System.out.println("Statistik: " + rs.getString("Statistik"));
                            System.out.println("Anzahl geprüfter Richtlinien: " + rs.getInt("Anzahl geprüfter Richtlinien"));
                            System.out.println("Gesamte Geräteprüfungen: " + rs.getInt("Gesamte Geräteprüfungen"));
                            System.out.println("Durchschnittl. Erfüllungsquote: " + 
                                rs.getDouble("Durchschnittl. Erfüllungsquote (%)") + "%");
                        }
                    }
                }
                
                hasResultSet = cstmt.getMoreResults();
            } while (hasResultSet || cstmt.getUpdateCount() != -1);
            
            System.out.println("\n✓ Alle Compliance-Checks erfolgreich durchgeführt.");
            
        } catch (SQLException e) {
            System.err.println("✗ Fehler: " + e.getMessage());
        }
    }
    
    // ============================================================
    // 10. Views anzeigen (über DB)
    // ============================================================
    private void showAllViews() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("ALLE VIEWS IN DER DATENBANK");
        System.out.println("=".repeat(60));
        
        System.out.println("\nWähle eine View:");
        System.out.println("1. V_AktiveGeräte");
        System.out.println("2. V_KostenProAbteilung");
        System.out.println("3. V_Vertragsuebersicht");
        System.out.println("4. V_ComplianceReport");
        System.out.println("5. V_AppInstallationen");
        System.out.println("6. Alle Views anzeigen");
        System.out.print("\nAuswahl (1-6): ");
        
        String choice = scanner.nextLine();
        
        switch (choice) {
            case "1":
                showView("V_AktiveGeräte", 25);
                break;
            case "2":
                showView("V_KostenProAbteilung", 0);
                break;
            case "3":
                showView("V_Vertragsuebersicht", 10);
                break;
            case "4":
                showView("V_ComplianceReport", 10);
                break;
            case "5":
                showView("V_AppInstallationen", 0);
                break;
            case "6":
                showAllViewsList();
                break;
            default:
                System.out.println("Ungültige Auswahl.");
        }
    }
    
    // ============================================================
    // 11. Tabellen anzeigen (über DB)
    // ============================================================
    private void showAllTables() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("ALLE TABELLEN IN DER DATENBANK");
        System.out.println("=".repeat(60));
        
        String sql = "SELECT TABLE_NAME " +
                    "FROM INFORMATION_SCHEMA.TABLES " +
                    "WHERE TABLE_SCHEMA = 'dbo' " +
                    "AND TABLE_TYPE = 'BASE TABLE' " +
                    "ORDER BY TABLE_NAME";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            System.out.printf("\n%-35s %-15s%n", "Tabelle", "Typ");
            System.out.println("-".repeat(50));
            
            int tableCount = 0;
            
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                System.out.printf("%-35s %-15s%n", tableName, "BASE TABLE");
                tableCount++;
            }
            
            System.out.println("\n" + "=".repeat(50));
            System.out.println("STATISTIK:");
            System.out.println("Anzahl Tabellen: " + tableCount);
            System.out.println("=".repeat(50));
            
        } catch (SQLException e) {
            System.err.println("✗ Fehler beim Abrufen der Tabellen: " + e.getMessage());
        }
    }
    
	 // ============================================================
	 // 12. AUDITLOG anzeigen (über DB) - KORRIGIERTE VERSION
	 // ============================================================
	 private void showAuditLog() {
	     System.out.println("\n" + "=".repeat(80));
	     System.out.println("AUDITLOG - ÄNDERUNGSPROTOKOLL");
	     System.out.println("=".repeat(80));
	     
	     System.out.println("\nFilteroptionen:");
	     System.out.println("1. Alle Einträge anzeigen");
	     System.out.println("2. Nur Statusänderungen von Endgeräten");
	     System.out.println("3. Letzte 24 Stunden");
	     System.out.println("4. Nach Datum filtern");
	     System.out.println("5. Nach Benutzer filtern");
	     System.out.print("\nWähle Filter (1-5): ");
	     
	     String filterChoice = scanner.nextLine();
	     
	     String sql = "SELECT * FROM AuditLog WHERE 1=1";
	     
	     switch (filterChoice) {
	         case "1":
	             break;
	         case "2":
	             sql += " AND tabelle = 'Endgeraet' AND aktion = 'Statusänderung'";
	             break;
	         case "3":
	             sql += " AND zeitpunkt >= DATEADD(HOUR, -24, GETDATE())";
	             break;
	         case "4":
	             System.out.print("Startdatum (YYYY-MM-DD): ");
	             String startDate = scanner.nextLine();
	             System.out.print("Enddatum (YYYY-MM-DD, optional): ");
	             String endDate = scanner.nextLine();
	             
	             if (!startDate.isEmpty()) {
	                 sql += " AND CONVERT(DATE, zeitpunkt) >= '" + startDate + "'";
	             }
	             if (!endDate.isEmpty()) {
	                 sql += " AND CONVERT(DATE, zeitpunkt) <= '" + endDate + "'";
	             }
	             break;
	         case "5":
	             System.out.print("Benutzername (oder Teil): ");
	             String user = scanner.nextLine();
	             if (!user.isEmpty()) {
	                 sql += " AND benutzer LIKE '%" + user + "%'";
	             }
	             break;
	         default:
	             System.out.println("Ungültige Auswahl, zeige alle Einträge.");
	     }
	     
	     sql += " ORDER BY zeitpunkt DESC";
	     
	     System.out.print("\nAnzahl der Einträge (0 für alle): ");
	     String limitStr = scanner.nextLine();
	     int limit = 0;
	     try {
	         limit = Integer.parseInt(limitStr);
	         if (limit > 0) {
	             sql += " OFFSET 0 ROWS FETCH NEXT " + limit + " ROWS ONLY";
	         }
	     } catch (NumberFormatException e) {
	     }
	     
	     System.out.println("\n" + "=".repeat(140));
	     System.out.println("AUDITLOG EINTRÄGE");
	     System.out.println("=".repeat(140));
	     
	     try (Statement stmt = connection.createStatement();
	          ResultSet rs = stmt.executeQuery(sql)) {
	         
	         // KORRIGIERT: Gerät-ID Spalte hinzugefügt
	         System.out.printf("\n%-5s %-15s %-20s %-25s %-25s %-20s %-15s %-10s%n", 
	             "ID", "Tabelle", "Aktion", "Alt", "Neu", "Zeitpunkt", "Benutzer", "Gerät-ID");
	         System.out.println("-".repeat(140));
	         
	         int count = 0;
	         while (rs.next()) {
	             String alt = rs.getString("alt");
	             String neu = rs.getString("neu");
	             
	             if (alt != null && alt.length() > 20) alt = alt.substring(0, 17) + "...";
	             if (neu != null && neu.length() > 20) neu = neu.substring(0, 17) + "...";
	             
	             // KORRIGIERT: endgeraet_id auslesen und anzeigen
	             Integer deviceId = null;
	             try {
	                 Object deviceIdObj = rs.getObject("endgeraet_id");
	                 if (deviceIdObj != null) {
	                     deviceId = rs.getInt("endgeraet_id");
	                 }
	             } catch (SQLException e) {
	                 // Spalte existiert nicht oder ist NULL
	             }
	             
	             System.out.printf("%-5d %-15s %-20s %-25s %-25s %-20s %-15s %-10s%n",
	                 rs.getInt("id"),
	                 rs.getString("tabelle"),
	                 rs.getString("aktion"),
	                 (alt != null ? alt : "NULL"),
	                 (neu != null ? neu : "NULL"),
	                 rs.getTimestamp("zeitpunkt"),
	                 rs.getString("benutzer"),
	                 (deviceId != null ? deviceId.toString() : "-"));
	             count++;
	         }
	         
	         System.out.println("\n" + "=".repeat(60));
	         System.out.println("GESAMT: " + count + " AuditLog-Einträge");
	         System.out.println("=".repeat(60));
	         
	         showAuditLogStatistics();
	         
	     } catch (SQLException e) {
	         System.err.println("✗ Fehler beim Abrufen des AuditLogs: " + e.getMessage());
	         checkAuditLogTable();
	     }
	 }
    
    // ============================================================
    // 13. AUDITLOG TEST - Trigger testen (mit Service)
    // ============================================================
    private void testAuditLogTrigger() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("AUDITLOG TRIGGER TEST");
        System.out.println("=".repeat(80));
        
        System.out.println("\nDies testet den Trigger trg_GeräteStatusChange.");
        System.out.println("Es wird ein Gerätestatus geändert, was einen AuditLog-Eintrag erzeugen sollte.");
        
        System.out.println("\nSuche ein geeignetes Testgerät...");
        
        String findSql = 
            "SELECT TOP 3 id, hersteller, modell, status " +
            "FROM Endgeraet " +
            "WHERE status IN ('LAGER', 'AKTIV') " +
            "ORDER BY id";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(findSql)) {
            
            System.out.println("\nVerfügbare Testgeräte:");
            System.out.printf("%-5s %-15s %-15s %-10s%n", "ID", "Hersteller", "Modell", "Status");
            System.out.println("-".repeat(45));
            
            while (rs.next()) {
                System.out.printf("%-5d %-15s %-15s %-10s%n",
                    rs.getInt("id"),
                    rs.getString("hersteller"),
                    rs.getString("modell"),
                    rs.getString("status"));
            }
        } catch (SQLException e) {
            System.err.println("✗ Fehler beim Suchen von Geräten: " + e.getMessage());
            return;
        }
        
        System.out.print("\nGeräte-ID für Test: ");
        String deviceIdStr = scanner.nextLine();
        
        System.out.print("Neuer Status (LAGER/AKTIV/DEFEKT/AUSGESCHIEDEN): ");
        String newStatus = scanner.nextLine().toUpperCase();
        
        if (!isValidStatus(newStatus)) {
            System.out.println("✗ Ungültiger Status! Erlaubt: LAGER, AKTIV, DEFEKT, AUSGESCHIEDEN");
            return;
        }
        
        try {
            int deviceId = Integer.parseInt(deviceIdStr);
            
            // Hole aktuellen Status über Service
            DeviceResponse device = deviceService.getDeviceById(deviceId);
            String currentStatus = device.getStatus();
            
            System.out.println("\n=== TEST VORBEREITUNG ===");
            System.out.println("Gerät ID: " + deviceId);
            System.out.println("Aktueller Status: " + currentStatus);
            System.out.println("Neuer Status: " + newStatus);
            
            if (currentStatus.equals(newStatus)) {
                System.out.println("\n⚠️  Status ist bereits " + newStatus + ". Wähle einen anderen Status.");
                return;
            }
            
            int auditCountBefore = getAuditLogCount();
            System.out.println("AuditLog-Einträge vorher: " + auditCountBefore);
            
            System.out.println("\n=== FÜHRE STATUSÄNDERUNG DURCH ===");
            
            // Status über Service ändern
            DeviceResponse updatedDevice = deviceService.updateDeviceStatus(deviceId, newStatus);
            
            System.out.println("Status erfolgreich geändert zu: " + updatedDevice.getStatus());
            
            // Warten für Trigger
            Thread.sleep(1000);
            
            int auditCountAfter = getAuditLogCount();
            System.out.println("AuditLog-Einträge nachher: " + auditCountAfter);
            
            int newEntries = auditCountAfter - auditCountBefore;
            System.out.println("Neue AuditLog-Einträge: " + newEntries);
            
            if (newEntries > 0) {
                System.out.println("\n✓ ERFOLG: Trigger wurde ausgelöst!");
                
                System.out.println("\n=== NEUE AUDITLOG-EINTRÄGE ===");
                
                String newAuditSql = 
                    "SELECT * FROM (" +
                    "    SELECT *, ROW_NUMBER() OVER (ORDER BY id DESC) as rn " +
                    "    FROM AuditLog " +
                    ") as numbered " +
                    "WHERE rn <= ? " +
                    "ORDER BY id DESC";
                
                try (PreparedStatement pstmt2 = connection.prepareStatement(newAuditSql)) {
                    pstmt2.setInt(1, newEntries);
                    
                    try (ResultSet rs = pstmt2.executeQuery()) {
                        System.out.printf("\n%-5s %-15s %-20s %-15s %-15s %-20s%n", 
                            "ID", "Tabelle", "Aktion", "Alt", "Neu", "Zeitpunkt");
                        System.out.println("-".repeat(90));
                        
                        while (rs.next()) {
                            System.out.printf("%-5d %-15s %-20s %-15s %-15s %-20s%n",
                                rs.getInt("id"),
                                rs.getString("tabelle"),
                                rs.getString("aktion"),
                                rs.getString("alt"),
                                rs.getString("neu"),
                                rs.getTimestamp("zeitpunkt"));
                        }
                    }
                }
                
                System.out.print("\nStatus zurücksetzen auf '" + currentStatus + "'? (j/n): ");
                String resetChoice = scanner.nextLine().toLowerCase();
                
                if (resetChoice.equals("j") || resetChoice.equals("ja")) {
                    deviceService.updateDeviceStatus(deviceId, currentStatus);
                    System.out.println("✓ Status zurückgesetzt auf: " + currentStatus);
                }
                
            } else {
                System.out.println("\n✗ FEHLER: Keine neuen AuditLog-Einträge erstellt!");
                System.out.println("Mögliche Ursachen:");
                System.out.println("1. Trigger ist nicht aktiv oder fehlerhaft");
                System.out.println("2. AuditLog-Tabelle existiert nicht");
                System.out.println("3. Benutzer hat keine INSERT-Rechte auf AuditLog");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("✗ Ungültige Geräte-ID!");
        } catch (EMMException e) {
            System.err.println("\n✗ Fehler [" + e.getErrorCode() + "]: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("✗ SQL Fehler: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("✗ Sleep unterbrochen: " + e.getMessage());
        }
    }
    
    // ============================================================
    // 14. Datenbank-Informationen (über DB)
    // ============================================================
    private void showDatabaseInfo() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("DATENBANK-INFORMATIONEN");
        System.out.println("=".repeat(60));
        
        try {
            var meta = connection.getMetaData();
            
            System.out.println("\n=== Verbindungsinformationen ===");
            System.out.println("Datenbank: " + meta.getDatabaseProductName() 
                             + " " + meta.getDatabaseProductVersion());
            System.out.println("JDBC-Treiber: " + meta.getDriverName() 
                             + " " + meta.getDriverVersion());
            System.out.println("URL: " + meta.getURL());
            System.out.println("Benutzer: " + meta.getUserName());
            
            System.out.println("\n=== Datenbank-Statistiken ===");
            
            showTablesInfo();
            showViewsInfo();
            showStoredProceduresInfo();
            
        } catch (SQLException e) {
            System.err.println("✗ Fehler: " + e.getMessage());
        }
    }
    
    // ============================================================
    // 15. Gerätestatistiken (mit Service)
    // ============================================================
    private void showDeviceStatistics() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("GERÄTESTATISTIKEN");
        System.out.println("=".repeat(60));
        
        try {
            int totalDevices = deviceService.getDeviceCount();
            int availableDevices = deviceService.getDeviceCountByStatus("LAGER");
            int activeDevices = deviceService.getDeviceCountByStatus("AKTIV");
            int defectiveDevices = deviceService.getDeviceCountByStatus("DEFEKT");
            int retiredDevices = deviceService.getDeviceCountByStatus("AUSGESCHIEDEN");
            
            System.out.println("\n=== ÜBERSICHT ===");
            System.out.println("Gesamte Geräte: " + totalDevices);
            System.out.println("Verfügbar (LAGER): " + availableDevices);
            System.out.println("Aktiv (AKTIV): " + activeDevices);
            System.out.println("Defekt (DEFEKT): " + defectiveDevices);
            System.out.println("Ausgeschieden (AUSGESCHIEDEN): " + retiredDevices);
            
            if (totalDevices > 0) {
                System.out.println("\n=== PROZENTUAL ===");
                System.out.printf("Verfügbar: %.1f%%%n", (availableDevices * 100.0 / totalDevices));
                System.out.printf("Aktiv: %.1f%%%n", (activeDevices * 100.0 / totalDevices));
                System.out.printf("Defekt: %.1f%%%n", (defectiveDevices * 100.0 / totalDevices));
                System.out.printf("Ausgeschieden: %.1f%%%n", (retiredDevices * 100.0 / totalDevices));
            }
            
            System.out.println("\n=== VERFÜGBARKEITSCHECK ===");
            System.out.print("Geräte-ID prüfen (oder leer lassen): ");
            String checkId = scanner.nextLine();
            
            if (!checkId.isEmpty()) {
                try {
                    int deviceId = Integer.parseInt(checkId);
                    boolean isAvailable = deviceService.isDeviceAvailable(deviceId);
                    System.out.println("Gerät " + deviceId + " ist verfügbar: " + 
                                      (isAvailable ? "✓ JA" : "✗ NEIN"));
                } catch (NumberFormatException e) {
                    System.out.println("Ungültige Geräte-ID");
                } catch (EMMException e) {
                    System.err.println("✗ Fehler: " + e.getMessage());
                }
            }
            
        } catch (EMMException e) {
            System.err.println("\n✗ Fehler [" + e.getErrorCode() + "]: " + e.getMessage());
        }
    }
    
    // ============================================================
    // Hilfsmethoden
    // ============================================================
    
    private void showComplianceDetails(int policyId) throws SQLException {
        System.out.println("\nDetails der Compliance-Prüfungen:");
        
        String sql = "SELECT TOP 10 cp.*, e.hersteller + ' ' + e.modell as Gerät, r.name as Richtlinie " +
                    "FROM CompliancePruefung cp " +
                    "JOIN Endgeraet e ON cp.endgeraetId = e.id " +
                    "JOIN Richtlinie r ON cp.policyId = r.id " +
                    "WHERE cp.policyId = ? " +
                    "ORDER BY cp.geprueftAm DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, policyId);
            ResultSet rs = pstmt.executeQuery();
            
            System.out.printf("\n%-5s %-25s %-10s %-15s %-50s%n", 
                "GerätID", "Gerät", "Erfüllt", "Geprüft am", "Bemerkung");
            System.out.println("-".repeat(105));
            
            int count = 0;
            while (rs.next()) {
                String erfuellt = rs.getBoolean("erfuellt") ? "✓" : "✗";
                String bemerkung = rs.getString("bemerkung");
                if (bemerkung.length() > 50) {
                    bemerkung = bemerkung.substring(0, 47) + "...";
                }
                
                System.out.printf("%-5d %-25s %-10s %-15s %-50s%n",
                    rs.getInt("endgeraetId"),
                    rs.getString("Gerät"),
                    erfuellt,
                    rs.getDate("geprueftAm"),
                    bemerkung);
                count++;
            }
            
            System.out.println("\nAngezeigt: " + count + " von " + getTotalComplianceChecks(policyId) + " Prüfungen");
        }
    }
    
    private int getTotalComplianceChecks(int policyId) throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM CompliancePruefung WHERE policyId = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, policyId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }
    
    private void showAllViewsList() {
        System.out.println("\n=== ALLE VIEWS ===");
        
        String sql = "SELECT name, create_date, modify_date " +
                    "FROM sys.views " +
                    "WHERE is_ms_shipped = 0 " +
                    "ORDER BY name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            System.out.printf("\n%-35s %-20s %-20s%n", "Name", "Erstellt am", "Geändert am");
            System.out.println("-".repeat(75));
            
            int count = 0;
            while (rs.next()) {
                System.out.printf("%-35s %-20s %-20s%n",
                    rs.getString("name"),
                    rs.getDate("create_date"),
                    rs.getDate("modify_date"));
                count++;
            }
            
            System.out.println("\nGesamt: " + count + " View(s)");
            
        } catch (SQLException e) {
            System.err.println("✗ Fehler: " + e.getMessage());
        }
    }
    
    private void showView(String viewName, int limit) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("VIEW: " + viewName);
        System.out.println("=".repeat(80));
        
        String sql = "SELECT * FROM " + viewName;
        if (limit > 0) {
            sql += " ORDER BY 1 OFFSET 0 ROWS FETCH NEXT " + limit + " ROWS ONLY";
        }
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();
            
            System.out.println();
            for (int i = 1; i <= columnCount; i++) {
                System.out.printf("%-20s ", meta.getColumnName(i));
            }
            System.out.println();
            System.out.println("-".repeat(columnCount * 20));
            
            int rowCount = 0;
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    Object value = rs.getObject(i);
                    String valueStr = (value != null) ? value.toString() : "NULL";
                    if (valueStr.length() > 18) {
                        valueStr = valueStr.substring(0, 15) + "...";
                    }
                    System.out.printf("%-20s ", valueStr);
                }
                System.out.println();
                rowCount++;
            }
            
            System.out.println("\nAngezeigt: " + rowCount + " Zeilen");
            
        } catch (SQLException e) {
            System.err.println("✗ Fehler beim Anzeigen der View: " + e.getMessage());
        }
    }
    
    private void showAuditLogStatistics() throws SQLException {
        System.out.println("\n" + "-".repeat(60));
        System.out.println("AUDITLOG STATISTIK");
        System.out.println("-".repeat(60));
        
        String statSql = 
            "SELECT " +
            "    COUNT(*) as TotalEntries, " +
            "    COUNT(DISTINCT tabelle) as TablesTracked, " +
            "    MIN(zeitpunkt) as FirstEntry, " +
            "    MAX(zeitpunkt) as LastEntry, " +
            "    COUNT(DISTINCT benutzer) as UsersTracked, " +
            "    (SELECT COUNT(*) FROM AuditLog WHERE tabelle = 'Endgeraet' AND aktion = 'Statusänderung') as StatusChanges " +
            "FROM AuditLog";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(statSql)) {
            
            if (rs.next()) {
                int total = rs.getInt("TotalEntries");
                if (total > 0) {
                    System.out.println("Gesamteinträge: " + total);
                    System.out.println("Überwachte Tabellen: " + rs.getInt("TablesTracked"));
                    System.out.println("Erster Eintrag: " + rs.getTimestamp("FirstEntry"));
                    System.out.println("Letzter Eintrag: " + rs.getTimestamp("LastEntry"));
                    System.out.println("Überwachte Benutzer: " + rs.getInt("UsersTracked"));
                    System.out.println("Statusänderungen (Endgeräte): " + rs.getInt("StatusChanges"));
                    
                    System.out.println("\nTop 5 Aktionen:");
                    String topActionsSql = 
                        "SELECT TOP 5 aktion, COUNT(*) as count " +
                        "FROM AuditLog " +
                        "GROUP BY aktion " +
                        "ORDER BY count DESC";
                    
                    try (Statement stmt2 = connection.createStatement();
                         ResultSet rs2 = stmt2.executeQuery(topActionsSql)) {
                        
                        while (rs2.next()) {
                            System.out.println("  " + rs2.getString("aktion") + ": " + rs2.getInt("count"));
                        }
                    }
                } else {
                    System.out.println("Das AuditLog ist leer.");
                }
            }
        }
    }
    
    private void showTablesInfo() throws SQLException {
        int tableCount = 0;
        System.out.println("\n=== TABELLEN ===");
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT name, type_desc, create_date " +
                 "FROM sys.objects " +
                 "WHERE type = 'U' " +
                 "AND is_ms_shipped = 0 " +
                 "ORDER BY name")) {
            
            System.out.printf("\n%-35s %-20s%n", "Name", "Typ");
            System.out.println("-".repeat(55));
            
            while (rs.next()) {
                String name = rs.getString("name");
                String type = rs.getString("type_desc");
                
                System.out.printf("%-35s %-20s%n", name, type);
                tableCount++;
            }
            
            if (tableCount == 0) {
                System.out.println("Keine Tabellen gefunden.");
            } else {
                System.out.println("\nAnzahl: " + tableCount + " Tabelle(n)");
            }
        }
    }
    
    private void showViewsInfo() throws SQLException {
        int viewCount = 0;
        System.out.println("\n=== VIEWS ===");
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT name, type_desc, create_date " +
                 "FROM sys.objects " +
                 "WHERE type = 'V' " +
                 "AND is_ms_shipped = 0 " +
                 "ORDER BY name")) {
            
            System.out.printf("\n%-35s %-20s%n", "Name", "Typ");
            System.out.println("-".repeat(55));
            
            while (rs.next()) {
                String name = rs.getString("name");
                String type = rs.getString("type_desc");
                
                System.out.printf("%-35s %-20s%n", name, type);
                viewCount++;
            }
            
            if (viewCount == 0) {
                System.out.println("Keine Views gefunden.");
            } else {
                System.out.println("\nAnzahl: " + viewCount + " View(s)");
            }
        }
    }
    
    private void showStoredProceduresInfo() throws SQLException {
        int userProcCount = 0;
        System.out.println("\n=== BENUTZERDEFINIERTE STORED PROCEDURES ===");
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT name, type_desc, create_date " +
                 "FROM sys.objects " +
                 "WHERE type IN ('P', 'PC') " +
                 "AND is_ms_shipped = 0 " +
                 "AND name NOT LIKE '%diagram%' " +
                 "AND name NOT LIKE '%Diagram%' " +
                 "ORDER BY name")) {
            
            System.out.printf("\n%-35s %-20s%n", "Name", "Typ");
            System.out.println("-".repeat(55));
            
            while (rs.next()) {
                String name = rs.getString("name");
                String type = rs.getString("type_desc");
                
                System.out.printf("%-35s %-20s%n", name, type);
                userProcCount++;
            }
            
            if (userProcCount == 0) {
                System.out.println("Keine benutzerdefinierten Stored Procedures gefunden.");
            } else {
                System.out.println("\nAnzahl: " + userProcCount + " benutzerdefinierte Stored Procedure(s)");
            }
        }
    }
    
    private boolean isValidStatus(String status) {
        return status.equals("LAGER") || status.equals("AKTIV") || 
               status.equals("DEFEKT") || status.equals("AUSGESCHIEDEN");
    }
    
    private int getAuditLogCount() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM AuditLog";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        return 0;
    }
    
    private void checkAuditLogTable() {
        System.out.println("\n=== PRÜFE AUDITLOG-TABELLE ===");
        
        String[] checkQueries = {
            "SELECT COUNT(*) as exists_flag FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'AuditLog'",
            "SELECT COUNT(*) as column_count FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'AuditLog'",
            "SELECT name FROM sys.triggers WHERE name = 'trg_GeräteStatusChange'"
        };
        
        String[] messages = {
            "AuditLog-Tabelle existiert: ",
            "Spalten in AuditLog: ",
            "Trigger 'trg_GeräteStatusChange' existiert: "
        };
        
        try {
            for (int i = 0; i < checkQueries.length; i++) {
                try (Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery(checkQueries[i])) {
                    if (rs.next()) {
                        Object result = rs.getObject(1);
                        System.out.println(messages[i] + result);
                    }
                }
            }
            
            String triggerCheck = 
                "SELECT COUNT(*) as has_trigger " +
                "FROM sys.triggers t " +
                "JOIN sys.objects o ON t.parent_id = o.object_id " +
                "WHERE t.name = 'trg_GeräteStatusChange' " +
                "AND o.name = 'Endgeraet'";
            
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(triggerCheck)) {
                if (rs.next() && rs.getInt("has_trigger") > 0) {
                    System.out.println("✓ Trigger ist auf Endgeraet-Tabelle registriert");
                } else {
                    System.out.println("✗ Trigger nicht auf Endgeraet gefunden");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Fehler beim Prüfen der AuditLog-Struktur: " + e.getMessage());
        }
    }
}