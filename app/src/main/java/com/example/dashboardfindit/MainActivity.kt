package com.example.dashboardfindit

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Navigation to Search Activity (Top Bar Search Icon)
        val btnSearchHeader: ImageView = findViewById(R.id.btnSearch)
        btnSearchHeader.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        // Navigation to Search Activity (Bottom Voice Search Button)
        // Navigation to Search Activity (Bottom Voice Search Button) — auto-launch voice input
        val btnVoiceSearch: ImageButton = findViewById(R.id.btnVoiceSearch)
        btnVoiceSearch.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java).apply {
                putExtra(SearchActivity.EXTRA_AUTO_START_VOICE, true)
            }
            startActivity(intent)
        }

        // Navigation to Take Picture Activity (Bottom Add Button)
        val btnAddItem: ImageButton = findViewById(R.id.btnAddItem)
        btnAddItem.setOnClickListener {
            startActivity(Intent(this, TakePictureActivity::class.java))
        }

        // Setup Hero ViewPager2 Carousel
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

        // Setup Spinner
        val spnFloor: Spinner = findViewById(R.id.spnFloor)
        val floors = arrayOf("All Floors", "1st Floor", "2nd Floor", "3rd Floor")
        spnFloor.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, floors)

        // Setup RecyclerView
        val rvAddedItems: RecyclerView = findViewById(R.id.rvAddedItems)
        rvAddedItems.layoutManager = GridLayoutManager(this, 2)
        val sampleItems = listOf(
            DashboardItem("Blue Tumbler", "Location: 1st Floor", "Feb 23, 2026 10:15 AM"),
            DashboardItem("Black Backpack", "Location: 2nd Floor", "Feb 23, 2026 11:30 AM"),
            DashboardItem("Wireless Mouse", "Location: 3rd Floor", "Feb 22, 2026 02:45 PM"),
            DashboardItem("Keys with Lanyard", "Location: 1st Floor", "Feb 21, 2026 08:00 AM")
        )
        rvAddedItems.adapter = ItemAdapter(sampleItems)
    }
}
