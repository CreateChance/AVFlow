package com.createchance.localstreamgenerator;

import android.util.Log;

import com.createchance.mediastreambase.IVideoInputSurfaceListener;
import com.createchance.mediastreambase.IVideoStreamConsumer;
import com.createchance.mediastreambase.IVideoStreamGenerator;
import com.createchance.mediastreambase.VideoInputSurface;

import java.io.File;
import java.io.IOException;

/**
 * ${DESC}
 *
 * @author createchance
 * @date 2018/8/27
 */
public final class LocalStreamGenerator implements IVideoStreamGenerator, IVideoInputSurfaceListener {

    private static final String TAG = "LocalStreamGenerator";

    private File mSourceFile;

    private IVideoStreamConsumer mConsumer;

    private VideoPlayer mPlayer;

    private LocalStreamGenerator() {

    }

    @Override
    public void start() {
        // alloc buffer
        new VideoPlayer.PlayTask(mPlayer, new VideoPlayer.PlayerFeedback() {
            @Override
            public void playbackStopped() {
                Log.d(TAG, "playbackStopped: ");
            }
        }).execute();
    }

    @Override
    public void stop() {
        Log.d(TAG, "shutdown: ");
    }

    @Override
    public void setConsumer(IVideoStreamConsumer consumer) {
        this.mConsumer = consumer;
        mConsumer.setInputSurfaceListener(this);
    }

    @Override
    public void onConsumerSurfaceCreated(IVideoStreamConsumer consumer,
                                         VideoInputSurface inputSurface) {
        try {
            mPlayer = new VideoPlayer(mSourceFile, inputSurface.mSurface, new VideoPlayer.FrameCallback() {
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
    }

    @Override
    public void onConsumerSurfaceChanged(IVideoStreamConsumer consumer,
                                         VideoInputSurface inputSurface,
                                         int width,
                                         int height) {

    }

    @Override
    public void onConsumerSurfaceDestroyed(IVideoStreamConsumer consumer,
                                           VideoInputSurface inputSurface) {

    }

    public static class Builder {
        private LocalStreamGenerator generator = new LocalStreamGenerator();

        public Builder sourceFile(File source) {
            generator.mSourceFile = source;

            return this;
        }

        public LocalStreamGenerator build() {
            return generator;
        }
    }
}
