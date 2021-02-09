package com.bil.bilmobileads.entity;

public class HostCustom {
    public String pbHost;
    public String pbAccountId;
    public String storedAuctionResponse;

    public HostCustom(String pbHost, String pbAccountId, String storedAuctionResponse) {
        this.pbHost = pbHost;
        this.pbAccountId = pbAccountId;
        this.storedAuctionResponse = storedAuctionResponse;
    }
}