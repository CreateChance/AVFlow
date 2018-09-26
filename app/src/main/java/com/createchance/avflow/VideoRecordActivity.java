package com.createchance.avflow;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.SeekBar;
import android.widget.TextView;

import com.createchance.avflow.model.Filter;
import com.createchance.avflow.model.Scene;
import com.createchance.avflow.model.SimpleModel;
import com.createchance.avflow.utils.AssetsUtil;
import com.createchance.avflow.utils.DensityUtil;
import com.createchance.avflowengine.AVFlowEngine;
import com.createchance.avflowengine.base.Logger;
import com.createchance.avflowengine.config.CameraInputConfig;
import com.createchance.avflowengine.config.PreviewOutputConfig;
import com.createchance.avflowengine.config.SaveOutputConfig;
import com.createchance.avflowengine.generator.CameraImpl;
import com.createchance.avflowengine.saver.SaveListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Video record activity.
 *
 * @author createchance
 * @date 2018-08-29
 */
public class VideoRecordActivity extends AppCompatActivity implements
        View.OnClickListener,
        TextureView.SurfaceTextureListener,
        Handler.Callback {

    private static final String TAG = "VideoRecordActivity";

    private final int MSG_UPDATE_COUNT = 100;

    private TextureView mPreview;
    private RecyclerView mThumbListView;
    private SceneThumbListAdapter mListAdapter;
    private ImageView
            mChooseRatioView,
            mChooseFilterView,
            mSwitchCameraView,
            mMoreView,
            mImportView;
    private View mBack, mNext;
    private int mCurrentSceneIndex;
    private View mCurrentModeView;
    private RoundProgressbar mCountDownView;
    private View mPanelChooseRatio, mPanelChooseFilter, mPanelMore;
    private SeekBar mFilterAdjustView;
    private View mUpperMask, mBottomMask;
    private View mFilterInfoView;
    private TextView mFilterCode, mFilterName;
    private Animator mShowAnim, mFadeAnim;

    private FilterListAdapter mFilterListAdapter;

    private Handler mHandler;

    // scenes
    private long mDurationOfScene = 5 * 1000;
    private long mCountStartTime;
    private boolean mIsRecording;

    private List<Filter> mFilterList;
    private Filter mCurrentFilter;

    private List<Scene> mSceneList = new ArrayList<>();

    private int mScreenWidth, mScreenHeight;
    private int mSceneWidth, mSceneHeight;

    private int mClipTop, mClipLeft, mClipBottom, mClipRight;

    private String mEngineToken;

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
        setClipArea(
                0,
                0,
                mScreenHeight,
                mScreenWidth
        );
        mSceneWidth = mScreenWidth;
        mSceneHeight = mScreenHeight;

        mHandler = new Handler(this);

        mBack = findViewById(R.id.vw_back);
        mNext = findViewById(R.id.vw_next);
        mUpperMask = findViewById(R.id.vw_upper_mask);
        mBottomMask = findViewById(R.id.vw_bottom_mask);
        mPreview = findViewById(R.id.vw_previewer);
        mPreview.setSurfaceTextureListener(this);
        mThumbListView = findViewById(R.id.rcv_scene_thumb_list);
        // default four scenes.
        mSceneList.add(new Scene());
        mSceneList.add(new Scene());
        mSceneList.add(new Scene());
        mSceneList.add(new Scene());
        mListAdapter = new SceneThumbListAdapter(this, mSceneList);
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
        mFilterInfoView = findViewById(R.id.vw_filter_name);
        mFilterCode = findViewById(R.id.tv_filter_code);
        mFilterName = findViewById(R.id.tv_filter_name);
        mFilterAdjustView = findViewById(R.id.sb_filter_adjust);
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
        if (!mCurrentFilter.canAdjust()) {
            mFilterAdjustView.setEnabled(false);
        }
        mFilterAdjustView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mCurrentFilter.adjust(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
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

        SimpleModel.getInstance().getSceneList().clear();

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
            case R.id.vw_back:
                onBackPressed();
                break;
            case R.id.vw_next:
                // goto video edit activity
                for (Scene scene : mSceneList) {
                    if (scene.mVideo != null) {
                        SimpleModel.getInstance().addScene(scene);
                    }
                }
                VideoEditActivity.start(this);
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
                if (AVFlowEngine.getInstance().getCamera(mEngineToken).getFacing() == CameraImpl.FACING_BACK) {
                    AVFlowEngine.getInstance().getCamera(mEngineToken).setFacing(CameraImpl.FACING_FRONT);
                } else {
                    AVFlowEngine.getInstance().getCamera(mEngineToken).setFacing(CameraImpl.FACING_BACK);
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
                break;
            case R.id.iv_import_video:

                break;
            case R.id.vw_current_mode:

                break;
            // Panel click
            case R.id.vw_ratio_9_16:
                animMaskToHeight(0, 0);
                setClipArea(
                        0,
                        0,
                        mScreenHeight,
                        mScreenWidth
                );
                mFilterInfoView.setTranslationY(0);
                ((TextView) findViewById(R.id.tv_ratio_9_16)).setTextColor(getResources().getColor(R.color.font_red));
                ((TextView) findViewById(R.id.tv_ratio_16_9)).setTextColor(getResources().getColor(R.color.font_grey));
                ((TextView) findViewById(R.id.tv_ratio_239_1)).setTextColor(getResources().getColor(R.color.font_grey));
                ((TextView) findViewById(R.id.tv_ratio_1_1)).setTextColor(getResources().getColor(R.color.font_grey));
                ((TextView) findViewById(R.id.tv_ratio_3_4)).setTextColor(getResources().getColor(R.color.font_grey));
                ((TextView) findViewById(R.id.tv_ratio_circle)).setTextColor(getResources().getColor(R.color.font_grey));

                mSceneHeight = mScreenHeight;
                break;
            case R.id.vw_ratio_16_9:
                animHeight = mScreenHeight - (int) (mScreenWidth * 9.0f / 16);
                animMaskToHeight((int) (animHeight * 0.4f), (int) (animHeight * 0.6f));
                setClipArea(
                        (int) (animHeight * 0.4f),
                        0,
                        mScreenHeight - (int) (animHeight * 0.6f),
                        mScreenWidth
                );
                mFilterInfoView.setTranslationY(
                        (int) (animHeight * 0.4f) + (int) (mScreenWidth * 9.0f / 16) / 2 - mScreenHeight / 2);
                ((TextView) findViewById(R.id.tv_ratio_9_16)).setTextColor(getResources().getColor(R.color.font_grey));
                ((TextView) findViewById(R.id.tv_ratio_16_9)).setTextColor(getResources().getColor(R.color.font_red));
                ((TextView) findViewById(R.id.tv_ratio_239_1)).setTextColor(getResources().getColor(R.color.font_grey));
                ((TextView) findViewById(R.id.tv_ratio_1_1)).setTextColor(getResources().getColor(R.color.font_grey));
                ((TextView) findViewById(R.id.tv_ratio_3_4)).setTextColor(getResources().getColor(R.color.font_grey));
                ((TextView) findViewById(R.id.tv_ratio_circle)).setTextColor(getResources().getColor(R.color.font_grey));

                mSceneHeight = (int) (mScreenWidth * 9.0f / 16);
                break;
            case R.id.vw_ratio_239_1:
                animHeight = mScreenHeight - (int) (mScreenWidth * 5.0f / 12);
                animMaskToHeight((int) (animHeight * 0.4f), (int) (animHeight * 0.6f));
                setClipArea(
                        (int) (animHeight * 0.4f),
                        0,
                        mScreenHeight - (int) (animHeight * 0.6f),
                        mScreenWidth
                );
                mFilterInfoView.setTranslationY(
                        (int) (animHeight * 0.4f) + (int) (mScreenWidth * 5.0f / 12) / 2 - mScreenHeight / 2);
                ((TextView) findViewById(R.id.tv_ratio_9_16)).setTextColor(getResources().getColor(R.color.font_grey));
                ((TextView) findViewById(R.id.tv_ratio_16_9)).setTextColor(getResources().getColor(R.color.font_grey));
                ((TextView) findViewById(R.id.tv_ratio_239_1)).setTextColor(getResources().getColor(R.color.font_red));
                ((TextView) findViewById(R.id.tv_ratio_1_1)).setTextColor(getResources().getColor(R.color.font_grey));
                ((TextView) findViewById(R.id.tv_ratio_3_4)).setTextColor(getResources().getColor(R.color.font_grey));
                ((TextView) findViewById(R.id.tv_ratio_circle)).setTextColor(getResources().getColor(R.color.font_grey));

                mSceneHeight = (int) (mScreenWidth * 5.0f / 12);
                break;
            case R.id.vw_ratio_3_4:
                animHeight = mScreenHeight - (int) (mScreenWidth * 4.0f / 3);
                animMaskToHeight((int) (animHeight * 0.4f), (int) (animHeight * 0.6f));
                setClipArea(
                        (int) (animHeight * 0.4f),
                        0,
                        mScreenHeight - (int) (animHeight * 0.6f),
                        mScreenWidth
                );
                mFilterInfoView.setTranslationY(
                        (int) (animHeight * 0.4f) + (int) (mScreenWidth * 4.0f / 3) / 2 - mScreenHeight / 2);
                ((TextView) findViewById(R.id.tv_ratio_9_16)).setTextColor(getResources().getColor(R.color.font_grey));
                ((TextView) findViewById(R.id.tv_ratio_16_9)).setTextColor(getResources().getColor(R.color.font_grey));
                ((TextView) findViewById(R.id.tv_ratio_239_1)).setTextColor(getResources().getColor(R.color.font_grey));
                ((TextView) findViewById(R.id.tv_ratio_1_1)).setTextColor(getResources().getColor(R.color.font_grey));
                ((TextView) findViewById(R.id.tv_ratio_3_4)).setTextColor(getResources().getColor(R.color.font_red));
                ((TextView) findViewById(R.id.tv_ratio_circle)).setTextColor(getResources().getColor(R.color.font_grey));

                mSceneHeight = (int) (mScreenWidth * 4.0f / 3);
                break;
            case R.id.vw_ratio_1_1:
                animHeight = mScreenHeight - mScreenWidth;
                animMaskToHeight((int) (animHeight * 0.4f), (int) (animHeight * 0.6f));
                setClipArea(
                        (int) (animHeight * 0.4f),
                        0,
                        mScreenHeight - (int) (animHeight * 0.6f),
                        mScreenWidth
                );
                mFilterInfoView.setTranslationY(
                        (int) (animHeight * 0.4f) + mScreenWidth / 2 - mScreenHeight / 2);
                ((TextView) findViewById(R.id.tv_ratio_9_16)).setTextColor(getResources().getColor(R.color.font_grey));
                ((TextView) findViewById(R.id.tv_ratio_16_9)).setTextColor(getResources().getColor(R.color.font_grey));
                ((TextView) findViewById(R.id.tv_ratio_239_1)).setTextColor(getResources().getColor(R.color.font_grey));
                ((TextView) findViewById(R.id.tv_ratio_1_1)).setTextColor(getResources().getColor(R.color.font_red));
                ((TextView) findViewById(R.id.tv_ratio_3_4)).setTextColor(getResources().getColor(R.color.font_grey));
                ((TextView) findViewById(R.id.tv_ratio_circle)).setTextColor(getResources().getColor(R.color.font_grey));

                mSceneHeight = mScreenWidth;
                break;
            case R.id.vw_ratio_circle:

                ((TextView) findViewById(R.id.tv_ratio_9_16)).setTextColor(getResources().getColor(R.color.font_grey));
                ((TextView) findViewById(R.id.tv_ratio_16_9)).setTextColor(getResources().getColor(R.color.font_grey));
                ((TextView) findViewById(R.id.tv_ratio_239_1)).setTextColor(getResources().getColor(R.color.font_grey));
                ((TextView) findViewById(R.id.tv_ratio_1_1)).setTextColor(getResources().getColor(R.color.font_grey));
                ((TextView) findViewById(R.id.tv_ratio_3_4)).setTextColor(getResources().getColor(R.color.font_grey));
                ((TextView) findViewById(R.id.tv_ratio_circle)).setTextColor(getResources().getColor(R.color.font_red));

                mSceneHeight = mScreenWidth;
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
        mEngineToken = AVFlowEngine.getInstance().newWorker();

        CameraInputConfig inputConfig = new CameraInputConfig.Builder()
                .facing(CameraInputConfig.FACING_BACK)
                .forceCameraV1(true)
                .rotation(CameraInputConfig.ROTATION_270)
                .surfaceSize(width, height)
                .build();
        PreviewOutputConfig previewOutputConfig = new PreviewOutputConfig.Builder()
                .surface(new Surface(surface), width, height)
                .build();
        AVFlowEngine.getInstance().configInput(mEngineToken, inputConfig);
        AVFlowEngine.getInstance().configOutput(mEngineToken, previewOutputConfig);
        AVFlowEngine.getInstance().start(mEngineToken);

        // show filter info.
        mFilterInfoView.setVisibility(View.VISIBLE);
        mFilterCode.setText(mCurrentFilter.mCode);
        mFilterName.setText(mCurrentFilter.mName);
        animFilterName();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "onSurfaceTextureSizeChanged: " + width + ", " + height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.d(TAG, "onSurfaceTextureDestroyed: ");
        AVFlowEngine.getInstance().stop(mEngineToken);
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_UPDATE_COUNT:
                if (!mIsRecording) {
                    mIsRecording = true;
                    SaveOutputConfig outputConfig = new SaveOutputConfig.Builder()
                            .clipArea(mClipTop, mClipLeft, mClipBottom, mClipRight)
                            .frameRate(30)
                            .rotation(SaveOutputConfig.ROTATION_0)
                            .listener(new SaveListener() {
                                @Override
                                public void onSaved(File file) {
                                    Scene scene = new Scene();
                                    scene.mVideo = file;
                                    scene.mFilter = mCurrentFilter;
                                    scene.mSpeedRate = 1.0f;
                                    scene.mWidth = mSceneWidth;
                                    scene.mHeight = mSceneHeight;
                                    mSceneList.remove(mCurrentSceneIndex);
                                    mSceneList.add(mCurrentSceneIndex, scene);
                                    mCurrentSceneIndex++;
                                    mListAdapter.refresh(mSceneList);
                                    if (mCurrentSceneIndex == mSceneList.size()) {
                                        SimpleModel.getInstance().setSceneList(mSceneList);
                                        VideoEditActivity.start(VideoRecordActivity.this);
                                    }
                                }
                            })
                            .build();
                    AVFlowEngine.getInstance().configOutput(mEngineToken, outputConfig);
                    AVFlowEngine.getInstance().startSave(mEngineToken, getOutputFile());
                }
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
                    AVFlowEngine.getInstance().finishSave(mEngineToken);
                    mIsRecording = false;
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
        String fileName = "avflow/" + System.currentTimeMillis() + "_scene.mp4";
        return new File(Environment.getExternalStorageDirectory(), fileName);
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
        getFilterList();
        mCurrentFilter = mFilterList.get(0);
        RecyclerView filterListView = findViewById(R.id.rcv_filter_list);
        mFilterListAdapter = new FilterListAdapter(this, mFilterList, new FilterListAdapter.Callback() {
            @Override
            public void onClick(int position) {
                mCurrentFilter = mFilterList.get(position);
                AVFlowEngine.getInstance().setPreviewFilter(mEngineToken, mCurrentFilter.get(VideoRecordActivity.this));
                mFilterInfoView.setVisibility(View.VISIBLE);
                mFilterCode.setText(mCurrentFilter.mCode);
                mFilterName.setText(mCurrentFilter.mName);
                animFilterName();

                if (mCurrentFilter.canAdjust()) {
                    mFilterAdjustView.setEnabled(true);
                    mFilterAdjustView.setProgress(0);
                } else {
                    mFilterAdjustView.setEnabled(false);
                    mFilterAdjustView.setProgress(0);
                }
            }
        }, 0);
        filterListView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        filterListView.setAdapter(mFilterListAdapter);
    }

    private void initPanelMore() {
        findViewById(R.id.tv_scene_speed_normal).setOnClickListener(this);
        findViewById(R.id.tv_scene_speed_fast).setOnClickListener(this);
        findViewById(R.id.tv_scene_speed_slow).setOnClickListener(this);
        findViewById(R.id.tv_beauty_none).setOnClickListener(this);
        findViewById(R.id.tv_beauty_medium).setOnClickListener(this);
        findViewById(R.id.tv_beauty_strong).setOnClickListener(this);
    }

    private void animMaskToHeight(int upperHeight, int bottomHeight) {
        Logger.d(TAG, "animMaskToHeight, " + upperHeight + ", " + bottomHeight);
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

    private void animFilterName() {
        if (mShowAnim != null) {
            mShowAnim.end();
        }
        if (mFadeAnim != null) {
            mFadeAnim.cancel();
        }

        ObjectAnimator translate = ObjectAnimator.ofFloat(mFilterName, "translationY", -50, 0);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(mFilterName, "alpha", 0f, 1f);
        ObjectAnimator fade = ObjectAnimator.ofFloat(mFilterInfoView, "alpha", 1f, 0f);
        fade.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mFilterInfoView.setVisibility(View.INVISIBLE);
                mFilterInfoView.setAlpha(1f);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                mFilterInfoView.setAlpha(1f);
            }
        });
        fade.setDuration(500);

        AnimatorSet showSet = new AnimatorSet();
        showSet.playTogether(translate, alpha);
        showSet.setDuration(500);
        showSet.start();

        AnimatorSet playSet = new AnimatorSet();
        playSet.play(fade).after(1500);
        playSet.start();
        mShowAnim = showSet;
        mFadeAnim = playSet;
    }

    private void getFilterList() {
        mFilterList = AssetsUtil.parseJsonToList(this, "filter_list.json", Filter.class);
    }

    private void setClipArea(int clipTop, int clipLeft, int clipBottom, int clipRight) {
        mClipTop = clipTop;
        mClipLeft = clipLeft;
        mClipBottom = clipBottom;
        mClipRight = clipRight;
    }
}
