package com.consentmanager.sdk.storage;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Used to retrieve and store the consentData, subjectToGdpr, vendors, purposes, consentToolUrl in the SharedPreferences
 */
public class CMPStorageConsentManager {
    //public Data to Public Stroage
    private static final String US_PRIVACY = "IABUSPrivacy_String";
    private static final String VENDORS = "CMConsent_ParsedVendorConsents";
    private static final String PURPOSES = "CMConsent_ParsedPurposeConsents";
    private static final String CONSENT_STRING = "CMConsent_ConsentString";

    //fallback String
    public static final String EMPTY_DEFAULT_STRING = "";


    public static String getUSPrivacyString(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(US_PRIVACY, EMPTY_DEFAULT_STRING);
    }

    public static void setUSPrivacyString(Context context, String usprivacy) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(US_PRIVACY, usprivacy).apply();
    }

    public static String getConsentString(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(CONSENT_STRING, EMPTY_DEFAULT_STRING);
    }

    public static void setConsentString(Context context, String consent) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(CONSENT_STRING, consent).apply();
    }

    /**
     * Returns the vendors binary String stored in the SharedPreferences
     *
     * @param context Context used to access the SharedPreferences
     * @return the stored vendors binary String
     */
    public static String getVendorsString(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(VENDORS, EMPTY_DEFAULT_STRING);
    }

    /**
     * Stores the passed vendors binary String in SharedPreferences
     *
     * @param context Context used to access the SharedPreferences
     * @param vendors binary String to be stored
     */
    public static void setVendorsString(Context context, String vendors) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(VENDORS, vendors).apply();
    }

    /**
     * Returns the purposes binary String stored in the SharedPreferences
     *
     * @param context Context used to access the SharedPreferences
     * @return the stored purposes binary String
     */
    public static String getPurposesString(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PURPOSES, EMPTY_DEFAULT_STRING);
    }

    /**
     * Stores the passed purposes binary String in SharedPreferences
     *
     * @param context  Context used to access the SharedPreferences
     * @param purposes binary String to be stored
     */
    public static void setPurposesString(Context context, String purposes) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PURPOSES, purposes).apply();
    }

    /**
     * Clears the Storage
     * @param context The Context of the App.
     */
    public static void clearConsents(Context context) {
        setUSPrivacyString(context, null);
        setVendorsString(context, null);
        setPurposesString(context, null);
    }
}