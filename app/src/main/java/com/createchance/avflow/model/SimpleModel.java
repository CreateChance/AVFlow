package com.createchance.avflow.model;

import java.util.ArrayList;
import java.util.List;

public class SimpleModel {
    private static SimpleModel sInstance;

    private final List<Scene> mSceneList;

    private SimpleModel() {
        mSceneList = new ArrayList<>();
    }

    public synchronized static SimpleModel getInstrance() {
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
}
