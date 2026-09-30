package cv.claudiotavares.acta.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
  primary = ActaBlueLight,
  onPrimary = Color.White,
  primaryContainer = ActaBlueDark,
  onPrimaryContainer = Color.White,
  secondary = WarningAmber,
  onSecondary = Color.White,
  secondaryContainer = Slate800,
  onSecondaryContainer = WarningAmberLight,
  tertiary = SuccessEmerald,
  background = Slate950,
  onBackground = Slate100,
  surface = Slate900,
  onSurface = Slate100,
  surfaceVariant = Slate800,
  onSurfaceVariant = Slate200,
  outline = Slate600
)

private val LightColorScheme = lightColorScheme(
  primary = ActaBlue,
  onPrimary = Color.White,
  primaryContainer = Slate100,
  onPrimaryContainer = ActaBlueDark,
  secondary = WarningAmber,
  onSecondary = Color.White,
  secondaryContainer = WarningAmberLight,
  onSecondaryContainer = Slate900,
  tertiary = SuccessEmerald,
  background = Slate50,
  onBackground = Slate900,
  surface = Color.White,
  onSurface = Slate900,
  surfaceVariant = Slate100,
  onSurfaceVariant = Slate700,
  outline = Slate200
)

@Composable
fun ActaTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Keep consistent executive brand styling
  content: @Composable () -> Unit
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
