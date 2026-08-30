package com.example.dashboardfindit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class HeroAdapter(private val imageList: List<Int>) :
    RecyclerView.Adapter<HeroAdapter.HeroViewHolder>() {

    class HeroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgHero: ImageView = itemView.findViewById(R.id.imgHeroItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hero_slide, parent, false)
        return HeroViewHolder(view)
    }

    override fun onBindViewHolder(holder: HeroViewHolder, position: Int) {
        holder.imgHero.setImageResource(imageList[position])
    }

    override fun getItemCount(): Int = imageList.size
}