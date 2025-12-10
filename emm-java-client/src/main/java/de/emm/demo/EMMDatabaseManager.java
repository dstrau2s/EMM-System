// EMMDatabaseManager.java (angepasst)
package de.emm.demo;

import java.sql.Connection;
import java.util.Scanner;
import de.emm.demo.database.EMMDatabaseConnection;
import de.emm.demo.repository.AuditRepository;
import de.emm.demo.repository.DeviceRepository;
import de.emm.demo.ui.MenuManager;



public class EMMDatabaseManager {
    
    public static void main(String[] args) {
        System.out.println("=== EMM Database Manager (Objektorientierte Version) ===");
        
        EMMDatabaseConnection dbConnection = null;
        Scanner scanner = new Scanner(System.in);
        
        try {
            // 1. Datenbankverbindung herstellen
            dbConnection = new EMMDatabaseConnection();
            
            if (!dbConnection.testConnection()) {
                System.err.println("✗ Verbindungstest fehlgeschlagen!");
                return;
            }
            
            dbConnection.showConnectionInfo();
            
            // 2. Repositories erstellen
            Connection conn = dbConnection.getConnection();
            DeviceRepository deviceRepo = new DeviceRepository(conn);
            AuditRepository auditRepo = new AuditRepository(conn, deviceRepo);
            
            // 3. MenuManager mit Connection erstellen
            MenuManager menuManager = new MenuManager(scanner, deviceRepo, auditRepo, conn);
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