package com.createchance.avflowengine.generator;

import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.IOException;

/**
 * ${DESC}
 *
 * @author createchance
 * @date 2018/8/27
 */
public class LocalStreamGenerator {

    private static final String TAG = "LocalStreamGenerator";

    private File mInputFile;

    private Surface mOutputSurface;

    private VideoPlayer mPlayer;

    public void setOutputTexture(int texture) {
        mOutputSurface = new Surface(new SurfaceTexture(texture));
    }

    public void setInputFile(File inputFile) {
        mInputFile = inputFile;
    }

    public void start() {
        try {
            mPlayer = new VideoPlayer(mInputFile, mOutputSurface, new VideoPlayer.FrameCallback() {
                @Override
                public void preRender(long presentationTimeUsec) {
                    Log.d(TAG, "preRender: " + presentationTimeUsec);
                }

                @Override
                public void postRender() {
                    Log.d(TAG, "postRender: ");
                }

                @Override
                public void loopReset() {
                    Log.d(TAG, "loopReset: ");
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        // alloc buffer
        new VideoPlayer.PlayTask(mPlayer, new VideoPlayer.PlayerFeedback() {
            @Override
            public void playbackStopped() {
                Log.d(TAG, "playbackStopped: ");
            }
        }).execute();
    }

    public void stop() {
        Log.d(TAG, "shutdown: ");
        if (mPlayer != null) {
            mPlayer.requestStop();
        }
    }
}
