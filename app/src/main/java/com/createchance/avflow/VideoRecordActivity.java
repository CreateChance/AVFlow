package com.createchance.avflow;

import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;

import com.createchance.avflow.model.Scene;
import com.createchance.avflow.model.SimpleModel;
import com.createchance.avflowengine.AVFlowEngine;
import com.createchance.avflowengine.processor.gpuimage.GPUImageGammaFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VideoRecordActivity extends AppCompatActivity implements View.OnClickListener, TextureView.SurfaceTextureListener {

    private static final String TAG = "VideoRecordActivity";

    private RecyclerView mThumbListView;
    private SceneThumbListAdapter mListAdapter;
    private List<Scene> mSceneList;
    private ImageView mRatioView;
    private RoundProgressbar mCountDownView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_record);

        View decorView = getWindow().getDecorView();
        int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(option);

        initDefaultSceneList();

        TextureView previewer = findViewById(R.id.vw_previewer);
        previewer.setSurfaceTextureListener(this);
        mThumbListView = findViewById(R.id.rcv_scene_thumb_list);
        mListAdapter = new SceneThumbListAdapter(this, mSceneList);
        mThumbListView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mThumbListView.setAdapter(mListAdapter);
        mRatioView = findViewById(R.id.iv_choose_ratio);
        mRatioView.setOnClickListener(this);
        mCountDownView = findViewById(R.id.vw_count_down);
        mCountDownView.setOnClickListener(this);
        findViewById(R.id.iv_back).setOnClickListener(this);
        findViewById(R.id.iv_next).setOnClickListener(this);
        findViewById(R.id.iv_choose_filter).setOnClickListener(this);
        findViewById(R.id.iv_switch_camera).setOnClickListener(this);
        findViewById(R.id.iv_more).setOnClickListener(this);
        findViewById(R.id.iv_import_video).setOnClickListener(this);
        findViewById(R.id.vw_current_mode).setOnClickListener(this);

        AVFlowEngine.getInstance().setForceCameraV1(true);
        AVFlowEngine.getInstance().setOutputFile(new File(Environment.getExternalStorageDirectory(), "avflow/output.mp4"));
        AVFlowEngine.getInstance().setInputFile(new File(Environment.getExternalStorageDirectory(), "videoeditor/input2.mp4"));
        AVFlowEngine.getInstance().setPreviewFilter(new GPUImageGammaFilter());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        AVFlowEngine.getInstance().stop();
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, VideoRecordActivity.class);
        context.startActivity(intent);
    }

    private void initDefaultSceneList() {
        mSceneList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            mSceneList.add(new Scene());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                if (SimpleModel.getInstrance().getSceneList().size() == 0) {
                    finish();
                } else {
                    List<Scene> sceneList = SimpleModel.getInstrance().getSceneList();
                    for (Scene scene : sceneList) {
                        scene.mVideo.delete();
                    }
                }
                break;
            case R.id.iv_next:
                // goto video edit activity
                break;
            case R.id.iv_choose_ratio:

                break;
            case R.id.iv_choose_filter:

                break;
            case R.id.iv_switch_camera:

                break;
            case R.id.iv_more:

                break;
            case R.id.vw_count_down:
                break;
            case R.id.iv_import_video:

                break;
            case R.id.vw_current_mode:

                break;
            default:
                break;
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "onSurfaceTextureAvailable: " + width + ", " + height);
        AVFlowEngine.getInstance().setSurfaceSize(1080, 1920);
        AVFlowEngine.getInstance().setPreviewSurface(new Surface(surface));
        AVFlowEngine.getInstance().prepare();
        AVFlowEngine.getInstance().startCameraGenerator();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}
