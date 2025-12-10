// AuditTableInfo.java
package de.emm.demo.model;

import java.util.ArrayList;
import java.util.List;

public class AuditTableInfo {
    private boolean tableExists;
    private int columnCount;
    private List<ColumnInfo> columns = new ArrayList<>();
    private boolean triggerExists;
    private String triggerName;
    private boolean triggerActive;
    
    // Innere Klasse für Spalteninformationen
    public static class ColumnInfo {
        private String name;
        private String dataType;
        private boolean nullable;
        private int maxLength;
        
        // Getter und Setter
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDataType() { return dataType; }
        public void setDataType(String dataType) { this.dataType = dataType; }
        
        public boolean isNullable() { return nullable; }
        public void setNullable(boolean nullable) { this.nullable = nullable; }
        
        public int getMaxLength() { return maxLength; }
        public void setMaxLength(int maxLength) { this.maxLength = maxLength; }
        
        @Override
        public String toString() {
            return String.format("Column[name=%s, type=%s, nullable=%s, maxLength=%d]",
                name, dataType, nullable, maxLength);
        }
    }
    
    // Getter und Setter für die Hauptklasse
    public boolean isTableExists() { return tableExists; }
    public void setTableExists(boolean tableExists) { this.tableExists = tableExists; }
    
    public int getColumnCount() { return columnCount; }
    public void setColumnCount(int columnCount) { this.columnCount = columnCount; }
    
    public List<ColumnInfo> getColumns() { return columns; }
    public void setColumns(List<ColumnInfo> columns) { this.columns = columns; }
    
    public boolean isTriggerExists() { return triggerExists; }
    public void setTriggerExists(boolean triggerExists) { this.triggerExists = triggerExists; }
    
    public String getTriggerName() { return triggerName; }
    public void setTriggerName(String triggerName) { this.triggerName = triggerName; }
    
    public boolean isTriggerActive() { return triggerActive; }
    public void setTriggerActive(boolean triggerActive) { this.triggerActive = triggerActive; }
    
    // Hilfsmethoden
    public String getColumnNames() {
        StringBuilder sb = new StringBuilder();
        for (ColumnInfo column : columns) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(column.getName());
        }
        return sb.toString();
    }
    
    public boolean hasRequiredColumns() {
        // Prüfe ob alle erforderlichen Spalten vorhanden sind
        String[] requiredColumns = {"id", "tabelle", "aktion", "zeitpunkt"};
        
        for (String required : requiredColumns) {
            boolean found = false;
            for (ColumnInfo column : columns) {
                if (required.equalsIgnoreCase(column.getName())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }
    
    public String getStatusSummary() {
        return String.format(
            "AuditTableInfo[exists=%s, columns=%d, triggerExists=%s, triggerActive=%s]",
            tableExists, columnCount, triggerExists, triggerActive
        );
    }
    
    @Override
    public String toString() {
        return String.format(
            "AuditTableInfo{tableExists=%s, columns=%d, trigger='%s' active=%s}",
            tableExists, columnCount, triggerName, triggerActive
        );
    }
}