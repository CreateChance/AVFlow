package com.createchance.mediastreamsaver;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;

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
public final class MuxerStreamSaver implements IVideoStreamConsumer {

    private static final String TAG = "MuxerStreamSaver";

    private IVideoInputSurfaceListener mListener;

    private File mOutputFile;
    private int mVideoWidth, mVideoHeight;

    private VideoInputSurface mInputSurface;

    private MuxerStreamSaver() {

    }

    @Override
    public void setInputSurfaceListener(IVideoInputSurfaceListener listener) {
        mListener = listener;

        WorkRunner.addTaskToBackground(new SaverThread());
    }

    @Override
    public void onNewVideoFrame(AVFrame videoFrame) {

    }

    private class SaverThread implements Runnable {

        private final long TIME_OUT = 5000;

        private MediaMuxer mMuxer;
        private MediaCodec mEncoder;
        private int mVideoTrackId;

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
            mVideoTrackId = mMuxer.addTrack(videoFormat);
            mMuxer.start();

            // init encoder
            mEncoder = MediaCodec.createEncoderByType("video/avc");
            mEncoder.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mInputSurface = new VideoInputSurface();
            mInputSurface.mSurface = mEncoder.createInputSurface();
            mEncoder.start();

            if (mListener != null) {
                mListener.onConsumerSurfaceCreated(MuxerStreamSaver.this, mInputSurface);
            }
        }

        private void doMux() {
            int outputBufferId;
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            ByteBuffer buffer;
            while (true) {
                outputBufferId = mEncoder.dequeueOutputBuffer(bufferInfo, TIME_OUT);
                Logger.i(TAG, "Output buffer id: " + outputBufferId);
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

                    mMuxer.writeSampleData(mVideoTrackId, buffer, bufferInfo);
                    mEncoder.releaseOutputBuffer(outputBufferId, false);
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
