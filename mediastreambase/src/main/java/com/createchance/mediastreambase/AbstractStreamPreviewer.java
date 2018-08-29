package com.createchance.mediastreambase;

import android.view.Surface;

/**
 * ${DESC}
 *
 * @author createchance
 * @date 2018/8/27
 */
public abstract class AbstractStreamPreviewer extends AbstractStreamNode {
    Surface mSurface;

    protected abstract void onAudioFrame(AVFrame audioFrame);

    protected abstract void onVideoFrame(AVFrame videoFrame);
}
