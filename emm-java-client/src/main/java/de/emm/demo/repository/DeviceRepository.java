// DeviceRepository.java (vollständige Version)
package de.emm.demo.repository;

import de.emm.demo.model.Device;
import de.emm.demo.model.DeviceAssignment;
import de.emm.demo.model.MonthlyReport;
import de.emm.demo.model.ComplianceCheck;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DeviceRepository {
    
    private final Connection connection;
    
    public DeviceRepository(Connection connection) {
        this.connection = connection;
    }
    
    // ==================== GRUNDLEGENDE CRUD-OPERATIONEN ====================
    
    /**
     * Erstellt ein neues Gerät
     */
    public Device createDevice(String hersteller, String modell, String os, 
                              String version, String imei, String status) throws SQLException {
        
        String sql = "{call sp_NeuesGerätErfassen(?, ?, ?, ?, ?, ?)}";
        
        try (CallableStatement cstmt = connection.prepareCall(sql)) {
            cstmt.setString(1, hersteller);
            cstmt.setString(2, modell);
            cstmt.setString(3, os);
            cstmt.setString(4, version);
            cstmt.setString(5, imei);
            cstmt.setString(6, status);
            
            try (ResultSet rs = cstmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    return new Device(id, hersteller, modell, os, version, imei, status);
                }
            }
        }
        return null;
    }
    
    /**
     * Holt ein Gerät anhand der ID
     */
    public Device getDeviceById(int deviceId) throws SQLException {
        String sql = "SELECT * FROM Endgeraet WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, deviceId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDevice(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Aktualisiert den Status eines Geräts
     */
    public boolean updateDeviceStatus(int deviceId, String newStatus) throws SQLException {
        String sql = "UPDATE Endgeraet SET status = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, deviceId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Prüft, ob ein Gerät existiert
     */
    public boolean deviceExists(int deviceId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Endgeraet WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, deviceId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    /**
     * Holt den Status eines Geräts
     */
    public String getDeviceStatus(int deviceId) throws SQLException {
        String sql = "SELECT status FROM Endgeraet WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, deviceId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("status");
                }
            }
        }
        return null;
    }
    
    // ==================== STORED PROCEDURE OPERATIONEN ====================
    
    /**
     * Ruft sp_GerätEntfernen_Einfach auf (Soft-Delete)
     */
    public String removeDevice(int deviceId, String reason) throws SQLException {
        String sql = "{call sp_GerätEntfernen_Einfach(?, ?)}";
        
        try (CallableStatement cstmt = connection.prepareCall(sql)) {
            cstmt.setInt(1, deviceId);
            if (reason == null || reason.isEmpty()) {
                cstmt.setNull(2, Types.NVARCHAR);
            } else {
                cstmt.setString(2, reason);
            }
            
            try (ResultSet rs = cstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Meldung");
                }
            }
        }
        return null;
    }
    
    /**
     * Ruft sp_GerätAusgeben auf (Gerät an Mitarbeiter ausgeben)
     */
    public String assignDeviceToEmployee(int deviceId, int employeeId, String issuedBy) throws SQLException {
        String sql = "{call sp_GerätAusgeben(?, ?, ?)}";
        
        try (CallableStatement cstmt = connection.prepareCall(sql)) {
            cstmt.setInt(1, deviceId);
            cstmt.setInt(2, employeeId);
            cstmt.setString(3, issuedBy);
            
            try (ResultSet rs = cstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Meldung");
                }
            }
        }
        return null;
    }
    
    /**
     * Ruft sp_GerätZuruecknehmen auf (Gerät zurücknehmen)
     */
    public String returnDevice(int deviceId) throws SQLException {
        String sql = "{call sp_GerätZuruecknehmen(?)}";
        
        try (CallableStatement cstmt = connection.prepareCall(sql)) {
            cstmt.setInt(1, deviceId);
            
            try (ResultSet rs = cstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Meldung");
                }
            }
        }
        return null;
    }
    
    /**
     * Ruft sp_GetMitarbeiterGeräte auf (Geräte eines Mitarbeiters)
     */
    public List<DeviceAssignment> getEmployeeDevices(int employeeId) throws SQLException {
        List<DeviceAssignment> assignments = new ArrayList<>();
        String sql = "{call sp_GetMitarbeiterGeräte(?)}";
        
        try (CallableStatement cstmt = connection.prepareCall(sql)) {
            cstmt.setInt(1, employeeId);
            
            try (ResultSet rs = cstmt.executeQuery()) {
                while (rs.next()) {
                    DeviceAssignment assignment = new DeviceAssignment();
                    assignment.setDeviceName(rs.getString("Gerät"));
                    assignment.setStatus(rs.getString("status"));
                    assignment.setOsVersion(rs.getString("OS_Version"));
                    assignment.setAssignmentDate(rs.getDate("ausgabedatum"));
                    assignment.setDaysInUse(rs.getInt("Tage_im_Einsatz"));
                    assignments.add(assignment);
                }
            }
        }
        return assignments;
    }
    
    /**
     * Ruft sp_GetVerfügbareGeräte auf (Verfügbare Geräte im Lager)
     */
    public List<Device> getAvailableDevices(String filter) throws SQLException {
        List<Device> devices = new ArrayList<>();
        String sql = "{call sp_GetVerfügbareGeräte(?)}";
        
        try (CallableStatement cstmt = connection.prepareCall(sql)) {
            if (filter == null || filter.isEmpty()) {
                cstmt.setNull(1, Types.NVARCHAR);
            } else {
                cstmt.setString(1, filter);
            }
            
            try (ResultSet rs = cstmt.executeQuery()) {
                while (rs.next()) {
                    Device device = new Device();
                    device.setId(rs.getInt("id"));
                    // Extrahiere Hersteller und Modell aus der "Gerät" Spalte
                    String gerat = rs.getString("Gerät");
                    if (gerat != null && gerat.contains(" ")) {
                        String[] parts = gerat.split(" ", 2);
                        device.setManufacturer(parts[0]);
                        device.setModel(parts.length > 1 ? parts[1] : "");
                    }
                    device.setOs(rs.getString("Betriebssystem"));
                    device.setStatus("LAGER");
                    devices.add(device);
                }
            }
        }
        return devices;
    }
    
    /**
     * Ruft sp_Monatsreport auf (Monatlicher Kostenreport)
     */
    public List<MonthlyReport> getMonthlyReport(int month, int year) throws SQLException {
        List<MonthlyReport> reports = new ArrayList<>();
        String sql = "{call sp_Monatsreport(?, ?)}";
        
        try (CallableStatement cstmt = connection.prepareCall(sql)) {
            cstmt.setInt(1, month);
            cstmt.setInt(2, year);
            
            try (ResultSet rs = cstmt.executeQuery()) {
                while (rs.next()) {
                    MonthlyReport report = new MonthlyReport();
                    report.setDepartment(rs.getString("Abteilung"));
                    report.setDeviceCount(rs.getInt("Anzahl_Geräte"));
                    report.setTotalCost(rs.getDouble("Gesamtkosten"));
                    report.setAvgDataVolume(rs.getDouble("Durchschnitts_Datenvolumen"));
                    report.setBudget(rs.getDouble("Budget"));
                    report.setBudgetRemaining(rs.getDouble("Budget_Rest"));
                    reports.add(report);
                }
            }
        }
        return reports;
    }
    
    /**
     * Ruft sp_DemoComplianceCheck auf (Compliance-Check für Richtlinie)
     */
    public ComplianceCheck performComplianceCheck(int policyId) throws SQLException {
        String sql = "{call sp_DemoComplianceCheck(?)}";
        
        try (CallableStatement cstmt = connection.prepareCall(sql)) {
            cstmt.setInt(1, policyId);
            
            boolean hasResultSet = cstmt.execute();
            if (hasResultSet) {
                try (ResultSet rs = cstmt.getResultSet()) {
                    if (rs.next()) {
                        ComplianceCheck check = new ComplianceCheck();
                        check.setPolicyName(rs.getString("Geprüfte Richtlinie"));
                        check.setDeviceCount(rs.getInt("Anzahl Geräte"));
                        check.setCompliantCount(rs.getInt("Erfüllt"));
                        check.setNonCompliantCount(rs.getInt("Nicht erfüllt"));
                        check.setComplianceRate(rs.getDouble("Erfüllungsquote (%)"));
                        check.setCheckTimestamp(rs.getTimestamp("Prüfungszeitpunkt"));
                        return check;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Ruft sp_AlleComplianceChecks auf (Alle Compliance-Checks)
     */
    public List<ComplianceCheck> performAllComplianceChecks() throws SQLException {
        List<ComplianceCheck> checks = new ArrayList<>();
        String sql = "{call sp_AlleComplianceChecks}";
        
        try (CallableStatement cstmt = connection.prepareCall(sql)) {
            boolean hasResultSet = cstmt.execute();
            int resultSetCount = 0;
            
            do {
                if (hasResultSet) {
                    ResultSet rs = cstmt.getResultSet();
                    resultSetCount++;
                    
                    if (resultSetCount == 1) {
                        // Erster ResultSet: Gesamtübersicht
                        while (rs.next()) {
                            ComplianceCheck check = new ComplianceCheck();
                            check.setPolicyName(rs.getString("Richtlinie"));
                            check.setDeviceCount(rs.getInt("Geräte"));
                            check.setCompliantCount(rs.getInt("✓ Erfüllt"));
                            check.setNonCompliantCount(rs.getInt("✗ Nicht erfüllt"));
                            check.setComplianceRate(rs.getDouble("Quote (%)"));
                            check.setCheckTimestamp(rs.getTimestamp("Geprüft am"));
                            checks.add(check);
                        }
                    }
                }
                hasResultSet = cstmt.getMoreResults();
            } while (hasResultSet || cstmt.getUpdateCount() != -1);
        }
        return checks;
    }
    
    // ==================== VIEW-OPERATIONEN ====================
    
    /**
     * Holt Daten aus der View V_AktiveGeräte
     */
    public List<Device> getActiveDevices(int limit) throws SQLException {
        List<Device> devices = new ArrayList<>();
        String sql = "SELECT * FROM V_AktiveGeräte";
        if (limit > 0) {
            sql += " ORDER BY 1 OFFSET 0 ROWS FETCH NEXT " + limit + " ROWS ONLY";
        }
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                devices.add(mapResultSetToDevice(rs));
            }
        }
        return devices;
    }
    
    /**
     * Holt Daten aus der View V_KostenProAbteilung
     */
    public ResultSet getCostsPerDepartment() throws SQLException {
        String sql = "SELECT * FROM V_KostenProAbteilung";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }
    
    /**
     * Holt Daten aus der View V_Vertragsuebersicht
     */
    public ResultSet getContractOverview(int limit) throws SQLException {
        String sql = "SELECT * FROM V_Vertragsuebersicht";
        if (limit > 0) {
            sql += " ORDER BY 1 OFFSET 0 ROWS FETCH NEXT " + limit + " ROWS ONLY";
        }
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }
    
    /**
     * Holt Daten aus der View V_ComplianceReport
     */
    public ResultSet getComplianceReport(int limit) throws SQLException {
        String sql = "SELECT * FROM V_ComplianceReport";
        if (limit > 0) {
            sql += " ORDER BY 1 OFFSET 0 ROWS FETCH NEXT " + limit + " ROWS ONLY";
        }
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }
    
    /**
     * Holt Daten aus der View V_AppInstallationen
     */
    public ResultSet getAppInstallations() throws SQLException {
        String sql = "SELECT * FROM V_AppInstallationen";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }
    
    // ==================== TABELLEN-OPERATIONEN ====================
    
    /**
     * Holt alle Tabellennamen aus der Datenbank
     */
    public List<String> getAllTableNames() throws SQLException {
        List<String> tableNames = new ArrayList<>();
        String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES " +
                    "WHERE TABLE_SCHEMA = 'dbo' AND TABLE_TYPE = 'BASE TABLE' " +
                    "ORDER BY TABLE_NAME";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                tableNames.add(rs.getString("TABLE_NAME"));
            }
        }
        return tableNames;
    }
    
    /**
     Holt alle Geräte mit einem bestimmten Status
     */
    public List<Device> getDevicesByStatus(String status) throws SQLException {
        List<Device> devices = new ArrayList<>();
        String sql = "SELECT * FROM Endgeraet WHERE status = ? ORDER BY id";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    devices.add(mapResultSetToDevice(rs));
                }
            }
        }
        return devices;
    }
    
    /**
     * Zählt alle Geräte
     */
    public int getDeviceCount() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM Endgeraet";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        return 0;
    }
    
    /**
     * Zählt Geräte nach Status
     */
    public int getDeviceCountByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM Endgeraet WHERE status = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        }
        return 0;
    }
    
    // ==================== HILFSMETHODEN ====================
    
    /**
     * Mappt ein ResultSet zu einem Device-Objekt
     */
    private Device mapResultSetToDevice(ResultSet rs) throws SQLException {
        Device device = new Device();
        device.setId(rs.getInt("id"));
        device.setManufacturer(rs.getString("hersteller"));
        device.setModel(rs.getString("modell"));
        device.setOs(rs.getString("betriebssystem"));
        device.setOsVersion(rs.getString("osVersion"));
        device.setImei(rs.getString("imei"));
        device.setStatus(rs.getString("status"));
        
        // Optionale Felder, falls vorhanden
        try {
            device.setCreatedAt(rs.getTimestamp("erstellt_am"));
        } catch (SQLException e) {
            // Feld existiert nicht, ignoriere
        }
        
        try {
            device.setUpdatedAt(rs.getTimestamp("aktualisiert_am"));
        } catch (SQLException e) {
            // Feld existiert nicht, ignoriere
        }
        
        return device;
    }
    
    /**
     * Prüft, ob eine bestimmte Stored Procedure existiert
     */
    public boolean storedProcedureExists(String procedureName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.ROUTINES " +
                    "WHERE ROUTINE_TYPE = 'PROCEDURE' AND ROUTINE_NAME = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, procedureName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    /**
     * Prüft, ob eine bestimmte View existiert
     */
    public boolean viewExists(String viewName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.VIEWS WHERE TABLE_NAME = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, viewName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    /**
     * Führt ein einfaches Test-Query aus, um die Verbindung zu testen
     */
    public boolean testConnection() throws SQLException {
        String sql = "SELECT 1 as test";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() && rs.getInt("test") == 1;
        }
    }
    
    /**
     * Holt alle Richtlinien für Compliance-Checks
     */
    public ResultSet getAllPolicies() throws SQLException {
        String sql = "SELECT id, name, beschreibung FROM Richtlinie ORDER BY id";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }
    
    /**
     * Holt Details zu Compliance-Prüfungen für eine bestimmte Richtlinie
     */
    public ResultSet getComplianceDetails(int policyId, int limit) throws SQLException {
        String sql = "SELECT TOP (?) cp.*, e.hersteller + ' ' + e.modell as Gerät, r.name as Richtlinie " +
                    "FROM CompliancePruefung cp " +
                    "JOIN Endgeraet e ON cp.endgeraetId = e.id " +
                    "JOIN Richtlinie r ON cp.policyId = r.id " +
                    "WHERE cp.policyId = ? " +
                    "ORDER BY cp.geprueftAm DESC";
        
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, limit);
        pstmt.setInt(2, policyId);
        return pstmt.executeQuery();
    }
    
    /**
     * Zählt Compliance-Prüfungen für eine bestimmte Richtlinie
     */
    public int getComplianceCheckCount(int policyId) throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM CompliancePruefung WHERE policyId = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, policyId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }
    
    public boolean isImeiExists(String imei) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM Endgeraet WHERE imei = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, imei);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        }
        return false;
    }
    
    public boolean employeeExists(int employeeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Mitarbeiter WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, employeeId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}