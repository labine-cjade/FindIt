package com.example.dashboardfindit

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class SearchActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_AUTO_START_VOICE = "extra_auto_start_voice"
    }

    private lateinit var etSearchInput: EditText

    private val voiceRecognitionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val spokenText: ArrayList<String>? =
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognizedText = spokenText?.firstOrNull()
            if (!recognizedText.isNullOrEmpty()) {
                etSearchInput.setText(recognizedText)
                etSearchInput.setSelection(recognizedText.length)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        etSearchInput = findViewById(R.id.etSearchInput)

        val btnBack: ImageButton = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        val btnVoiceSearch: ImageButton = findViewById(R.id.btnVoiceSearch)
        btnVoiceSearch.setOnClickListener {
            startVoiceRecognition()
        }

        // If launched from the bottom nav mic button, open the voice dialog immediately
        if (intent.getBooleanExtra(EXTRA_AUTO_START_VOICE, false)) {
            startVoiceRecognition()
        }
    }

    private fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak the item name...")
        }

        try {
            voiceRecognitionLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                this,
                "Voice recognition is not available on this device",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
