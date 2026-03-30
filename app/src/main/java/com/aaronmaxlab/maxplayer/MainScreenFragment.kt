package com.aaronmaxlab.maxplayer

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.aaronmaxlab.filepicker.DocumentPickerFragment
import com.aaronmaxlab.maxplayer.databinding.FragmentMainScreenBinding
import com.aaronmaxlab.maxplayer.localdb.SqlDbHelper
import com.aaronmaxlab.maxplayer.preferences.AboutActivity
import com.aaronmaxlab.maxplayer.subclass.TvFocusHelper

class MainScreenFragment : Fragment(R.layout.fragment_main_screen) {
    private lateinit var binding: FragmentMainScreenBinding

    private lateinit var documentTreeOpener: ActivityResultLauncher<Uri?>
    private lateinit var filePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var playerLauncher: ActivityResultLauncher<Intent>

    private var firstRun = false
    private var returningFromPlayer = false

    private var prev = ""
    private var prevData: String? = null
    private var lastPath = ""

    private lateinit var database: SqlDbHelper

    private var isTv = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firstRun = savedInstanceState == null
        database = SqlDbHelper(context)
        documentTreeOpener = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) {
            it?.let { root ->
                requireContext().contentResolver.takePersistableUriPermission(
                    root, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                saveChoice("doc", root.toString())

                val i = Intent(context, FilePickerActivity::class.java)
                i.putExtra("skip", FilePickerActivity.DOC_PICKER)
                i.putExtra("root", root.toString())
                filePickerLauncher.launch(i)
            }
        }
        filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_OK) {
                return@registerForActivityResult
            }
            it.data?.getStringExtra("last_path")?.let { path ->
                lastPath = path
            }
            it.data?.getStringExtra("path")?.let { path ->
                playFile(path)
            }
        }
        playerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // we don't care about the result but remember that we've been here
            returningFromPlayer = true
            Log.v(TAG, "returned from player ($it)")
        }


         isTv =
            resources.configuration.uiMode and
                    Configuration.UI_MODE_TYPE_MASK ==
                    Configuration.UI_MODE_TYPE_TELEVISION


    }

    fun getDefaultSharedPreferencesCompat(context: Context): SharedPreferences {
        return context.getSharedPreferences(
            context.packageName + "_preferences",
            Context.MODE_PRIVATE
        )
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentMainScreenBinding.bind(view)

        Utils.handleInsetsAsPadding(binding.root)

        binding.docBtn.setOnClickListener {
            try {
                documentTreeOpener.launch(null)
            } catch (e: ActivityNotFoundException) {
                // Android TV doesn't come with a document picker and certain versions just throw
                // instead of handling this gracefully
                binding.docBtn.isEnabled = false
            }
        }
//        binding.urlBtn.setOnClickListener {
//            saveChoice("url")
//            val helper = Utils.OpenUrlDialog(requireContext())
//            with (helper) {
//                builder.setPositiveButton(R.string.dialog_ok) { _, _ ->
//                    playFile(helper.text)
//                }
//                builder.setNegativeButton(R.string.dialog_cancel) { dialog, _ -> dialog.cancel() }
//                create().show()
//            }
//
//        }

        binding.urlBtn.setOnClickListener {
            Utils.OpenUrlDialog(
                context = requireContext(),
                onOpen = { playlist, url ->
                    playFile(url, playlistName = playlist)
                    requireActivity().finish()
                },
                onCancel = {
                    Log.d("Dialog", "User cancelled open URL")
                }
            ).show()
        }


        binding.urlBtn2.setOnClickListener {
            Utils.OpenUrlDialog(
                isVideoMode = true,
                headerText = "Enter Video URL",
                context = requireContext(),
                onOpen = { playlist, url ->
                    playFile(url, isVideoUrl = true)
                    requireActivity().finish()
                },
                onCancel = {
                    Log.d("Dialog", "User cancelled open URL")
                }
            ).show()
        }



        binding.filepickerBtn.setOnClickListener {
           // saveChoice("file")
            val i = Intent(context, FilePickerActivity::class.java)
            i.putExtra("skip", FilePickerActivity.FILE_PICKER)
            if (lastPath != "")
                i.putExtra("default_path", lastPath)
            filePickerLauncher.launch(i)
        }

        binding.settingsBtn.setOnClickListener {
            startActivity(Intent(context, LearningActivity::class.java))
        }


        if (BuildConfig.DEBUG) {
            binding.settingsBtn.setOnLongClickListener { showDebugMenu(); true }
        }

        onConfigurationChanged(view.resources.configuration)

        binding.switch1.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                saveChoice("")
            }
        }


        if(isTv){
            TvFocusHelper.applyFocus( binding.urlBtn, binding.urlBtn2, binding.filepickerBtn, binding.settingsBtn, binding.aboutBtn, binding.licenseInfo)

            binding.aboutBtn?.setOnClickListener {
                val intent = Intent(requireContext(), LicensesActivity::class.java)
                startActivity(intent)
            }

            binding.licenseInfo?.setOnClickListener {
                val intent = Intent(requireContext(), AboutActivity::class.java)
                startActivity(intent)
            }
        }

    }

    private fun showDebugMenu() {
        assert(BuildConfig.DEBUG)
        val context = requireContext()
        with (AlertDialog.Builder(context)) {
            setItems(DEBUG_ACTIVITIES) { dialog, idx ->
                dialog.dismiss()
                val intent = Intent(Intent.ACTION_MAIN)
                intent.setClassName(context, "${context.packageName}.${DEBUG_ACTIVITIES[idx]}")
                startActivity(intent)
            }
            create().show()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // phone screens are too small to show the action buttons alongside the logo
        if (!Utils.isXLargeTablet(requireContext())) {
           // binding.logo.isVisible = newConfig.orientation != Configuration.ORIENTATION_LANDSCAPE
        }
    }

    override fun onResume() {
        super.onResume()
        if (firstRun) {
            restoreChoice()
        } else if (returningFromPlayer) {
            restoreChoice(prev, prevData)
        }
        firstRun = false
        returningFromPlayer = false
    }



    private fun saveChoice(type: String, data: String? = null) {
        val sharedPreferences = getDefaultSharedPreferencesCompat(requireContext())

        if (type.isEmpty()) {
            // Clear saved choice
            prev = ""
            prevData = null
            lastPath = ""

            with(sharedPreferences.edit()) {
                remove("MainScreenFragment_remember")
                remove("MainScreenFragment_remember_data")
                apply()
            }
            return
        }

        // Save normally
        if (prev != type)
            lastPath = ""
        prev = type
        prevData = data

        with(sharedPreferences.edit()) {
            putString("MainScreenFragment_remember", type)
            if (data == null)
                remove("MainScreenFragment_remember_data")
            else
                putString("MainScreenFragment_remember_data", data)
            apply()
        }
    }


    private fun restoreChoice() {
        val sharedPreferences = getDefaultSharedPreferencesCompat(requireContext())
        restoreChoice(
            sharedPreferences.getString("MainScreenFragment_remember", "") ?: "",
            sharedPreferences.getString("MainScreenFragment_remember_data", "") ?: ""
        )
    }


    private fun restoreChoice(type: String, data: String?) {
        when (type) {
            "doc" -> {
                val uri = Uri.parse(data)
                // check that we can still access the folder
                if (!DocumentPickerFragment.isTreeUsable(requireContext(), uri))
                    return

                val i = Intent(context, FilePickerActivity::class.java)
                i.putExtra("skip", FilePickerActivity.DOC_PICKER)
                i.putExtra("root", uri.toString())
                if (lastPath != "")
                    i.putExtra("default_path", lastPath)
                    binding.switch1.isChecked = true
                filePickerLauncher.launch(i)
            }
            "url" -> binding.urlBtn.callOnClick()
            "file" -> binding.filepickerBtn.callOnClick()
        }
    }

    private fun playFile(filepath: String, isVideoUrl: Boolean = false, playlistName:String = "Video Playlist") {
        val i: Intent
        if (filepath.startsWith("content://")) {
            i = Intent(Intent.ACTION_VIEW, filepath.toUri())
        } else {
            i = Intent()
            i.putExtra("filepath", filepath)
            i.putExtra("isVideoUrl", isVideoUrl)
            i.putExtra("playlistName", playlistName)
        }
        i.setClass(requireContext(), MPVActivity::class.java)
        playerLauncher.launch(i)

    }

    companion object {
        private const val TAG = "maxplayer"

        // list of debug or testing activities that can be launched
        private val DEBUG_ACTIVITIES = arrayOf(
            "IntentTestActivity",
            "CodecInfoActivity"
        )
    }
}
