package com.findit.dashboard

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InventoryManagerTest {

    private fun sample(title: String) = DashboardItem(
        title = title,
        location = locationText("Desk"),
        date = "Feb 23, 2026 11:43 AM"
    )

    @Test
    fun addItem_putsNewestItemFirst() = runBlocking {
        InventoryManager.addItem(sample("First test item"))
        InventoryManager.addItem(sample("Second test item"))

        val items = InventoryManager.allItems.first()
        assertEquals("Second test item", items[0].title)
    }

    @Test
    fun updateItem_changesTitleAndKeepsAudio() = runBlocking {
        InventoryManager.addItem(sample("Before update"))
        val added = InventoryManager.allItems.first().first { it.title == "Before update" }

        InventoryManager.updateItem(
            added.copy(title = "After update", audioPath = "/tmp/message.m4a")
        )

        val updated = InventoryManager.allItems.first().first { it.id == added.id }
        assertEquals("After update", updated.title)
        assertEquals("/tmp/message.m4a", updated.audioPath)
    }

    @Test
    fun newItem_hasNoAudioAndDefaultFloor() {
        val item = sample("No audio")
        assertNull(item.audioPath)
        assertEquals("1st Floor", item.floor)
    }

    @Test
    fun locationHelpers_addAndRemovePrefix() {
        val item = sample("Keys")
        assertEquals("Location: Desk", item.location)
        assertEquals("Desk", item.plainLocation())
        assertTrue(Floors.choices.first() == Floors.ALL)
    }
}
