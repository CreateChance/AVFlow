package com.createchance.avflow;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.createchance.avflow.model.Scene;
import com.createchance.avflow.utils.DensityUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * scene thumbnail list adapter
 *
 * @author createchance
 * @date 2018-09-06
 */
public class SceneThumbListAdapter extends RecyclerView.Adapter<SceneThumbListAdapter.ViewHolder> {

    private static final String TAG = "SceneThumbListAdapter";

    private Context mContext;

    private List<Data> mSceneList;

    private ClickListener mListener;

    public SceneThumbListAdapter(Context context, List<Scene> sceneList) {
        this(context, sceneList, null);
    }

    public SceneThumbListAdapter(Context context, List<Scene> sceneList, ClickListener listener) {
        mContext = context;
        mSceneList = new ArrayList<>();
        initList(sceneList);
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_scene_thumb, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Data data = mSceneList.get(position);

        if (data.mScene.mVideo != null && data.mScene.mVideo.exists() && data.mScene.mVideo.isFile()) {
            RequestOptions requestOptions = new RequestOptions();
            requestOptions = requestOptions.transforms(new CenterCrop(),
                    new RoundedCorners(DensityUtil.dip2px(mContext, 4)));
            Glide.with(mContext)
                    .load(data.mScene.mVideo)
                    .apply(requestOptions)
                    .into(holder.thumb);
        }

        if (data.mSelected) {
            holder.thumb.setBackgroundResource(R.drawable.bg_red_stroke);
        } else {
            holder.thumb.setBackgroundResource(R.drawable.bg_dark_grey);
        }
    }

    @Override
    public int getItemCount() {
        return mSceneList.size();
    }

    public void refresh(List<Scene> sceneList) {
        initList(sceneList);
        notifyDataSetChanged();
    }

    public void selectOne(int position) {
        for (Data data : mSceneList) {
            data.mSelected = false;
        }
        mSceneList.get(position).mSelected = true;
        notifyDataSetChanged();
    }

    public void selectAll() {
        for (Data data : mSceneList) {
            data.mSelected = true;
        }
        notifyDataSetChanged();
    }

    private void initList(List<Scene> sceneList) {
        mSceneList.clear();
        for (Scene scene : sceneList) {
            mSceneList.add(new Data(scene, false));
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView thumb;

        public ViewHolder(View itemView) {
            super(itemView);

            thumb = itemView.findViewById(R.id.iv_scene_thumb);
            thumb.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onClick(mSceneList.get(getAdapterPosition()).mScene);
            }
        }
    }

    class Data {
        Scene mScene;
        boolean mSelected;

        Data(Scene scene, boolean selected) {
            mScene = scene;
            mSelected = selected;
        }
    }

    public interface ClickListener {
        void onClick(Scene scene);
    }
}
