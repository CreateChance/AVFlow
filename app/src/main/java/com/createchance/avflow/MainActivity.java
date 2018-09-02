package com.createchance.avflow;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.createchance.camerastreamgenerator.CameraStreamGenerator;
import com.createchance.localstreamgenerator.LocalStreamGenerator;
import com.createchance.mediastreambase.IVideoStreamConsumer;
import com.createchance.mediastreambase.IVideoStreamGenerator;
import com.createchance.mediastreamprocessor.CodecStreamProcessor;
import com.createchance.mediastreamprocessor.gpuimage.GPUImageFilter;
import com.createchance.mediastreamprocessor.gpuimage.GPUImageLookupFilter;
import com.createchance.mediastreamprocessor.gpuimage.GPUImageSwirlFilter;
import com.createchance.mediastreamsaver.MuxerStreamSaver;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private IVideoStreamGenerator mGenerator;
    private CodecStreamProcessor mProcessor;
    private IVideoStreamConsumer mPreviewer;
    private MuxerStreamSaver mSaver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setNavigationBarColor(Color.TRANSPARENT);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        mPreviewer = findViewById(R.id.vw_preview);
        mProcessor = getProcessor();
        mGenerator = getCameraGenerator();
        mSaver = getSaver();

        mGenerator.setConsumer(mProcessor);
        mProcessor.setConsumer(mPreviewer);
        mProcessor.setConsumer(mSaver);
        mProcessor.setFilter(mPreviewer, getFilter());
        mProcessor.setFilter(mSaver, getFilter());
    }

    @Override
    protected void onStop() {
        super.onStop();

        mGenerator.stop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_generator:
                mGenerator.start();
                break;
            case R.id.btn_start_saver:
                mSaver.startSave();
                break;
            case R.id.btn_stop_saver:
                mSaver.stopSave();
                break;
            default:
                break;
        }

    }

    private MuxerStreamSaver getSaver() {
        MuxerStreamSaver saver = new MuxerStreamSaver.Builder()
                .output(new File(Environment.getExternalStorageDirectory(), "videoeditor/test.mp4"))
                .videoSize(1080, 1920)
                .build();
        return saver;
    }

    private IVideoStreamGenerator getCameraGenerator() {
        return new CameraStreamGenerator();
    }

    private IVideoStreamGenerator getLocalGenerator() {
        return new LocalStreamGenerator.Builder()
                .sourceFile(new File(Environment.getExternalStorageDirectory(), "videoeditor/input2.mp4"))
                .build();
    }

    private CodecStreamProcessor getProcessor() {
        GPUImageLookupFilter lookupFilter = new GPUImageLookupFilter();
        lookupFilter.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.lookup_filter9));


//        GPUImageAddBlendFilter addBlendFilter = new GPUImageAddBlendFilter();
//        addBlendFilter.setBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
//        addBlendFilter.setRotation(Rotation.ROTATION_90, false, false);

//        GPUImage3x3ConvolutionFilter convolutionFilter = new GPUImage3x3ConvolutionFilter();
//        convolutionFilter.setConvolutionKernel(new float[]{
//                -1.0f, 0.0f, 1.0f,
//                -2.0f, 0.0f, 2.0f,
//                -1.0f, 0.0f, 1.0f
//        });
        CodecStreamProcessor processor = new CodecStreamProcessor.Builder()
                .build();
        return processor;
    }

    private GPUImageFilter getFilter() {
//        GPUImageLookupFilter lookupFilter = new GPUImageLookupFilter();
//        lookupFilter.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.lookup_filter9));
        GPUImageSwirlFilter swirlFilter = new GPUImageSwirlFilter();

        return swirlFilter;
    }
}
