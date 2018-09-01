package com.createchance.mediastreamprocessor;

import android.graphics.SurfaceTexture;
import android.view.Surface;

import com.createchance.mediastreambase.AVFrame;
import com.createchance.mediastreambase.IVideoInputSurfaceListener;
import com.createchance.mediastreambase.IVideoStreamConsumer;
import com.createchance.mediastreambase.IVideoStreamGenerator;
import com.createchance.mediastreambase.Logger;
import com.createchance.mediastreambase.VideoInputSurface;
import com.createchance.mediastreamprocessor.gles.EglCore;
import com.createchance.mediastreamprocessor.gles.WindowSurface;
import com.createchance.mediastreamprocessor.gpuimage.OpenGlUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * ${DESC}
 *
 * @author createchance
 * @date 2018/8/27
 */
public final class CodecStreamProcessor implements IVideoStreamConsumer,
        IVideoStreamGenerator, SurfaceTexture.OnFrameAvailableListener, IVideoInputSurfaceListener {

    private static final String TAG = "CodecStreamProcessor";

    private VideoInputSurface mVideoInputSurface;

    private EglCore mEglCore;

    private VideoFrameDrawer mVideoFrameDrawer;

    private Set<VideoStreamConsumerRecord> mConsumers = new HashSet<>();
    private IVideoInputSurfaceListener mListener;

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        Logger.i(TAG, "onFrameAvailable");
        mVideoInputSurface.mSurfaceTexture.updateTexImage();
        for (VideoStreamConsumerRecord record : mConsumers) {
            record.mDrawSurface.makeCurrent();
            mVideoFrameDrawer.draw();
            record.mDrawSurface.swapBuffers();
        }
    }

    @Override
    public void setInputSurfaceListener(IVideoInputSurfaceListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onNewVideoFrame(AVFrame videoFrame) {

    }

    @Override
    public void start() {
        // init egl
        mEglCore = new EglCore(null, EglCore.FLAG_RECORDABLE);

        for (VideoStreamConsumerRecord consumer : mConsumers) {
            if (consumer.mInputSurface != null) {
                Logger.d(TAG, "init consumer");
                consumer.mDrawSurface = new WindowSurface(mEglCore,
                        consumer.mInputSurface.mSurface,
                        false);
                consumer.mDrawSurface.makeCurrent();
            } else {
                Logger.e(TAG, "Consumer not init!");
            }
        }

        mVideoInputSurface = new VideoInputSurface();
        int oesTextureId = OpenGlUtils.createOesTexture();
        mVideoInputSurface.mSurfaceTexture = new SurfaceTexture(oesTextureId);
        mVideoInputSurface.mSurface = new Surface(mVideoInputSurface.mSurfaceTexture);
        mVideoInputSurface.mSurfaceTexture.setOnFrameAvailableListener(this);
        if (mListener != null) {
            mListener.onConsumerSurfaceInitDone(this, mVideoInputSurface);
        }

        mVideoFrameDrawer = new VideoFrameDrawer(oesTextureId, 1080, 1920);
    }

    @Override
    public void stop() {

    }

    @Override
    public void setConsumer(IVideoStreamConsumer consumer) {
        VideoStreamConsumerRecord record = new VideoStreamConsumerRecord();
        record.mConsumer = consumer;
        mConsumers.add(record);
        consumer.setInputSurfaceListener(this);
    }

    @Override
    public void onConsumerSurfaceInitDone(IVideoStreamConsumer consumer,
                                          VideoInputSurface inputSurface) {
        for (VideoStreamConsumerRecord record : mConsumers) {
            if (record.mConsumer == consumer) {
                record.mInputSurface = inputSurface;
                break;
            }
        }
    }
}
