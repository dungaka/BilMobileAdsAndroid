package com.consentmanager.sdk.callbacks;

/**
 * Provides a listener that will be called when the Network is not reachable
 */
public interface OnNetworkExceptionCallback {

    /**
     * Listener called when when the Network is not reachable
     * @param message the Error Message
     */
    void onErrorOccur(String message);

}