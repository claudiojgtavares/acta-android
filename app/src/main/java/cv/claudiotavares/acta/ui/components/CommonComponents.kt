package cv.claudiotavares.acta.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cv.claudiotavares.acta.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

fun formatMs(ms: Long): String {
  val totalSec = ms / 1000
  val min = totalSec / 60
  val sec = totalSec % 60
  return String.format(Locale.getDefault(), "%02d:%02d", min, sec)
}

fun formatDateTimePt(timestamp: Long): String {
  val sdf = SimpleDateFormat("dd 'de' MMMM 'de' yyyy, HH:mm", Locale("pt", "PT"))
  return sdf.format(Date(timestamp))
}

@Composable
fun StatusBadge(
  estado: String,
  modifier: Modifier = Modifier
) {
  val (bg, fg, label) = when (estado.lowercase()) {
    "agendada" -> Triple(Slate100, Slate700, "Agendada")
    "em_curso" -> Triple(RecordingRedLight, RecordingRed, "Em Curso")
    "concluida" -> Triple(SuccessEmeraldLight, SuccessEmerald, "Concluída")
    "rascunho" -> Triple(Slate100, Slate600, "Rascunho")
    "em_revisao" -> Triple(WarningAmberLight, WarningAmber, "Em Revisão")
    "aprovada" -> Triple(SuccessEmeraldLight, SuccessEmerald, "Aprovada")
    "por atribuir" -> Triple(RecordingRedLight, RecordingRed, "Por Atribuir")
    "atribuida" -> Triple(Slate100, Slate700, "Atribuída")
    else -> Triple(Slate100, Slate700, estado)
  }

  Surface(
    color = bg,
    shape = RoundedCornerShape(12.dp),
    modifier = modifier
  ) {
    Text(
      text = label,
      color = fg,
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    )
  }
}

@Composable
fun SpeakerBadge(
  rotulo: String,
  nomeParticipante: String?,
  onClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val color = getSpeakerColor(rotulo)
  val displayName = nomeParticipante ?: rotulo

  Surface(
    color = color.copy(alpha = 0.12f),
    shape = RoundedCornerShape(16.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f)),
    modifier = modifier.then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
      Box(
        modifier = Modifier
          .size(8.dp)
          .clip(CircleShape)
          .background(color)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = displayName,
        color = color,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold
      )
      if (onClick != null) {
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "✎",
          color = color,
          fontSize = 11.sp
        )
      }
    }
  }
}

/**
 * MANDATORY VISUAL ACTIVE RECORDING INDICATOR (ECRÃ 5)
 * "indicador visual de gravação ativa (obrigatório, nunca opcional)"
 */
@Composable
fun ActiveRecordingPulsingIndicator(
  isPaused: Boolean,
  elapsedTimeMs: Long,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = if (isPaused) 1f else 1.35f,
    animationSpec = infiniteRepeatable(
      animation = tween(600, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "rec_pulse"
  )

  Surface(
    color = if (isPaused) WarningAmberLight else RecordingRedLight,
    shape = RoundedCornerShape(20.dp),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (isPaused) WarningAmber else RecordingRed
    ),
    modifier = modifier
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
      Icon(
        imageVector = Icons.Default.FiberManualRecord,
        contentDescription = "A gravar",
        tint = if (isPaused) WarningAmber else RecordingRed,
        modifier = Modifier
          .size(16.dp)
          .scale(pulseScale)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = if (isPaused) "PAUSADA" else "REC",
        color = if (isPaused) WarningAmber else RecordingRed,
        fontWeight = FontWeight.Black,
        style = MaterialTheme.typography.labelMedium
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = formatMs(elapsedTimeMs),
        color = Slate900,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.labelLarge
      )
    }
  }
}

@Composable
fun AudioAmplitudeBars(
  amplitude: Float,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.height(28.dp),
    horizontalArrangement = Arrangement.spacedBy(3.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    val barCount = 12
    for (i in 0 until barCount) {
      val factor = (kotlin.math.sin(i * 0.5 + amplitude * 5).toFloat().coerceIn(0.1f, 1f))
      val h = (8.dp + (20.dp * amplitude * factor)).coerceIn(4.dp, 26.dp)
      Box(
        modifier = Modifier
          .width(3.dp)
          .height(h)
          .clip(RoundedCornerShape(2.dp))
          .background(if (amplitude > 0.05f) RecordingRed else Slate400)
      )
    }
  }
}
