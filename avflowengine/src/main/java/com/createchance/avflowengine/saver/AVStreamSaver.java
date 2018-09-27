package com.createchance.avflowengine.saver;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

import com.createchance.avflowengine.base.Logger;
import com.createchance.avflowengine.base.UiThreadUtil;
import com.createchance.avflowengine.base.WorkRunner;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * ${DESC}
 *
 * @author createchance
 * @date 2018/8/27
 */
public class AVStreamSaver {

    private static final String TAG = "AVStreamSaver";

    private File mOutputFile;
    private int mVideoWidth, mVideoHeight;

    private Surface mInputSurface;

    private SaverThread mSaveThread;
    private MediaMuxer mMuxer;
    private MediaCodec mEncoder;
    private int mVideoTrackId = -1;
    private boolean mRequestStop;
    private boolean mNeedDelete;
    private SaveListener mListener;

    private int mBitRate = 3000000;
    private int mFrameRate = 30;

    public AVStreamSaver() {
        mSaveThread = new SaverThread();
    }

    public Surface getInputSurface() {
        return mInputSurface;
    }

    public void setOutputSize(int width, int height) {
        if (width % 2 != 0) {
            mVideoWidth = width + 1;
        } else {
            mVideoWidth = width;
        }
        if (height % 2 != 0) {
            mVideoHeight = height + 1;
        } else {
            mVideoHeight = height;
        }
    }

    public void setFrameRate(int frameRate) {
        mFrameRate = frameRate;
    }

    public void prepare() {
        // init video format
        MediaFormat videoFormat = MediaFormat.createVideoFormat("video/avc", mVideoWidth, mVideoHeight);
        videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, mBitRate);
        videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, mFrameRate);
        videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

        try {
            // init encoder
            mEncoder = MediaCodec.createEncoderByType("video/avc");
            mEncoder.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mInputSurface = mEncoder.createInputSurface();
            mEncoder.start();
        } catch (Exception e) {
            e.printStackTrace();
            if (mEncoder != null) {
                mEncoder.stop();
            }
        }
    }

    public void beginSave(File outputFile, int orientation, SaveListener listener) {
        mOutputFile = outputFile;
        if (mOutputFile != null) {
            // init muxer
            try {
                mMuxer = new MediaMuxer(mOutputFile.getAbsolutePath(),
                        MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                mMuxer.setOrientationHint(orientation);
                mListener = listener;
                WorkRunner.addTaskToBackground(mSaveThread);
            } catch (IOException e) {
                e.printStackTrace();
                if (mMuxer != null) {
                    mMuxer.release();
                }
            }
        }
    }

    public void finishSave() {
        mRequestStop = true;
    }

    public void cancelSave() {
        mNeedDelete = true;
        finishSave();
    }

    private class SaverThread implements Runnable {

        private final long TIME_OUT = 5000;

        @Override
        public void run() {
            if (mListener != null) {
                UiThreadUtil.post(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onStarted(mOutputFile);
                    }
                });
            }
            doMux();
            release();
            if (mNeedDelete) {
                mNeedDelete = false;
                if (mOutputFile != null) {
                    mOutputFile.delete();
                }
            }
            Logger.d(TAG, "Save worker done.");
        }

        private void doMux() {
            int outputBufferId;
            final MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            ByteBuffer buffer;
            long framePts = 1000 * 1000 / mFrameRate;
            long nowPts = 0;
            while (true) {
                if (mRequestStop) {
                    mRequestStop = false;
                    Logger.d(TAG, "Request stop, so we are stopping.");
                    break;
                }

                outputBufferId = mEncoder.dequeueOutputBuffer(bufferInfo, TIME_OUT);
                if (outputBufferId >= 0) {
                    if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        Logger.d(TAG, "Reach video eos.");
                        mEncoder.signalEndOfInputStream();
                        break;
                    }

                    if (Build.VERSION.SDK_INT >= 21) {
                        buffer = mEncoder.getOutputBuffer(outputBufferId);
                    } else {
                        buffer = mEncoder.getOutputBuffers()[outputBufferId];
                    }

                    bufferInfo.presentationTimeUs = nowPts;
                    nowPts += framePts;
                    Log.i(TAG, "doMux..........., pts: " + bufferInfo.presentationTimeUs);
                    mMuxer.writeSampleData(mVideoTrackId, buffer, bufferInfo);
                    mEncoder.releaseOutputBuffer(outputBufferId, false);
                    if (mListener != null) {
                        UiThreadUtil.post(new Runnable() {
                            @Override
                            public void run() {
                                mListener.onSaveGoing(bufferInfo.presentationTimeUs, mOutputFile);
                            }
                        });
                    }
                } else if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    MediaFormat videoFormat = mEncoder.getOutputFormat();
                    Logger.d(TAG, "Encode format: " + videoFormat);
                    mVideoTrackId = mMuxer.addTrack(videoFormat);
                    mMuxer.start();
                }
            }

            Logger.d(TAG, "Mux done!");
        }

        private void release() {
            if (mEncoder != null) {
                mEncoder.stop();
            }
            if (mMuxer != null) {
                if (mVideoTrackId != -1) {
                    mMuxer.stop();
                }
                mMuxer.release();
            }
            UiThreadUtil.post(new Runnable() {
                @Override
                public void run() {
                    if (mListener != null) {
                        mListener.onSaved(mOutputFile);
                    }
                }
            });
        }
    }
}
