package com.createchance.avflowengine.processor;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.view.Surface;

import com.createchance.avflowengine.base.Logger;
import com.createchance.avflowengine.processor.gles.EglCore;
import com.createchance.avflowengine.processor.gles.WindowSurface;
import com.createchance.avflowengine.processor.gpuimage.GPUImageFilter;
import com.createchance.avflowengine.processor.gpuimage.OpenGlUtils;

import java.nio.FloatBuffer;

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
    private TextureWriter mPreviewTextureWriter, mSaveTextureWriter;

    private int mOesWidth, mOesHeight;

    public CodecStreamProcessor() {
        // init egl
        mEglCore = new EglCore(null, EglCore.FLAG_RECORDABLE);
        mOutputSurfaceDrawer = new VideoFrameDrawer();
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        Logger.v(TAG, "onFrameAvailable");
        if (mVideoInputSurface != null) {
            mVideoInputSurface.updateTexImage();
            mOutputSurfaceDrawer.draw(mOesReader, mPreviewDrawSurface, mPreviewFilter, mPreviewTextureWriter);
            if (mSaveSurface != null) {
                mOutputSurfaceDrawer.draw(mOesReader, mSaveDrawSurface, mPreviewFilter, mSaveTextureWriter);
            }
        }
    }

    public void setPreviewSurface(Surface surface) {
        if (surface == null) {
            Logger.e(TAG, "Preview surface can not be null!");
            return;
        }

        mPreviewSurface = surface;
        mPreviewDrawSurface = new WindowSurface(mEglCore, mPreviewSurface, false);
        mPreviewDrawSurface.makeCurrent();
        mOutputSurfaceDrawer.createFrameBuffer();
    }

    public void setSaveSurface(Surface surface, int clipTop, int clipLeft, int clipBottom, int clipRight) {
        if (surface == null) {
            Logger.e(TAG, "Saver surface can not be null!");
            return;
        }

        Logger.d(TAG, "Save start!!!!!!!! clip top: " + clipTop + ", clip left: " + clipLeft + ", clip bottom: " + clipBottom + ", clip right: " + clipRight);
        mSaveTextureWriter = new TextureWriter(
                getVertexBuffer(clipTop, clipLeft, clipBottom, clipRight),
                getTextureBuffer(clipTop, clipLeft, clipBottom, clipRight));
        mSaveSurface = surface;
        mSaveDrawSurface = new WindowSurface(mEglCore, mSaveSurface, false);
        mSaveDrawSurface.createTexture(
                0,
                -700,
                mOesWidth,
                mOesHeight);
    }

    public void clearSaveSurface() {
        mSaveSurface = null;
    }

    public void setSurfaceSize(int width, int height) {
        mOesWidth = width;
        mOesHeight = height;
        createOesTexture();
        mPreviewDrawSurface.createTexture(
                0,
                0,
                mOesWidth,
                mOesHeight);
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
        mPreviewDrawSurface.makeCurrent();
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
    }

    public SurfaceTexture getOesTextureId() {
        return mVideoInputSurface;
    }

    public void stop() {
        mOutputSurfaceDrawer.stop();
        mEglCore.release();
        mVideoInputSurface.release();
        mPreviewDrawSurface.release();
        if (mSaveDrawSurface != null) {
            mSaveDrawSurface.release();
        }
        mVideoInputSurface = null;
    }

    private void createOesTexture() {
        mOesTextureId = OpenGlUtils.createOesTexture();
        mVideoInputSurface = new SurfaceTexture(mOesTextureId);
        mVideoInputSurface.setOnFrameAvailableListener(this);
        mOesReader = new OesTextureReader(mOesTextureId);
        mPreviewTextureWriter = new TextureWriter(null, null);
    }

    private FloatBuffer getVertexBuffer(int clipTop, int clipLeft, int clipBottom, int clipRight) {
        float top = (clipTop - mOesHeight / 2) * 1.0f / (mOesHeight / 2);
        float left = (clipLeft - mOesWidth / 2) * 1.0f / (mOesWidth / 2);
        float bottom = (clipBottom - mOesHeight / 2) * 1.0f / (mOesHeight / 2);
        float right = (clipRight - mOesWidth / 2) * 1.0f / (mOesWidth / 2);

        Logger.d(TAG, "getVertexBuffer, clipTop: " + clipTop
                + ", clipLeft: " + clipLeft
                + ", clipBottom: " + clipBottom
                + ", clipRight: " + clipRight);
        Logger.d(TAG, "getVertexBuffer, top: " + top
                + ", left: " + left
                + ", bottom: " + bottom
                + ", right: " + right);

        return OpenGlUtils.createFloatBuffer(
                new float[]{
                        left, bottom,
                        left, top,
                        right, bottom,
                        right, top,
                }
        );
    }

    private FloatBuffer getTextureBuffer(int clipTop, int clipLeft, int clipBottom, int clipRight) {
        float top = (mOesHeight - clipTop) * 1.0f / mOesHeight;
        float left = clipLeft * 1.0f / mOesWidth;
        float bottom = (mOesHeight - clipBottom) * 1.0f / mOesHeight;
        float right = clipRight * 1.0f / mOesWidth;

        Logger.d(TAG, "getTextureBuffer, clipTop: " + clipTop
                + ", clipLeft: " + clipLeft
                + ", clipBottom: " + clipBottom
                + ", clipRight: " + clipRight);
        Logger.d(TAG, "getTextureBuffer, top: " + top
                + ", left: " + left
                + ", bottom: " + bottom
                + ", right: " + right);

        return OpenGlUtils.createFloatBuffer(
                new float[]{
                        left, bottom,
                        left, top,
                        right, bottom,
                        right, top,
                });
    }
}
