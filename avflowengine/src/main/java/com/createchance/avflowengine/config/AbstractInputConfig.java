package com.createchance.avflowengine.config;

public abstract class AbstractInputConfig {
    public static final int TYPE_CAMERA = 0;
    public static final int TYPE_FILE = 1;

    protected final int mType;

    protected int mSurfaceWidth, mSurfaceHeight;

    AbstractInputConfig(int type) {
        mType = type;
    }

    public int getType() {
        return mType;
    }

    public int getSurfaceWidth() {
        return mSurfaceWidth;
    }

    public int getSurfaceHeight() {
        return mSurfaceHeight;
    }
}
