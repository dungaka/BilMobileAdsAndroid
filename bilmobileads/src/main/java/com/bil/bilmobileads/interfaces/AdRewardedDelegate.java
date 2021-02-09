package com.bil.bilmobileads.interfaces;

import com.bil.bilmobileads.entity.ADRewardItem;

public class AdRewardedDelegate {

    public void onRewardedAdLoaded() {
    }

    public void onRewardedAdOpened() {
    }

    public void onRewardedAdClosed() {
    }

    public void onUserEarnedReward(ADRewardItem rewardItem) {
    }

    public void onRewardedAdFailedToLoad(String error) {
    }

    public void onRewardedAdFailedToShow(String error) {
    }
}
