package cv.claudiotavares.acta.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cv.claudiotavares.acta.data.model.ParticipanteEntity
import cv.claudiotavares.acta.data.model.ReuniaoEntity
import cv.claudiotavares.acta.data.model.SegmentoEntity
import cv.claudiotavares.acta.ui.components.SpeakerBadge
import cv.claudiotavares.acta.ui.components.formatMs
import cv.claudiotavares.acta.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevisaoTranscricaoScreen(
  reuniao: ReuniaoEntity?,
  participantes: List<ParticipanteEntity>,
  segmentos: List<SegmentoEntity>,
  highlightedAnchorMs: Pair<Long, Long>?,
  onBack: () -> Unit,
  onSaveSegmento: (SegmentoEntity) -> Unit,
  onOpenActa: () -> Unit
) {
  var selectedTab by remember { mutableStateOf(0) } // 0: Todos, 1: Por rever (baixa confiança)
  var editingSegmento by remember { mutableStateOf<SegmentoEntity?>(null) }

  val segmentosFiltrados = remember(segmentos, selectedTab) {
    if (selectedTab == 1) {
      segmentos.filter { it.confianca < 0.75f || !it.revisado }
    } else {
      segmentos
    }
  }

  val totalPorRever = segmentos.count { it.confianca < 0.75f || !it.revisado }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Revisão da Transcrição",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = reuniao?.titulo ?: "Reunião",
              style = MaterialTheme.typography.bodySmall,
              color = Slate500
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("btn_voltar_revisao")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
          }
        },
        actions = {
          FilledTonalButton(
            onClick = onOpenActa,
            modifier = Modifier.padding(end = 8.dp).testTag("btn_ir_para_acta")
          ) {
            Text("Ver Acta")
          }
        }
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 16.dp)
    ) {
      // Filter Tabs: "Todos" and "Por rever"
      TabRow(
        selectedTabIndex = selectedTab,
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp)
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = { Text("Todos (${segmentos.size})") },
          modifier = Modifier.testTag("tab_todos_segmentos")
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text("Por rever")
              if (totalPorRever > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  color = WarningAmberLight,
                  shape = RoundedCornerShape(10.dp)
                ) {
                  Text(
                    text = "$totalPorRever",
                    color = WarningAmber,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
            }
          },
          modifier = Modifier.testTag("tab_por_rever")
        )
      }

      if (segmentosFiltrados.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = null,
              tint = SuccessEmerald,
              modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = if (selectedTab == 1) "Sem segmentos pendentes de revisão!" else "Nenhum segmento capturado.",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold
            )
            if (selectedTab == 1) {
              Text(
                text = "Todos os segmentos foram validados ou apresentam elevada confiança.",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate500
              )
            }
          }
        }
      } else {
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
            .testTag("lista_revisao_segmentos")
        ) {
          items(segmentosFiltrados) { seg ->
            val part = participantes.find { it.id == seg.participanteId }
            val isHighlighted = highlightedAnchorMs != null &&
              (seg.inicioMs in highlightedAnchorMs.first..highlightedAnchorMs.second ||
               seg.fimMs in highlightedAnchorMs.first..highlightedAnchorMs.second)

            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(
                containerColor = if (isHighlighted) WarningAmberLight else MaterialTheme.colorScheme.surface
              ),
              border = if (isHighlighted) androidx.compose.foundation.BorderStroke(2.dp, WarningAmber) else null,
              elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("cartao_revisao_${seg.id}")
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  SpeakerBadge(
                    rotulo = seg.rotuloOrador,
                    nomeParticipante = part?.nome
                  )

                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = "[${formatMs(seg.inicioMs)} - ${formatMs(seg.fimMs)}]",
                      style = MaterialTheme.typography.labelSmall,
                      color = Slate500
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                      onClick = { editingSegmento = seg },
                      modifier = Modifier
                        .size(32.dp)
                        .testTag("btn_editar_seg_${seg.id}")
                    ) {
                      Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                      )
                    }
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                  text = seg.texto,
                  style = MaterialTheme.typography.bodyMedium,
                  color = Slate900,
                  lineHeight = 20.sp
                )

                if (seg.confianca < 0.75f) {
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(
                    text = "⚠ Confiança baixa (${(seg.confianca * 100).toInt()}%). Recomenda-se verificação auditiva.",
                    style = MaterialTheme.typography.labelSmall,
                    color = WarningAmber,
                    fontWeight = FontWeight.SemiBold
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  // Edit Segment Dialog
  if (editingSegmento != null) {
    EditarSegmentoDialog(
      segmento = editingSegmento!!,
      participantes = participantes,
      onDismiss = { editingSegmento = null },
      onConfirm = { updatedSeg ->
        onSaveSegmento(updatedSeg)
        editingSegmento = null
      }
    )
  }
}

@Composable
fun EditarSegmentoDialog(
  segmento: SegmentoEntity,
  participantes: List<ParticipanteEntity>,
  onDismiss: () -> Unit,
  onConfirm: (SegmentoEntity) -> Unit
) {
  var textoEditado by remember { mutableStateOf(segmento.texto) }
  var participanteSelecionadoId by remember { mutableStateOf(segmento.participanteId) }
  var expandedPart by remember { mutableStateOf(false) }

  val nomeAtual = participantes.find { it.id == participanteSelecionadoId }?.nome ?: "Sem atribuição (${segmento.rotuloOrador})"

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .testTag("dialog_editar_segmento")
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Text(
          text = "Editar Segmento da Transcrição",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Marca temporal: [${formatMs(segmento.inicioMs)} - ${formatMs(segmento.fimMs)}]",
          style = MaterialTheme.typography.bodySmall,
          color = Slate500
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Speaker selection
        Text(
          text = "Orador atribuído:",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
          OutlinedButton(
            onClick = { expandedPart = true },
            modifier = Modifier.fillMaxWidth().testTag("dropdown_orador_segmento")
          ) {
            Text(nomeAtual)
          }
          DropdownMenu(
            expanded = expandedPart,
            onDismissRequest = { expandedPart = false }
          ) {
            participantes.forEach { part ->
              DropdownMenuItem(
                text = { Text("${part.nome} (${part.funcao})") },
                onClick = {
                  participanteSelecionadoId = part.id
                  expandedPart = false
                }
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = textoEditado,
          onValueChange = { textoEditado = it },
          label = { Text("Texto da Intervenção") },
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
            .testTag("input_texto_segmento_editado")
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          TextButton(onClick = onDismiss) {
            Text("Cancelar")
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              if (textoEditado.isNotBlank()) {
                onConfirm(
                  segmento.copy(
                    texto = textoEditado.trim(),
                    participanteId = participanteSelecionadoId,
                    revisado = true,
                    confianca = 1.0f // Manual edit guarantees high confidence
                  )
                )
              }
            },
            enabled = textoEditado.isNotBlank(),
            modifier = Modifier.testTag("btn_guardar_segmento_editado")
          ) {
            Text("Validar e Guardar")
          }
        }
      }
    }
  }
}
