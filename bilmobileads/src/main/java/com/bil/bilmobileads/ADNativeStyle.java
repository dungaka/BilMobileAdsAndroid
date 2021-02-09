package com.bil.bilmobileads;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.view.ViewGroup;

import com.bil.bilmobileads.entity.ADFormat;
import com.bil.bilmobileads.entity.AdInfor;
import com.bil.bilmobileads.entity.AdUnitObj;
import com.bil.bilmobileads.entity.LogType;
import com.bil.bilmobileads.interfaces.AdNativeDelegate;
import com.bil.bilmobileads.interfaces.ResultCallback;
import com.bil.bilmobileads.interfaces.WorkCompleteDelegate;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;

import org.prebid.mobile.NativeAdUnit;
import org.prebid.mobile.NativeDataAsset;
import org.prebid.mobile.NativeEventTracker;
import org.prebid.mobile.NativeImageAsset;
import org.prebid.mobile.NativeTitleAsset;
import org.prebid.mobile.OnCompleteListener;
import org.prebid.mobile.ResultCode;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ADNativeStyle implements Application.ActivityLifecycleCallbacks {

    // MARK: - View
    private ViewGroup adView;
    private AdNativeDelegate adDelegate;

    // MARK: - AD
    private PublisherAdRequest amRequest;
    private NativeAdUnit adUnit;
    private PublisherAdView amNative;
    // MARK: - AD Info
    private String placement;
    private AdUnitObj adUnitObj;

    // MARK: - Properties
    private ADFormat adFormatDefault;

    private boolean isLoadNativeSucc = false;
    private boolean setDefaultBidType = true;
    private boolean isFetchingAD = false;
    private Application application;

    public ADNativeStyle(ViewGroup adView, final String placementStr) {
        if (adView == null || placementStr == null) {
            PBMobileAds.getInstance().log(LogType.ERROR, "AdView Placeholder or placement is null");
            throw new NullPointerException();
        }
        PBMobileAds.getInstance().log(LogType.INFOR, "ADNativeStyle placement: " + placementStr + " Init");

        this.adView = adView;
        this.placement = placementStr;

        this.getConfigAD();
    }

    // MARK: - Handler AD
    void getConfigAD() {
        this.adUnitObj = PBMobileAds.getInstance().getAdUnitObj(this.placement);
        if (this.adUnitObj == null) {
            this.isFetchingAD = true;
//            registerlifecycle();

            // Get AdUnit Info
            PBMobileAds.getInstance().getADConfig(this.placement, new ResultCallback<AdUnitObj, Exception>() {
                @Override
                public void success(AdUnitObj data) {
                    PBMobileAds.getInstance().log(LogType.INFOR, "ADNativeStyle placement: " + placement + " Init Success");
                    isFetchingAD = false;
                    adUnitObj = data;

                    PBMobileAds.getInstance().showCMP(new WorkCompleteDelegate() {
                        @Override
                        public void doWork() {
                            application.registerActivityLifecycleCallbacks(ADNativeStyle.this);
                            // Setup Application Delegate
                            load();
                        }
                    });
                }

                @Override
                public void failure(Exception error) {
                    PBMobileAds.getInstance().log(LogType.INFOR, "ADNativeStyle placement: " + placement + " Init Failed with Error: " + error.getLocalizedMessage() + ". Please check your internet connect.");
                    isFetchingAD = false;
                    destroy();
                }
            });
        } else {
            this.load();
        }
    }

    boolean processNoBids() {
        if (this.adUnitObj.adInfor.size() >= 2 && this.adFormatDefault == this.adUnitObj.defaultFormat) {
            this.setDefaultBidType = false;
            this.load();

            return true;
        } else {
            // Both or .video, .html is no bids -> wait and preload.
            PBMobileAds.getInstance().log(LogType.INFOR, "ADNativeStyle Placement '" + this.placement + "' No Bids.");
            return false;
        }
    }

    void resetAD() {
        if (this.adUnit == null || this.amNative == null) return;

        this.isFetchingAD = false;
        this.isLoadNativeSucc = false;

        this.adUnit.stopAutoRefresh();
        this.adUnit = null;

        this.amNative.destroy();
        this.amNative.setAdListener(null);
        this.amNative = null;
    }

    void handlerResult(ResultCode resultCode) {
        if (resultCode == ResultCode.SUCCESS) {
            this.amNative.loadAd(this.amRequest);
        } else {
            if (resultCode == ResultCode.NO_BIDS) {
                this.processNoBids();
            } else if (resultCode == ResultCode.TIMEOUT) {
                PBMobileAds.getInstance().log(LogType.INFOR, "ADNativeStyle Placement '" + this.placement + "' Timeout. Please check your internet connect.");
            }
        }
    }

    // MARK: - Public FUNC
    public void load() {
        PBMobileAds.getInstance().log(LogType.DEBUG, "ADNativeStyle Placement '" + this.placement + "' - isLoaded: " + this.isLoaded() + " | isFetchingAD: " + this.isFetchingAD);
        if (this.adView == null) {
            PBMobileAds.getInstance().log(LogType.ERROR, "ADNativeStyle placement: " + this.placement + ", AdView Placeholder is null.");
            return;
        }

        if (this.adUnitObj == null || this.isLoaded() || this.isFetchingAD) {
            if (this.adUnitObj == null && !this.isFetchingAD) {
                PBMobileAds.getInstance().log(LogType.INFOR, "ADNativeStyle placement: " + this.placement + " is not ready to load.");
                this.getConfigAD();
                return;
            }
            return;
        }
        this.resetAD();

        // Check Active
        if (!this.adUnitObj.isActive || this.adUnitObj.adInfor.size() <= 0) {
            PBMobileAds.getInstance().log(LogType.INFOR, "AdNativeStyle Placement '" + this.placement + "' is not active or not exist.");
            return;
        }

        // Check and Set Default
        this.adFormatDefault = this.adUnitObj.defaultFormat;
        if (!this.setDefaultBidType && this.adUnitObj.adInfor.size() >= 2) {
            this.adFormatDefault = this.adFormatDefault == ADFormat.VAST ? ADFormat.HTML : ADFormat.VAST;
            this.setDefaultBidType = true;
        }

        // Get AdInfor
        boolean isVideo = this.adFormatDefault == ADFormat.VAST;
        AdInfor adInfor = PBMobileAds.getInstance().getAdInfor(isVideo, this.adUnitObj);
        if (adInfor == null) {
            PBMobileAds.getInstance().log(LogType.INFOR, "AdInfor of ADNativeStyle Placement '" + this.placement + "' is not exist.");
            return;
        }

        PBMobileAds.getInstance().log(LogType.INFOR, "Load ADNativeStyle Placement: " + this.placement);
        PBMobileAds.getInstance().setupPBS(adInfor.host);
        PBMobileAds.getInstance().log(LogType.DEBUG, "[ADNativeStyle] - configID: " + adInfor.configId + " | adUnitID: " + adInfor.adUnitID);
        this.adUnit = new NativeAdUnit(adInfor.configId);
        this.adUnit.setContextType(NativeAdUnit.CONTEXT_TYPE.SOCIAL_CENTRIC);
        this.adUnit.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CONTENT_FEED);
        this.adUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.GENERAL_SOCIAL);

        // Set auto refresh time | refreshTime is -> sec
        this.startFetchData();

        setupNativeAsset();

        // Create AdView
        this.amNative = new PublisherAdView(PBMobileAds.getInstance().getContextApp());
        this.amNative.setAdUnitId(adInfor.adUnitID);
        this.amNative.setAdSizes(AdSize.FLUID);
        this.amNative.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();

                isFetchingAD = false;
                isLoadNativeSucc = true;
                PBMobileAds.getInstance().log(LogType.INFOR, "onAdLoaded: ADNativeStyle Placement '" + placement + "'");
                if (adDelegate != null) adDelegate.onAdLoaded();
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();

                PBMobileAds.getInstance().log(LogType.INFOR, "onAdClicked: ADNativeStyle Placement '" + placement + "'");
                if (adDelegate != null) adDelegate.onAdClicked();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                super.onAdFailedToLoad(errorCode);
                isLoadNativeSucc = false;

                if (errorCode == AdRequest.ERROR_CODE_NO_FILL) {
                    if (!processNoBids()) {
                        isFetchingAD = false;
                        if (adDelegate != null)
                            adDelegate.onAdFailedToLoad("onAdFailedToLoad: ADNativeStyle Placement '" + placement + "' with error: " + PBMobileAds.getInstance().getADError(errorCode));
                    }
                } else {
                    isFetchingAD = false;
                    String messErr = "onAdFailedToLoad: ADNativeStyle Placement '" + placement + "' with error: " + PBMobileAds.getInstance().getADError(errorCode);
                    PBMobileAds.getInstance().log(LogType.INFOR, messErr);
                    if (adDelegate != null) adDelegate.onAdFailedToLoad(messErr);
                }
            }
        });

        // Add AD to view
        this.adView.removeAllViews();
        this.adView.addView(this.amNative);

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
                PBMobileAds.getInstance().log(LogType.DEBUG, "PBS demand fetch ADNativeStyle placement '" + placement + "' for DFP: " + resultCode.name());
                handlerResult(resultCode);
            }
        });
    }

    public void destroy() {
        ((Activity) PBMobileAds.getInstance().getContextApp()).getApplication().unregisterActivityLifecycleCallbacks(this);

        if (this.adUnit == null) return;
        PBMobileAds.getInstance().log(LogType.INFOR, "Destroy ADNativeStyle Placement: " + this.placement);
        this.resetAD();
    }

    public void setListener(AdNativeDelegate adDelegate) {
        this.adDelegate = adDelegate;
    }

    public int getWidth() {
        if (this.adUnit == null) return 0;
        return this.amNative.getWidth();
    }

    public int getHeight() {
        if (this.adUnit == null) return 0;
        return this.amNative.getHeight();
    }

    public boolean isLoaded() {
        return this.isLoadNativeSucc;
    }

    public void startFetchData() {
        if (this.adUnit == null) return;

        if (this.adUnitObj.refreshTime > 0) {
            this.adUnit.setAutoRefreshPeriodMillis(this.adUnitObj.refreshTime * 1000); // convert sec to milisec
        }
    }

    public void stopFetchData() {
        if (this.adUnit == null) return;
        this.adUnit.setAutoRefreshPeriodMillis(600000 * 1000);
    }

    // MARK: - Private FUNC
    void setupNativeAsset() {
        // Add event trackers requirements, this is required
        ArrayList<NativeEventTracker.EVENT_TRACKING_METHOD> methods = new ArrayList<>();
        methods.add(NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE);
        methods.add(NativeEventTracker.EVENT_TRACKING_METHOD.JS);
        try {
            NativeEventTracker tracker = new NativeEventTracker(NativeEventTracker.EVENT_TYPE.IMPRESSION, methods);
            this.adUnit.addEventTracker(tracker);
        } catch (Exception e) {
            e.printStackTrace();
            PBMobileAds.getInstance().log(LogType.DEBUG, e.getLocalizedMessage());
        }
        // Require a title asset
        NativeTitleAsset title = new NativeTitleAsset();
        title.setLength(90);
        title.setRequired(true);
        this.adUnit.addAsset(title);
        // Require an icon asset
        NativeImageAsset icon = new NativeImageAsset();
        icon.setImageType(NativeImageAsset.IMAGE_TYPE.ICON);
        icon.setWMin(20);
        icon.setHMin(20);
        icon.setRequired(true);
        this.adUnit.addAsset(icon);
        // Require an main image asset
        NativeImageAsset image = new NativeImageAsset();
        image.setImageType(NativeImageAsset.IMAGE_TYPE.MAIN);
        image.setHMin(200);
        image.setWMin(200);
        image.setRequired(true);
        this.adUnit.addAsset(image);
        // Require sponsored text
        NativeDataAsset data = new NativeDataAsset();
        data.setLen(90);
        data.setDataType(NativeDataAsset.DATA_TYPE.SPONSORED);
        data.setRequired(true);
        this.adUnit.addAsset(data);
        // Require main description
        NativeDataAsset body = new NativeDataAsset();
        body.setRequired(true);
        body.setDataType(NativeDataAsset.DATA_TYPE.DESC);
        this.adUnit.addAsset(body);
        // Require call to action
        NativeDataAsset cta = new NativeDataAsset();
        cta.setRequired(true);
        cta.setDataType(NativeDataAsset.DATA_TYPE.CTATEXT);
        this.adUnit.addAsset(cta);
    }

    // MARK: - Application Delegate
    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        String activityName = activity.getClass().getSimpleName();
        if (activityName.equalsIgnoreCase("CMPConsentToolActivity")
                || activityName.equalsIgnoreCase("AdActivity"))
            return;

        this.startFetchData();
        if (this.adUnit != null)
            PBMobileAds.getInstance().log(LogType.DEBUG, "onForeground: ADNativeStyle Placement '" + this.placement + "'");
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        String activityName = activity.getClass().getSimpleName();
        if (activityName.equalsIgnoreCase("CMPConsentToolActivity")
                || activityName.equalsIgnoreCase("AdActivity"))
            return;

        this.stopFetchData();
        if (this.adUnit != null)
            PBMobileAds.getInstance().log(LogType.DEBUG, "onBackground: ADNativeStyle Placement '" + this.placement + "'");
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }
}

