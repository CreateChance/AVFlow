package com.createchance.avflowengine.config;

import com.createchance.avflowengine.saver.SaveListener;

public class SaveOutputConfig extends AbstractOutputConfig {

    public static final int ROTATION_0 = 0;
    public static final int ROTATION_90 = 90;
    public static final int ROTATION_180 = 180;
    public static final int ROTATION_270 = 270;

    private int mFrameRate = 30;

    private int mOutputRotation;
    private SaveListener mSaveListener;
    private int mClipTop, mClipLeft, mClipBottom, mClipRight;

    private SaveOutputConfig() {
        super(TYPE_SAVE);
    }

    public int getFrameRate() {
        return mFrameRate;
    }

    public int getOutputRotation() {
        return mOutputRotation;
    }

    public SaveListener getSaveListener() {
        return mSaveListener;
    }

    public int getClipTop() {
        return mClipTop;
    }

    public int getClipLeft() {
        return mClipLeft;
    }

    public int getClipBottom() {
        return mClipBottom;
    }

    public int getClipRight() {
        return mClipRight;
    }

    public static class Builder {
        private SaveOutputConfig config = new SaveOutputConfig();

        public Builder frameRate(int frameRate) {
            config.mFrameRate = frameRate;

            return this;
        }

        public Builder rotation(int rotation) {
            config.mOutputRotation = rotation;

            return this;
        }

        public Builder clipArea(int clipTop, int clipLeft, int clipBottom, int clipRight) {
            config.mClipTop = clipTop;
            config.mClipLeft = clipLeft;
            config.mClipBottom = clipBottom;
            config.mClipRight = clipRight;

            return this;
        }

        public Builder listener(SaveListener listener) {
            config.mSaveListener = listener;

            return this;
        }

        public SaveOutputConfig build() {
            return config;
        }
    }
}
