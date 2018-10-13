package com.createchance.avflowengine;

import android.graphics.Bitmap;
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
    private static final int MSG_UPDATE_PREVIEW_TEXT_PARAMS = 11;
    private static final int MSG_UPDATE_SAVE_TEXT_PARAMS = 12;
    private static final int MSG_REMOVE_PREVIEW_TEXT = 13;
    private static final int MSG_REMOVE_SAVE_TEXT = 14;
    private static final int MSG_SET_PREVIEW_IMAGE = 15;
    private static final int MSG_SET_SAVE_IMAGE = 16;
    private static final int MSG_REMOVE_PREVIEW_IMAGE = 17;
    private static final int MSG_REMOVE_SAVE_IMAGE = 18;

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
                                (float) previewTextParams.get(7),
                                (Bitmap) previewTextParams.get(8));
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
                                (float) saveTextParams.get(7),
                                (Bitmap) saveTextParams.get(8));
                        break;
                    case MSG_UPDATE_PREVIEW_TEXT_PARAMS:
                        List<Object> updatePreviewParams = (List<Object>) msg.obj;
                        handleUpdatePreviewTextParams((int) updatePreviewParams.get(0),
                                (int) updatePreviewParams.get(1),
                                (float) updatePreviewParams.get(2),
                                (float) updatePreviewParams.get(3),
                                (float) updatePreviewParams.get(4));
                        break;
                    case MSG_UPDATE_SAVE_TEXT_PARAMS:
                        List<Object> updateSaveParams = (List<Object>) msg.obj;
                        handleUpdateSaveTextParams((int) updateSaveParams.get(0),
                                (int) updateSaveParams.get(1),
                                (float) updateSaveParams.get(2),
                                (float) updateSaveParams.get(3),
                                (float) updateSaveParams.get(4));
                        break;
                    case MSG_REMOVE_PREVIEW_TEXT:
                        handleRemovePreviewText();
                        break;
                    case MSG_REMOVE_SAVE_TEXT:
                        handleRemoveSaveText();
                        break;
                    case MSG_SET_PREVIEW_IMAGE:
                        List<Object> setPreviewImageParams = (List<Object>) msg.obj;
                        handleSetPreviewImage((List<String>) setPreviewImageParams.get(0),
                                (int) setPreviewImageParams.get(1),
                                (int) setPreviewImageParams.get(2),
                                (float) setPreviewImageParams.get(3));
                        break;
                    case MSG_SET_SAVE_IMAGE:
                        List<Object> setSaveImageParams = (List<Object>) msg.obj;
                        handleSetSaveImage((List<String>) setSaveImageParams.get(0),
                                (int) setSaveImageParams.get(1),
                                (int) setSaveImageParams.get(2),
                                (float) setSaveImageParams.get(3));
                        break;
                    case MSG_REMOVE_PREVIEW_IMAGE:
                        handleRemovePreviewImage();
                        break;
                    case MSG_REMOVE_SAVE_IMAGE:
                        handleRemoveSaveImage();
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
                        float blue,
                        Bitmap background) {
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
        params.add(background);
        message.obj = params;
        mHandler.sendMessage(message);
    }

    void updatePreviewTextParams(int posX,
                                 int posY,
                                 float red,
                                 float green,
                                 float blue) {
        if (mPreviewOutputConfig == null) {
            Logger.d(TAG, "Preview not initialized!");
            return;
        }

        Message message = Message.obtain();
        message.what = MSG_UPDATE_PREVIEW_TEXT_PARAMS;
        List<Object> params = new ArrayList<>(5);
        params.add(posX);
        params.add(posY);
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
                     float blue,
                     Bitmap background) {
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
        params.add(background);
        message.obj = params;
        mHandler.sendMessage(message);
    }

    void updateSaveTextParams(int posX,
                              int posY,
                              float red,
                              float green,
                              float blue) {
        if (mSaveOutputConfig == null) {
            Logger.d(TAG, "Save not initialized!");
            return;
        }

        Message message = Message.obtain();
        message.what = MSG_UPDATE_SAVE_TEXT_PARAMS;
        List<Object> params = new ArrayList<>(5);
        params.add(posX);
        params.add(posY);
        params.add(red);
        params.add(green);
        params.add(blue);
        message.obj = params;
        mHandler.sendMessage(message);
    }

    void removePreviewText() {
        mHandler.sendEmptyMessage(MSG_REMOVE_PREVIEW_TEXT);
    }

    void removeSaveText() {
        mHandler.sendEmptyMessage(MSG_REMOVE_SAVE_TEXT);
    }

    void setPreviewImage(List<String> imageList, int posX, int posY, float scaleFactor) {
        if (mPreviewOutputConfig == null) {
            Logger.d(TAG, "Preview not initialized!");
            return;
        }

        Message message = Message.obtain();
        message.what = MSG_SET_PREVIEW_IMAGE;
        List<Object> params = new ArrayList<>(4);
        params.add(imageList);
        params.add(posX);
        params.add(posY);
        params.add(scaleFactor);
        message.obj = params;
        mHandler.sendMessage(message);
    }

    void setSaveImage(List<String> imageList, int posX, int posY, float scaleFactor) {
        if (mSaveOutputConfig == null) {
            Logger.d(TAG, "Save not initialized!");
            return;
        }

        Message message = Message.obtain();
        message.what = MSG_SET_SAVE_IMAGE;
        List<Object> params = new ArrayList<>(4);
        params.add(imageList);
        params.add(posX);
        params.add(posY);
        params.add(scaleFactor);
        message.obj = params;
        mHandler.sendMessage(message);
    }

    void removePreviewImage() {
        if (mPreviewOutputConfig == null) {
            Logger.d(TAG, "Preview not initialized!");
            return;
        }

        mHandler.sendEmptyMessage(MSG_REMOVE_PREVIEW_IMAGE);
    }

    void removeSaveImage() {
        if (mSaveOutputConfig == null) {
            Logger.d(TAG, "Save not initialized!");
            return;
        }

        mHandler.sendEmptyMessage(MSG_REMOVE_SAVE_IMAGE);
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
                                      float blue,
                                      Bitmap background) {
        mProcessor.setPreviewText(fontPath, text, posX, posY, textSize, red, green, blue, background);
    }

    private void handleSetSaveText(String fontPath,
                                   String text,
                                   int posX,
                                   int posY,
                                   int textSize,
                                   float red,
                                   float green,
                                   float blue,
                                   Bitmap background) {
        mProcessor.setSaveText(fontPath, text, posX, posY, textSize, red, green, blue, background);
    }

    private void handleUpdatePreviewTextParams(int posX,
                                               int posY,
                                               float red,
                                               float green,
                                               float blue) {
        mProcessor.updatePreviewTextParams(posX, posY, red, green, blue);
    }

    private void handleUpdateSaveTextParams(int posX,
                                            int posY,
                                            float red,
                                            float green,
                                            float blue) {
        mProcessor.updateSaveTextParams(posX, posY, red, green, blue);
    }

    private void handleSetPreviewImage(List<String> imageList, int posX, int posY, float scaleFactor) {
        mProcessor.setPreviewImage(imageList, posX, posY, scaleFactor);
    }

    private void handleSetSaveImage(List<String> imageList, int posX, int posY, float scaleFactor) {
        mProcessor.setSaveImage(imageList, posX, posY, scaleFactor);
    }

    private void handleRemovePreviewText() {
        mProcessor.removePreviewText();
    }

    private void handleRemoveSaveText() {
        mProcessor.removeSaveText();
    }

    private void handleRemovePreviewImage() {
        mProcessor.removePreviewImage();
    }

    private void handleRemoveSaveImage() {
        mProcessor.removeSaveImage();
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
