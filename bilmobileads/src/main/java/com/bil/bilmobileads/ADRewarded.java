package com.bil.bilmobileads;

import android.app.Activity;
import android.util.Log;

import com.bil.bilmobileads.entity.ADRewardItem;
import com.bil.bilmobileads.entity.LogType;
import com.bil.bilmobileads.interfaces.ResultCallback;
import com.bil.bilmobileads.interfaces.WorkCompleteDelegate;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.bil.bilmobileads.entity.ADFormat;
import com.bil.bilmobileads.entity.AdInfor;
import com.bil.bilmobileads.entity.AdUnitObj;
import com.bil.bilmobileads.interfaces.AdRewardedDelegate;

import org.prebid.mobile.AdUnit;
import org.prebid.mobile.OnCompleteListener;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.RewardedVideoAdUnit;

import androidx.annotation.NonNull;

public class ADRewarded {

    // MARK: - View
    private Activity activityAd;
    private AdRewardedDelegate adDelegate;

    // MARK: - AD
    private PublisherAdRequest amRequest;
    private AdUnit adUnit;
    private RewardedAd amRewarded;
    // MARK: - AD info
    private String placement;
    private AdUnitObj adUnitObj;

    // MARK: Properties
    private boolean isFetchingAD = false;

    public ADRewarded(Activity activity, final String placementStr) {
        if (activity == null || placementStr == null) {
            PBMobileAds.getInstance().log(LogType.ERROR, "Activity or Placement is null");
            throw new NullPointerException();
        }
        PBMobileAds.getInstance().log(LogType.INFOR, "ADRewarded placement: " + placementStr + " Init");

        this.activityAd = activity;
        this.placement = placementStr;

        this.getConfigAD();
    }

    // MARK: - Handle AD
    void getConfigAD() {
        this.adUnitObj = PBMobileAds.getInstance().getAdUnitObj(this.placement);
        if (this.adUnitObj == null) {
            this.isFetchingAD = true;

            // Get AdUnit Info
            PBMobileAds.getInstance().getADConfig(this.placement, new ResultCallback<AdUnitObj, Exception>() {
                @Override
                public void success(AdUnitObj data) {
                    PBMobileAds.getInstance().log(LogType.INFOR, "ADRewarded placement: " + placement + " Init Success");
                    isFetchingAD = false;
                    adUnitObj = data;

                    PBMobileAds.getInstance().showCMP(new WorkCompleteDelegate() {
                        @Override
                        public void doWork() {
                            preLoad();
                        }
                    });
                }

                @Override
                public void failure(Exception error) {
                    PBMobileAds.getInstance().log(LogType.INFOR, "ADRewarded placement: " + placement + " Init Failed with Error: " + error.getLocalizedMessage() + ". Please check your internet connect.");
                    isFetchingAD = false;
                }
            });
        } else {
            this.preLoad();
        }
    }

    void resetAD() {
        if (this.adUnit == null || this.amRewarded == null) return;

        this.isFetchingAD = false;

        this.adUnit.stopAutoRefresh();
        this.adUnit = null;

        this.amRewarded = null;
    }

    void handlerResult(ResultCode resultCode) {
        if (resultCode == ResultCode.SUCCESS) {
            this.amRewarded.loadAd(this.amRequest, new RewardedAdLoadCallback() {
                @Override
                public void onRewardedAdLoaded() {
                    isFetchingAD = false;

                    PBMobileAds.getInstance().log(LogType.INFOR, "onRewardedAdLoaded: ADRewarded Placement '" + placement + "' Loaded Success");
                    if (adDelegate != null) adDelegate.onRewardedAdLoaded();
                }

                @Override
                public void onRewardedAdFailedToLoad(int errorCode) {
                    isFetchingAD = false;

                    String messErr = "onRewardedAdFailedToLoad: ADRewarded Placement '" + placement + "' failed: " + PBMobileAds.getInstance().getAdRewardedError(errorCode);
                    PBMobileAds.getInstance().log(LogType.INFOR, messErr);
                    if (adDelegate != null) adDelegate.onRewardedAdFailedToLoad(messErr);
                }
            });
        } else {
            this.isFetchingAD = false;

            if (resultCode == ResultCode.NO_BIDS) {
                PBMobileAds.getInstance().log(LogType.INFOR, "ADRewarded Placement '" + this.placement + "' No Bids.");
            } else if (resultCode == ResultCode.TIMEOUT) {
                PBMobileAds.getInstance().log(LogType.INFOR, "ADRewarded Placement '" + this.placement + "' Timeout. Please check your internet connect.");
            }
        }
    }

    // MARK: - Public FUNC
    public void preLoad() {
        PBMobileAds.getInstance().log(LogType.DEBUG, "ADRewarded Placement '" + this.placement + "' - isReady: " + this.isReady() + " | isFetchingAD: " + this.isFetchingAD);
        if (this.adUnitObj == null || this.isReady() || this.isFetchingAD) {
            if (this.adUnitObj == null && !this.isFetchingAD) {
                PBMobileAds.getInstance().log(LogType.INFOR, "ADRewarded placement: " + this.placement + " is not ready to load.");
                this.getConfigAD();
                return;
            }
            return;
        }
        this.resetAD();

        // Check Active
        if (!this.adUnitObj.isActive || this.adUnitObj.adInfor.size() <= 0) {
            PBMobileAds.getInstance().log(LogType.INFOR, "ADRewarded Placement '" + this.placement + "' is not active or not exist.");
            return;
        }

        // Check AdInfor
        boolean isVideo = this.adUnitObj.defaultFormat == ADFormat.VAST;
        AdInfor adInfor = PBMobileAds.getInstance().getAdInfor(isVideo, this.adUnitObj);
        if (adInfor == null) {
            PBMobileAds.getInstance().log(LogType.INFOR, "AdInfor of ADRewarded Placement '" + this.placement + "' is not exist.");
            return;
        }

        PBMobileAds.getInstance().log(LogType.INFOR, "Preload ADRewarded Placement: " + this.placement);
        PBMobileAds.getInstance().setupPBS(adInfor.host);
        PBMobileAds.getInstance().log(LogType.DEBUG, "[ADRewarded Video] - configId: " + adInfor.configId + " | adUnitID: " + adInfor.adUnitID);
        this.adUnit = new RewardedVideoAdUnit(adInfor.configId);
        this.amRewarded = new RewardedAd(PBMobileAds.getInstance().getContextApp(), adInfor.adUnitID);

        // Create Request PBS
        this.isFetchingAD = true;
        final PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        if (PBMobileAds.getInstance().isTestMode) {
            builder.addTestDevice(Constants.DEVICE_ID_TEST);
        }
        this.amRequest = builder.build();
        this.adUnit.fetchDemand(this.amRequest, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {
                PBMobileAds.getInstance().log(LogType.DEBUG, "PBS demand fetch ADRewarded placement '" + placement + "' for DFP: " + resultCode.name());
                handlerResult(resultCode);
            }
        });
    }

    public void show() {
        if (this.isReady()) {
            this.amRewarded.show(this.activityAd, new RewardedAdCallback() {
                @Override
                public void onRewardedAdOpened() {
                    PBMobileAds.getInstance().log(LogType.INFOR, "onRewardedAdOpened: ADRewarded Placement '" + placement + "'");
                    if (adDelegate != null) adDelegate.onRewardedAdOpened();
                }

                @Override
                public void onRewardedAdClosed() {
                    PBMobileAds.getInstance().log(LogType.INFOR, "onRewardedAdClosed: ADRewarded Placement '" + placement + "'");
                    if (adDelegate != null) adDelegate.onRewardedAdClosed();
                }

                @Override
                public void onUserEarnedReward(@NonNull RewardItem reward) {
                    String dataReward = "Type: " + reward.getType() + " | Amount: " + reward.getAmount();
                    PBMobileAds.getInstance().log(LogType.INFOR, "onUserEarnedReward: ADRewarded Placement '" + placement + "' with RewardItem - " + dataReward);

                    ADRewardItem rewardItem = new ADRewardItem(reward.getType(), reward.getAmount());
                    if (adDelegate != null) adDelegate.onUserEarnedReward(rewardItem);
                }

                @Override
                public void onRewardedAdFailedToShow(int errorCode) {
                    super.onRewardedAdFailedToShow(errorCode);

                    String messErr = "onRewardedAdFailedToShow: ADRewarded Placement '" + placement + "' with error: " + PBMobileAds.getInstance().getAdRewardedError(errorCode);
                    PBMobileAds.getInstance().log(LogType.INFOR, messErr);
                    if (adDelegate != null) adDelegate.onRewardedAdFailedToShow(messErr);
                }
            });
        } else {
            PBMobileAds.getInstance().log(LogType.INFOR, "ADRewarded Placement '" + this.placement + "' currently unavailable, call preload() first.");
        }
    }

    public void destroy() {
        PBMobileAds.getInstance().log(LogType.INFOR, "Destroy ADRewarded Placement: " + this.placement);
        this.resetAD();
    }

    public boolean isReady() {
        if (this.adUnit == null || this.amRewarded == null) return false;
        return this.amRewarded.isLoaded();
    }

    public void setListener(AdRewardedDelegate adDelegate) {
        this.adDelegate = adDelegate;
    }
}
