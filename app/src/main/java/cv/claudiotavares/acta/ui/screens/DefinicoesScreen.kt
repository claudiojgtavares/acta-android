package cv.claudiotavares.acta.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cv.claudiotavares.acta.data.model.RegistoAuditoriaEntity
import cv.claudiotavares.acta.ui.components.formatDateTimePt
import cv.claudiotavares.acta.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefinicoesScreen(
  auditorias: List<RegistoAuditoriaEntity>,
  onBack: () -> Unit,
  onAplicarRetencao: (dias: Int) -> Unit,
  onLimparTodosOsDados: () -> Unit,
  onExportarJson: suspend () -> String
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  var showConfirmDeleteDialog by remember { mutableStateOf(false) }
  var showAuditoriaDialog by remember { mutableStateOf(false) }
  var diasRetencao by remember { mutableStateOf(90) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Definições e Governança",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("btn_voltar_definicoes")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
          }
        }
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(16.dp)
        .verticalScroll(rememberScrollState())
    ) {
      // 1. Políticas de Retenção
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Política de Retenção de Dados", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Elimine periodicamente dados de sessões e gravações expiradas para cumprir o princípio da minimização de dados do RGPD.",
            style = MaterialTheme.typography.bodySmall,
            color = Slate600
          )
          Spacer(modifier = Modifier.height(14.dp))

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Eliminar sessões com mais de:", style = MaterialTheme.typography.bodyMedium)
            Text("$diasRetencao dias", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
          }

          Slider(
            value = diasRetencao.toFloat(),
            onValueChange = { diasRetencao = it.toInt() },
            valueRange = 30f..365f,
            steps = 10,
            modifier = Modifier.testTag("slider_retencao")
          )

          Spacer(modifier = Modifier.height(6.dp))
          FilledTonalButton(
            onClick = {
              onAplicarRetencao(diasRetencao)
              Toast.makeText(context, "Política de retenção aplicada (sessões > $diasRetencao dias eliminadas).", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth().testTag("btn_aplicar_retencao")
          ) {
            Text("Aplicar Retenção Agora")
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 2. Registo de Auditoria
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.HistoryEdu, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Registo de Auditoria Local", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Histórico imutável de todas as escritas relevantes (quem, quando, o que mudou). Total: ${auditorias.size} registos.",
            style = MaterialTheme.typography.bodySmall,
            color = Slate600
          )
          Spacer(modifier = Modifier.height(14.dp))
          OutlinedButton(
            onClick = { showAuditoriaDialog = true },
            modifier = Modifier.fillMaxWidth().testTag("btn_ver_auditoria")
          ) {
            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Consultar Livro de Auditoria")
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 3. Exportar dados locais
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Download, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cópia de Segurança e Exportação", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Exporte todos os registos locais em formato JSON normalizado para arquivo externo.",
            style = MaterialTheme.typography.bodySmall,
            color = Slate600
          )
          Spacer(modifier = Modifier.height(14.dp))
          OutlinedButton(
            onClick = {
              coroutineScope.launch {
                val json = onExportarJson()
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("ACTA JSON", json))
                Toast.makeText(context, "Exportação JSON copiada para a área de transferência!", Toast.LENGTH_SHORT).show()
              }
            },
            modifier = Modifier.fillMaxWidth().testTag("btn_exportar_json")
          ) {
            Text("Exportar Base de Dados Local (JSON)")
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 4. Zona de Perigo: Eliminar todos os dados locais
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = RecordingRedLight),
        border = androidx.compose.foundation.BorderStroke(1.dp, RecordingRed),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.DeleteForever, contentDescription = null, tint = RecordingRed)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Eliminar Todos os Dados Locais", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = RecordingRed)
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Elimina permanentemente todas as reuniões, participantes, gravações e actas. O sistema voltará ao estado estritamente vazio (sem dados de demonstração).",
            style = MaterialTheme.typography.bodySmall,
            color = Slate800
          )
          Spacer(modifier = Modifier.height(14.dp))
          Button(
            onClick = { showConfirmDeleteDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = RecordingRed),
            modifier = Modifier.fillMaxWidth().testTag("btn_eliminar_todos_dados")
          ) {
            Text("Eliminar Todos os Dados")
          }
        }
      }
    }
  }

  // Confirmação de eliminação de dados
  if (showConfirmDeleteDialog) {
    AlertDialog(
      onDismissRequest = { showConfirmDeleteDialog = false },
      title = { Text("Confirmar Eliminação Total") },
      text = {
        Text("Tem a certeza absoluta de que pretende apagar todos os dados locais da aplicação ACTA? Esta ação é irreversível e deixará a aplicação totalmente vazia.")
      },
      confirmButton = {
        Button(
          onClick = {
            onLimparTodosOsDados()
            showConfirmDeleteDialog = false
            Toast.makeText(context, "Todos os dados foram eliminados.", Toast.LENGTH_SHORT).show()
          },
          colors = ButtonDefaults.buttonColors(containerColor = RecordingRed),
          modifier = Modifier.testTag("btn_confirmar_destruicao_dados")
        ) {
          Text("Sim, Eliminar Tudo")
        }
      },
      dismissButton = {
        TextButton(onClick = { showConfirmDeleteDialog = false }) {
          Text("Cancelar")
        }
      }
    )
  }

  // Dialog com lista de registos de auditoria
  if (showAuditoriaDialog) {
    Dialog(onDismissRequest = { showAuditoriaDialog = false }) {
      Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp).padding(8.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("Livro de Auditoria", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
          Text("Registo cronológico de operações críticas", style = MaterialTheme.typography.bodySmall, color = Slate500)
          Spacer(modifier = Modifier.height(12.dp))

          if (auditorias.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
              Text("Nenhum registo de auditoria.", color = Slate500)
            }
          } else {
            LazyColumn(
              verticalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              items(auditorias) { aud ->
                Surface(
                  color = Slate100,
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.SpaceBetween,
                      modifier = Modifier.fillMaxWidth()
                    ) {
                      Text(aud.accao.uppercase(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                      Text(formatDateTimePt(aud.timestamp), style = MaterialTheme.typography.labelSmall, color = Slate500)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(aud.detalhe, style = MaterialTheme.typography.bodySmall, color = Slate900)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Autor: ${aud.autor}", style = MaterialTheme.typography.labelSmall, color = Slate600)
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(onClick = { showAuditoriaDialog = false }) {
              Text("Fechar")
            }
          }
        }
      }
    }
  }
}
