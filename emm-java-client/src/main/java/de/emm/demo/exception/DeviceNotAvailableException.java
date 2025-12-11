// DeviceNotAvailableException.java
package de.emm.demo.exception;

public class DeviceNotAvailableException extends EMMException {
    private static final long serialVersionUID = 1L;  // Hinzufügen der serialVersionUID
    
    private final int deviceId;
    private final String currentStatus;
    
    public DeviceNotAvailableException(int deviceId, String currentStatus) {
        super(ErrorCode.DEVICE_NOT_AVAILABLE, 
              String.format("Gerät %d ist nicht verfügbar. Aktueller Status: %s", 
                          deviceId, currentStatus));
        this.deviceId = deviceId;
        this.currentStatus = currentStatus;
    }
    
    public int getDeviceId() {
        return deviceId;
    }
    
    public String getCurrentStatus() {
        return currentStatus;
    }
}