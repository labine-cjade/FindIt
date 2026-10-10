package com.findit.dashboard

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * A Singleton object that manages the in-memory list of items.
 * This replaces the repository/database layer to keep things simple.
 */
object InventoryManager {

    private val _items = MutableStateFlow<List<DashboardItem>>(emptyList())
    val allItems: Flow<List<DashboardItem>> = _items.asStateFlow()

    fun addItem(item: DashboardItem) {
        _items.update { currentList ->
            // Simple ID generation for in-memory items
            val newItem = item.copy(id = currentList.size + 1)
            listOf(newItem) + currentList
        }
    }

    fun updateItem(item: DashboardItem) {
        _items.update { currentList ->
            currentList.map { if (it.id == item.id) item else it }
        }
    }

    fun deleteItem(item: DashboardItem) {
        _items.update { currentList ->
            currentList.filter { it.id != item.id }
        }
    }
}