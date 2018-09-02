package com.createchance.mediastreampreviewer;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.createchance.mediastreambase.AVFrame;
import com.createchance.mediastreambase.IVideoInputSurfaceListener;
import com.createchance.mediastreambase.IVideoStreamConsumer;
import com.createchance.mediastreambase.VideoInputSurface;

/**
 * ${DESC}
 *
 * @author createchance
 * @date 2018/8/27
 */
public final class SurfacePreviewer extends SurfaceView implements IVideoStreamConsumer,
        SurfaceHolder.Callback {

    private static final String TAG = "SurfacePreviewer";

    private Context mContext;

    private VideoInputSurface mVideoInputSurface;

    private IVideoInputSurfaceListener mListener;

    public SurfacePreviewer(Context context) {
        super(context);
        onCreate(context);
    }

    public SurfacePreviewer(Context context, AttributeSet attrs) {
        super(context, attrs);
        onCreate(context);
    }

    public SurfacePreviewer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onCreate(context);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mVideoInputSurface = new VideoInputSurface();
        mVideoInputSurface.mSurfaceHolder = holder;
        mVideoInputSurface.mSurface = holder.getSurface();
        if (mListener != null) {
            mListener.onConsumerSurfaceCreated(this, mVideoInputSurface);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged: " + width + ", " + height);
        if (mListener != null) {
            mListener.onConsumerSurfaceChanged(this, mVideoInputSurface, width, height);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mListener != null) {
            mListener.onConsumerSurfaceDestroyed(this, mVideoInputSurface);
        }
    }

    private void onCreate(Context context) {
        this.mContext = context;
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
    }

    @Override
    public void setInputSurfaceListener(IVideoInputSurfaceListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onNewVideoFrame(AVFrame videoFrame) {

    }
}
