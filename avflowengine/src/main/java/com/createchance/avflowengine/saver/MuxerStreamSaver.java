package com.createchance.avflowengine.saver;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

import com.createchance.avflowengine.base.Logger;
import com.createchance.avflowengine.base.WorkRunner;

import java.io.File;
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
    private int mVideoTrackId;
    private boolean mRequestStop;

    public Surface getInputSurface() {
        return mInputSurface;
    }

    public void setOutputSize(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
    }

    public void setOutputFile(File outputFile) {
        mOutputFile = outputFile;
    }

    public void prepare() {
        // init video format
        MediaFormat videoFormat = MediaFormat.createVideoFormat("video/avc", mVideoWidth, mVideoHeight);
        videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, 3000000);
        videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

        try {
            // init muxer
            mMuxer = new MediaMuxer(mOutputFile.getAbsolutePath(),
                    MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

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

            if (mMuxer != null) {
                mMuxer.stop();
                mMuxer.release();
            }
        }
    }

    public void start() {
        mSaveThread = new SaverThread();
        WorkRunner.addTaskToBackground(mSaveThread);
    }

    public void stop() {
        mRequestStop = true;
    }

    public void beginSave() {
        mSaveThread.beginSave();
    }

    public void finishSave() {
        mSaveThread.finishSave();
    }

    private class SaverThread implements Runnable {

        private final long TIME_OUT = 5000;

        private boolean mSave;

        @Override
        public void run() {
            doMux();
            release();
        }

        void beginSave() {
            mMuxer.start();
            mSave = true;
        }

        void finishSave() {
            mSave = false;
            mMuxer.stop();
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

                    if (mSave) {
                        mMuxer.writeSampleData(mVideoTrackId, buffer, bufferInfo);
                    }
                    mEncoder.releaseOutputBuffer(outputBufferId, false);
                    Log.d(TAG, "doMux...........");
                } else if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    MediaFormat encodeFormat = mEncoder.getOutputFormat();
                    Logger.d(TAG, "Encode format: " + encodeFormat);
                    // init muxer
                    mVideoTrackId = mMuxer.addTrack(encodeFormat);
                }
            }

            Logger.d(TAG, "Mux done!");
        }

        private void release() {
            if (mEncoder != null) {
                mEncoder.stop();
            }

            if (mMuxer != null) {
                if (mSave) {
                    mMuxer.stop();
                }
                mMuxer.release();
            }
        }
    }
}
