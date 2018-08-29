package com.createchance.localstreamgenerator;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import com.createchance.mediastreambase.AVFrame;
import com.createchance.mediastreambase.AbstractStreamGenerator;
import com.createchance.mediastreambase.Constants;

import java.io.File;
import java.nio.ByteBuffer;

/**
 * ${DESC}
 *
 * @author createchance
 * @date 2018/8/27
 */
public final class LocalStreamGenerator extends AbstractStreamGenerator {

    private static final String TAG = "LocalStreamGenerator";

    private final long WAIT_TIMEOUT = 5000;

    private File mSourceFile;

    private MediaExtractor mAudioExtractor, mVideoExtractor;
    private MediaCodec mAudioDecoder, mVideoDecoder;
    private int mAudioTrackId = -1, mVideoTrackId = -1;
    private boolean reachAudioEos = false;
    private boolean reachVideoEos = false;
    private ByteBuffer mAudioBuffer, mVideoBuffer;

    private LocalStreamGenerator() {

    }

    @Override
    protected boolean init() {
        Log.d(TAG, "init: ");

        // alloc buffer
        mAudioBuffer = ByteBuffer.allocate(512 * 1024);
        mVideoBuffer = ByteBuffer.allocate(512 * 1024);

        try {
            mAudioExtractor = new MediaExtractor();
            mAudioExtractor.setDataSource(mSourceFile.getAbsolutePath());
            mVideoExtractor = new MediaExtractor();
            mVideoExtractor.setDataSource(mSourceFile.getAbsolutePath());
            for (int i = 0; i < mAudioExtractor.getTrackCount(); i++) {
                MediaFormat mediaFormat = mAudioExtractor.getTrackFormat(i);
                String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith(Constants.AUDIO_PREFIX)) {
                    mAudioTrackId = i;
                    mAudioExtractor.selectTrack(mAudioTrackId);
                    mAudioDecoder = MediaCodec.createDecoderByType(mime);
                    mAudioDecoder.configure(mediaFormat, null, null, 0);
                    mAudioDecoder.start();
                } else if (mime.startsWith(Constants.VIDEO_PREFIX)) {
                    mVideoTrackId = i;
                    mVideoExtractor.selectTrack(mVideoTrackId);
                    mVideoDecoder = MediaCodec.createDecoderByType(mime);
                    mVideoDecoder.configure(mediaFormat, getVideoSurface(), null, 0);
                    mVideoDecoder.start();
                }

                if (mAudioTrackId != -1 && mVideoTrackId != -1) {
                    break;
                }
            }

            if (mAudioTrackId == -1 && mVideoTrackId == -1) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    protected void run() {
        generateAudioFrame();
        generateVideoFrame();
    }

    @Override
    protected void shutdown() {
        Log.d(TAG, "shutdown: ");
        if (mAudioExtractor != null) {
            mAudioExtractor.release();
        }

        if (mVideoExtractor != null) {
            mVideoExtractor.release();
        }

        if (mAudioDecoder != null) {
            mAudioDecoder.stop();
            mAudioDecoder.release();
        }

        if (mVideoDecoder != null) {
            mVideoDecoder.stop();
            mVideoDecoder.release();
        }
    }

    private void generateAudioFrame() {
        int sampleSize = -1;
        int inputBufferId, outputBufferId;
        ByteBuffer buffer;
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        AVFrame audioFrame = new AVFrame();

        if (mAudioTrackId == -1) {
            return;
        }

        while (!reachAudioEos) {
            inputBufferId = mAudioDecoder.dequeueInputBuffer(WAIT_TIMEOUT);
            if (inputBufferId != -1) {
                if (Build.VERSION.SDK_INT >= 21) {
                    buffer = mAudioDecoder.getInputBuffer(inputBufferId);
                } else {
                    buffer = mAudioDecoder.getInputBuffers()[inputBufferId];
                }

                sampleSize = mAudioExtractor.readSampleData(buffer, 0);
                if (sampleSize < 0) {
                    mAudioDecoder.queueInputBuffer(
                            inputBufferId,
                            0,
                            0,
                            mAudioExtractor.getSampleTime(),
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    reachAudioEos = true;
                } else {
                    mAudioDecoder.queueInputBuffer(
                            inputBufferId,
                            0,
                            sampleSize,
                            mAudioExtractor.getSampleTime(),
                            0);
                    mAudioExtractor.advance();
                }

                // clear buffer
                buffer.clear();

                while (true) {
                    outputBufferId = mAudioDecoder.dequeueOutputBuffer(bufferInfo, WAIT_TIMEOUT);
                    if (outputBufferId >= 0) {
                        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            audioFrame.mIsLast = true;
                            audioFrame.mFlag = bufferInfo.flags;
                            audioFrame.mPresentTime = bufferInfo.presentationTimeUs;
                            audioFrame.mSampleSize = bufferInfo.size;
                            audioFrame.mBuffer = buffer;
                            outputAudioFrame(audioFrame);
                            break;
                        }
                        if (Build.VERSION.SDK_INT >= 21) {
                            buffer = mAudioDecoder.getOutputBuffer(outputBufferId);
                        } else {
                            buffer = mAudioDecoder.getOutputBuffers()[outputBufferId];
                        }
                        audioFrame.mFlag = bufferInfo.flags;
                        audioFrame.mPresentTime = bufferInfo.presentationTimeUs;
                        audioFrame.mSampleSize = bufferInfo.size;
                        audioFrame.mBuffer = buffer;
                        outputAudioFrame(audioFrame);
                        mAudioDecoder.releaseOutputBuffer(outputBufferId, false);
                    }
                }
            }
        }
    }

    private void generateVideoFrame() {
        int sampleSize = -1;
        int inputBufferId, outputBufferId;
        ByteBuffer buffer;
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        AVFrame videoFrame = new AVFrame();

        if (mVideoTrackId == -1) {
            return;
        }

        while (!reachVideoEos) {
            inputBufferId = mVideoDecoder.dequeueInputBuffer(WAIT_TIMEOUT);
            if (inputBufferId != -1) {
                if (Build.VERSION.SDK_INT >= 21) {
                    buffer = mVideoDecoder.getInputBuffer(inputBufferId);
                } else {
                    buffer = mVideoDecoder.getInputBuffers()[inputBufferId];
                }

                sampleSize = mVideoExtractor.readSampleData(buffer, 0);
                if (sampleSize < 0) {
                    mVideoDecoder.queueInputBuffer(
                            inputBufferId,
                            0,
                            0,
                            mVideoExtractor.getSampleTime(),
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    reachVideoEos = true;
                } else {
                    mVideoDecoder.queueInputBuffer(
                            inputBufferId,
                            0,
                            sampleSize,
                            mVideoExtractor.getSampleTime(),
                            0);
                    mVideoExtractor.advance();
                }

                // clear buffer
                buffer.clear();

                while (true) {
                    outputBufferId = mVideoDecoder.dequeueOutputBuffer(bufferInfo, WAIT_TIMEOUT);
                    if (outputBufferId >= 0) {
                        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            videoFrame.mIsLast = true;
                            videoFrame.mFlag = bufferInfo.flags;
                            videoFrame.mPresentTime = bufferInfo.presentationTimeUs;
                            videoFrame.mSampleSize = bufferInfo.size;
                            videoFrame.mBuffer = buffer;
                            outputVideoFrame(videoFrame);
                            break;
                        }
                        if (Build.VERSION.SDK_INT >= 21) {
                            buffer = mVideoDecoder.getOutputBuffer(outputBufferId);
                        } else {
                            buffer = mVideoDecoder.getOutputBuffers()[outputBufferId];
                        }
                        videoFrame.mFlag = bufferInfo.flags;
                        videoFrame.mPresentTime = bufferInfo.presentationTimeUs;
                        videoFrame.mSampleSize = bufferInfo.size;
                        videoFrame.mBuffer = buffer;
                        outputVideoFrame(videoFrame);
                        mVideoDecoder.releaseOutputBuffer(outputBufferId, true);
                    }
                }
            }
        }
    }

    public static class Builder {
        private LocalStreamGenerator generator = new LocalStreamGenerator();

        public Builder sourceFile(File source) {
            generator.mSourceFile = source;

            return this;
        }

        public LocalStreamGenerator build() {
            return generator;
        }
    }
}
