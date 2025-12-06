package de.emm.demo;

public class JavaVersionTest {
    public static void main(String[] args) {
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("Java Vendor: " + System.getProperty("java.vendor"));
        System.out.println("Java Home: " + System.getProperty("java.home"));
        
        // Test JDBC Treiber Verfügbarkeit
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            System.out.println("JDBC Treiber gefunden!");
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Treiber NICHT gefunden!");
        }
    }
}