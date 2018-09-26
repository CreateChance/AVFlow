package com.createchance.avflowengine.config;

public class CameraInputConfig extends AbstractInputConfig {

    public static final int FACING_FRONT = 0;
    public static final int FACING_BACK = 1;

    public static final int ROTATION_0 = 0;
    public static final int ROTATION_90 = 90;
    public static final int ROTATION_180 = 180;
    public static final int ROTATION_270 = 270;

    private boolean mForceCameraV1;

    private int mFacing;

    private int mRotation;

    private CameraInputConfig() {
        super(TYPE_CAMERA);
    }

    public boolean isForceCameraV1() {
        return mForceCameraV1;
    }

    public int getFacing() {
        return mFacing;
    }

    public int getRotation() {
        return mRotation;
    }

    public static class Builder {
        private CameraInputConfig config = new CameraInputConfig();

        public Builder surfaceSize(int surfaceWidth, int surfaceHeight) {
            config.mSurfaceWidth = surfaceWidth;
            config.mSurfaceHeight = surfaceHeight;

            return this;
        }

        public Builder forceCameraV1(boolean force) {
            config.mForceCameraV1 = force;

            return this;
        }

        public Builder facing(int facing) {
            config.mFacing = facing;

            return this;
        }

        public Builder rotation(int rotation) {
            config.mRotation = rotation;

            return this;
        }

        public CameraInputConfig build() {
            return config;
        }
    }
}
