package cv.claudiotavares.acta.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Security
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
import cv.claudiotavares.acta.data.model.ReuniaoEntity
import cv.claudiotavares.acta.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantesScreen(
  reuniao: ReuniaoEntity?,
  participantes: List<ParticipanteEntity>,
  onBack: () -> Unit,
  onAddParticipante: (String, String, String, Boolean, Boolean) -> Unit,
  onUpdateParticipante: (ParticipanteEntity) -> Unit,
  onDeleteParticipante: (ParticipanteEntity) -> Unit
) {
  var showAddDialog by remember { mutableStateOf(false) }

  val semConsentimento = participantes.filter { !it.consentimentoGravacao }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Participantes e Consentimentos",
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
          IconButton(onClick = onBack, modifier = Modifier.testTag("btn_voltar_participantes")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
          }
        },
        actions = {
          FilledTonalButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.padding(end = 8.dp).testTag("btn_adicionar_participante")
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Adicionar")
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
      if (semConsentimento.isNotEmpty()) {
        Surface(
          color = RecordingRedLight,
          shape = RoundedCornerShape(12.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, RecordingRed),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("alerta_consentimento_faltante")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
          ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = RecordingRed)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Atenção: ${semConsentimento.size} participante(s) sem consentimento de gravação. O início da sessão será bloqueado até validação formal.",
              color = RecordingRed,
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      } else if (participantes.isNotEmpty()) {
        Surface(
          color = SuccessEmeraldLight,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
          ) {
            Icon(Icons.Default.Security, contentDescription = null, tint = SuccessEmerald)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Conformidade RGPD/Legal: Todos os ${participantes.size} participantes têm consentimento de gravação ativo.",
              color = SuccessEmerald,
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Medium
            )
          }
        }
      }

      if (participantes.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "Nenhum participante registado",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Adicione os participantes convocados para registar os seus consentimentos antes de iniciar a reunião.",
              style = MaterialTheme.typography.bodyMedium,
              color = Slate500,
              modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
              onClick = { showAddDialog = true },
              modifier = Modifier.testTag("btn_adicionar_primeiro_participante")
            ) {
              Text("Adicionar Participante")
            }
          }
        }
      } else {
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
            .testTag("lista_participantes")
        ) {
          items(participantes) { part ->
            ParticipanteItemCard(
              participante = part,
              onToggleGravacao = { onUpdateParticipante(part.copy(consentimentoGravacao = it)) },
              onToggleTranscricao = { onUpdateParticipante(part.copy(consentimentoTranscricao = it)) },
              onDelete = { onDeleteParticipante(part) }
            )
          }
        }
      }
    }
  }

  if (showAddDialog) {
    NovoParticipanteDialog(
      onDismiss = { showAddDialog = false },
      onConfirm = { nome, funcao, email, grav, transc ->
        onAddParticipante(nome, funcao, email, grav, transc)
        showAddDialog = false
      }
    )
  }
}

@Composable
fun ParticipanteItemCard(
  participante: ParticipanteEntity,
  onToggleGravacao: (Boolean) -> Unit,
  onToggleTranscricao: (Boolean) -> Unit,
  onDelete: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("cartao_participante_${participante.id}")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = participante.nome,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "${participante.funcao} • ${participante.email}",
            style = MaterialTheme.typography.bodySmall,
            color = Slate600
          )
        }
        IconButton(
          onClick = onDelete,
          modifier = Modifier.testTag("btn_eliminar_part_${participante.id}")
        ) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Remover",
            tint = Slate400
          )
        }
      }

      HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Slate200)

      // Dois interruptores por linha conforme especificação: "consentiu gravação" e "consentiu transcrição"
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Consentiu gravação",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (participante.consentimentoGravacao) SuccessEmerald else RecordingRed
          )
          Text(
            text = "Obrigatório para iniciar a sessão áudio",
            style = MaterialTheme.typography.labelSmall,
            color = Slate500
          )
        }
        Switch(
          checked = participante.consentimentoGravacao,
          onCheckedChange = onToggleGravacao,
          modifier = Modifier.testTag("switch_gravacao_${participante.id}")
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Consentiu transcrição",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (participante.consentimentoTranscricao) SuccessEmerald else Slate700
          )
          Text(
            text = "Processamento e diarização de voz por IA",
            style = MaterialTheme.typography.labelSmall,
            color = Slate500
          )
        }
        Switch(
          checked = participante.consentimentoTranscricao,
          onCheckedChange = onToggleTranscricao,
          modifier = Modifier.testTag("switch_transcricao_${participante.id}")
        )
      }
    }
  }
}

@Composable
fun NovoParticipanteDialog(
  onDismiss: () -> Unit,
  onConfirm: (String, String, String, Boolean, Boolean) -> Unit
) {
  var nome by remember { mutableStateOf("") }
  var funcao by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var gravacao by remember { mutableStateOf(true) }
  var transcricao by remember { mutableStateOf(true) }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .testTag("dialog_novo_participante")
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Text(
          text = "Adicionar Participante",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = nome,
          onValueChange = { nome = it },
          label = { Text("Nome Completo") },
          placeholder = { Text("Ex: Dra. Teresa Bento") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("input_nome_participante")
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = funcao,
          onValueChange = { funcao = it },
          label = { Text("Função / Cargo") },
          placeholder = { Text("Ex: Diretora Comercial") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("input_funcao_participante")
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = email,
          onValueChange = { email = it },
          label = { Text("Email Institucional") },
          placeholder = { Text("teresa@example.test") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("input_email_participante")
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(text = "Consentiu gravação", style = MaterialTheme.typography.bodyMedium)
          Switch(
            checked = gravacao,
            onCheckedChange = { gravacao = it },
            modifier = Modifier.testTag("switch_novo_gravacao")
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(text = "Consentiu transcrição", style = MaterialTheme.typography.bodyMedium)
          Switch(
            checked = transcricao,
            onCheckedChange = { transcricao = it },
            modifier = Modifier.testTag("switch_novo_transcricao")
          )
        }

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
              if (nome.isNotBlank()) {
                onConfirm(nome.trim(), funcao.trim().ifBlank { "Participante" }, email.trim(), gravacao, transcricao)
              }
            },
            enabled = nome.isNotBlank(),
            modifier = Modifier.testTag("btn_confirmar_novo_participante")
          ) {
            Text("Guardar")
          }
        }
      }
    }
  }
}
