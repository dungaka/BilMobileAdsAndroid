package com.consentmanager.sdk.callbacks;


/**
 * Provides a listener that will be called when the consentmanager Server responded with an error Message
 */
public interface OnErrorDialogCallback {

    /**
     * Listener called when when the consentmanager Server responded with an error Message
     * @param message the Error Message
     */
    void onErrorOccur(String message);

}