package com.consentmanager.sdk.model;


import android.content.Context;

import androidx.annotation.NonNull;

import com.consentmanager.sdk.storage.CMPPrivateStorage;
import com.consentmanager.sdk.storage.CMPStorageV1;

import java.io.Serializable;

public class CMPSettings implements Serializable {
    private static CMPSettings instance;
    private static Context context;

    private SubjectToGdpr subjectToGdpr;
    private String consentToolUrl;
    private String consentString;

    private CMPSettings() {}

    /**
     * Creates an instance of this class
     *
     * @param isGdprEnabled  Enum that indicates
     *                       'CMPGDPRDisabled' – value 0, not subject to GDPR,
     *                       'CMPGDPREnabled'  – value 1, subject to GDPR,
     *                       'CMPGDPRUnknown'  - value -1, unset.
     * @param consentToolUrl url that is used to create and load the request into the WebView - it is the request for the consent webpage. This property is mandatory
     * @param consentString  If this property is given, it enforces reinitialization with the given string, configured based on the consentToolUrl. This property is optional.
     */
    private CMPSettings(SubjectToGdpr isGdprEnabled, @NonNull String consentToolUrl, String consentString) {
        setConsentString(consentString);
        setConsentToolUrl(consentToolUrl);
        setSubjectToGdpr(isGdprEnabled);
    }

    /**
     * Careful, unsafe operation! If the Instance was not initialised, this will return the CMPSettings
     * stored in the CMPStorage.
     * @return the created CMPSettings singleton
     */
    public static CMPSettings getInstance(Context context){
        CMPSettings.context = context;
        if( instance == null) {
            instance = new CMPSettings(
                    CMPStorageV1.getSubjectToGdpr(context),
                    CMPPrivateStorage.getConsentToolURL(context),
                    CMPStorageV1.getConsentString(context));
        }
        return instance;
    }

    /**
     * Creates and returns a new CMPSettings Instance
     * @param isGdprEnabled  Enum that indicates
     *                       'CMPGDPRDisabled' – value 0, not subject to GDPR,
     *                       'CMPGDPREnabled'  – value 1, subject to GDPR,
     *                       'CMPGDPRUnknown'  - value -1, unset.
     * @param consentToolUrl url that is used to create and load the request into the WebView - it is the request for the consent webpage. This property is mandatory
     * @param consentString  If this property is given, it enforces reinitialization with the given string, configured based on the consentToolUrl. This property is optional.
     * @return the created or current CMP SettingsClass with the adapted values
     */
    public static CMPSettings getInstance(Context context, SubjectToGdpr isGdprEnabled, @NonNull String consentToolUrl, String consentString){
        CMPSettings.context = context;
        if( instance == null) {
            instance = new CMPSettings(isGdprEnabled, consentToolUrl, consentString);
        } else {
            instance.setConsentString(consentString);
            instance.setConsentToolUrl(consentToolUrl);
            instance.setSubjectToGdpr(isGdprEnabled);
        }
        return instance;
    }

    /**
     * @return Enum that indicates
     * 'CMPGDPRDisabled' – value 0, not subject to GDPR,
     * 'CMPGDPREnabled'  – value 1, subject to GDPR,
     * 'CMPGDPRUnknown'  - value -1, unset.
     */
    public SubjectToGdpr getSubjectToGdpr() {
        return subjectToGdpr;
    }


    /**
     * @param subjectToGdpr Enum that indicates
     *                      'CMPGDPRDisabled' – value 0, not subject to GDPR,
     *                      'CMPGDPREnabled'  – value 1, subject to GDPR,
     *                      'CMPGDPRUnknown'  - value -1, unset.
     */
    public void setSubjectToGdpr(SubjectToGdpr subjectToGdpr) {
        CMPStorageV1.setSubjectToGdpr(context, subjectToGdpr);
        this.subjectToGdpr = subjectToGdpr;
    }

    /**
     * @return url that is used to create and load the request into the WebView
     */
    public String getConsentToolUrl() {
        return consentToolUrl;
    }

    /**
     * @param consentToolUrl url that is used to create and load the request into the WebView
     */
    public void setConsentToolUrl(String consentToolUrl) {
        CMPPrivateStorage.setConsentToolURL(context, consentToolUrl);
        this.consentToolUrl = consentToolUrl;
    }

    /**
     * @return the consent string passed as a websafe base64-encoded string.
     */
    public String getConsentString() {
        return consentString;
    }

    /**
     * @param consentString the consent string passed as a websafe base64-encoded string.
     */
    public void setConsentString(String consentString) {
        CMPStorageV1.setConsentString(context, consentString);
        this.consentString = consentString;
    }
}