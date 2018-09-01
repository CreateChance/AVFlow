package com.createchance.camerastreamgenerator;

import android.hardware.Camera;
import android.util.Log;

import com.createchance.mediastreambase.IVideoInputSurfaceListener;
import com.createchance.mediastreambase.IVideoStreamConsumer;
import com.createchance.mediastreambase.IVideoStreamGenerator;
import com.createchance.mediastreambase.Logger;
import com.createchance.mediastreambase.VideoInputSurface;

import java.io.IOException;

/**
 * ${DESC}
 *
 * @author createchance
 * @date 2018/8/27
 */
public final class CameraStreamGenerator implements IVideoStreamGenerator, IVideoInputSurfaceListener {

    private static final String TAG = "CameraStreamGenerator";

    private IVideoStreamConsumer mConsumer;

    private Camera mCamera;

    private VideoInputSurface mInputSurface;

    @Override
    public void start() {
        if (mInputSurface == null) {
            Logger.e(TAG, "Can not start during init.");
            return;
        }
    }

    @Override
    public void stop() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
        }
    }

    @Override
    public void setConsumer(IVideoStreamConsumer consumer) {
        this.mConsumer = consumer;
        mConsumer.setInputSurfaceListener(this);
    }

    @Override
    public void onConsumerSurfaceInitDone(IVideoStreamConsumer consumer,
                                          VideoInputSurface inputSurface) {
        this.mInputSurface = inputSurface;

        Camera.CameraInfo info = new Camera.CameraInfo();

        int numCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numCameras; i++) {
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                mCamera = Camera.open(i);
                break;
            }
        }
        if (mCamera == null) {
            Logger.d(TAG, "No front-facing camera found; opening default");
            // opens first back-facing camera
            mCamera = Camera.open();
        }

        try {
            Log.d(TAG, "onConsumerSurfaceInitDone: " + mInputSurface.mSurfaceHolder);
            mCamera.setPreviewTexture(mInputSurface.mSurfaceTexture);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        mCamera.startPreview();
    }
}
