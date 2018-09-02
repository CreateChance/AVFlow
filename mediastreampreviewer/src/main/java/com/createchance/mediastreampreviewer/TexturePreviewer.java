package com.createchance.mediastreampreviewer;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;

import com.createchance.mediastreambase.AVFrame;
import com.createchance.mediastreambase.IVideoInputSurfaceListener;
import com.createchance.mediastreambase.IVideoStreamConsumer;
import com.createchance.mediastreambase.VideoInputSurface;

/**
 * ${DESC}
 *
 * @author createchance
 * @date 2018/9/2
 */
public class TexturePreviewer extends TextureView implements IVideoStreamConsumer,
        TextureView.SurfaceTextureListener {

    private static final String TAG = "TexturePreviewer";

    private Context mContext;

    private IVideoInputSurfaceListener mListener;

    private VideoInputSurface mInputSurface;

    public TexturePreviewer(Context context) {
        super(context);
        init(context);
    }

    public TexturePreviewer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TexturePreviewer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    public void setInputSurfaceListener(IVideoInputSurfaceListener listener) {
        mListener = listener;
    }

    @Override
    public void onNewVideoFrame(AVFrame videoFrame) {

    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mInputSurface = new VideoInputSurface();
        mInputSurface.mSurfaceTexture = surface;
        mInputSurface.mSurface = new Surface(surface);
        if (mListener != null) {
            mListener.onConsumerSurfaceCreated(this, mInputSurface);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        if (mListener != null) {
            mListener.onConsumerSurfaceChanged(this, mInputSurface, width, height);
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mListener != null) {
            mListener.onConsumerSurfaceDestroyed(this, mInputSurface);
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private void init(Context context) {
        mContext = context;
        setSurfaceTextureListener(this);
    }
}
