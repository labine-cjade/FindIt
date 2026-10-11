package com.findit.dashboard

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.findit.dashboard.viewmodel.DashboardViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class SearchActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_AUTO_START_VOICE = "extra_auto_start_voice"
    }

    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var itemAdapter: ItemAdapter
    private lateinit var etSearchInput: EditText
    private var allItems: List<DashboardItem> = emptyList()

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
                performSearch(recognizedText)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        etSearchInput = findViewById(R.id.etSearchInput)
        val rvSearchResults: RecyclerView = findViewById(R.id.rvSearchResults)

        rvSearchResults.layoutManager = GridLayoutManager(this, 2)
        itemAdapter = ItemAdapter { item ->
            showItemDetailsDialog(item)
        }
        rvSearchResults.adapter = itemAdapter

        viewModel.allItems.observe(this) { items ->
            allItems = items
            performSearch(etSearchInput.text.toString())
        }

        etSearchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                performSearch(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

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

    private fun performSearch(query: String) {
        val filteredList = if (query.isEmpty()) {
            allItems
        } else {
            allItems.filter { it.title.contains(query, ignoreCase = true) || it.location.contains(query, ignoreCase = true) }
        }
        itemAdapter.submitList(filteredList)
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

    private fun createBottomSheetDialog(layoutRes: Int): Pair<Dialog, View> {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val contentView = layoutInflater.inflate(layoutRes, null)
        dialog.setContentView(contentView)

        dialog.window?.let { window ->
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            window.setGravity(Gravity.BOTTOM)
        }
        return Pair(dialog, contentView)
    }

    private fun addWaveformBars(container: LinearLayout, barCount: Int = 32) {
        container.removeAllViews()
        val barWidthPx = (2 * resources.displayMetrics.density).toInt()
        val barMarginPx = (2 * resources.displayMetrics.density).toInt()
        repeat(barCount) {
            val heightDp = Random.nextInt(6, 28)
            val bar = View(this).apply {
                setBackgroundColor(Color.parseColor("#1B4965"))
                layoutParams = LinearLayout.LayoutParams(barWidthPx, (heightDp * resources.displayMetrics.density).toInt()).apply {
                    marginEnd = barMarginPx
                }
            }
            container.addView(bar)
        }
    }

    private fun loadItemImage(imgView: ImageView, photoPath: String?) {
        if (!photoPath.isNullOrEmpty()) {
            val file = File(photoPath)
            if (file.exists()) {
                imgView.setImageURI(Uri.fromFile(file))
            } else {
                imgView.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        } else {
            imgView.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }

    private fun showItemDetailsDialog(item: DashboardItem) {
        val (dialog, detailsView) = createBottomSheetDialog(R.layout.dialog_item_details)

        detailsView.findViewById<TextView>(R.id.tvDetailTitle).text = item.title
        detailsView.findViewById<TextView>(R.id.tvDetailLocation).text = item.location
        detailsView.findViewById<TextView>(R.id.tvDetailDate).text = item.date
        loadItemImage(detailsView.findViewById(R.id.imgDetailItem), item.photoPath)
        addWaveformBars(detailsView.findViewById(R.id.waveformDetail))

        detailsView.findViewById<ImageButton>(R.id.btnCloseDetails).setOnClickListener {
            dialog.dismiss()
        }

        detailsView.findViewById<Button>(R.id.btnUpdateItem).setOnClickListener {
            dialog.dismiss()
            showUpdateItemDialog(item)
        }

        dialog.show()
    }

    private fun showUpdateItemDialog(item: DashboardItem) {
        val (dialog, updateView) = createBottomSheetDialog(R.layout.dialog_update_item)

        val etItemName: EditText = updateView.findViewById(R.id.etItemName)
        val etLocation: EditText = updateView.findViewById(R.id.etLocation)
        etItemName.setText(item.title)
        etLocation.setText(item.location.removePrefix("Location: "))
        loadItemImage(updateView.findViewById(R.id.imgUpdateItem), item.photoPath)
        addWaveformBars(updateView.findViewById(R.id.waveformUpdate), barCount = 22)

        updateView.findViewById<ImageButton>(R.id.btnCloseUpdate).setOnClickListener {
            dialog.dismiss()
        }

        updateView.findViewById<LinearLayout>(R.id.btnUpdatePhoto).setOnClickListener {
            Toast.makeText(this, "Photo update coming soon", Toast.LENGTH_SHORT).show()
        }
        updateView.findViewById<ImageButton>(R.id.btnLocationMic).setOnClickListener {
            Toast.makeText(this, "Voice input coming soon", Toast.LENGTH_SHORT).show()
        }
        updateView.findViewById<ImageButton>(R.id.btnPlayUpdate).setOnClickListener {
            Toast.makeText(this, "Playback coming soon", Toast.LENGTH_SHORT).show()
        }
        updateView.findViewById<ImageButton>(R.id.btnEditMessage).setOnClickListener {
            Toast.makeText(this, "Message edit coming soon", Toast.LENGTH_SHORT).show()
        }

        updateView.findViewById<Button>(R.id.btnSaveUpdate).setOnClickListener {
            showConfirmChangesDialog(dialog, item, etItemName, etLocation)
        }

        dialog.show()
    }

    private fun showConfirmChangesDialog(
        updateDialog: Dialog,
        item: DashboardItem,
        etItemName: EditText,
        etLocation: EditText
    ) {
        val confirmDialog = Dialog(this)
        confirmDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        confirmDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val confirmView = layoutInflater.inflate(R.layout.dialog_confirm_changes, null)
        confirmDialog.setContentView(confirmView)

        confirmView.findViewById<Button>(R.id.btnConfirmNo).setOnClickListener {
            confirmDialog.dismiss()
        }

        confirmView.findViewById<Button>(R.id.btnConfirmYes).setOnClickListener {
            val enteredName = etItemName.text.toString().trim()
            val enteredLocation = etLocation.text.toString().trim()

            if (enteredName.isEmpty() || enteredLocation.isEmpty()) {
                Toast.makeText(this, "Please fill in name and location", Toast.LENGTH_SHORT).show()
                confirmDialog.dismiss()
                return@setOnClickListener
            }

            val timestamp = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(Date())
            val updatedItem = item.copy(
                title = enteredName,
                location = "Location: $enteredLocation",
                date = timestamp
            )
            viewModel.updateItem(updatedItem)

            confirmDialog.dismiss()
            updateDialog.dismiss()
            Toast.makeText(this, "Item updated!", Toast.LENGTH_SHORT).show()
        }

        confirmDialog.show()
    }
}