package com.bil.bilmobileads.unity;

import android.app.Activity;
import android.util.Log;

import com.bil.bilmobileads.ADRewarded;
import com.bil.bilmobileads.entity.ADRewardItem;
import com.bil.bilmobileads.interfaces.AdRewardedDelegate;

public class UADRewarded {

    private ADRewarded rewarded;

    /**
     * The {@code Activity} on which the rewarded will display.
     */
    private Activity unityActivity;

    /**
     * A listener implemented in Unity via AndroidJavaProxy to receive ad events.
     */
    private UADRewardedListener adListener;

    /**
     * Whether or not the {@link ADRewarded} is ready to be shown.
     */
    private boolean isLoaded;

    public UADRewarded(Activity unityActivity, UADRewardedListener adListener) {
        this.unityActivity = unityActivity;
        this.adListener = adListener;
        this.isLoaded = false;
    }

    public boolean isPluginReady() {
        if (rewarded == null) {
            Log.d(Utils.LOGTAG, "ADRewarded is null. You need init ADRewarded first.");
            return false;
        }
        return true;
    }

    /**
     * Creates an {@link ADRewarded}.
     *
     * @param adUnitId Your rewarded ad unit ID.
     */
    public void create(final String adUnitId) {
        unityActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                rewarded = new ADRewarded(unityActivity, adUnitId);
                rewarded.setListener(new AdRewardedDelegate() {
                    @Override
                    public void onRewardedAdLoaded() {
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
                    public void onRewardedAdOpened() {
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
                    public void onRewardedAdClosed() {
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
                    public void onUserEarnedReward(final ADRewardItem adRewardItem) {
                        if (adListener != null) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    if (adListener != null) {
                                        adListener.onUserEarnedReward(adRewardItem.getType(), adRewardItem.getAmount());
                                    }
                                }
                            }).start();
                        }
                    }

                    @Override
                    public void onRewardedAdFailedToLoad(final String error) {
                        if (adListener != null) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    if (adListener != null) {
                                        adListener.onAdFailedToLoad(error);
                                    }
                                }
                            }).start();
                        }
                    }

                    @Override
                    public void onRewardedAdFailedToShow(final String error) {
                        if (adListener != null) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    if (adListener != null) {
                                        adListener.onAdFailedToShow(error);
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
     * preLoads an {@link ADRewarded}.
     */
    public void preLoad() {
        if (!isPluginReady()) return;

        unityActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(Utils.LOGTAG, "Calling preLoad() ADRewarded");
                rewarded.preLoad();
            }
        });
    }

    /**
     * Load the {@link ADRewarded} if it has loaded.
     */
    public void show() {
        if (!isPluginReady()) return;

        unityActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(Utils.LOGTAG, "Calling show() ADRewarded");

                if (rewarded.isReady()) {
                    isLoaded = false;
                    rewarded.show();
                } else {
                    Log.d(Utils.LOGTAG, "ADRewarded was not ready to be shown.");
                }
            }
        });
    }

    /**
     * Destroys the {@link ADRewarded}.
     * <p>
     * Currently there is no rewarded.destroy() method. This method is a placeholder in case
     * there is any cleanup to do here in the future.
     */
    public void destroy() {
        if (!isPluginReady()) return;

        unityActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(Utils.LOGTAG, "Calling destroy() ADRewarded");

                rewarded.destroy();
                rewarded = null;
            }
        });
    }

    /**
     * Returns {@code True} if the {@link ADRewarded} has loaded.
     */
    public boolean isReady() {
        return isLoaded;
    }
}