package com.findit.dashboard

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.findit.dashboard.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private val dots = mutableListOf<ImageView>()

    private val pages = listOf(
        OnboardingPage(
            imageRes = R.drawable.firstonboard,
            titleRes = R.string.onboarding_title_1,
            subtitleRes = R.string.onboarding_subtitle_1,
            showLogo = true
        ),
        OnboardingPage(
            imageRes = R.drawable.secondonboard,
            titleRes = R.string.onboarding_title_2,
            subtitleRes = R.string.onboarding_subtitle_2
        ),
        OnboardingPage(
            imageRes = R.drawable.thirdonboard,
            titleRes = R.string.onboarding_title_3,
            subtitleRes = R.string.onboarding_subtitle_3
        ),
        OnboardingPage(
            imageRes = R.drawable.lastonboard,
            titleRes = R.string.onboarding_title_4,
            subtitleRes = R.string.onboarding_subtitle_4
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewPager.adapter = OnboardingAdapter(pages)
        setupDots(0)

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateDots(position)
                binding.btnAction.text = if (position == pages.lastIndex) {
                    getString(R.string.get_started)
                } else {
                    getString(R.string.next)
                }
            }
        })

        binding.btnAction.setOnClickListener {
            val current = binding.viewPager.currentItem
            if (current < pages.lastIndex) {
                binding.viewPager.currentItem = current + 1
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }

    private fun setupDots(activePosition: Int) {
        binding.dotsIndicator.removeAllViews()
        dots.clear()
        for (i in pages.indices) {
            val dot = ImageView(this)
            val params = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(6, 0, 6, 0)
            dot.layoutParams = params
            dot.setImageResource(
                if (i == activePosition) R.drawable.dot_active else R.drawable.dot_inactive
            )
            dots.add(dot)
            binding.dotsIndicator.addView(dot)
        }
    }

    private fun updateDots(activePosition: Int) {
        dots.forEachIndexed { index, dot ->
            dot.setImageResource(
                if (index == activePosition) R.drawable.dot_active else R.drawable.dot_inactive
            )
        }
    }
}