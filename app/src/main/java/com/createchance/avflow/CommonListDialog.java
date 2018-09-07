package com.createchance.avflow;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Custom dialog
 *
 * @author createchance
 * @date 2018-09-04
 */
public class CommonListDialog extends Dialog implements View.OnClickListener {

    private static final String TAG = "CommonListDialog";

    private Context mContext;

    private List<String> mInfoList;

    private ItemChooseListener mListener;

    public CommonListDialog(@NonNull Context context, List<String> infoList, ItemChooseListener listener) {
        super(context, R.style.CustomDialog);
        this.mContext = context;
        this.mInfoList = infoList;
        this.mListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_common_list);
        RecyclerView recyclerView = findViewById(R.id.rcv_list);
        InfoListAdapter adapter = new InfoListAdapter(mContext, mInfoList, mListener);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setAdapter(adapter);
        findViewById(R.id.iv_close).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close:
                dismiss();
                break;
            default:
                break;
        }
    }

    private class InfoListAdapter extends RecyclerView.Adapter<InfoListAdapter.ViewHolder> {

        private Context mContext;
        private List<String> mInfoList;

        InfoListAdapter(Context context, List<String> infoList, ItemChooseListener listener) {
            mContext = context;
            mInfoList = infoList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_info, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String info = mInfoList.get(position);
            holder.textView.setText(info);
        }

        @Override
        public int getItemCount() {
            return mInfoList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView textView;

            public ViewHolder(View itemView) {
                super(itemView);

                textView = itemView.findViewById(R.id.tv_title);
                textView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                mListener.onChoose(getAdapterPosition());
                dismiss();
            }
        }
    }

    interface ItemChooseListener {
        void onChoose(int pos);
    }
}
