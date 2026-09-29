package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val RacingDarkColorScheme = darkColorScheme(
  primary = NeonCyan,
  onPrimary = CarbonBlack,
  primaryContainer = CarbonSurfaceVariant,
  onPrimaryContainer = NeonCyan,
  secondary = NeonAmber,
  onSecondary = CarbonBlack,
  secondaryContainer = CarbonSurfaceVariant,
  onSecondaryContainer = NeonAmber,
  tertiary = ApexGreen,
  onTertiary = CarbonBlack,
  background = CarbonBlack,
  onBackground = TextPrimary,
  surface = CarbonSurface,
  onSurface = TextPrimary,
  surfaceVariant = CarbonSurfaceVariant,
  onSurfaceVariant = TextSecondary,
  outline = CarbonBorder,
  error = RedlineRed,
  onError = TextPrimary
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit
) {
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        window.statusBarColor = CarbonBlack.toArgb()
        window.navigationBarColor = CarbonBlack.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
      }
    }
  }

  MaterialTheme(
    colorScheme = RacingDarkColorScheme,
    typography = Typography,
    content = content
  )
}
