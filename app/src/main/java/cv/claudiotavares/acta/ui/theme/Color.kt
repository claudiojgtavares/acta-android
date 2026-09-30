package cv.claudiotavares.acta.ui.theme

import androidx.compose.ui.graphics.Color

// ACTA Executive Palette
val Slate950 = Color(0xFF0F172A)
val Slate900 = Color(0xFF0F172A)
val Slate800 = Color(0xFF1E293B)
val Slate700 = Color(0xFF334155)
val Slate600 = Color(0xFF475569)
val Slate500 = Color(0xFF64748B)
val Slate400 = Color(0xFF94A3B8)
val Slate200 = Color(0xFFE2E8F0)
val Slate100 = Color(0xFFF1F5F9)
val Slate50 = Color(0xFFF8FAFC)

val ActaBlue = Color(0xFF1E40AF)
val ActaBlueLight = Color(0xFF3B82F6)
val ActaBlueDark = Color(0xFF172554)
val ActaCyan = Color(0xFF0284C7)

val RecordingRed = Color(0xFFDC2626)
val RecordingRedLight = Color(0xFFFEE2E2)
val WarningAmber = Color(0xFFD97706)
val WarningAmberLight = Color(0xFFFEF3C7)
val SuccessEmerald = Color(0xFF059669)
val SuccessEmeraldLight = Color(0xFFD1FAE5)

// Speaker Color Palette for Diarization
val SpeakerColors = listOf(
  Color(0xFF2563EB), // Blue
  Color(0xFF059669), // Emerald
  Color(0xFF7C3AED), // Violet
  Color(0xFFEA580C), // Orange
  Color(0xFF0891B2), // Cyan
  Color(0xFFBE185D), // Pink
  Color(0xFF4F46E5), // Indigo
  Color(0xFFB45309)  // Amber
)

fun getSpeakerColor(rotulo: String): Color {
  val index = rotulo.filter { it.isDigit() }.toIntOrNull()?.let { (it - 1).coerceAtLeast(0) } ?: 0
  return SpeakerColors[index % SpeakerColors.size]
}
