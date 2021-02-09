package com.consentmanager.sdk.server;

import com.consentmanager.sdk.model.CMPConfig;

class ServerRequest {
    String url;

    public ServerRequest(CMPConfig config, String consent, String idfa){
        this.url = String.format("https://%s/delivery/appjson.php?id=%s&name=%s&consent=%s&idfa=%s&l=%s",
                config.getServerDomain(),
                config.getId(),
                config.getAppName(),
                consent,
                idfa,
                config.getLanguage());
    }

    public String getAsURL(){
        return url;
    }
}
