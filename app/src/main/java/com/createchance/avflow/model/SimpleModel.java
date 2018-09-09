package com.createchance.avflow.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model to provide data access.
 *
 * @author createchance
 * @date 2018-09-05
 */
public class SimpleModel {
    private static SimpleModel sInstance;

    private final List<Scene> mSceneList;

    private SimpleModel() {
        mSceneList = new ArrayList<>();
        initSceneList();
    }

    public synchronized static SimpleModel getInstance() {
        if (sInstance == null) {
            sInstance = new SimpleModel();
        }

        return sInstance;
    }

    public void addScene(Scene scene) {
        mSceneList.add(scene);
    }

    public void setSceneList(List<Scene> sceneList) {
        mSceneList.clear();
        mSceneList.addAll(sceneList);
    }

    public List<Scene> getSceneList() {
        return mSceneList;
    }

    private void initSceneList() {
        for (int i = 0; i < 4; i++) {
            mSceneList.add(new Scene());
        }
    }
}
