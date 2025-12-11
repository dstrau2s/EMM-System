// DuplicateImeiException.java
package de.emm.demo.exception;

public class DuplicateImeiException extends EMMException {
	private static final long serialVersionUID = 1L;  // serialVersionUID hinzugefügt
    private final String imei;
    
    public DuplicateImeiException(String imei) {
        super(ErrorCode.DUPLICATE_IMEI, 
              String.format("IMEI '%s' ist bereits vergeben", imei));
        this.imei = imei;
    }
    
    public String getImei() {
        return imei;
    }
}