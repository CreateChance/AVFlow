package com.createchance.avflowengine;

import android.opengl.GLES20;
import android.view.Surface;

import com.createchance.avflowengine.base.Logger;
import com.createchance.avflowengine.generator.CameraImpl;
import com.createchance.avflowengine.generator.CameraStreamGenerator;
import com.createchance.avflowengine.generator.LocalStreamGenerator;
import com.createchance.avflowengine.generator.VideoPlayListener;
import com.createchance.avflowengine.processor.CodecStreamProcessor;
import com.createchance.avflowengine.processor.gpuimage.GPUImageFilter;
import com.createchance.avflowengine.saver.MuxerStreamSaver;
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
    private CodecStreamProcessor mProcessor;
    private MuxerStreamSaver mSaver;

    private int mInputSurfaceWidth, mInputSurfaceHeight;

    private AVFlowEngine() {
    }

    public synchronized static AVFlowEngine getInstance() {
        if (sInstance == null) {
            sInstance = new AVFlowEngine();
        }

        return sInstance;
    }

    public void init() {
        mCameraGenerator = new CameraStreamGenerator();
        mLocalGenerator = new LocalStreamGenerator();
        mProcessor = new CodecStreamProcessor();
        mSaver = new MuxerStreamSaver();
    }

    public void setInputSize(int surfaceWidth, int surfaceHeight) {
        mInputSurfaceWidth = surfaceWidth;
        mInputSurfaceHeight = surfaceHeight;
        mProcessor.setSurfaceSize(mInputSurfaceWidth, mInputSurfaceHeight);
    }

    public void preparePreview(Surface previewSurface) {
        mProcessor.setPreviewSurface(previewSurface);
    }

    public void prepareSave(int clipTop, int clipLeft, int clipBottom, int clipRight) {
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
    }

    public void prepareEngine(int rotation) {
        mProcessor.prepare(rotation);
        mCameraGenerator.setOutputTexture(mProcessor.getOesTextureId());
        mLocalGenerator.setOutputTexture(mProcessor.getOesTextureId());
    }

    public void setPreviewFilter(GPUImageFilter filter) {
        if (filter != null) {
            filter.init();
            GLES20.glUseProgram(filter.getProgram());
            filter.onOutputSizeChanged(mInputSurfaceWidth, mInputSurfaceHeight);
        }
        mProcessor.setPreviewFilter(filter);
    }

    public void setSaveFilter(GPUImageFilter filter) {
        if (filter != null) {
            filter.init();
            GLES20.glUseProgram(filter.getProgram());
            filter.onOutputSizeChanged(mInputSurfaceWidth, mInputSurfaceHeight);
        }
        mProcessor.setSaveFilter(filter);
    }

    public CameraImpl getCamera() {
        return mCameraGenerator.getCamera();
    }

    public void startCameraGenerator(boolean forceV1) {
        mCameraGenerator.start(forceV1);
    }

    public void startLocalGenerator(List<File> inputFileList, boolean loopPlay, float speedRate, VideoPlayListener listener) {
        mLocalGenerator.setInputFile(inputFileList);
        mLocalGenerator.setLoop(loopPlay);
        mLocalGenerator.setSpeed(speedRate);
        mLocalGenerator.start(listener);
    }

    public void stopCameraGenerator() {
        if (mCameraGenerator != null) {
            mCameraGenerator.stop();
        }
    }

    public void stopLocalGenerator() {
        if (mLocalGenerator != null) {
            mLocalGenerator.stop();
        }
    }

    public void startSave(File outputFile, int orientation, SaveListener saveListener) {
        mSaver.beginSave(outputFile, orientation, saveListener);
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
        // stop all nodes to stop this engine.
        if (mCameraGenerator != null) {
            mCameraGenerator.stop();
        }
        if (mLocalGenerator != null) {
            mLocalGenerator.stop();
        }
        if (mProcessor != null) {
            mProcessor.stop();
        }
        cancelSave();

        mCameraGenerator = null;
        mLocalGenerator = null;
        mProcessor = null;
        mSaver = null;
    }
}
