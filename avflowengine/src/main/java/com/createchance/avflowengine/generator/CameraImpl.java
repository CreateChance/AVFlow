package com.createchance.avflowengine.generator;

import android.view.MotionEvent;

import java.util.List;

public abstract class CameraImpl {
    protected static final String TAG = "Camera";
    public static final int VERSION_ONE = 1;
    public static final int VERSION_TWO = 2;

    public static final int FACING_FRONT = 1;
    public static final int FACING_BACK = 2;

    public static final int RESULT_OK = 0;
    public static final int ERROR_NO_CAMERA = -1;
    public static final int ERROR_OP_UNSUPPORTED = -2;
    public static final int ERROR_UNKNOW = -3;

    public static final int FLASH_MODE_OFF = 1;
    public static final int FLASH_MODE_TORCH = 2;

    public static final int FOCUS_MODE_AUTO = 1;
    public static final int FOCUS_MODE_CONTINUOUS_VIDEO = 2;
    public static final int FOCUS_MODE_EDOF = 3;
    public static final int FOCUS_MODE_FIXED = 4;
    public static final int FOCUS_MODE_INFINITY = 5;
    public static final int FOCUS_MODE_MACRO = 6;

    public static final int WHITE_BALANCE_AUTO = 1;
    public static final int WHITE_BALANCE_CLOUDY_DAYLIGHT = 2;
    public static final int WHITE_BALANCE_DAYLIGHT = 3;
    public static final int WHITE_BALANCE_FLUORESCENT = 4;
    public static final int WHITE_BALANCE_INCANDESCENT = 5;
    public static final int WHITE_BALANCE_SHADE = 6;
    public static final int WHITE_BALANCE_TWILIGHT = 7;
    public static final int WHITE_BALANCE_WARM_FLUORESCENT = 8;

    public static final int ORIENTATION_0 = 0;
    public static final int ORIENTATION_90 = 90;
    public static final int ORIENTATION_180 = 180;
    public static final int ORIENTATION_270 = 270;

    public abstract int getVersion();

    public abstract int startPreview(int facing);

    public abstract int stopPreview();

    public abstract int getFlashLightMode();

    public abstract int setFlashLightMode(int state);

    public abstract int setAspectRatio(AspectRatio ratio);

    public abstract AspectRatio getAspectRatio();

    public abstract int setFocusMode(int mode);

    public abstract int getFocusMode();

    public abstract int triggerFocus(MotionEvent event);

    public abstract int zoom(boolean isZoomIn);

    public abstract int getZoom();

    public abstract int getMaxZoom();

    public abstract boolean isCameraOpened();

    public abstract int setFacing(int cameraId);

    public abstract int getFacing();

    public abstract int setWhiteBalanceMode(int mode);

    public abstract int getWhiteBalanceMode();

    public abstract int setExposureCompensation(int value);

    public abstract int getExposureCompensation();

    public abstract List<Integer> getExposureCompensationRange();

    public abstract float getExposureCompensationStep();

    public abstract int setDisplayOrientation(int displayOrientation);
}
