package com.bil.bilmobileads.unity;

import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.Gravity;

/**
 * Utilities for the Google Mobile Ads Unity plugin.
 */
public class Utils {

    /**
     * Tag used for logging statements.
     */
    public static final String LOGTAG = "uPBMobileAds";

    /**
     * Position constant for top of the screen.
     */
    public static final int TOP_CENTER = 0;

    /**
     * Position constant for top-left of the screen.
     */
    public static final int TOP_LEFT = 1;

    /**
     * Position constant for top-right of the screen.
     */
    public static final int TOP_RIGHT = 2;

    /**
     * Position constant for bottom of the screen.
     */
    public static final int BOTTOM_CENTER = 3;

    /**
     * Position constant for bottom-left of the screen.
     */
    public static final int BOTTOM_LEFT = 4;

    /**
     * Position constant bottom-right of the screen.
     */
    public static final int BOTTOM_RIGHT = 5;

    /**
     * Position constant center of the screen.
     */
    public static final int CENTER = 6;

    public static int getGravityForPositionCode(int positionCode) {
        int gravity = 0;
        switch (positionCode) {
            case Utils.TOP_CENTER:
                gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                break;
            case Utils.TOP_LEFT:
                gravity = Gravity.TOP | Gravity.LEFT;
                break;
            case Utils.TOP_RIGHT:
                gravity = Gravity.TOP | Gravity.RIGHT;
                break;
            case Utils.BOTTOM_CENTER:
                gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                break;
            case Utils.BOTTOM_LEFT:
                gravity = Gravity.BOTTOM | Gravity.LEFT;
                break;
            case Utils.BOTTOM_RIGHT:
                gravity = Gravity.BOTTOM | Gravity.RIGHT;
                break;
            case Utils.CENTER:
                gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;
                break;
            default:
                throw new IllegalArgumentException("Attempted to position ad with invalid ad "
                        + "position.");
        }
        return gravity;
    }

    public static float convertPixelsToDp(float px) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return px / metrics.density;
    }

    public static float convertDpToPixel(float dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return dp * metrics.density;
    }
}
