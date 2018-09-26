package com.createchance.avflow;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.createchance.avflow.model.Filter;
import com.createchance.avflow.model.Scene;
import com.createchance.avflow.model.SimpleModel;
import com.createchance.avflow.utils.AssetsUtil;
import com.createchance.avflow.utils.DensityUtil;
import com.createchance.avflowengine.AVFlowEngine;
import com.createchance.avflowengine.PreviewConfig;
import com.createchance.avflowengine.base.UiThreadUtil;
import com.createchance.avflowengine.generator.VideoPlayListener;

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

    private View mShotPanelTitle, mTextPanelTitle, mStickerPanelTitle, mMusicPanelTitle;
    private FrameLayout mPanelContainer;
    private View mShotPanel, mTextPanel, mStickPanel, mMusicPanel, mFilterPanel, mInfoPanel;

    private FilterListAdapter mFilterListAdapter;
    private RecyclerView mFilterListView;
    private List<Filter> mFilterList;
    private Filter mCurrentFilter;
    private View mFilterInfoView;
    private TextView mFilterCode, mFilterName;
    private Animator mShowAnim, mFadeAnim;
    private SeekBar mFilterAdjustView;

    private RecyclerView mSceneListView;
    private SceneThumbListAdapter mSceneListAdapter;

    private int mCurrentSceneIndex;

    private Surface mPreviewSurface;
    private int mSurfaceWidth, mSurfaceHeight;

    private VideoPlayListener mVideoPlayListener = new VideoPlayListener() {
        @Override
        public void onListPlayStarted() {
            Log.d(TAG, "onListPlayStarted: ");
        }

        @Override
        public void onFilePlayStarted(final int position, final File file) {
            Log.d(TAG, "onFilePlayStarted: " + file);
            UiThreadUtil.post(new Runnable() {
                @Override
                public void run() {
                    Filter filter;
                    if (mCurrentSceneIndex == -1) {
                        filter = SimpleModel.getInstance().getSceneList().get(position).mFilter;
                    } else {
                        filter = SimpleModel.getInstance().getSceneList().get(mCurrentSceneIndex).mFilter;
                    }
                    AVFlowEngine.getInstance().setPreviewFilter(filter.get(VideoEditActivity.this));
                    mFilterInfoView.setVisibility(View.VISIBLE);
                    mFilterCode.setText(filter.mCode);
                    mFilterName.setText(filter.mName);
                    animFilterName();
                    mFilterListAdapter.refreshCurrentFilter(mFilterList.indexOf(SimpleModel.getInstance().getSceneList().get(position).mFilter));
                }
            });
        }

        @Override
        public void onFilePlayGoing(long currentTime, long duration, File file) {

        }

        @Override
        public void onFilePlayDone(int position, File file) {
            Log.d(TAG, "onFilePlayDone: " + file);
        }

        @Override
        public void onListPlayDone() {
            Log.d(TAG, "onListPlayDone: ");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_edit);

        View decorView = getWindow().getDecorView();
        int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(option);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        findViewById(R.id.vw_back).setOnClickListener(this);
        findViewById(R.id.vw_next).setOnClickListener(this);
        findViewById(R.id.tv_play_all).setOnClickListener(this);
        final TextureView preview = findViewById(R.id.vw_previewer);
        preview.setSurfaceTextureListener(this);
        mShotPanelTitle = findViewById(R.id.vw_panel_shot);
        mTextPanelTitle = findViewById(R.id.vw_panel_text);
        mStickerPanelTitle = findViewById(R.id.vw_panel_sticker);
        mMusicPanelTitle = findViewById(R.id.vw_panel_music);
        mShotPanelTitle.setOnClickListener(this);
        mTextPanelTitle.setOnClickListener(this);
        mStickerPanelTitle.setOnClickListener(this);
        mMusicPanelTitle.setOnClickListener(this);
        mPanelContainer = findViewById(R.id.vw_panel_container);
        mFilterInfoView = findViewById(R.id.vw_filter_name);
        mFilterCode = findViewById(R.id.tv_filter_code);
        mFilterName = findViewById(R.id.tv_filter_name);

        initShotPanel();
        initTextPanel();
        initStickerPanel();
        initMusicPanel();
        initFilterPanel();
        initInfoPanel();

        // init scene list
        mSceneListView = findViewById(R.id.rcv_scene_list);
        mSceneListAdapter = new SceneThumbListAdapter(
                this,
                SimpleModel.getInstance().getSceneList(),
                new SceneThumbListAdapter.ClickListener() {
                    @Override
                    public void onClick(Scene scene) {
                        if (mCurrentSceneIndex == -1) {
                            gotoPanel(mShotPanel);
                        }
                        AVFlowEngine.getInstance().reset();
                        List<File> videoList = new ArrayList<>();
                        videoList.add(scene.mVideo);
                        PreviewConfig config = new PreviewConfig.Builder()
                                .surface(mPreviewSurface, mSurfaceWidth, mSurfaceHeight)
                                .dataSource(new PreviewConfig.FileSource(
                                        videoList,
                                        PreviewConfig.ROTATION_180,
                                        true,
                                        1.0f,
                                        mVideoPlayListener))
                                .build();
                        AVFlowEngine.getInstance().startPreview(config);
                        mCurrentSceneIndex = SimpleModel.getInstance().getSceneList().indexOf(scene);
                        mSceneListAdapter.selectOne(mCurrentSceneIndex);
                    }
                });
        mSceneListView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mSceneListView.setAdapter(mSceneListAdapter);

        UiThreadUtil.post(new Runnable() {
            @Override
            public void run() {
                // just take the fist one's width and height.
                int width = SimpleModel.getInstance().getSceneList().get(0).mWidth;
                int height = SimpleModel.getInstance().getSceneList().get(0).mHeight;
                int maxHeight = DensityUtil.dip2px(VideoEditActivity.this, 300);
                float ratio = width * 1.0f / height;
                if (height > maxHeight) {
                    height = maxHeight;
                    width = (int) (height * ratio);
                }
                ViewGroup.LayoutParams layoutParams = preview.getLayoutParams();
                layoutParams.width = width;
                layoutParams.height = height;
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
                AVFlowEngine.getInstance().reset();
                VideoComposeActivity.start(this);
                break;
            case R.id.tv_play_all:
                AVFlowEngine.getInstance().reset();
                List<File> videoList = new ArrayList<>();
                for (Scene scene : SimpleModel.getInstance().getSceneList()) {
                    videoList.add(scene.mVideo);
                }

                PreviewConfig config = new PreviewConfig.Builder()
                        .surface(mPreviewSurface, mSurfaceWidth, mSurfaceHeight)
                        .dataSource(new PreviewConfig.FileSource(
                                videoList,
                                PreviewConfig.ROTATION_180,
                                true,
                                1.0f,
                                mVideoPlayListener))
                        .build();
                AVFlowEngine.getInstance().startPreview(config);

                mSceneListAdapter.selectAll();
                mCurrentSceneIndex = -1;
                ((TextView) mInfoPanel.findViewById(R.id.tv_info_text)).setText(R.string.video_edit_shot_select_one_info);
                gotoPanel(mInfoPanel);
                break;
            case R.id.vw_panel_shot:
                mShotPanelTitle.setBackgroundResource(R.color.theme_dark);
                mTextPanelTitle.setBackgroundResource(R.color.theme_black);
                mStickerPanelTitle.setBackgroundResource(R.color.theme_black);
                mMusicPanelTitle.setBackgroundResource(R.color.theme_black);

                gotoPanel(mShotPanel);
                break;
            case R.id.vw_panel_text:
                mShotPanelTitle.setBackgroundResource(R.color.theme_black);
                mTextPanelTitle.setBackgroundResource(R.color.theme_dark);
                mStickerPanelTitle.setBackgroundResource(R.color.theme_black);
                mMusicPanelTitle.setBackgroundResource(R.color.theme_black);

                gotoPanel(mTextPanel);
                break;
            case R.id.vw_panel_sticker:
                mShotPanelTitle.setBackgroundResource(R.color.theme_black);
                mTextPanelTitle.setBackgroundResource(R.color.theme_black);
                mStickerPanelTitle.setBackgroundResource(R.color.theme_dark);
                mMusicPanelTitle.setBackgroundResource(R.color.theme_black);

                gotoPanel(mStickPanel);
                break;
            case R.id.vw_panel_music:
                mShotPanelTitle.setBackgroundResource(R.color.theme_black);
                mTextPanelTitle.setBackgroundResource(R.color.theme_black);
                mStickerPanelTitle.setBackgroundResource(R.color.theme_black);
                mMusicPanelTitle.setBackgroundResource(R.color.theme_dark);

                gotoPanel(mMusicPanel);
                break;
            case R.id.vw_shot_filter:
                gotoPanel(mFilterPanel);
                break;
            case R.id.iv_filter_panel_back:
                gotoPanel(mShotPanel);
                break;
            default:
                break;
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "onSurfaceTextureAvailable: ");
        mPreviewSurface = new Surface(surface);
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        // start play preview now.
        List<File> playList = new ArrayList<>();
        playList.add(SimpleModel.getInstance().getSceneList().get(mCurrentSceneIndex).mVideo);
        PreviewConfig config = new PreviewConfig.Builder()
                .surface(mPreviewSurface, mSurfaceWidth, mSurfaceHeight)
                .dataSource(new PreviewConfig.FileSource(
                        playList,
                        PreviewConfig.ROTATION_180,
                        true,
                        1.0f,
                        mVideoPlayListener))
                .build();
        AVFlowEngine.getInstance().startPreview(config);
        mSceneListAdapter.selectOne(mCurrentSceneIndex);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mPreviewSurface = null;
        mSurfaceHeight = 0;
        mSurfaceWidth = 0;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private void initShotPanel() {
        mShotPanel = getLayoutInflater().inflate(R.layout.edit_panel_shot, mPanelContainer, false);
        mShotPanel.findViewById(R.id.vw_shot_mute).setOnClickListener(this);
        mShotPanel.findViewById(R.id.vw_shot_adjust).setOnClickListener(this);
        mShotPanel.findViewById(R.id.vw_shot_filter).setOnClickListener(this);
        mShotPanel.findViewById(R.id.vw_shot_beauty).setOnClickListener(this);
        mShotPanel.findViewById(R.id.vw_shot_speed).setOnClickListener(this);
        gotoPanel(mShotPanel);
    }

    private void initTextPanel() {
        mTextPanel = getLayoutInflater().inflate(R.layout.edit_panel_text, mPanelContainer, false);
    }

    private void initStickerPanel() {
        mStickPanel = getLayoutInflater().inflate(R.layout.edit_panel_sticker, mPanelContainer, false);
    }

    private void initMusicPanel() {
        mMusicPanel = getLayoutInflater().inflate(R.layout.edit_panel_music, mPanelContainer, false);
    }

    private void initFilterPanel() {
        mFilterPanel = getLayoutInflater().inflate(R.layout.edit_panel_filter, mPanelContainer, false);
        mFilterPanel.findViewById(R.id.iv_filter_panel_back).setOnClickListener(this);
        mFilterAdjustView = mFilterPanel.findViewById(R.id.sb_filter_adjust);
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
        getFilterList();
        mCurrentFilter = mFilterList.get(0);
        mFilterListView = mFilterPanel.findViewById(R.id.rcv_filter_list);
        mFilterListAdapter = new FilterListAdapter(this, mFilterList, new FilterListAdapter.Callback() {
            @Override
            public void onClick(int position) {
                mCurrentFilter = mFilterList.get(position);
                SimpleModel.getInstance().getSceneList().get(mCurrentSceneIndex).mFilter = mCurrentFilter;
                AVFlowEngine.getInstance().setPreviewFilter(mCurrentFilter.get(VideoEditActivity.this));
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
        }, mFilterList.indexOf(SimpleModel.getInstance().getSceneList().get(mCurrentSceneIndex).mFilter));
        mFilterListView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mFilterListView.setAdapter(mFilterListAdapter);
    }

    private void initInfoPanel() {
        mInfoPanel = getLayoutInflater().inflate(R.layout.edit_panel_info, mPanelContainer, false);
    }

    private void gotoPanel(View panelView) {
        mPanelContainer.removeAllViews();
        mPanelContainer.addView(panelView);
    }

    private void getFilterList() {
        mFilterList = AssetsUtil.parseJsonToList(this, "filter_list.json", Filter.class);
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
}
