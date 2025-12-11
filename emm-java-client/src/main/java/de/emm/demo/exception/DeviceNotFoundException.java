// DeviceNotFoundException.java
package de.emm.demo.exception;

public class DeviceNotFoundException extends EMMException {
    private static final long serialVersionUID = 1L;  // serialVersionUID hinzugefügt
    
    private final int deviceId;
    
    public DeviceNotFoundException(int deviceId) {
        super(ErrorCode.DEVICE_NOT_FOUND, 
              String.format("Gerät mit ID %d nicht gefunden", deviceId));
        this.deviceId = deviceId;
    }
    
    public int getDeviceId() {
        return deviceId;
    }
}