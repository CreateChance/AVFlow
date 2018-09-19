package com.createchance.avflowengine.generator;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;

import java.io.File;

public class VideoFile {
    public File mFile;
    public int mVideoTrackId = -1, mAudioTrackId = -1;
    public long mDurationUs;
    public long mVideoFrameInterval;
    public int mOrientation;
    public MediaFormat mVideoFormat, mAudioFormat;
    public MediaExtractor mVideoExtractor, mAudioExtractor;
    public MediaCodec mVideoDecoder, mAudioDecoder;
}
