package com.consentmanager.sdk.model;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;

import com.consentmanager.sdk.exceptions.CMPConsentToolSettingsException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class CMPConfig {
    private final static String PACKAGE_NAME = "com.consentmanager.sdk";
    private final static String CONFIG_ID = ".ID";
    private final static String CONFIG_SERVER_DOMAIN = ".SERVER_DOMAIN";
    private final static String CONFIG_APP_NAME = ".APP_NAME";
    private final static String CONFIG_LANGUAGE = ".LANGUAGE";
    private static CMPConfig config;
    private int id;
    private String serverDomain;
    private String appName;
    private String language;

    private CMPConfig(){}

    private CMPConfig(int id, String server_domain, String app_name, String language) {
        this.setId(id);
        this.setServerDomain(server_domain);
        this.setAppName(app_name);
        this.setLanguage(language);
    }

    private void setLanguage(String language) {
        this.language = language;
    }

    public String getLanguage() {
        return language;
    }

    private void setAppName(String app_name) {
        this.appName = encodeValue(app_name);
    }

    public String getAppName() {
        return appName;
    }

    private void setServerDomain(String server_domain) {
        this.serverDomain = server_domain;
    }

    public String getServerDomain() {
        return serverDomain;
    }

    private void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static CMPConfig createInstance(Context context) {
        config = getConfigParams(context);
        return config;
    }

    public static CMPConfig getInstance(Context context) {
        if( config != null) {
            return config;
        }
        throw new CMPConsentToolSettingsException("CMP consent Settings are not configured yet");
    }

    public static CMPConfig createInstance(int id, String server_domain, String app_name, String language) {
        if( config == null){
            config = new CMPConfig(id, server_domain, app_name, language);
        } else {
            config.setId(id);
            config.setServerDomain(server_domain);
            config.setAppName(app_name);
            config.setLanguage(language);
        }
        return config;
    }

    private static String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new CMPConsentToolSettingsException(String.format("The Server domain URL given is not valid to UTF-8 encode: %s", value));
        }
    }

    private static CMPConfig getConfigParams(Context context) {
            return new CMPConfig(
                    getManifestInteger(context,PACKAGE_NAME + CONFIG_ID, 0),
                    getManifestString(context,PACKAGE_NAME + CONFIG_SERVER_DOMAIN, ""),
                    getManifestString(context,PACKAGE_NAME + CONFIG_APP_NAME, ""),
                    getManifestString(context,PACKAGE_NAME + CONFIG_LANGUAGE, "EN"));
    }

    private static String getManifestString(Context context, String meta_key, String fallback){
        try {
            ApplicationInfo app = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = app.metaData;
            String val = bundle.getString(meta_key, fallback);
            if( val.equals(fallback)){
                throw new CMPConsentToolSettingsException(String.format("The field %s is not filled in your Manifest file!", meta_key));
            } else {
                return val;
            }
        } catch (PackageManager.NameNotFoundException e) {
            throw new CMPConsentToolSettingsException(String.format("The field %s is not given in your Manifest file!", meta_key));
        }
    }

    private static Integer getManifestInteger(Context context, String meta_key, Integer fallback) {
        try {
            ApplicationInfo app = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = app.metaData;
            Integer val = bundle.getInt(meta_key, fallback);
            if( val.equals(fallback)){
                throw new CMPConsentToolSettingsException(String.format("The field %s is not filled in your Manifest file!", meta_key));
            } else {
                return val;
            }
        } catch (PackageManager.NameNotFoundException e) {
            throw new CMPConsentToolSettingsException(String.format("The field %s is not given in your Manifest file!", meta_key));
        }
    }

}
