package com.createchance.mediastreamsaver;

import com.createchance.mediastreambase.AVFrame;
import com.createchance.mediastreambase.IVideoInputSurfaceListener;
import com.createchance.mediastreambase.IVideoStreamConsumer;

/**
 * ${DESC}
 *
 * @author createchance
 * @date 2018/8/27
 */
public final class MuxerStreamSaver implements IVideoStreamConsumer {

    private static final String TAG = "MuxerStreamSaver";

    @Override
    public void setInputSurfaceListener(IVideoInputSurfaceListener listener) {

    }

    @Override
    public void onNewVideoFrame(AVFrame videoFrame) {

    }
}
