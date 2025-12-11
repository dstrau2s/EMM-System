// EMMException.java
package de.emm.demo.exception;

public class EMMException extends RuntimeException {
	private static final long serialVersionUID = 1L;
    private final ErrorCode errorCode;
    
    public EMMException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public EMMException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
    
    public enum ErrorCode {
        // Gerätebezogene Fehler
        DEVICE_NOT_FOUND,
        DEVICE_NOT_AVAILABLE,
        DEVICE_ALREADY_ASSIGNED,
        DUPLICATE_IMEI,
        INVALID_DEVICE_STATUS,
        INVALID_STATUS_TRANSITION,
        
        // Mitarbeiterbezogene Fehler
        EMPLOYEE_NOT_FOUND,
        
        // Compliance-Fehler
        POLICY_NOT_FOUND,
        
        // Allgemeine Fehler
        VALIDATION_ERROR,
        DATABASE_ERROR,
        PERMISSION_DENIED,
        CONFIGURATION_ERROR
    }
}