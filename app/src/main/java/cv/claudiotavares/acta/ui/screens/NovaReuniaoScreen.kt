package cv.claudiotavares.acta.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cv.claudiotavares.acta.ui.theme.Slate500

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovaReuniaoScreen(
  onBack: () -> Unit,
  onCreate: (titulo: String, local: String, linguaOrigem: String, linguaAlvo: String) -> Unit
) {
  var titulo by remember { mutableStateOf("") }
  var local by remember { mutableStateOf("Sala de Reuniões Principal (Híbrida)") }
  var linguaOrigem by remember { mutableStateOf("Português (Portugal)") }
  var linguaAlvo by remember { mutableStateOf("Português (Portugal)") }

  val idiomas = listOf("Português (Portugal)", "Inglês (UK)", "Espanhol (Espanha)", "Francês (França)")
  var expandedOrigem by remember { mutableStateOf(false) }
  var expandedAlvo by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Nova Reunião",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("btn_voltar_nova_reuniao")) {
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
        .padding(20.dp)
        .verticalScroll(rememberScrollState())
    ) {
      Text(
        text = "Identificação e Parâmetros",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Configure os dados essenciais antes de adicionar participantes e iniciar a captação.",
        style = MaterialTheme.typography.bodyMedium,
        color = Slate500
      )

      Spacer(modifier = Modifier.height(20.dp))

      OutlinedTextField(
        value = titulo,
        onValueChange = { titulo = it },
        label = { Text("Título da Reunião *") },
        placeholder = { Text("Ex: Reunião de Direção Estratégica Q4") },
        leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_titulo_reuniao")
      )

      Spacer(modifier = Modifier.height(16.dp))

      OutlinedTextField(
        value = local,
        onValueChange = { local = it },
        label = { Text("Local ou Meio da Sessão") },
        placeholder = { Text("Ex: Sala do Conselho / Microsoft Teams") },
        leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_local_reuniao")
      )

      Spacer(modifier = Modifier.height(20.dp))
      HorizontalDivider()
      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = "Configuração Linguística",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Língua Falada (Origem)
      ExposedDropdownMenuBox(
        expanded = expandedOrigem,
        onExpandedChange = { expandedOrigem = !expandedOrigem }
      ) {
        OutlinedTextField(
          value = linguaOrigem,
          onValueChange = {},
          readOnly = true,
          label = { Text("Língua Falada (Áudio Origem)") },
          leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) },
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedOrigem) },
          modifier = Modifier
            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            .fillMaxWidth()
            .testTag("dropdown_lingua_origem")
        )
        ExposedDropdownMenu(
          expanded = expandedOrigem,
          onDismissRequest = { expandedOrigem = false }
        ) {
          idiomas.forEach { idioma ->
            DropdownMenuItem(
              text = { Text(idioma) },
              onClick = {
                linguaOrigem = idioma
                expandedOrigem = false
              }
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Língua da Transcrição (Alvo)
      ExposedDropdownMenuBox(
        expanded = expandedAlvo,
        onExpandedChange = { expandedAlvo = !expandedAlvo }
      ) {
        OutlinedTextField(
          value = linguaAlvo,
          onValueChange = {},
          readOnly = true,
          label = { Text("Língua da Transcrição e Acta") },
          leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) },
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAlvo) },
          modifier = Modifier
            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            .fillMaxWidth()
            .testTag("dropdown_lingua_alvo")
        )
        ExposedDropdownMenu(
          expanded = expandedAlvo,
          onDismissRequest = { expandedAlvo = false }
        ) {
          idiomas.forEach { idioma ->
            DropdownMenuItem(
              text = { Text(idioma) },
              onClick = {
                linguaAlvo = idioma
                expandedAlvo = false
              }
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(32.dp))

      Button(
        onClick = {
          if (titulo.isNotBlank()) {
            onCreate(titulo.trim(), local.trim(), linguaOrigem, linguaAlvo)
          }
        },
        enabled = titulo.isNotBlank(),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("btn_guardar_reuniao")
      ) {
        Text("Criar e Prosseguir para Participantes", fontWeight = FontWeight.Bold)
      }
    }
  }
}
