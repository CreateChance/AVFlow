package com.createchance.avflowengine;

import android.opengl.GLES20;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.createchance.avflowengine.base.Logger;
import com.createchance.avflowengine.generator.CameraImpl;
import com.createchance.avflowengine.generator.CameraStreamGenerator;
import com.createchance.avflowengine.generator.LocalStreamGenerator;
import com.createchance.avflowengine.processor.AVStreamProcessor;
import com.createchance.avflowengine.processor.gpuimage.GPUImageFilter;
import com.createchance.avflowengine.saver.AVStreamSaver;
import com.createchance.avflowengine.saver.SaveListener;

import java.io.File;

/**
 * Engine worker, all engine work done in this thread.
 *
 * @author createchance
 * @date 2018-09-26
 */
final class EngineWorker extends HandlerThread {
    private static final String TAG = "EngineWorker";

    private static final int MSG_START_PREVIEW = 0;
    private static final int MSG_RESTART_PREVIEW = 1;
    private static final int MSG_SET_PREVIEW_FILTER = 2;
    private static final int MSG_START_SAVE = 3;
    private static final int MSG_SET_SAVE_FILTER = 4;
    private static final int MSG_FINISH_SAVE = 5;
    private static final int MSG_CANCEL_SAVE = 6;
    private static final int MSG_RESET = 7;

    private CameraStreamGenerator mCameraGenerator;
    private LocalStreamGenerator mLocalGenerator;
    private AVStreamProcessor mProcessor;
    private AVStreamSaver mSaver;

    private PreviewConfig mPreviewConfig;

    private Handler mHandler;

    private int mClipTop, mClipLeft, mClipBottom, mClipRight;
    private File mOutputFile;
    private int mOutputRotation;
    private SaveListener mSaveListener;

    private String mToken;

    EngineWorker(String name) {
        super(name);
        mToken = genToken();
    }

    void startThread() {
        start();
        mHandler = new Handler(getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                Logger.d(TAG, "New message: " + msg.what);
                switch (msg.what) {
                    case MSG_START_PREVIEW:
                        handleStartPreview((PreviewConfig) msg.obj);
                        break;
                    case MSG_RESTART_PREVIEW:
                        handleRestartPreview((PreviewConfig) msg.obj);
                        break;
                    case MSG_SET_PREVIEW_FILTER:
                        handleSetPreviewFilter((GPUImageFilter) msg.obj);
                        break;
                    case MSG_START_SAVE:
                        handleStartSave(mClipTop, mClipLeft, mClipBottom, mClipRight, mOutputFile, mOutputRotation, mSaveListener);
                        break;
                    case MSG_SET_SAVE_FILTER:
                        handleSetSaveFilter((GPUImageFilter) msg.obj);
                        break;
                    case MSG_FINISH_SAVE:
                        handleFinishSave();
                        break;
                    case MSG_CANCEL_SAVE:
                        handleCancelSave();
                        break;
                    case MSG_RESET:
                        handleReset();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    void startPreview(PreviewConfig config) {
        if (config == null) {
            Logger.e(TAG, "Config can not be null!");
            return;
        }

        Message message = Message.obtain();
        message.what = MSG_START_PREVIEW;
        message.obj = config;
        mHandler.sendMessage(message);
    }

    void restartPreview(PreviewConfig config) {
        Message message = Message.obtain();
        message.what = MSG_RESTART_PREVIEW;
        message.obj = config;
        mHandler.sendMessage(message);
    }

    void setPreviewFilter(GPUImageFilter filter) {
        if (mPreviewConfig == null) {
            Logger.e(TAG, "You should start preview first!");
            return;
        }

        Message message = Message.obtain();
        message.what = MSG_SET_PREVIEW_FILTER;
        message.obj = filter;
        mHandler.sendMessage(message);
    }

    void startSave(int clipTop,
                   int clipLeft,
                   int clipBottom,
                   int clipRight,
                   File outputFile,
                   int rotation,
                   SaveListener saveListener) {
        mClipTop = clipTop;
        mClipLeft = clipLeft;
        mClipBottom = clipBottom;
        mClipRight = clipRight;
        mOutputFile = outputFile;
        mSaveListener = saveListener;
        mOutputRotation = rotation;

        mHandler.sendEmptyMessage(MSG_START_SAVE);
    }

    void setSaveFilter(GPUImageFilter filter) {
        if (mPreviewConfig == null) {
            Logger.e(TAG, "You should start preview first!");
            return;
        }

        Message message = Message.obtain();
        message.what = MSG_SET_SAVE_FILTER;
        message.obj = filter;
        mHandler.sendMessage(message);
    }

    void finishSave() {
        if (mPreviewConfig == null) {
            Logger.e(TAG, "You should start preview first!");
            return;
        }

        mHandler.sendEmptyMessage(MSG_FINISH_SAVE);
    }

    void cancelSave() {
        if (mPreviewConfig == null) {
            Logger.e(TAG, "You should start preview first!");
            return;
        }

        mHandler.sendEmptyMessage(MSG_CANCEL_SAVE);
    }

    void reset() {
        mHandler.sendEmptyMessage(MSG_RESET);
    }

    CameraImpl getCamera() {
        return mCameraGenerator.getCamera();
    }

    String getToken() {
        return mToken;
    }

    private String genToken() {
        return String.valueOf(System.currentTimeMillis());
    }

    private void handleStartPreview(PreviewConfig config) {
        mPreviewConfig = config;

        mCameraGenerator = new CameraStreamGenerator();
        mLocalGenerator = new LocalStreamGenerator();
        mProcessor = new AVStreamProcessor();
        mSaver = new AVStreamSaver();

        mProcessor.setPreviewSurface(config.mSurface, config.mSurfaceWidth, config.mSurfaceHeight);
        if (config.mDataSource.mSource == PreviewConfig.SOURCE_CAMERA) {
            mProcessor.prepare(((PreviewConfig.CameraSource) config.mDataSource).mRotation);
            mCameraGenerator.setOutputTexture(mProcessor.getOesTextureId());
            mLocalGenerator.setOutputTexture(mProcessor.getOesTextureId());
            mCameraGenerator.start(((PreviewConfig.CameraSource) config.mDataSource).mForceCameraV1);
        } else if (config.mDataSource.mSource == PreviewConfig.SOURCE_FILE) {
            mProcessor.prepare(((PreviewConfig.FileSource) config.mDataSource).mRotation);
            mCameraGenerator.setOutputTexture(mProcessor.getOesTextureId());
            mLocalGenerator.setOutputTexture(mProcessor.getOesTextureId());

            mLocalGenerator.setInputFile(((PreviewConfig.FileSource) config.mDataSource).mSourceFileList);
            mLocalGenerator.setLoop(((PreviewConfig.FileSource) config.mDataSource).mLoop);
            mLocalGenerator.setSpeed(((PreviewConfig.FileSource) config.mDataSource).mSpeedRate);
            mLocalGenerator.start(((PreviewConfig.FileSource) config.mDataSource).mListener);
        }
    }

    private void handleRestartPreview(PreviewConfig config) {
        mPreviewConfig = config;
        if (config.mDataSource.mSource == PreviewConfig.SOURCE_CAMERA) {
            mCameraGenerator.stop();
            mProcessor.prepare(((PreviewConfig.CameraSource) config.mDataSource).mRotation);
            mCameraGenerator.setOutputTexture(mProcessor.getOesTextureId());
            mLocalGenerator.setOutputTexture(mProcessor.getOesTextureId());
            mCameraGenerator.start(((PreviewConfig.CameraSource) config.mDataSource).mForceCameraV1);
        } else if (config.mDataSource.mSource == PreviewConfig.SOURCE_FILE) {
            mLocalGenerator.stop();
            mProcessor.prepare(((PreviewConfig.FileSource) config.mDataSource).mRotation);
            mCameraGenerator.setOutputTexture(mProcessor.getOesTextureId());
            mLocalGenerator.setOutputTexture(mProcessor.getOesTextureId());
            mLocalGenerator.setInputFile(((PreviewConfig.FileSource) config.mDataSource).mSourceFileList);
            mLocalGenerator.setLoop(((PreviewConfig.FileSource) config.mDataSource).mLoop);
            mLocalGenerator.setSpeed(((PreviewConfig.FileSource) config.mDataSource).mSpeedRate);
            mLocalGenerator.start(((PreviewConfig.FileSource) config.mDataSource).mListener);
        }
    }

    private void handleSetPreviewFilter(GPUImageFilter filter) {
        if (filter != null) {
            filter.init();
            GLES20.glUseProgram(filter.getProgram());
            filter.onOutputSizeChanged(mPreviewConfig.mSurfaceWidth, mPreviewConfig.mSurfaceHeight);
        }
        mProcessor.setPreviewFilter(filter);
    }

    private void handleStartSave(int clipTop,
                                 int clipLeft,
                                 int clipBottom,
                                 int clipRight,
                                 File outputFile,
                                 int rotation,
                                 SaveListener saveListener) {
        if (mPreviewConfig == null) {
            mCameraGenerator = new CameraStreamGenerator();
            mLocalGenerator = new LocalStreamGenerator();
            mProcessor = new AVStreamProcessor();
            mSaver = new AVStreamSaver();
        }

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
        mSaver.beginSave(outputFile, rotation, saveListener);
    }

    private void handleSetSaveFilter(GPUImageFilter filter) {
        if (filter != null) {
            filter.init();
            GLES20.glUseProgram(filter.getProgram());
            filter.onOutputSizeChanged(mPreviewConfig.mSurfaceWidth, mPreviewConfig.mSurfaceHeight);
        }
        mProcessor.setSaveFilter(filter);
    }

    private void handleFinishSave() {
        mProcessor.clearSaveSurface();
        mSaver.finishSave();
    }

    private void handleCancelSave() {
        if (mProcessor != null) {
            mProcessor.clearSaveSurface();
        }
        if (mSaver != null) {
            mSaver.cancelSave();
        }
    }

    private void handleReset() {
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
        handleCancelSave();

        mPreviewConfig = null;
        mCameraGenerator = null;
        mLocalGenerator = null;
        mProcessor = null;
        mSaver = null;
    }
}
