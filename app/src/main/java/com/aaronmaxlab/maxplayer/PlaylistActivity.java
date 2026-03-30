package com.aaronmaxlab.maxplayer;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aaronmaxlab.maxplayer.adapters.FavlistAdapter;
import com.aaronmaxlab.maxplayer.databinding.ActivityPlaylistBinding;
import com.aaronmaxlab.maxplayer.localdb.SqlDbHelper;
import com.aaronmaxlab.maxplayer.models.M3uModel;
import com.aaronmaxlab.maxplayer.subclass.DeviceUtils;
import com.aaronmaxlab.maxplayer.subclass.TvFocusHelper;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class PlaylistActivity extends AppCompatActivity {
    ActivityPlaylistBinding binding;
    RecyclerView recyclerView;
    private boolean isTv = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPlaylistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        recyclerView = binding.favRecyclerView;
        isTv = DeviceUtils.isTv(this);

       String name = "Favorites";

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        try {
            Intent intent = getIntent();
             name = intent.getStringExtra("catName");
        } catch (Exception e) {
            Log.d("Intent Error", e.toString());
        }

        ArrayList<M3uModel> arrayList = getAllDataList(name);

        if(isTv) {

            TvFocusHelper.applyFocus(binding.backButton);

            assert binding.backButton != null;
            binding.backButton.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        }else {
            assert binding.playlistToolbar != null;
            binding.playlistToolbar.setTitle(name);

            assert binding.playlistToolbar != null;
            binding.playlistToolbar.setNavigationOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
        }

        if(!arrayList.isEmpty()){
            binding.emptyView.setVisibility(GONE);
        }else{
            binding.emptyView.setVisibility(VISIBLE);
        }

        recyclerView.setLayoutManager(new GridLayoutManager(this,  isTv ? 4 : 3));
        FavlistAdapter adapter = new FavlistAdapter( arrayList, this);
        recyclerView.setAdapter(adapter);
    }

    public ArrayList<M3uModel> getAllDataList(String catName) {
        ArrayList<M3uModel> list = new ArrayList<>();
        SqlDbHelper database = new SqlDbHelper(this);
        Cursor cursor = database.readAllData();

        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("channelName"));
                    String icon = cursor.getString(cursor.getColumnIndexOrThrow("channelIcon"));
                    String url = cursor.getString(cursor.getColumnIndexOrThrow("channelUrl"));
                    String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                    if(catName.trim().equalsIgnoreCase(category)) {
                        M3uModel model = new M3uModel(name, url, icon, 0);
                        list.add(model);
                    }

                } while (cursor.moveToNext());

                cursor.close();
            }
        } catch (Error e) {
            Log.d("Db Error", e.toString());
        }

        return list;
    }
}