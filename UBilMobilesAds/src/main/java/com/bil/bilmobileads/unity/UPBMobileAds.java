package com.bil.bilmobileads.unity;

import android.app.Activity;

import com.bil.bilmobileads.PBMobileAds;

public class UPBMobileAds {

    private Activity unityActivity;

    public UPBMobileAds(Activity unityActivity) {
        this.unityActivity = unityActivity;
    }

    public void initialize(final boolean testMode) {
        unityActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PBMobileAds.getInstance().initialize(unityActivity, testMode);
            }
        });
    }

    public void enableCOPPA() {
        unityActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PBMobileAds.getInstance().enableCOPPA();
            }
        });
    }

    public void disableCOPPA() {
        unityActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PBMobileAds.getInstance().disableCOPPA();
            }
        });
    }

    public void setYearOfBirth(final int yearOfBirth) throws Exception {
        unityActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    PBMobileAds.getInstance().setYearOfBirth(yearOfBirth);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void setGender(final int gender) {
        unityActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (gender) {
                    case 0: // Unknown
                        PBMobileAds.getInstance().setGender(PBMobileAds.GENDER.UNKNOWN);
                        break;
                    case 1: // Male
                        PBMobileAds.getInstance().setGender(PBMobileAds.GENDER.MALE);
                        break;
                    case 2: // Female
                        PBMobileAds.getInstance().setGender(PBMobileAds.GENDER.FEMALE);
                        break;
                }
            }
        });
    }
}
