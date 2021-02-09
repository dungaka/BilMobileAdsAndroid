package com.bil.bilmobileads;

import com.bil.bilmobileads.entity.ADFormat;
import com.bil.bilmobileads.entity.AdInfor;
import com.bil.bilmobileads.entity.AdUnitObj;
import com.bil.bilmobileads.entity.LogType;
import com.bil.bilmobileads.interfaces.AdNativeDelegate;
import com.bil.bilmobileads.interfaces.ResultCallback;
import com.bil.bilmobileads.interfaces.WorkCompleteDelegate;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;

import org.prebid.mobile.NativeAdUnit;
import org.prebid.mobile.NativeDataAsset;
import org.prebid.mobile.NativeEventTracker;
import org.prebid.mobile.NativeImageAsset;
import org.prebid.mobile.NativeTitleAsset;
import org.prebid.mobile.OnCompleteListener;
import org.prebid.mobile.ResultCode;

import java.util.ArrayList;

public class ADNativeCustom {

    // MARK: - View
    private AdNativeDelegate adNativeDelegate;

    // MARK: - AD
    private PublisherAdRequest amRequest;
    private NativeAdUnit adUnit;
    private AdLoader amNativeDFP;
    //    private UnifiedNativeAd unifiedNativeAdObj;
    //    private ADNativeView.Builder builder;

    // MARK: - AD Info
    private String placement;
    private AdUnitObj adUnitObj;

    // MARK: - Properties
    private boolean isFetchingAD = false;
    public static final int MAX_ADS = 5;
    private int curNumOfAds = 0;

    public ADNativeCustom(final String placementStr) {
        if (placementStr == null) {
            PBMobileAds.getInstance().log(LogType.ERROR, "Placement is null");
            throw new NullPointerException();
        }
        PBMobileAds.getInstance().log(LogType.INFOR, "ADNativeCustom placement: " + placementStr + " Init");

        this.placement = placementStr;

        this.getConfigAD();
    }

    // MARK: - Handle AD
    void getConfigAD() {
        this.adUnitObj = PBMobileAds.getInstance().getAdUnitObj(this.placement);
        if (this.adUnitObj == null) {
            this.isFetchingAD = true;

            // Get AdUnit
            PBMobileAds.getInstance().getADConfig(this.placement, new ResultCallback<AdUnitObj, Exception>() {
                @Override
                public void success(AdUnitObj data) {
                    PBMobileAds.getInstance().log(LogType.INFOR, "ADNativeCustom placement: " + placement + " Init Success");
                    isFetchingAD = false;
                    adUnitObj = data;

                    PBMobileAds.getInstance().showCMP(new WorkCompleteDelegate() {
                        @Override
                        public void doWork() {
                            preload();
                        }
                    });
                }

                @Override
                public void failure(Exception error) {
                    PBMobileAds.getInstance().log(LogType.INFOR, "ADNativeCustom placement: " + placement + " Init Failed with Error: " + error.getLocalizedMessage() + ". Please check your internet connect.");
                    isFetchingAD = false;
                }
            });
        } else {
            this.preload();
        }
    }

    void resetAD() {
        if (this.adUnit == null || this.amNativeDFP == null) return;

        this.isFetchingAD = false;

        this.adUnit.stopAutoRefresh();
        this.adUnit = null;

        this.amNativeDFP = null;
    }

    void handlerResult(ResultCode resultCode) {
        if (resultCode == ResultCode.SUCCESS) {
            this.amNativeDFP.loadAd(this.amRequest);
        } else {
            this.isFetchingAD = false;

            if (resultCode == ResultCode.NO_BIDS) {
                PBMobileAds.getInstance().log(LogType.INFOR, "ADNativeCustom Placement '" + this.placement + "' No Bids.");
            } else if (resultCode == ResultCode.TIMEOUT) {
                PBMobileAds.getInstance().log(LogType.INFOR, "ADNativeCustom Placement '" + this.placement + "' Timeout. Please check your internet connect.");
            }
        }
    }

    // MARK: - Public FUNC
    public void preload() {
        PBMobileAds.getInstance().log(LogType.DEBUG, "ADNativeCustom Placement '" + this.placement + "' - isFetchingAD: " + this.isFetchingAD);
        if (this.adUnitObj == null || this.isFetchingAD) {
            if (this.adUnitObj == null && !this.isFetchingAD) {
                PBMobileAds.getInstance().log(LogType.INFOR, "ADNativeCustom placement: " + this.placement + " is not ready to load.");
                this.getConfigAD();
                return;
            }
            return;
        }
        this.resetAD();

        // Check store max native ads
        if (this.curNumOfAds == MAX_ADS) {
            PBMobileAds.getInstance().log(LogType.INFOR, "ADNativeCustom Placement '" + this.placement + "' current store " + this.curNumOfAds + " ads. (Store max " + MAX_ADS + " ads)");
            return;
        }

        // Check Active
        if (!this.adUnitObj.isActive || this.adUnitObj.adInfor.size() <= 0) {
            PBMobileAds.getInstance().log(LogType.INFOR, "ADNativeCustom Placement '" + this.placement + "' is not active or not exist.");
            return;
        }

        // Get AdInfor
        boolean isVideo = this.adUnitObj.defaultFormat == ADFormat.VAST;
        AdInfor adInfor = PBMobileAds.getInstance().getAdInfor(isVideo, this.adUnitObj);
        if (adInfor == null) {
            PBMobileAds.getInstance().log(LogType.INFOR, "AdInfor of ADNativeCustom Placement '" + this.placement + "' is not exist.");
            return;
        }

        PBMobileAds.getInstance().log(LogType.INFOR, "Load ADNativeCustom Placement: " + this.placement);
        PBMobileAds.getInstance().setupPBS(adInfor.host);
        PBMobileAds.getInstance().log(LogType.DEBUG, "[ADNativeCustom] - configID: " + adInfor.configId + " | adUnitID: " + adInfor.adUnitID);
        this.adUnit = new NativeAdUnit(adInfor.configId);
        this.adUnit.setContextType(NativeAdUnit.CONTEXT_TYPE.SOCIAL_CENTRIC);
        this.adUnit.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CONTENT_FEED);
        this.adUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.GENERAL_SOCIAL);

        setupNativeAsset();

        // Create AdLoadder
        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(true)
                .build();
        NativeAdOptions nativeOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();
        this.amNativeDFP = new AdLoader.Builder(PBMobileAds.getInstance().getContextApp(), adInfor.adUnitID)
                .forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                    @Override
                    public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                        curNumOfAds++;
                        isFetchingAD = false;

                        // You must call destroy on old ads when you are done with them, otherwise you will have a memory leak.
                        //  if (unifiedNativeAdObj != null) unifiedNativeAdObj.destroy();
                        //  unifiedNativeAdObj = unifiedNativeAd;

                        // Create AdNativeViewBuider
                        // if (builder != null) {
                        //     builder.destroy();
                        //     builder = null;
                        // }
                        ADNativeView.Builder builder = new ADNativeView.Builder(unifiedNativeAd);

                        PBMobileAds.getInstance().log(LogType.INFOR, "onNativeAdViewLoaded: ADNativeCustom Placement '" + placement + "'");
                        if (adNativeDelegate != null) adNativeDelegate.onNativeViewLoaded(builder);
                    }
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();

                        isFetchingAD = false;
                        PBMobileAds.getInstance().log(LogType.INFOR, "onAdLoaded: ADNativeCustom Placement '" + placement + "'");
                        if (adNativeDelegate != null) adNativeDelegate.onAdLoaded();
                    }

                    @Override
                    public void onAdImpression() {
                        super.onAdImpression();

                        curNumOfAds--;
                        PBMobileAds.getInstance().log(LogType.INFOR, "onAdImpression: ADNativeCustom Placement '" + placement + "'");
                        if (adNativeDelegate != null) adNativeDelegate.onAdImpression();
                    }

                    @Override
                    public void onAdLeftApplication() {
                        super.onAdLeftApplication();

                        PBMobileAds.getInstance().log(LogType.INFOR, "onAdClicked: ADNativeStyle Placement '" + placement + "'");
                        if (adNativeDelegate != null) adNativeDelegate.onAdClicked();
                    }

                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        super.onAdFailedToLoad(errorCode);

                        isFetchingAD = false;
                        String messErr = "onAdFailedToLoad: ADNativeCustom Placement '" + placement + "' with error: " + PBMobileAds.getInstance().getADError(errorCode);
                        PBMobileAds.getInstance().log(LogType.INFOR, messErr);
                        if (adNativeDelegate != null) adNativeDelegate.onAdFailedToLoad(messErr);
                    }
                })
                .withNativeAdOptions(nativeOptions)
                .build();

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
                PBMobileAds.getInstance().log(LogType.DEBUG, "PBS demand fetch ADNativeCustom placement '" + placement + "' for DFP: " + resultCode.name());
                handlerResult(resultCode);
            }
        });
    }

    public void destroy() {
        PBMobileAds.getInstance().log(LogType.INFOR, "Destroy ADNativeCustom Placement: " + this.placement);
        this.resetAD();
        //  if (unifiedNativeAdObj != null) {
        //      unifiedNativeAdObj.destroy();
        //      unifiedNativeAdObj = null;
        //  }
    }

    public void setListener(AdNativeDelegate adNativeDelegate) {
        this.adNativeDelegate = adNativeDelegate;
    }

    public int numOfAds() {
        return this.curNumOfAds;
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
            PBMobileAds.getInstance().log(LogType.ERROR, e.getLocalizedMessage());
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
}

