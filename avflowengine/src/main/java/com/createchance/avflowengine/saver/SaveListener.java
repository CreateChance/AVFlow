package com.createchance.avflowengine.saver;

import java.io.File;

/**
 * ${DESC}
 *
 * @author createchance
 * @date 2018/9/9
 */
public interface SaveListener {
    void onStarted(File file);

    void onSaveGoing(long currentDurationUs, File file);

    void onSaved(File file);
}
