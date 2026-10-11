package com.findit.dashboard.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FindItColors = lightColorScheme(
    primary = Navy,
    onPrimary = Color.White,
    secondary = GradientTop,
    background = GradientBottom,
    surface = Color.White,
    onSurface = Color.Black
)

// darkTheme and dynamicColor are kept so older calls still compile,
// but FindIt always uses its own light blue colors from the design.
@Composable
fun DashboardFindItTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FindItColors,
        typography = Typography,
        content = content
    )
}
