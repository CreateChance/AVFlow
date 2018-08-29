package com.createchance.mediastreambase;

import android.view.Surface;

/**
 * ${DESC}
 *
 * @author createchance
 * @date 2018/8/27
 */
public abstract class AbstractStreamProcessor extends AbstractStreamNode {
    protected Surface mVideoInputSurface;
    AbstractStreamPreviewer mPreviewer;
    AbstractStreamSaver mSaver;

    protected Surface getPreviewSurface() {
        return mPreviewer == null ? null : mPreviewer.mSurface;
    }

    protected Surface getSaveSurface() {
        return mSaver == null ? null : mSaver.mSurface;
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
