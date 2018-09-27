package com.createchance.avflowengine.config;

import com.createchance.avflowengine.generator.FilePlayListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileInputConfig extends AbstractInputConfig {

    public static final int ROTATION_0 = 0;
    public static final int ROTATION_90 = 90;
    public static final int ROTATION_180 = 180;
    public static final int ROTATION_270 = 270;

    public static final float SPEEDRATE_FASTEST = 0;

    private List<File> mInputFiles;
    private boolean mLoop;
    private float mSpeedRate;
    private FilePlayListener mListener;

    private int mRotation;

    private FileInputConfig() {
        super(TYPE_FILE);
        mInputFiles = new ArrayList<>();
    }

    public List<File> getInputFiles() {
        return mInputFiles;
    }

    public boolean isLoop() {
        return mLoop;
    }

    public float getSpeedRate() {
        return mSpeedRate;
    }

    public FilePlayListener getListener() {
        return mListener;
    }

    public int getRotation() {
        return mRotation;
    }

    public static class Builder {
        private FileInputConfig config = new FileInputConfig();

        public Builder surfaceSize(int surfaceWidth, int surfaceHeight) {
            config.mSurfaceWidth = surfaceWidth;
            config.mSurfaceHeight = surfaceHeight;

            return this;
        }

        public Builder addFile(File file) {
            config.mInputFiles.add(file);

            return this;
        }

        public Builder loop(boolean loop) {
            config.mLoop = loop;

            return this;
        }

        public Builder speedRate(float speedRate) {
            config.mSpeedRate = speedRate;

            return this;
        }

        public Builder rotation(int rotation) {
            config.mRotation = rotation;

            return this;
        }

        public Builder listener(FilePlayListener listener) {
            config.mListener = listener;

            return this;
        }

        public FileInputConfig build() {
            return config;
        }
    }
}
