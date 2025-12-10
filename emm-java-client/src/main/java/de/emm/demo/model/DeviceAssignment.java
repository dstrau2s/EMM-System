// DeviceAssignment.java
package de.emm.demo.model;

import java.util.Date;

public class DeviceAssignment {
    private String deviceName;
    private String status;
    private String osVersion;
    private Date assignmentDate;
    private int daysInUse;
    
    // Getter und Setter
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getOsVersion() { return osVersion; }
    public void setOsVersion(String osVersion) { this.osVersion = osVersion; }
    
    public Date getAssignmentDate() { return assignmentDate; }
    public void setAssignmentDate(Date assignmentDate) { this.assignmentDate = assignmentDate; }
    
    public int getDaysInUse() { return daysInUse; }
    public void setDaysInUse(int daysInUse) { this.daysInUse = daysInUse; }
}