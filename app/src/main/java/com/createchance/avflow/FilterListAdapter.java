package com.createchance.avflow;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.createchance.avflow.model.Filter;

import java.util.List;

/**
 * ${DESC}
 *
 * @author createchance
 * @date 2018/9/16
 */
public class FilterListAdapter extends RecyclerView.Adapter<FilterListAdapter.ViewHolder> {

    private static final String TAG = "FilterListAdapter";

    private Context mContext;

    private List<Filter> mFilterList;
    private int mCurrent;

    private Callback mCallback;

    public FilterListAdapter(Context context, List<Filter> filterList, Callback callback, int current) {
        mContext = context;
        mFilterList = filterList;
        mCallback = callback;
        mCurrent = current;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_filter, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Filter filter = mFilterList.get(position);
        holder.filterCode.setText(filter.mCode);
        if (position == mCurrent) {
            holder.filterCode.setTextColor(mContext.getResources().getColor(R.color.font_red));
        } else {
            holder.filterCode.setTextColor(mContext.getResources().getColor(R.color.font_grey));
        }
    }

    @Override
    public int getItemCount() {
        return mFilterList.size();
    }

    public void refreshCurrentFilter(int position) {
        mCurrent = position;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView filterCode;

        ViewHolder(View itemView) {
            super(itemView);

            filterCode = itemView.findViewById(R.id.tv_filter_code);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mCurrent = getAdapterPosition();
            notifyDataSetChanged();
            if (mCallback != null) {
                mCallback.onClick(mCurrent);
            }
        }
    }

    public interface Callback {
        void onClick(int position);
    }
}
