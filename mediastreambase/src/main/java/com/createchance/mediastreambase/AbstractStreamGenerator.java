package com.createchance.mediastreambase;

import android.view.Surface;

/**
 * ${DESC}
 *
 * @author createchance
 * @date 2018/8/27
 */
public abstract class AbstractStreamGenerator extends AbstractStreamNode {

    protected AbstractStreamProcessor mProcessor;

    protected final Surface getVideoSurface() {
        return mProcessor.mVideoInputSurface;
    }

    protected abstract void run();

    protected final void outputAudioFrame(AVFrame audioFrame) {
        mProcessor.onAudioFrame(audioFrame);
    }

    protected final void outputVideoFrame(AVFrame videoFrame) {
        mProcessor.onVideoFrame(videoFrame);
    }
}
