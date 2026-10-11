package com.findit.dashboard.ui.components

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import java.io.File

/**
 * Records a short voice message to a file and plays it back.
 * Records with MediaRecorder, plays with MediaPlayer (both are standard Android classes).
 */
class VoiceMessageRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null

    /** Starts recording into [file]. Returns false if the microphone could not be started. */
    fun startRecording(file: File): Boolean {
        val newRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        return try {
            newRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            newRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            newRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            newRecorder.setOutputFile(file.absolutePath)
            newRecorder.prepare()
            newRecorder.start()
            recorder = newRecorder
            true
        } catch (e: Exception) {
            newRecorder.release()
            false
        }
    }

    /** Stops recording. Returns false if nothing usable was recorded (for example, stopped too quickly). */
    fun stopRecording(): Boolean {
        val current = recorder ?: return false
        recorder = null
        return try {
            current.stop()
            true
        } catch (e: RuntimeException) {
            false
        } finally {
            current.release()
        }
    }

    /** Plays the file. [onFinished] is called when the message ends. Returns false if it cannot be played. */
    fun startPlaying(path: String, onFinished: () -> Unit): Boolean {
        stopPlaying()
        val newPlayer = MediaPlayer()
        return try {
            newPlayer.setDataSource(path)
            newPlayer.setOnCompletionListener {
                stopPlaying()
                onFinished()
            }
            newPlayer.prepare()
            newPlayer.start()
            player = newPlayer
            true
        } catch (e: Exception) {
            newPlayer.release()
            false
        }
    }

    fun stopPlaying() {
        player?.release()
        player = null
    }

    /** Call when the screen closes. */
    fun release() {
        stopPlaying()
        stopRecording()
    }
}
