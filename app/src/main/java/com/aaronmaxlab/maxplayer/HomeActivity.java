package com.aaronmaxlab.maxplayer;

import android.app.UiModeManager;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.aaronmaxlab.maxplayer.databinding.ActivityHomeBinding;
import com.aaronmaxlab.maxplayer.subclass.TvFocusHelper;


public class HomeActivity extends AppCompatActivity {
    private boolean isTv = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivityHomeBinding binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        UiModeManager uiModeManager =
                (UiModeManager) getSystemService(UI_MODE_SERVICE);


        if (uiModeManager != null) {
            isTv = uiModeManager.getCurrentModeType()
                    == Configuration.UI_MODE_TYPE_TELEVISION;
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new MainScreenFragment())
                    .commit();
        }

        if(isTv){

            TvFocusHelper.applyFocus(binding.backBtn);
            assert binding.backBtn != null;
            binding.backBtn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        }else{
            ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });

            assert binding.homeMaterialToolbar != null;
            binding.homeMaterialToolbar.setNavigationOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
        }


    }




}