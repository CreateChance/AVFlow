package com.createchance.avflow;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.createchance.avflow.model.Scene;
import com.createchance.avflow.model.SimpleModel;
import com.createchance.avflow.utils.DensityUtil;
import com.createchance.avflowengine.AVFlowEngine;
import com.createchance.avflowengine.base.Logger;
import com.createchance.avflowengine.generator.CameraImpl;
import com.createchance.avflowengine.saver.SaveListener;

import java.io.File;
import java.util.Locale;

public class VideoRecordActivity extends AppCompatActivity implements
        View.OnClickListener,
        TextureView.SurfaceTextureListener,
        Handler.Callback {

    private static final String TAG = "VideoRecordActivity";

    private final int MSG_UPDATE_COUNT = 100;

    private TextureView mPreview;
    private RecyclerView mThumbListView;
    private SceneThumbListAdapter mListAdapter;
    private int mCurrentScenePos;
    private ImageView mBack,
            mNext,
            mChooseRatioView,
            mChooseFilterView,
            mSwitchCameraView,
            mMoreView,
            mImportView;
    private View mCurrentModeView;
    private RoundProgressbar mCountDownView;
    private View mPanelChooseRatio, mPanelChooseFilter, mPanelMore;
    private View mUpperMask, mBottomMask;

    private Handler mHandler;

    // scenes
    private long mDurationOfScene = 5 * 1000;
    private long mCountStartTime;

    private int mScreenWidth, mScreenHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_record);

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

        mScreenWidth = getWindowManager().getDefaultDisplay().getWidth();
        mScreenHeight = getWindowManager().getDefaultDisplay().getHeight()
                + DensityUtil.getNaviBarHeight(this);
        AVFlowEngine.getInstance().setClipArea(
                0,
                0,
                mScreenHeight,
                mScreenWidth
        );

        mHandler = new Handler(this);

        mBack = findViewById(R.id.iv_back);
        mNext = findViewById(R.id.iv_next);
        mUpperMask = findViewById(R.id.vw_upper_mask);
        mBottomMask = findViewById(R.id.vw_bottom_mask);
        mPreview = findViewById(R.id.vw_previewer);
        mPreview.setSurfaceTextureListener(this);
        mThumbListView = findViewById(R.id.rcv_scene_thumb_list);
        mListAdapter = new SceneThumbListAdapter(this, SimpleModel.getInstance().getSceneList());
        mThumbListView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mThumbListView.setAdapter(mListAdapter);
        mChooseRatioView = findViewById(R.id.iv_choose_ratio);
        mChooseRatioView.setOnClickListener(this);
        mCountDownView = findViewById(R.id.vw_count_down);
        mCountDownView.setOnClickListener(this);
        mCountDownView.setText(String.valueOf(mDurationOfScene / 1000f) + "s");
        mChooseFilterView = findViewById(R.id.iv_choose_filter);
        mSwitchCameraView = findViewById(R.id.iv_switch_camera);
        mMoreView = findViewById(R.id.iv_more);
        mImportView = findViewById(R.id.iv_import_video);
        mCurrentModeView = findViewById(R.id.vw_current_mode);
        mPanelChooseRatio = findViewById(R.id.vw_choose_ratio);
        mPanelChooseFilter = findViewById(R.id.vw_choose_filter);
        mPanelMore = findViewById(R.id.vw_more);
        initPanelChooseRatio();
        initPanelChooseFilter();
        initPanelMore();
        mBack.setOnClickListener(this);
        mNext.setOnClickListener(this);
        mChooseFilterView.setOnClickListener(this);
        mSwitchCameraView.setOnClickListener(this);
        mMoreView.setOnClickListener(this);
        mImportView.setOnClickListener(this);
        mCurrentModeView.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPause() {
        super.onPause();

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        AVFlowEngine.getInstance().stop();
    }

    @Override
    public void onBackPressed() {
        if (mPanelChooseRatio.getVisibility() == View.VISIBLE) {
            mPanelChooseRatio.setVisibility(View.GONE);
            return;
        }
        if (mPanelChooseFilter.getVisibility() == View.VISIBLE) {
            mPanelChooseFilter.setVisibility(View.GONE);
            return;
        }
        if (mPanelMore.getVisibility() == View.VISIBLE) {
            mPanelMore.setVisibility(View.GONE);
            return;
        }
        for (Scene scene : SimpleModel.getInstance().getSceneList()) {
            if (scene.mVideo != null && scene.mVideo.exists()) {
                scene.mVideo.delete();
            }
        }

        super.onBackPressed();
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, VideoRecordActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        int animHeight;
        switch (v.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.iv_next:
                // goto video edit activity
                break;
            case R.id.iv_choose_ratio:
                if (mPanelChooseRatio.getVisibility() == View.VISIBLE) {
                    mPanelChooseRatio.setVisibility(View.GONE);
                } else {
                    mPanelChooseRatio.setVisibility(View.VISIBLE);
                }
                mPanelChooseFilter.setVisibility(View.GONE);
                mPanelMore.setVisibility(View.GONE);
                break;
            case R.id.iv_choose_filter:
                mPanelChooseRatio.setVisibility(View.GONE);
                if (mPanelChooseFilter.getVisibility() == View.VISIBLE) {
                    mPanelChooseFilter.setVisibility(View.GONE);
                } else {
                    mPanelChooseFilter.setVisibility(View.VISIBLE);
                }
                mPanelMore.setVisibility(View.GONE);
                break;
            case R.id.iv_switch_camera:
                if (AVFlowEngine.getInstance().getCamera().getFacing() == CameraImpl.FACING_BACK) {
                    AVFlowEngine.getInstance().getCamera().setFacing(CameraImpl.FACING_FRONT);
                } else {
                    AVFlowEngine.getInstance().getCamera().setFacing(CameraImpl.FACING_BACK);
                }
                break;
            case R.id.iv_more:
                mPanelChooseRatio.setVisibility(View.GONE);
                mPanelChooseFilter.setVisibility(View.GONE);
                if (mPanelMore.getVisibility() == View.VISIBLE) {
                    mPanelMore.setVisibility(View.GONE);
                } else {
                    mPanelMore.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.vw_count_down:
                // hide all views
                mBack.setVisibility(View.INVISIBLE);
                mNext.setVisibility(View.INVISIBLE);
                mChooseRatioView.setVisibility(View.INVISIBLE);
                mChooseFilterView.setVisibility(View.INVISIBLE);
                mSwitchCameraView.setVisibility(View.INVISIBLE);
                mMoreView.setVisibility(View.INVISIBLE);
                mImportView.setVisibility(View.INVISIBLE);
                mThumbListView.setVisibility(View.INVISIBLE);
                mCurrentModeView.setVisibility(View.INVISIBLE);
                mPanelChooseRatio.setVisibility(View.GONE);
                mPanelChooseFilter.setVisibility(View.GONE);
                mPanelMore.setVisibility(View.GONE);

                mCountDownView.setCenterColor(Color.parseColor("#00000000"));
                mCountDownView.setClickable(false);
                mCountStartTime = System.currentTimeMillis();
                mHandler.sendEmptyMessage(MSG_UPDATE_COUNT);
                AVFlowEngine.getInstance().startSave(getOutputFile(), new SaveListener() {
                    @Override
                    public void onSaved(File file) {
                        SimpleModel.getInstance().getSceneList().get(mCurrentScenePos).mVideo = file;
                        mCurrentScenePos++;
                        mListAdapter.refresh(SimpleModel.getInstance().getSceneList());
                    }
                });
                break;
            case R.id.iv_import_video:

                break;
            case R.id.vw_current_mode:

                break;
            // Panel click
            case R.id.vw_ratio_9_16:
                animToHeight(0, 0);
                AVFlowEngine.getInstance().setClipArea(
                        0,
                        0,
                        mScreenHeight,
                        mScreenWidth
                );
                break;
            case R.id.vw_ratio_16_9:
                animHeight = mScreenHeight - (int) (mScreenWidth * 9.0f / 16);
                animToHeight((int) (animHeight * 0.4f), (int) (animHeight * 0.6f));
                AVFlowEngine.getInstance().setClipArea(
                        (int) (animHeight * 0.4f),
                        0,
                        mScreenHeight - (int) (animHeight * 0.6f),
                        mScreenWidth
                );
                break;
            case R.id.vw_ratio_239_1:
                animHeight = mScreenHeight - (int) (mScreenWidth * 5.0f / 12);
                animToHeight((int) (animHeight * 0.4f), (int) (animHeight * 0.6f));
                AVFlowEngine.getInstance().setClipArea(
                        (int) (animHeight * 0.4f),
                        0,
                        mScreenHeight - (int) (animHeight * 0.6f),
                        mScreenWidth
                );
                break;
            case R.id.vw_ratio_3_4:
                animHeight = mScreenHeight - (int) (mScreenWidth * 4.0f / 3);
                animToHeight((int) (animHeight * 0.4f), (int) (animHeight * 0.6f));
                AVFlowEngine.getInstance().setClipArea(
                        (int) (animHeight * 0.4f),
                        0,
                        mScreenHeight - (int) (animHeight * 0.6f),
                        mScreenWidth
                );
                break;
            case R.id.vw_ratio_1_1:
                animHeight = mScreenHeight - mScreenWidth;
                animToHeight((int) (animHeight * 0.4f), (int) (animHeight * 0.6f));
                AVFlowEngine.getInstance().setClipArea(
                        (int) (animHeight * 0.4f),
                        0,
                        mScreenHeight - (int) (animHeight * 0.6f),
                        mScreenWidth
                );
                break;
            case R.id.vw_ratio_circle:

                break;
            case R.id.tv_scene_speed_normal:

                break;
            case R.id.tv_scene_speed_fast:

                break;
            case R.id.tv_scene_speed_slow:

                break;
            case R.id.tv_beauty_none:

                break;
            case R.id.tv_beauty_medium:

                break;
            case R.id.tv_beauty_strong:

                break;
            default:
                break;
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "onSurfaceTextureAvailable: " + width + ", " + height);
        AVFlowEngine.Config config = new AVFlowEngine.Config();
        config.mPreviewSurface = new Surface(surface);
        config.mForceCameraV1 = true;
        config.mSurfaceWidth = width;
        config.mSurfaceHeight = height;
        AVFlowEngine.getInstance().configure(config);
        AVFlowEngine.getInstance().prepare();
        AVFlowEngine.getInstance().startCameraGenerator();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "onSurfaceTextureSizeChanged: " + width + ", " + height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_UPDATE_COUNT:
                long now = System.currentTimeMillis();
                float progress = (now - mCountStartTime) * 1.0f / mDurationOfScene;
                if (progress > 1.0f) {
                    progress = 1.0f;
                }
                String text = String.format(
                        Locale.getDefault(),
                        "%.1fs",
                        (mDurationOfScene - (now - mCountStartTime)) / 1000.0f);
                mCountDownView.setText(text);
                mCountDownView.setProgress(progress);

                if (progress < 1.0f) {
                    mHandler.sendEmptyMessageDelayed(MSG_UPDATE_COUNT, 16);
                } else {
                    AVFlowEngine.getInstance().finishSave();
                    mBack.setVisibility(View.VISIBLE);
                    mNext.setVisibility(View.VISIBLE);
                    mChooseFilterView.setVisibility(View.VISIBLE);
                    mSwitchCameraView.setVisibility(View.VISIBLE);
                    mMoreView.setVisibility(View.VISIBLE);
                    mImportView.setVisibility(View.VISIBLE);
                    mThumbListView.setVisibility(View.VISIBLE);
                    mCurrentModeView.setVisibility(View.VISIBLE);
                    mCountDownView.setText(String.valueOf(mDurationOfScene / 1000f) + "s");
                    mCountDownView.setCenterColor(Color.parseColor("#FF4240"));
                    mCountDownView.setProgress(0f);
                    mCountDownView.setClickable(true);
                }
                break;
            default:
                break;
        }
        return true;
    }

    private File getOutputFile() {
        String fileName = System.currentTimeMillis() + ".mp4";
        return new File(getFilesDir(), fileName);
    }

    private void initPanelChooseRatio() {
        findViewById(R.id.vw_ratio_9_16).setOnClickListener(this);
        findViewById(R.id.vw_ratio_16_9).setOnClickListener(this);
        findViewById(R.id.vw_ratio_239_1).setOnClickListener(this);
        findViewById(R.id.vw_ratio_3_4).setOnClickListener(this);
        findViewById(R.id.vw_ratio_1_1).setOnClickListener(this);
        findViewById(R.id.vw_ratio_circle).setOnClickListener(this);
    }

    private void initPanelChooseFilter() {
        RecyclerView filterListView = findViewById(R.id.rcv_filter_list);
    }

    private void initPanelMore() {
        findViewById(R.id.tv_scene_speed_normal).setOnClickListener(this);
        findViewById(R.id.tv_scene_speed_fast).setOnClickListener(this);
        findViewById(R.id.tv_scene_speed_slow).setOnClickListener(this);
        findViewById(R.id.tv_beauty_none).setOnClickListener(this);
        findViewById(R.id.tv_beauty_medium).setOnClickListener(this);
        findViewById(R.id.tv_beauty_strong).setOnClickListener(this);
    }

    private void animToHeight(int upperHeight, int bottomHeight) {
        Logger.d(TAG, "animToHeight, " + upperHeight + ", " + bottomHeight);
        ValueAnimator upperAnim = ValueAnimator.ofInt(mUpperMask.getMeasuredHeight(), upperHeight);
        upperAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams params = mUpperMask.getLayoutParams();
                params.height = val;
                mUpperMask.setLayoutParams(params);
            }
        });
        upperAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

            }
        });
        upperAnim.setDuration(300);

        ValueAnimator bottomAnim = ValueAnimator.ofInt(mBottomMask.getMeasuredHeight(), bottomHeight);
        bottomAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams params = mBottomMask.getLayoutParams();
                params.height = val;
                mBottomMask.setLayoutParams(params);
            }
        });
        bottomAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

            }
        });
        bottomAnim.setDuration(300);

        bottomAnim.start();
        upperAnim.start();
    }
}
