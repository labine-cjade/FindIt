package com.example.dashboardfindit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class DashboardItem(
    val title: String,
    val location: String,
    val date: String
)

class ItemAdapter(private val itemsList: List<DashboardItem>) :
    RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val currentItem = itemsList[position]
        holder.tvTitle.text = currentItem.title
        holder.tvLocation.text = currentItem.location
        holder.tvDate.text = currentItem.date
    }

    override fun getItemCount(): Int = itemsList.size
}