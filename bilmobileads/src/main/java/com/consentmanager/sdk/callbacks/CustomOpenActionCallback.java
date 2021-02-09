package com.consentmanager.sdk.callbacks;

import com.consentmanager.sdk.activities.CMPConsentToolActivity;
import com.consentmanager.sdk.model.CMPSettings;
import com.consentmanager.sdk.server.ServerResponse;

/**
 * Provides a listener that will be called when a custom WebView should be opened, apart from the default one.
 */
public interface CustomOpenActionCallback {

    /**
     * Listener called when a custom WebView should be opened, apart from the default one.
     */
    void onOpenCMPConsentToolActivity(ServerResponse response, CMPSettings settings);

}