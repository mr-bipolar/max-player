package com.aaronmaxlab.maxplayer;

import android.content.ActivityNotFoundException;
import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aaronmaxlab.maxplayer.adapters.PlaylistAdapter;
import com.aaronmaxlab.maxplayer.databinding.ActivityMainBinding;
import com.aaronmaxlab.maxplayer.localdb.SqlDbHelper;
import com.aaronmaxlab.maxplayer.models.M3uModel;
import com.aaronmaxlab.maxplayer.models.PlaylistModel;
import com.aaronmaxlab.maxplayer.preferences.AboutActivity;
import com.aaronmaxlab.maxplayer.preferences.PreferenceActivity;
import com.aaronmaxlab.maxplayer.subclass.DeviceUtils;
import com.aaronmaxlab.maxplayer.subclass.TvFocusHelper;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private SqlDbHelper database;
    private List<PlaylistModel> playlists;
    private final PlaylistAdapter[] adapterRef = new PlaylistAdapter[1];
    private  DrawerLayout  drawerLayout;
    boolean isTv = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        isTv = DeviceUtils.isTv(this);

        if (isTv) {

            //  Focus
            TvFocusHelper.applyFocus(
                    binding.channelsViewBtn,
                    binding.moviesViewBtn,
                    binding.sportsViewBtn,
                    binding.newsViewBtn,
                    binding.btnSettings,
                    binding.seriesViewBtn,
                    binding.seeAllBtn,
                    binding.playlistViewBtn);

            // TV Playlist btn
            assert binding.playlistViewBtn != null;
            binding.playlistViewBtn.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, PlaylistViewActivity.class);
                startActivity(intent);
            });

            // TV Media Btn
            assert binding.btnSettings != null;
            binding.btnSettings.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
            });



        } else {
            //  Phone
            ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });

            assert binding.toolbar != null;
            ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar, (v, insets) -> {
                Insets statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars());
                v.setPadding(
                        v.getPaddingLeft(),
                        statusBar.top,
                        v.getPaddingRight(),
                        v.getPaddingBottom()
                );
                return insets;
            });


            drawerLayout = binding.drawerLayout;
            MaterialToolbar toolbar = binding.toolbar;
            setSupportActionBar(toolbar);

            DeviceUtils.applyBlur(binding.drawerBlurLayer, 20f);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this,
                    drawerLayout,
                    toolbar,
                    R.string.navigation_drawer_open,
                    R.string.navigation_drawer_close
            );
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
            toggle.getDrawerArrowDrawable().setColor(
                    ContextCompat.getColor(this, R.color.accent)
            );

            // sidebar button action
            binding.toolbar.setNavigationOnClickListener(v -> {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });


            // Android Mobile
            binding.toolbar.setNavigationIconTint(ContextCompat.getColor(this, R.color.accent));
            binding.toolbar.setTitleTextColor(ContextCompat.getColor(this, android.R.color.white));
            binding.toolbar.setOverflowIcon(
                    DrawableCompat.wrap(Objects.requireNonNull(binding.toolbar.getOverflowIcon()))
            );

            // sidebar buttons
            assert binding.playerInfo != null;
            binding.playerInfo.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
            });

            assert binding.licenseInfo != null;
            binding.licenseInfo.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, LicensesActivity.class);
                startActivity(intent);
            });

            // policy url

            assert binding.policyInfo != null;
            binding.policyInfo.setOnClickListener(v -> {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://sites.google.com/view/max-player-privacy-policy")));
            });

            // Rate app
            assert binding.rateApp != null;
            binding.rateApp.setOnClickListener(v -> {
                String packageName = getPackageName();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + packageName)));
                } catch (ActivityNotFoundException e) {
                    // If Play Store not installed, open in browser
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
                }
            });

            // Share app
            assert binding.shareApp != null;
            binding.shareApp.setOnClickListener(v -> {
                String shareMessage = "📺 Max Player - Live TV & IPTV Player\n\n"
                        + "• Watch Live TV\n"
                        + "• Smooth streaming\n"
                        + "• Easy playlist support\n\n"
                        + "Get it on Google Play:\n"
                        + "https://play.google.com/store/apps/details?id="
                        + getPackageName();

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Max Player App");
                intent.putExtra(Intent.EXTRA_TEXT, shareMessage);

                startActivity(Intent.createChooser(intent, "Share via"));
            });

            assert binding.fabBtn != null;
            binding.fabBtn.setOnClickListener(view -> {
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
            });

            database = new SqlDbHelper(this);
            playlists = database.getAllPlaylists();

            RecyclerView recyclerView = binding.m3uPlaylist;


            PlaylistAdapter adapter = new PlaylistAdapter(
                    playlists,
                    new PlaylistAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(PlaylistModel playlist) {
                            Intent intent = new Intent(MainActivity.this, MPVActivity.class);
                            intent.putExtra("filepath", playlist.getUrl());
                            startActivity(intent);
                        }

                        @Override
                        public void onDeleteClick(PlaylistModel playlist) {

                            AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Delete")
                                    .setMessage("Remove this item?")
                                    .setPositiveButton("Delete", (d, w) -> {

                                        boolean deleted = database.deleteM3uPlaylist(playlist.getUrl());
                                        if (deleted) {
                                            playlists.remove(playlist);

                                            // Use adapter reference
                                            adapterRef[0].notifyDataSetChanged();

                                            Toast.makeText(MainActivity.this, "Playlist deleted", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(MainActivity.this, "Failed to delete playlist", Toast.LENGTH_SHORT).show();
                                        }

                                    })
                                    .setNegativeButton("Cancel", null)
                                    .create();

                            dialog.show();

                            if (dialog.getWindow() != null) {
                                dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_stack_card);
                            }

                        }


                        @Override
                        public void onUpdateClick(PlaylistModel playlist) {
                            // Inflate custom layout
                            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_open_url, null);

                            EditText etPlaylist = view.findViewById(R.id.etPlaylist);
                            EditText etUrl = view.findViewById(R.id.etUrl);
                            Button btnOpen = view.findViewById(R.id.btnOpen);
                            TextView headerLabel = view.findViewById(R.id.header_label);
                            headerLabel.setText("Update Playlist");
                            // Pre-fill fields with existing playlist data
                            etPlaylist.setText(playlist.getName());
                            etUrl.setText(playlist.getUrl());


                            AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
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
                                    Toast.makeText(MainActivity.this, "Playlist updated", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "Please enter valid data", Toast.LENGTH_SHORT).show();
                                }
                            });


                            dialog.show();
                        }

                    }
            );

            adapterRef[0] = adapter;
            assert recyclerView != null;
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);

        }

        // Category List buttons

        binding.seeAllBtn.setOnClickListener(v -> {
            callPlaylist("Favourite");
        });

        binding.channelsViewBtn.setOnClickListener(v -> {
            callPlaylist("Channel");
        });

        binding.moviesViewBtn.setOnClickListener(v -> {
            callPlaylist("Movies");
        });


        binding.seriesViewBtn.setOnClickListener(v -> {
            callPlaylist("Series");
        });

        binding.sportsViewBtn.setOnClickListener(v -> {
            callPlaylist("Sports");
        });

        binding.newsViewBtn.setOnClickListener(v -> {
            callPlaylist("News");
        });

    }


    void close() {
        drawerLayout.closeDrawer(GravityCompat.START);
    }


    void callPlaylist(String itemName){
        Intent intent = new Intent(MainActivity.this, PlaylistActivity.class);
        intent.putExtra("catName", itemName);
        startActivity(intent);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(!isTv) {
            // Reload playlist from DB
            playlists.clear();
            playlists.addAll(database.getAllPlaylists());

            // Notify adapter
            adapterRef[0].notifyDataSetChanged();
        }
    }

}

