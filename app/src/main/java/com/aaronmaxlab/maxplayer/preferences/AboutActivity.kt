package com.aaronmaxlab.maxplayer.preferences

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.preference.PreferenceManager
import com.aaronmaxlab.maxplayer.BuildConfig
import com.aaronmaxlab.maxplayer.MPVLib
import com.aaronmaxlab.maxplayer.MPVLib.MpvLogLevel
import com.aaronmaxlab.maxplayer.R
import com.aaronmaxlab.maxplayer.databinding.ActivityAboutBinding
import com.aaronmaxlab.maxplayer.subclass.DeviceUtils
import com.aaronmaxlab.maxplayer.subclass.TvFocusHelper
import com.google.android.material.color.DynamicColors


class AboutActivity : AppCompatActivity(), MPVLib.LogObserver {
    private lateinit var binding: ActivityAboutBinding
    private var logs = ""
    private var mpvDestroyed = true
    var isTv: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (preferences.getBoolean("material_you_theming", false))
            DynamicColors.applyToActivityIfAvailable(this)
        enableEdgeToEdge()
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isTv = DeviceUtils.isTv(this)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.aboutMaterialToolbar?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        if(isTv){

            TvFocusHelper.applyFocus(binding.backButton)

            binding.backButton?.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }




        logs = "mpv-android ${BuildConfig.VERSION_NAME} / ${BuildConfig.VERSION_CODE} (${BuildConfig.BUILD_TYPE})\n"

        // create mpv context to capture version info from log
        MPVLib.create(this)
        mpvDestroyed = false
        MPVLib.addLogObserver(this)
        MPVLib.init()
    }

    private fun updateLog() {
        runOnUiThread {
            binding.logs.text = logs
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!mpvDestroyed) {
            MPVLib.destroy()
            mpvDestroyed = true
        }
    }

    override fun logMessage(prefix: String, level: Int, text: String) {
        if (prefix != "cplayer")
            return
        if (level == MpvLogLevel.MPV_LOG_LEVEL_V)
         logs += text.replace(oldValue = "/home/Aaron", newValue = "", ignoreCase = true)

        if (text.startsWith("List of enabled features:", true)) {
            // stop receiving log messages and populate text field
            MPVLib.removeLogObserver(this)
            updateLog()
        }
    }
}
