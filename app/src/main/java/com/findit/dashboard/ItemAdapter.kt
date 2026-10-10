package com.findit.dashboard

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.io.File

data class DashboardItem(
    val id: Int = 0,
    val title: String,
    val location: String,
    val date: String,
    val photoPath: String? = null
)

class ItemAdapter(private val onDetailsClick: (DashboardItem) -> Unit) :
    ListAdapter<DashboardItem, ItemAdapter.ItemViewHolder>(DiffCallback()) {

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgItem: ImageView = itemView.findViewById(R.id.imgItem)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val btnDetails: View = itemView.findViewById(R.id.btnDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.tvTitle.text = currentItem.title
        holder.tvLocation.text = currentItem.location
        holder.tvDate.text = currentItem.date

        if (!currentItem.photoPath.isNullOrEmpty()) {
            val file = File(currentItem.photoPath)
            if (file.exists()) {
                holder.imgItem.setImageURI(Uri.fromFile(file))
            } else {
                holder.imgItem.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        } else {
            holder.imgItem.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.btnDetails.setOnClickListener { onDetailsClick(currentItem) }
    }

    class DiffCallback : DiffUtil.ItemCallback<DashboardItem>() {
        override fun areItemsTheSame(oldItem: DashboardItem, newItem: DashboardItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DashboardItem, newItem: DashboardItem): Boolean {
            return oldItem == newItem
        }
    }
}
