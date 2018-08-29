package com.createchance.mediastreampreviewer;

import android.util.Log;

import com.createchance.mediastreambase.AVFrame;
import com.createchance.mediastreambase.AbstractStreamPreviewer;

/**
 * ${DESC}
 *
 * @author createchance
 * @date 2018/8/27
 */
public final class SurfacePreviewer extends AbstractStreamPreviewer {

    private static final String TAG = "SurfacePreviewer";

    @Override
    protected boolean init() {
        return false;
    }

    @Override
    protected void shutdown() {
        Log.d(TAG, "shutdown: ");
    }

    @Override
    protected void onAudioFrame(AVFrame audioFrame) {

    }

    @Override
    protected void onVideoFrame(AVFrame videoFrame) {

    }
}
