package com.bil.bilmobileads;

import com.bil.bilmobileads.entity.LogType;
import com.bil.bilmobileads.interfaces.ResultCallback;
import com.bil.bilmobileads.interfaces.WorkCompleteDelegate;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;

import com.bil.bilmobileads.entity.ADFormat;
import com.bil.bilmobileads.entity.AdInfor;
import com.bil.bilmobileads.entity.AdUnitObj;
import com.bil.bilmobileads.interfaces.AdDelegate;

import org.prebid.mobile.AdUnit;
import org.prebid.mobile.InterstitialAdUnit;
import org.prebid.mobile.OnCompleteListener;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.VideoInterstitialAdUnit;

public class ADInterstitial {

    // MARK: - View
    private AdDelegate adDelegate;

    // MARK: - AD
    private PublisherAdRequest amRequest;
    private AdUnit adUnit;
    private PublisherInterstitialAd amInterstitial;
    // MARK: - AD info
    private String placement;
    private AdUnitObj adUnitObj;

    // MARK: Properties
    private ADFormat adFormatDefault;

    private boolean isFetchingAD = false;
    private boolean setDefaultBidType = true;

    public ADInterstitial(final String placementStr) {
        if (placementStr == null) {
            PBMobileAds.getInstance().log(LogType.ERROR, "Placement is null");
            throw new NullPointerException();
        }

        PBMobileAds.getInstance().log(LogType.INFOR, "ADInterstitial placement: " + placementStr + " Init");
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
                    PBMobileAds.getInstance().log(LogType.INFOR, "ADInterstitial placement: " + placement + " Init Success");
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
                    PBMobileAds.getInstance().log(LogType.INFOR, "ADInterstitial placement: " + placement + " Init Failed with Error: " + error.getLocalizedMessage() + ". Please check your internet connect.");
                    isFetchingAD = false;
                }
            });
        } else {
            this.preLoad();
        }
    }

    boolean processNoBids() {
        if (this.adUnitObj.adInfor.size() >= 2 && this.adFormatDefault == this.adUnitObj.defaultFormat) {
            this.setDefaultBidType = false;
            this.preLoad();

            return true;
        } else {
            // Both or .video, .html is no bids -> wait and preload.
            PBMobileAds.getInstance().log(LogType.INFOR, "ADInterstitial Placement '" + this.placement + "' No Bids.");
            return false;
        }
    }

    void resetAD() {
        if (this.adUnit == null || this.amInterstitial == null) return;

        this.isFetchingAD = false;

        this.adUnit.stopAutoRefresh();
        this.adUnit = null;

        this.amInterstitial.setAdListener(null);
        this.amInterstitial = null;
    }

    void handlerResult(ResultCode resultCode) {
        if (resultCode == ResultCode.SUCCESS) {
            this.amInterstitial.loadAd(this.amRequest);
        } else {
            this.isFetchingAD = false;

            if (resultCode == ResultCode.NO_BIDS) {
                this.processNoBids();
            } else if (resultCode == ResultCode.TIMEOUT) {
                PBMobileAds.getInstance().log(LogType.INFOR, "ADInterstitial Placement '" + this.placement + "' Timeout. Please check your internet connect.");
            }
        }
    }

    // MARK: - Public FUNC
    public void preLoad() {
        PBMobileAds.getInstance().log(LogType.DEBUG, "ADInterstitial Placement '" + this.placement + "' - isReady: " + this.isReady() + " | isFetchingAD: " + this.isFetchingAD);
        if (this.adUnitObj == null || this.isReady() || this.isFetchingAD) {
            if (this.adUnitObj == null && !this.isFetchingAD) {
                PBMobileAds.getInstance().log(LogType.INFOR, "ADInterstitial placement: " + this.placement + " is not ready to load.");
                this.getConfigAD();
                return;
            }
            return;
        }
        this.resetAD();

        // Check Active
        if (!this.adUnitObj.isActive || this.adUnitObj.adInfor.size() <= 0) {
            PBMobileAds.getInstance().log(LogType.INFOR, "ADInterstitial Placement '" + this.placement + "' is not active or not exist.");
            return;
        }

        // Check and Set Default
        this.adFormatDefault = this.adUnitObj.defaultFormat;
        if (!this.setDefaultBidType && this.adUnitObj.adInfor.size() >= 2) {
            this.adFormatDefault = this.adFormatDefault == ADFormat.VAST ? ADFormat.HTML : ADFormat.VAST;
            this.setDefaultBidType = true;
        }

        // Check AdInfor
        boolean isVideo = this.adFormatDefault == ADFormat.VAST;
        AdInfor adInfor = PBMobileAds.getInstance().getAdInfor(isVideo, this.adUnitObj);
        if (adInfor == null) {
            PBMobileAds.getInstance().log(LogType.INFOR, "AdInfor of ADInterstitial Placement '" + this.placement + "' is not exist.");
            return;
        }

        PBMobileAds.getInstance().log(LogType.INFOR, "Preload ADInterstitial Placement: " + this.placement);
        PBMobileAds.getInstance().setupPBS(adInfor.host);
        if (isVideo) {
            PBMobileAds.getInstance().log(LogType.DEBUG, "[ADInterstitial Video] - configId: " + adInfor.configId + " | adUnitID: " + adInfor.adUnitID);
            this.adUnit = new VideoInterstitialAdUnit(adInfor.configId);
        } else {
            PBMobileAds.getInstance().log(LogType.DEBUG, "[ADInterstitial HTML] - configId: " + adInfor.configId + " | adUnitID: " + adInfor.adUnitID);
            this.adUnit = new InterstitialAdUnit(adInfor.configId);
        }
        this.amInterstitial = new PublisherInterstitialAd(PBMobileAds.getInstance().getContextApp());
        this.amInterstitial.setAdUnitId(adInfor.adUnitID);
        this.amInterstitial.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();

                isFetchingAD = false;
                PBMobileAds.getInstance().log(LogType.INFOR, "onAdLoaded: ADInterstitial Placement '" + placement + "'");
                if (adDelegate != null) adDelegate.onAdLoaded();
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();

                PBMobileAds.getInstance().log(LogType.INFOR, "onAdOpened: ADInterstitial Placement '" + placement + "'");
                if (adDelegate != null) adDelegate.onAdOpened();
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();

                PBMobileAds.getInstance().log(LogType.INFOR, "onAdClosed: ADInterstitial Placement '" + placement + "'");
                if (adDelegate != null) adDelegate.onAdClosed();
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();

                PBMobileAds.getInstance().log(LogType.INFOR, "onAdClicked: ADInterstitial Placement '" + placement + "'");
                if (adDelegate != null) adDelegate.onAdClicked();
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();

                PBMobileAds.getInstance().log(LogType.INFOR, "onAdLeftApplication: ADInterstitial Placement '" + placement + "'");
                if (adDelegate != null) adDelegate.onAdLeftApplication();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                super.onAdFailedToLoad(errorCode);

                if (errorCode == AdRequest.ERROR_CODE_NO_FILL) {
                    if (!processNoBids()) {
                        isFetchingAD = false;
                        if (adDelegate != null)
                            adDelegate.onAdFailedToLoad("onAdFailedToLoad: ADInterstitial Placement '" + placement + "' with error: " + PBMobileAds.getInstance().getADError(errorCode));
                    }
                } else {
                    isFetchingAD = false;
                    String messErr = "onAdFailedToLoad: ADInterstitial Placement '" + placement + "' with error: " + PBMobileAds.getInstance().getADError(errorCode);
                    PBMobileAds.getInstance().log(LogType.INFOR, messErr);
                    if (adDelegate != null) adDelegate.onAdFailedToLoad(messErr);
                }
            }
        });

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
                PBMobileAds.getInstance().log(LogType.DEBUG, "PBS demand fetch ADInterstitial placement '" + placement + "' for DFP: " + resultCode.name());
                handlerResult(resultCode);
            }
        });
    }

    public void show() {
        if (isReady()) {
            this.amInterstitial.show();
        } else {
            PBMobileAds.getInstance().log(LogType.INFOR, "ADInterstitial Placement '" + this.placement + "' currently unavailable, call preload() first.");
        }
    }

    public void destroy() {
        PBMobileAds.getInstance().log(LogType.INFOR, "Destroy ADInterstitial Placement: " + this.placement);
        this.resetAD();
    }

    public boolean isReady() {
        if (this.adUnit == null || this.amInterstitial == null) return false;
        return this.amInterstitial.isLoaded();
    }

    public void setListener(AdDelegate adDelegate) {
        this.adDelegate = adDelegate;
    }

}
