package de.emm.demo;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class EMMDatabaseManager {
    
    // === KONFIGURATION - HIER ANPASSEN! ===
    private static final String SERVER = "localhost\\SQLEXPRESS"; 
    private static final String DATABASE = "EMM_Demo";            
    private static final int PORT = 1433;                         
    
    // SQL Server Login Credentials
    private static final String USERNAME = "emm_user";            
    private static final String PASSWORD = "emm_x123";            
    
    // JDBC URL für SQL Server Authentication
    private static final String URL = String.format(
        "jdbc:sqlserver://%s:%d;databaseName=%s;encrypt=false;trustServerCertificate=true",
        SERVER, PORT, DATABASE
    );
    
    // Formatter für Datumsausgabe
    //private static final DateTimeFormatter DATE_FORMATTER = 
    //    DateTimeFormatter.ofPattern("dd.MM.yyyy");
    
    public static void main(String[] args) {
        System.out.println("=== EMM Database Manager (SQL Server Auth) ===");
        System.out.println("Server: " + SERVER);
        System.out.println("Datenbank: " + DATABASE);
        System.out.println("Benutzer: " + USERNAME);
        System.out.println("URL: " + URL);
        System.out.println("\nTeste Verbindung...");
        
        Scanner scanner = new Scanner(System.in);
        Connection connection = null;
        
        try {
            // 1. JDBC-Treiber laden
            try {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                System.out.println("✓ JDBC-Treiber geladen");
            } catch (ClassNotFoundException e) {
                System.err.println("✗ JDBC-Treiber nicht gefunden!");
                System.err.println("Stelle sicher, dass mssql-jdbc in pom.xml enthalten ist.");
                return;
            }
            
            // 2. Verbindung herstellen
            System.out.println("\n1. Versuche Verbindung zur Datenbank...");
            connection = getConnection();
            System.out.println("✓ Verbindung erfolgreich!");
            
            // 3. Verbindungsdetails anzeigen
            showConnectionInfo(connection);
            
            // 4. Einfachen Test durchführen, bevor Menü angezeigt wird
            if (simpleTest(connection)) {
                // 5. Menü anzeigen
                showMainMenu(scanner, connection);
            } else {
                System.out.println("Grundlegende Tests fehlgeschlagen. Überprüfe die Benutzerrechte.");
            }
            
        } catch (SQLException e) {
            handleSQLException(e);
        } catch (Exception e) {
            System.err.println("\n✗ Allgemeiner Fehler: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(scanner, connection);
        }
    }
    
    private static Connection getConnection() throws SQLException {
        System.out.println("Verbindungsparameter:");
        System.out.println("  URL: " + URL);
        System.out.println("  User: " + USERNAME);
        System.out.println("  Passwort: " + "***".repeat(PASSWORD.length()));
        
        // Verbindung mit SQL Server Authentication
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
    
    private static void showConnectionInfo(Connection conn) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        
        System.out.println("\n2. Verbindungsdetails:");
        System.out.println("   Datenbank: " + meta.getDatabaseProductName() 
                         + " " + meta.getDatabaseProductVersion());
        System.out.println("   JDBC-Treiber: " + meta.getDriverName() 
                         + " " + meta.getDriverVersion());
        System.out.println("   URL: " + meta.getURL());
        System.out.println("   Benutzer: " + meta.getUserName());
        
        // Einfacher Test ohne komplizierte Abfrage
        System.out.println("\n3. Führe einfache Verbindungstests durch...");
    }
    
    private static boolean simpleTest(Connection conn) {
        System.out.println("\n=== EINFACHE VERBINDUNGSTESTS ===");
        
        boolean allTestsPassed = true;
        
        // Test 1: Einfache SELECT-Abfrage
        System.out.println("\n1. Teste einfache SELECT-Abfrage...");
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM Endgeraet")) {
            if (rs.next()) {
                int count = rs.getInt("count");
                System.out.println("✓ SELECT erfolgreich - " + count + " Geräte in der Datenbank");
            }
        } catch (SQLException e) {
            System.err.println("✗ SELECT fehlgeschlagen: " + e.getMessage());
            allTestsPassed = false;
        }
        
        // Test 2: View abfragen
        System.out.println("\n2. Teste View-Abfrage...");
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT TOP 1 * FROM V_AktiveGeräte")) {
            if (rs.next()) {
                System.out.println("✓ View-Abfrage erfolgreich");
            } else {
                System.out.println("✓ View-Abfrage erfolgreich (keine aktiven Geräte)");
            }
        } catch (SQLException e) {
            System.err.println("✗ View-Abfrage fehlgeschlagen: " + e.getMessage());
            allTestsPassed = false;
        }
        
        return allTestsPassed;
    }
    
    private static void showMainMenu(Scanner scanner, Connection conn) throws SQLException {
        boolean running = true;
        
        while (running) {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("=== EMM DATENBANK-MANAGER ===");
            System.out.println("=".repeat(60));
            System.out.println(" 1. sp_NeuesGerätErfassen - Neues Gerät erfassen");
            System.out.println(" 2. sp_GerätEntfernen_Einfach - Gerät entfernen (Soft-Delete)");
            System.out.println(" 3. sp_GerätAusgeben - Gerät an Mitarbeiter ausgeben");
            System.out.println(" 4. sp_GerätZuruecknehmen - Gerät zurücknehmen");
            System.out.println(" 5. sp_GetMitarbeiterGeräte - Geräte eines Mitarbeiters");
            System.out.println(" 6. sp_GetVerfügbareGeräte - Verfügbare Geräte im Lager");
            System.out.println(" 7. sp_Monatsreport - Monatlichen Kostenreport");
            System.out.println(" 8. sp_DemoComplianceCheck - Compliance-Check für Richtlinie");
            System.out.println(" 9. sp_AlleComplianceChecks - Alle Compliance-Checks");
            System.out.println("10. Views anzeigen (V_AktiveGeräte, V_KostenProAbteilung, etc.)");
            System.out.println("11. Tabellen anzeigen (Endgeraet, Mitarbeiter, etc.)");
            System.out.println("12. AUDITLOG - Änderungsprotokoll anzeigen"); // NEUE OPTION
            System.out.println("13. AUDITLOG TEST - Trigger testen");         // NEUE OPTION
            System.out.println("14. Datenbank-Informationen");
            System.out.println("15. Beenden"); // Geändert von 13 auf 15
            System.out.print("\nWähle eine Option (1-15): ");
            
            String choice = scanner.nextLine();
            
            switch (choice) {
                case "1":
                    call_sp_NeuesGerätErfassen(scanner, conn);
                    break;
                case "2":
                    call_sp_GerätEntfernen_Einfach(scanner, conn);
                    break;
                case "3":
                    call_sp_GerätAusgeben(scanner, conn);
                    break;
                case "4":
                    call_sp_GerätZuruecknehmen(scanner, conn);
                    break;
                case "5":
                    call_sp_GetMitarbeiterGeräte(scanner, conn);
                    break;
                case "6":
                    call_sp_GetVerfügbareGeräte(scanner, conn);
                    break;
                case "7":
                    call_sp_Monatsreport(scanner, conn);
                    break;
                case "8":
                    call_sp_DemoComplianceCheck(scanner, conn);
                    break;
                case "9":
                    call_sp_AlleComplianceChecks(conn);
                    break;
                case "10":
                    showAllViews(scanner, conn);
                    break;
                case "11":
                    showAllTables(conn);
                    break;
                case "12":
                    showAuditLog(scanner, conn);
                    break;
                case "13":
                    testAuditLogTrigger(scanner, conn);
                    break;
                case "14":
                    showDatabaseInfo(conn);
                    break;
                case "15":
                    System.out.println("\nProgramm wird beendet. Auf Wiedersehen!");
                    running = false;
                    break;
                default:
                    System.out.println("Ungültige Eingabe! Bitte 1-15 wählen.");
            }
        }
    }
    
    // ============================================================
    // 1. sp_NeuesGerätErfassen
    // ============================================================
    private static void call_sp_NeuesGerätErfassen(Scanner scanner, Connection conn) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("SP_NEUESGERÄTERFASSEN - Neues Gerät erfassen");
        System.out.println("=".repeat(60));
        
        System.out.print("Hersteller (z.B. Apple, Samsung, Dell): ");
        String hersteller = scanner.nextLine();
        
        System.out.print("Modell (z.B. iPhone 15, Galaxy S24, XPS 13): ");
        String modell = scanner.nextLine();
        
        System.out.print("Betriebssystem (Windows, iOS, Android, macOS): ");
        String os = scanner.nextLine();
        
        System.out.print("OS Version (z.B. 17.2, 14, 23H2): ");
        String version = scanner.nextLine();
        
        System.out.print("IMEI/Seriennummer: ");
        String imei = scanner.nextLine();
        
        System.out.print("Status (LAGER/AKTIV/DEFEKT/AUSGESCHIEDEN) [LAGER]: ");
        String status = scanner.nextLine();
        if (status.isEmpty()) status = "LAGER";
        
        System.out.println("\nErfasse neues Gerät...");
        
        try (CallableStatement cstmt = conn.prepareCall("{call sp_NeuesGerätErfassen(?, ?, ?, ?, ?, ?)}")) {
            cstmt.setString(1, hersteller);
            cstmt.setString(2, modell);
            cstmt.setString(3, os);
            cstmt.setString(4, version);
            cstmt.setString(5, imei);
            cstmt.setString(6, status);
            
            ResultSet rs = cstmt.executeQuery();
            if (rs.next()) {
                int newId = rs.getInt(1);
                System.out.println("\n✓ Erfolg! Neues Gerät erfasst mit ID: " + newId);
                System.out.println("Hersteller: " + hersteller);
                System.out.println("Modell: " + modell);
                System.out.println("Status: " + status);
            }
        } catch (SQLException e) {
            System.err.println("✗ Fehler: " + e.getMessage());
        }
    }
    
    // ============================================================
    // 2. sp_GerätEntfernen_Einfach
    // ============================================================
    private static void call_sp_GerätEntfernen_Einfach(Scanner scanner, Connection conn) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("SP_GERÄTENTFERNEN_EINFACH - Gerät entfernen (Soft-Delete)");
        System.out.println("=".repeat(60));
        
        System.out.print("Geräte-ID zum Entfernen: ");
        String gerateIdStr = scanner.nextLine();
        
        System.out.print("Entfernungsgrund (optional): ");
        String grund = scanner.nextLine();
        
        try {
            int gerateId = Integer.parseInt(gerateIdStr);
            
            try (CallableStatement cstmt = conn.prepareCall("{call sp_GerätEntfernen_Einfach(?, ?)}")) {
                cstmt.setInt(1, gerateId);
                if (grund.isEmpty()) {
                    cstmt.setNull(2, Types.NVARCHAR);
                } else {
                    cstmt.setString(2, grund);
                }
                
                ResultSet rs = cstmt.executeQuery();
                if (rs.next()) {
                    System.out.println("\n✓ Erfolg: " + rs.getString("Meldung"));
                    System.out.println("Entfernte Geräte-ID: " + rs.getInt("EntfernteGeräteID"));
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("✗ Ungültige Geräte-ID!");
        } catch (SQLException e) {
            System.err.println("✗ Fehler: " + e.getMessage());
        }
    }
    
    // ============================================================
    // 3. sp_GerätAusgeben
    // ============================================================
    private static void call_sp_GerätAusgeben(Scanner scanner, Connection conn) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("SP_GERÄTAUSGEBEN - Gerät an Mitarbeiter ausgeben");
        System.out.println("=".repeat(60));
        
        System.out.print("Geräte-ID: ");
        String gerateIdStr = scanner.nextLine();
        
        System.out.print("Mitarbeiter-ID: ");
        String mitarbeiterIdStr = scanner.nextLine();
        
        System.out.print("Ausgegeben von (z.B. IT-Admin, IT-Support): ");
        String ausgegebenVon = scanner.nextLine();
        
        try {
            int gerateId = Integer.parseInt(gerateIdStr);
            int mitarbeiterId = Integer.parseInt(mitarbeiterIdStr);
            
            try (CallableStatement cstmt = conn.prepareCall("{call sp_GerätAusgeben(?, ?, ?)}")) {
                cstmt.setInt(1, gerateId);
                cstmt.setInt(2, mitarbeiterId);
                cstmt.setString(3, ausgegebenVon);
                
                ResultSet rs = cstmt.executeQuery();
                if (rs.next()) {
                    System.out.println("\n✓ Erfolg: " + rs.getString("Meldung"));
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("✗ Ungültige ID!");
        } catch (SQLException e) {
            System.err.println("✗ Fehler: " + e.getMessage());
            if (e.getErrorCode() == 50000) { // RAISERROR Fehler
                System.err.println("Das Gerät ist nicht verfügbar oder nicht im Lager.");
            }
        }
    }
    
    // ============================================================
    // 4. sp_GerätZuruecknehmen
    // ============================================================
    private static void call_sp_GerätZuruecknehmen(Scanner scanner, Connection conn) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("SP_GERÄTZURUECKNEHMEN - Gerät zurücknehmen");
        System.out.println("=".repeat(60));
        
        System.out.print("Geräte-ID zur Rücknahme: ");
        String gerateIdStr = scanner.nextLine();
        
        try {
            int gerateId = Integer.parseInt(gerateIdStr);
            
            try (CallableStatement cstmt = conn.prepareCall("{call sp_GerätZuruecknehmen(?)}")) {
                cstmt.setInt(1, gerateId);
                
                ResultSet rs = cstmt.executeQuery();
                if (rs.next()) {
                    System.out.println("\n✓ Erfolg: " + rs.getString("Meldung"));
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("✗ Ungültige Geräte-ID!");
        } catch (SQLException e) {
            System.err.println("✗ Fehler: " + e.getMessage());
        }
    }
    
    // ============================================================
    // 5. sp_GetMitarbeiterGeräte
    // ============================================================
    private static void call_sp_GetMitarbeiterGeräte(Scanner scanner, Connection conn) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("SP_GETMITARBEITERGERÄTE - Geräte eines Mitarbeiters");
        System.out.println("=".repeat(60));
        
        System.out.print("Mitarbeiter-ID: ");
        String mitarbeiterIdStr = scanner.nextLine();
        
        try {
            int mitarbeiterId = Integer.parseInt(mitarbeiterIdStr);
            
            try (CallableStatement cstmt = conn.prepareCall("{call sp_GetMitarbeiterGeräte(?)}")) {
                cstmt.setInt(1, mitarbeiterId);
                
                ResultSet rs = cstmt.executeQuery();
                
                System.out.println("\nGeräte von Mitarbeiter ID " + mitarbeiterId + ":");
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
    // 6. sp_GetVerfügbareGeräte
    // ============================================================
    private static void call_sp_GetVerfügbareGeräte(Scanner scanner, Connection conn) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("SP_GETVERFÜGBAREGERÄTE - Verfügbare Geräte im Lager");
        System.out.println("=".repeat(60));
        
        System.out.print("Gerätetyp-Filter (z.B. iPhone, Galaxy, leer für alle): ");
        String filter = scanner.nextLine();
        
        try (CallableStatement cstmt = conn.prepareCall("{call sp_GetVerfügbareGeräte(?)}")) {
            if (filter.isEmpty()) {
                cstmt.setNull(1, Types.NVARCHAR);
            } else {
                cstmt.setString(1, filter);
            }
            
            ResultSet rs = cstmt.executeQuery();
            
            System.out.println("\nVerfügbare Geräte im Lager" + 
                (filter.isEmpty() ? "" : " (Filter: " + filter + ")") + ":");
            System.out.printf("\n%-5s %-25s %-25s %-15s %-20s%n", 
                "ID", "Gerät", "Betriebssystem", "Lagerdauer", "Im Lager seit");
            System.out.println("-".repeat(90));
            
            int count = 0;
            while (rs.next()) {
                System.out.printf("%-5d %-25s %-25s %-15d %-20s%n",
                    rs.getInt("id"),
                    rs.getString("Gerät"),
                    rs.getString("Betriebssystem"),
                    rs.getInt("Lagerdauer_Tage"),
                    rs.getDate("Im_Lager_seit"));
                count++;
            }
            
            if (count == 0) {
                System.out.println("Keine verfügbaren Geräte gefunden.");
            } else {
                System.out.println("\nGesamt: " + count + " verfügbare Gerät(e)");
            }
        } catch (SQLException e) {
            System.err.println("✗ Fehler: " + e.getMessage());
        }
    }
    
    // ============================================================
    // 7. sp_Monatsreport
    // ============================================================
    private static void call_sp_Monatsreport(Scanner scanner, Connection conn) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("SP_MONATSREPORT - Monatlicher Kostenreport");
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
        
        try (CallableStatement cstmt = conn.prepareCall("{call sp_Monatsreport(?, ?)}")) {
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
    // 8. sp_DemoComplianceCheck
    // ============================================================
    private static void call_sp_DemoComplianceCheck(Scanner scanner, Connection conn) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("SP_DEMOCOMPLIANCECHECK - Compliance-Check für Richtlinie");
        System.out.println("=".repeat(60));
        
        // Zuerst verfügbare Richtlinien anzeigen
        System.out.println("\nVerfügbare Richtlinien:");
        try (Statement stmt = conn.createStatement();
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
            
            try (CallableStatement cstmt = conn.prepareCall("{call sp_DemoComplianceCheck(?)}")) {
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
                        
                        // Details der Prüfungen anzeigen
                        showComplianceDetails(conn, policyId);
                    }
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("✗ Ungültige Richtlinien-ID!");
        } catch (SQLException e) {
            System.err.println("✗ Fehler: " + e.getMessage());
        }
    }
    
    private static void showComplianceDetails(Connection conn, int policyId) throws SQLException {
        System.out.println("\nDetails der Compliance-Prüfungen:");
        
        String sql = "SELECT TOP 10 cp.*, e.hersteller + ' ' + e.modell as Gerät, r.name as Richtlinie " +
                    "FROM CompliancePruefung cp " +
                    "JOIN Endgeraet e ON cp.endgeraetId = e.id " +
                    "JOIN Richtlinie r ON cp.policyId = r.id " +
                    "WHERE cp.policyId = ? " +
                    "ORDER BY cp.geprueftAm DESC";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
            
            System.out.println("\nAngezeigt: " + count + " von " + getTotalComplianceChecks(conn, policyId) + " Prüfungen");
        }
    }
    
    private static int getTotalComplianceChecks(Connection conn, int policyId) throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM CompliancePruefung WHERE policyId = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, policyId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }
    
    // ============================================================
    // 9. sp_AlleComplianceChecks
    // ============================================================
    private static void call_sp_AlleComplianceChecks(Connection conn) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("SP_ALLECOMPLIANCECHECKS - Alle Compliance-Checks durchführen");
        System.out.println("=".repeat(60));
        
        System.out.println("\nStarte umfassenden Compliance-Check für alle Richtlinien...");
        System.out.println("Dies kann einen Moment dauern...");
        
        try (CallableStatement cstmt = conn.prepareCall("{call sp_AlleComplianceChecks}")) {
            // Diese Prozedur gibt mehrere ResultSets zurück
            boolean hasResultSet = cstmt.execute();
            int resultSetCount = 0;
            
            do {
                if (hasResultSet) {
                    ResultSet rs = cstmt.getResultSet();
                    resultSetCount++;
                    
                    if (resultSetCount == 1) {
                        // Erster ResultSet: Gesamtübersicht
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
                        // Zweiter ResultSet: Zusammenfassung
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
    // 10. Views anzeigen
    // ============================================================
    private static void showAllViews(Scanner scanner, Connection conn) throws SQLException {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("ALLE VIEWS IN DER DATENBANK");
        System.out.println("=".repeat(60));
        
        System.out.println("\nWähle eine View:");
        System.out.println("1. V_AktiveGeräte");
        System.out.println("2. V_KostenProAbteilung");
        System.out.println("3. V_Vertragsuebersicht");
        System.out.println("4. V_ComplianceReport");
        System.out.println("5. V_AppInstallationen");
        System.out.print("\nAuswahl (1-5): ");
        
        String choice = scanner.nextLine();
        
        switch (choice) {
            case "1":
                showView(conn, "V_AktiveGeräte", 25);
                break;
            case "2":
                showView(conn, "V_KostenProAbteilung", 0);
                break;
            case "3":
                showView(conn, "V_Vertragsuebersicht", 10);
                break;
            case "4":
                showView(conn, "V_ComplianceReport", 10);
                break;
            case "5":
                showView(conn, "V_AppInstallationen", 0);
                break;
            default:
                System.out.println("Ungültige Auswahl.");
        }
    } 
    
    private static void showView(Connection conn, String viewName, int limit) throws SQLException {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("VIEW: " + viewName);
        System.out.println("=".repeat(80));
        
        String sql = "SELECT * FROM " + viewName;
        if (limit > 0) {
            sql += " ORDER BY 1 OFFSET 0 ROWS FETCH NEXT " + limit + " ROWS ONLY";
        }
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();
            
            // Spaltenüberschriften
            System.out.println();
            for (int i = 1; i <= columnCount; i++) {
                System.out.printf("%-20s ", meta.getColumnName(i));
            }
            System.out.println();
            System.out.println("-".repeat(columnCount * 20));
            
            // Daten
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
    
	 // ============================================================
	 // 11. Tabellen anzeigen - Minimale Version
	 // ============================================================
	 private static void showAllTables(Connection conn) throws SQLException {
	     System.out.println("\n" + "=".repeat(60));
	     System.out.println("ALLE TABELLEN IN DER DATENBANK");
	     System.out.println("=".repeat(60));
	     
	     // NUR TABELLEN (keine Views)
	     String sql = "SELECT TABLE_NAME " +
	                 "FROM INFORMATION_SCHEMA.TABLES " +
	                 "WHERE TABLE_SCHEMA = 'dbo' " +
	                 "AND TABLE_TYPE = 'BASE TABLE' " +
	                 "ORDER BY TABLE_NAME";
	     
	     try (Statement stmt = conn.createStatement();
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
    // Hilfsmethoden für Fehlerbehandlung
    // ============================================================
    private static void handleSQLException(SQLException e) {
        System.err.println("\n✗ SQL FEHLER:");
        System.err.println("  Message: " + e.getMessage());
        System.err.println("  SQL State: " + e.getSQLState());
        System.err.println("  Error Code: " + e.getErrorCode());
        
        System.out.println("\n=== FEHLERBEHEBUNG ===");
        
        if (e.getErrorCode() == 18456) { // Login failed
            System.err.println("\nLOGIN FEHLGESCHLAGEN");
            System.err.println("Überprüfe:");
            System.err.println("1. SQL Server Authentication aktiviert?");
            System.err.println("2. Login 'emm_user' existiert?");
            System.err.println("3. Passwort korrekt?");
        } else if (e.getErrorCode() == 4060) { // Cannot open database
            System.err.println("\nDATENBANK NICHT GEFUNDEN!");
            System.err.println("Stelle sicher, dass die Datenbank 'EMM_Demo' existiert.");
        } else if (e.getErrorCode() == 229) { // Permission denied
            System.err.println("\nBERECHTIGUNG VERWEIGERT!");
            System.err.println("Dem Benutzer fehlen Rechte für diese Operation.");
        }
        
        System.err.println("\nStacktrace für detaillierte Analyse:");
        e.printStackTrace();
    }
    
	 // ============================================================
	 // 12. AUDITLOG anzeigen - NEUE METHODE
	 // ============================================================
	 private static void showAuditLog(Scanner scanner, Connection conn) {
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
	             // Kein zusätzlicher Filter
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
	         // Kein Limit
	     }
	     
	     System.out.println("\n" + "=".repeat(120));
	     System.out.println("AUDITLOG EINTRÄGE");
	     System.out.println("=".repeat(120));
	     
	     try (Statement stmt = conn.createStatement();
	          ResultSet rs = stmt.executeQuery(sql)) {
	         
	         System.out.printf("\n%-5s %-15s %-20s %-25s %-25s %-20s %-15s%n", 
	             "ID", "Tabelle", "Aktion", "Alt", "Neu", "Zeitpunkt", "Benutzer");
	         System.out.println("-".repeat(125));
	         
	         int count = 0;
	         while (rs.next()) {
	             String alt = rs.getString("alt");
	             String neu = rs.getString("neu");
	             
	             // Kürze lange Werte für bessere Darstellung
	             if (alt != null && alt.length() > 20) alt = alt.substring(0, 17) + "...";
	             if (neu != null && neu.length() > 20) neu = neu.substring(0, 17) + "...";
	             
	             System.out.printf("%-5d %-15s %-20s %-25s %-25s %-20s %-15s%n",
	                 rs.getInt("id"),
	                 rs.getString("tabelle"),
	                 rs.getString("aktion"),
	                 (alt != null ? alt : "NULL"),
	                 (neu != null ? neu : "NULL"),
	                 rs.getTimestamp("zeitpunkt"),
	                 rs.getString("benutzer"));
	             count++;
	         }
	         
	         System.out.println("\n" + "=".repeat(60));
	         System.out.println("GESAMT: " + count + " AuditLog-Einträge");
	         System.out.println("=".repeat(60));
	         
	         // Zeige Statistik
	         showAuditLogStatistics(conn);
	         
	     } catch (SQLException e) {
	         System.err.println("✗ Fehler beim Abrufen des AuditLogs: " + e.getMessage());
	         System.err.println("SQL: " + sql);
	         
	         // Prüfe ob Tabelle existiert
	         checkAuditLogTable(conn);
	     }
	 }

	 // ============================================================
	 // Hilfsmethode für AuditLog-Statistik
	 // ============================================================
	 private static void showAuditLogStatistics(Connection conn) throws SQLException {
	     System.out.println("\n" + "-".repeat(60));
	     System.out.println("AUDITLOG STATISTIK");
	     System.out.println("-".repeat(60));
	     
	     // Statistik SQL
	     String statSql = 
	         "SELECT " +
	         "    COUNT(*) as TotalEntries, " +
	         "    COUNT(DISTINCT tabelle) as TablesTracked, " +
	         "    MIN(zeitpunkt) as FirstEntry, " +
	         "    MAX(zeitpunkt) as LastEntry, " +
	         "    COUNT(DISTINCT benutzer) as UsersTracked, " +
	         "    (SELECT COUNT(*) FROM AuditLog WHERE tabelle = 'Endgeraet' AND aktion = 'Statusänderung') as StatusChanges " +
	         "FROM AuditLog";
	     
	     try (Statement stmt = conn.createStatement();
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
	                 
	                 // Top 5 Aktionen
	                 System.out.println("\nTop 5 Aktionen:");
	                 String topActionsSql = 
	                     "SELECT TOP 5 aktion, COUNT(*) as count " +
	                     "FROM AuditLog " +
	                     "GROUP BY aktion " +
	                     "ORDER BY count DESC";
	                 
	                 try (Statement stmt2 = conn.createStatement();
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

	// ============================================================
	// 13. AUDITLOG TEST - Trigger testen
	// ============================================================
	private static void testAuditLogTrigger(Scanner scanner, Connection conn) {
	    System.out.println("\n" + "=".repeat(80));
	    System.out.println("AUDITLOG TRIGGER TEST");
	    System.out.println("=".repeat(80));
	    
	    System.out.println("\nDies testet den Trigger trg_GeräteStatusChange.");
	    System.out.println("Es wird ein Gerätestatus geändert, was einen AuditLog-Eintrag erzeugen sollte.");
	    
	    // 1. Suche ein Gerät zum Testen
	    System.out.println("\nSuche ein geeignetes Testgerät...");
	    
	    // DIESE QUERY IST SICHER (keine Benutzereingaben)
	    String findSql = 
	        "SELECT TOP 3 id, hersteller, modell, status " +
	        "FROM Endgeraet " +
	        "WHERE status IN ('LAGER', 'AKTIV') " +
	        "ORDER BY id";
	    
	    try (Statement stmt = conn.createStatement();
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
	        
	        // 1. Aktuellen Status abrufen (vor Änderung)
	        String currentStatus = getDeviceStatus(conn, deviceId);
	        if (currentStatus == null) {
	            System.out.println("✗ Gerät mit ID " + deviceId + " nicht gefunden.");
	            return;
	        }
	        
	        System.out.println("\n=== TEST VORBEREITUNG ===");
	        System.out.println("Gerät ID: " + deviceId);
	        System.out.println("Aktueller Status: " + currentStatus);
	        System.out.println("Neuer Status: " + newStatus);
	        
	        if (currentStatus.equals(newStatus)) {
	            System.out.println("\n⚠️  Status ist bereits " + newStatus + ". Wähle einen anderen Status.");
	            return;
	        }
	        
	        // 2. Aktuelle AuditLog-Einträge zählen (vor Änderung)
	        int auditCountBefore = getAuditLogCount(conn);
	        System.out.println("AuditLog-Einträge vorher: " + auditCountBefore);
	        
	        System.out.println("\n=== FÜHRE STATUSÄNDERUNG DURCH ===");
	        
	        // 3. Status ändern (dies sollte den Trigger auslösen)
	        String updateSql = "UPDATE Endgeraet SET status = ? WHERE id = ?";
	        try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
	            pstmt.setString(1, newStatus);
	            pstmt.setInt(2, deviceId);
	            
	            int rowsAffected = pstmt.executeUpdate();
	            System.out.println("UPDATE ausgeführt. Betroffene Zeilen: " + rowsAffected);
	            
	            if (rowsAffected > 0) {
	                // 4. Warten (für bessere Sichtbarkeit in Logs)
	                Thread.sleep(1000);
	                
	                // 5. AuditLog-Einträge nachher zählen
	                int auditCountAfter = getAuditLogCount(conn);
	                System.out.println("AuditLog-Einträge nachher: " + auditCountAfter);
	                
	                int newEntries = auditCountAfter - auditCountBefore;
	                System.out.println("Neue AuditLog-Einträge: " + newEntries);
	                
	                if (newEntries > 0) {
	                    System.out.println("\n✓ ERFOLG: Trigger wurde ausgelöst!");
	                    
	                    // 6. Zeige die neuen AuditLog-Einträge
	                    System.out.println("\n=== NEUE AUDITLOG-EINTRÄGE ===");
	                    
	                    // FETCH NEXT mit ROW_NUMBER() (für SQL Server)
	                    String newAuditSql = 
	                        "SELECT * FROM (" +
	                        "    SELECT *, ROW_NUMBER() OVER (ORDER BY id DESC) as rn " +
	                        "    FROM AuditLog " +
	                        ") as numbered " +
	                        "WHERE rn <= ? " +
	                        "ORDER BY id DESC";
	                    
	                    try (PreparedStatement pstmt2 = conn.prepareStatement(newAuditSql)) {
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
	                    
	                    // 7. Status zurückändern (für weitere Tests)
	                    System.out.print("\nStatus zurücksetzen auf '" + currentStatus + "'? (j/n): ");
	                    String resetChoice = scanner.nextLine().toLowerCase();
	                    
	                    if (resetChoice.equals("j") || resetChoice.equals("ja")) {
	                        try (PreparedStatement pstmt3 = conn.prepareStatement(updateSql)) {
	                            pstmt3.setString(1, currentStatus);
	                            pstmt3.setInt(2, deviceId);
	                            pstmt3.executeUpdate();
	                            System.out.println("✓ Status zurückgesetzt auf: " + currentStatus);
	                        }
	                    }
	                    
	                } else {
	                    System.out.println("\n✗ FEHLER: Keine neuen AuditLog-Einträge erstellt!");
	                    System.out.println("Mögliche Ursachen:");
	                    System.out.println("1. Trigger ist nicht aktiv oder fehlerhaft");
	                    System.out.println("2. AuditLog-Tabelle existiert nicht");
	                    System.out.println("3. Benutzer hat keine INSERT-Rechte auf AuditLog");
	                }
	            }
	        }
	        
	    } catch (NumberFormatException e) {
	        System.out.println("✗ Ungültige Geräte-ID!");
	    } catch (SQLException e) {
	        System.err.println("✗ SQL Fehler: " + e.getMessage());
	        if (e.getErrorCode() == 544) { // CHECK constraint violation
	            System.err.println("Status muss einer der folgenden sein: LAGER, AKTIV, DEFEKT, AUSGESCHIEDEN");
	        }
	    } catch (InterruptedException e) {
	        System.err.println("✗ Sleep unterbrochen: " + e.getMessage());
	    }
	}
	 // ============================================================
	 // Hilfsmethoden für AuditLog
	 // ============================================================
	 private static boolean isValidStatus(String status) {
	     return status.equals("LAGER") || status.equals("AKTIV") || 
	        status.equals("DEFEKT") || status.equals("AUSGESCHIEDEN");
	 }
	
	 private static String getDeviceStatus(Connection conn, int deviceId) throws SQLException {
	     String sql = "SELECT status FROM Endgeraet WHERE id = ?";
	 try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
	     pstmt.setInt(1, deviceId);
	     ResultSet rs = pstmt.executeQuery();
	     if (rs.next()) {
	         return rs.getString("status");
	         }
	     }
	     return null;
	 }
	
	 private static int getAuditLogCount(Connection conn) throws SQLException {
	     String sql = "SELECT COUNT(*) as count FROM AuditLog";
	 try (Statement stmt = conn.createStatement();
	      ResultSet rs = stmt.executeQuery(sql)) {
	     if (rs.next()) {
	         return rs.getInt("count");
	         }
	     }
	     return 0;
	 }
	
	 private static void checkAuditLogTable(Connection conn) {
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
	         try (Statement stmt = conn.createStatement();
	              ResultSet rs = stmt.executeQuery(checkQueries[i])) {
	             if (rs.next()) {
	                 Object result = rs.getObject(1);
	                 System.out.println(messages[i] + result);
	             }
	         }
	     }
	     
	     // Prüfe ob Trigger auf Endgeraet-Tabelle aktiv ist
	     String triggerCheck = 
	         "SELECT COUNT(*) as has_trigger " +
	         "FROM sys.triggers t " +
	         "JOIN sys.objects o ON t.parent_id = o.object_id " +
	         "WHERE t.name = 'trg_GeräteStatusChange' " +
	         "AND o.name = 'Endgeraet'";
	     
	     try (Statement stmt = conn.createStatement();
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
	
	private static void closeResources(Scanner scanner, Connection conn) {
	    try {
	        if (scanner != null) {
	            scanner.close();
	        }
	    } catch (Exception e) {
	        System.err.println("Fehler beim Schließen des Scanners: " + e.getMessage());
	    }
	    
	    try {
	        if (conn != null && !conn.isClosed()) {
	            conn.close();
	            System.out.println("\n✓ Verbindung ordnungsgemäß geschlossen.");
	        }
	    } catch (SQLException e) {
	        System.err.println("Fehler beim Schließen der Verbindung: " + e.getMessage());
	    }
}
	 
	// ============================================================
	// 14. Datenbank-Informationen
	// ============================================================
	private static void showDatabaseInfo(Connection conn) throws SQLException {
	    System.out.println("\n" + "=".repeat(60));
	    System.out.println("DATENBANK-INFORMATIONEN");
	    System.out.println("=".repeat(60));
	    
	    DatabaseMetaData meta = conn.getMetaData();
	    
	    System.out.println("\n=== Verbindungsinformationen ===");
	    System.out.println("Datenbank: " + meta.getDatabaseProductName() 
	                     + " " + meta.getDatabaseProductVersion());
	    System.out.println("JDBC-Treiber: " + meta.getDriverName() 
	                     + " " + meta.getDriverVersion());
	    System.out.println("URL: " + meta.getURL());
	    System.out.println("Benutzer: " + meta.getUserName());
	    
	    // Statistiken
	    System.out.println("\n=== Datenbank-Statistiken ===");
	    
	    // Tabellen auflisten und zählen
	    int tableCount = 0;
	    System.out.println("\n=== TABELLEN ===");
	    
	    try (Statement stmt = conn.createStatement();
	         ResultSet rs = stmt.executeQuery(
	             "SELECT name, type_desc, create_date " +
	             "FROM sys.objects " +
	             "WHERE type = 'U' " +  // U = User table
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
	        
	    } catch (SQLException e) {
	        System.out.println("Kann Tabellen nicht auflisten: " + e.getMessage());
	        // Fallback: Einfache Zählung über DatabaseMetaData
	        String[] tableTypes = {"TABLE"};
	        ResultSet tables = meta.getTables(null, "dbo", "%", tableTypes);
	        while (tables.next()) tableCount++;
	        tables.close();
	    }
	    
	    // Views auflisten und zählen
	    int viewCount = 0;
	    System.out.println("\n=== VIEWS ===");
	    
	    try (Statement stmt = conn.createStatement();
	         ResultSet rs = stmt.executeQuery(
	             "SELECT name, type_desc, create_date " +
	             "FROM sys.objects " +
	             "WHERE type = 'V' " +  // V = View
	             "AND is_ms_shipped = 0 " +  // Nur benutzerdefinierte Views
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
	        
	    } catch (SQLException e) {
	        System.out.println("Kann Views nicht auflisten: " + e.getMessage());
	        // Fallback: Einfache Zählung
	        try (Statement stmt2 = conn.createStatement();
	             ResultSet rs2 = stmt2.executeQuery(
	                 "SELECT COUNT(*) as view_count " +
	                 "FROM sys.objects " +
	                 "WHERE type = 'V' " +
	                 "AND is_ms_shipped = 0")) {
	            if (rs2.next()) {
	                viewCount = rs2.getInt("view_count");
	            }
	        }
	    }
	    
	    // Stored Procedures auflisten und zählen (filtere Diagramm-Prozeduren heraus)
	    int userProcCount = 0;
	    System.out.println("\n=== BENUTZERDEFINIERTE STORED PROCEDURES ===");
	    
	    try (Statement stmt = conn.createStatement();
	         ResultSet rs = stmt.executeQuery(
	             "SELECT name, type_desc, create_date " +
	             "FROM sys.objects " +
	             "WHERE type IN ('P', 'PC') " +  	// P = SQL Stored Procedure, PC = CLR Stored Procedure
	             "AND is_ms_shipped = 0 " +       	// Nur benutzerdefinierte
	             "AND name NOT LIKE '%diagram%' " + // Filtere Diagramm-Prozeduren heraus
	             "AND name NOT LIKE '%Diagram%' " + // Großbuchstaben auch
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
	        
	    } catch (SQLException e) {
	        System.out.println("Kann Stored Procedures nicht auflisten: " + e.getMessage());
	        // Fallback: Einfache Zählung
	        try (Statement stmt2 = conn.createStatement();
	             ResultSet rs2 = stmt2.executeQuery(
	                 "SELECT COUNT(*) as proc_count " +
	                 "FROM sys.objects " +
	                 "WHERE type IN ('P', 'PC') " +
	                 "AND is_ms_shipped = 0 " +
	                 "AND name NOT LIKE '%diagram%' " +
	                 "AND name NOT LIKE '%Diagram%'")) {
	            if (rs2.next()) {
	                userProcCount = rs2.getInt("proc_count");
	            }
	        }
	    }
	}
}