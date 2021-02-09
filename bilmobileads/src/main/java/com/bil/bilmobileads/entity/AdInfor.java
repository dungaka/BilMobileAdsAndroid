package com.bil.bilmobileads.entity;

public class AdInfor {
    public boolean isVideo;
    public HostCustom host;
    public String configId;
    public String adUnitID;

    public AdInfor(boolean isVideo, HostCustom host, String configId, String adUnitID) {
        this.isVideo = isVideo;
        this.host = host;
        this.configId = configId;
        this.adUnitID = adUnitID;
    }
}
