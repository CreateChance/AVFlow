package com.createchance.mediastreambase;

/**
 * ${DESC}
 *
 * @author createchance
 * @date 2018/8/27
 */
public abstract class AbstractStreamNode {

    protected AVFlowSession mSession;

    protected abstract boolean init();

    protected abstract void shutdown();
}
