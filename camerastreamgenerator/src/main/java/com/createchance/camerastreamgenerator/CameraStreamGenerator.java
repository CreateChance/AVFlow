package com.createchance.camerastreamgenerator;

import android.util.Log;

import com.createchance.mediastreambase.AVFrame;
import com.createchance.mediastreambase.AbstractStreamGenerator;

/**
 * ${DESC}
 *
 * @author gaochao1-iri
 * @date 2018/8/27
 */
public final class CameraStreamGenerator extends AbstractStreamGenerator {

    private static final String TAG = "CameraStreamGenerator";

    private int mTime;

    @Override
    protected void generate(AVFrame avFrame) {
        Log.d(TAG, "generate: " + avFrame);

        mTime++;
        if (mTime == 3) {
            avFrame.mIsLast = true;
        }
    }

    @Override
    protected boolean init() {
        Log.d(TAG, "init: ");
    }

    @Override
    protected void shutdown() {
        Log.d(TAG, "shutdown: ");
    }
}
