package com.bil.bilmobileads.unity;

public interface UADListener {

    void onAdLoaded();

    void onAdOpened();

    void onAdClosed();

    void onAdClicked();

    void onAdLeftApplication();

    void onAdFailedToLoad(String errorReason);

}
