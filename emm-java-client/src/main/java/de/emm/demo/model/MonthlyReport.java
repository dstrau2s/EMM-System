// MonthlyReport.java
package de.emm.demo.model;

public class MonthlyReport {
    private String department;
    private int deviceCount;
    private double totalCost;
    private double avgDataVolume;
    private double budget;
    private double budgetRemaining;
    
    // Getter und Setter
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public int getDeviceCount() { return deviceCount; }
    public void setDeviceCount(int deviceCount) { this.deviceCount = deviceCount; }
    
    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }
    
    public double getAvgDataVolume() { return avgDataVolume; }
    public void setAvgDataVolume(double avgDataVolume) { this.avgDataVolume = avgDataVolume; }
    
    public double getBudget() { return budget; }
    public void setBudget(double budget) { this.budget = budget; }
    
    public double getBudgetRemaining() { return budgetRemaining; }
    public void setBudgetRemaining(double budgetRemaining) { this.budgetRemaining = budgetRemaining; }
}