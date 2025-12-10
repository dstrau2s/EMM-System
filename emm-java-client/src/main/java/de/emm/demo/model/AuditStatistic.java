// AuditStatistic.java
package de.emm.demo.model;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

public class AuditStatistic {
    private int totalEntries;
    private int tablesTracked;
    private Date firstEntry;
    private Date lastEntry;
    private int usersTracked;
    private int statusChanges;
    private List<ActionCount> topActions = new ArrayList<>();
    private List<TableCount> tableDistribution = new ArrayList<>();
    private List<DailyActivity> dailyActivities = new ArrayList<>();
    
    // Innere Klassen für spezifische Statistiken
    public static class ActionCount {
        private String action;
        private int count;
        
        // Getter und Setter
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
    }
    
    public static class TableCount {
        private String tableName;
        private int count;
        
        // Getter und Setter
        public String getTableName() { return tableName; }
        public void setTableName(String tableName) { this.tableName = tableName; }
        
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
    }
    
    public static class DailyActivity {
        private Date date;
        private int entryCount;
        private int userCount;
        
        // Getter und Setter
        public Date getDate() { return date; }
        public void setDate(Date date) { this.date = date; }
        
        public int getEntryCount() { return entryCount; }
        public void setEntryCount(int entryCount) { this.entryCount = entryCount; }
        
        public int getUserCount() { return userCount; }
        public void setUserCount(int userCount) { this.userCount = userCount; }
    }
    
    // Getter und Setter für die Hauptklasse
    public int getTotalEntries() { return totalEntries; }
    public void setTotalEntries(int totalEntries) { this.totalEntries = totalEntries; }
    
    public int getTablesTracked() { return tablesTracked; }
    public void setTablesTracked(int tablesTracked) { this.tablesTracked = tablesTracked; }
    
    public Date getFirstEntry() { return firstEntry; }
    public void setFirstEntry(Date firstEntry) { this.firstEntry = firstEntry; }
    
    public Date getLastEntry() { return lastEntry; }
    public void setLastEntry(Date lastEntry) { this.lastEntry = lastEntry; }
    
    public int getUsersTracked() { return usersTracked; }
    public void setUsersTracked(int usersTracked) { this.usersTracked = usersTracked; }
    
    public int getStatusChanges() { return statusChanges; }
    public void setStatusChanges(int statusChanges) { this.statusChanges = statusChanges; }
    
    public List<ActionCount> getTopActions() { return topActions; }
    public void setTopActions(List<ActionCount> topActions) { this.topActions = topActions; }
    
    public List<TableCount> getTableDistribution() { return tableDistribution; }
    public void setTableDistribution(List<TableCount> tableDistribution) { this.tableDistribution = tableDistribution; }
    
    public List<DailyActivity> getDailyActivities() { return dailyActivities; }
    public void setDailyActivities(List<DailyActivity> dailyActivities) { this.dailyActivities = dailyActivities; }
    
    // Hilfsmethoden
    public double getAverageEntriesPerDay() {
        if (firstEntry == null || lastEntry == null || totalEntries == 0) {
            return 0.0;
        }
        
        long diffInMillis = lastEntry.getTime() - firstEntry.getTime();
        long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);
        
        if (diffInDays == 0) {
            return totalEntries;
        }
        
        return (double) totalEntries / diffInDays;
    }
    
    public double getComplianceRate() {
        if (statusChanges == 0) {
            return 0.0;
        }
        
        // Beispiel: Angenommen, Statusänderungen sind ein Indikator für Compliance
        // In der Praxis würde hier die tatsächliche Compliance-Logik stehen
        return (double) statusChanges / totalEntries * 100;
    }
    
    public String getSummary() {
        return String.format(
            "AuditStatistik: %d Einträge, %d Tabellen, %d Benutzer, %d Statusänderungen",
            totalEntries, tablesTracked, usersTracked, statusChanges
        );
    }
    
    @Override
    public String toString() {
        return String.format(
            "AuditStatistic[total=%d, tables=%d, users=%d, first=%s, last=%s]",
            totalEntries, tablesTracked, usersTracked, firstEntry, lastEntry
        );
    }
}