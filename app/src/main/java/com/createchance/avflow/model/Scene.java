package com.createchance.avflow.model;

import android.graphics.Bitmap;

import java.io.File;
import java.util.List;

/**
 * Scene data
 *
 * @author createchance
 * @date 2018-10-13
 */
public class Scene {
    public File mVideo;
    public Filter mFilter;
    public float mSpeedRate;
    public int mWidth, mHeight;
    public Text mText;
    public StickerList mStickerList;

    public static class Text {
        public String mFontPath;
        public String mValue;
        public int mPosX, mPosY;
        public int mTextSize = 90;
        public float mRed = 1f, mGreen = 1f, mBlue = 1f;
        public Bitmap mBackground;
    }

    public static class StickerList {
        public List<String> mValue;
        public int mPosX, mPosY;
        public float mScaleFactor;
    }
}
