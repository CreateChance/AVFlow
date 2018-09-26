package com.createchance.avflowengine.config;

public abstract class AbstractOutputConfig {
    public static final int TYPE_PREVIEW = 0;
    public static final int TYPE_SAVE = 1;

    public final int mType;

    AbstractOutputConfig(int type) {
        mType = type;
    }
}
