// Device.java (erweitert)
package de.emm.demo.model;

import java.util.Date;

public class Device {
    private int id;
    private String manufacturer;
    private String model;
    private String os;
    private String osVersion;
    private String imei;
    private String status;
    private Date createdAt;
    private Date updatedAt;
    
    // Konstruktoren
    public Device() {}
    
    public Device(int id, String manufacturer, String model, String os, 
                 String osVersion, String imei, String status) {
        this.id = id;
        this.manufacturer = manufacturer;
        this.model = model;
        this.os = os;
        this.osVersion = osVersion;
        this.imei = imei;
        this.status = status;
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
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    
    @Override
    public String toString() {
        return String.format("Device[id=%d, %s %s, OS=%s %s, Status=%s]", 
            id, manufacturer, model, os, osVersion, status);
    }
    
    // Hilfsmethoden
    public String getFullName() {
        return manufacturer + " " + model;
    }
    
    public boolean isAvailable() {
        return "LAGER".equals(status);
    }
    
    public boolean isActive() {
        return "AKTIV".equals(status);
    }
    
    public boolean isDefective() {
        return "DEFEKT".equals(status);
    }
    
    public boolean isRetired() {
        return "AUSGESCHIEDEN".equals(status);
    }
}