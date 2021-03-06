package com.createchance.avflow;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.createchance.avflow.model.Scene;
import com.createchance.avflow.model.SimpleModel;
import com.createchance.avflowengine.AVFlowEngine;
import com.createchance.avflowengine.base.UiThreadUtil;
import com.createchance.avflowengine.config.FileInputConfig;
import com.createchance.avflowengine.config.SaveOutputConfig;
import com.createchance.avflowengine.generator.FilePlayListener;
import com.createchance.avflowengine.saver.SaveListener;

import java.io.File;

/**
 * Video compose activity.
 *
 * @author createchacne
 * @date 2018-09-20
 */
public class VideoComposeActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "VideoComposeActivity";

    private SquareProgressBar mSquareProgressBar;

    private TextView mSaveInfoView;
    private Button mBtnComplete;

    private String mEngineToken;

    private boolean mIsComposing;

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

        mBtnComplete = findViewById(R.id.btn_complete);
        mBtnComplete.setOnClickListener(this);
        mSaveInfoView = findViewById(R.id.tv_save_info);
        mSquareProgressBar = findViewById(R.id.vw_square_progressbar);
        mSquareProgressBar.setColor(getResources().getColor(R.color.theme_red));
        mSquareProgressBar.setProgress(0);
        mSquareProgressBar.setWidth(3);
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(SimpleModel.getInstance().getSceneList().get(0).mVideo.getAbsolutePath());
        mSquareProgressBar.setImage(mediaMetadataRetriever.getFrameAtTime());
        mediaMetadataRetriever.release();

        mEngineToken = AVFlowEngine.getInstance().newWorker();
        FileInputConfig.Builder inputConfigBuilder = new FileInputConfig.Builder()
                .surfaceSize(SimpleModel.getInstance().getSceneList().get(0).mWidth,
                        SimpleModel.getInstance().getSceneList().get(0).mHeight)
                .rotation(FileInputConfig.ROTATION_180)
                .speedRate(FileInputConfig.SPEED_RATE_NORMAL)
                .loop(false)
                .listener(new FilePlayListener() {
                    @Override
                    public void onListPlayStarted() {
                        Log.d(TAG, "onListPlayStarted: ");
                    }

                    @Override
                    public void onFilePlayStarted(int position, File file) {
                        Log.d(TAG, "onFilePlayStarted: " + position + ", file: " + file);
                        Scene scene = SimpleModel.getInstance().getSceneList().get(position);
                        AVFlowEngine.getInstance().setSaveFilter(
                                mEngineToken,
                                scene.mFilter.get(VideoComposeActivity.this));

                        if (scene.mText != null) {
                            AVFlowEngine.getInstance().setSaveText(mEngineToken,
                                    scene.mText.mFontPath,
                                    scene.mText.mValue,
                                    scene.mText.mPosX,
                                    scene.mText.mPosY,
                                    scene.mText.mTextSize,
                                    scene.mText.mRed,
                                    scene.mText.mGreen,
                                    scene.mText.mBlue,
                                    scene.mText.mBackground);
                        } else {
                            AVFlowEngine.getInstance().removeSaveText(mEngineToken);
                        }

                        if (scene.mStickerList != null) {
                            Bitmap sticker = BitmapFactory.decodeFile(scene.mStickerList.mValue.get(0));
                            int stickerWidth = sticker.getWidth();
                            int stickerHeight = sticker.getHeight();
                            sticker.recycle();
                            int videoWidth = SimpleModel.getInstance().getSceneList().get(0).mWidth;
                            int videoHeight = SimpleModel.getInstance().getSceneList().get(0).mHeight;
                            AVFlowEngine.getInstance().setSaveImage(mEngineToken,
                                    scene.mStickerList.mValue,
                                    (videoWidth - stickerWidth) / 2,
                                    (videoHeight - stickerHeight) / 2,
                                    scene.mStickerList.mScaleFactor);
                        } else {
                            AVFlowEngine.getInstance().removeSaveImage(mEngineToken);
                        }
                    }

                    @Override
                    public void onFilePlayGoing(long currentTime, long duration, final long globalTime, final long totalDuration, File file) {
                        Log.d(TAG, "onFilePlayGoing: " + currentTime
                                + ", duration: " + duration
                                + ", global Time: " + globalTime
                                + ", total duration: " + totalDuration
                                + ", file: " + file);
                        UiThreadUtil.post(new Runnable() {
                            @Override
                            public void run() {
                                mSquareProgressBar.setProgress(globalTime * 100.0f / totalDuration);
                            }
                        });
                    }

                    @Override
                    public void onFilePlayDone(int position, File file) {
                        Log.d(TAG, "onFilePlayDone: " + position + ", file: " + file);
                    }

                    @Override
                    public void onListPlayDone() {
                        Log.d(TAG, "onListPlayDone: ");
                        UiThreadUtil.post(new Runnable() {
                            @Override
                            public void run() {
                                mSquareProgressBar.setProgress(100);
                                mSaveInfoView.setText(R.string.video_composed);
                                mBtnComplete.setVisibility(View.VISIBLE);
                                mIsComposing = false;
                            }
                        });
                        AVFlowEngine.getInstance().finishSave(mEngineToken);
                    }
                });
        for (Scene scene : SimpleModel.getInstance().getSceneList()) {
            inputConfigBuilder.addFile(scene.mVideo);
        }
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(SimpleModel.getInstance().getSceneList().get(0).mVideo.getAbsolutePath());
        int bitrate = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE));
        retriever.release();
        Log.d(TAG, "onCreate, bitrate: " + bitrate);
        SaveOutputConfig outputConfig = new SaveOutputConfig.Builder()
                .rotation(SaveOutputConfig.ROTATION_0)
                .frameRate(30)
                .bitrate(3000000)
                .clipArea(
                        0,
                        0,
                        SimpleModel.getInstance().getSceneList().get(0).mHeight,
                        SimpleModel.getInstance().getSceneList().get(0).mWidth)
                .listener(new SaveListener() {
                    @Override
                    public void onStarted(File file) {

                    }

                    @Override
                    public void onSaveGoing(long currentDurationUs, File file) {

                    }

                    @Override
                    public void onSaved(File file) {
                        Log.d(TAG, "onSaved: " + file);
                    }
                })
                .build();
        AVFlowEngine.getInstance().configInput(mEngineToken, inputConfigBuilder.build());
        AVFlowEngine.getInstance().configOutput(mEngineToken, outputConfig);
        AVFlowEngine.getInstance().startSave(mEngineToken, new File(Environment.getExternalStorageDirectory(), "avflow/final.mp4"));
        AVFlowEngine.getInstance().start(mEngineToken);
        mIsComposing = true;
    }

    @Override
    public void onBackPressed() {
        if (mIsComposing) {
            Toast.makeText(this, R.string.video_composing, Toast.LENGTH_SHORT).show();
            return;
        }
        SimpleModel.getInstance().getSceneList().clear();
        AVFlowEngine.getInstance().stop(mEngineToken);
        MainActivity.start(this);
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, VideoComposeActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_complete:
                onBackPressed();
                break;
            default:
                break;
        }
    }
}
