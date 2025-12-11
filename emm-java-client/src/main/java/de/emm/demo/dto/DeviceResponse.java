// DeviceResponse.java
package de.emm.demo.dto;


import de.emm.demo.model.Device;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DeviceResponse {
    private int id;
    private String manufacturer;
    private String model;
    private String os;
    private String osVersion;
    private String imei;
    private String status;
    private LocalDateTime createdAt;
    private String formattedCreatedAt;
    
    // Factory-Methode
    public static DeviceResponse fromEntity(Device device) {
        DeviceResponse response = new DeviceResponse();
        response.setId(device.getId());
        response.setManufacturer(device.getManufacturer());
        response.setModel(device.getModel());
        response.setOs(device.getOs());
        response.setOsVersion(device.getOsVersion());
        response.setImei(device.getImei());
        response.setStatus(device.getStatus());
        
        if (device.getCreatedAt() != null) {
            // Konvertierung von Date zu LocalDateTime
            Date createdAt = device.getCreatedAt();
            LocalDateTime localDateTime = createdAt.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            
            response.setCreatedAt(localDateTime);
            response.setFormattedCreatedAt(
                localDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
            );
        }
        
        return response;
    }
    
    // Getter und Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
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
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public String getFormattedCreatedAt() { return formattedCreatedAt; }
    public void setFormattedCreatedAt(String formattedCreatedAt) { 
        this.formattedCreatedAt = formattedCreatedAt; 
    }
    
    public String getFullName() {
        return manufacturer + " " + model;
    }
    
    @Override
    public String toString() {
        return String.format("DeviceResponse[id=%d, %s %s, Status=%s]", 
                           id, manufacturer, model, status);
    }
}