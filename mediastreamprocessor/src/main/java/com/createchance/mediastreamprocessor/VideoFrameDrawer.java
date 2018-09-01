package com.createchance.mediastreamprocessor;

import android.opengl.GLES20;

/**
 * ${DESC}
 *
 * @author createchance
 * @date 2018/9/1
 */
class VideoFrameDrawer {
    private int mFrameNum;

    private OesFilter mOesFilter;

    private int mSurfaceWidth, mSurfaceHeight;

    public VideoFrameDrawer(int oesTextureId, int surfaceWidth, int surfaceHeight) {
        mSurfaceWidth = surfaceWidth;
        mSurfaceHeight = surfaceHeight;
        mOesFilter = new OesFilter(oesTextureId, surfaceWidth, surfaceHeight);
    }

    public void draw() {
        GLES20.glViewport(0, 0, mSurfaceWidth, mSurfaceHeight);
        mOesFilter.draw();
        drawExtra(mFrameNum, mSurfaceWidth, mSurfaceHeight);
        mFrameNum++;
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
}
