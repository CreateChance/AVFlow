package com.createchance.avflowengine.generator;

import android.graphics.SurfaceTexture;
import android.os.Build;

/**
 * ${DESC}
 *
 * @author createchance
 * @date 2018/8/27
 */
public class CameraStreamGenerator {

    private static final String TAG = "CameraStreamGenerator";

    private CameraImpl mCamera;
    private boolean mForceV1Camera;

    private SurfaceTexture mOutputSurface;

    public void setOutputTexture(SurfaceTexture surfaceTexture) {
        mOutputSurface = surfaceTexture;
    }

    public void start() {
        if (mForceV1Camera) {
            mCamera = new CameraV1(mOutputSurface);
        } else {
            if (Build.VERSION.SDK_INT < 21) {
                mCamera = new CameraV1(mOutputSurface);
            }
        }

        mCamera.startPreview(CameraImpl.FACING_BACK);
    }

    public void stop() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera = null;
        }
    }

    public CameraImpl getCamera() {
        return mCamera;
    }

    public void setForceV1Camera(boolean force) {
        mForceV1Camera = force;
    }
}