package com.createchance.avflowengine.processor;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.view.Surface;

import com.createchance.avflowengine.base.Logger;
import com.createchance.avflowengine.processor.gles.EglCore;
import com.createchance.avflowengine.processor.gles.WindowSurface;
import com.createchance.avflowengine.processor.gpuimage.GPUImageFilter;
import com.createchance.avflowengine.processor.gpuimage.OpenGlUtils;

/**
 * ${DESC}
 *
 * @author createchance
 * @date 2018/8/27
 */
public final class CodecStreamProcessor implements SurfaceTexture.OnFrameAvailableListener {

    private static final String TAG = "CodecStreamProcessor";

    private SurfaceTexture mVideoInputSurface;

    private EglCore mEglCore;

    private VideoFrameDrawer mOutputSurfaceDrawer;
    private Surface mPreviewSurface, mSaveSurface;
    private WindowSurface mPreviewDrawSurface, mSaveDrawSurface;
    private GPUImageFilter mPreviewFilter, mSaveFilter;

    private int mOesTextureId = -1;
    private OesTextureReader mOesReader;
    private TextureWriter mTextureWriter;

    private int mOesWidth, mOesHeight;

    public CodecStreamProcessor() {
        // init egl
        mEglCore = new EglCore(null, EglCore.FLAG_RECORDABLE);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        Logger.v(TAG, "onFrameAvailable");
        if (mVideoInputSurface != null) {
            mVideoInputSurface.updateTexImage();
            mOutputSurfaceDrawer.draw(mOesReader, mPreviewDrawSurface, mPreviewFilter, mTextureWriter);
            mOutputSurfaceDrawer.draw(mOesReader, mSaveDrawSurface, mPreviewFilter, mTextureWriter);
        }
    }

    public void setPreviewSurface(Surface surface) {
        if (surface == null) {
            Logger.e(TAG, "Surface can not be null!");
            return;
        }

        mPreviewSurface = surface;
    }

    public void setSaveSurface(Surface surface) {
        if (surface == null) {
            Logger.e(TAG, "Saver can not be null!");
            return;
        }

        mSaveSurface = surface;
    }

    public void setSurfaceSize(int width, int height) {
        mOesWidth = width;
        mOesHeight = height;
    }

    public void setPreviewFilter(GPUImageFilter filter) {
        mPreviewFilter = filter;
    }

    public void setSaveFilter(GPUImageFilter filter) {
        mSaveFilter = filter;
    }

    /**
     * All setting is done, start it!
     */
    public void start() {
        mPreviewDrawSurface = new WindowSurface(mEglCore, mPreviewSurface, false);
        mSaveDrawSurface = new WindowSurface(mEglCore, mSaveSurface, false);
        mPreviewDrawSurface.makeCurrent();
        mOutputSurfaceDrawer = new VideoFrameDrawer(mOesWidth, mOesHeight);
        if (mPreviewFilter != null) {
            mPreviewFilter.init();
            GLES20.glUseProgram(mPreviewFilter.getProgram());
            mPreviewFilter.onOutputSizeChanged(mOesWidth, mOesHeight);
        }
        if (mSaveFilter != null) {
            mSaveFilter.init();
            GLES20.glUseProgram(mSaveFilter.getProgram());
            mSaveFilter.onOutputSizeChanged(mOesWidth, mOesHeight);
        }
        createOesTexture();
    }

    public int getOesTextureId() {
        return mOesTextureId;
    }

    public void stop() {
        mOutputSurfaceDrawer.stop();
        mEglCore.release();
        mVideoInputSurface.release();
        mVideoInputSurface = null;
    }

    private void createOesTexture() {
        Logger.d(TAG, "createOesTexture, " + mOesWidth + ", " + mOesHeight);
        int oesTextureId = OpenGlUtils.createOesTexture();
        mVideoInputSurface = new SurfaceTexture(oesTextureId);
        mVideoInputSurface.setOnFrameAvailableListener(this);
        mOesReader = new OesTextureReader(oesTextureId, mOesWidth, mOesHeight);
        mTextureWriter = new TextureWriter(mOesWidth, mOesHeight);
    }
}
