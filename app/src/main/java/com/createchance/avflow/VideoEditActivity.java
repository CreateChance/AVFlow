package com.createchance.avflow;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import com.createchance.avflowengine.base.UiThreadUtil;
import com.createchance.avflowengine.config.FileInputConfig;
import com.createchance.avflowengine.config.PreviewOutputConfig;
import com.createchance.avflowengine.generator.FilePlayListener;

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
    private View mShotPanel, mTextPanel, mStickPanel, mMusicPanel, mFilterPanel, mInfoPanel, mTextTitlePanel;

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

    private List<File> mStickersPathList;

    private int mSurfaceWidth, mSurfaceHeight;
    private float mSurfaceScaleFactor = 1.0f;

    private String mEngineToken;

    private FilePlayListener mVideoPlayListener = new FilePlayListener() {
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
                    Scene scene;
                    if (mCurrentSceneIndex == -1) {
                        scene = SimpleModel.getInstance().getSceneList().get(position);
                    } else {
                        scene = SimpleModel.getInstance().getSceneList().get(mCurrentSceneIndex);
                    }
                    filter = scene.mFilter;
                    AVFlowEngine.getInstance().setPreviewFilter(mEngineToken, filter.get(VideoEditActivity.this));
                    mFilterInfoView.setVisibility(View.VISIBLE);
                    mFilterCode.setText(filter.mCode);
                    mFilterName.setText(filter.mName);
                    animFilterName();
                    mFilterListAdapter.refreshCurrentFilter(mFilterList.indexOf(SimpleModel.getInstance().getSceneList().get(position).mFilter));

                    if (scene.mText != null && !TextUtils.isEmpty(scene.mText.mValue)) {
                        AVFlowEngine.getInstance().setPreviewText(mEngineToken,
                                scene.mText.mFontPath,
                                scene.mText.mValue,
                                (int) (scene.mText.mPosX * mSurfaceScaleFactor),
                                (int) (scene.mText.mPosY * mSurfaceScaleFactor),
                                (int) (scene.mText.mTextSize * mSurfaceScaleFactor),
                                scene.mText.mRed,
                                scene.mText.mGreen,
                                scene.mText.mBlue,
                                scene.mText.mBackground);
                    } else {
                        AVFlowEngine.getInstance().removePreviewText(mEngineToken);
                    }

                    if (scene.mStickerList != null) {
                        AVFlowEngine.getInstance().setPreviewImage(mEngineToken,
                                scene.mStickerList.mValue,
                                scene.mStickerList.mPosX,
                                scene.mStickerList.mPosY,
                                scene.mStickerList.mScaleFactor);
                    } else {
                        AVFlowEngine.getInstance().removePreviewImage(mEngineToken);
                    }
                }
            });
        }

        @Override
        public void onFilePlayGoing(long currentTime, long duration, final long globalTime, final long totalDuration, File file) {

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
        initTextTitlePanel();

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

                        mCurrentSceneIndex = SimpleModel.getInstance().getSceneList().indexOf(scene);
                        mSceneListAdapter.selectOne(mCurrentSceneIndex);

                        replayScene(scene);
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
                int screenHeight = VideoEditActivity.this.getWindowManager().getDefaultDisplay().getHeight();
                int maxHeight = (int) ((screenHeight - DensityUtil.dip2px(VideoEditActivity.this, 48)) * 0.6f);
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
    public void onClick(View v) {
        Scene scene;
        List<String> stickerList = null;
        Bitmap sticker;
        int stickerWidth, stickerHeight;
        switch (v.getId()) {
            case R.id.vw_back:
                onBackPressed();
                break;
            case R.id.vw_next:
                VideoComposeActivity.start(this);
                break;
            case R.id.tv_play_all:
                FileInputConfig.Builder inputConfigBuilder = new FileInputConfig.Builder();
                for (Scene s : SimpleModel.getInstance().getSceneList()) {
                    inputConfigBuilder.addFile(s.mVideo);
                }

                inputConfigBuilder.loop(true)
                        .rotation(FileInputConfig.ROTATION_180)
                        .speedRate(FileInputConfig.SPEED_RATE_NORMAL)
                        .surfaceSize(mSurfaceWidth, mSurfaceHeight)
                        .listener(mVideoPlayListener);

                AVFlowEngine.getInstance().configInput(mEngineToken, inputConfigBuilder.build());

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

                gotoPanel(mTextTitlePanel);
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
            case R.id.vw_text_title:
                gotoPanel(mTextTitlePanel);
                break;
            case R.id.vw_text_caption:

                break;
            case R.id.vw_text_label:

                break;
            case R.id.btn_set_title:
                SetTextDialog.start(this, new SetTextDialog.OnClickListener() {
                    @Override
                    public void onConfirm(String text) {
                        // update params of scene.
                        String fontPath = new File(getFilesDir(), "fonts/MFYanSong-Regular.ttf").getAbsolutePath();
                        Scene scene = SimpleModel.getInstance().getSceneList().get(mCurrentSceneIndex);
                        scene.mText = new Scene.Text();
                        scene.mText.mFontPath = fontPath;
                        scene.mText.mValue = text;
                        scene.mText.mBackground = null;
                        scene.mText.mPosX = 0;
                        scene.mText.mPosY = (int) (mSurfaceHeight / (2 * mSurfaceScaleFactor));

                        AVFlowEngine.getInstance().setPreviewText(mEngineToken,
                                scene.mText.mFontPath,
                                scene.mText.mValue,
                                scene.mText.mPosX,
                                mSurfaceHeight / 2,
                                (int) (scene.mText.mTextSize * mSurfaceScaleFactor),
                                scene.mText.mRed,
                                scene.mText.mGreen,
                                scene.mText.mBlue,
                                scene.mText.mBackground);

                        SeekBar changePosX = mTextTitlePanel.findViewById(R.id.sb_change_font_posx);
                        SeekBar changePosY = mTextTitlePanel.findViewById(R.id.sb_change_font_posy);
                        changePosX.setProgress(scene.mText.mPosX);
                        changePosY.setProgress(mSurfaceHeight / 2);
                    }

                    @Override
                    public void onCancel() {

                    }
                });
                break;
            case R.id.btn_delete_title:
                scene = SimpleModel.getInstance().getSceneList().get(mCurrentSceneIndex);
                if (scene.mText != null) {
                    AVFlowEngine.getInstance().removePreviewText(mEngineToken);
                    scene.mText = null;
                }
                break;
            case R.id.tv_text_bg_1:
                scene = SimpleModel.getInstance().getSceneList().get(mCurrentSceneIndex);
                if (scene.mText == null) {
                    return;
                }

                scene.mText.mBackground = BitmapFactory.decodeResource(getResources(), R.drawable.icon_text_bg_1);
                scene.mText.mRed = 0;
                scene.mText.mGreen = 0;
                scene.mText.mBlue = 0;

                AVFlowEngine.getInstance().setPreviewText(mEngineToken,
                        scene.mText.mFontPath,
                        scene.mText.mValue,
                        (int) (scene.mText.mPosX * mSurfaceScaleFactor),
                        (int) (scene.mText.mPosY * mSurfaceScaleFactor),
                        (int) (scene.mText.mTextSize * mSurfaceScaleFactor),
                        scene.mText.mRed,
                        scene.mText.mGreen,
                        scene.mText.mBlue,
                        scene.mText.mBackground);
                break;
            case R.id.tv_text_bg_2:
                scene = SimpleModel.getInstance().getSceneList().get(mCurrentSceneIndex);
                if (scene.mText == null) {
                    return;
                }

                scene.mText.mBackground = BitmapFactory.decodeResource(getResources(), R.drawable.icon_text_bg_2);
                scene.mText.mRed = 1f;
                scene.mText.mGreen = 1f;
                scene.mText.mBlue = 1f;

                AVFlowEngine.getInstance().setPreviewText(mEngineToken,
                        scene.mText.mFontPath,
                        scene.mText.mValue,
                        (int) (scene.mText.mPosX * mSurfaceScaleFactor),
                        (int) (scene.mText.mPosY * mSurfaceScaleFactor),
                        (int) (scene.mText.mTextSize * mSurfaceScaleFactor),
                        scene.mText.mRed,
                        scene.mText.mGreen,
                        scene.mText.mBlue,
                        scene.mText.mBackground);
                break;
            case R.id.tv_text_bg_3:
                scene = SimpleModel.getInstance().getSceneList().get(mCurrentSceneIndex);
                if (scene.mText == null) {
                    return;
                }

                scene.mText.mBackground = BitmapFactory.decodeResource(getResources(), R.drawable.icon_text_bg_3);
                scene.mText.mRed = 1f;
                scene.mText.mGreen = 1f;
                scene.mText.mBlue = 1f;

                AVFlowEngine.getInstance().setPreviewText(mEngineToken,
                        scene.mText.mFontPath,
                        scene.mText.mValue,
                        (int) (scene.mText.mPosX * mSurfaceScaleFactor),
                        (int) (scene.mText.mPosY * mSurfaceScaleFactor),
                        (int) (scene.mText.mTextSize * mSurfaceScaleFactor),
                        scene.mText.mRed,
                        scene.mText.mGreen,
                        scene.mText.mBlue,
                        scene.mText.mBackground);
                break;
            case R.id.tv_text_bg_4:
                scene = SimpleModel.getInstance().getSceneList().get(mCurrentSceneIndex);
                if (scene.mText == null) {
                    return;
                }

                scene.mText.mBackground = BitmapFactory.decodeResource(getResources(), R.drawable.icon_text_bg_4);
                scene.mText.mRed = 0;
                scene.mText.mGreen = 0;
                scene.mText.mBlue = 0;

                AVFlowEngine.getInstance().setPreviewText(mEngineToken,
                        scene.mText.mFontPath,
                        scene.mText.mValue,
                        (int) (scene.mText.mPosX * mSurfaceScaleFactor),
                        (int) (scene.mText.mPosY * mSurfaceScaleFactor),
                        (int) (scene.mText.mTextSize * mSurfaceScaleFactor),
                        scene.mText.mRed,
                        scene.mText.mGreen,
                        scene.mText.mBlue,
                        scene.mText.mBackground);
                break;
            case R.id.tv_text_font_1:
                scene = SimpleModel.getInstance().getSceneList().get(mCurrentSceneIndex);
                if (scene.mText == null) {
                    return;
                }

                scene.mText.mFontPath = new File(getFilesDir(), "fonts/KaBuQiNuo.otf").getAbsolutePath();
                AVFlowEngine.getInstance().setPreviewText(mEngineToken,
                        scene.mText.mFontPath,
                        scene.mText.mValue,
                        (int) (scene.mText.mPosX * mSurfaceScaleFactor),
                        (int) (scene.mText.mPosY * mSurfaceScaleFactor),
                        (int) (scene.mText.mTextSize * mSurfaceScaleFactor),
                        scene.mText.mRed,
                        scene.mText.mGreen,
                        scene.mText.mBlue,
                        scene.mText.mBackground);
                break;
            case R.id.tv_text_font_2:
                scene = SimpleModel.getInstance().getSceneList().get(mCurrentSceneIndex);
                if (scene.mText == null) {
                    return;
                }

                scene.mText.mFontPath = new File(getFilesDir(), "fonts/MFYanSong-Regular.ttf").getAbsolutePath();
                AVFlowEngine.getInstance().setPreviewText(mEngineToken,
                        scene.mText.mFontPath,
                        scene.mText.mValue,
                        (int) (scene.mText.mPosX * mSurfaceScaleFactor),
                        (int) (scene.mText.mPosY * mSurfaceScaleFactor),
                        (int) (scene.mText.mTextSize * mSurfaceScaleFactor),
                        scene.mText.mRed,
                        scene.mText.mGreen,
                        scene.mText.mBlue,
                        scene.mText.mBackground);
                break;
            case R.id.tv_text_font_3:
                scene = SimpleModel.getInstance().getSceneList().get(mCurrentSceneIndex);
                if (scene.mText == null) {
                    return;
                }

                scene.mText.mFontPath = new File(getFilesDir(), "fonts/SentyWEN2017.ttf").getAbsolutePath();
                AVFlowEngine.getInstance().setPreviewText(mEngineToken,
                        scene.mText.mFontPath,
                        scene.mText.mValue,
                        (int) (scene.mText.mPosX * mSurfaceScaleFactor),
                        (int) (scene.mText.mPosY * mSurfaceScaleFactor),
                        (int) (scene.mText.mTextSize * mSurfaceScaleFactor),
                        scene.mText.mRed,
                        scene.mText.mGreen,
                        scene.mText.mBlue,
                        scene.mText.mBackground);
                break;
            case R.id.tv_text_font_4:
                scene = SimpleModel.getInstance().getSceneList().get(mCurrentSceneIndex);
                if (scene.mText == null) {
                    return;
                }

                scene.mText.mFontPath = new File(getFilesDir(), "fonts/YouLangRuanBi.ttf").getAbsolutePath();
                AVFlowEngine.getInstance().setPreviewText(mEngineToken,
                        scene.mText.mFontPath,
                        scene.mText.mValue,
                        (int) (scene.mText.mPosX * mSurfaceScaleFactor),
                        (int) (scene.mText.mPosY * mSurfaceScaleFactor),
                        (int) (scene.mText.mTextSize * mSurfaceScaleFactor),
                        scene.mText.mRed,
                        scene.mText.mGreen,
                        scene.mText.mBlue,
                        scene.mText.mBackground);
                break;
            case R.id.iv_set_text_panel_back:
                gotoPanel(mTextPanel);
                break;
            case R.id.iv_sticker_1:
                stickerList = getStickerList(mStickersPathList.get(0));
                sticker = BitmapFactory.decodeFile(stickerList.get(0));
                stickerWidth = sticker.getWidth();
                stickerHeight = sticker.getHeight();
                sticker.recycle();
                scene = SimpleModel.getInstance().getSceneList().get(mCurrentSceneIndex);
                scene.mStickerList = new Scene.StickerList();
                scene.mStickerList.mValue = stickerList;
                scene.mStickerList.mPosX = (mSurfaceWidth - stickerWidth) / 2;
                scene.mStickerList.mPosY = (mSurfaceHeight - stickerHeight) / 2;
                scene.mStickerList.mScaleFactor = 1.0f;
                AVFlowEngine.getInstance().removePreviewImage(mEngineToken);
                replayScene(scene);
                break;
            case R.id.iv_sticker_2:
                stickerList = getStickerList(mStickersPathList.get(1));
                sticker = BitmapFactory.decodeFile(stickerList.get(0));
                stickerWidth = sticker.getWidth();
                stickerHeight = sticker.getHeight();
                sticker.recycle();
                scene = SimpleModel.getInstance().getSceneList().get(mCurrentSceneIndex);
                scene.mStickerList = new Scene.StickerList();
                scene.mStickerList.mValue = stickerList;
                scene.mStickerList.mPosX = (mSurfaceWidth - stickerWidth) / 2;
                scene.mStickerList.mPosY = (mSurfaceHeight - stickerHeight) / 2;
                scene.mStickerList.mScaleFactor = 1.0f;
                AVFlowEngine.getInstance().removePreviewImage(mEngineToken);
                replayScene(scene);
                break;
            case R.id.iv_sticker_3:
                stickerList = getStickerList(mStickersPathList.get(2));
                sticker = BitmapFactory.decodeFile(stickerList.get(0));
                stickerWidth = sticker.getWidth();
                stickerHeight = sticker.getHeight();
                sticker.recycle();
                scene = SimpleModel.getInstance().getSceneList().get(mCurrentSceneIndex);
                scene.mStickerList = new Scene.StickerList();
                scene.mStickerList.mValue = stickerList;
                scene.mStickerList.mPosX = (mSurfaceWidth - stickerWidth) / 2;
                scene.mStickerList.mPosY = (mSurfaceHeight - stickerHeight) / 2;
                scene.mStickerList.mScaleFactor = 1.0f;
                AVFlowEngine.getInstance().removePreviewImage(mEngineToken);
                replayScene(scene);
                break;
            case R.id.iv_sticker_4:
                stickerList = getStickerList(mStickersPathList.get(3));
                sticker = BitmapFactory.decodeFile(stickerList.get(0));
                stickerWidth = sticker.getWidth();
                stickerHeight = sticker.getHeight();
                sticker.recycle();
                scene = SimpleModel.getInstance().getSceneList().get(mCurrentSceneIndex);
                scene.mStickerList = new Scene.StickerList();
                scene.mStickerList.mValue = stickerList;
                scene.mStickerList.mPosX = (mSurfaceWidth - stickerWidth) / 2;
                scene.mStickerList.mPosY = (mSurfaceHeight - stickerHeight) / 2;
                scene.mStickerList.mScaleFactor = 1.0f;
                AVFlowEngine.getInstance().removePreviewImage(mEngineToken);
                replayScene(scene);
                break;
            case R.id.iv_sticker_5:
                stickerList = getStickerList(mStickersPathList.get(4));
                sticker = BitmapFactory.decodeFile(stickerList.get(0));
                stickerWidth = sticker.getWidth();
                stickerHeight = sticker.getHeight();
                sticker.recycle();
                scene = SimpleModel.getInstance().getSceneList().get(mCurrentSceneIndex);
                scene.mStickerList = new Scene.StickerList();
                scene.mStickerList.mValue = stickerList;
                scene.mStickerList.mPosX = (mSurfaceWidth - stickerWidth) / 2;
                scene.mStickerList.mPosY = (mSurfaceHeight - stickerHeight) / 2;
                scene.mStickerList.mScaleFactor = 1.0f;
                AVFlowEngine.getInstance().removePreviewImage(mEngineToken);
                replayScene(scene);
                break;
            case R.id.iv_sticker_6:
                stickerList = getStickerList(mStickersPathList.get(5));
                sticker = BitmapFactory.decodeFile(stickerList.get(0));
                stickerWidth = sticker.getWidth();
                stickerHeight = sticker.getHeight();
                sticker.recycle();
                scene = SimpleModel.getInstance().getSceneList().get(mCurrentSceneIndex);
                scene.mStickerList = new Scene.StickerList();
                scene.mStickerList.mValue = stickerList;
                scene.mStickerList.mPosX = (mSurfaceWidth - stickerWidth) / 2;
                scene.mStickerList.mPosY = (mSurfaceHeight - stickerHeight) / 2;
                scene.mStickerList.mScaleFactor = 0.75f;
                AVFlowEngine.getInstance().removePreviewImage(mEngineToken);
                replayScene(scene);
                break;
            case R.id.iv_sticker_7:
                stickerList = getStickerList(mStickersPathList.get(6));
                sticker = BitmapFactory.decodeFile(stickerList.get(0));
                stickerWidth = sticker.getWidth();
                stickerHeight = sticker.getHeight();
                sticker.recycle();
                scene = SimpleModel.getInstance().getSceneList().get(mCurrentSceneIndex);
                scene.mStickerList = new Scene.StickerList();
                scene.mStickerList.mValue = stickerList;
                scene.mStickerList.mPosX = (mSurfaceWidth - stickerWidth) / 2;
                scene.mStickerList.mPosY = (mSurfaceHeight - stickerHeight) / 2;
                scene.mStickerList.mScaleFactor = 1.0f;
                AVFlowEngine.getInstance().removePreviewImage(mEngineToken);
                replayScene(scene);
                break;
            case R.id.iv_sticker_8:
                stickerList = getStickerList(mStickersPathList.get(7));
                sticker = BitmapFactory.decodeFile(stickerList.get(0));
                stickerWidth = sticker.getWidth();
                stickerHeight = sticker.getHeight();
                sticker.recycle();
                scene = SimpleModel.getInstance().getSceneList().get(mCurrentSceneIndex);
                scene.mStickerList = new Scene.StickerList();
                scene.mStickerList.mValue = stickerList;
                scene.mStickerList.mPosX = (mSurfaceWidth - stickerWidth) / 2;
                scene.mStickerList.mPosY = (mSurfaceHeight - stickerHeight) / 2;
                scene.mStickerList.mScaleFactor = 0.8f;
                AVFlowEngine.getInstance().removePreviewImage(mEngineToken);
                replayScene(scene);
                break;
            case R.id.iv_sticker_9:
                stickerList = getStickerList(mStickersPathList.get(8));
                sticker = BitmapFactory.decodeFile(stickerList.get(0));
                stickerWidth = sticker.getWidth();
                stickerHeight = sticker.getHeight();
                sticker.recycle();
                scene = SimpleModel.getInstance().getSceneList().get(mCurrentSceneIndex);
                scene.mStickerList = new Scene.StickerList();
                scene.mStickerList.mValue = stickerList;
                scene.mStickerList.mPosX = (mSurfaceWidth - stickerWidth) / 2;
                scene.mStickerList.mPosY = (mSurfaceHeight - stickerHeight) / 2;
                scene.mStickerList.mScaleFactor = 0.8f;
                AVFlowEngine.getInstance().removePreviewImage(mEngineToken);
                replayScene(scene);
                break;
            case R.id.iv_sticker_10:
                stickerList = getStickerList(mStickersPathList.get(9));
                sticker = BitmapFactory.decodeFile(stickerList.get(0));
                stickerWidth = sticker.getWidth();
                stickerHeight = sticker.getHeight();
                sticker.recycle();
                scene = SimpleModel.getInstance().getSceneList().get(mCurrentSceneIndex);
                scene.mStickerList = new Scene.StickerList();
                scene.mStickerList.mValue = stickerList;
                scene.mStickerList.mPosX = (mSurfaceWidth - stickerWidth) / 2;
                scene.mStickerList.mPosY = (mSurfaceHeight - stickerHeight) / 2;
                scene.mStickerList.mScaleFactor = 0.8f;
                AVFlowEngine.getInstance().removePreviewImage(mEngineToken);
                replayScene(scene);
                break;
            case R.id.btn_delete_stickers:
                scene = SimpleModel.getInstance().getSceneList().get(mCurrentSceneIndex);
                if (scene.mStickerList != null) {
                    AVFlowEngine.getInstance().removePreviewImage(mEngineToken);
                    scene.mStickerList = null;
                    replayScene(scene);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "onSurfaceTextureAvailable: ");
        mEngineToken = AVFlowEngine.getInstance().newWorker();

        mSurfaceWidth = width;
        mSurfaceHeight = height;
        // start play preview now.
        FileInputConfig inputConfig = new FileInputConfig.Builder()
                .addFile(SimpleModel.getInstance().getSceneList().get(0).mVideo)
                .loop(true)
                .speedRate(FileInputConfig.SPEED_RATE_NORMAL)
                .rotation(FileInputConfig.ROTATION_180)
                .surfaceSize(mSurfaceWidth, mSurfaceHeight)
                .listener(mVideoPlayListener)
                .build();
        PreviewOutputConfig outputConfig = new PreviewOutputConfig.Builder()
                .surface(new Surface(surface), width, height)
                .build();
        AVFlowEngine.getInstance().configInput(mEngineToken, inputConfig);
        AVFlowEngine.getInstance().configOutput(mEngineToken, outputConfig);
        AVFlowEngine.getInstance().start(mEngineToken);
        mSceneListAdapter.selectOne(0);

        SeekBar changePosX = mTextTitlePanel.findViewById(R.id.sb_change_font_posx);
        SeekBar changePosY = mTextTitlePanel.findViewById(R.id.sb_change_font_posy);
        changePosX.setMax(mSurfaceWidth);
        changePosY.setMax(mSurfaceHeight);

        mSurfaceScaleFactor = mSurfaceHeight * 1.0f /
                SimpleModel.getInstance().getSceneList().get(0).mHeight;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        AVFlowEngine.getInstance().stop(mEngineToken);
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
        mTextPanel.findViewById(R.id.vw_text_title).setOnClickListener(this);
        mTextPanel.findViewById(R.id.vw_text_caption).setOnClickListener(this);
        mTextPanel.findViewById(R.id.vw_text_label).setOnClickListener(this);
    }

    private void initStickerPanel() {
        mStickPanel = getLayoutInflater().inflate(R.layout.edit_panel_sticker, mPanelContainer, false);
        mStickPanel.findViewById(R.id.iv_sticker_1).setOnClickListener(this);
        mStickPanel.findViewById(R.id.iv_sticker_2).setOnClickListener(this);
        mStickPanel.findViewById(R.id.iv_sticker_3).setOnClickListener(this);
        mStickPanel.findViewById(R.id.iv_sticker_4).setOnClickListener(this);
        mStickPanel.findViewById(R.id.iv_sticker_5).setOnClickListener(this);
        mStickPanel.findViewById(R.id.iv_sticker_6).setOnClickListener(this);
        mStickPanel.findViewById(R.id.iv_sticker_7).setOnClickListener(this);
        mStickPanel.findViewById(R.id.iv_sticker_8).setOnClickListener(this);
        mStickPanel.findViewById(R.id.iv_sticker_9).setOnClickListener(this);
        mStickPanel.findViewById(R.id.iv_sticker_10).setOnClickListener(this);
        mStickPanel.findViewById(R.id.btn_delete_stickers).setOnClickListener(this);

        initStickers();
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
                AVFlowEngine.getInstance().setPreviewFilter(mEngineToken, mCurrentFilter.get(VideoEditActivity.this));
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

    private void initTextTitlePanel() {
        mTextTitlePanel = getLayoutInflater().inflate(R.layout.edit_panel_text_title, mPanelContainer, false);
        mTextTitlePanel.findViewById(R.id.btn_set_title).setOnClickListener(this);
        mTextTitlePanel.findViewById(R.id.btn_delete_title).setOnClickListener(this);
        mTextTitlePanel.findViewById(R.id.iv_set_text_panel_back).setOnClickListener(this);
        mTextTitlePanel.findViewById(R.id.tv_text_bg_1).setOnClickListener(this);
        mTextTitlePanel.findViewById(R.id.tv_text_bg_2).setOnClickListener(this);
        mTextTitlePanel.findViewById(R.id.tv_text_bg_3).setOnClickListener(this);
        mTextTitlePanel.findViewById(R.id.tv_text_bg_4).setOnClickListener(this);
        TextView font1 = mTextTitlePanel.findViewById(R.id.tv_text_font_1);
        TextView font2 = mTextTitlePanel.findViewById(R.id.tv_text_font_2);
        TextView font3 = mTextTitlePanel.findViewById(R.id.tv_text_font_3);
        TextView font4 = mTextTitlePanel.findViewById(R.id.tv_text_font_4);
        font1.setOnClickListener(this);
        font2.setOnClickListener(this);
        font3.setOnClickListener(this);
        font4.setOnClickListener(this);
        Typeface type1 = Typeface.createFromAsset(getAssets(), "KaBuQiNuo.otf");
        Typeface type2 = Typeface.createFromAsset(getAssets(), "MFYanSong-Regular.ttf");
        Typeface type3 = Typeface.createFromAsset(getAssets(), "SentyWEN2017.ttf");
        Typeface type4 = Typeface.createFromAsset(getAssets(), "YouLangRuanBi.ttf");
        font1.setTypeface(type1);
        font2.setTypeface(type2);
        font3.setTypeface(type3);
        font4.setTypeface(type4);
        SeekBar changeSize = mTextTitlePanel.findViewById(R.id.sb_change_font_size);
        changeSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    return;
                }
                Scene scene = SimpleModel.getInstance().getSceneList().get(mCurrentSceneIndex);
                if (scene.mText == null || TextUtils.isEmpty(scene.mText.mValue)) {
                    return;
                }
                // update params of scene.
                scene.mText.mTextSize = (int) (progress / mSurfaceScaleFactor);

                AVFlowEngine.getInstance().setPreviewText(mEngineToken,
                        scene.mText.mFontPath,
                        scene.mText.mValue,
                        (int) (scene.mText.mPosX * mSurfaceScaleFactor),
                        (int) (scene.mText.mPosY * mSurfaceScaleFactor),
                        progress,
                        scene.mText.mRed,
                        scene.mText.mGreen,
                        scene.mText.mBlue,
                        scene.mText.mBackground);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        SeekBar changeRed = mTextTitlePanel.findViewById(R.id.sb_change_font_red);
        changeRed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    return;
                }
                Scene scene = SimpleModel.getInstance().getSceneList().get(mCurrentSceneIndex);
                if (scene.mText == null || TextUtils.isEmpty(scene.mText.mValue)) {
                    return;
                }

                // update params of scene.
                scene.mText.mRed = progress * 1.0f / seekBar.getMax();

                AVFlowEngine.getInstance().updatePreviewTextParams(mEngineToken,
                        (int) (scene.mText.mPosX * mSurfaceScaleFactor),
                        (int) (scene.mText.mPosY * mSurfaceScaleFactor),
                        scene.mText.mRed,
                        scene.mText.mGreen,
                        scene.mText.mBlue);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        SeekBar changeGreen = mTextTitlePanel.findViewById(R.id.sb_change_font_green);
        changeGreen.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    return;
                }
                Scene scene = SimpleModel.getInstance().getSceneList().get(mCurrentSceneIndex);
                if (scene.mText == null || TextUtils.isEmpty(scene.mText.mValue)) {
                    return;
                }

                // update params of scene.
                scene.mText.mGreen = progress * 1.0f / seekBar.getMax();

                AVFlowEngine.getInstance().updatePreviewTextParams(mEngineToken,
                        (int) (scene.mText.mPosX * mSurfaceScaleFactor),
                        (int) (scene.mText.mPosY * mSurfaceScaleFactor),
                        scene.mText.mRed,
                        scene.mText.mGreen,
                        scene.mText.mBlue);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        SeekBar changeBlue = mTextTitlePanel.findViewById(R.id.sb_change_font_blue);
        changeBlue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    return;
                }
                Scene scene = SimpleModel.getInstance().getSceneList().get(mCurrentSceneIndex);
                if (scene.mText == null || TextUtils.isEmpty(scene.mText.mValue)) {
                    return;
                }

                // update params of scene.
                scene.mText.mBlue = progress * 1.0f / seekBar.getMax();

                AVFlowEngine.getInstance().updatePreviewTextParams(mEngineToken,
                        (int) (scene.mText.mPosX * mSurfaceScaleFactor),
                        (int) (scene.mText.mPosY * mSurfaceScaleFactor),
                        scene.mText.mRed,
                        scene.mText.mGreen,
                        scene.mText.mBlue);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        SeekBar changePosX = mTextTitlePanel.findViewById(R.id.sb_change_font_posx);
        changePosX.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    return;
                }
                Scene scene = SimpleModel.getInstance().getSceneList().get(mCurrentSceneIndex);
                if (scene.mText == null || TextUtils.isEmpty(scene.mText.mValue)) {
                    return;
                }

                // update params of scene.
                scene.mText.mPosX = (int) (progress / mSurfaceScaleFactor);

                AVFlowEngine.getInstance().updatePreviewTextParams(mEngineToken,
                        progress,
                        (int) (scene.mText.mPosY * mSurfaceScaleFactor),
                        scene.mText.mRed,
                        scene.mText.mGreen,
                        scene.mText.mBlue);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        SeekBar changePosY = mTextTitlePanel.findViewById(R.id.sb_change_font_posy);
        changePosY.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    return;
                }
                Scene scene = SimpleModel.getInstance().getSceneList().get(mCurrentSceneIndex);
                if (scene.mText == null || TextUtils.isEmpty(scene.mText.mValue)) {
                    return;
                }

                // update params of scene.
                scene.mText.mPosY = (int) (progress / mSurfaceScaleFactor);

                AVFlowEngine.getInstance().updatePreviewTextParams(mEngineToken,
                        (int) (scene.mText.mPosX * mSurfaceScaleFactor),
                        progress,
                        scene.mText.mRed,
                        scene.mText.mGreen,
                        scene.mText.mBlue);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
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

    private void initStickers() {
        mStickersPathList = new ArrayList<>();
        mStickersPathList.add(new File(getFilesDir(), "stickers/beach"));
        mStickersPathList.add(new File(getFilesDir(), "stickers/book"));
        mStickersPathList.add(new File(getFilesDir(), "stickers/city"));
        mStickersPathList.add(new File(getFilesDir(), "stickers/drink"));
        mStickersPathList.add(new File(getFilesDir(), "stickers/imcome"));
        mStickersPathList.add(new File(getFilesDir(), "stickers/pingfan"));
        mStickersPathList.add(new File(getFilesDir(), "stickers/train"));
        mStickersPathList.add(new File(getFilesDir(), "stickers/tubu"));
        mStickersPathList.add(new File(getFilesDir(), "stickers/weizhi"));
        mStickersPathList.add(new File(getFilesDir(), "stickers/xingchen"));
    }

    private List<String> getStickerList(File path) {
        String[] fileNames = path.list();
        List<String> pathList = new ArrayList<>();

        for (String fileName : fileNames) {
            pathList.add(new File(path, fileName).getAbsolutePath());
        }

        return pathList;
    }

    private void replayScene(Scene scene) {
        FileInputConfig inputConfig = new FileInputConfig.Builder()
                .addFile(scene.mVideo)
                .loop(true)
                .speedRate(FileInputConfig.SPEED_RATE_NORMAL)
                .rotation(FileInputConfig.ROTATION_180)
                .surfaceSize(mSurfaceWidth, mSurfaceHeight)
                .listener(mVideoPlayListener)
                .build();
        AVFlowEngine.getInstance().configInput(mEngineToken, inputConfig);
    }
}
