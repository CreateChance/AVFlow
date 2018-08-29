package com.createchance.avflow;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.createchance.camerastreamgenerator.CameraStreamGenerator;
import com.createchance.mediastreambase.AVFlowSession;
import com.createchance.mediastreampreviewer.TexturePreviewer;
import com.createchance.mediastreamprocessor.CodecStreamProcessor;
import com.createchance.mediastreamsaver.MuxerStreamSaver;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_start).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        AVFlowSession session = new AVFlowSession.Builder(this)
                .installGenerator(new CameraStreamGenerator())
                .installProcessor(new CodecStreamProcessor())
                .installPreviewer(new TexturePreviewer())
                .installSaver(new MuxerStreamSaver())
                .build();
        session.run();
    }
}
