package com.createchance.avflow.model;

import android.graphics.Bitmap;

import java.io.File;

public class Scene {
    public File mVideo;
    public Filter mFilter;
    public float mSpeedRate;
    public int mWidth, mHeight;
    public Text mText;

    public static class Text {
        public String mFontPath;
        public String mValue;
        public int mPosX, mPosY;
        public int mTextSize = 90;
        public float mRed = 1f, mGreen = 1f, mBlue = 1f;
        public Bitmap mBackground;
    }
}
