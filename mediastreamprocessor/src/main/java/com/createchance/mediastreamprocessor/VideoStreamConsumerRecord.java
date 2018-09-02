package com.createchance.mediastreamprocessor;

import com.createchance.mediastreambase.IVideoStreamConsumer;
import com.createchance.mediastreambase.VideoInputSurface;
import com.createchance.mediastreamprocessor.gles.WindowSurface;

/**
 * ${DESC}
 *
 * @author gaochao1-iri
 * @date 2018/9/1
 */
class VideoStreamConsumerRecord {
    IVideoStreamConsumer mConsumer;
    VideoInputSurface mInputSurface;
    WindowSurface mDrawSurface;
    boolean mDestroyed;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        VideoStreamConsumerRecord record = (VideoStreamConsumerRecord) o;

        return mConsumer != null ? mConsumer.equals(record.mConsumer) : record.mConsumer == null;
    }

    @Override
    public int hashCode() {
        return mConsumer != null ? mConsumer.hashCode() : 0;
    }
}
