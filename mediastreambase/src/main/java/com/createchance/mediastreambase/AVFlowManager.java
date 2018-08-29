package com.createchance.mediastreambase;

/**
 * ${DESC}
 *
 * @author gaochao1-iri
 * @date 2018/8/27
 */
class AVFlowManager {

    private static AVFlowManager sInstance;

    private AVFlowManager() {

    }

    public static synchronized AVFlowManager getInstance() {
        if (sInstance == null) {
            sInstance = new AVFlowManager();
        }

        return sInstance;
    }

    void run(AVFlowSession session) {
        session.mProcessor.init();
        if (session.mPreviewer != null) {
            session.mPreviewer.init();
        }
        if (session.mSaver != null) {
            session.mSaver.init();
        }
        session.mGenerator.init();

        session.mGenerator.run();
    }
}
