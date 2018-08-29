package com.createchance.mediastreamsaver;

import android.util.Log;

import com.createchance.mediastreambase.AVFrame;
import com.createchance.mediastreambase.AbstractStreamSaver;

/**
 * ${DESC}
 *
 * @author gaochao1-iri
 * @date 2018/8/27
 */
public final class MuxerStreamSaver extends AbstractStreamSaver {

    private static final String TAG = "MuxerStreamSaver";

    @Override
    protected void save(AVFrame avFrame) {
        Log.d(TAG, "save: " + avFrame);
    }

    @Override
    protected void init() {
        Log.d(TAG, "init: ");
    }

    @Override
    protected void shutdown() {
        Log.d(TAG, "shutdown: ");
    }
}
