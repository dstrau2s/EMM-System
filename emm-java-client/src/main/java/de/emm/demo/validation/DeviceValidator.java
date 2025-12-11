// DeviceValidator.java
package de.emm.demo.validation;

import de.emm.demo.dto.CreateDeviceRequest;
import de.emm.demo.exception.EMMException;
import java.util.regex.Pattern;

public class DeviceValidator {
    
    private static final Pattern IMEI_PATTERN = Pattern.compile("^[0-9]{15}$");
    private static final Pattern STATUS_PATTERN = 
        Pattern.compile("^(LAGER|AKTIV|DEFEKT|AUSGESCHIEDEN)$", Pattern.CASE_INSENSITIVE);
    
    public static void validateCreateRequest(CreateDeviceRequest request) {
        if (request == null) {
            throw new EMMException(EMMException.ErrorCode.VALIDATION_ERROR, 
                                 "Request darf nicht null sein");
        }
        
        validateManufacturer(request.getManufacturer());
        validateModel(request.getModel());
        validateOs(request.getOs());
        validateOsVersion(request.getOsVersion());
        validateImei(request.getImei());
        validateStatus(request.getStatus());
    }
    
    private static void validateManufacturer(String manufacturer) {
        if (manufacturer == null || manufacturer.trim().isEmpty()) {
            throw new EMMException(EMMException.ErrorCode.VALIDATION_ERROR, 
                                 "Hersteller darf nicht leer sein");
        }
        if (manufacturer.length() > 50) {
            throw new EMMException(EMMException.ErrorCode.VALIDATION_ERROR, 
                                 "Hersteller darf maximal 50 Zeichen haben");
        }
    }
    
    private static void validateModel(String model) {
        if (model == null || model.trim().isEmpty()) {
            throw new EMMException(EMMException.ErrorCode.VALIDATION_ERROR, 
                                 "Modell darf nicht leer sein");
        }
        if (model.length() > 50) {
            throw new EMMException(EMMException.ErrorCode.VALIDATION_ERROR, 
                                 "Modell darf maximal 50 Zeichen haben");
        }
    }
    
    private static void validateOs(String os) {
        if (os == null || os.trim().isEmpty()) {
            throw new EMMException(EMMException.ErrorCode.VALIDATION_ERROR, 
                                 "Betriebssystem darf nicht leer sein");
        }
        if (os.length() > 20) {
            throw new EMMException(EMMException.ErrorCode.VALIDATION_ERROR, 
                                 "Betriebssystem darf maximal 20 Zeichen haben");
        }
    }
    
    private static void validateOsVersion(String osVersion) {
        if (osVersion == null || osVersion.trim().isEmpty()) {
            throw new EMMException(EMMException.ErrorCode.VALIDATION_ERROR, 
                                 "OS-Version darf nicht leer sein");
        }
        if (osVersion.length() > 20) {
            throw new EMMException(EMMException.ErrorCode.VALIDATION_ERROR, 
                                 "OS-Version darf maximal 20 Zeichen haben");
        }
    }
    
    private static void validateImei(String imei) {
        if (imei == null || imei.trim().isEmpty()) {
            throw new EMMException(EMMException.ErrorCode.VALIDATION_ERROR, 
                                 "IMEI darf nicht leer sein");
        }
        
        if (!IMEI_PATTERN.matcher(imei).matches()) {
            throw new EMMException(EMMException.ErrorCode.VALIDATION_ERROR, 
                                 "IMEI muss genau 15 Ziffern enthalten");
        }
    }
    
    private static void validateStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new EMMException(EMMException.ErrorCode.VALIDATION_ERROR, 
                                 "Status darf nicht leer sein");
        }
        
        if (!STATUS_PATTERN.matcher(status).matches()) {
            throw new EMMException(EMMException.ErrorCode.VALIDATION_ERROR, 
                                 "Status muss einer der folgenden sein: LAGER, AKTIV, DEFEKT, AUSGESCHIEDEN");
        }
    }
    
    public static boolean isValidStatusTransition(String fromStatus, String toStatus) {
        // Erlaubte Statusübergänge:
        // LAGER -> AKTIV (Ausgabe)
        // AKTIV -> LAGER (Rücknahme)
        // * -> DEFEKT (Defekt melden)
        // * -> AUSGESCHIEDEN (Entfernen)
        
        if (fromStatus.equalsIgnoreCase(toStatus)) {
            return true; // Keine Änderung ist immer erlaubt
        }
        
        switch (fromStatus.toUpperCase()) {
            case "LAGER":
                return toStatus.equalsIgnoreCase("AKTIV") || 
                       toStatus.equalsIgnoreCase("DEFEKT") || 
                       toStatus.equalsIgnoreCase("AUSGESCHIEDEN");
            case "AKTIV":
                return toStatus.equalsIgnoreCase("LAGER") || 
                       toStatus.equalsIgnoreCase("DEFEKT") || 
                       toStatus.equalsIgnoreCase("AUSGESCHIEDEN");
            case "DEFEKT":
                return toStatus.equalsIgnoreCase("LAGER") || 
                       toStatus.equalsIgnoreCase("AUSGESCHIEDEN");
            case "AUSGESCHIEDEN":
                return false; // AUSGESCHIEDEN ist Endzustand
            default:
                return false;
        }
    }
}