package cv.claudiotavares.acta.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
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
import cv.claudiotavares.acta.data.model.*
import cv.claudiotavares.acta.ui.components.StatusBadge
import cv.claudiotavares.acta.ui.components.formatDateTimePt
import cv.claudiotavares.acta.ui.components.formatMs
import cv.claudiotavares.acta.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActaScreen(
  reuniao: ReuniaoEntity?,
  participantes: List<ParticipanteEntity>,
  acta: ActaEntity?,
  deliberacoes: List<DeliberacaoEntity>,
  accoes: List<AccaoEntity>,
  todasVersoes: List<ActaEntity>,
  onSelectVersao: (Int) -> Unit,
  onOpenRevisao: () -> Unit,
  onAprovarActa: (nomeAprovador: String) -> Unit,
  onJumpToExcerpt: (inicioMs: Long, fimMs: Long) -> Unit,
  onEditSumula: (sumula: String, ordem: String, prox: String) -> Unit,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  var showAprovarDialog by remember { mutableStateOf(false) }
  var showEditDialog by remember { mutableStateOf(false) }
  var showVersionMenu by remember { mutableStateOf(false) }

  val isAprovada = acta?.estado == "aprovada"
  val podeAprovar = acta?.revisaoAbertaPeloMenosUmaVez == true && !isAprovada

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Acta Formal da Reunião",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.width(8.dp))
              acta?.let {
                Surface(
                  color = MaterialTheme.colorScheme.primaryContainer,
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Text(
                    text = "v${it.versao}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
            }
            Text(
              text = reuniao?.titulo ?: "Reunião",
              style = MaterialTheme.typography.bodySmall,
              color = Slate500,
              maxLines = 1
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("btn_voltar_acta")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
          }
        },
        actions = {
          // Seletor de versão
          if (todasVersoes.size > 1) {
            Box {
              IconButton(onClick = { showVersionMenu = true }, modifier = Modifier.testTag("btn_historico_versoes")) {
                Icon(Icons.Default.History, contentDescription = "Versões")
              }
              DropdownMenu(
                expanded = showVersionMenu,
                onDismissRequest = { showVersionMenu = false }
              ) {
                todasVersoes.forEach { v ->
                  DropdownMenuItem(
                    text = {
                      Text("Versão ${v.versao} (${v.estado.replace("_", " ")})")
                    },
                    trailingIcon = {
                      if (v.id == acta?.id) Icon(Icons.Default.Check, contentDescription = null)
                    },
                    onClick = {
                      onSelectVersao(v.versao)
                      showVersionMenu = false
                    }
                  )
                }
              }
            }
          }

          IconButton(
            onClick = {
              if (acta != null && reuniao != null) {
                val markdown = exportarParaMarkdown(reuniao, participantes, acta, deliberacoes, accoes)
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Acta", markdown))
                Toast.makeText(context, "Acta copiada em formato Markdown!", Toast.LENGTH_SHORT).show()
              }
            },
            modifier = Modifier.testTag("btn_exportar_acta")
          ) {
            Icon(Icons.Default.Share, contentDescription = "Exportar Markdown")
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
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          OutlinedButton(
            onClick = onOpenRevisao,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f).height(48.dp).testTag("btn_abrir_revisao_da_acta")
          ) {
            Icon(Icons.Default.RateReview, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Rever Transcrição")
          }

          if (isAprovada) {
            FilledTonalButton(
              onClick = { showEditDialog = true },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.weight(1f).height(48.dp).testTag("btn_criar_nova_versao")
            ) {
              Icon(Icons.Default.Edit, contentDescription = null)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Criar Nova Versão")
            }
          } else {
            Button(
              onClick = { showAprovarDialog = true },
              enabled = podeAprovar,
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = SuccessEmerald),
              modifier = Modifier.weight(1f).height(48.dp).testTag("btn_aprovar_acta")
            ) {
              Icon(Icons.Default.CheckCircle, contentDescription = null)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Aprovar Acta")
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
        .verticalScroll(rememberScrollState())
    ) {
      // FAIXA FIXA NO TOPO (MANDATÓRIA)
      // "Faixa fixa no topo: 'Documento gerado por inteligência artificial. Carece de revisão humana.'"
      Surface(
        color = WarningAmberLight,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("faixa_aviso_ia")
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
          Icon(
            imageVector = Icons.Default.SmartToy,
            contentDescription = null,
            tint = WarningAmber,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Documento gerado por inteligência artificial. Carece de revisão humana.",
            color = Slate900,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
          )
        }
      }

      if (acta == null) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("A carregar acta...", color = Slate500)
          }
        }
      } else {
        Column(modifier = Modifier.padding(16.dp)) {
          // Status e aviso de bloqueio de aprovação
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            StatusBadge(estado = acta.estado)

            if (isAprovada && acta.aprovadoPor != null) {
              Text(
                text = "Aprovada por ${acta.aprovadoPor}",
                style = MaterialTheme.typography.bodySmall,
                color = SuccessEmerald,
                fontWeight = FontWeight.SemiBold
              )
            }
          }

          if (!isAprovada && !acta.revisaoAbertaPeloMenosUmaVez) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
              color = Slate100,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(10.dp)
              ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = Slate600, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "O botão 'Aprovar acta' será ativado após aceder e conferir o ecrã de revisão da transcrição.",
                  style = MaterialTheme.typography.bodySmall,
                  color = Slate700
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // 1. Identificação
          RubricaCard(titulo = "1. Identificação da Reunião", icon = Icons.Default.Info) {
            Text("Título: ${reuniao?.titulo ?: "Reunião de Coordenação"}", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Data e Hora: ${formatDateTimePt(reuniao?.dataHoraInicio ?: System.currentTimeMillis())}")
            Spacer(modifier = Modifier.height(4.dp))
            Text("Local: ${reuniao?.local ?: "Não especificado"}")
            Spacer(modifier = Modifier.height(4.dp))
            Text("Língua de Registo: ${reuniao?.linguaAlvo ?: "Português (Portugal)"}")
          }

          Spacer(modifier = Modifier.height(12.dp))

          // 2. Presentes
          RubricaCard(titulo = "2. Presentes e Intervenientes", icon = Icons.Default.Group) {
            if (participantes.isEmpty()) {
              Text("Nenhum participante registado.", color = Slate500)
            } else {
              participantes.forEach { part ->
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(vertical = 2.dp)
                ) {
                  Text("• ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                  Text(part.nome, fontWeight = FontWeight.SemiBold)
                  Text(" — ${part.funcao} (${part.email})", color = Slate600, style = MaterialTheme.typography.bodySmall)
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // 3. Ordem de Trabalhos
          RubricaCard(
            titulo = "3. Ordem de Trabalhos",
            icon = Icons.Default.FormatListNumbered,
            onEdit = if (!isAprovada) { { showEditDialog = true } } else null
          ) {
            Text(text = acta.ordemDeTrabalhos, lineHeight = 20.sp)
          }

          Spacer(modifier = Modifier.height(12.dp))

          // 4. Súmula Executiva
          RubricaCard(
            titulo = "4. Súmula Executiva",
            icon = Icons.Default.Article,
            onEdit = if (!isAprovada) { { showEditDialog = true } } else null
          ) {
            Text(text = acta.sumulaExecutiva, lineHeight = 22.sp, color = Slate900)
          }

          Spacer(modifier = Modifier.height(12.dp))

          // 5. Deliberações
          RubricaCard(titulo = "5. Deliberações Formais", icon = Icons.Default.Gavel) {
            if (deliberacoes.isEmpty()) {
              Text("Nenhuma deliberação registada nesta reunião.", color = Slate500)
            } else {
              deliberacoes.forEachIndexed { idx, delib ->
                DeliberacaoItemView(
                  indice = idx + 1,
                  deliberacao = delib,
                  onJump = { onJumpToExcerpt(delib.ancoraInicioMs, delib.ancoraFimMs) }
                )
                if (idx < deliberacoes.size - 1) {
                  HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Slate200)
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // 6. Ações
          RubricaCard(titulo = "6. Ações e Tarefas Operacionais", icon = Icons.Default.Assignment) {
            if (accoes.isEmpty()) {
              Text("Nenhuma ação atribuída.", color = Slate500)
            } else {
              accoes.forEachIndexed { idx, accao ->
                AccaoItemView(
                  indice = idx + 1,
                  accao = accao,
                  onJump = { onJumpToExcerpt(accao.ancoraInicioMs, accao.ancoraFimMs) }
                )
                if (idx < accoes.size - 1) {
                  HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Slate200)
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // 7. Próxima Reunião
          RubricaCard(
            titulo = "7. Próxima Reunião",
            icon = Icons.Default.Event,
            onEdit = if (!isAprovada) { { showEditDialog = true } } else null
          ) {
            Text(text = acta.proximaReuniao, color = Slate900)
          }

          Spacer(modifier = Modifier.height(24.dp))
        }
      }
    }
  }

  // Dialog de aprovação da acta
  if (showAprovarDialog && acta != null) {
    var nomeAprovador by remember { mutableStateOf(participantes.firstOrNull()?.nome ?: "Secretário(a)") }
    Dialog(onDismissRequest = { showAprovarDialog = false }) {
      Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.padding(8.dp).testTag("dialog_aprovar_acta")
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Text("Aprovação Formal da Acta", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            "Após aprovação, a acta versão ${acta.versao} fica imutável. Qualquer edição posterior dará origem a uma nova versão (ex: v${acta.versao + 1}).",
            style = MaterialTheme.typography.bodySmall,
            color = Slate600
          )
          Spacer(modifier = Modifier.height(14.dp))
          OutlinedTextField(
            value = nomeAprovador,
            onValueChange = { nomeAprovador = it },
            label = { Text("Nome do Aprovador / Presidente") },
            modifier = Modifier.fillMaxWidth().testTag("input_nome_aprovador")
          )
          Spacer(modifier = Modifier.height(20.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { showAprovarDialog = false }) {
              Text("Cancelar")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = {
                onAprovarActa(nomeAprovador.trim())
                showAprovarDialog = false
              },
              colors = ButtonDefaults.buttonColors(containerColor = SuccessEmerald),
              modifier = Modifier.testTag("btn_confirmar_aprovacao_acta")
            ) {
              Text("Aprovar e Selar")
            }
          }
        }
      }
    }
  }

  // Dialog de edição de súmula / ordem de trabalhos
  if (showEditDialog && acta != null) {
    var novaSumula by remember { mutableStateOf(acta.sumulaExecutiva) }
    var novaOrdem by remember { mutableStateOf(acta.ordemDeTrabalhos) }
    var novaProx by remember { mutableStateOf(acta.proximaReuniao) }

    Dialog(onDismissRequest = { showEditDialog = false }) {
      Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.padding(8.dp).fillMaxWidth().testTag("dialog_editar_acta")
      ) {
        Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
          Text("Editar Conteúdo da Acta", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
          if (isAprovada) {
            Spacer(modifier = Modifier.height(4.dp))
            Text("Esta acta já se encontra aprovada. Guardar alterações criará automaticamente uma nova versão.", color = WarningAmber, style = MaterialTheme.typography.bodySmall)
          }
          Spacer(modifier = Modifier.height(14.dp))

          OutlinedTextField(
            value = novaOrdem,
            onValueChange = { novaOrdem = it },
            label = { Text("Ordem de Trabalhos") },
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = novaSumula,
            onValueChange = { novaSumula = it },
            label = { Text("Súmula Executiva") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp)
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = novaProx,
            onValueChange = { novaProx = it },
            label = { Text("Próxima Reunião") },
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(20.dp))

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { showEditDialog = false }) {
              Text("Cancelar")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = {
                onEditSumula(novaSumula, novaOrdem, novaProx)
                showEditDialog = false
              },
              modifier = Modifier.testTag("btn_guardar_edicao_acta")
            ) {
              Text(if (isAprovada) "Criar Nova Versão" else "Guardar Alterações")
            }
          }
        }
      }
    }
  }
}

@Composable
fun RubricaCard(
  titulo: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  onEdit: (() -> Unit)? = null,
  content: @Composable ColumnScope.() -> Unit
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        if (onEdit != null) {
          IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Slate500, modifier = Modifier.size(16.dp))
          }
        }
      }
      HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Slate200)
      content()
    }
  }
}

@Composable
fun DeliberacaoItemView(
  indice: Int,
  deliberacao: DeliberacaoEntity,
  onJump: () -> Unit
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      verticalAlignment = Alignment.Top,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth()
    ) {
      Text(
        text = "Deliberação $indice",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )
      // Botão de salto para o excerto de origem na transcrição
      OutlinedButton(
        onClick = onJump,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
        modifier = Modifier.height(28.dp).testTag("btn_salto_delib_${deliberacao.id}")
      ) {
        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "${formatMs(deliberacao.ancoraInicioMs)}",
          style = MaterialTheme.typography.labelSmall
        )
      }
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(text = deliberacao.texto, style = MaterialTheme.typography.bodyMedium, color = Slate900)

    if (!deliberacao.verificada) {
      Spacer(modifier = Modifier.height(4.dp))
      Surface(
        color = WarningAmberLight,
        shape = RoundedCornerShape(4.dp)
      ) {
        Text(
          text = "⚠ Âncora não verificada na transcrição",
          color = WarningAmber,
          style = MaterialTheme.typography.labelSmall,
          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
      }
    }
  }
}

/**
 * REGRA 3: Uma ação sem responsável atribuído fica com estado 'por atribuir'
 * e é destacada visualmente — nunca fica silenciosamente sem dono.
 */
@Composable
fun AccaoItemView(
  indice: Int,
  accao: AccaoEntity,
  onJump: () -> Unit
) {
  val semResponsavel = accao.responsavelId == null && accao.responsavelNome.isNullOrBlank()

  Surface(
    color = if (semResponsavel) RecordingRedLight else Slate50,
    shape = RoundedCornerShape(8.dp),
    border = if (semResponsavel) androidx.compose.foundation.BorderStroke(1.dp, RecordingRed) else null,
    modifier = Modifier.fillMaxWidth().testTag("accao_${accao.id}")
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "Ação $indice",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (semResponsavel) RecordingRed else Slate700
          )
          Spacer(modifier = Modifier.width(8.dp))
          StatusBadge(estado = if (semResponsavel) "por atribuir" else accao.estado)
        }

        // Botão que salta para o excerto de origem na transcrição
        OutlinedButton(
          onClick = onJump,
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
          modifier = Modifier.height(28.dp).testTag("btn_salto_accao_${accao.id}")
        ) {
          Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "${formatMs(accao.ancoraInicioMs)}",
            style = MaterialTheme.typography.labelSmall
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = accao.descricao,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        color = Slate900
      )

      Spacer(modifier = Modifier.height(6.dp))
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "Responsável: ${accao.responsavelNome ?: "POR ATRIBUIR (SEM RESPONSÁVEL)"}",
          style = MaterialTheme.typography.bodySmall,
          color = if (semResponsavel) RecordingRed else Slate700,
          fontWeight = if (semResponsavel) FontWeight.Bold else FontWeight.Normal
        )

        accao.prazo?.let {
          Text(
            text = "Prazo: $it",
            style = MaterialTheme.typography.bodySmall,
            color = Slate600
          )
        }
      }
    }
  }
}

fun exportarParaMarkdown(
  reuniao: ReuniaoEntity,
  participantes: List<ParticipanteEntity>,
  acta: ActaEntity,
  deliberacoes: List<DeliberacaoEntity>,
  accoes: List<AccaoEntity>
): String {
  return buildString {
    append("# ACTA DE REUNIÃO — ${reuniao.titulo.uppercase()}\n\n")
    append("> **Aviso Legal**: Documento gerado por inteligência artificial. Carece de revisão humana.\n")
    append("> Versão: ${acta.versao} | Estado: ${acta.estado.uppercase()}\n\n")
    append("## 1. Identificação\n")
    append("- **Data e Hora**: ${formatDateTimePt(reuniao.dataHoraInicio)}\n")
    append("- **Local**: ${reuniao.local}\n")
    append("- **Língua**: ${reuniao.linguaAlvo}\n\n")
    append("## 2. Presentes e Consentimentos\n")
    participantes.forEach {
      append("- **${it.nome}** (${it.funcao}) — Gravação: ${if (it.consentimentoGravacao) "Sim" else "Não"}\n")
    }
    append("\n## 3. Ordem de Trabalhos\n")
    append("${acta.ordemDeTrabalhos}\n\n")
    append("## 4. Súmula Executiva\n")
    append("${acta.sumulaExecutiva}\n\n")
    append("## 5. Deliberações\n")
    deliberacoes.forEachIndexed { i, d ->
      append("${i + 1}. **${d.texto}** (Excerto: [${formatMs(d.ancoraInicioMs)} - ${formatMs(d.ancoraFimMs)}])\n")
    }
    append("\n## 6. Ações Operacionais\n")
    accoes.forEachIndexed { i, a ->
      append("${i + 1}. **${a.descricao}**\n")
      append("   - Responsável: ${a.responsavelNome ?: "POR ATRIBUIR"}\n")
      append("   - Prazo: ${a.prazo ?: "A definir"}\n")
      append("   - Âncora: [${formatMs(a.ancoraInicioMs)} - ${formatMs(a.ancoraFimMs)}]\n")
    }
    append("\n## 7. Próxima Reunião\n")
    append("${acta.proximaReuniao}\n")
  }
}
