package cv.claudiotavares.acta.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Warning
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
import cv.claudiotavares.acta.data.repository.ConsentimentoCheckResult
import cv.claudiotavares.acta.ui.theme.*

@Composable
fun AvisoGravacaoDialog(
  participantes: List<ParticipanteEntity>,
  consentimentoCheck: ConsentimentoCheckResult?,
  onDismiss: () -> Unit,
  onConfirmStart: () -> Unit,
  onManageParticipants: () -> Unit
) {
  var checkInformou by remember { mutableStateOf(false) }
  var checkTodosConsentiram by remember { mutableStateOf(false) }
  var checkIlegalidade by remember { mutableStateOf(false) }

  val semConsentimento = participantes.filter { !it.consentimentoGravacao }
  val temBloqueioLegal = semConsentimento.isNotEmpty() || participantes.isEmpty()

  val todasCaixasMarcadas = checkInformou && checkTodosConsentiram && checkIlegalidade
  val botaoIniciarAtivo = todasCaixasMarcadas && !temBloqueioLegal

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .testTag("modal_aviso_gravacao")
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .verticalScroll(rememberScrollState())
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Gavel,
            contentDescription = null,
            tint = if (temBloqueioLegal) RecordingRed else WarningAmber,
            modifier = Modifier.size(28.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Aviso Prévio Obrigatório",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "Nos termos da legislação em vigor, a gravação de reuniões exige o consentimento explícito e informado de todos os intervenientes.",
          style = MaterialTheme.typography.bodyMedium,
          color = Slate600
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Bloqueio legal se participante não tiver consentimento
        if (temBloqueioLegal) {
          Surface(
            color = RecordingRedLight,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, RecordingRed),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("aviso_bloqueio_consentimento")
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Warning,
                  contentDescription = null,
                  tint = RecordingRed,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Início Bloqueado por Falta de Consentimento",
                  color = RecordingRed,
                  fontWeight = FontWeight.Bold,
                  style = MaterialTheme.typography.labelLarge
                )
              }
              Spacer(modifier = Modifier.height(6.dp))
              if (participantes.isEmpty()) {
                Text(
                  text = "Não existem participantes registados nesta reunião. É obrigatório adicionar os participantes presentes antes de iniciar a captura.",
                  color = Slate800,
                  style = MaterialTheme.typography.bodySmall
                )
              } else {
                Text(
                  text = "Os seguintes participantes presentes não prestaram consentimento de gravação:",
                  color = Slate800,
                  style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                semConsentimento.forEach { part ->
                  Text(
                    text = "• ${part.nome} (${part.funcao})",
                    color = RecordingRed,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodySmall
                  )
                }
              }
              Spacer(modifier = Modifier.height(10.dp))
              OutlinedButton(
                onClick = {
                  onDismiss()
                  onManageParticipants()
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = RecordingRed),
                modifier = Modifier.fillMaxWidth().testTag("btn_gerir_participantes_aviso")
              ) {
                Text("Gerir Consentimentos dos Participantes")
              }
            }
          }
          Spacer(modifier = Modifier.height(16.dp))
        }

        // Três caixas de verificação obrigatórias
        Text(
          text = "Lista de Verificação (Checklist Obrigatória):",
          fontWeight = FontWeight.Bold,
          style = MaterialTheme.typography.labelLarge,
          color = Slate900
        )
        Spacer(modifier = Modifier.height(8.dp))

        ChecklistItem(
          checked = checkInformou,
          onCheckedChange = { checkInformou = it },
          label = "Informei todos os presentes de que a sessão será gravada e transcrita.",
          tag = "check_informou_presentes"
        )

        ChecklistItem(
          checked = checkTodosConsentiram,
          onCheckedChange = { checkTodosConsentiram = it },
          label = "Todos consentiram formalmente com o registo áudio e textual da reunião.",
          tag = "check_todos_consentiram"
        )

        ChecklistItem(
          checked = checkIlegalidade,
          onCheckedChange = { checkIlegalidade = it },
          label = "Sei que gravar sem consentimento prévio pode ser ilegal e passível de sanções legais.",
          tag = "check_ilegalidade"
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          TextButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("btn_cancelar_aviso")
          ) {
            Text("Cancelar", color = Slate600)
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = onConfirmStart,
            enabled = botaoIniciarAtivo,
            colors = ButtonDefaults.buttonColors(
              containerColor = RecordingRed,
              disabledContainerColor = Slate200
            ),
            modifier = Modifier.testTag("btn_iniciar_gravacao")
          ) {
            Text(
              text = "Iniciar Gravação",
              fontWeight = FontWeight.Bold,
              color = if (botaoIniciarAtivo) androidx.compose.ui.graphics.Color.White else Slate500
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ChecklistItem(
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  label: String,
  tag: String
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp)
  ) {
    Checkbox(
      checked = checked,
      onCheckedChange = onCheckedChange,
      modifier = Modifier.testTag(tag)
    )
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      color = Slate800,
      lineHeight = 18.sp
    )
  }
}
