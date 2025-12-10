// ComplianceCheck.java
package de.emm.demo.model;

import java.util.Date;

public class ComplianceCheck {
    private String policyName;
    private int deviceCount;
    private int compliantCount;
    private int nonCompliantCount;
    private double complianceRate;
    private Date checkTimestamp;
    
    // Getter und Setter
    public String getPolicyName() { return policyName; }
    public void setPolicyName(String policyName) { this.policyName = policyName; }
    
    public int getDeviceCount() { return deviceCount; }
    public void setDeviceCount(int deviceCount) { this.deviceCount = deviceCount; }
    
    public int getCompliantCount() { return compliantCount; }
    public void setCompliantCount(int compliantCount) { this.compliantCount = compliantCount; }
    
    public int getNonCompliantCount() { return nonCompliantCount; }
    public void setNonCompliantCount(int nonCompliantCount) { this.nonCompliantCount = nonCompliantCount; }
    
    public double getComplianceRate() { return complianceRate; }
    public void setComplianceRate(double complianceRate) { this.complianceRate = complianceRate; }
    
    public Date getCheckTimestamp() { return checkTimestamp; }
    public void setCheckTimestamp(Date checkTimestamp) { this.checkTimestamp = checkTimestamp; }
}