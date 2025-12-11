// CreateDeviceRequest.java
package de.emm.demo.dto;

public class CreateDeviceRequest {
    private String manufacturer;
    private String model;
    private String os;
    private String osVersion;
    private String imei;
    private String status = "LAGER"; // Default-Wert
    
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
}