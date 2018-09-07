package com.createchance.avflowengine.processor;

import android.opengl.GLES20;
import android.util.Log;

import com.createchance.avflowengine.processor.gles.WindowSurface;
import com.createchance.avflowengine.processor.gpuimage.GPUImageFilter;
import com.createchance.avflowengine.processor.gpuimage.Rotation;
import com.createchance.avflowengine.processor.gpuimage.util.TextureRotationUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * ${DESC}
 *
 * @author createchance
 * @date 2018/9/1
 */
class VideoFrameDrawer {

    private static final String TAG = "VideoFrameDrawer";

    private int mSurfaceWidth, mSurfaceHeight;

    private int[] mFrameBuffer = new int[1];
    private int[] mTextureIds = new int[2];

    private final float CUBE[] = {
            -1.0f, 1.0f,
            -1.0f, -1.0f,
            1.0f, 1.0f,
            1.0f, -1.0f,
    };
    private FloatBuffer mGLCubeBuffer;
    private FloatBuffer mGLTextureBuffer;


    VideoFrameDrawer(int width, int height) {
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        createFrameBuffer();

        mGLCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLCubeBuffer.put(CUBE).position(0);

        mGLTextureBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        float[] textureCords = TextureRotationUtil.getRotation(Rotation.ROTATION_270, false, true);
        mGLTextureBuffer.put(textureCords).position(0);
    }

    void stop() {
        deleteFrameBuffer();
    }

    void draw(OesTextureReader reader, WindowSurface drawSurface, GPUImageFilter filter, TextureWriter writer) {
        drawSurface.makeCurrent();
        bindFrameBuffer(0);
        GLES20.glViewport(0, 0, mSurfaceWidth, mSurfaceHeight);
        reader.read();
        unbindFrameBuffer();

        if (filter != null) {
            bindFrameBuffer(1);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            filter.onDraw(mTextureIds[0], mGLCubeBuffer, mGLTextureBuffer);
            unbindFrameBuffer();
        }
        if (filter == null) {
            writer.write(mTextureIds[0]);
        } else {
            writer.write(mTextureIds[1]);
        }
        drawSurface.swapBuffers();
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
}
