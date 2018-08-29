package com.createchance.mediastreambase;

import android.content.Context;

/**
 * ${DESC}
 *
 * @author gaochao1-iri
 * @date 2018/8/27
 */
public class AVFlowSession {

    private Context mContext;

    AbstractStreamProcessor mProcessor;
    AbstractStreamGenerator mGenerator;
    AbstractStreamPreviewer mPreviewer;
    AbstractStreamSaver mSaver;

    private AVFlowSession(Context context) {
        this.mContext = context;
    }

    public void run() {
        if (checkRational()) {
            buildGraph();
            AVFlowManager.getInstance().run(this);
        }
    }

    private boolean checkRational() {
        return mContext != null &&
                mProcessor != null &&
                mGenerator != null &&
                (mPreviewer != null || mSaver != null);
    }

    private void buildGraph() {
        mGenerator.mProcessor = mProcessor;
        mProcessor.mPreviewer = mPreviewer;
        mProcessor.mSaver = mSaver;
    }

    public static class Builder {

        private AVFlowSession session;

        public Builder(Context context) {
            session = new AVFlowSession(context.getApplicationContext());
        }

        public Builder installProcessor(AbstractStreamProcessor processor) {
            processor.mSession = session;
            session.mProcessor = processor;

            return this;
        }

        public Builder installGenerator(AbstractStreamGenerator generator) {
            generator.mSession = session;
            session.mGenerator = generator;

            return this;
        }

        public Builder installPreviewer(AbstractStreamPreviewer previewer) {
            previewer.mSession = session;
            session.mPreviewer = previewer;

            return this;
        }

        public Builder installSaver(AbstractStreamSaver saver) {
            saver.mSession = session;
            session.mSaver = saver;

            return this;
        }

        public AVFlowSession build() {
            return session;
        }
    }
}
