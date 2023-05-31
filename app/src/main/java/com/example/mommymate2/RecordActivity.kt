package com.example.mommymate2

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.example.mommymate2.databinding.ActivityRecordBinding
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.io.File
import android.media.AudioFormat
import android.media.AudioRecord
import android.os.Environment
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.IOException


class RecordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecordBinding



    private lateinit var audioRecord: AudioRecord
    var isRecording = false



    private var mediaRecorder : MediaRecorder? = null
     private lateinit var bufferedOutputStream: BufferedOutputStream
//     var outputStream: FileOutputStream? = null
//private var bufferedOutputStream: BufferedOutputStream? = null
//    private var outputStream: FileOutputStream? = null
    private lateinit var outputStream: FileOutputStream

    private val REQUIRED_PERMISSION =
        arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private val REQUEST_CODE_PERMISSION = 199


    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.navigation.setOnItemSelectedListener { menuItem ->

            when (menuItem.itemId) {
                R.id.navigation_main -> {

                    true
                }

                R.id.navigation_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.navigation_setting -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }






        binding.rekamMulai.setOnClickListener {

            binding.rekamOff.visibility = View.GONE
            binding.rekamOn.visibility = View.VISIBLE
            binding.lagiRekam.visibility = View.VISIBLE
                startRecording()
        }

        binding.rekamMati.setOnClickListener {
            binding.rekamOff.visibility = View.VISIBLE
            binding.rekamOn.visibility = View.GONE
            binding.lagiRekam.visibility = View.GONE
            stopRecording()
        }


    }

    private fun allPermissionGranted() = REQUIRED_PERMISSION.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (!allPermissionGranted()) {
                Toast.makeText(
                    this,
                    "Tidak dapat menggunakan audio",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun startRecording() {
        if (allPermissionGranted()) {
            Log.d("Recording", "Start recording")



            val directoryName = "recordings"
            val outputDirectory = File(Environment.getExternalStorageDirectory(), directoryName)
            outputDirectory.mkdirs()

            val outputFile = File(outputDirectory, "recording.wav")

            val sampleRate = 44100
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

            val audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            val data = ByteArray(bufferSize)
            outputStream = FileOutputStream(outputFile)
            bufferedOutputStream = BufferedOutputStream(outputStream)

            audioRecord.startRecording()
            isRecording = true

            Thread {
                while (isRecording) {
                    val bytesRead = audioRecord.read(data, 0, bufferSize)
                    bufferedOutputStream.write(data, 0, bytesRead)
                }

                bufferedOutputStream.close()
                outputStream.close()
            }.start()
            Log.d("Recording", "Recording started")

        } else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSION,REQUEST_CODE_PERMISSION)

        }
    }

    private fun stopRecording() {
        Log.d("Recording", "Stop recording")
        isRecording = false
        audioRecord.stop()
        audioRecord.release()

        try {
            bufferedOutputStream?.close()
            outputStream?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        Log.d("TAG.Recording", "Recording finished")
    }
}



