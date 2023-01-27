package com.example.socketdemo;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.socketdemo.databinding.ItemRvMsgBinding;

import java.util.List;

public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.ViewHolder>{

    private List<Message> mMsgList;

    static class ViewHolder extends RecyclerView.ViewHolder {

        ItemRvMsgBinding mView;

        public ViewHolder(@NonNull ItemRvMsgBinding itemView) {
            super(itemView.getRoot());
            mView = itemView;
        }

    }

    public MsgAdapter(List<Message> mMsgList) {
        this.mMsgList = mMsgList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRvMsgBinding itemRvMsgBinding = ItemRvMsgBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ViewHolder(itemRvMsgBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message msg = mMsgList.get(position);

        if (msg.getType() == 1) {
            holder.mView.tvServerMsg.setText(msg.getMsg());
            holder.mView.msgLayoutServer.setVisibility(View.VISIBLE);
            holder.mView.msgLayoutClient.setVisibility(View.GONE);
        } else {
            holder.mView.tvClientMsg.setText(msg.getMsg());
            holder.mView.msgLayoutServer.setVisibility(View.GONE);
            holder.mView.msgLayoutClient.setVisibility(View.VISIBLE);;
        }
    }

    @Override
    public int getItemCount() {
        return mMsgList.size();
    }

}
