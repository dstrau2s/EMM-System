// AuditLogEntry.java (erweitert mit Geräte-Referenz)
package de.emm.demo.model;

import java.util.Date;

public class AuditLogEntry {
    private int id;
    private String table;
    private String action;
    private String oldValue;
    private String newValue;
    private Date timestamp;
    private String user;
    private Integer deviceId;  // Neue Spalte
    private Device device;     // Optional: Vollständiges Geräte-Objekt
    
    // Konstruktoren
    public AuditLogEntry() {}
    
    public AuditLogEntry(String table, String action, String oldValue, 
                        String newValue, String user, Integer deviceId) {
        this.table = table;
        this.action = action;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.user = user;
        this.deviceId = deviceId;
        this.timestamp = new Date();
    }
    
    // Getter und Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getTable() { return table; }
    public void setTable(String table) { this.table = table; }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    
    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }
    
    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }
    
    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    
    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }
    
    public Integer getDeviceId() { return deviceId; }
    public void setDeviceId(Integer deviceId) { this.deviceId = deviceId; }
    
    public Device getDevice() { return device; }
    public void setDevice(Device device) { this.device = device; }
    
    @Override
    public String toString() {
        String deviceInfo = (deviceId != null) ? " GerätID:" + deviceId : "";
        if (device != null) {
            deviceInfo = " Gerät:" + device.getManufacturer() + " " + device.getModel();
        }
        
        return String.format("AuditLog[id=%d, %s.%s, %s -> %s, %s%s]", 
            id, table, action, 
            truncate(oldValue, 20), truncate(newValue, 20), 
            timestamp, deviceInfo);
    }
    
    private String truncate(String value, int length) {
        if (value == null) return "null";
        if (value.length() <= length) return value;
        return value.substring(0, length - 3) + "...";
    }
    
    // Hilfsmethoden
    public boolean isStatusChange() {
        return "Statusänderung".equals(action) && "Endgeraet".equals(table);
    }
    
    public boolean isDeviceRelated() {
        return deviceId != null || "Endgeraet".equals(table);
    }
    
    public boolean hasDeviceInfo() {
        return deviceId != null;
    }
    
    public String getShortSummary() {
        String deviceRef = (deviceId != null) ? " (GerätID:" + deviceId + ")" : "";
        return String.format("%s.%s%s by %s at %tH:%tM", 
            table, action, deviceRef, user, timestamp, timestamp);
    }
}