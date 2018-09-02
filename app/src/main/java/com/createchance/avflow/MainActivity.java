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
import com.createchance.mediastreamprocessor.gpuimage.GPUImage3x3ConvolutionFilter;
import com.createchance.mediastreamprocessor.gpuimage.GPUImageLookupFilter;
import com.createchance.mediastreamsaver.MuxerStreamSaver;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private IVideoStreamGenerator mGenerator;
    private CodecStreamProcessor mProcessor;
    private IVideoStreamConsumer mPreviewer;
    private IVideoStreamConsumer mSaver;

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
        mGenerator = getLocalGenerator();
        mSaver = getSaver();

        mGenerator.setConsumer(mProcessor);
        mProcessor.setConsumer(mPreviewer);
        mProcessor.setConsumer(mSaver);
    }

    @Override
    protected void onStop() {
        super.onStop();

        mGenerator.stop();
    }

    @Override
    public void onClick(View v) {
        mProcessor.start();
        mGenerator.start();
    }

    private IVideoStreamConsumer getSaver() {
        IVideoStreamConsumer saver = new MuxerStreamSaver.Builder()
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
                .gpuImageFilter(lookupFilter)
                .build();
        return processor;
    }
}
