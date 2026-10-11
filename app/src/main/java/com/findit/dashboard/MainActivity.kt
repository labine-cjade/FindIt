package com.findit.dashboard

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.findit.dashboard.ui.components.CardMode
import com.findit.dashboard.ui.components.ItemCardSheet
import com.findit.dashboard.ui.components.currentTimestamp
import com.findit.dashboard.ui.home.HomeScreen
import com.findit.dashboard.ui.theme.DashboardFindItTheme
import com.findit.dashboard.viewmodel.DashboardViewModel

/** The Dashboard screen, written in Jetpack Compose. */
class MainActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_AUTO_START_VOICE = "extra_auto_start_voice"
    }

    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Keep status bar transparent over blue/gradient theme
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))

        setContent {
            DashboardFindItTheme {
                val items by viewModel.itemsFlow.collectAsState(initial = emptyList())
                var detailsItem by remember { mutableStateOf<DashboardItem?>(null) }

                HomeScreen(
                    allItems = items,
                    onSearchClick = {
                        startActivity(Intent(this, SearchActivity::class.java))
                    },
                    onVoiceSearchClick = {
                        val intent = Intent(this, SearchActivity::class.java)
                        intent.putExtra(EXTRA_AUTO_START_VOICE, true)
                        startActivity(intent)
                    },
                    onAddClick = {
                        startActivity(Intent(this, TakePictureActivity::class.java))
                    },
                    onDetailsClick = { detailsItem = it }
                )

                detailsItem?.let { item ->
                    key(item.id) {
                        ItemCardSheet(
                            initialMode = CardMode.DETAILS,
                            photoPath = item.photoPath,
                            initialName = item.title,
                            initialLocation = item.plainLocation(),
                            initialAudioPath = item.audioPath,
                            dateText = item.date,
                            onDismiss = { detailsItem = null },
                            onSave = { name, location, audioPath ->
                                viewModel.updateItem(
                                    item.copy(
                                        title = name,
                                        location = locationText(location),
                                        date = currentTimestamp(),
                                        audioPath = audioPath
                                    )
                                )
                                detailsItem = null
                                Toast.makeText(this, "Item updated!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}