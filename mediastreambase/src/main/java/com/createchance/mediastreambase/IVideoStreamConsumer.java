package com.createchance.mediastreambase;

/**
 * ${DESC}
 *
 * @author gaochao1-iri
 * @date 2018/9/1
 */
public interface IVideoStreamConsumer {
    void setInputSurfaceListener(IVideoInputSurfaceListener listener);

    void onNewVideoFrame(AVFrame videoFrame);
}
