package com.createchance.mediastreambase;

import android.graphics.SurfaceTexture;

/**
 * ${DESC}
 *
 * @author gaochao1-iri
 * @date 2018/8/27
 */
public abstract class AbstractStreamProcessor extends AbstractStreamNode {
    protected SurfaceTexture mSurfaceTexture;
    AbstractStreamPreviewer mPreviewer;
    AbstractStreamSaver mSaver;

    protected SurfaceTexture getPreviewTexture() {
        return mPreviewer == null ? null : mPreviewer.mSurfaceTexture;
    }

    protected SurfaceTexture getSaveTexture() {
        return mSaver == null ? null : mSaver.mSurfaceTexture;
    }

    protected abstract void onAudioFrame(AVFrame audioFrame);

    protected abstract void onVideoFrame(AVFrame videoFrame);

    protected final void outputAudioFrame(AVFrame audioFrame) {
        if (mPreviewer != null) {
            mPreviewer.onAudioFrame(audioFrame);
        }
        if (mSaver != null) {
            mSaver.onAudioFrame(audioFrame);
        }
    }

    protected final void outputVideoFrame(AVFrame videoFrame) {
        if (mPreviewer != null) {
            mPreviewer.onVideoFrame(videoFrame);
        }
        if (mSaver != null) {
            mSaver.onVideoFrame(videoFrame);
        }
    }
}
