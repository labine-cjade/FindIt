package com.findit.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.findit.dashboard.DashboardItem
import com.findit.dashboard.InventoryManager

class DashboardViewModel : ViewModel() {

    val allItems = InventoryManager.allItems.asLiveData()

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