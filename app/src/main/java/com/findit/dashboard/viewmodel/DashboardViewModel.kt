package com.findit.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.Flow
import com.findit.dashboard.DashboardItem
import com.findit.dashboard.InventoryManager

class DashboardViewModel : ViewModel() {

    // LiveData is still used by SearchActivity (XML screen).
    val allItems = InventoryManager.allItems.asLiveData()

    // Compose screens collect this one with collectAsState().
    val itemsFlow: Flow<List<DashboardItem>> = InventoryManager.allItems

    fun addItem(item: DashboardItem) {
        InventoryManager.addItem(item)
    }

    fun updateItem(item: DashboardItem) {
        InventoryManager.updateItem(item)
    }

    fun deleteItem(item: DashboardItem) {
        InventoryManager.deleteItem(item)
    }
}