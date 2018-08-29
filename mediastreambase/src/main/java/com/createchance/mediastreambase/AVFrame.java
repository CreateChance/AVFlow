package com.createchance.mediastreambase;

import java.nio.ByteBuffer;

/**
 * ${DESC}
 *
 * @author gaochao1-iri
 * @date 2018/8/27
 */
public class AVFrame {
    public boolean mIsLast;

    public long mPresentTime;

    public int mFlag;

    public int mSampleSize;

    public ByteBuffer mBuffer;

    void reset() {
        mIsLast = false;
        mPresentTime = -1;
        mSampleSize = -1;
        mFlag = -1;
    }

    @Override
    public String toString() {
        return "AVFrame{" +
                "mIsLast=" + mIsLast +
                '}';
    }
}
