package com.aaronmaxlab.maxplayer.adapters;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.aaronmaxlab.maxplayer.R;
import com.aaronmaxlab.maxplayer.models.M3uModel;
import com.aaronmaxlab.maxplayer.subclass.ChannelSelect;
import com.aaronmaxlab.maxplayer.subclass.DeviceUtils;
import com.aaronmaxlab.maxplayer.subclass.TvFocusHelper;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class M3u8Adapter extends RecyclerView.Adapter<M3u8Adapter.M3u8ViewHolder> {

    private final Context context;
    private final ArrayList<M3uModel> m3uModels;
    private final ChannelSelect listener;
    private int selectedPlaylistIndex = -1;
    private boolean isTv = false;
    public M3u8Adapter(Context context, ArrayList<M3uModel> m3uModels, ChannelSelect listner) {
        this.context = context;
        this.m3uModels = m3uModels;
        this.listener = listner;
    }

    public void setSelectedPlaylistIndex(int index) {
        selectedPlaylistIndex = index;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public M3u8Adapter.M3u8ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        isTv = DeviceUtils.isTv(parent.getContext());
        if(isTv){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_card_tv, parent, false);
            return new M3u8ViewHolder(view);
        }else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_model, parent, false);
            return new M3u8ViewHolder(view);
        }
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull M3u8ViewHolder holder, int position) {

        M3uModel m3uModel = m3uModels.get(position);

        Glide.with(context)
                .load(m3uModel.getChannelLogo())
                .placeholder(R.drawable.mpv_logo)
                .into(holder.poster);

        holder.titleTxt.setText(m3uModel.getChannelName());

        boolean isSelected = m3uModel.getPlaylistIndex() == selectedPlaylistIndex;

        if (isTv) {
            holder.movieLayout.setSelected(isSelected);
        } else {
            holder.favButton.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            holder.cardView.setBackgroundResource(
                    isSelected
                            ? R.drawable.bg_stack_card_active
                            : R.drawable.bg_stack_card
            );
        }

       // TvFocusHelper.applyFocus(holder.movieLayout);
        if(isTv){
            holder.movieLayout.setOnClickListener(v -> {
                selectedPlaylistIndex = m3uModel.getPlaylistIndex();
                notifyDataSetChanged();

                listener.onChannelClicked(m3uModel, position);

            });

        }else{
            holder.cardView.setOnClickListener(v -> {
                selectedPlaylistIndex = m3uModel.getPlaylistIndex();
                notifyDataSetChanged();

                listener.onChannelClicked(m3uModel, position);

            });
        }

        holder.favButton.setOnClickListener(v -> {
            listener.onSubClicked(m3uModel);
        });
    }



    @Override
    public int getItemCount() {
        return m3uModels.size();
    }

    public static class M3u8ViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView titleTxt;
        ImageButton favButton;
        ConstraintLayout cardView;
        LinearLayout movieLayout;
        public M3u8ViewHolder(@NonNull View itemView) {
            super(itemView);

            poster = itemView.findViewById(R.id.posterImg);
            titleTxt = itemView.findViewById(R.id.titleTxt);
            favButton = itemView.findViewById(R.id.favBtn);
            cardView = itemView.findViewById(R.id.cardView);
            movieLayout = itemView.findViewById(R.id.movie_tv_card);
        }
    }



}
