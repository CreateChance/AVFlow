package com.createchance.mediastreambase;

import android.graphics.SurfaceTexture;

/**
 * ${DESC}
 *
 * @author createchance
 * @date 2018/8/27
 */
public abstract class AbstractStreamSaver extends AbstractStreamNode {
    SurfaceTexture mSurfaceTexture;

    protected abstract void onAudioFrame(AVFrame audioFrame);

    protected abstract void onVideoFrame(AVFrame videoFrame);
}
