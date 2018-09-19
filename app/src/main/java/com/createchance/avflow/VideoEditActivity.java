package com.createchance.avflow;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.createchance.avflow.model.Scene;
import com.createchance.avflow.model.SimpleModel;
import com.createchance.avflowengine.AVFlowEngine;
import com.createchance.avflowengine.base.UiThreadUtil;
import com.createchance.avflowengine.generator.VideoPlayListener;
import com.createchance.avflowengine.processor.gpuimage.GPUImageSwirlFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Video edit activity, do video editing here.
 *
 * @author createchacne
 * @date 2018-09-18
 */
public class VideoEditActivity extends AppCompatActivity implements View.OnClickListener, TextureView.SurfaceTextureListener {

    private static final String TAG = "VideoEditActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_edit);

        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setNavigationBarColor(Color.TRANSPARENT);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        findViewById(R.id.vw_back).setOnClickListener(this);
        findViewById(R.id.vw_next).setOnClickListener(this);
        final TextureView preview = findViewById(R.id.vw_previewer);
        preview.setSurfaceTextureListener(this);

        UiThreadUtil.post(new Runnable() {
            @Override
            public void run() {
                float ratio = SimpleModel.getInstance().getSceneList().get(0).mRatio;
                ViewGroup.LayoutParams layoutParams = preview.getLayoutParams();
                layoutParams.width = (int) (layoutParams.height / ratio);
                preview.setLayoutParams(layoutParams);
            }
        });
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, VideoEditActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        AVFlowEngine.getInstance().reset();
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.vw_back:
                onBackPressed();
                break;
            case R.id.vw_next:
                AVFlowEngine.getInstance().setPreviewFilter(new GPUImageSwirlFilter());
                break;
            default:
                break;
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "onSurfaceTextureAvailable: ");
        AVFlowEngine.getInstance().init();
        AVFlowEngine.getInstance().setInputSize(width, height);
        AVFlowEngine.getInstance().setPreview(new Surface(surface));
        AVFlowEngine.getInstance().prepare(180);

        // start play now.
        List<File> playList = new ArrayList<>();
        for (Scene scene : SimpleModel.getInstance().getSceneList()) {
            Log.d(TAG, "onSurfaceTextureAvailable, scene list: " + SimpleModel.getInstance().getSceneList());
            playList.add(scene.mVideo);
        }
        AVFlowEngine.getInstance().startLocalGenerator(
                playList,
                true,
                1.0f,
                new VideoPlayListener() {
                    @Override
                    public void onListPlayStarted() {

                    }

                    @Override
                    public void onFilePlayStarted(final int position, File file) {
                        UiThreadUtil.post(new Runnable() {
                            @Override
                            public void run() {
                                AVFlowEngine.getInstance().setPreviewFilter(
                                        SimpleModel.getInstance().getSceneList().get(position).mFilter.get(VideoEditActivity.this));
                            }
                        });
                    }

                    @Override
                    public void onFilePlayGoing(long currentTime, long duration, File file) {

                    }

                    @Override
                    public void onFilePlayDone(int position, File file) {

                    }

                    @Override
                    public void onListPlayDone() {

                    }
                });
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}
