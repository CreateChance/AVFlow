package com.createchance.avflowengine.processor;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.view.Surface;

import com.createchance.avflowengine.base.Logger;
import com.createchance.avflowengine.processor.gles.EglCore;
import com.createchance.avflowengine.processor.gles.WindowSurface;
import com.createchance.avflowengine.processor.gpuimage.GPUImageFilter;
import com.createchance.avflowengine.processor.gpuimage.GPUImageFilterGroup;
import com.createchance.avflowengine.processor.gpuimage.OpenGlUtils;

import java.nio.FloatBuffer;
import java.util.List;

/**
 * ${DESC}
 *
 * @author createchance
 * @date 2018/8/27
 */
public final class AVStreamProcessor implements SurfaceTexture.OnFrameAvailableListener {

    private static final String TAG = "AVStreamProcessor";

    private SurfaceTexture mVideoInputSurface;

    private EglCore mEglCore;

    private VideoFrameHandler mOutputSurfaceDrawer;
    private Surface mPreviewSurface, mSaveSurface;
    private WindowSurface mPreviewDrawSurface, mSaveDrawSurface;
    private GPUImageFilter mPreviewFilter, mSaveFilter;
    private TextDrawer mPreviewTextDrawer, mSaveTextDrawer;
    private ImageDrawer mPreviewImageDrawer, mSaveImageDrawer;

    private int mOesTextureId = -1;
    private OesTextureReader mOesReader;
    private OutputDrawer mPreviewTextureDrawer, mSaveTextureDrawer;

    private int mOesWidth, mOesHeight;

    public AVStreamProcessor() {
        // init egl
        mEglCore = new EglCore(null, EglCore.FLAG_RECORDABLE);
        mOutputSurfaceDrawer = new VideoFrameHandler();
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        Logger.v(TAG, "onFrameAvailable, thread name: " + Thread.currentThread().getName());
        if (mVideoInputSurface != null) {
            if (mPreviewSurface != null) {
                mOutputSurfaceDrawer.draw(mOesReader,
                        mPreviewDrawSurface,
                        mPreviewFilter,
                        mPreviewTextureDrawer,
                        mPreviewTextDrawer,
                        mPreviewImageDrawer);
            }
            if (mSaveSurface != null) {
                mOutputSurfaceDrawer.draw(mOesReader,
                        mSaveDrawSurface,
                        mSaveFilter,
                        mSaveTextureDrawer,
                        mSaveTextDrawer,
                        mSaveImageDrawer);
            }
            mVideoInputSurface.updateTexImage();
        }
    }

    public void setOesSurfaceSize(int surfaceWidth, int surfaceHeight) {
        mOesWidth = surfaceWidth;
        mOesHeight = surfaceHeight;
    }

    public void setPreviewSurface(Surface surface) {
        if (surface == null) {
            Logger.e(TAG, "Preview surface can not be null!");
            return;
        }

        mPreviewSurface = surface;
        mPreviewDrawSurface = new WindowSurface(mEglCore, mPreviewSurface, false);
        mPreviewDrawSurface.makeCurrent();
        mPreviewDrawSurface.createTexture(
                0,
                0,
                mOesWidth,
                mOesHeight,
                mOesWidth,
                mOesHeight);
        mOutputSurfaceDrawer.createFrameBuffer();
    }

    public void setSaveSurface(Surface surface, int clipTop, int clipLeft, int clipBottom, int clipRight) {
        if (surface == null) {
            Logger.e(TAG, "Saver surface can not be null!");
            return;
        }

        Logger.d(TAG, "setSaveSurface, clip top: " + clipTop
                + ", clip left: " + clipLeft
                + ", clip bottom: " + clipBottom
                + ", clip right: " + clipRight);
        mSaveSurface = surface;
        mSaveDrawSurface = new WindowSurface(mEglCore, mSaveSurface, false);
        mSaveDrawSurface.makeCurrent();
        mSaveDrawSurface.createTexture(
                clipLeft,
                0,
                clipRight - clipLeft,
                clipBottom - clipTop,
                mOesWidth,
                mOesHeight);
        mSaveTextureDrawer = new OutputDrawer(
                getVertexBuffer(),
                getTextureBuffer(clipTop, clipLeft, clipBottom, clipRight));
        mOutputSurfaceDrawer.createFrameBuffer();
    }

    public void clearSaveSurface() {
        mSaveSurface = null;
    }

    public void setPreviewFilter(GPUImageFilter filter) {
        if (mPreviewFilter != null) {
            mPreviewFilter.destroy();
        }
        if (filter instanceof GPUImageFilterGroup) {
            ((GPUImageFilterGroup) filter).setOutput(
                    mOutputSurfaceDrawer.getOffScreenFrameBuffer(),
                    mPreviewDrawSurface.getOutputTextureIds()[1]);
        }
        mPreviewFilter = filter;
    }

    public void setSaveFilter(GPUImageFilter filter) {
        if (mSaveFilter != null) {
            mSaveFilter.destroy();
        }
        if (filter instanceof GPUImageFilterGroup) {
            ((GPUImageFilterGroup) filter).setOutput(
                    mOutputSurfaceDrawer.getOffScreenFrameBuffer(),
                    mSaveDrawSurface.getOutputTextureIds()[1]);
        }
        mSaveFilter = filter;
    }

    public void setPreviewText(String fontPath,
                               String text,
                               int posX,
                               int posY,
                               int textSize,
                               float red,
                               float green,
                               float blue,
                               Bitmap background) {
        if (mPreviewTextDrawer != null) {
            mPreviewTextDrawer.release();
        }
        mPreviewTextDrawer = new TextDrawer();
        mPreviewTextDrawer.setText(fontPath, text, posX, posY, textSize, red, green, blue, background);
    }

    public void setSaveText(String fontPath,
                            String text,
                            int posX,
                            int posY,
                            int textSize,
                            float red,
                            float green,
                            float blue,
                            Bitmap background) {
        if (mSaveTextDrawer != null) {
            mSaveTextDrawer.release();
        }
        mSaveTextDrawer = new TextDrawer();
        mSaveTextDrawer.setText(fontPath, text, posX, posY, textSize, red, green, blue, background);
    }

    public void setPreviewImage(List<String> imageList, int posX, int posY, float scaleFactor) {
        if (mPreviewImageDrawer != null) {
            mPreviewImageDrawer.release();
        }
        mPreviewImageDrawer = new ImageDrawer();
        mPreviewImageDrawer.setImage(imageList, posX, posY, scaleFactor);
    }

    public void setSaveImage(List<String> imageList, int posX, int posY, float scaleFactor) {
        if (mSaveImageDrawer != null) {
            mSaveImageDrawer.release();
        }
        mSaveImageDrawer = new ImageDrawer();
        mSaveImageDrawer.setImage(imageList, posX, posY, scaleFactor);
    }

    public void updatePreviewTextParams(int posX,
                                        int posY,
                                        float red,
                                        float green,
                                        float blue) {
        if (mPreviewTextDrawer != null) {
            mPreviewTextDrawer.setParams(posX, posY, red, green, blue);
        }
    }

    public void updateSaveTextParams(int posX,
                                     int posY,
                                     float red,
                                     float green,
                                     float blue) {
        if (mSaveTextDrawer != null) {
            mSaveTextDrawer.setParams(posX, posY, red, green, blue);
        }
    }

    public void removePreviewText() {
        if (mPreviewTextDrawer != null) {
            mPreviewTextDrawer.release();
            mPreviewTextDrawer = null;
        }
    }

    public void removeSaveText() {
        if (mSaveTextDrawer != null) {
            mSaveTextDrawer.release();
            mSaveTextDrawer = null;
        }
    }

    public void removePreviewImage() {
        if (mPreviewImageDrawer != null) {
            mPreviewImageDrawer.release();
            mPreviewImageDrawer = null;
        }
    }

    public void removeSaveImage() {
        if (mSaveImageDrawer != null) {
            mSaveImageDrawer.release();
            mSaveImageDrawer = null;
        }
    }

    public void prepare(int rotation) {
        mOesTextureId = OpenGlUtils.createOesTexture();
        mVideoInputSurface = new SurfaceTexture(mOesTextureId);
        mOesReader = new OesTextureReader(mOesTextureId, mOesWidth, mOesHeight, rotation);
        mPreviewTextureDrawer = new OutputDrawer(null, null);
        mVideoInputSurface.setOnFrameAvailableListener(this);
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

    private FloatBuffer getVertexBuffer() {
        return OpenGlUtils.createFloatBuffer(
                new float[]{
                        -1, 1f,
                        -1, -1f,
                        1f, 1f,
                        1f, -1f,
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
