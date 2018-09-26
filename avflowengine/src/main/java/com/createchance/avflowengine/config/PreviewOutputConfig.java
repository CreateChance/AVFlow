package com.createchance.avflowengine.config;

import android.view.Surface;

public class PreviewOutputConfig extends AbstractOutputConfig {

    private Surface mSurface;
    private int mSurfaceWidth, mSurfaceHeight;

    private PreviewOutputConfig() {
        super(TYPE_PREVIEW);
    }

    public static class Builder {
        private PreviewOutputConfig config = new PreviewOutputConfig();

        public Builder surface(Surface surface, int width, int height) {
            config.mSurface = surface;
            config.mSurfaceWidth = width;
            config.mSurfaceHeight = height;

            return this;
        }

        public PreviewOutputConfig build() {
            return config;
        }
    }

    public Surface getSurface() {
        return mSurface;
    }

    public int getSurfaceWidth() {
        return mSurfaceWidth;
    }

    public int getSurfaceHeight() {
        return mSurfaceHeight;
    }
}
