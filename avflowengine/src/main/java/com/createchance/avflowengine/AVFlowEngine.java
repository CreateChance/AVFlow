package com.createchance.avflowengine;

import android.view.Surface;

import com.createchance.avflowengine.generator.CameraStreamGenerator;
import com.createchance.avflowengine.generator.LocalStreamGenerator;
import com.createchance.avflowengine.processor.CodecStreamProcessor;
import com.createchance.avflowengine.processor.gpuimage.GPUImageFilter;
import com.createchance.avflowengine.saver.MuxerStreamSaver;

import java.io.File;

/**
 * av flow engine.
 *
 * @author createchance
 * @date 2018-09-06
 */
public class AVFlowEngine {
    private static AVFlowEngine sInstance;

    private CameraStreamGenerator mCameraGenerator;
    private LocalStreamGenerator mLocalGenerator;
    private CodecStreamProcessor mProcessor;
    private MuxerStreamSaver mSaver;

    private AVFlowEngine() {
        mCameraGenerator = new CameraStreamGenerator();
        mLocalGenerator = new LocalStreamGenerator();
        mProcessor = new CodecStreamProcessor();
        mSaver = new MuxerStreamSaver();
    }

    public synchronized static AVFlowEngine getInstance() {
        if (sInstance == null) {
            sInstance = new AVFlowEngine();
        }

        return sInstance;
    }

    public void setPreviewSurface(Surface surface) {
        mProcessor.setPreviewSurface(surface);
    }

    public void setForceCameraV1(boolean force) {
        mCameraGenerator.setForceV1Camera(force);
    }

    public void setSurfaceSize(int width, int height) {
        mProcessor.setSurfaceSize(width, height);
        mSaver.setOutputSize(width, height);
    }

    public void setPreviewFilter(GPUImageFilter filter) {
        mProcessor.setPreviewFilter(filter);
    }

    public void setSaveFilter(GPUImageFilter filter) {
        mProcessor.setSaveFilter(filter);
    }

    public void setInputFile(File inputFile) {
        mLocalGenerator.setInputFile(inputFile);
    }

    public void setOutputFile(File outputFile) {
        mSaver.setOutputFile(outputFile);
    }

    public void prepare() {
        mSaver.prepare();
        mSaver.start();
        mProcessor.setSaveSurface(mSaver.getInputSurface());
        mProcessor.start();
        mCameraGenerator.setOutputTexture(mProcessor.getOesTextureId());
        mLocalGenerator.setOutputTexture(mProcessor.getOesTextureId());
    }

    public void startCameraGenerator() {
        mCameraGenerator.start();
    }

    public void startLocalGenerator() {
        mLocalGenerator.start();
    }

    public void stopCameraGenerator() {
        mCameraGenerator.stop();
    }

    public void stopLocalGenerator() {
        mLocalGenerator.stop();
    }

    public void stop() {
        // stop all nodes to stop this engine.
        mCameraGenerator.stop();
        mLocalGenerator.stop();
        mProcessor.stop();
        mSaver.stop();
    }
}
