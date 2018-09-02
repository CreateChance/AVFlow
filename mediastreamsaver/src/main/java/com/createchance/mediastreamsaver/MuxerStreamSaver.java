package com.createchance.mediastreamsaver;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.createchance.mediastreambase.AVFrame;
import com.createchance.mediastreambase.IVideoInputSurfaceListener;
import com.createchance.mediastreambase.IVideoStreamConsumer;
import com.createchance.mediastreambase.Logger;
import com.createchance.mediastreambase.VideoInputSurface;
import com.createchance.mediastreambase.WorkRunner;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * ${DESC}
 *
 * @author createchance
 * @date 2018/8/27
 */
public final class MuxerStreamSaver implements IVideoStreamConsumer, Handler.Callback {

    private static final String TAG = "MuxerStreamSaver";

    private IVideoInputSurfaceListener mListener;

    private File mOutputFile;
    private int mVideoWidth, mVideoHeight;

    private VideoInputSurface mInputSurface;

    private Handler mHandler;
    private final int MSG_INIT_DONE = 100;

    private SaverThread mSaveThread;

    private MuxerStreamSaver() {
        mHandler = new Handler(this);
    }

    @Override
    public void setInputSurfaceListener(IVideoInputSurfaceListener listener) {
        mListener = listener;

        mSaveThread = new SaverThread();
        WorkRunner.addTaskToBackground(mSaveThread);
    }

    @Override
    public void onNewVideoFrame(AVFrame videoFrame) {

    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_INIT_DONE:
                if (mListener != null) {
                    mListener.onConsumerSurfaceCreated(MuxerStreamSaver.this, mInputSurface);
                    mListener.onConsumerSurfaceChanged(MuxerStreamSaver.this, mInputSurface, mVideoWidth, mVideoHeight);
                }
                break;
            default:
                break;
        }

        return true;
    }

    public void startSave() {
        mSaveThread.startSave();
    }

    public void stopSave() {
        mSaveThread.stopSave();
    }

    private class SaverThread implements Runnable {

        private final long TIME_OUT = 5000;

        private MediaMuxer mMuxer;
        private MediaCodec mEncoder;
        private int mVideoTrackId;

        private boolean mSave;

        @Override
        public void run() {
            try {
                prepare();
                doMux();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                release();
            }
        }

        public void startSave() {
            mMuxer.start();
            mSave = true;
        }

        public void stopSave() {
            mSave = false;
            mMuxer.stop();
        }

        private void prepare() throws IOException {
            // init video format
            MediaFormat videoFormat = MediaFormat.createVideoFormat("video/avc", mVideoWidth, mVideoHeight);
            videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, 3000000);
            videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
            videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

            // init muxer
            mMuxer = new MediaMuxer(mOutputFile.getAbsolutePath(),
                    MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            // init encoder
            mEncoder = MediaCodec.createEncoderByType("video/avc");
            mEncoder.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mInputSurface = new VideoInputSurface();
            mInputSurface.mSurface = mEncoder.createInputSurface();
            mEncoder.start();

            mHandler.sendEmptyMessage(MSG_INIT_DONE);
        }

        private void doMux() {
            int outputBufferId;
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            ByteBuffer buffer;
            while (true) {
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
                mMuxer.stop();
                mMuxer.release();
            }
        }
    }

    public static class Builder {
        MuxerStreamSaver saver = new MuxerStreamSaver();

        public Builder output(File outputFile) {
            saver.mOutputFile = outputFile;

            return this;
        }

        public Builder videoSize(int width, int height) {
            saver.mVideoWidth = width;
            saver.mVideoHeight = height;

            return this;
        }

        public MuxerStreamSaver build() {
            return saver;
        }
    }
}
