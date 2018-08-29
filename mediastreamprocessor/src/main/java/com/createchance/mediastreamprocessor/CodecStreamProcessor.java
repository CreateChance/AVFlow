package com.createchance.mediastreamprocessor;

import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Surface;

import com.createchance.mediastreambase.AVFrame;
import com.createchance.mediastreambase.AbstractStreamProcessor;
import com.createchance.mediastreamprocessor.gles.EglCore;
import com.createchance.mediastreamprocessor.gles.WindowSurface;
import com.createchance.mediastreamprocessor.gpuimage.OpenGlUtils;

/**
 * ${DESC}
 *
 * @author createchance
 * @date 2018/8/27
 */
public final class CodecStreamProcessor extends AbstractStreamProcessor {

    private static final String TAG = "CodecStreamProcessor";

    private SurfaceTexture mSurfaceTexture;
    private OutputSurface mSaveOutputSurface;
    private InputSurface mInputSurface;

    private EglCore mEglCore;
    private WindowSurface mDisplaySurface;

    @Override
    protected boolean init() {
        Log.d(TAG, "init: ");

        // init egl
        mEglCore = new EglCore(null, EglCore.FLAG_RECORDABLE);
        mDisplaySurface = new WindowSurface(mEglCore, holder.getSurface(), false);
        mDisplaySurface.makeCurrent();

        mSaveOutputSurface = new OutputSurface(getSaveSurface());

//        mInputSurface = new InputSurface()

        mSurfaceTexture = new SurfaceTexture(OpenGlUtils.createOesTexture());
        mVideoInputSurface = new Surface(mSurfaceTexture);

        return true;
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
