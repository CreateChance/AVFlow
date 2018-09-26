package com.createchance.avflowengine.generator;

import android.graphics.SurfaceTexture;
import android.view.Surface;

import java.io.File;
import java.util.List;

/**
 * ${DESC}
 *
 * @author createchance
 * @date 2018/8/27
 */
public class LocalStreamGenerator {

    private static final String TAG = "LocalStreamGenerator";

    private List<File> mInputFileList;

    private Surface mOutputSurface;

    private final VideoPlayer mVideoPlayer;

    public LocalStreamGenerator() {
        mVideoPlayer = new VideoPlayer();
    }

    public void setOutputTexture(SurfaceTexture surfaceTexture) {
        mOutputSurface = new Surface(surfaceTexture);
        mVideoPlayer.setOutputSurface(mOutputSurface);
    }

    public void setInputFile(List<File> inputFiles) {
        mInputFileList = inputFiles;
        mVideoPlayer.setInputFileList(mInputFileList);
    }

    public void setLoop(boolean loop) {
        mVideoPlayer.setLoop(loop);
    }

    public void setSpeed(float speedRate) {
        mVideoPlayer.setSpeedRate(speedRate);
    }

    public void start(FilePlayListener listener) {
        mVideoPlayer.start(listener);
    }

    public void stop() {
        mVideoPlayer.stop();
    }
}
