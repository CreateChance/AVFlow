package com.createchance.avflowengine;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.createchance.avflowengine.base.Logger;
import com.createchance.avflowengine.config.AbstractInputConfig;
import com.createchance.avflowengine.config.AbstractOutputConfig;
import com.createchance.avflowengine.generator.CameraImpl;
import com.createchance.avflowengine.processor.FreeType;
import com.createchance.avflowengine.processor.gpuimage.GPUImageFilter;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * av flow engine.
 *
 * @author createchance
 * @date 2018-09-06
 */
public class AVFlowEngine {
    private static final String TAG = "AVFlowEngine";

    private static AVFlowEngine sInstance;

    private Map<String, EngineWorker> mWorkMap;

    private AVFlowEngine() {
    }

    public synchronized static AVFlowEngine getInstance() {
        if (sInstance == null) {
            sInstance = new AVFlowEngine();
            sInstance.mWorkMap = new HashMap<>();
            FreeType.init();
        }

        return sInstance;
    }

    public String newWorker() {
        EngineWorker worker = new EngineWorker("EngineWorkerThread");
        worker.startThread();
        String token = worker.getToken();
        mWorkMap.put(token, worker);

        return token;
    }

    public void configInput(String token, AbstractInputConfig config) {
        if (config == null) {
            Logger.e(TAG, "Invalid config: " + config);
            return;
        }

        if (checkToken(token)) {
            EngineWorker worker = mWorkMap.get(token);
            worker.configInput(config);
        }
    }

    public void configOutput(String token, AbstractOutputConfig config) {
        if (config == null) {
            Logger.e(TAG, "Invalid config: " + config);
            return;
        }

        if (checkToken(token)) {
            EngineWorker worker = mWorkMap.get(token);
            worker.configOutput(config);
        }
    }

    public void start(String token) {
        if (checkToken(token)) {
            EngineWorker worker = mWorkMap.get(token);
            worker.startWork();
        }
    }

    public void stop(String token) {
        if (checkToken(token)) {
            EngineWorker worker = mWorkMap.get(token);
            worker.stopWork();
            // remove this worker after reset.
            mWorkMap.remove(token);
        }
    }

    public void setPreviewFilter(String token, GPUImageFilter filter) {
        if (checkToken(token)) {
            EngineWorker worker = mWorkMap.get(token);
            worker.setPreviewFilter(filter);
        }
    }

    public void startSave(String token, File outputFile) {
        if (checkToken(token)) {
            EngineWorker worker = mWorkMap.get(token);
            worker.startSave(outputFile);
        }
    }

    public void setPreviewText(String token,
                               String fontPath,
                               String text,
                               int posX,
                               int posY,
                               int textSize,
                               float red,
                               float green,
                               float blue,
                               Bitmap background) {
        if (checkToken(token)) {
            EngineWorker worker = mWorkMap.get(token);
            worker.setPreviewText(fontPath, text, posX, posY, textSize, red, green, blue, background);
        }
    }

    public void removePreviewText(String token) {
        if (checkToken(token)) {
            EngineWorker worker = mWorkMap.get(token);
            worker.removePreviewText();
        }
    }

    public void updatePreviewTextParams(String token,
                                        int posX,
                                        int posY,
                                        float red,
                                        float green,
                                        float blue) {
        if (checkToken(token)) {
            EngineWorker worker = mWorkMap.get(token);
            worker.updatePreviewTextParams(posX, posY, red, green, blue);
        }
    }

    public void setSaveText(String token,
                            String fontPath,
                            String text,
                            int posX,
                            int posY,
                            int textSize,
                            float red,
                            float green,
                            float blue,
                            Bitmap background) {
        if (checkToken(token)) {
            EngineWorker worker = mWorkMap.get(token);
            worker.setSaveText(fontPath, text, posX, posY, textSize, red, green, blue, background);
        }
    }

    public void removeSaveText(String token) {
        if (checkToken(token)) {
            EngineWorker worker = mWorkMap.get(token);
            worker.removeSaveText();
        }
    }

    public void updateSaveTextParams(String token,
                                     int posX,
                                     int posY,
                                     float red,
                                     float green,
                                     float blue) {
        if (checkToken(token)) {
            EngineWorker worker = mWorkMap.get(token);
            worker.updateSaveTextParams(posX, posY, red, green, blue);
        }
    }

    public void setPreviewImage(String token, List<String> imageList, int posX, int posY, float scaleFactor) {
        if (checkToken(token)) {
            EngineWorker worker = mWorkMap.get(token);
            worker.setPreviewImage(imageList, posX, posY, scaleFactor);
        }
    }

    public void setSaveImage(String token, List<String> imageList, int posX, int posY, float scaleFactor) {
        if (checkToken(token)) {
            EngineWorker worker = mWorkMap.get(token);
            worker.setSaveImage(imageList, posX, posY, scaleFactor);
        }
    }

    public void removePreviewImage(String token) {
        if (checkToken(token)) {
            EngineWorker worker = mWorkMap.get(token);
            worker.removePreviewImage();
        }
    }

    public void removeSaveImage(String token) {
        if (checkToken(token)) {
            EngineWorker worker = mWorkMap.get(token);
            worker.removeSaveImage();
        }
    }

    public void setSaveFilter(String token, GPUImageFilter filter) {
        if (checkToken(token)) {
            EngineWorker worker = mWorkMap.get(token);
            worker.setSaveFilter(filter);
        }
    }

    public void finishSave(String token) {
        if (checkToken(token)) {
            EngineWorker worker = mWorkMap.get(token);
            worker.finishSave();
        }
    }

    public void cancelSave(String token) {
        if (checkToken(token)) {
            EngineWorker worker = mWorkMap.get(token);
            worker.cancelSave();
        }
    }

    public CameraImpl getCamera(String token) {
        if (checkToken(token)) {
            EngineWorker worker = mWorkMap.get(token);
            return worker.getCamera();
        }
        return null;
    }

    private boolean checkToken(String token) {
        if (TextUtils.isEmpty(token)) {
            Logger.e(TAG, "Invalid token: " + token);
            return false;
        }

        if (!mWorkMap.containsKey(token)) {
            Logger.e(TAG, "No worker has a token: " + token);
            return false;
        }

        return true;
    }

    public native void test(String fontPath, String text);
}
