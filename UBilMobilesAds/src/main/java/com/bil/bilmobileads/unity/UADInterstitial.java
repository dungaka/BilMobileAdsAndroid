package com.bil.bilmobileads.unity;

import android.app.Activity;
import android.util.Log;

import com.bil.bilmobileads.ADInterstitial;
import com.bil.bilmobileads.interfaces.AdDelegate;

public class UADInterstitial {

    private ADInterstitial interstitial;

    /**
     * The {@code Activity} on which the interstitial will display.
     */
    private Activity unityActivity;

    /**
     * A listener implemented in Unity via AndroidJavaProxy to receive ad events.
     */
    private UADListener adListener;

    /**
     * Whether or not the {@link ADInterstitial} is ready to be shown.
     */
    private boolean isLoaded;

    public UADInterstitial(Activity unityActivity, UADListener adListener) {
        this.unityActivity = unityActivity;
        this.adListener = adListener;
        this.isLoaded = false;
    }

    public boolean isPluginReady() {
        if (interstitial == null) {
            Log.d(Utils.LOGTAG, "ADInterstitial is null. You need init ADInterstitial first.");
            return false;
        }
        return true;
    }

    /**
     * Creates an {@link ADInterstitial}.
     *
     * @param adUnitId Your interstitial ad unit ID.
     */
    public void create(final String adUnitId) {
        unityActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                interstitial = new ADInterstitial(adUnitId);
                interstitial.setListener(new AdDelegate() {
                    @Override
                    public void onAdLoaded() {
                        isLoaded = true;
                        if (adListener != null) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    if (adListener != null) {
                                        adListener.onAdLoaded();
                                    }
                                }
                            }).start();
                        }
                    }

                    @Override
                    public void onAdOpened() {
                        if (adListener != null) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    if (adListener != null) {
                                        adListener.onAdOpened();
                                    }
                                }
                            }).start();
                        }
                    }

                    @Override
                    public void onAdClosed() {
                        if (adListener != null) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    if (adListener != null) {
                                        adListener.onAdClosed();
                                    }
                                }
                            }).start();
                        }
                    }

                    @Override
                    public void onAdClicked() {
                        if (adListener != null) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    if (adListener != null) {
                                        adListener.onAdClicked();
                                    }
                                }
                            }).start();
                        }
                    }

                    @Override
                    public void onAdLeftApplication() {
                        if (adListener != null) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    if (adListener != null) {
                                        adListener.onAdLeftApplication();
                                    }
                                }
                            }).start();
                        }
                    }

                    @Override
                    public void onAdFailedToLoad(final String errorCode) {
                        if (adListener != null) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    if (adListener != null) {
                                        adListener.onAdFailedToLoad(errorCode);
                                    }
                                }
                            }).start();
                        }
                    }
                });
            }
        });
    }

    /**
     * preLoads an {@link ADInterstitial}.
     */
    public void preLoad() {
        if (!isPluginReady()) return;

        unityActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(Utils.LOGTAG, "Calling preLoad() ADInterstitial");

                interstitial.preLoad();
            }
        });
    }

    /**
     * Load the {@link ADInterstitial} if it has loaded.
     */
    public void show() {
        if (!isPluginReady()) return;

        unityActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(Utils.LOGTAG, "Calling show() ADInterstitial");

                if (interstitial.isReady()) {
                    isLoaded = false;
                    interstitial.show();
                } else {
                    Log.d(Utils.LOGTAG, "ADInterstitial was not ready to be shown.");
                }
            }
        });
    }

    /**
     * Destroys the {@link ADInterstitial}.
     * <p>
     * Currently there is no interstitial.destroy() method. This method is a placeholder in case
     * there is any cleanup to do here in the future.
     */
    public void destroy() {
        if (!isPluginReady()) return;

        unityActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(Utils.LOGTAG, "Calling destroy() ADInterstitial");

                interstitial.destroy();
                interstitial = null;
            }
        });
    }

    /**
     * Returns {@code True} if the {@link ADInterstitial} has loaded.
     */
    public boolean isReady() {
        return isLoaded;
    }
}
