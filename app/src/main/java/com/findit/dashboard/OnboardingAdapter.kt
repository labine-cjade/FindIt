package com.findit.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.findit.dashboard.databinding.ItemOnboardingPageBinding

class OnboardingAdapter(
    private val pages: List<OnboardingPage>
) : RecyclerView.Adapter<OnboardingAdapter.PageViewHolder>() {

    inner class PageViewHolder(val binding: ItemOnboardingPageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val binding = ItemOnboardingPageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val page = pages[position]
        holder.binding.imgIllustration.setImageResource(page.imageRes)
        holder.binding.txtTitle.setText(page.titleRes)
        holder.binding.txtSubtitle.setText(page.subtitleRes)
        holder.binding.txtLogo.visibility =
            if (page.showLogo) android.view.View.VISIBLE else android.view.View.GONE
    }

    override fun getItemCount(): Int = pages.size
}