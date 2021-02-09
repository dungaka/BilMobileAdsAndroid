package com.bil.bilmobileads.unity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bil.bilmobileads.ADBanner;
import com.bil.bilmobileads.interfaces.AdDelegate;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class UADBanner {

    private ADBanner banner;

    /**
     * The {@code Activity} on which the banner will display.
     */
    private Activity unityActivity;

    /**
     * A listener implemented in Unity via AndroidJavaProxy to receive ad events.
     */
    private UADListener adListener;

    /**
     * The {@link RelativeLayout} to display ad.
     */
    private RelativeLayout adPlaceholder;

    /**
     * A {@code View.OnLayoutChangeListener} used to detect orientation changes and reposition banner
     * ads as required.
     */
    private View.OnLayoutChangeListener viewChangeListener;

    /**
     * A code indicating where to place the ad.
     * TopCenter = 0,
     * TopLeft = 1,
     * TopRight = 2,
     * BottomCenter = 3,
     * BottomLeft = 4,
     * BottomRight = 5,
     * Center = 6
     */
    private int positionCode;

    /**
     * A boolean indicating whether the ad has been hidden.
     * true -> ad is off, false -> ad is on screen.
     */
    private boolean isHidden;

    /**
     * Whether or not the {@link ADBanner} is ready to be shown.
     */
    private boolean isLoaded;

    public UADBanner(Activity unityActivity, UADListener adListener) {
        this.unityActivity = unityActivity;
        this.adListener = adListener;
        this.isLoaded = false;
        this.isHidden = false;
    }

    public boolean isPluginReady() {
        if (banner == null) {
            Log.d(Utils.LOGTAG, "ADBanner is null. You need init ADBanner first.");
            return false;
        }
        return true;
    }

    /**
     * Creates an {@link ADBanner}.
     *
     * @param adUnitId Your banner ad unit ID.
     * @param position Your banner ad position.
     */
    public void create(final String adUnitId, final int position) {
        unityActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                positionCode = position;

                createADPlaceholder();
                unityActivity.addContentView(adPlaceholder,
                        new LinearLayout.LayoutParams(
                                FrameLayout.LayoutParams.WRAP_CONTENT,
                                FrameLayout.LayoutParams.WRAP_CONTENT));

                viewChangeListener = new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        boolean viewBoundsChanged = left != oldLeft || right != oldRight || bottom != oldBottom || top != oldTop;
                        if (!viewBoundsChanged) return;
                        if (!isHidden) updateADPosition();
                    }
                };
                unityActivity.getWindow()
                        .getDecorView()
                        .getRootView()
                        .addOnLayoutChangeListener(viewChangeListener);

                banner = new ADBanner(adPlaceholder, adUnitId);
                banner.setListener(new AdDelegate() {
                    @Override
                    public void onAdLoaded() {
                        isLoaded = true;

                        updateADPosition();
                        if (adListener != null) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    if (adListener != null) adListener.onAdLoaded();
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
                                    if (adListener != null) adListener.onAdOpened();
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
                                    if (adListener != null) adListener.onAdClosed();
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
                                    if (adListener != null) adListener.onAdClicked();
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
                                    if (adListener != null) adListener.onAdLeftApplication();
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
                                    if (adListener != null) adListener.onAdFailedToLoad(errorCode);
                                }
                            }).start();
                        }
                    }
                });
            }
        });
    }

    /**
     * Load an {@link ADBanner}.
     */
    public void load() {
        if (!isPluginReady()) return;

        unityActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(Utils.LOGTAG, "Calling Load() ADBanner");
                banner.load();
            }
        });
    }

    /**
     * Show the {@link ADBanner} if it has loaded.
     */
    public void show() {
        if (!isPluginReady()) return;

        unityActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(Utils.LOGTAG, "Calling show() ADBanner");
                isHidden = false;
                adPlaceholder.setVisibility(View.VISIBLE);
                updateADPosition();

                banner.startFetchData();
            }
        });
    }

    /**
     * Hide the {@link ADBanner} if it has loaded.
     */
    public void hide() {
        if (!isPluginReady()) return;

        unityActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(Utils.LOGTAG, "Calling hide() ADBanner");
                isHidden = true;
                adPlaceholder.setVisibility(View.GONE);

                banner.stopFetchData();
            }
        });
    }

    /**
     * Destroys the {@link ADBanner}.
     * <p>
     * Currently there is no banner.destroy() method. This method is a placeholder in case
     * there is any cleanup to do here in the future.
     */
    public void destroy() {
        if (!isPluginReady()) return;

        unityActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(Utils.LOGTAG, "Calling Destroy() ADBanner");

                adPlaceholder.removeAllViews();
                adPlaceholder.setVisibility(View.GONE);
                banner.destroy();
                banner = null;
            }
        });
        unityActivity.getWindow()
                .getDecorView()
                .getRootView()
                .removeOnLayoutChangeListener(viewChangeListener);
    }

    public void setPosition(final int adPosition) {
        if (!isPluginReady()) return;

        unityActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                positionCode = adPosition;
                updateADPosition();
            }
        });
    }

    public int getWidthInPixels() {
        if (!isPluginReady()) return 0;

        FutureTask<Integer> task = new FutureTask<>(
                new Callable<Integer>() {
                    @Override
                    public Integer call() {
                        return banner.getWidthInPixels();
                    }
                });
        unityActivity.runOnUiThread(task);

        int result = -1;
        try {
            result = task.get();
        } catch (InterruptedException e) {
            Log.e(Utils.LOGTAG, String.format("Failed to get ad width: %s", e.getLocalizedMessage()));
        } catch (ExecutionException e) {
            Log.e(Utils.LOGTAG, String.format("Failed to get ad width: %s", e.getLocalizedMessage()));
        }
        return result;
    }

    public int getHeightInPixels() {
        if (!isPluginReady()) return 0;

        FutureTask<Integer> task = new FutureTask<>(
                new Callable<Integer>() {
                    @Override
                    public Integer call() {
                        return banner.getHeightInPixels();
                    }
                });
        unityActivity.runOnUiThread(task);

        int result = -1;
        try {
            result = task.get();
        } catch (InterruptedException e) {
            Log.e(Utils.LOGTAG, String.format("Failed to get ad height: %s", e.getLocalizedMessage()));
        } catch (ExecutionException e) {
            Log.e(Utils.LOGTAG, String.format("Failed to get ad height: %s", e.getLocalizedMessage()));
        }
        return result;
    }

    // Private Func
    private void createADPlaceholder() {
        // Create a RelativeLayout and add the ad view to it
        if (this.adPlaceholder == null) {
            this.adPlaceholder = new RelativeLayout(this.unityActivity);
        } else {
            // Remove the layout if it has a parent
            FrameLayout parentView = (FrameLayout) this.adPlaceholder.getParent();
            if (parentView != null)
                parentView.removeView(this.adPlaceholder);
        }
        this.adPlaceholder.setBackgroundColor(Color.TRANSPARENT);
        this.adPlaceholder.setVisibility(View.VISIBLE);

        this.updateADPosition();
    }

    private void updateADPosition() {
        if (adPlaceholder == null || isHidden) return;

        unityActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adPlaceholder.setLayoutParams(getLayoutParams());
            }
        });
    }

    /**
     * Create layout params for the ad view with relevant positioning details.
     *
     * @return configured {@link FrameLayout.LayoutParams }.
     */
    private FrameLayout.LayoutParams getLayoutParams() {
        Insets insets = getSafeInsets();

        final FrameLayout.LayoutParams adParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        adParams.gravity = Utils.getGravityForPositionCode(positionCode);
        adParams.bottomMargin = insets.bottom;
        adParams.rightMargin = insets.right;
        adParams.leftMargin = insets.left;
        if (positionCode == Utils.TOP_CENTER || positionCode == Utils.TOP_LEFT || positionCode == Utils.TOP_RIGHT) {
            adParams.topMargin = insets.top;
        }

        return adParams;
    }

    /**
     * Class to hold the insets of the cutout area.
     */
    private static class Insets {
        int top = 0;
        int bottom = 0;
        int left = 0;
        int right = 0;
    }

    private Insets getSafeInsets() {
        Insets insets = new Insets();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return insets;
        }

        Window window = unityActivity.getWindow();
        if (window == null) return insets;

        WindowInsets windowInsets = window.getDecorView().getRootWindowInsets();
        if (windowInsets == null) return insets;

        DisplayCutout displayCutout = windowInsets.getDisplayCutout();
        if (displayCutout == null) return insets;

        insets.top = displayCutout.getSafeInsetTop();
        insets.left = displayCutout.getSafeInsetLeft();
        insets.bottom = displayCutout.getSafeInsetBottom();
        insets.right = displayCutout.getSafeInsetRight();

        return insets;
    }
}
