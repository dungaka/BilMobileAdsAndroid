package com.consentmanager.sdk.exceptions;

public class CMPConsentToolException extends RuntimeException{

    private CMPConsentToolException() {}

    public CMPConsentToolException(String message) {
        super(message);
    }
}
