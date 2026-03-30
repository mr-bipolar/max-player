package com.aaronmaxlab.maxplayer;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.aaronmaxlab.maxplayer.databinding.ActivityLicensesBinding;
import com.aaronmaxlab.maxplayer.subclass.DeviceUtils;
import com.aaronmaxlab.maxplayer.subclass.TvFocusHelper;

public class LicensesActivity extends AppCompatActivity {

    ActivityLicensesBinding binding;
    private boolean isTv = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLicensesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        isTv = DeviceUtils.isTv(this);

        if (isTv) {

            assert binding.backButton != null;
            binding.backButton.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

            TvFocusHelper.applyFocus(binding.backButton, binding.btnPrivacy, binding.btnRateUs, binding.btnShare);

            assert binding.btnPrivacy != null;
            binding.btnPrivacy.setOnClickListener(v -> {
              startActivity(new Intent(Intent.ACTION_VIEW,
                      Uri.parse("https://sites.google.com/view/max-player-privacy-policy")));
          });

            assert binding.btnRateUs != null;
            binding.btnRateUs.setOnClickListener(v -> {
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

            assert binding.btnShare != null;
            binding.btnShare.setOnClickListener(v -> {
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

        } else {

            assert binding.licensesMaterialToolbar != null;
            binding.licensesMaterialToolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        }

        assert binding.txtVersion != null;
        try {
            String v = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            binding.txtVersion.setText("Version " + v);
        } catch (Exception e) {
            binding.txtVersion.setText("Version 2.6");
        }
    }
}