package com.createchance.mediastreamprocessor;

import android.opengl.GLES20;
import android.util.Log;

import com.createchance.mediastreambase.IVideoInputSurfaceListener;
import com.createchance.mediastreambase.IVideoStreamConsumer;
import com.createchance.mediastreambase.Logger;
import com.createchance.mediastreambase.VideoInputSurface;
import com.createchance.mediastreamprocessor.gles.EglCore;
import com.createchance.mediastreamprocessor.gles.WindowSurface;
import com.createchance.mediastreamprocessor.gpuimage.GPUImageFilter;
import com.createchance.mediastreamprocessor.gpuimage.Rotation;
import com.createchance.mediastreamprocessor.gpuimage.util.TextureRotationUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * ${DESC}
 *
 * @author createchance
 * @date 2018/9/1
 */
class VideoFrameDrawer implements IVideoInputSurfaceListener {

    private static final String TAG = "VideoFrameDrawer";

    private int mFrameNum;

    private int mOesTextureId;
    private OesTextureReader mOesTextureReader;

    private int mSurfaceWidth, mSurfaceHeight;

    private int[] mFrameBuffer = new int[1];
    private int[] mTextureIds = new int[2];

    private GPUImageFilter mGPUImageFilter;

    private ShowFilter mShowFilter;

    private final float CUBE[] = {
            -1.0f, 1.0f,
            -1.0f, -1.0f,
            1.0f, 1.0f,
            1.0f, -1.0f,
    };
    private FloatBuffer mGLCubeBuffer;
    private FloatBuffer mGLTextureBuffer;

    private boolean mStop;

    private DrawerListener mListener;

    private EglCore mEglCore;

    private IVideoStreamConsumer mConsumer;

    private WindowSurface mDrawSurface;

    private boolean mInitDone;

    private static final Object LOCK = new Object();
    private static boolean mHasCurrentSurface;

    public VideoFrameDrawer(EglCore eglCore,
                            IVideoStreamConsumer consumer,
                            DrawerListener listener) {
        mEglCore = eglCore;
        mConsumer = consumer;
        mListener = listener;

        mConsumer.setInputSurfaceListener(this);
    }

    void release() {
        mStop = true;

        deleteFrameBuffer();

        if (mDrawSurface != null) {
            mDrawSurface.release();
        }
    }

    void stop() {
        mStop = true;

        deleteFrameBuffer();

        if (mDrawSurface != null) {
            mDrawSurface.release();
        }

        if (mListener != null) {
            mListener.onDestroyed(this);
        }
    }

    IVideoStreamConsumer getConsumer() {
        return mConsumer;
    }

    void setGpuImageFilter(GPUImageFilter gpuImageFilter) {
        mGPUImageFilter = gpuImageFilter;
    }

    void setOesTextureId(int textureId, int width, int height) {
        mOesTextureId = textureId;
        mOesTextureReader = new OesTextureReader(mOesTextureId, width, height);
    }

    void draw() {
        if (mStop) {
            Logger.w(TAG, "Can not draw, because we are stopped.");
            return;
        }

        if (!mInitDone) {
            Logger.e(TAG, "Not init now, can not draw!");
            return;
        }

        if (mOesTextureReader == null) {
            Logger.e(TAG, "No oes texture, can not draw!");
            return;
        }

        Log.d(TAG, "draw: " + mFrameBuffer[0] + ", texture0: " + mTextureIds[0]);
        mDrawSurface.makeCurrent();
        bindFrameBuffer(0);
        GLES20.glViewport(0, 0, mSurfaceWidth, mSurfaceHeight);
        mOesTextureReader.read();
        unbindFrameBuffer();

        if (mGPUImageFilter != null) {
            bindFrameBuffer(1);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            mGPUImageFilter.onDraw(mTextureIds[0], mGLCubeBuffer, mGLTextureBuffer);
            drawExtra(mFrameNum, mSurfaceWidth, mSurfaceHeight);
            mFrameNum++;
            unbindFrameBuffer();
        }
        if (mGPUImageFilter == null) {
            mShowFilter.draw(mTextureIds[0]);
        } else {
            mShowFilter.draw(mTextureIds[1]);
        }
        mDrawSurface.swapBuffers();
    }

    private void init(VideoInputSurface inputSurface) {
        mDrawSurface = new WindowSurface(mEglCore, inputSurface.mSurface, false);
        synchronized (LOCK) {
            if (!mHasCurrentSurface) {
                mHasCurrentSurface = true;
                mDrawSurface.makeCurrent();
            }
        }
    }

    private void setSurfaceSize(int width, int height) {
        mSurfaceWidth = width;
        mSurfaceHeight = height;

        deleteFrameBuffer();
        createFrameBuffer();

        if (mGPUImageFilter != null) {
            mGPUImageFilter.init();
            GLES20.glUseProgram(mGPUImageFilter.getProgram());
            mGPUImageFilter.onOutputSizeChanged(mSurfaceWidth, mSurfaceHeight);
        }

        mGLCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLCubeBuffer.put(CUBE).position(0);

        mGLTextureBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        float[] textureCords = TextureRotationUtil.getRotation(Rotation.ROTATION_270, false, true);
        mGLTextureBuffer.put(textureCords).position(0);

        mShowFilter = new ShowFilter(mSurfaceWidth, mSurfaceHeight);

        mInitDone = true;
        if (mListener != null) {
            mListener.onSurfaceConfigDone(this);
        }
    }

    private void drawExtra(int frameNum, int width, int height) {
        // We "draw" with the scissor rect and clear calls.  Note this uses window coordinates.
        int val = frameNum % 3;
        switch (val) {
            case 0:
                GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
                break;
            case 1:
                GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
                break;
            case 2:
                GLES20.glClearColor(0.0f, 0.0f, 1.0f, 1.0f);
                break;
            default:
                break;
        }

        int xpos = (int) (width * ((frameNum % 100) / 100.0f));
        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
        GLES20.glScissor(xpos, 0, width / 32, height / 32);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
    }

    private void createFrameBuffer() {
        GLES20.glGenFramebuffers(mFrameBuffer.length, mFrameBuffer, 0);
        GLES20.glGenTextures(mTextureIds.length, mTextureIds, 0);
        Log.d(TAG, "createFrameBuffer: " + mFrameBuffer[0]);
        for (int i = 0; i < mTextureIds.length; i++) {
            // bind to fbo texture cause we are going to do setting.
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureIds[i]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mSurfaceWidth, mSurfaceHeight,
                    0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
            // 设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            // 设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            // 设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            // 设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            // unbind fbo texture.
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        }
    }

    private void bindFrameBuffer(int index) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffer[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, mTextureIds[index], 0);
    }

    private void unbindFrameBuffer() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    private void deleteFrameBuffer() {
        GLES20.glDeleteFramebuffers(mFrameBuffer.length, mFrameBuffer, 0);
        GLES20.glDeleteTextures(mTextureIds.length, mTextureIds, 0);
    }

    @Override
    public void onConsumerSurfaceCreated(IVideoStreamConsumer consumer,
                                         VideoInputSurface inputSurface) {
        init(inputSurface);
    }

    @Override
    public void onConsumerSurfaceChanged(IVideoStreamConsumer consumer,
                                         VideoInputSurface inputSurface,
                                         int width,
                                         int height) {
        setSurfaceSize(width, height);
    }

    @Override
    public void onConsumerSurfaceDestroyed(IVideoStreamConsumer consumer,
                                           VideoInputSurface inputSurface) {
        stop();
    }

    interface DrawerListener {
        void onSurfaceConfigDone(VideoFrameDrawer drawer);

        void onDestroyed(VideoFrameDrawer drawer);
    }
}
