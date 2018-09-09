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

    private List<Scene> mSceneList;

    public SceneThumbListAdapter(Context context, List<Scene> sceneList) {
        mContext = context;
        mSceneList = sceneList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_scene_thumb, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Scene scene = mSceneList.get(position);

        if (scene.mVideo != null && scene.mVideo.exists() && scene.mVideo.isFile()) {
            RequestOptions requestOptions = new RequestOptions();
            requestOptions = requestOptions.transforms(new CenterCrop(),
                    new RoundedCorners(DensityUtil.dip2px(mContext, 4)));
            Glide.with(mContext)
                    .load(scene.mVideo)
                    .apply(requestOptions)
                    .into(holder.thumb);
        }
    }

    @Override
    public int getItemCount() {
        return mSceneList.size();
    }

    public void refresh(List<Scene> sceneList) {
        mSceneList = sceneList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView thumb;

        public ViewHolder(View itemView) {
            super(itemView);

            thumb = itemView.findViewById(R.id.iv_scene_thumb);
        }
    }
}
