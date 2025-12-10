// AuditRepository.java (korrigierte Vollversion)
package de.emm.demo.repository;

import de.emm.demo.model.AuditLogEntry;
import de.emm.demo.model.AuditStatistic;
//import de.emm.demo.model.AuditTableInfo;
import de.emm.demo.model.Device;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AuditRepository {
    
    private final Connection connection;
    private DeviceRepository deviceRepository;
    
    public AuditRepository(Connection connection) {
        this.connection = connection;
    }
    
    public AuditRepository(Connection connection, DeviceRepository deviceRepository) {
        this.connection = connection;
        this.deviceRepository = deviceRepository;
    }
    
    // ==================== GRUNDLEGENDE CRUD-OPERATIONEN ====================
    
    /**
     * Erstellt einen neuen AuditLog-Eintrag
     */
    public boolean createAuditLogEntry(String table, String action, 
                                      String oldValue, String newValue, 
                                      String user, Integer deviceId) throws SQLException {
        String sql = "INSERT INTO AuditLog (tabelle, aktion, alt, neu, benutzer, zeitpunkt, endgeraet_id) " +
                    "VALUES (?, ?, ?, ?, ?, GETDATE(), ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, table);
            pstmt.setString(2, action);
            pstmt.setString(3, oldValue);
            pstmt.setString(4, newValue);
            pstmt.setString(5, user);
            if (deviceId != null) {
                pstmt.setInt(6, deviceId);
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Erstellt einen AuditLog-Eintrag ohne Geräte-ID (für Abwärtskompatibilität)
     */
    public boolean createAuditLogEntry(String table, String action, 
                                      String oldValue, String newValue, 
                                      String user) throws SQLException {
        return createAuditLogEntry(table, action, oldValue, newValue, user, null);
    }
    
    /**
     * Holt einen AuditLog-Eintrag anhand der ID
     */
    public AuditLogEntry getAuditLogById(int id) throws SQLException {
        String sql = "SELECT * FROM AuditLog WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAuditLogEntry(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Holt alle AuditLog-Einträge
     */
    public List<AuditLogEntry> getAllAuditLogs() throws SQLException {
        return getAuditLogsByFilter(null, null, null, null, null, null, "zeitpunkt DESC");
    }
    
    /**
     * Holt alle AuditLog-Einträge mit Geräte-Informationen
     */
    public List<AuditLogEntry> getAllAuditLogsWithDevices() throws SQLException {
        List<AuditLogEntry> entries = getAllAuditLogs();
        enrichWithDeviceInfo(entries);
        return entries;
    }
    
    /**
     * Löscht einen AuditLog-Eintrag
     */
    public boolean deleteAuditLogEntry(int id) throws SQLException {
        String sql = "DELETE FROM AuditLog WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    // ==================== FILTER-OPERATIONEN ====================
    
    /**
     * Holt AuditLog-Einträge mit verschiedenen Filtern
     */
    public List<AuditLogEntry> getAuditLogsByFilter(String table, String action, 
                                                   Date startDate, Date endDate,
                                                   String user, Integer deviceId,
                                                   String orderBy) throws SQLException {
        List<AuditLogEntry> entries = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM AuditLog WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (table != null && !table.isEmpty()) {
            sql.append(" AND tabelle = ?");
            params.add(table);
        }
        
        if (action != null && !action.isEmpty()) {
            sql.append(" AND aktion = ?");
            params.add(action);
        }
        
        if (startDate != null) {
            sql.append(" AND zeitpunkt >= ?");
            params.add(new Timestamp(startDate.getTime()));
        }
        
        if (endDate != null) {
            sql.append(" AND zeitpunkt <= ?");
            params.add(new Timestamp(endDate.getTime()));
        }
        
        if (user != null && !user.isEmpty()) {
            sql.append(" AND benutzer LIKE ?");
            params.add("%" + user + "%");
        }
        
        if (deviceId != null) {
            sql.append(" AND endgeraet_id = ?");
            params.add(deviceId);
        }
        
        if (orderBy != null && !orderBy.isEmpty()) {
            sql.append(" ORDER BY ").append(orderBy);
        } else {
            sql.append(" ORDER BY zeitpunkt DESC");
        }
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    entries.add(mapResultSetToAuditLogEntry(rs));
                }
            }
        }
        
        enrichWithDeviceInfo(entries);
        return entries;
    }
    
    /**
     * Überladene Version ohne deviceId Parameter
     */
    public List<AuditLogEntry> getAuditLogsByFilter(String table, String action, 
                                                   Date startDate, Date endDate,
                                                   String user, String orderBy) throws SQLException {
        return getAuditLogsByFilter(table, action, startDate, endDate, user, null, orderBy);
    }
    
    /**
     * Holt die neuesten AuditLog-Einträge (begrenzt)
     */
    public List<AuditLogEntry> getRecentAuditLogs(int limit) throws SQLException {
        String sql = "SELECT TOP (?) * FROM AuditLog ORDER BY zeitpunkt DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            
            List<AuditLogEntry> entries = new ArrayList<>();
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    entries.add(mapResultSetToAuditLogEntry(rs));
                }
            }
            
            enrichWithDeviceInfo(entries);
            return entries;
        }
    }
    
    /**
     * Holt AuditLog-Einträge der letzten 24 Stunden
     */
    public List<AuditLogEntry> getAuditLogsLast24Hours() throws SQLException {
        String sql = "SELECT * FROM AuditLog WHERE zeitpunkt >= DATEADD(HOUR, -24, GETDATE()) " +
                    "ORDER BY zeitpunkt DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            List<AuditLogEntry> entries = new ArrayList<>();
            while (rs.next()) {
                entries.add(mapResultSetToAuditLogEntry(rs));
            }
            
            enrichWithDeviceInfo(entries);
            return entries;
        }
    }
    
    /**
     * Holt nur Statusänderungen von Endgeräten
     */
    public List<AuditLogEntry> getDeviceStatusChanges() throws SQLException {
        return getAuditLogsByFilter("Endgeraet", "Statusänderung", null, null, null, null, "zeitpunkt DESC");
    }
    
    /**
     * Holt AuditLog-Einträge für eine bestimmte Tabelle
     */
    public List<AuditLogEntry> getAuditLogsForTable(String tableName) throws SQLException {
        return getAuditLogsByFilter(tableName, null, null, null, null, null, "zeitpunkt DESC");
    }
    
    /**
     * Holt AuditLog-Einträge für einen bestimmten Benutzer
     */
    public List<AuditLogEntry> getAuditLogsForUser(String userName) throws SQLException {
        return getAuditLogsByFilter(null, null, null, null, userName, null, "zeitpunkt DESC");
    }
    
    /**
     * Holt AuditLog-Einträge für ein bestimmtes Gerät
     */
    public List<AuditLogEntry> getAuditLogsForDevice(int deviceId) throws SQLException {
        return getAuditLogsByFilter(null, null, null, null, null, deviceId, "zeitpunkt DESC");
    }
    
    // ==================== STATISTIK-OPERATIONEN ====================
    
    /**
     * Zählt alle AuditLog-Einträge
     */
    public int getAuditLogCount() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM AuditLog";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        return 0;
    }
    
    /**
     * Holt umfassende AuditLog-Statistiken
     */
    public AuditStatistic getAuditStatistics() throws SQLException {
        AuditStatistic stats = new AuditStatistic();
        
        String sql = 
            "SELECT " +
            "    COUNT(*) as TotalEntries, " +
            "    COUNT(DISTINCT tabelle) as TablesTracked, " +
            "    MIN(zeitpunkt) as FirstEntry, " +
            "    MAX(zeitpunkt) as LastEntry, " +
            "    COUNT(DISTINCT benutzer) as UsersTracked, " +
            "    (SELECT COUNT(*) FROM AuditLog WHERE tabelle = 'Endgeraet' AND aktion = 'Statusänderung') as StatusChanges " +
            "FROM AuditLog";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                stats.setTotalEntries(rs.getInt("TotalEntries"));
                stats.setTablesTracked(rs.getInt("TablesTracked"));
                stats.setFirstEntry(rs.getTimestamp("FirstEntry"));
                stats.setLastEntry(rs.getTimestamp("LastEntry"));
                stats.setUsersTracked(rs.getInt("UsersTracked"));
                stats.setStatusChanges(rs.getInt("StatusChanges"));
            }
        }
        
        // Top 5 Aktionen
        stats.setTopActions(getTopActions(5));
        
        // Verteilung nach Tabellen
        stats.setTableDistribution(getTableDistribution());
        
        return stats;
    }
    
    /**
     * Holt die Top-N Aktionen
     */
    public List<AuditStatistic.ActionCount> getTopActions(int limit) throws SQLException {
        List<AuditStatistic.ActionCount> actions = new ArrayList<>();
        String sql = "SELECT TOP (?) aktion, COUNT(*) as count " +
                    "FROM AuditLog " +
                    "GROUP BY aktion " +
                    "ORDER BY count DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    AuditStatistic.ActionCount action = new AuditStatistic.ActionCount();
                    action.setAction(rs.getString("aktion"));
                    action.setCount(rs.getInt("count"));
                    actions.add(action);
                }
            }
        }
        return actions;
    }
    
    /**
     * Holt die Verteilung nach Tabellen
     */
    public List<AuditStatistic.TableCount> getTableDistribution() throws SQLException {
        List<AuditStatistic.TableCount> tables = new ArrayList<>();
        String sql = "SELECT tabelle, COUNT(*) as count " +
                    "FROM AuditLog " +
                    "GROUP BY tabelle " +
                    "ORDER BY count DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                AuditStatistic.TableCount table = new AuditStatistic.TableCount();
                table.setTableName(rs.getString("tabelle"));
                table.setCount(rs.getInt("count"));
                tables.add(table);
            }
        }
        return tables;
    }
    
    // ==================== HILFSMETHODEN ====================
    
    /**
     * Mappt ein ResultSet zu einem AuditLogEntry-Objekt
     */
    private AuditLogEntry mapResultSetToAuditLogEntry(ResultSet rs) throws SQLException {
        AuditLogEntry entry = new AuditLogEntry();
        entry.setId(rs.getInt("id"));
        entry.setTable(rs.getString("tabelle"));
        entry.setAction(rs.getString("aktion"));
        entry.setOldValue(rs.getString("alt"));
        entry.setNewValue(rs.getString("neu"));
        entry.setTimestamp(rs.getTimestamp("zeitpunkt"));
        entry.setUser(rs.getString("benutzer"));
        
        // Neue Spalte
        int deviceId = rs.getInt("endgeraet_id");
        if (!rs.wasNull()) {
            entry.setDeviceId(deviceId);
        }
        
        return entry;
    }
    
    /**
     * Ergänzt AuditLog-Einträge mit Geräte-Informationen
     */
    private void enrichWithDeviceInfo(List<AuditLogEntry> entries) throws SQLException {
        if (deviceRepository == null) {
            deviceRepository = new DeviceRepository(connection);
        }
        
        for (AuditLogEntry entry : entries) {
            if (entry.getDeviceId() != null && entry.getDevice() == null) {
                try {
                    Device device = deviceRepository.getDeviceById(entry.getDeviceId());
                    entry.setDevice(device);
                } catch (SQLException e) {
                    // Gerät konnte nicht geladen werden, Eintrag bleibt ohne Geräteinfo
                }
            }
        }
    }
    
    // ==================== TABELLEN-ÜBERWACHUNGS-OPERATIONEN ====================
    
    /**
     * Prüft, ob die AuditLog-Tabelle existiert
     */
    public boolean auditLogTableExists() throws SQLException {
        String sql = "SELECT COUNT(*) as exists_flag FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'AuditLog'";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("exists_flag") > 0;
            }
        }
        return false;
    }
    
    /**
     * Prüft alle Trigger in der Datenbank
     */
    public List<String> getAllTriggers() throws SQLException {
        List<String> triggers = new ArrayList<>();
        String sql = "SELECT name FROM sys.triggers ORDER BY name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                triggers.add(rs.getString("name"));
            }
        }
        return triggers;
    }
    
    /**
     * Prüft, ob ein bestimmter Trigger existiert
     */
    public boolean triggerExists(String triggerName) throws SQLException {
        String sql = "SELECT COUNT(*) as exists_flag FROM sys.triggers WHERE name = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, triggerName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("exists_flag") > 0;
                }
            }
        }
        return false;
    }
}