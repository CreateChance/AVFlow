package com.createchance.avflowengine.generator;

import java.io.File;

/**
 * Video play listener
 *
 * @author createchance
 * @date 2018-09-19
 */
public interface FilePlayListener {
    void onListPlayStarted();

    void onFilePlayStarted(int position, File file);

    void onFilePlayGoing(long currentTime, long duration, long globalTime, long totalDuration, File file);

    void onFilePlayDone(int position, File file);

    void onListPlayDone();
}
