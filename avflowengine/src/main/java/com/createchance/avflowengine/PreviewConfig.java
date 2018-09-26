package com.createchance.avflowengine;

import android.view.Surface;

import com.createchance.avflowengine.generator.VideoPlayListener;

import java.io.File;
import java.util.List;

/**
 * Preview config.
 *
 * @author createchance
 * @date 2018-09-26
 */
public class PreviewConfig {
    public static final int ROTATION_0 = 0;
    public static final int ROTATION_90 = 90;
    public static final int ROTATION_180 = 180;
    public static final int ROTATION_270 = 270;

    static final int SOURCE_CAMERA = 1;
    static final int SOURCE_FILE = 2;

    Surface mSurface;
    DataSource mDataSource;
    int mSurfaceWidth, mSurfaceHeight;

    private PreviewConfig() {

    }

    public static class Builder {
        private PreviewConfig mConfig = new PreviewConfig();

        public Builder surface(Surface surface, int width, int height) {
            mConfig.mSurface = surface;
            mConfig.mSurfaceWidth = width;
            mConfig.mSurfaceHeight = height;

            return this;
        }

        public Builder dataSource(DataSource source) {
            mConfig.mDataSource = source;

            return this;
        }

        public PreviewConfig build() {
            return mConfig;
        }
    }

    abstract static class DataSource {
        int mSource;

        DataSource(int source) {
            mSource = source;
        }
    }

    public static class CameraSource extends DataSource {

        public static final int CAMERA_FACING_FRONT = 1;
        public static final int CAMERA_FACING_BACK = 2;

        int mRotation;
        int mFacing;
        boolean mForceCameraV1;

        public CameraSource(int rotation, int facing, boolean forceV1) {
            super(SOURCE_CAMERA);
            mRotation = rotation;
            mFacing = facing;
            mForceCameraV1 = forceV1;
        }
    }

    public static class FileSource extends DataSource {

        int mRotation;
        List<File> mSourceFileList;
        boolean mLoop;
        float mSpeedRate;
        VideoPlayListener mListener;

        public FileSource(List<File> sourceFiles, int rotation,boolean loop, float speedRate, VideoPlayListener listener) {
            super(SOURCE_FILE);
            mSourceFileList = sourceFiles;
            mRotation = rotation;
            mLoop = loop;
            mSpeedRate = speedRate;
            mListener = listener;
        }
    }
}
