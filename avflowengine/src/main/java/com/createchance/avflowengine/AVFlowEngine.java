package com.createchance.avflowengine;

import android.text.TextUtils;

import com.createchance.avflowengine.base.Logger;
import com.createchance.avflowengine.generator.CameraImpl;
import com.createchance.avflowengine.processor.gpuimage.GPUImageFilter;
import com.createchance.avflowengine.saver.SaveListener;

import java.io.File;
import java.util.HashMap;
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

    public void startPreview(String token, PreviewConfig config) {
        if (checkToken(token)) {
            EngineWorker worker = mWorkMap.get(token);
            worker.startPreview(config);
        }
    }

    public void restartPreview(String token, PreviewConfig config) {
        if (checkToken(token)) {
            EngineWorker worker = mWorkMap.get(token);
            worker.restartPreview(config);
        }
    }

    public void setPreviewFilter(String token, GPUImageFilter filter) {
        if (checkToken(token)) {
            EngineWorker worker = mWorkMap.get(token);
            worker.setPreviewFilter(filter);
        }
    }

    public void startSave(String token,
                          int clipTop,
                          int clipLeft,
                          int clipBottom,
                          int clipRight,
                          File outputFile,
                          int rotation,
                          SaveListener saveListener) {
        if (checkToken(token)) {
            EngineWorker worker = mWorkMap.get(token);
            worker.startSave(clipTop, clipLeft, clipBottom, clipRight, outputFile, rotation, saveListener);
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

    public void reset(String token) {
        if (checkToken(token)) {
            EngineWorker worker = mWorkMap.get(token);
            worker.reset();
            // remove this worker after reset.
            mWorkMap.remove(token);
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
}
