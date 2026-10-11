package com.findit.dashboard.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Colors taken from the app's XML (colors.xml and the bg_* drawables).
val Navy = Color(0xFF1B4965)          // buttons, mic and play circles
val Ink = Color(0xFF102A43)           // headings on the blue background
val GradientTop = Color(0xFF2B6B9B)   // bg_main_gradient (top)
val GradientMid = Color(0xFF72B2DC)
val GradientBottom = Color(0xFFCBE6F8)
val DropdownBlue = Color(0xFFA9CDE7)  // floor dropdown
val ConfirmBlue = Color(0xFFC5E3F6)   // confirm dialog card
val LabelGray = Color(0xFF6B7280)
val FieldBorder = Color(0xFFD1D5DB)
val HintGray = Color(0xFF9CA3AF)
val OutlineGray = Color(0xFF4A4A4A)   // "See Details" outline
val HeroPill = Color(0xCC243B53)      // "See details" pill on the hero card
val FabBlack = Color(0x80000000)      // translucent bottom buttons
val CameraBackground = Color(0xFF1B262C)
val CameraHeader = Color(0xFF3A6073)
val TabGray = Color(0xFFC4C4C4)

/** The blue background used by the Home screen. */
val MainGradient = Brush.verticalGradient(listOf(GradientTop, GradientMid, GradientBottom))
