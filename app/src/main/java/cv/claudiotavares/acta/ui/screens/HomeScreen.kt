package cv.claudiotavares.acta.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cv.claudiotavares.acta.data.model.ReuniaoEntity
import cv.claudiotavares.acta.ui.components.StatusBadge
import cv.claudiotavares.acta.ui.components.formatDateTimePt
import cv.claudiotavares.acta.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  reunioes: List<ReuniaoEntity>,
  onNovaReuniao: () -> Unit,
  onOpenReuniao: (String) -> Unit,
  onOpenParticipantes: (String) -> Unit,
  onOpenSessao: (String) -> Unit,
  onOpenRevisao: (String) -> Unit,
  onOpenActa: (String) -> Unit,
  onOpenDefinicoes: () -> Unit
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "A",
                color = androidx.compose.ui.graphics.Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "ACTA",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
              )
              Text(
                text = "Captura, Transcrição & Actas",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
              )
            }
          }
        },
        actions = {
          IconButton(onClick = onOpenDefinicoes, modifier = Modifier.testTag("btn_definicoes_home")) {
            Icon(Icons.Default.Settings, contentDescription = "Definições")
          }
        }
      )
    },
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = onNovaReuniao,
        icon = { Icon(Icons.Default.Add, contentDescription = null) },
        text = { Text("Nova Reunião", fontWeight = FontWeight.Bold) },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = androidx.compose.ui.graphics.Color.White,
        modifier = Modifier.testTag("btn_nova_reuniao_fab")
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 16.dp)
    ) {
      if (reunioes.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth().testTag("estado_vazio_reunioes")
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.padding(28.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(64.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Description,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(32.dp)
                )
              }
              Spacer(modifier = Modifier.height(16.dp))
              Text(
                text = "Sem reuniões registadas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "Crie uma nova reunião para configurar a ordem de trabalhos, validar os consentimentos dos intervenientes e iniciar a gravação com transcrição em tempo real.",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate500,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 20.sp
              )
              Spacer(modifier = Modifier.height(20.dp))
              Button(
                onClick = onNovaReuniao,
                modifier = Modifier.testTag("btn_criar_primeira_reuniao")
              ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Criar Nova Reunião")
              }
            }
          }
        }
      } else {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
        ) {
          Text(
            text = "Reuniões Registadas (${reunioes.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Slate800
          )
        }

        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(12.dp),
          contentPadding = PaddingValues(bottom = 80.dp),
          modifier = Modifier
            .fillMaxSize()
            .testTag("lista_reunioes")
        ) {
          items(reunioes) { reuniao ->
            ReuniaoCardItem(
              reuniao = reuniao,
              onOpen = { onOpenActa(reuniao.id) },
              onParticipantes = { onOpenParticipantes(reuniao.id) },
              onGravar = { onOpenSessao(reuniao.id) },
              onRever = { onOpenRevisao(reuniao.id) },
              onActa = { onOpenActa(reuniao.id) }
            )
          }
        }
      }
    }
  }
}

@Composable
fun ReuniaoCardItem(
  reuniao: ReuniaoEntity,
  onOpen: () -> Unit,
  onParticipantes: () -> Unit,
  onGravar: () -> Unit,
  onRever: () -> Unit,
  onActa: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onOpen() }
      .testTag("cartao_reuniao_${reuniao.id}")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        StatusBadge(estado = reuniao.estado)
        Text(
          text = formatDateTimePt(reuniao.dataHoraInicio),
          style = MaterialTheme.typography.labelSmall,
          color = Slate500
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = reuniao.titulo,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Slate900,
        lineHeight = 22.sp
      )

      Spacer(modifier = Modifier.height(6.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Place, contentDescription = null, tint = Slate400, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = reuniao.local,
          style = MaterialTheme.typography.bodySmall,
          color = Slate600
        )
      }

      HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Slate200)

      // Quick Action Buttons
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        OutlinedButton(
          onClick = onParticipantes,
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
          modifier = Modifier.weight(1f).height(36.dp).testTag("btn_card_participantes_${reuniao.id}")
        ) {
          Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(15.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Pessoas", style = MaterialTheme.typography.labelMedium)
        }

        OutlinedButton(
          onClick = onRever,
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
          modifier = Modifier.weight(1f).height(36.dp).testTag("btn_card_rever_${reuniao.id}")
        ) {
          Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(15.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Rever", style = MaterialTheme.typography.labelMedium)
        }

        Button(
          onClick = {
            if (reuniao.estado == "concluida") onActa() else onGravar()
          },
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (reuniao.estado == "concluida") MaterialTheme.colorScheme.primary else RecordingRed
          ),
          modifier = Modifier.weight(1f).height(36.dp).testTag("btn_card_acao_${reuniao.id}")
        ) {
          if (reuniao.estado == "concluida") {
            Icon(Icons.Default.Article, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Acta", style = MaterialTheme.typography.labelMedium)
          } else {
            Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Gravar", style = MaterialTheme.typography.labelMedium)
          }
        }
      }
    }
  }
}
