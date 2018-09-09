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
public class MuxerStreamSaver {

    private static final String TAG = "MuxerStreamSaver";

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

    public MuxerStreamSaver() {
        mSaveThread = new SaverThread();
    }

    public Surface getInputSurface() {
        return mInputSurface;
    }

    public void setOutputSize(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
    }

    public void prepare() {
        // init video format
        MediaFormat videoFormat = MediaFormat.createVideoFormat("video/avc", mVideoWidth, mVideoHeight);
        videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, 3000000);
        videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
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

    public void beginSave(File outputFile, SaveListener listener) {
        mOutputFile = outputFile;
        if (mOutputFile != null) {
            // init muxer
            try {
                mMuxer = new MediaMuxer(mOutputFile.getAbsolutePath(),
                        MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
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
            doMux();
            release();
            if (mNeedDelete) {
                mNeedDelete = false;
                if (mOutputFile != null) {
                    mOutputFile.delete();
                }
            }
        }

        private void doMux() {
            int outputBufferId;
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            ByteBuffer buffer;
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

                    Log.i(TAG, "doMux...........");
                    mMuxer.writeSampleData(mVideoTrackId, buffer, bufferInfo);
                    mEncoder.releaseOutputBuffer(outputBufferId, false);
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
