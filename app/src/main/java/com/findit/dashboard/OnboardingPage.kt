package com.findit.dashboard

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class OnboardingPage(
    @DrawableRes val imageRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    val showLogo: Boolean = false
)