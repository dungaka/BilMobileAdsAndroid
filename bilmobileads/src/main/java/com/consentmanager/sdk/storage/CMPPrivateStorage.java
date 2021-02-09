package com.consentmanager.sdk.storage;

import android.content.Context;
import android.preference.PreferenceManager;

import java.util.Date;

public class CMPPrivateStorage {
    //private Data to Save
    private static final String CONSENT_TOOL_URL = "IABConsent_ConsentToolUrl";
    private static final String CMP_REQUEST = "IABConsent_CMPRequest";

    //fallback String
    public static final String EMPTY_DEFAULT_STRING = "";

    /**
     * Stores the passed date millis as a string in the shared preferences
     *
     * @param context  Context used to access the SharedPreferences
     * @param date the Date, when the Server was last requested
     */
    public static void setLastRequested(Context context, Date date) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(CMP_REQUEST, ""+date.getTime()).apply();
    }

    /**
     * Returns the date when the server was requested for the last time
     *
     * @param context Context used to access the SharedPreferences
     * @return the stored Date
     */
    public static Date getLastRequested(Context context) {
        String value = PreferenceManager.getDefaultSharedPreferences(context).getString(CMP_REQUEST, EMPTY_DEFAULT_STRING);
        if( value.equals(EMPTY_DEFAULT_STRING)){
            return null;
        } else {
            Long time = Long.parseLong(value);
            return new Date(time);
        }
    }

    /**
     * Returns the ConsentToolURL as a String.
     * @param context The context of the app View
     * @return The ConsentToolURL
     */
    public static String getConsentToolURL(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(CONSENT_TOOL_URL, EMPTY_DEFAULT_STRING);
    }

    /**
     * Sets a new ConsentToolURL .
     * @param context The context of the app View
     * @param url The Url, which should be shown.
     */
    public static void setConsentToolURL(Context context, String url) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(CONSENT_TOOL_URL, url).apply();
    }
}
