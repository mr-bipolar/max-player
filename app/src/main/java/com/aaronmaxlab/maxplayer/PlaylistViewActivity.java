package com.aaronmaxlab.maxplayer;

import static androidx.constraintlayout.widget.ConstraintSet.VISIBLE;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aaronmaxlab.maxplayer.adapters.PlaylistAdapter;
import com.aaronmaxlab.maxplayer.databinding.ActivityPlaylistViewBinding;
import com.aaronmaxlab.maxplayer.localdb.SqlDbHelper;
import com.aaronmaxlab.maxplayer.models.PlaylistModel;
import com.aaronmaxlab.maxplayer.subclass.DeviceUtils;
import com.aaronmaxlab.maxplayer.subclass.TvFocusHelper;

import java.util.List;

public class PlaylistViewActivity extends AppCompatActivity {

    private SqlDbHelper database;
    private List<PlaylistModel> playlists;
    private final PlaylistAdapter[] adapterRef = new PlaylistAdapter[1];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivityPlaylistViewBinding binding = ActivityPlaylistViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        boolean isTv = DeviceUtils.isTv(this);

       if(isTv){
           RecyclerView playlistView = binding.playlistView;

           TvFocusHelper.applyFocus(binding.backBtn);

           assert binding.backBtn != null;
           binding.backBtn.setOnClickListener(v -> {
               getOnBackPressedDispatcher().onBackPressed();
           });

           database = new SqlDbHelper(this);
           playlists = database.getAllPlaylists();

           assert binding.emptyView != null;
           if(playlists.isEmpty()){

               binding.emptyView.setVisibility(View.VISIBLE);
           }else{
               binding.emptyView.setVisibility(View.GONE);
           }

           PlaylistAdapter adapter = new PlaylistAdapter(
                   playlists,
                   new PlaylistAdapter.OnItemClickListener() {
                       @Override
                       public void onItemClick(PlaylistModel playlist) {
                           Intent intent = new Intent(PlaylistViewActivity.this, MPVActivity.class);
                           intent.putExtra("filepath", playlist.getUrl());
                           startActivity(intent);
                       }

                       @Override
                       public void onDeleteClick(PlaylistModel playlist) {
                           boolean deleted = database.deleteM3uPlaylist(playlist.getUrl());
                           if (deleted) {
                               playlists.remove(playlist);

                               // Use adapter reference
                               adapterRef[0].notifyDataSetChanged();

                               Toast.makeText(PlaylistViewActivity.this, "Playlist deleted", Toast.LENGTH_SHORT).show();
                           } else {
                               Toast.makeText(PlaylistViewActivity.this, "Failed to delete playlist", Toast.LENGTH_SHORT).show();
                           }
                       }


                       @Override
                       public void onUpdateClick(PlaylistModel playlist) {
                           // Inflate custom layout
                           View view = LayoutInflater.from(PlaylistViewActivity.this).inflate(R.layout.dialog_open_url, null);

                           EditText etPlaylist = view.findViewById(R.id.etPlaylist);
                           EditText etUrl = view.findViewById(R.id.etUrl);
                           Button btnOpen = view.findViewById(R.id.btnOpen);
                           TextView headerLabel = view.findViewById(R.id.header_label);
                           headerLabel.setText("Update Playlist");
                           // Pre-fill fields with existing playlist data
                           etPlaylist.setText(playlist.getName());
                           etUrl.setText(playlist.getUrl());


                           AlertDialog dialog = new AlertDialog.Builder(PlaylistViewActivity.this)
                                   .setView(view)
                                   .create();

                           btnOpen.setEnabled(true);

                           btnOpen.setOnClickListener(v -> {
                               // Get text values here, when the user clicks
                               String newName = etPlaylist.getText().toString().trim();
                               String newUrl = etUrl.getText().toString().trim();

                               if (!newName.isEmpty() && !newUrl.isEmpty()) {
                                   // Update playlist in database
                                   database.updateM3uPlaylist(playlist.getUrl(), newName, newUrl);
                                   playlists.clear();
                                   playlists.addAll(database.getAllPlaylists());
                                   adapterRef[0].notifyDataSetChanged();
                                   dialog.dismiss();
                                   Toast.makeText(PlaylistViewActivity.this, "Playlist updated", Toast.LENGTH_SHORT).show();
                               } else {
                                   Toast.makeText(PlaylistViewActivity.this, "Please enter valid data", Toast.LENGTH_SHORT).show();
                               }
                           });


                           dialog.show();
                       }

                   }
           );

           adapterRef[0] = adapter;
           playlistView.setLayoutManager(new GridLayoutManager(this, 3));
           playlistView.setAdapter(adapter);
       }
    }
}