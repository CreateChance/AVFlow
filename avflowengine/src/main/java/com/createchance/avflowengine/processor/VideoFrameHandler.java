package com.createchance.avflowengine.processor;

import android.opengl.GLES20;

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
class VideoFrameHandler {

    private static final String TAG = "VideoFrameHandler";

    private int[] mFrameBuffer = new int[1];

    private boolean mFrameBufferInitDone;

    private final float CUBE[] = {
            -1.0f, 1.0f,
            -1.0f, -1.0f,
            1.0f, 1.0f,
            1.0f, -1.0f,
    };
    private FloatBuffer mGLCubeBuffer;
    private FloatBuffer mGLTextureBuffer;

    VideoFrameHandler() {
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
        deleteOffScreenFrameBuffer();
    }

    void draw(OesTextureReader reader,
              WindowSurface drawSurface,
              GPUImageFilter filter,
              OutputDrawer outputDrawer,
              TextDrawer textDrawer,
              ImageDrawer imageDrawer) {
        drawSurface.makeCurrent();
        bindOffScreenFrameBuffer(drawSurface.getOutputTextureIds()[0]);
        reader.read();
        bindDefaultFrameBuffer();

        if (filter != null) {
            bindOffScreenFrameBuffer(drawSurface.getOutputTextureIds()[1]);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            filter.onDraw(drawSurface.getOutputTextureIds()[0], mGLCubeBuffer, mGLTextureBuffer);
            bindDefaultFrameBuffer();
        }
        if (filter == null) {
            outputDrawer.write(drawSurface.getOutputTextureIds()[0],
                    drawSurface.getX(),
                    drawSurface.getY(),
                    drawSurface.getTextureWidth(),
                    drawSurface.getTextureHeight());
        } else {
            outputDrawer.write(drawSurface.getOutputTextureIds()[1],
                    drawSurface.getX(),
                    drawSurface.getY(),
                    drawSurface.getTextureWidth(),
                    drawSurface.getTextureHeight());
        }
        if (textDrawer != null) {
            textDrawer.draw();
        }
        if (imageDrawer != null) {
            imageDrawer.draw();
        }
        drawSurface.swapBuffers();
    }

    synchronized void createFrameBuffer() {
        if (!mFrameBufferInitDone) {
            mFrameBufferInitDone = true;
            GLES20.glGenFramebuffers(mFrameBuffer.length, mFrameBuffer, 0);
        }
    }

    int getOffScreenFrameBuffer() {
        return mFrameBuffer[0];
    }

    private void bindOffScreenFrameBuffer(int textureId) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffer[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, textureId, 0);
    }

    private void bindDefaultFrameBuffer() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    private void deleteOffScreenFrameBuffer() {
        GLES20.glDeleteFramebuffers(mFrameBuffer.length, mFrameBuffer, 0);
    }
}
