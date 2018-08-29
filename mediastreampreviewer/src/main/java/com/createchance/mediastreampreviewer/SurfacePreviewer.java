package com.createchance.mediastreampreviewer;

import android.util.Log;

import com.createchance.mediastreambase.AVFrame;
import com.createchance.mediastreambase.AbstractStreamPreviewer;

/**
 * ${DESC}
 *
 * @author gaochao1-iri
 * @date 2018/8/27
 */
public final class SurfacePreviewer extends AbstractStreamPreviewer {

    private static final String TAG = "SurfacePreviewer";

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
