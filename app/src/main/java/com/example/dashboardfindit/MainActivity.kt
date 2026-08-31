package com.example.dashboardfindit

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnSearchHeader: ImageView = findViewById(R.id.btnSearch)
        btnSearchHeader.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        val btnVoiceSearch: ImageButton = findViewById(R.id.btnVoiceSearch)
        btnVoiceSearch.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        val btnAddItem: ImageButton = findViewById(R.id.btnAddItem)
        btnAddItem.setOnClickListener {
            startActivity(Intent(this, TakePictureActivity::class.java))
        }

        val viewPagerHero: ViewPager2 = findViewById(R.id.viewPagerHero)
        val btnNextHero: ImageButton = findViewById(R.id.btnNextHero)

        val heroImages = listOf(
            android.R.drawable.ic_menu_gallery,
            android.R.drawable.ic_menu_camera,
            android.R.drawable.ic_menu_compass
        )
        viewPagerHero.adapter = HeroAdapter(heroImages)

        btnNextHero.setOnClickListener {
            val totalItems = viewPagerHero.adapter?.itemCount ?: 0
            if (totalItems > 0) {
                val nextItem = (viewPagerHero.currentItem + 1) % totalItems
                viewPagerHero.setCurrentItem(nextItem, true)
            }
        }

        val spnFloor: Spinner = findViewById(R.id.spnFloor)
        val floors = arrayOf("All Floors", "1st Floor", "2nd Floor", "3rd Floor")
        spnFloor.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, floors)

        val rvAddedItems: RecyclerView = findViewById(R.id.rvAddedItems)
        rvAddedItems.layoutManager = GridLayoutManager(this, 2)
        val sampleItems = listOf(
            DashboardItem("Blue Tumbler", "Location: 1st Floor", "Feb 23, 2026 10:15 AM"),
            DashboardItem("Black Backpack", "Location: 2nd Floor", "Feb 23, 2026 11:30 AM"),
            DashboardItem("Wireless Mouse", "Location: 3rd Floor", "Feb 22, 2026 02:45 PM"),
            DashboardItem("Keys with Lanyard", "Location: 1st Floor", "Feb 21, 2026 08:00 AM")
        )
        rvAddedItems.adapter = ItemAdapter(sampleItems) { item ->
            showItemDetailsDialog(item)
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
                setBackgroundColor(Color.parseColor("#243B53"))
                layoutParams = LinearLayout.LayoutParams(barWidthPx, (heightDp * resources.displayMetrics.density).toInt()).apply {
                    marginEnd = barMarginPx
                }
            }
            container.addView(bar)
        }
    }

    private fun showItemDetailsDialog(item: DashboardItem) {
        val (dialog, detailsView) = createBottomSheetDialog(R.layout.dialog_item_details)

        detailsView.findViewById<TextView>(R.id.tvDetailTitle).text = item.title
        detailsView.findViewById<TextView>(R.id.tvDetailLocation).text = item.location
        detailsView.findViewById<TextView>(R.id.tvDetailDate).text = item.date
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
        addWaveformBars(updateView.findViewById(R.id.waveformUpdate), barCount = 22)

        updateView.findViewById<ImageButton>(R.id.btnCloseUpdate).setOnClickListener {
            dialog.dismiss()
        }

        updateView.findViewById<ImageButton>(R.id.btnLocationMic).setOnClickListener {
            Toast.makeText(this, "Voice input coming soon", Toast.LENGTH_SHORT).show()
        }
        updateView.findViewById<ImageButton>(R.id.btnPlayUpdate).setOnClickListener {
            Toast.makeText(this, "Playback coming soon", Toast.LENGTH_SHORT).show()
        }
        updateView.findViewById<LinearLayout>(R.id.btnUpdatePhoto).setOnClickListener {
            Toast.makeText(this, "Photo update coming soon", Toast.LENGTH_SHORT).show()
        }

        updateView.findViewById<Button>(R.id.btnSaveUpdate).setOnClickListener {
            showConfirmChangesDialog(dialog)
        }

        dialog.show()
    }

    private fun showConfirmChangesDialog(updateDialog: Dialog) {
        val confirmDialog = Dialog(this)
        confirmDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        confirmDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val confirmView = layoutInflater.inflate(R.layout.dialog_confirm_changes, null)
        confirmDialog.setContentView(confirmView)

        confirmView.findViewById<Button>(R.id.btnConfirmNo).setOnClickListener {
            confirmDialog.dismiss()
        }

        confirmView.findViewById<Button>(R.id.btnConfirmYes).setOnClickListener {
            confirmDialog.dismiss()
            updateDialog.dismiss()
            Toast.makeText(this, "Item updated!", Toast.LENGTH_SHORT).show()
        }

        confirmDialog.show()
    }
}
