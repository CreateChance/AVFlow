package com.createchance.avflow;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.createchance.avflow.model.Scene;
import com.createchance.avflow.model.SimpleModel;

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
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, VideoComposeActivity.class);
        context.startActivity(intent);
    }
}
