package cv.claudiotavares.acta.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import cv.claudiotavares.acta.data.model.ParticipanteEntity
import cv.claudiotavares.acta.ui.theme.Slate500
import cv.claudiotavares.acta.ui.theme.Slate600
import cv.claudiotavares.acta.ui.theme.getSpeakerColor

@Composable
fun AtribuirOradorDialog(
  rotuloOrador: String,
  participantes: List<ParticipanteEntity>,
  participanteAtualId: String?,
  onDismiss: () -> Unit,
  onSelectParticipante: (String) -> Unit
) {
  val speakerColor = getSpeakerColor(rotuloOrador)

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .testTag("dialog_atribuir_orador")
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(speakerColor),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = rotuloOrador.replace("spk_", ""),
              color = androidx.compose.ui.graphics.Color.White,
              fontWeight = FontWeight.Bold
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "Atribuir Orador $rotuloOrador",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "A escolha será propagada a todos os segmentos deste orador na sessão.",
              style = MaterialTheme.typography.bodySmall,
              color = Slate600
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (participantes.isEmpty()) {
          Text(
            text = "Não existem participantes registados nesta reunião para associar.",
            style = MaterialTheme.typography.bodyMedium,
            color = Slate500,
            modifier = Modifier.padding(vertical = 12.dp)
          )
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(max = 280.dp)
          ) {
            items(participantes) { part ->
              val isSelected = part.id == participanteAtualId
              Surface(
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp)
                  .clickable { onSelectParticipante(part.id) }
                  .testTag("participante_opcao_${part.id}")
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(12.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else Slate500
                  )
                  Spacer(modifier = Modifier.width(12.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = part.nome,
                      fontWeight = FontWeight.SemiBold,
                      style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                      text = "${part.funcao} • ${part.email}",
                      style = MaterialTheme.typography.bodySmall,
                      color = Slate600
                    )
                  }
                  if (isSelected) {
                    Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = "Selecionado",
                      tint = MaterialTheme.colorScheme.primary
                    )
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          TextButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("btn_cancelar_atribuicao")
          ) {
            Text("Fechar")
          }
        }
      }
    }
  }
}
