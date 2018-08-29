package com.createchance.mediastreambase;

/**
 * ${DESC}
 *
 * @author createchance
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
        if (session.mPreviewer != null) {
            session.mPreviewer.init();
        }
        if (session.mSaver != null) {
            session.mSaver.init();
        }
        session.mProcessor.init();
        session.mGenerator.init();

        session.mGenerator.run();
    }
}
