package com.createchance.mediastreambase;

import android.graphics.SurfaceTexture;

/**
 * ${DESC}
 *
 * @author gaochao1-iri
 * @date 2018/8/27
 */
public abstract class AbstractStreamGenerator extends AbstractStreamNode {

    protected AbstractStreamProcessor mProcessor;

    protected final SurfaceTexture getVideoSurfaceTexture() {
        return mProcessor.mSurfaceTexture;
    }

    protected abstract void run();

    protected final void outputAudioFrame(AVFrame audioFrame) {
        mProcessor.onAudioFrame(audioFrame);
    }

    protected final void outputVideoFrame(AVFrame videoFrame) {
        mProcessor.onVideoFrame(videoFrame);
    }
}
