package com.aaronmaxlab.maxplayer;

import android.app.UiModeManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.aaronmaxlab.maxplayer.databinding.ActivitySplashScreenBinding;

public class SplashScreen extends AppCompatActivity {
    ActivitySplashScreenBinding binding;
    SharedPreferences sharedPreferences;
    private boolean isTv = false;
    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_AD_SHOWN = "ad_shown";
    // 5 seconds timeout
    private final Handler handler = new Handler(Looper.getMainLooper());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        final ImageView logo = binding.maxLogo;

        UiModeManager uiModeManager =
                (UiModeManager) getSystemService(UI_MODE_SERVICE);


        if (uiModeManager != null) {
            isTv = uiModeManager.getCurrentModeType()
                    == Configuration.UI_MODE_TYPE_TELEVISION;
        }

        // logo
        float startScale = isTv ? 0.5f : 0.4f;
        float endScale   = isTv ? 1.1f : 1.0f;
        long duration    = isTv ? 1600 : 1200;

        logo.setScaleX(startScale);
        logo.setScaleY(startScale);
        logo.setAlpha(0f);

        logo.animate()
                .scaleX(endScale)
                .scaleY(endScale)
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();

        handler.postDelayed(() -> {
            if(!isTv) {
                try {
                    sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    boolean isAddShow = sharedPreferences.getBoolean(KEY_AD_SHOWN, false);
                    if (isAddShow) {
                        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(KEY_AD_SHOWN, false);
                        editor.apply();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, 4000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}