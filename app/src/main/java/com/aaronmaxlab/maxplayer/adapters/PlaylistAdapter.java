package com.aaronmaxlab.maxplayer.adapters;

import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.aaronmaxlab.maxplayer.R;
import com.aaronmaxlab.maxplayer.models.PlaylistModel;
import com.aaronmaxlab.maxplayer.subclass.DeviceUtils;
import com.aaronmaxlab.maxplayer.subclass.TvFocusHelper;

import java.util.List;
import java.util.Objects;

public class PlaylistAdapter
        extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    private final List<PlaylistModel> playlistList;
    private final OnItemClickListener listener;
    private boolean isTv = false;
    public interface OnItemClickListener {
        void onItemClick(PlaylistModel playlist);
        void onDeleteClick(PlaylistModel playlist);
        void onUpdateClick(PlaylistModel playlist);

    }

    public PlaylistAdapter(List<PlaylistModel> playlistList,
                           OnItemClickListener listener) {
        this.playlistList = playlistList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.playlist_view, parent, false);
        isTv  = DeviceUtils.isTv(parent.getContext());
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull PlaylistViewHolder holder,
            int position) {

        PlaylistModel playlist = playlistList.get(position);
        String name = playlist.getName();
        if (name == null || name.trim().isEmpty()) {
            name = "Playlist";
        }
        holder.name.setText(name);
        holder.count.setText(playlist.getCount() + " Channels");

        holder.itemView.setOnClickListener(v ->
                listener.onItemClick(playlist));

        holder.more.setOnClickListener(v -> {

            // Create an AlertDialog
            AlertDialog dialog = getAlertDialog(v, playlist);
            GradientDrawable drawable = new GradientDrawable();
            int color = ContextCompat.getColor(v.getContext(), R.color.white);
            drawable.setColor(color);

            drawable.setCornerRadius(16f);
            int padding = 20; // in pixels
            InsetDrawable insetDrawable = new InsetDrawable(drawable, padding, padding, padding, padding);
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(insetDrawable);

            dialog.show();
        });
        // Tv
        if(isTv) {
            TvFocusHelper.applyFocus(holder.more, holder.itemLayout);

        }
    }

    @NonNull
    private AlertDialog getAlertDialog(View v, PlaylistModel playlist) {
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext(), R.style.CustomDialogTheme);

        // Options
        String[] options = {"Edit Playlist", "Delete  Playlist"};

        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                if (listener != null) {
                    listener.onUpdateClick(playlist);
                }
            } else if (which == 1) {
                if (listener != null) {
                    listener.onDeleteClick(playlist);
                }
            }
        });

        AlertDialog dialog = builder.create();
        if (isTv) {
            dialog.setOnShowListener(d -> {

                ListView listView = dialog.getListView();
                if (listView != null) {

                    listView.setSelector(R.drawable.tv_dialog_selector);
                    listView.setDrawSelectorOnTop(true);
                    listView.setFocusable(true);
                    listView.setFocusableInTouchMode(true);
                    listView.requestFocus();
                    listView.setSelection(0);
                }
            });
        }
        return dialog;
    }

    @Override
    public int getItemCount() {
        return playlistList.size();
    }

    static class PlaylistViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView count;
        ImageButton more;
        FrameLayout itemLayout;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.tvPlaylistName);
            count = itemView.findViewById(R.id.tvPlaylistCount);
            more = itemView.findViewById(R.id.ivMore);
            itemLayout = itemView.findViewById(R.id.cardRoot);
        }
    }
}
