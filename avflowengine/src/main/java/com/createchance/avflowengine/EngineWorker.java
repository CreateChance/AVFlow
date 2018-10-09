package com.createchance.avflowengine;

import android.opengl.GLES20;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.createchance.avflowengine.base.Logger;
import com.createchance.avflowengine.config.AbstractInputConfig;
import com.createchance.avflowengine.config.AbstractOutputConfig;
import com.createchance.avflowengine.config.CameraInputConfig;
import com.createchance.avflowengine.config.FileInputConfig;
import com.createchance.avflowengine.config.PreviewOutputConfig;
import com.createchance.avflowengine.config.SaveOutputConfig;
import com.createchance.avflowengine.generator.CameraImpl;
import com.createchance.avflowengine.generator.CameraStreamGenerator;
import com.createchance.avflowengine.generator.LocalStreamGenerator;
import com.createchance.avflowengine.processor.AVStreamProcessor;
import com.createchance.avflowengine.processor.gpuimage.GPUImageFilter;
import com.createchance.avflowengine.saver.AVStreamSaver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Engine worker, all engine work done in this thread.
 *
 * @author createchance
 * @date 2018-09-26
 */
final class EngineWorker extends HandlerThread {
    private static final String TAG = "EngineWorker";

    private static final int MSG_CONFIG_INPUT = 0;
    private static final int MSG_CONFIG_OUTPUT = 1;
    private static final int MSG_START = 2;
    private static final int MSG_STOP = 3;
    private static final int MSG_START_SAVE = 4;
    private static final int MSG_FINISH_SAVE = 5;
    private static final int MSG_CANCEL_SAVE = 6;
    private static final int MSG_SET_SAVE_FILTER = 7;
    private static final int MSG_SET_PREVIEW_FILTER = 8;
    private static final int MSG_SET_PREVIEW_TEXT = 9;
    private static final int MSG_SET_SAVE_TEXT = 10;

    private CameraStreamGenerator mCameraGenerator;
    private LocalStreamGenerator mLocalGenerator;
    private AVStreamProcessor mProcessor;
    private AVStreamSaver mSaver;

    private CameraInputConfig mCameraInputConfig;
    private FileInputConfig mFileInputConfig;
    private PreviewOutputConfig mPreviewOutputConfig;
    private SaveOutputConfig mSaveOutputConfig;

    private File mOutputFile;

    private boolean mOutputConfigDone;

    private Handler mHandler;

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
                    case MSG_CONFIG_INPUT:
                        handleConfigInput((AbstractInputConfig) msg.obj);
                        break;
                    case MSG_CONFIG_OUTPUT:
                        handleConfigOutput((AbstractOutputConfig) msg.obj);
                        break;
                    case MSG_START:
                        handleStartWork();
                        break;
                    case MSG_STOP:
                        handleStopWork();
                        break;
                    case MSG_START_SAVE:
                        handleStartSave(mOutputFile);
                        break;
                    case MSG_FINISH_SAVE:
                        handleFinishSave();
                        break;
                    case MSG_CANCEL_SAVE:
                        handleCancelSave();
                        break;
                    case MSG_SET_SAVE_FILTER:
                        handleSetSaveFilter((GPUImageFilter) msg.obj);
                        break;
                    case MSG_SET_PREVIEW_FILTER:
                        handleSetPreviewFilter((GPUImageFilter) msg.obj);
                        break;
                    case MSG_SET_PREVIEW_TEXT:
                        List<Object> previewTextParams = (List<Object>) msg.obj;
                        handleSetPreviewText((String) previewTextParams.get(0),
                                (String) previewTextParams.get(1),
                                (int) previewTextParams.get(2),
                                (int) previewTextParams.get(3),
                                (int) previewTextParams.get(4),
                                (float) previewTextParams.get(5),
                                (float) previewTextParams.get(6),
                                (float) previewTextParams.get(7));
                        break;
                    case MSG_SET_SAVE_TEXT:
                        List<Object> saveTextParams = (List<Object>) msg.obj;
                        handleSetSaveText((String) saveTextParams.get(0),
                                (String) saveTextParams.get(1),
                                (int) saveTextParams.get(2),
                                (int) saveTextParams.get(3),
                                (int) saveTextParams.get(4),
                                (float) saveTextParams.get(5),
                                (float) saveTextParams.get(6),
                                (float) saveTextParams.get(7));
                        break;
                    default:
                        break;
                }
            }
        };
        mCameraGenerator = new CameraStreamGenerator();
        mLocalGenerator = new LocalStreamGenerator();
        mProcessor = new AVStreamProcessor();
        mSaver = new AVStreamSaver();
    }

    void configInput(AbstractInputConfig config) {
        Message message = Message.obtain();
        message.what = MSG_CONFIG_INPUT;
        message.obj = config;
        mHandler.sendMessage(message);
    }

    void configOutput(AbstractOutputConfig config) {
        Message message = Message.obtain();
        message.what = MSG_CONFIG_OUTPUT;
        message.obj = config;
        mHandler.sendMessage(message);
    }

    void startWork() {
        Message message = Message.obtain();
        message.what = MSG_START;
        mHandler.sendMessage(message);
    }

    void stopWork() {
        Message message = Message.obtain();
        message.what = MSG_STOP;
        mHandler.sendMessage(message);
    }

    void startSave(File outputFile) {
        mOutputFile = outputFile;

        mHandler.sendEmptyMessage(MSG_START_SAVE);
    }

    void setPreviewFilter(GPUImageFilter filter) {
        if (mPreviewOutputConfig == null) {
            Logger.d(TAG, "Preview not initialized!");
            return;
        }

        Message message = Message.obtain();
        message.what = MSG_SET_PREVIEW_FILTER;
        message.obj = filter;
        mHandler.sendMessage(message);
    }

    void setSaveFilter(GPUImageFilter filter) {
        if (mSaveOutputConfig == null) {
            Logger.d(TAG, "Save not initialized!");
            return;
        }

        Message message = Message.obtain();
        message.what = MSG_SET_SAVE_FILTER;
        message.obj = filter;
        mHandler.sendMessage(message);
    }

    void setPreviewText(String fontPath,
                        String text,
                        int posX,
                        int posY,
                        int textSize,
                        float red,
                        float green,
                        float blue) {
        if (mPreviewOutputConfig == null) {
            Logger.d(TAG, "Preview not initialized!");
            return;
        }

        Message message = Message.obtain();
        message.what = MSG_SET_PREVIEW_TEXT;
        List<Object> params = new ArrayList<>(8);
        params.add(fontPath);
        params.add(text);
        params.add(posX);
        params.add(posY);
        params.add(textSize);
        params.add(red);
        params.add(green);
        params.add(blue);
        message.obj = params;
        mHandler.sendMessage(message);
    }

    void setSaveText(String fontPath,
                     String text,
                     int posX,
                     int posY,
                     int textSize,
                     float red,
                     float green,
                     float blue) {
        if (mSaveOutputConfig == null) {
            Logger.d(TAG, "Save not initialized!");
            return;
        }

        Message message = Message.obtain();
        message.what = MSG_SET_SAVE_TEXT;
        List<Object> params = new ArrayList<>(8);
        params.add(fontPath);
        params.add(text);
        params.add(posX);
        params.add(posY);
        params.add(textSize);
        params.add(red);
        params.add(green);
        params.add(blue);
        message.obj = params;
        mHandler.sendMessage(message);
    }

    void finishSave() {
        if (mSaveOutputConfig == null) {
            Logger.d(TAG, "Save not initialized!");
            return;
        }

        mHandler.sendEmptyMessage(MSG_FINISH_SAVE);
    }

    void cancelSave() {
        if (mSaveOutputConfig == null) {
            Logger.d(TAG, "Save not initialized!");
            return;
        }

        mHandler.sendEmptyMessage(MSG_CANCEL_SAVE);
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

    private void handleConfigInput(AbstractInputConfig config) {
        if (mCameraInputConfig != null) {
            Logger.e(TAG, "Camera input has config already.");
            return;
        }

        switch (config.getType()) {
            case AbstractInputConfig.TYPE_CAMERA:
                mCameraInputConfig = (CameraInputConfig) config;
                handleCameraInput();
                break;
            case AbstractInputConfig.TYPE_FILE:
                if (mFileInputConfig == null) {
                    mFileInputConfig = (FileInputConfig) config;
                    handleFileInput();
                } else {
                    mFileInputConfig = (FileInputConfig) config;
                    updateFileInput();
                }
                break;
            default:
                break;
        }
    }

    private void handleConfigOutput(AbstractOutputConfig config) {
        switch (config.mType) {
            case AbstractOutputConfig.TYPE_PREVIEW:
                mPreviewOutputConfig = (PreviewOutputConfig) config;
                handlePreviewOutput();
                break;
            case AbstractOutputConfig.TYPE_SAVE:
                mSaveOutputConfig = (SaveOutputConfig) config;
                handleSaveOutput();
                break;
            default:
                break;
        }
    }

    private void handleStartWork() {
        if (mCameraInputConfig != null) {
            mCameraGenerator.start(mCameraInputConfig.isForceCameraV1());
        } else {
            mLocalGenerator.start(mFileInputConfig.getListener());
        }
    }

    private void handleStopWork() {
        if (mCameraInputConfig != null) {
            mCameraGenerator.stop();
        } else if (mFileInputConfig != null) {
            mLocalGenerator.stop();
        }

        mProcessor.stop();

        handleCancelSave();
    }

    private void handleCameraInput() {
        mProcessor.setOesSurfaceSize(
                mCameraInputConfig.getSurfaceWidth(),
                mCameraInputConfig.getSurfaceHeight());
    }

    private void handleFileInput() {
        mProcessor.setOesSurfaceSize(
                mFileInputConfig.getSurfaceWidth(),
                mFileInputConfig.getSurfaceHeight());
        mLocalGenerator.setInputFile(mFileInputConfig.getInputFiles());
        mLocalGenerator.setLoop(mFileInputConfig.isLoop());
        mLocalGenerator.setSpeed(mFileInputConfig.getSpeedRate());
    }

    private void updateFileInput() {
        mLocalGenerator.stop();
        mLocalGenerator.setInputFile(mFileInputConfig.getInputFiles());
        mLocalGenerator.setLoop(mFileInputConfig.isLoop());
        mLocalGenerator.setSpeed(mFileInputConfig.getSpeedRate());
        mLocalGenerator.start(mFileInputConfig.getListener());
    }

    private void handlePreviewOutput() {
        mProcessor.setPreviewSurface(mPreviewOutputConfig.getSurface());

        if (mOutputConfigDone) {
            return;
        }

        mOutputConfigDone = true;
        if (mCameraInputConfig != null) {
            mProcessor.prepare(mCameraInputConfig.getRotation());
        } else if (mFileInputConfig != null) {
            mProcessor.prepare(mFileInputConfig.getRotation());
        }
        mCameraGenerator.setOutputTexture(mProcessor.getOesTextureId());
        mLocalGenerator.setOutputTexture(mProcessor.getOesTextureId());
    }

    private void handleSaveOutput() {
        mSaver.setOutputSize(
                mSaveOutputConfig.getClipRight() - mSaveOutputConfig.getClipLeft(),
                mSaveOutputConfig.getClipBottom() - mSaveOutputConfig.getClipTop());
        mSaver.setFrameRate(mSaveOutputConfig.getFrameRate());
        mSaver.setBitrate(mSaveOutputConfig.getBitrate());
        mSaver.prepare();
        mProcessor.setSaveSurface(
                mSaver.getInputSurface(),
                mSaveOutputConfig.getClipTop(),
                mSaveOutputConfig.getClipLeft(),
                mSaveOutputConfig.getClipBottom(),
                mSaveOutputConfig.getClipRight());

        Logger.d(TAG, "Clip top: "
                + mSaveOutputConfig.getClipTop()
                + ", clip left: "
                + mSaveOutputConfig.getClipLeft()
                + ", clip bottom: "
                + mSaveOutputConfig.getClipBottom()
                + ", clip right: "
                + mSaveOutputConfig.getClipRight());

        if (mOutputConfigDone) {
            return;
        }

        mOutputConfigDone = true;
        if (mCameraInputConfig != null) {
            mProcessor.prepare(mCameraInputConfig.getRotation());
        } else if (mFileInputConfig != null) {
            mProcessor.prepare(mFileInputConfig.getRotation());
        }
        mCameraGenerator.setOutputTexture(mProcessor.getOesTextureId());
        mLocalGenerator.setOutputTexture(mProcessor.getOesTextureId());
    }

    private void handleSetPreviewFilter(GPUImageFilter filter) {
        if (filter != null) {
            filter.init();
            GLES20.glUseProgram(filter.getProgram());
            filter.onOutputSizeChanged(
                    mPreviewOutputConfig.getSurfaceWidth(),
                    mPreviewOutputConfig.getSurfaceHeight());
        }
        mProcessor.setPreviewFilter(filter);
    }

    private void handleStartSave(File outputFile) {
        mSaver.beginSave(
                outputFile,
                mSaveOutputConfig.getOutputRotation(),
                mSaveOutputConfig.getSaveListener());
    }

    private void handleSetSaveFilter(GPUImageFilter filter) {
        if (filter != null) {
            filter.init();
            GLES20.glUseProgram(filter.getProgram());
            filter.onOutputSizeChanged(
                    mSaveOutputConfig.getClipRight() - mSaveOutputConfig.getClipLeft(),
                    mSaveOutputConfig.getClipBottom() - mSaveOutputConfig.getClipTop());
        }
        mProcessor.setSaveFilter(filter);
    }

    private void handleSetPreviewText(String fontPath,
                                      String text,
                                      int posX,
                                      int posY,
                                      int textSize,
                                      float red,
                                      float green,
                                      float blue) {
        mProcessor.setPreviewText(fontPath, text, posX, posY, textSize, red, green, blue);
    }

    private void handleSetSaveText(String fontPath,
                                   String text,
                                   int posX,
                                   int posY,
                                   int textSize,
                                   float red,
                                   float green,
                                   float blue) {
        mProcessor.setSaveText(fontPath, text, posX, posY, textSize, red, green, blue);
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
}
