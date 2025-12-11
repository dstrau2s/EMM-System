// DeviceService.java
package de.emm.demo.service;

import de.emm.demo.dto.CreateDeviceRequest;
import de.emm.demo.dto.DeviceResponse;
import de.emm.demo.exception.DeviceNotFoundException;
import de.emm.demo.exception.DeviceNotAvailableException;
import de.emm.demo.exception.DuplicateImeiException;
import de.emm.demo.exception.EMMException;
import de.emm.demo.model.Device;
import de.emm.demo.repository.DeviceRepository;
import de.emm.demo.validation.DeviceValidator;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class DeviceService {
    
    private final DeviceRepository deviceRepository;
    private final AuditService auditService;
    
    public DeviceService(DeviceRepository deviceRepository, AuditService auditService) {
        this.deviceRepository = deviceRepository;
        this.auditService = auditService;
    }
    
    /**
     * Erstellt ein neues Gerät
     */
    public DeviceResponse createDevice(CreateDeviceRequest request) {
        // 1. Validierung
        DeviceValidator.validateCreateRequest(request);
        
        // 2. Business-Logik: Prüfe ob IMEI bereits existiert
        try {
            if (deviceRepository.isImeiExists(request.getImei())) {
                throw new DuplicateImeiException(request.getImei());
            }
        } catch (SQLException e) {
            throw new EMMException(EMMException.ErrorCode.DATABASE_ERROR, 
                                 "Fehler beim Prüfen der IMEI: " + e.getMessage(), e);
        }
        
        // 3. Gerät erstellen
        Device device;
        try {
            device = deviceRepository.createDevice(
                request.getManufacturer(),
                request.getModel(),
                request.getOs(),
                request.getOsVersion(),
                request.getImei(),
                request.getStatus()
            );
        } catch (SQLException e) {
            throw new EMMException(EMMException.ErrorCode.DATABASE_ERROR, 
                                 "Fehler beim Erstellen des Geräts: " + e.getMessage(), e);
        }
        
        if (device == null) {
            throw new EMMException(EMMException.ErrorCode.DATABASE_ERROR, 
                                 "Gerät konnte nicht erstellt werden (keine ID zurückgegeben)");
        }
        
        // 4. Audit-Log erstellen
        auditService.logDeviceCreated(device);
        
        // 5. Response erstellen
        return DeviceResponse.fromEntity(device);
    }
    /**
     * Holt ein Gerät anhand der ID
     */
    public DeviceResponse getDeviceById(int deviceId) {
        Device device;
        try {
            device = deviceRepository.getDeviceById(deviceId);
        } catch (SQLException e) {
            throw new EMMException(EMMException.ErrorCode.DATABASE_ERROR, 
                                 "Fehler beim Abrufen des Geräts", e);
        }
        
        if (device == null) {
            throw new DeviceNotFoundException(deviceId);
        }
        
        return DeviceResponse.fromEntity(device);
    }
    
    /**
     * Aktualisiert den Status eines Geräts
     */
    public DeviceResponse updateDeviceStatus(int deviceId, String newStatus) {
        // 1. Gerät existiert?
        Device device;
        try {
            device = deviceRepository.getDeviceById(deviceId);
            if (device == null) {
                throw new DeviceNotFoundException(deviceId);
            }
        } catch (SQLException e) {
            throw new EMMException(EMMException.ErrorCode.DATABASE_ERROR, 
                                 "Fehler beim Abrufen des Geräts", e);
        }
        
        // 2. Statusübergang validieren
        String currentStatus = device.getStatus();
        if (!DeviceValidator.isValidStatusTransition(currentStatus, newStatus)) {
            throw new EMMException(EMMException.ErrorCode.INVALID_STATUS_TRANSITION,
                String.format("Ungültiger Statusübergang: %s -> %s", currentStatus, newStatus));
        }
        
        // 3. Status aktualisieren
        boolean updated;
        try {
            updated = deviceRepository.updateDeviceStatus(deviceId, newStatus);
        } catch (SQLException e) {
            throw new EMMException(EMMException.ErrorCode.DATABASE_ERROR, 
                                 "Fehler beim Aktualisieren des Status", e);
        }
        
        if (!updated) {
            throw new EMMException(EMMException.ErrorCode.DATABASE_ERROR, 
                                 "Status konnte nicht aktualisiert werden");
        }
        
        // 4. Audit-Log
        auditService.logDeviceStatusChanged(deviceId, currentStatus, newStatus);
        
        // 5. Aktualisiertes Gerät zurückgeben
        device.setStatus(newStatus);
        return DeviceResponse.fromEntity(device);
    }
    
	    /**
	     * Weist ein Gerät einem Mitarbeiter zu
	     */
	    public String assignDeviceToEmployee(int deviceId, int employeeId, String issuedBy) {
	        // 1. Prüfe ob Gerät existiert und verfügbar ist
	        Device device;
	        try {
	            device = deviceRepository.getDeviceById(deviceId);
	            if (device == null) {
	                throw new DeviceNotFoundException(deviceId);
	            }
	            
	            if (!"LAGER".equals(device.getStatus())) {
	                throw new DeviceNotAvailableException(deviceId, device.getStatus());
	            }
	        } catch (SQLException e) {
	            throw new EMMException(EMMException.ErrorCode.DATABASE_ERROR, 
	                                 "Fehler beim Prüfen des Geräts: " + e.getMessage(), e);
	        }
	        
	        // 2. Prüfe ob Mitarbeiter existiert (vereinfacht)
	        try {
	            if (!employeeExists(employeeId)) {
	                throw new EMMException(EMMException.ErrorCode.EMPLOYEE_NOT_FOUND,
	                    String.format("Mitarbeiter mit ID %d nicht gefunden", employeeId));
	            }
	        } catch (SQLException e) {
	            throw new EMMException(EMMException.ErrorCode.DATABASE_ERROR,
	                                 "Fehler beim Prüfen des Mitarbeiters", e);
	        }
	        
	        // 3. Gerät ausgeben (Stored Procedure)
	        String result;
	        try {
	            result = deviceRepository.assignDeviceToEmployee(deviceId, employeeId, issuedBy);
	            
	            if (result == null || result.isEmpty()) {
	                throw new EMMException(EMMException.ErrorCode.DATABASE_ERROR,
	                                     "Keine Rückmeldung von der Stored Procedure");
	            }
	        } catch (SQLException e) {
	            throw new EMMException(EMMException.ErrorCode.DATABASE_ERROR, 
	                                 "Fehler beim Ausgeben des Geräts: " + e.getMessage(), e);
	        }
	        
	        // 4. Audit-Log
	        auditService.logDeviceAssigned(deviceId, employeeId, issuedBy);
	        
	        return result;
	    }
 	    
    
    /**
     * Nimmt ein Gerät zurück
     */
    public String returnDevice(int deviceId) {
        // 1. Prüfe ob Gerät ausgegeben ist
        Device device;
        try {
            device = deviceRepository.getDeviceById(deviceId);
            if (device == null) {
                throw new DeviceNotFoundException(deviceId);
            }
            
            if (!"AKTIV".equals(device.getStatus())) {
                throw new EMMException(EMMException.ErrorCode.INVALID_DEVICE_STATUS,
                    String.format("Gerät %d ist nicht ausgegeben (Status: %s)", 
                                deviceId, device.getStatus()));
            }
        } catch (SQLException e) {
            throw new EMMException(EMMException.ErrorCode.DATABASE_ERROR, 
                                 "Fehler beim Prüfen des Geräts", e);
        }
        
        // 2. Gerät zurücknehmen (Stored Procedure)
        String result;
        try {
            result = deviceRepository.returnDevice(deviceId);
        } catch (SQLException e) {
            throw new EMMException(EMMException.ErrorCode.DATABASE_ERROR, 
                                 "Fehler beim Zurücknehmen des Geräts", e);
        }
        
        // 3. Audit-Log
        auditService.logDeviceReturned(deviceId);
        
        return result;
    }
    
    /**
     * Entfernt ein Gerät (Soft-Delete)
     */
    public String removeDevice(int deviceId, String reason) {
        // 1. Prüfe ob Gerät existiert
        try {
            if (!deviceRepository.deviceExists(deviceId)) {
                throw new DeviceNotFoundException(deviceId);
            }
        } catch (SQLException e) {
            throw new EMMException(EMMException.ErrorCode.DATABASE_ERROR, 
                                 "Fehler beim Prüfen des Geräts", e);
        }
        
        // 2. Gerät entfernen (Stored Procedure)
        String result;
        try {
            result = deviceRepository.removeDevice(deviceId, reason);
        } catch (SQLException e) {
            throw new EMMException(EMMException.ErrorCode.DATABASE_ERROR, 
                                 "Fehler beim Entfernen des Geräts", e);
        }
        
        // 3. Audit-Log
        auditService.logDeviceRemoved(deviceId, reason);
        
        return result;
    }
    
    /**
     * Holt verfügbare Geräte
     */
    public List<DeviceResponse> getAvailableDevices(String filter) {
        List<Device> devices;
        try {
            devices = deviceRepository.getAvailableDevices(filter);
        } catch (SQLException e) {
            throw new EMMException(EMMException.ErrorCode.DATABASE_ERROR, 
                                 "Fehler beim Abrufen verfügbarer Geräte", e);
        }
        
        return devices.stream()
            .map(DeviceResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * Prüft ob IMEI bereits existiert
     */
    private boolean isImeiExists(String imei) throws SQLException {
        // Diese Methode fehlt noch im DeviceRepository
        // Müsste ergänzt werden:
        // String sql = "SELECT COUNT(*) FROM Endgeraet WHERE imei = ?";
        // ...
        // Für jetzt: immer false zurückgeben (Demo)
        return false;
    }
    
    /**
     * Hilfsmethode für das UI: Status-Überprüfung
     */
    public boolean isDeviceAvailable(int deviceId) {
        try {
            String status = deviceRepository.getDeviceStatus(deviceId);
            return status != null && "LAGER".equals(status);
        } catch (SQLException e) {
            throw new EMMException(EMMException.ErrorCode.DATABASE_ERROR, 
                                 "Fehler beim Prüfen des Gerätestatus", e);
        }
    }
    
    /**
     * Zählt alle Geräte
     */
    public int getDeviceCount() {
        try {
            return deviceRepository.getDeviceCount();
        } catch (SQLException e) {
            throw new EMMException(EMMException.ErrorCode.DATABASE_ERROR, 
                                 "Fehler beim Zählen der Geräte", e);
        }
    }
    
    /**
     * Zählt Geräte nach Status
     */
    public int getDeviceCountByStatus(String status) {
        try {
            return deviceRepository.getDeviceCountByStatus(status);
        } catch (SQLException e) {
            throw new EMMException(EMMException.ErrorCode.DATABASE_ERROR, 
                                 "Fehler beim Zählen der Geräte", e);
        }
    }
    
 // Hilfsmethode für Mitarbeiter-Prüfung
    private boolean employeeExists(int employeeId) throws SQLException {
        // Diese Methode in DeviceRepository ergänzen
        return deviceRepository.employeeExists(employeeId);
    }
}