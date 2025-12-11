// EMMDatabaseManager.java (angepasst)
package de.emm.demo;

import java.sql.Connection;
import java.util.Scanner;
import de.emm.demo.database.EMMDatabaseConnection;
import de.emm.demo.repository.AuditRepository;
import de.emm.demo.repository.DeviceRepository;
import de.emm.demo.service.AuditService;
import de.emm.demo.service.ComplianceService;
import de.emm.demo.service.DeviceService;
import de.emm.demo.service.ReportService;
import de.emm.demo.ui.MenuManager;


public class EMMDatabaseManager {
 
 public static void main(String[] args) {
     System.out.println("=== EMM Database Manager (Service-Architektur) ===");
     
     EMMDatabaseConnection dbConnection = null;
     Scanner scanner = new Scanner(System.in);
     
     try {
         // 1. Datenbankverbindung
         dbConnection = new EMMDatabaseConnection();
         
         if (!dbConnection.testConnection()) {
             System.err.println("✗ Verbindungstest fehlgeschlagen!");
             return;
         }
         
         dbConnection.showConnectionInfo();
         
         // 2. Repositories erstellen
         Connection conn = dbConnection.getConnection();
         DeviceRepository deviceRepo = new DeviceRepository(conn);
         AuditRepository auditRepo = new AuditRepository(conn);
         
         // 3. Services erstellen
         AuditService auditService = new AuditService(auditRepo);
         DeviceService deviceService = new DeviceService(deviceRepo, auditService);
         ComplianceService complianceService = new ComplianceService();
         ReportService reportService = new ReportService();
         
         // 4. MenuManager mit Services
         MenuManager menuManager = new MenuManager(
             scanner, 
             deviceService, 
             complianceService, 
             reportService, 
             conn
         );
         menuManager.run();
         
     } catch (Exception e) {
         System.err.println("\n✗ Allgemeiner Fehler: " + e.getMessage());
         e.printStackTrace();
     } finally {
         if (dbConnection != null) {
             dbConnection.close();
         }
         if (scanner != null) {
             scanner.close();
         }
     }
 }
}