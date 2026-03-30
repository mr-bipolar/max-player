package com.aaronmaxlab.maxplayer.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.aaronmaxlab.maxplayer.MPVActivity;
import com.aaronmaxlab.maxplayer.R;
import com.aaronmaxlab.maxplayer.localdb.SqlDbHelper;
import com.aaronmaxlab.maxplayer.models.M3uModel;
import com.aaronmaxlab.maxplayer.subclass.DeviceUtils;
import com.aaronmaxlab.maxplayer.subclass.TvFocusHelper;
import com.bumptech.glide.Glide;

import java.util.ArrayList;


public class FavlistAdapter extends RecyclerView.Adapter<FavlistAdapter.FavlistViewHolder> {

    private final ArrayList<M3uModel> m3uModelList;
    private final Context context;
    private boolean isTV = false;

    public FavlistAdapter(ArrayList<M3uModel> m3uModelList, Context context) {
        this.m3uModelList = m3uModelList;
        this.context = context;
    }


    @NonNull
    @Override
    public FavlistAdapter.FavlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fav_view, parent, false);
        isTV = DeviceUtils.isTv(context);
        return new FavlistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavlistAdapter.FavlistViewHolder holder, int position) {
           M3uModel m3uModel = m3uModelList.get(position);
           String name = m3uModel.getChannelName();
        if (name == null || name.trim().isEmpty()) {
            name = "Channel";
        }
        holder.title.setText(name);

        Glide.with(context)
                .load(m3uModel.getChannelLogo())
                .placeholder(R.drawable.mpv_logo)
                .into(holder.poster);

        holder.editBtn.setOnClickListener(v -> {

            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setTitle("Delete")
                    .setMessage("Remove this item?")
                    .setPositiveButton("Delete", (d, w) -> {

                        int pos = holder.getAbsoluteAdapterPosition();

                        if (pos != RecyclerView.NO_POSITION) {

                            M3uModel item = m3uModelList.get(pos);

                            try (SqlDbHelper database = new SqlDbHelper(v.getContext())) {
                                database.deleteChannel(item.getChannelUrl());
                            } catch (Exception e) {
                                Log.d("Db Error", e.toString());
                            }

                            m3uModelList.remove(pos);
                            notifyItemRemoved(pos);
                            notifyItemRangeChanged(pos, m3uModelList.size());
                        }

                    })
                    .setNegativeButton("Cancel", null)
                    .create();

            dialog.show();

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_stack_card);
            }
        });

        holder.layout.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), MPVActivity.class);
            intent.putExtra("filepath", m3uModel.getChannelUrl());
            intent.putExtra("isVideoUrl", true);
            v.getContext().startActivity(intent);
        });

        if (isTV){
            TvFocusHelper.applyFocus(holder.layout, holder.editBtn);
        }

    }

    @Override
    public int getItemCount() {
        return m3uModelList.size();
    }

    public static class FavlistViewHolder  extends  RecyclerView.ViewHolder{

        TextView title;
        ImageView poster;
        ImageButton editBtn;
        ConstraintLayout layout;

        public FavlistViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.favTitle);
            poster = itemView.findViewById(R.id.favImage);
            editBtn = itemView.findViewById(R.id.favEdit);
            layout = itemView.findViewById(R.id.favLayout);

        }
    }
}
