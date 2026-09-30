package cv.claudiotavares.acta

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import cv.claudiotavares.acta.ui.ActaViewModel
import cv.claudiotavares.acta.ui.Screen
import cv.claudiotavares.acta.ui.screens.*
import cv.claudiotavares.acta.ui.theme.ActaTheme

class MainActivity : ComponentActivity() {
  private val viewModel: ActaViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      ActaTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          ActaApp(viewModel = viewModel)
        }
      }
    }
  }
}

@Composable
fun ActaApp(viewModel: ActaViewModel) {
  val currentScreen by viewModel.currentScreen.collectAsState()
  val reunioes by viewModel.reunioes.collectAsState()
  val currentReuniao by viewModel.currentReuniao.collectAsState()
  val currentParticipantes by viewModel.currentParticipantes.collectAsState()
  val currentSegmentos by viewModel.currentSegmentos.collectAsState()
  val currentActa by viewModel.currentActa.collectAsState()
  val todasVersoesActa by viewModel.todasVersoesActa.collectAsState()
  val currentDeliberacoes by viewModel.currentDeliberacoes.collectAsState()
  val currentAccoes by viewModel.currentAccoes.collectAsState()
  val currentAuditorias by viewModel.currentAuditorias.collectAsState()
  val consentimentoCheck by viewModel.consentimentoCheck.collectAsState()
  val highlightedAnchorMs by viewModel.highlightedAnchorMs.collectAsState()

  val isRecording by viewModel.audioRecorder.isRecording.collectAsState()
  val isPaused by viewModel.audioRecorder.isPaused.collectAsState()
  val elapsedTimeMs by viewModel.audioRecorder.elapsedTimeMs.collectAsState()
  val audioAmplitude by viewModel.audioRecorder.audioAmplitude.collectAsState()

  var pendingRecordReuniaoId by remember { mutableStateOf<String?>(null) }
  var showAvisoDialog by remember { mutableStateOf(false) }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    if (isGranted) {
      pendingRecordReuniaoId?.let { id ->
        viewModel.iniciarSessaoGravacao(id) { motivo ->
          // In case blocked
        }
      }
    }
  }

  // Pre-recording warning dialog (ECRÃ 4 + REGRA DE NEGÓCIO 1)
  if (showAvisoDialog && pendingRecordReuniaoId != null) {
    AvisoGravacaoDialog(
      participantes = currentParticipantes,
      consentimentoCheck = consentimentoCheck,
      onDismiss = {
        showAvisoDialog = false
        pendingRecordReuniaoId = null
      },
      onConfirmStart = {
        val id = pendingRecordReuniaoId!!
        showAvisoDialog = false
        pendingRecordReuniaoId = null
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        viewModel.iniciarSessaoGravacao(id)
      },
      onManageParticipants = {
        val id = pendingRecordReuniaoId!!
        showAvisoDialog = false
        pendingRecordReuniaoId = null
        viewModel.navigateTo(Screen.Participantes(id))
      }
    )
  }

  when (val screen = currentScreen) {
    is Screen.Home -> {
      HomeScreen(
        reunioes = reunioes,
        onNovaReuniao = { viewModel.navigateTo(Screen.NovaReuniao) },
        onOpenReuniao = { id ->
          viewModel.selectReuniao(id)
          viewModel.navigateTo(Screen.Acta(id))
        },
        onOpenParticipantes = { id ->
          viewModel.selectReuniao(id)
          viewModel.navigateTo(Screen.Participantes(id))
        },
        onOpenSessao = { id ->
          viewModel.selectReuniao(id)
          viewModel.checkConsentimento(id)
          pendingRecordReuniaoId = id
          showAvisoDialog = true
        },
        onOpenRevisao = { id ->
          viewModel.selectReuniao(id)
          viewModel.navigateTo(Screen.Revisao(id))
        },
        onOpenActa = { id ->
          viewModel.selectReuniao(id)
          viewModel.navigateTo(Screen.Acta(id))
        },
        onOpenDefinicoes = {
          viewModel.navigateTo(Screen.Definicoes)
        }
      )
    }

    is Screen.NovaReuniao -> {
      NovaReuniaoScreen(
        onBack = { viewModel.navigateTo(Screen.Home) },
        onCreate = { titulo, local, origem, alvo ->
          viewModel.criarNovaReuniao(titulo, local, origem, alvo) { novaId ->
            viewModel.navigateTo(Screen.Participantes(novaId))
          }
        }
      )
    }

    is Screen.Participantes -> {
      ParticipantesScreen(
        reuniao = currentReuniao,
        participantes = currentParticipantes,
        onBack = { viewModel.navigateTo(Screen.Home) },
        onAddParticipante = { nome, funcao, email, grav, transc ->
          viewModel.adicionarParticipante(screen.reuniaoId, nome, funcao, email, grav, transc)
        },
        onUpdateParticipante = { part ->
          viewModel.atualizarParticipante(part)
        },
        onDeleteParticipante = { part ->
          viewModel.removerParticipante(part)
        }
      )
    }

    is Screen.Sessao -> {
      SessaoScreen(
        reuniao = currentReuniao,
        participantes = currentParticipantes,
        segmentos = currentSegmentos,
        isRecording = isRecording,
        isPaused = isPaused,
        elapsedTimeMs = elapsedTimeMs,
        audioAmplitude = audioAmplitude,
        onPause = { viewModel.pausarGravacao() },
        onResume = { viewModel.retomarGravacao() },
        onFinish = { viewModel.terminarGravacao(screen.reuniaoId) },
        onAtribuirOrador = { rotulo, partId ->
          viewModel.atribuirOrador(rotulo, partId)
        },
        onBack = { viewModel.navigateTo(Screen.Home) }
      )
    }

    is Screen.Revisao -> {
      RevisaoTranscricaoScreen(
        reuniao = currentReuniao,
        participantes = currentParticipantes,
        segmentos = currentSegmentos,
        highlightedAnchorMs = highlightedAnchorMs,
        onBack = {
          viewModel.clearHighlightedAnchor()
          viewModel.navigateTo(Screen.Home)
        },
        onSaveSegmento = { seg ->
          viewModel.atualizarSegmento(seg)
        },
        onOpenActa = {
          viewModel.clearHighlightedAnchor()
          viewModel.navigateTo(Screen.Acta(screen.reuniaoId))
        }
      )
    }

    is Screen.Acta -> {
      ActaScreen(
        reuniao = currentReuniao,
        participantes = currentParticipantes,
        acta = currentActa,
        deliberacoes = currentDeliberacoes,
        accoes = currentAccoes,
        todasVersoes = todasVersoesActa,
        onSelectVersao = { v -> viewModel.selectActaVersao(v) },
        onOpenRevisao = { viewModel.navigateTo(Screen.Revisao(screen.reuniaoId)) },
        onAprovarActa = { aprovador ->
          currentActa?.let {
            viewModel.aprovarActa(it, aprovador)
          }
        },
        onJumpToExcerpt = { inicioMs, fimMs ->
          viewModel.highlightAnchor(inicioMs, fimMs)
          viewModel.navigateTo(Screen.Revisao(screen.reuniaoId))
        },
        onEditSumula = { sumula, ordem, prox ->
          currentActa?.let {
            viewModel.atualizarSumulaActa(it, sumula, ordem, prox)
          }
        },
        onBack = { viewModel.navigateTo(Screen.Home) }
      )
    }

    is Screen.Definicoes -> {
      DefinicoesScreen(
        auditorias = currentAuditorias,
        onBack = { viewModel.navigateTo(Screen.Home) },
        onAplicarRetencao = { dias -> viewModel.aplicarRetencao(dias) },
        onLimparTodosOsDados = { viewModel.limparTodosOsDadosLocais() },
        onExportarJson = { viewModel.exportarJson() }
      )
    }
  }
}
