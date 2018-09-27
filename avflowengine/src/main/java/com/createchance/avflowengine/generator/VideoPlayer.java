package com.createchance.avflowengine.generator;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.view.Surface;

import com.createchance.avflowengine.base.Logger;
import com.createchance.avflowengine.base.WorkRunner;
import com.createchance.avflowengine.config.FileInputConfig;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Video player, extract video frame and play it on target surface.
 *
 * @author createchance
 * @date 2018-09-19
 */
class VideoPlayer {

    private static final String TAG = "VideoPlayer";

    private List<File> mInputFileList;

    private Surface mOutputSurface;

    private PlayWorker mPlayWork;

    private boolean mLoop;

    private boolean mStop;

    private float mSpeedRate;

    private FilePlayListener mListener;

    VideoPlayer() {

    }

    void setOutputSurface(Surface outputSurface) {
        mOutputSurface = outputSurface;
    }

    void setInputFileList(List<File> inputFileList) {
        mInputFileList = inputFileList;
    }

    void setLoop(boolean loop) {
        mLoop = loop;
    }

    void setSpeedRate(float rate) {
        mSpeedRate = rate;
    }

    boolean start(FilePlayListener listener) {
        if (mOutputSurface == null) {
            Logger.e(TAG, "Output surface can not be null!");
            return false;
        }

        if (mInputFileList == null || mInputFileList.size() == 0) {
            Logger.e(TAG, "Input file list can not be empty!");
            return false;
        }

        if (mPlayWork == null) {
            mPlayWork = new PlayWorker();
        }
        mListener = listener;
        WorkRunner.addTaskToBackground(mPlayWork);
        Logger.d(TAG, "Start playing now.");

        return true;
    }

    void stop() {
        Logger.d(TAG, "Stop playing now.");
        mStop = true;
        if (mPlayWork != null) {
            mPlayWork.waitForStop();
        }
    }

    private class PlayWorker implements Runnable {
        private final String VIDEO_MIME_PREFIX = "video";
        private final String AUDIO_MIME_PREFIX = "audio";

        private List<VideoFile> mVideoList;

        private final long WAIT_TIME_OUT = 0;

        private final Object mStopLock = new Object();

        private long mBaseTimeUS, mGlobalTimeUs, mTotalDurationUs;

        @Override
        public void run() {
            try {
                prepare();
                while (true) {
                    if (mListener != null) {
                        mListener.onListPlayStarted();
                    }
                    play();
                    if (mListener != null) {
                        mListener.onListPlayDone();
                    }
                    if (mStop) {
                        mStop = false;
                        Logger.d(TAG, "We are asking to stop, so stop now.");
                        break;
                    }
                    if (mLoop) {
                        Logger.d(TAG, "Play list done, but we are going to loop.");
                        resetExtractor();
                    } else {
                        Logger.d(TAG, "Play list done, so we are going to stop.");
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                release();
            }

            synchronized (mStopLock) {
                mStopLock.notify();
            }

            Logger.d(TAG, "Play worker done!");
        }

        public void waitForStop() {
            synchronized (mStopLock) {
                try {
                    mStopLock.wait();
                } catch (InterruptedException ie) {
                    // discard
                }
            }
        }

        private void prepare() throws IOException {
            mVideoList = new ArrayList<>();
            for (File file : mInputFileList) {
                VideoFile videoFile = new VideoFile();
                videoFile.mFile = file;
                MediaExtractor extractor = new MediaExtractor();
                extractor.setDataSource(file.getAbsolutePath());
                for (int i = 0; i < extractor.getTrackCount(); i++) {
                    MediaFormat mediaFormat = extractor.getTrackFormat(i);
                    String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
                    if (mime.startsWith(VIDEO_MIME_PREFIX)) {
                        videoFile.mVideoTrackId = i;
                        videoFile.mVideoFormat = mediaFormat;
                        videoFile.mDurationUs = mediaFormat.getLong(MediaFormat.KEY_DURATION);
                        mTotalDurationUs += videoFile.mDurationUs;
                        videoFile.mVideoFrameInterval = 1000 / mediaFormat.getInteger(MediaFormat.KEY_FRAME_RATE);
                        MediaExtractor videoExtractor = new MediaExtractor();
                        videoExtractor.setDataSource(videoFile.mFile.getAbsolutePath());
                        videoFile.mVideoExtractor = videoExtractor;
                    } else if (mime.startsWith(AUDIO_MIME_PREFIX)) {
                        videoFile.mAudioTrackId = i;
                        videoFile.mAudioFormat = mediaFormat;
                        MediaExtractor audioExtractor = new MediaExtractor();
                        audioExtractor.setDataSource(videoFile.mFile.getAbsolutePath());
                        videoFile.mVideoExtractor = audioExtractor;
                    }
                }
                MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                mediaMetadataRetriever.setDataSource(file.getAbsolutePath());
                videoFile.mOrientation = Integer.valueOf(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
                mediaMetadataRetriever.release();
                mVideoList.add(videoFile);
            }
            // We get the first one's mime type as decoder type.
            if (mVideoList.size() != 0) {
                for (VideoFile videoFile : mVideoList) {
                    if (videoFile.mVideoTrackId != -1) {
                        videoFile.mVideoDecoder = MediaCodec.createDecoderByType(videoFile.mVideoFormat.getString(MediaFormat.KEY_MIME));
                    }
                }
            }
        }

        private void resetExtractor() {
            for (VideoFile videoFile : mVideoList) {
                if (videoFile.mVideoExtractor != null) {
                    videoFile.mVideoExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                }
                if (videoFile.mAudioExtractor != null) {
                    videoFile.mAudioExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                }
            }
        }

        private void play() {
            int videoInputBufferId, videoOutputBufferId;
            ByteBuffer buffer;
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int sampleSize;

            Iterator<VideoFile> videoFileIterator = mVideoList.iterator();
            while (true) {
                if (mStop) {
                    break;
                }

                if (!videoFileIterator.hasNext()) {
                    Logger.d(TAG, "All list play done.");
                    break;
                }

                VideoFile currentVideoFile = videoFileIterator.next();

                if (mListener != null) {
                    mListener.onFilePlayStarted(mVideoList.indexOf(currentVideoFile), currentVideoFile.mFile);
                }

                if (currentVideoFile.mVideoTrackId != -1) {
                    // select video track id.
                    currentVideoFile.mVideoExtractor.selectTrack(currentVideoFile.mVideoTrackId);
                    currentVideoFile.mVideoDecoder.configure(currentVideoFile.mVideoFormat, mOutputSurface, null, 0);
                    currentVideoFile.mVideoDecoder.start();
                    boolean reachVideoEos = false;
                    while (true) {
                        if (mStop) {
                            break;
                        }
                        if (!reachVideoEos) {
                            // adjust play speed here.
                            if (mSpeedRate != FileInputConfig.SPEEDRATE_FASTEST) {
                                try {
                                    Thread.sleep((long) (currentVideoFile.mVideoFrameInterval / mSpeedRate));
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            videoInputBufferId = currentVideoFile.mVideoDecoder.dequeueInputBuffer(WAIT_TIME_OUT);
                            if (videoInputBufferId >= 0) {
                                if (Build.VERSION.SDK_INT < 21) {
                                    buffer = currentVideoFile.mVideoDecoder.getInputBuffers()[videoInputBufferId];
                                } else {
                                    buffer = currentVideoFile.mVideoDecoder.getInputBuffer(videoInputBufferId);
                                }

                                sampleSize = currentVideoFile.mVideoExtractor.readSampleData(buffer, 0);
                                if (sampleSize == -1) {
                                    reachVideoEos = true;
                                    currentVideoFile.mVideoDecoder.queueInputBuffer(
                                            videoInputBufferId,
                                            0,
                                            0,
                                            currentVideoFile.mVideoExtractor.getSampleTime(),
                                            MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                                } else {
                                    currentVideoFile.mVideoDecoder.queueInputBuffer(
                                            videoInputBufferId,
                                            0,
                                            sampleSize,
                                            currentVideoFile.mVideoExtractor.getSampleTime(),
                                            0);
                                }
                                currentVideoFile.mVideoExtractor.advance();
                            }
                        } else {
                            Logger.d(TAG, "We have already reached eos of video track, so skip.");
                        }

                        while (true) {
                            videoOutputBufferId = currentVideoFile.mVideoDecoder.dequeueOutputBuffer(bufferInfo, WAIT_TIME_OUT);
                            if (videoOutputBufferId == MediaCodec.INFO_TRY_AGAIN_LATER) {
                                Logger.v(TAG, "No more video data for now, try again later.");
                                break;
                            } else if (videoOutputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                                Logger.d(TAG, "Output format changed, current format: " + currentVideoFile.mVideoDecoder.getOutputFormat());
                            } else if (videoOutputBufferId == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                                Logger.d(TAG, "Output buffers changed.");
                            } else if (videoOutputBufferId >= 0) {
                                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                                    break;
                                } else {
                                    Logger.v(TAG, "Playing video, current frame info: " + bufferInfo);
                                }

                                mGlobalTimeUs = bufferInfo.presentationTimeUs + mBaseTimeUS;

                                if (mListener != null) {
                                    mListener.onFilePlayGoing(
                                            bufferInfo.presentationTimeUs,
                                            currentVideoFile.mDurationUs,
                                            mGlobalTimeUs,
                                            mTotalDurationUs,
                                            currentVideoFile.mFile);
                                }

                                currentVideoFile.mVideoDecoder.releaseOutputBuffer(videoOutputBufferId, bufferInfo.size != 0);
                            }
                        }
                        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            Logger.d(TAG, "Reach video eos of file: " + currentVideoFile.mFile);
                            bufferInfo = new MediaCodec.BufferInfo();
                            currentVideoFile.mVideoDecoder.flush();
                            currentVideoFile.mVideoDecoder.stop();
                            break;
                        }
                    }
                } else {
                    Logger.e(TAG, "Current file has not video track, file: " + currentVideoFile.mFile);
                }

                mBaseTimeUS += currentVideoFile.mDurationUs;
                if (mListener != null) {
                    mListener.onFilePlayDone(mVideoList.indexOf(currentVideoFile), currentVideoFile.mFile);
                }
            }
        }

        private void release() {
            if (mVideoList != null) {
                for (VideoFile videoFile : mVideoList) {
                    if (videoFile.mVideoDecoder != null) {
                        videoFile.mVideoDecoder.stop();
                        videoFile.mVideoDecoder.release();
                    }
                    if (videoFile.mAudioDecoder != null) {
                        videoFile.mAudioDecoder.stop();
                        videoFile.mAudioDecoder.release();
                    }
                    if (videoFile.mVideoExtractor != null) {
                        videoFile.mVideoExtractor.release();
                    }
                    if (videoFile.mAudioExtractor != null) {
                        videoFile.mAudioExtractor.release();
                    }
                }
            }
        }
    }

}
