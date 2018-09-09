package com.createchance.avflowengine.generator;

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.MotionEvent;

import com.createchance.avflowengine.base.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Camera impl for v1 api before api 21.
 *
 * @author createchance
 * @date 2018-09-03
 */
@TargetApi(20)
class CameraV1 extends CameraImpl {

    private Camera mCamera;
    private List<CameraDeviceInfo> mDeviceList = new ArrayList<>();
    private int mCurrentDevicePos = -1;

    private final SurfaceTexture mSurface;

    private boolean mIsPreviewing;

    CameraV1(SurfaceTexture surface) {
        mSurface = surface;
        init();
    }

    @Override
    public int getVersion() {
        return VERSION_ONE;
    }

    @Override
    public int startPreview(int facing) {
        if (mDeviceList.size() == 0) {
            Logger.e(TAG, "No camera device in this device!");
            return ERROR_NO_CAMERA;
        }

        if (facing != CameraImpl.FACING_FRONT &&
                facing != CameraImpl.FACING_BACK) {
            Logger.e(TAG, "Unknown facing type: " + facing);
            return ERROR_OP_UNSUPPORTED;
        }

        if (mIsPreviewing) {
            Logger.e(TAG, "Current is previewing, can not start!");
            return ERROR_OP_UNSUPPORTED;
        }

        // find target camera device
        CameraDeviceInfo targetDevice = null;
        for (CameraDeviceInfo device : mDeviceList) {
            if (device.mFacing == facing) {
                targetDevice = device;
            }
        }
        if (targetDevice == null) {
            Logger.e(TAG, "No camera facing " + facing + " in this devices!");
            return ERROR_OP_UNSUPPORTED;
        }

        // open target device
        try {
            mCamera = Camera.open((Integer) targetDevice.mCameraId);
            mCamera.setPreviewTexture(mSurface);
            mCamera.startPreview();
            setFocusMode(FOCUS_MODE_CONTINUOUS_VIDEO);
            mCurrentDevicePos = mDeviceList.indexOf(targetDevice);
            mIsPreviewing = true;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return ERROR_OP_UNSUPPORTED;
        }

        return RESULT_OK;
    }

    @Override
    public int stopPreview() {
        if (!mIsPreviewing) {
            Logger.e(TAG, "Current is not previewing!");
            return ERROR_OP_UNSUPPORTED;
        }

        // stop preview and release.
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
        mCurrentDevicePos = -1;
        mIsPreviewing = false;

        return RESULT_OK;
    }

    @Override
    public int getFlashLightMode() {
        if (mCamera == null) {
            Logger.e(TAG, "Not init!");
            return ERROR_OP_UNSUPPORTED;
        }

        int result = ERROR_UNKNOW;
        Camera.Parameters parameters = mCamera.getParameters();
        if (Camera.Parameters.FLASH_MODE_OFF.equals(parameters.getFlashMode())) {
            result = FLASH_MODE_OFF;
        } else if (Camera.Parameters.FLASH_MODE_TORCH.equals(parameters.getFlashMode())) {
            result = FLASH_MODE_TORCH;
        }

        return result;
    }

    @Override
    public int setFlashLightMode(int mode) {
        if (mCamera == null) {
            Logger.e(TAG, "Not init!");
            return ERROR_OP_UNSUPPORTED;
        }

        int result = ERROR_UNKNOW;
        if (mode != FLASH_MODE_OFF && mode != FLASH_MODE_TORCH) {
            Logger.e(TAG, "Unknown mode: " + mode);
            result = ERROR_OP_UNSUPPORTED;
        } else if (mode == FLASH_MODE_OFF) {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(parameters);
            result = RESULT_OK;
        } else if (mode == FLASH_MODE_TORCH) {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(parameters);
            result = RESULT_OK;
        }

        return result;
    }

    @Override
    public int setAspectRatio(AspectRatio ratio) {
        return 0;
    }

    @Override
    public AspectRatio getAspectRatio() {
        return null;
    }

    @Override
    public int setFocusMode(int mode) {
        if (mCamera == null) {
            Logger.e(TAG, "Not init!");
            return ERROR_OP_UNSUPPORTED;
        }

        if (mode != FOCUS_MODE_AUTO &&
                mode != FOCUS_MODE_CONTINUOUS_VIDEO &&
                mode != FOCUS_MODE_EDOF &&
                mode != FOCUS_MODE_FIXED &&
                mode != FOCUS_MODE_INFINITY &&
                mode != FOCUS_MODE_MACRO) {
            Logger.e(TAG, "Unknown mode: " + mode);
        }

        Camera.Parameters parameters = mCamera.getParameters();
        if (mode == FOCUS_MODE_AUTO) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        } else if (mode == FOCUS_MODE_CONTINUOUS_VIDEO) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        } else if (mode == FOCUS_MODE_EDOF) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_EDOF);
        } else if (mode == FOCUS_MODE_FIXED) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
        } else if (mode == FOCUS_MODE_INFINITY) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
        } else if (mode == FOCUS_MODE_MACRO) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
        }

        try {
            mCamera.setParameters(parameters);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ERROR_OP_UNSUPPORTED;
        }

        return RESULT_OK;
    }

    @Override
    public int getFocusMode() {
        if (mCamera == null) {
            Logger.e(TAG, "Not init!");
            return ERROR_OP_UNSUPPORTED;
        }

        int mode = ERROR_UNKNOW;
        Camera.Parameters parameters = mCamera.getParameters();
        switch (parameters.getFocusMode()) {
            case Camera.Parameters.FOCUS_MODE_AUTO:
                mode = FOCUS_MODE_AUTO;
                break;
            case Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO:
                mode = FOCUS_MODE_CONTINUOUS_VIDEO;
                break;
            case Camera.Parameters.FOCUS_MODE_EDOF:
                mode = FOCUS_MODE_EDOF;
                break;
            case Camera.Parameters.FOCUS_MODE_FIXED:
                mode = FOCUS_MODE_FIXED;
                break;
            case Camera.Parameters.FOCUS_MODE_INFINITY:
                mode = FOCUS_MODE_INFINITY;
                break;
            case Camera.Parameters.FOCUS_MODE_MACRO:
                mode = FOCUS_MODE_MACRO;
                break;
            default:
                break;
        }

        return mode;
    }

    @Override
    public int triggerFocus(MotionEvent event) {
        if (mCamera == null) {
            Logger.e(TAG, "Not init!");
            return ERROR_OP_UNSUPPORTED;
        }

        Camera.Parameters parameters = mCamera.getParameters();
        String focusMode = parameters.getFocusMode();
        if (Camera.Parameters.FOCUS_MODE_EDOF.equals(focusMode) ||
                Camera.Parameters.FOCUS_MODE_FIXED.equals(focusMode) ||
                Camera.Parameters.FOCUS_MODE_INFINITY.equals(focusMode)) {
            Logger.e(TAG, "Current focus mode " + focusMode + " can not trigger focus!");
            return ERROR_OP_UNSUPPORTED;
        }

        Camera.Size previewSize = parameters.getPreviewSize();
        Logger.d(TAG, "Preview width: " + previewSize.width + ", height: " + previewSize.height);
        int viewWidth = previewSize.width;
        int viewHeight = previewSize.height;
        Rect focusRect = calculateTapArea(event.getX(), event.getY(), 3f, viewWidth, viewHeight);
        if (parameters.getMaxNumFocusAreas() > 0) {
            // cancel auto focus first
            mCamera.cancelAutoFocus();
            parameters.setFocusAreas(Collections.singletonList(new Camera.Area(focusRect, 1)));
            try {
                mCamera.setParameters(parameters);
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        Logger.d(TAG, "Trigger result: " + success);
                    }
                });
            } catch (RuntimeException e) {
                e.printStackTrace();
                Logger.e(TAG, "Trigger focus failed!");
            }
        } else {
            Logger.e(TAG, "getMaxNumFocusAreas is 0 or negative.");
            return ERROR_OP_UNSUPPORTED;
        }

        return RESULT_OK;
    }

    @Override
    public int zoom(boolean isZoomIn) {
        if (mCamera == null) {
            Logger.e(TAG, "Not init!");
            return ERROR_OP_UNSUPPORTED;
        }

        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters.isZoomSupported()) {
            int maxZoom = parameters.getMaxZoom();
            int currentZoom = parameters.getZoom();
            if (isZoomIn && currentZoom < maxZoom) {
                currentZoom++;
            } else if (!isZoomIn && currentZoom > 0) {
                currentZoom--;
            }

            parameters.setZoom(currentZoom);
            mCamera.setParameters(parameters);
        } else {
            return ERROR_OP_UNSUPPORTED;
        }

        return RESULT_OK;
    }

    @Override
    public int getZoom() {
        if (mCamera == null) {
            Logger.e(TAG, "Not init!");
            return ERROR_OP_UNSUPPORTED;
        }

        Camera.Parameters parameters = mCamera.getParameters();
        return parameters.getZoom();
    }

    @Override
    public int getMaxZoom() {
        if (mCamera == null) {
            Logger.e(TAG, "Not init!");
            return ERROR_OP_UNSUPPORTED;
        }

        Camera.Parameters parameters = mCamera.getParameters();
        return parameters.getMaxZoom();
    }

    @Override
    public boolean isCameraOpened() {
        return mCamera != null;
    }

    @Override
    public int setFacing(int facing) {
        if (mCurrentDevicePos != -1) {
            CameraDeviceInfo currentDevice = mDeviceList.get(mCurrentDevicePos);
            if (currentDevice.mFacing == facing) {
                Logger.w(TAG, "Current facing is the same as " + facing + ", no need!");
                return ERROR_OP_UNSUPPORTED;
            } else {
                stopPreview();
                startPreview(facing);
            }
        }

        startPreview(facing);

        return RESULT_OK;
    }

    @Override
    public int getFacing() {
        if (mCurrentDevicePos == -1) {
            Logger.e(TAG, "Camera is not opened!");
            return ERROR_OP_UNSUPPORTED;
        }

        CameraDeviceInfo deviceInfo = mDeviceList.get(mCurrentDevicePos);
        return deviceInfo.mFacing;
    }

    @Override
    public int setWhiteBalanceMode(int mode) {
        if (mCamera == null) {
            Logger.e(TAG, "Not init!");
            return ERROR_OP_UNSUPPORTED;
        }

        if (mode != WHITE_BALANCE_AUTO &&
                mode != WHITE_BALANCE_CLOUDY_DAYLIGHT &&
                mode != WHITE_BALANCE_DAYLIGHT &&
                mode != WHITE_BALANCE_FLUORESCENT &&
                mode != WHITE_BALANCE_INCANDESCENT &&
                mode != WHITE_BALANCE_SHADE &&
                mode != WHITE_BALANCE_TWILIGHT &&
                mode != WHITE_BALANCE_WARM_FLUORESCENT) {
            Logger.e(TAG, "Unknown white balance mode: " + mode);
            return ERROR_OP_UNSUPPORTED;
        }

        Camera.Parameters parameters = mCamera.getParameters();
        switch (mode) {
            case WHITE_BALANCE_AUTO:
                parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
                break;
            case WHITE_BALANCE_CLOUDY_DAYLIGHT:
                parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT);
                break;
            case WHITE_BALANCE_DAYLIGHT:
                parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_DAYLIGHT);
                break;
            case WHITE_BALANCE_FLUORESCENT:
                parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_FLUORESCENT);
                break;
            case WHITE_BALANCE_INCANDESCENT:
                parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_INCANDESCENT);
                break;
            case WHITE_BALANCE_SHADE:
                parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_SHADE);
                break;
            case WHITE_BALANCE_TWILIGHT:
                parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_TWILIGHT);
                break;
            case WHITE_BALANCE_WARM_FLUORESCENT:
                parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_WARM_FLUORESCENT);
                break;
            default:
                break;
        }
        mCamera.setParameters(parameters);

        return RESULT_OK;
    }

    @Override
    public int getWhiteBalanceMode() {
        if (mCamera == null) {
            Logger.e(TAG, "Not init!");
            return ERROR_OP_UNSUPPORTED;
        }

        Camera.Parameters parameters = mCamera.getParameters();
        String mode = parameters.getWhiteBalance();
        int result = ERROR_UNKNOW;
        switch (mode) {
            case Camera.Parameters.WHITE_BALANCE_AUTO:
                result = WHITE_BALANCE_AUTO;
                break;
            case Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT:
                result = WHITE_BALANCE_CLOUDY_DAYLIGHT;
                break;
            case Camera.Parameters.WHITE_BALANCE_DAYLIGHT:
                result = WHITE_BALANCE_DAYLIGHT;
                break;
            case Camera.Parameters.WHITE_BALANCE_FLUORESCENT:
                result = WHITE_BALANCE_FLUORESCENT;
                break;
            case Camera.Parameters.WHITE_BALANCE_INCANDESCENT:
                result = WHITE_BALANCE_INCANDESCENT;
                break;
            case Camera.Parameters.WHITE_BALANCE_SHADE:
                result = WHITE_BALANCE_SHADE;
                break;
            case Camera.Parameters.WHITE_BALANCE_TWILIGHT:
                result = WHITE_BALANCE_TWILIGHT;
                break;
            case Camera.Parameters.WHITE_BALANCE_WARM_FLUORESCENT:
                result = WHITE_BALANCE_WARM_FLUORESCENT;
                break;
            default:
                break;
        }

        return result;
    }

    @Override
    public int setExposureCompensation(int value) {
        if (mCamera == null) {
            Logger.e(TAG, "Not init!");
            return ERROR_OP_UNSUPPORTED;
        }

        List<Integer> range = getExposureCompensationRange();
        if (range == null) {
            Logger.e(TAG, "Current camera do not support exposure compensation");
            return ERROR_OP_UNSUPPORTED;
        }

        if (value < range.get(0) || value > range.get(1)) {
            Logger.e(TAG, "Value invalid, should between " + range.get(0) + " and " + range.get(1));
            return ERROR_OP_UNSUPPORTED;
        }

        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setExposureCompensation(value);
        mCamera.setParameters(parameters);

        return RESULT_OK;
    }

    @Override
    public int getExposureCompensation() {
        if (mCamera == null) {
            Logger.e(TAG, "Not init!");
            return ERROR_OP_UNSUPPORTED;
        }

        Camera.Parameters parameters = mCamera.getParameters();
        return parameters.getExposureCompensation();
    }

    @Override
    public List<Integer> getExposureCompensationRange() {
        if (mCamera == null) {
            Logger.e(TAG, "Not init!");
            return null;
        }

        Camera.Parameters parameters = mCamera.getParameters();
        List<Integer> list = new ArrayList<>();

        list.add(parameters.getMinExposureCompensation());
        list.add(parameters.getMaxExposureCompensation());

        return list;
    }

    @Override
    public float getExposureCompensationStep() {
        if (mCamera == null) {
            Logger.e(TAG, "Not init!");
            return ERROR_OP_UNSUPPORTED;
        }

        Camera.Parameters parameters = mCamera.getParameters();
        return parameters.getExposureCompensationStep();
    }

    @Override
    public int setDisplayOrientation(int orientation) {
        if (mCamera == null) {
            Logger.e(TAG, "Not init!");
            return ERROR_OP_UNSUPPORTED;
        }

        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setRotation(calcCameraRotation(orientation));
        mCamera.setParameters(parameters);

        return RESULT_OK;
    }

    private void init() {
        int cameraNumbers = Camera.getNumberOfCameras();
        if (cameraNumbers <= 0) {
            Logger.e(TAG, "No camera device found in this device!");
            return;
        }
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < cameraNumbers; i++) {
            CameraDeviceInfo device;
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                device = new CameraDeviceInfo();
                device.mCameraId = i;
                device.mFacing = FACING_FRONT;
                device.mOrientation = cameraInfo.orientation;
                mDeviceList.add(device);
            } else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                device = new CameraDeviceInfo();
                device.mCameraId = i;
                device.mFacing = FACING_BACK;
                device.mOrientation = cameraInfo.orientation;
                mDeviceList.add(device);
            }
        }
    }

    private Rect calculateTapArea(float x, float y, float coefficient, int width, int height) {
        float baseAreaSize = 100;

        int areaSize = Float.valueOf(baseAreaSize * coefficient).intValue();
        int centerX = (int) (x / width * 2000 - 1000);
        int centerY = (int) (y / height * 2000 - 1000);

        int halfAreaSize = areaSize / 2;
        // 点击的矩形区域
        RectF rectF = new RectF(
                clamp(centerX - halfAreaSize, -1000, 1000),
                clamp(centerY - halfAreaSize, -1000, 1000),
                clamp(centerX + halfAreaSize, -1000, 1000),
                clamp(centerY + halfAreaSize, -1000, 1000));
        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
    }

    /**
     * Calculate display orientation
     * https://developer.android.com/reference/android/hardware/Camera.html#setDisplayOrientation(int)
     * <p>
     * This calculation is used for orienting the preview
     * <p>
     * Note: This is not the same calculation as the camera rotation
     *
     * @param screenOrientationDegrees Screen orientation in degrees
     * @return Number of degrees required to rotate preview
     */
    private int calcDisplayOrientation(int screenOrientationDegrees) {
        CameraDeviceInfo cameraDeviceInfo = mDeviceList.get(mCurrentDevicePos);
        if (cameraDeviceInfo.mFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            return (360 - (cameraDeviceInfo.mOrientation + screenOrientationDegrees) % 360) % 360;
        } else {  // back-facing
            return (cameraDeviceInfo.mOrientation - screenOrientationDegrees + 360) % 360;
        }
    }

    /**
     * Calculate camera rotation
     * <p>
     * This calculation is applied to the output JPEG either via Exif Orientation tag
     * or by actually transforming the bitmap. (Determined by vendor camera API implementation)
     * <p>
     * Note: This is not the same calculation as the display orientation
     *
     * @param screenOrientationDegrees Screen orientation in degrees
     * @return Number of degrees to rotate image in order for it to view correctly.
     */
    private int calcCameraRotation(int screenOrientationDegrees) {
        CameraDeviceInfo cameraDeviceInfo = mDeviceList.get(mCurrentDevicePos);
        if (cameraDeviceInfo.mFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            return (cameraDeviceInfo.mOrientation + screenOrientationDegrees) % 360;
        } else {  // back-facing
            final int landscapeFlip = isLandscape(screenOrientationDegrees) ? 180 : 0;
            return (cameraDeviceInfo.mOrientation + screenOrientationDegrees + landscapeFlip) % 360;
        }
    }

    /**
     * Test if the supplied orientation is in landscape.
     *
     * @param orientationDegrees Orientation in degrees (0,90,180,270)
     * @return True if in landscape, false if portrait
     */
    private boolean isLandscape(int orientationDegrees) {
        return (orientationDegrees == ORIENTATION_90 ||
                orientationDegrees == ORIENTATION_270);
    }

    /**
     * This method takes a numerical value and ensures it fits in a given numerical range. If the
     * number is smaller than the minimum required by the range, then the minimum of the range will
     * be returned. If the number is higher than the maximum allowed by the range then the maximum
     * of the range will be returned.
     *
     * @param value the value to be clamped.
     * @param min   minimum resulting value.
     * @param max   maximum resulting value.
     * @return the clamped value.
     */
    private float clamp(float value, float min, float max) {
        if (value < min) {
            return min;
        } else if (value > max) {
            return max;
        }
        return value;
    }
}
