package com.bil.bilmobileads.unity;

public interface UADRewardedListener {

    void onAdLoaded();

    void onAdOpened();

    void onAdClosed();

    void onUserEarnedReward(String type, double amount);

    void onAdFailedToLoad(String errorReason);

    void onAdFailedToShow(String errorReason);
}
