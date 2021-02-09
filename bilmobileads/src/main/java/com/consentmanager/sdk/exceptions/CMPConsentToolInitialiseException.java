package com.consentmanager.sdk.exceptions;

public class CMPConsentToolInitialiseException extends CMPConsentToolException {

    public CMPConsentToolInitialiseException() {
        super("You first have to initialise the CPMConsentTool with the createInstance Method, before using the Instance");
    }

    public CMPConsentToolInitialiseException(String message) {
        super(message);
    }
}
