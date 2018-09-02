package com.createchance.mediastreamprocessor;

import android.opengl.GLES20;

import com.createchance.mediastreamprocessor.gpuimage.GPUImageContrastFilter;
import com.createchance.mediastreamprocessor.gpuimage.GPUImageDirectionalSobelEdgeDetectionFilter;
import com.createchance.mediastreamprocessor.gpuimage.GPUImageFilter;
import com.createchance.mediastreamprocessor.gpuimage.GPUImageFilterGroup;
import com.createchance.mediastreamprocessor.gpuimage.GPUImageGrayscaleFilter;
import com.createchance.mediastreamprocessor.gpuimage.Rotation;
import com.createchance.mediastreamprocessor.gpuimage.util.TextureRotationUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.List;

import static com.createchance.mediastreamprocessor.gpuimage.util.TextureRotationUtil.TEXTURE_ROTATED_90;

/**
 * ${DESC}
 *
 * @author createchance
 * @date 2018/9/1
 */
class VideoFrameDrawer {
    private int mFrameNum;

    private OesTextureReader mOesTextureReader;

    private int mSurfaceWidth, mSurfaceHeight;

    private int[] mFrameBuffer = new int[1];
    private int[] mTextureIds = new int[2];

    private GPUImageFilterGroup mGPUImageFilter;

    private ShowFilter mShowFilter;

    private final float CUBE[] = {
            -1.0f, 1.0f,
            -1.0f, -1.0f,
            1.0f, 1.0f,
            1.0f, -1.0f,
    };
    private final FloatBuffer mGLCubeBuffer;
    private final FloatBuffer mGLTextureBuffer;

    public VideoFrameDrawer(int oesTextureId, int surfaceWidth, int surfaceHeight) {
        mSurfaceWidth = surfaceWidth;
        mSurfaceHeight = surfaceHeight;
        mOesTextureReader = new OesTextureReader(oesTextureId, surfaceWidth, surfaceHeight);
        createFrameBuffer();

        List<GPUImageFilter> filters = new LinkedList<GPUImageFilter>();
        filters.add(new GPUImageContrastFilter());
        filters.add(new GPUImageDirectionalSobelEdgeDetectionFilter());
        filters.add(new GPUImageGrayscaleFilter());
        mGPUImageFilter = new GPUImageFilterGroup(filters);
        mGPUImageFilter.init();
        GLES20.glUseProgram(mGPUImageFilter.getProgram());
        mGPUImageFilter.onOutputSizeChanged(mSurfaceWidth, mSurfaceHeight);

        mGLCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLCubeBuffer.put(CUBE).position(0);

        mGLTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_ROTATED_90.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        float[] textureCords = TextureRotationUtil.getRotation(Rotation.ROTATION_270, false, true);
        mGLTextureBuffer.put(textureCords).position(0);

        mShowFilter = new ShowFilter(mTextureIds[1], surfaceWidth, surfaceHeight);
    }

    public void draw() {
        bindFrameBuffer(0);
        GLES20.glViewport(0, 0, mSurfaceWidth, mSurfaceHeight);
        mOesTextureReader.read();
        unbindFrameBuffer();

        bindFrameBuffer(1);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        mGPUImageFilter.onDraw(mTextureIds[0], mGLCubeBuffer, mGLTextureBuffer);
        drawExtra(mFrameNum, mSurfaceWidth, mSurfaceHeight);
        mFrameNum++;
        unbindFrameBuffer();
        mShowFilter.draw();
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
