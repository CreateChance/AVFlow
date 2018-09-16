package com.createchance.avflowengine;

import android.opengl.GLES20;
import android.view.Surface;

import com.createchance.avflowengine.base.Logger;
import com.createchance.avflowengine.generator.CameraImpl;
import com.createchance.avflowengine.generator.CameraStreamGenerator;
import com.createchance.avflowengine.generator.LocalStreamGenerator;
import com.createchance.avflowengine.processor.CodecStreamProcessor;
import com.createchance.avflowengine.processor.gpuimage.GPUImageFilter;
import com.createchance.avflowengine.saver.MuxerStreamSaver;
import com.createchance.avflowengine.saver.SaveListener;

import java.io.File;

/**
 * av flow engine.
 *
 * @author createchance
 * @date 2018-09-06
 */
public class AVFlowEngine {
    private static final String TAG = "AVFlowEngine";

    private static AVFlowEngine sInstance;

    private CameraStreamGenerator mCameraGenerator;
    private LocalStreamGenerator mLocalGenerator;
    private CodecStreamProcessor mProcessor;
    private MuxerStreamSaver mSaver;

    private Config mConfig;

    private int mClipTop, mClipLeft, mClipBottom, mClipRight;

    private boolean mAllowToChangeClip = true;

    private AVFlowEngine() {
    }

    public synchronized static AVFlowEngine getInstance() {
        if (sInstance == null) {
            sInstance = new AVFlowEngine();
        }

        return sInstance;
    }

    public void configure(Config config) {
        mConfig = config;
        mCameraGenerator = new CameraStreamGenerator();
        mLocalGenerator = new LocalStreamGenerator();
        mProcessor = new CodecStreamProcessor();
        mSaver = new MuxerStreamSaver();

        setPreviewSurface(config.mPreviewSurface);
        setSurfaceSize(config.mSurfaceWidth, config.mSurfaceHeight);
        setForceCameraV1(config.mForceCameraV1);
    }

    public void setPreviewFilter(GPUImageFilter filter) {
        if (filter != null) {
            filter.init();
            GLES20.glUseProgram(filter.getProgram());
            filter.onOutputSizeChanged(mConfig.mSurfaceWidth, mConfig.mSurfaceHeight);
        }
        mProcessor.setPreviewFilter(filter);
    }

    public void setSaveFilter(GPUImageFilter filter) {
        if (filter != null) {
            filter.init();
            GLES20.glUseProgram(filter.getProgram());
            filter.onOutputSizeChanged(mConfig.mSurfaceWidth, mConfig.mSurfaceHeight);
        }
        mProcessor.setSaveFilter(filter);
    }

    public void setClipArea(int clipTop, int clipLeft, int clipBottom, int clipRight) {
        if (!mAllowToChangeClip) {
            Logger.e(TAG, "You can not change clip area duration running!");
            return;
        }
        mClipTop = clipTop;
        mClipLeft = clipLeft;
        mClipBottom = clipBottom;
        mClipRight = clipRight;
        Logger.d(TAG, "Clip top: "
                + mClipTop
                + ", clip left: "
                + mClipLeft
                + ", clip bottom: "
                + mClipBottom
                + ", clip right: "
                + mClipRight);
    }

    public void prepare() {
        mProcessor.start();
        mCameraGenerator.setOutputTexture(mProcessor.getOesTextureId());
        mLocalGenerator.setOutputTexture(mProcessor.getOesTextureId());
    }

    public CameraImpl getCamera() {
        return mCameraGenerator.getCamera();
    }

    public void startCameraGenerator() {
        mCameraGenerator.start();
    }

    public void startLocalGenerator(File inputFile) {
        mLocalGenerator.setInputFile(inputFile);
        mLocalGenerator.start();
    }

    public void stopCameraGenerator() {
        mCameraGenerator.stop();
    }

    public void stopLocalGenerator() {
        mLocalGenerator.stop();
    }

    public void startSave(File outputFile, SaveListener saveListener) {
        mSaver.setOutputSize(mClipRight - mClipLeft, mClipBottom - mClipTop);
        mSaver.prepare();
        mProcessor.setSaveSurface(mSaver.getInputSurface(), mClipTop, mClipLeft, mClipBottom, mClipRight);
        mSaver.beginSave(outputFile, saveListener);
        mAllowToChangeClip = false;
    }

    public void finishSave() {
        mProcessor.clearSaveSurface();
        mSaver.finishSave();
    }

    public void cancelSave() {
        mProcessor.clearSaveSurface();
        mSaver.cancelSave();
    }

    public void stop() {
        // stop all nodes to stop this engine.
        mCameraGenerator.stop();
        mLocalGenerator.stop();
        mProcessor.stop();
        cancelSave();
        mAllowToChangeClip = true;
    }

    private void setPreviewSurface(Surface surface) {
        mProcessor.setPreviewSurface(surface);
    }

    private void setForceCameraV1(boolean force) {
        mCameraGenerator.setForceV1Camera(force);
    }

    private void setSurfaceSize(int width, int height) {
        mProcessor.setSurfaceSize(width, height);
    }

    public static class Config {
        public Surface mPreviewSurface;
        public int mSurfaceWidth, mSurfaceHeight;
        public boolean mForceCameraV1;
    }
}
