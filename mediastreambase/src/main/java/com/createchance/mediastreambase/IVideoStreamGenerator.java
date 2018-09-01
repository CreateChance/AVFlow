package com.createchance.mediastreambase;

/**
 * ${DESC}
 *
 * @author createchance
 * @date 2018/8/27
 */
public interface IVideoStreamGenerator {

    void start();

    void stop();

    void setConsumer(IVideoStreamConsumer consumer);
}
