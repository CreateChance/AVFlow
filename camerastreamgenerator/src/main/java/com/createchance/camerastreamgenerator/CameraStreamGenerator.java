package com.createchance.camerastreamgenerator;

import android.util.Log;

import com.createchance.mediastreambase.AbstractStreamGenerator;

/**
 * ${DESC}
 *
 * @author createchance
 * @date 2018/8/27
 */
public final class CameraStreamGenerator extends AbstractStreamGenerator {

    private static final String TAG = "CameraStreamGenerator";

    @Override
    protected boolean init() {
        Log.d(TAG, "init: ");

        return true;
    }

    @Override
    protected void shutdown() {
        Log.d(TAG, "shutdown: ");
    }

    @Override
    protected void run() {

    }
}
