// DeviceDTO.java
package de.emm.demo.dto;

import de.emm.demo.model.Device;

public class DeviceDTO {
    private String manufacturer;
    private String model;
    private String os;
    private String osVersion;
    private String imei;
    private String status;
    
    // Standardkonstruktor
    public DeviceDTO() {}
    
    // Konstruktor für einfache Erstellung
    public DeviceDTO(String manufacturer, String model, String os, 
                    String osVersion, String imei, String status) {
        this.manufacturer = manufacturer;
        this.model = model;
        this.os = os;
        this.osVersion = osVersion;
        this.imei = imei;
        this.status = status;
    }
    
    // Factory-Methode aus Entity
    public static DeviceDTO fromEntity(Device device) {
        DeviceDTO dto = new DeviceDTO();
        dto.setManufacturer(device.getManufacturer());
        dto.setModel(device.getModel());
        dto.setOs(device.getOs());
        dto.setOsVersion(device.getOsVersion());
        dto.setImei(device.getImei());
        dto.setStatus(device.getStatus());
        return dto;
    }
    
    // Getter und Setter
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    
    public String getOs() { return os; }
    public void setOs(String os) { this.os = os; }
    
    public String getOsVersion() { return osVersion; }
    public void setOsVersion(String osVersion) { this.osVersion = osVersion; }
    
    public String getImei() { return imei; }
    public void setImei(String imei) { this.imei = imei; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    @Override
    public String toString() {
        return String.format("%s %s [%s]", manufacturer, model, imei);
    }
}