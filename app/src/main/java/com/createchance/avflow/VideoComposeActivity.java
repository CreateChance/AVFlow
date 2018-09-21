package com.createchance.avflow;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.createchance.avflow.model.Scene;
import com.createchance.avflow.model.SimpleModel;
import com.createchance.avflowengine.AVFlowEngine;
import com.createchance.avflowengine.base.UiThreadUtil;
import com.createchance.avflowengine.generator.VideoPlayListener;
import com.createchance.avflowengine.saver.SaveListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Video compose activity.
 *
 * @author createchacne
 * @date 2018-09-20
 */
public class VideoComposeActivity extends AppCompatActivity {

    private static final String TAG = "VideoComposeActivity";

    private SquareProgressBar mSquareProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_compose);

        View decorView = getWindow().getDecorView();
        int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(option);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        mSquareProgressBar = findViewById(R.id.vw_square_progressbar);
        mSquareProgressBar.setColor(getResources().getColor(R.color.theme_red));
        mSquareProgressBar.setProgress(40);
        mSquareProgressBar.setWidth(3);
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(SimpleModel.getInstance().getSceneList().get(0).mVideo.getAbsolutePath());
        mSquareProgressBar.setImage(mediaMetadataRetriever.getFrameAtTime());
        mediaMetadataRetriever.release();

        List<File> videoList = new ArrayList<>();
        for (Scene scene : SimpleModel.getInstance().getSceneList()) {
            videoList.add(scene.mVideo);
        }
        AVFlowEngine.getInstance().init();
        AVFlowEngine.getInstance().setInputSize(SimpleModel.getInstance().getSceneList().get(0).mWidth,
                SimpleModel.getInstance().getSceneList().get(0).mHeight);
        AVFlowEngine.getInstance().prepareSave(
                0,
                0,
                SimpleModel.getInstance().getSceneList().get(0).mHeight,
                SimpleModel.getInstance().getSceneList().get(0).mWidth);
        AVFlowEngine.getInstance().prepareEngine(180);
        AVFlowEngine.getInstance().startSave(
                new File(Environment.getExternalStorageDirectory(), "avflow/final.mp4"),
                0,
                new SaveListener() {
                    @Override
                    public void onSaved(File file) {
                        Log.d(TAG, "onSaved: " + file);
                    }
                }
        );
        AVFlowEngine.getInstance().startLocalGenerator(
                videoList,
                false,
                1.0f,
                new VideoPlayListener() {
                    @Override
                    public void onListPlayStarted() {
                        Log.d(TAG, "onListPlayStarted: ");
                    }

                    @Override
                    public void onFilePlayStarted(final int position, File file) {
                        Log.d(TAG, "onFilePlayStarted: " + file);
                        UiThreadUtil.post(new Runnable() {
                            @Override
                            public void run() {
                                AVFlowEngine.getInstance().setSaveFilter(SimpleModel.getInstance().getSceneList().get(position).mFilter.get(VideoComposeActivity.this));
                            }
                        });
                    }

                    @Override
                    public void onFilePlayGoing(long currentTime, long duration, File file) {
                        Log.d(TAG, "onFilePlayGoing: " + currentTime + ", duration: " + duration + ", file: " + file);
                    }

                    @Override
                    public void onFilePlayDone(int position, File file) {
                        Log.d(TAG, "onFilePlayDone: " + file);
                    }

                    @Override
                    public void onListPlayDone() {
                        Log.d(TAG, "onListPlayDone: ");
                        AVFlowEngine.getInstance().finishSave();
                    }
                }
        );
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, VideoComposeActivity.class);
        context.startActivity(intent);
    }
}
