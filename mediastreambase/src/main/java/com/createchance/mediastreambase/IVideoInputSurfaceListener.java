package com.createchance.mediastreambase;

/**
 * ${DESC}
 *
 * @author gaochao1-iri
 * @date 2018/9/1
 */
public interface IVideoInputSurfaceListener {
    void onConsumerSurfaceCreated(IVideoStreamConsumer consumer, VideoInputSurface inputSurface);

    void onConsumerSurfaceChanged(
            IVideoStreamConsumer consumer,
            VideoInputSurface inputSurface,
            int width,
            int height);

    void onConsumerSurfaceDestroyed(IVideoStreamConsumer consumer, VideoInputSurface inputSurface);
}
