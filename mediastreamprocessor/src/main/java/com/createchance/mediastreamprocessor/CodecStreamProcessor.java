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
import com.createchance.mediastreamprocessor.gpuimage.GPUImageFilter;
import com.createchance.mediastreamprocessor.gpuimage.OpenGlUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * ${DESC}
 *
 * @author createchance
 * @date 2018/8/27
 */
public final class CodecStreamProcessor implements IVideoStreamConsumer,
        IVideoStreamGenerator,
        SurfaceTexture.OnFrameAvailableListener,
        VideoFrameDrawer.DrawerListener {

    private static final String TAG = "CodecStreamProcessor";

    private VideoInputSurface mVideoInputSurface;

    private EglCore mEglCore;

    private final List<VideoFrameDrawer> mVideoFrameDrawers;

    private IVideoInputSurfaceListener mListener;

    private int mOesTextureId = -1;

    private CodecStreamProcessor() {
        mVideoFrameDrawers = new ArrayList<>();
        // init egl
        mEglCore = new EglCore(null, EglCore.FLAG_RECORDABLE);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        Logger.i(TAG, "onFrameAvailable");
        mVideoInputSurface.mSurfaceTexture.updateTexImage();
        for (VideoFrameDrawer drawer : mVideoFrameDrawers) {
            drawer.draw();
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

    }

    @Override
    public void stop() {
        for (VideoFrameDrawer drawer : mVideoFrameDrawers) {
            drawer.release();
        }

        mEglCore.release();
    }

    @Override
    public void setConsumer(IVideoStreamConsumer consumer) {
        if (consumer == null) {
            Logger.e(TAG, "Consumer can not be null!");
            return;
        }

        VideoFrameDrawer drawer = new VideoFrameDrawer(mEglCore, consumer, this);
        mVideoFrameDrawers.add(drawer);
    }

    @Override
    public void onSurfaceConfigDone(VideoFrameDrawer drawer) {
        if (mOesTextureId == -1) {
            createOesTexture();
        }

        for (VideoFrameDrawer videoFrameDrawer : mVideoFrameDrawers) {
            videoFrameDrawer.setOesTextureId(mOesTextureId, 1080, 1920);
        }
    }

    @Override
    public void onDestroyed(VideoFrameDrawer drawer) {
        synchronized (mVideoFrameDrawers) {
            mVideoFrameDrawers.remove(drawer);
        }
    }

    public boolean setFilter(IVideoStreamConsumer consumer, GPUImageFilter filter) {
        for (VideoFrameDrawer drawer : mVideoFrameDrawers) {
            if (drawer.getConsumer() == consumer) {
                drawer.setGpuImageFilter(filter);
                return true;
            }
        }

        return false;
    }

    private void createOesTexture() {
        mVideoInputSurface = new VideoInputSurface();
        int oesTextureId = OpenGlUtils.createOesTexture();
        mVideoInputSurface.mSurfaceTexture = new SurfaceTexture(oesTextureId);
        mVideoInputSurface.mSurface = new Surface(mVideoInputSurface.mSurfaceTexture);
        mVideoInputSurface.mSurfaceTexture.setOnFrameAvailableListener(this);
        if (mListener != null) {
            mListener.onConsumerSurfaceCreated(this, mVideoInputSurface);
        }
    }

    public static class Builder {
        private CodecStreamProcessor processor = new CodecStreamProcessor();

        public CodecStreamProcessor build() {
            return processor;
        }
    }
}
