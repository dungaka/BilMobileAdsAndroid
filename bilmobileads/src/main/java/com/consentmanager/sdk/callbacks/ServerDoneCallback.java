package com.consentmanager.sdk.callbacks;

import com.consentmanager.sdk.CMPConsentTool;
import com.consentmanager.sdk.server.ServerResponse;

/**
 * Provides a listener that will be called when {@link CMPConsentTool} requested the consentmanager Server an got a response
 */
public interface ServerDoneCallback {

    /**
     * Listener called when {@link CMPConsentTool} requested the consentmanager Server an got a response
     */
    void onServerResponse(ServerResponse response);

}