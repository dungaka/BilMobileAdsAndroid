package com.consentmanager.sdk.exceptions;

public class CMPConsentToolSettingsException extends CMPConsentToolException {

    public CMPConsentToolSettingsException(String message) {
        super(message);
        super.printStackTrace();
        System.exit(1);
    }
}
