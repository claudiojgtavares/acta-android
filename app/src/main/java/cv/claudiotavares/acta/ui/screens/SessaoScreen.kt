package cv.claudiotavares.acta.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cv.claudiotavares.acta.data.model.ParticipanteEntity
import cv.claudiotavares.acta.data.model.ReuniaoEntity
import cv.claudiotavares.acta.data.model.SegmentoEntity
import cv.claudiotavares.acta.ui.components.*
import cv.claudiotavares.acta.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessaoScreen(
  reuniao: ReuniaoEntity?,
  participantes: List<ParticipanteEntity>,
  segmentos: List<SegmentoEntity>,
  isRecording: Boolean,
  isPaused: Boolean,
  elapsedTimeMs: Long,
  audioAmplitude: Float,
  onPause: () -> Unit,
  onResume: () -> Unit,
  onFinish: () -> Unit,
  onAtribuirOrador: (rotulo: String, participanteId: String) -> Unit,
  onBack: () -> Unit
) {
  var selectedSpeakerForAttribution by remember { mutableStateOf<String?>(null) }
  val listState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()

  // Auto scroll to latest transcribed segment
  LaunchedEffect(segmentos.size) {
    if (segmentos.isNotEmpty()) {
      listState.animateScrollToItem(segmentos.size - 1)
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = reuniao?.titulo ?: "Sessão em Curso",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              maxLines = 1
            )
            Text(
              text = "Captura de Áudio e Transcrição em Tempo Real",
              style = MaterialTheme.typography.bodySmall,
              color = Slate500
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("btn_voltar_sessao")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
          }
        }
      )
    },
    bottomBar = {
      Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          // Mandatory Visual active recording indicator and waveform
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            ActiveRecordingPulsingIndicator(
              isPaused = isPaused,
              elapsedTimeMs = elapsedTimeMs,
              modifier = Modifier.testTag("indicador_gravacao_ativa")
            )

            AudioAmplitudeBars(
              amplitude = audioAmplitude,
              modifier = Modifier.padding(horizontal = 8.dp)
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Control buttons: Pausar, Retomar, Terminar
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            if (isPaused) {
              FilledTonalButton(
                onClick = onResume,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .weight(1f)
                  .height(48.dp)
                  .testTag("btn_retomar_sessao")
              ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Retomar")
              }
            } else {
              OutlinedButton(
                onClick = onPause,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .weight(1f)
                  .height(48.dp)
                  .testTag("btn_pausar_sessao")
              ) {
                Icon(Icons.Default.Pause, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Pausar")
              }
            }

            Button(
              onClick = onFinish,
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = RecordingRed),
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("btn_terminar_sessao")
            ) {
              Icon(Icons.Default.Stop, contentDescription = null)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Terminar e Gerar")
            }
          }
        }
      }
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 16.dp)
    ) {
      if (segmentos.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "A escutar microfone...",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Slate700
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "As intervenções dos participantes aparecerão aqui em tempo real com rotulagem automática de oradores.",
              style = MaterialTheme.typography.bodyMedium,
              color = Slate500,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
          }
        }
      } else {
        LazyColumn(
          state = listState,
          verticalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp)
            .testTag("lista_transcricao_tempo_real")
        ) {
          items(segmentos) { seg ->
            SegmentoAoVivoItem(
              segmento = seg,
              participantes = participantes,
              onSpeakerClick = {
                selectedSpeakerForAttribution = seg.rotuloOrador
              }
            )
          }
        }
      }
    }
  }

  // Dialog to attribute speaker spk_N (ECRÃ 6)
  if (selectedSpeakerForAttribution != null) {
    val rotulo = selectedSpeakerForAttribution!!
    val currentAssignedPartId = segmentos.find { it.rotuloOrador == rotulo }?.participanteId

    AtribuirOradorDialog(
      rotuloOrador = rotulo,
      participantes = participantes,
      participanteAtualId = currentAssignedPartId,
      onDismiss = { selectedSpeakerForAttribution = null },
      onSelectParticipante = { partId ->
        onAtribuirOrador(rotulo, partId)
        selectedSpeakerForAttribution = null
      }
    )
  }
}

/**
 * Cartão de segmento em tempo real:
 * - Faixa de cor lateral por rótulo de orador
 * - Marca temporal por segmento [MM:SS]
 * - Sublinhado ou badge nos segmentos com confiança baixa (< 0.75)
 */
@Composable
fun SegmentoAoVivoItem(
  segmento: SegmentoEntity,
  participantes: List<ParticipanteEntity>,
  onSpeakerClick: () -> Unit
) {
  val speakerColor = getSpeakerColor(segmento.rotuloOrador)
  val part = participantes.find { it.id == segmento.participanteId }
  val isBaixaConfianca = segmento.confianca < 0.75f

  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("segmento_${segmento.id}")
  ) {
    Row(modifier = Modifier.fillMaxWidth()) {
      // Faixa de cor lateral por rótulo de orador
      Box(
        modifier = Modifier
          .width(5.dp)
          .fillMaxHeight()
          .background(speakerColor)
      )

      Column(modifier = Modifier.padding(12.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          // Rótulo do orador clicável para atribuir participante
          SpeakerBadge(
            rotulo = segmento.rotuloOrador,
            nomeParticipante = part?.nome,
            onClick = onSpeakerClick,
            modifier = Modifier.testTag("rotulo_orador_${segmento.rotuloOrador}")
          )

          Row(verticalAlignment = Alignment.CenterVertically) {
            if (isBaixaConfianca) {
              Surface(
                color = WarningAmberLight,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.padding(end = 6.dp)
              ) {
                Text(
                  text = "Baixa Confiança (${(segmento.confianca * 100).toInt()}%)",
                  color = WarningAmber,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }

            // Marca temporal [MM:SS]
            Text(
              text = "[${formatMs(segmento.inicioMs)} - ${formatMs(segmento.fimMs)}]",
              style = MaterialTheme.typography.labelSmall,
              color = Slate500,
              fontWeight = FontWeight.Medium
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Texto com sublinhado se baixa confiança
        Text(
          text = segmento.texto,
          style = MaterialTheme.typography.bodyMedium,
          color = Slate900,
          lineHeight = 20.sp,
          textDecoration = if (isBaixaConfianca) TextDecoration.Underline else TextDecoration.None
        )
      }
    }
  }
}
