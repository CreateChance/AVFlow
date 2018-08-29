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
public final class TexturePreviewer extends AbstractStreamPreviewer {

    private static final String TAG = "TexturePreviewer";

    @Override
    protected void preview(AVFrame avFrame) {
        Log.d(TAG, "preview: " + avFrame);
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
