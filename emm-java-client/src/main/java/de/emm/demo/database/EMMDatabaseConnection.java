// EMMDatabaseConnection.java
package de.emm.demo.database;

import java.sql.*;
import java.util.Properties;

public class EMMDatabaseConnection {
    
    private static final String SERVER = "localhost\\SQLEXPRESS";
    private static final String DATABASE = "EMM_Demo";
    private static final int PORT = 1433;
    private static final String USERNAME = "emm_user";
    private static final String PASSWORD = "emm_x123";
    
    private Connection connection;
    
    public EMMDatabaseConnection() {
        // Konstruktor
    }
    
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connect();
        }
        return connection;
    }
    
    private void connect() throws SQLException {
        String url = String.format(
            "jdbc:sqlserver://%s:%d;databaseName=%s;encrypt=false;trustServerCertificate=true",
            SERVER, PORT, DATABASE
        );
        
        System.out.println("Verbinde zu Datenbank...");
        System.out.println("  URL: " + url);
        System.out.println("  User: " + USERNAME);
        
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("JDBC-Treiber nicht gefunden", e);
        }
        
        Properties props = new Properties();
        props.setProperty("user", USERNAME);
        props.setProperty("password", PASSWORD);
        
        connection = DriverManager.getConnection(url, props);
        System.out.println("✓ Verbindung erfolgreich");
    }
    
    public boolean testConnection() {
        try {
            Connection conn = getConnection();
            if (conn != null && !conn.isClosed()) {
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT 1")) {
                    return rs.next();
                }
            }
            return false;
        } catch (SQLException e) {
            return false;
        }
    }
    
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✓ Datenbankverbindung geschlossen");
            }
        } catch (SQLException e) {
            System.err.println("Fehler beim Schließen der Verbindung: " + e.getMessage());
        }
    }
    
    public void showConnectionInfo() throws SQLException {
        Connection conn = getConnection();
        DatabaseMetaData meta = conn.getMetaData();
        
        System.out.println("\n=== Verbindungsdetails ===");
        System.out.println("Datenbank: " + meta.getDatabaseProductName() 
                         + " " + meta.getDatabaseProductVersion());
        System.out.println("JDBC-Treiber: " + meta.getDriverName() 
                         + " " + meta.getDriverVersion());
        System.out.println("URL: " + meta.getURL());
        System.out.println("Benutzer: " + meta.getUserName());
    }
    
    // Fehlerbehandlung
    public static void handleSQLException(SQLException e) {
        System.err.println("\n✗ SQL FEHLER:");
        System.err.println("  Message: " + e.getMessage());
        System.err.println("  SQL State: " + e.getSQLState());
        System.err.println("  Error Code: " + e.getErrorCode());
        
        System.out.println("\n=== FEHLERBEHEBUNG ===");
        
        if (e.getErrorCode() == 18456) {
            System.err.println("\nLOGIN FEHLGESCHLAGEN");
            System.err.println("Überprüfe:");
            System.err.println("1. SQL Server Authentication aktiviert?");
            System.err.println("2. Login 'emm_user' existiert?");
            System.err.println("3. Passwort korrekt?");
        } else if (e.getErrorCode() == 4060) {
            System.err.println("\nDATENBANK NICHT GEFUNDEN!");
            System.err.println("Stelle sicher, dass die Datenbank 'EMM_Demo' existiert.");
        } else if (e.getErrorCode() == 229) {
            System.err.println("\nBERECHTIGUNG VERWEIGERT!");
            System.err.println("Dem Benutzer fehlen Rechte für diese Operation.");
        }
    }
}