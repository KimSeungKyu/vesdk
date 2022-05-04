package com.test.vesdktest

import android.Manifest
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import ly.img.android.pesdk.VideoEditorSettingsList
import ly.img.android.pesdk.backend.model.constant.OutputMode
import ly.img.android.pesdk.backend.model.state.LoadSettings
import ly.img.android.pesdk.backend.model.state.SaveSettings
import ly.img.android.pesdk.ui.activity.VideoEditorActivityResultContract
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    val TAG = javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun getVideo(view: View) {
        requestPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) {
            requestGetVideo.launch("video/*")
        }
    }

    val requestGetVideo = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        runCatching {
            VideoEditorSettingsList(saveUriPermissions = true)
                .configure<LoadSettings> {
                    it.source = uri
                }
                .configure<SaveSettings> {
                    val tempDir = createTempDir()
                    val resultFile = File(tempDir, "result.mp4")
                    it.setOutputToUri(resultFile.toUri())
                    it.outputMode = OutputMode.EXPORT_IF_NECESSARY
                }
        }.onSuccess {
            requestVideoEditor.launch(it)
            it.release()
        }.onFailure {
            it.printStackTrace()
            Toast.makeText(this, "VideoEditor Error", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestVideoEditor = registerForActivityResult(VideoEditorActivityResultContract()) { result ->
        Log.i(TAG, "Source video is located here ${result.sourceUri}")

    }

    fun Context.createTempDir(): File {
        val time = SimpleDateFormat("yyyy-MM-dd-hh-mm-ss", Locale.getDefault()).format(Date(System.currentTimeMillis()))
        return File(filesDir, "temp-$time").apply {
            if (!exists()) mkdir()
        }
    }
}