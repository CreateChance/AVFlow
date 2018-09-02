package com.createchance.mediastreambase;

/**
 * ${DESC}
 *
 * @author createchance
 * @date 2018/9/1
 */
public interface IVideoStreamConsumer {
    void setInputSurfaceListener(IVideoInputSurfaceListener listener);

    void onNewVideoFrame(AVFrame videoFrame);
}
