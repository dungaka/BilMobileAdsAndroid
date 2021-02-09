package com.bil.bilmobileads.entity;

import com.bil.bilmobileads.Constants;

public enum ADType {
    Banner(Constants.BANNER),
    SmartBanner(Constants.SMART_BANNER),
    Interstitial(Constants.INTERSTITIAL),
    Rewarded(Constants.REWARDED);

    private String type;

    ADType(String type) {
        this.type = type;
    }
}
