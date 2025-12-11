// AuditService.java
package de.emm.demo.service;

import de.emm.demo.model.Device;
import de.emm.demo.repository.AuditRepository;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class AuditService {
    
    private final AuditRepository auditRepository;
    
    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }
    
    public void logDeviceCreated(Device device) {
        try {
            String action = String.format("Gerät erstellt: %s %s (IMEI: %s)", 
                device.getManufacturer(), device.getModel(), device.getImei());
            logAction("Endgeraet", "CREATE", null, device.toString(), action);
        } catch (SQLException e) {
            System.err.println("Fehler beim Audit-Log: " + e.getMessage());
            // Keine Exception werfen - Audit-Log sollte Hauptoperation nicht blockieren
        }
    }
    
    public void logDeviceStatusChanged(int deviceId, String oldStatus, String newStatus) {
        try {
            String action = String.format("Status geändert: %s -> %s", oldStatus, newStatus);
            logAction("Endgeraet", "STATUS_CHANGE", oldStatus, newStatus, action);
        } catch (SQLException e) {
            System.err.println("Fehler beim Audit-Log: " + e.getMessage());
        }
    }
    
    public void logDeviceAssigned(int deviceId, int employeeId, String issuedBy) {
        try {
            String action = String.format("Gerät an Mitarbeiter %d ausgegeben durch %s", 
                employeeId, issuedBy);
            logAction("Endgeraet", "ASSIGN", null, String.valueOf(employeeId), action);
        } catch (SQLException e) {
            System.err.println("Fehler beim Audit-Log: " + e.getMessage());
        }
    }
    
    public void logDeviceReturned(int deviceId) {
        try {
            logAction("Endgeraet", "RETURN", "AKTIV", "LAGER", "Gerät zurückgenommen");
        } catch (SQLException e) {
            System.err.println("Fehler beim Audit-Log: " + e.getMessage());
        }
    }
    
    public void logDeviceRemoved(int deviceId, String reason) {
        try {
            String action = reason != null ? 
                String.format("Gerät entfernt: %s", reason) : "Gerät entfernt";
            logAction("Endgeraet", "REMOVE", null, null, action);
        } catch (SQLException e) {
            System.err.println("Fehler beim Audit-Log: " + e.getMessage());
        }
    }
    
    private void logAction(String table, String action, String oldValue, 
                          String newValue, String description) throws SQLException {
        // Hier würdest du direkt in die AuditLog-Tabelle schreiben
        // Für jetzt nur Konsolenausgabe
        System.out.printf("[AUDIT] %s - %s.%s: %s -> %s | %s%n",
            LocalDateTime.now(), table, action, 
            oldValue != null ? oldValue : "NULL",
            newValue != null ? newValue : "NULL",
            description);
    }
}