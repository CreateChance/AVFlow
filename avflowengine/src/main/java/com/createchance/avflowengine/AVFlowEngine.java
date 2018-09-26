package com.createchance.avflowengine;

import android.opengl.GLES20;
import android.view.Surface;

import com.createchance.avflowengine.base.Logger;
import com.createchance.avflowengine.generator.CameraImpl;
import com.createchance.avflowengine.generator.CameraStreamGenerator;
import com.createchance.avflowengine.generator.LocalStreamGenerator;
import com.createchance.avflowengine.generator.VideoPlayListener;
import com.createchance.avflowengine.processor.AVStreamProcessor;
import com.createchance.avflowengine.processor.gpuimage.GPUImageFilter;
import com.createchance.avflowengine.saver.AVStreamSaver;
import com.createchance.avflowengine.saver.SaveListener;

import java.io.File;
import java.util.List;

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
    private AVStreamProcessor mProcessor;
    private AVStreamSaver mSaver;

    private PreviewConfig mPreviewConfig;

    private AVFlowEngine() {
    }

    public synchronized static AVFlowEngine getInstance() {
        if (sInstance == null) {
            sInstance = new AVFlowEngine();
        }

        return sInstance;
    }

    public void startPreview(PreviewConfig config) {
        if (config == null) {
            Logger.e(TAG, "Config can not be null!");
            return;
        }

        mPreviewConfig = config;
        init();
        mProcessor.setSurfaceSize(config.mSurfaceWidth, config.mSurfaceHeight);
        preparePreview(config.mSurface);
        if (config.mDataSource.mSource == PreviewConfig.SOURCE_CAMERA) {
            prepareEngine(((PreviewConfig.CameraSource) config.mDataSource).mRotation);
            startCameraGenerator(((PreviewConfig.CameraSource) config.mDataSource).mForceCameraV1);
        } else if (config.mDataSource.mSource == PreviewConfig.SOURCE_FILE) {
            prepareEngine(((PreviewConfig.FileSource) config.mDataSource).mRotation);
            startLocalGenerator(((PreviewConfig.FileSource) config.mDataSource).mSourceFileList,
                    ((PreviewConfig.FileSource) config.mDataSource).mLoop,
                    ((PreviewConfig.FileSource) config.mDataSource).mSpeedRate,
                    ((PreviewConfig.FileSource) config.mDataSource).mListener);
        }
    }

    public void setPreviewFilter(GPUImageFilter filter) {
        if (mPreviewConfig == null) {
            Logger.e(TAG, "You should start preview first!");
            return;
        }

        if (filter != null) {
            filter.init();
            GLES20.glUseProgram(filter.getProgram());
            filter.onOutputSizeChanged(mPreviewConfig.mSurfaceWidth, mPreviewConfig.mSurfaceHeight);
        }
        mProcessor.setPreviewFilter(filter);
    }

    public void startSave(int clipTop,
                          int clipLeft,
                          int clipBottom,
                          int clipRight,
                          File outputFile,
                          int orientation,
                          SaveListener saveListener) {
        Logger.d(TAG, "Clip top: "
                + clipTop
                + ", clip left: "
                + clipLeft
                + ", clip bottom: "
                + clipBottom
                + ", clip right: "
                + clipRight);
        mSaver.setOutputSize(clipRight - clipLeft, clipBottom - clipTop);
        mSaver.prepare();
        mProcessor.setSaveSurface(mSaver.getInputSurface(), clipTop, clipLeft, clipBottom, clipRight);
        mSaver.beginSave(outputFile, orientation, saveListener);
    }

    public void setSaveFilter(GPUImageFilter filter) {
        if (filter != null) {
            filter.init();
            GLES20.glUseProgram(filter.getProgram());
            filter.onOutputSizeChanged(mPreviewConfig.mSurfaceWidth, mPreviewConfig.mSurfaceHeight);
        }
        mProcessor.setSaveFilter(filter);
    }

    public void finishSave() {
        mProcessor.clearSaveSurface();
        mSaver.finishSave();
    }

    public void cancelSave() {
        if (mProcessor != null) {
            mProcessor.clearSaveSurface();
        }
        if (mSaver != null) {
            mSaver.cancelSave();
        }
    }

    public void reset() {
        if (mPreviewConfig == null) {
            Logger.e(TAG, "You should start preview first!");
            return;
        }

        if (mPreviewConfig.mDataSource.mSource == PreviewConfig.SOURCE_CAMERA) {
            if (mCameraGenerator != null) {
                mCameraGenerator.stop();
            }
        } else if (mPreviewConfig.mDataSource.mSource == PreviewConfig.SOURCE_FILE) {
            if (mLocalGenerator != null) {
                mLocalGenerator.stop();
            }
        }

        if (mProcessor != null) {
            mProcessor.stop();
        }
        cancelSave();

        mPreviewConfig = null;
        mCameraGenerator = null;
        mLocalGenerator = null;
        mProcessor = null;
        mSaver = null;
    }

    private void init() {
        mCameraGenerator = new CameraStreamGenerator();
        mLocalGenerator = new LocalStreamGenerator();
        mProcessor = new AVStreamProcessor();
        mSaver = new AVStreamSaver();
    }

    private void preparePreview(Surface previewSurface) {
        mProcessor.setPreviewSurface(previewSurface);
    }

    private void prepareEngine(int rotation) {
        mProcessor.prepare(rotation);
        mCameraGenerator.setOutputTexture(mProcessor.getOesTextureId());
        mLocalGenerator.setOutputTexture(mProcessor.getOesTextureId());
    }

    public CameraImpl getCamera() {
        return mCameraGenerator.getCamera();
    }

    private void startCameraGenerator(boolean forceV1) {
        mCameraGenerator.start(forceV1);
    }

    private void startLocalGenerator(List<File> inputFileList, boolean loopPlay, float speedRate, VideoPlayListener listener) {
        mLocalGenerator.setInputFile(inputFileList);
        mLocalGenerator.setLoop(loopPlay);
        mLocalGenerator.setSpeed(speedRate);
        mLocalGenerator.start(listener);
    }
}
