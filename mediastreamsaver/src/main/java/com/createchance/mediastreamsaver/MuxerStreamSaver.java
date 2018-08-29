package com.createchance.mediastreamsaver;

import android.util.Log;

import com.createchance.mediastreambase.AVFrame;
import com.createchance.mediastreambase.AbstractStreamSaver;

/**
 * ${DESC}
 *
 * @author createchance
 * @date 2018/8/27
 */
public final class MuxerStreamSaver extends AbstractStreamSaver {

    private static final String TAG = "MuxerStreamSaver";

    @Override
    protected void onAudioFrame(AVFrame audioFrame) {

    }

    @Override
    protected void onVideoFrame(AVFrame videoFrame) {

    }

    @Override
    protected boolean init() {
        return true;
    }

    @Override
    protected void shutdown() {
        Log.d(TAG, "shutdown: ");
    }
}
