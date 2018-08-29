package com.createchance.mediastreamprocessor;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import com.createchance.mediastreambase.AVFrame;
import com.createchance.mediastreambase.AbstractStreamProcessor;

import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.glTexParameterf;

/**
 * ${DESC}
 *
 * @author gaochao1-iri
 * @date 2018/8/27
 */
public final class CodecStreamProcessor extends AbstractStreamProcessor {

    private static final String TAG = "CodecStreamProcessor";

    @Override
    protected boolean init() {
        Log.d(TAG, "init: ");
        mSurfaceTexture = new SurfaceTexture(createOesTexture());

        return true;
    }

    @Override
    protected void shutdown() {
        Log.d(TAG, "shutdown: ");
    }

    private int createOesTexture() {
        int texture[] = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        if (texture[0] == 0) {
            return 0;
        }
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        return texture[0];
    }

    @Override
    protected void onAudioFrame(AVFrame audioFrame) {

    }

    @Override
    protected void onVideoFrame(AVFrame videoFrame) {

    }
}
