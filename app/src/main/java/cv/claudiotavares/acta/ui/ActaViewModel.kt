package cv.claudiotavares.acta.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cv.claudiotavares.acta.audio.AudioRecorderManager
import cv.claudiotavares.acta.data.model.*
import cv.claudiotavares.acta.data.repository.ActaRepository
import cv.claudiotavares.acta.data.repository.ConsentimentoCheckResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class Screen {
  object Home : Screen()
  object NovaReuniao : Screen()
  data class Participantes(val reuniaoId: String) : Screen()
  data class Sessao(val reuniaoId: String) : Screen()
  data class Revisao(val reuniaoId: String) : Screen()
  data class Acta(val reuniaoId: String) : Screen()
  object Definicoes : Screen()
}

class ActaViewModel(application: Application) : AndroidViewModel(application) {
  private val repository = ActaRepository(application)
  val audioRecorder = AudioRecorderManager(application)

  private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
  val currentScreen = _currentScreen.asStateFlow()

  private val _selectedReuniaoId = MutableStateFlow<String?>(null)
  val selectedReuniaoId = _selectedReuniaoId.asStateFlow()

  private val _activeSessao = MutableStateFlow<SessaoEntity?>(null)
  val activeSessao = _activeSessao.asStateFlow()

  private val _selectedActaVersao = MutableStateFlow<Int?>(null)
  val selectedActaVersao = _selectedActaVersao.asStateFlow()

  private val _consentimentoCheck = MutableStateFlow<ConsentimentoCheckResult?>(null)
  val consentimentoCheck = _consentimentoCheck.asStateFlow()

  private val _highlightedAnchorMs = MutableStateFlow<Pair<Long, Long>?>(null)
  val highlightedAnchorMs = _highlightedAnchorMs.asStateFlow()

  val reunioes = repository.getAllReunioes().stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    emptyList()
  )

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val currentReuniao = _selectedReuniaoId.flatMapLatest { id ->
    if (id != null) repository.getReuniaoFlow(id) else flowOf(null)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val currentParticipantes = _selectedReuniaoId.flatMapLatest { id ->
    if (id != null) repository.getParticipantesFlow(id) else flowOf(emptyList())
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val currentSegmentos = _selectedReuniaoId.flatMapLatest { id ->
    if (id != null) repository.getSegmentosFlow(id) else flowOf(emptyList())
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val currentSegmentosBaixaConfianca = _selectedReuniaoId.flatMapLatest { id ->
    if (id != null) repository.getSegmentosBaixaConfiancaFlow(id) else flowOf(emptyList())
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val todasVersoesActa = _selectedReuniaoId.flatMapLatest { id ->
    if (id != null) repository.getTodasVersoesFlow(id) else flowOf(emptyList())
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val currentActa = combine(_selectedReuniaoId, _selectedActaVersao, todasVersoesActa) { id, versao, todas ->
    if (id == null || todas.isEmpty()) null
    else if (versao != null) todas.find { it.versao == versao } ?: todas.firstOrNull()
    else todas.firstOrNull()
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val currentDeliberacoes = currentActa.flatMapLatest { acta ->
    if (acta != null) repository.getDeliberacoesFlow(acta.id) else flowOf(emptyList())
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val currentAccoes = currentActa.flatMapLatest { acta ->
    if (acta != null) repository.getAccoesFlow(acta.id) else flowOf(emptyList())
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val currentAuditorias = _selectedReuniaoId.flatMapLatest { id ->
    if (id != null) repository.getAuditoriaFlow(id) else repository.getAllAuditoriaFlow()
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  init {
    viewModelScope.launch {
      repository.initializeDatabaseIfNeeded()
    }

    // Listen for live segments during active recording
    viewModelScope.launch {
      audioRecorder.segmentEvents.collect { event ->
        val reuniaoId = _selectedReuniaoId.value ?: return@collect
        val sessaoId = _activeSessao.value?.id ?: return@collect

        val seg = SegmentoEntity(
          sessaoId = sessaoId,
          reuniaoId = reuniaoId,
          inicioMs = event.inicioMs,
          fimMs = event.fimMs,
          texto = event.texto,
          confianca = event.confianca,
          rotuloOrador = event.rotuloOrador
        )
        repository.gravarSegmentoAoVivo(seg)
      }
    }
  }

  fun navigateTo(screen: Screen) {
    _currentScreen.value = screen
    when (screen) {
      is Screen.Participantes -> _selectedReuniaoId.value = screen.reuniaoId
      is Screen.Sessao -> _selectedReuniaoId.value = screen.reuniaoId
      is Screen.Revisao -> {
        _selectedReuniaoId.value = screen.reuniaoId
        viewModelScope.launch {
          repository.marcarRevisaoAberta(screen.reuniaoId)
        }
      }
      is Screen.Acta -> {
        _selectedReuniaoId.value = screen.reuniaoId
        _selectedActaVersao.value = null
      }
      else -> {}
    }
  }

  fun selectReuniao(id: String) {
    _selectedReuniaoId.value = id
  }

  fun selectActaVersao(versao: Int) {
    _selectedActaVersao.value = versao
  }

  fun highlightAnchor(inicioMs: Long, fimMs: Long) {
    _highlightedAnchorMs.value = Pair(inicioMs, fimMs)
  }

  fun clearHighlightedAnchor() {
    _highlightedAnchorMs.value = null
  }

  fun checkConsentimento(reuniaoId: String) {
    viewModelScope.launch {
      _consentimentoCheck.value = repository.verificarConsentimentosParaGravacao(reuniaoId)
    }
  }

  fun iniciarSessaoGravacao(reuniaoId: String, onDenied: (String) -> Unit = {}) {
    viewModelScope.launch {
      val check = repository.verificarConsentimentosParaGravacao(reuniaoId)
      _consentimentoCheck.value = check
      if (!check.apto) {
        onDenied(check.motivoBloqueio ?: "Consentimento de gravação não verificado.")
        return@launch
      }

      val sessao = repository.iniciarSessaoGravacao(reuniaoId)
      _activeSessao.value = sessao
      audioRecorder.startRecording()
      _currentScreen.value = Screen.Sessao(reuniaoId)
    }
  }

  fun pausarGravacao() {
    audioRecorder.pauseRecording()
  }

  fun retomarGravacao() {
    audioRecorder.resumeRecording()
  }

  fun terminarGravacao(reuniaoId: String) {
    viewModelScope.launch {
      audioRecorder.stopRecording()
      val sessaoId = _activeSessao.value?.id ?: "sessao-${System.currentTimeMillis()}"
      repository.terminarSessaoGravacao(sessaoId, reuniaoId)
      _activeSessao.value = null

      // Automatically generate proposal minutes
      val novaActaId = repository.gerarActa(reuniaoId)
      _currentScreen.value = Screen.Acta(reuniaoId)
    }
  }

  fun criarNovaReuniao(
    titulo: String,
    local: String,
    linguaOrigem: String,
    linguaAlvo: String,
    onSuccess: (String) -> Unit
  ) {
    viewModelScope.launch {
      val nova = ReuniaoEntity(
        titulo = titulo.ifBlank { "Reunião de Coordenação" },
        local = local.ifBlank { "Sala Principal / Online" },
        linguaOrigem = linguaOrigem,
        linguaAlvo = linguaAlvo,
        estado = "agendada"
      )
      repository.criarReuniao(nova)
      _selectedReuniaoId.value = nova.id
      onSuccess(nova.id)
    }
  }

  fun adicionarParticipante(
    reuniaoId: String,
    nome: String,
    funcao: String,
    email: String,
    consentiuGravacao: Boolean,
    consentiuTranscricao: Boolean
  ) {
    viewModelScope.launch {
      val part = ParticipanteEntity(
        reuniaoId = reuniaoId,
        nome = nome,
        funcao = funcao,
        email = email,
        consentimentoGravacao = consentiuGravacao,
        consentimentoTranscricao = consentiuTranscricao
      )
      repository.adicionarParticipante(part)
      checkConsentimento(reuniaoId)
    }
  }

  fun atualizarParticipante(participante: ParticipanteEntity) {
    viewModelScope.launch {
      repository.atualizarParticipante(participante)
      checkConsentimento(participante.reuniaoId)
    }
  }

  fun removerParticipante(participante: ParticipanteEntity) {
    viewModelScope.launch {
      repository.removerParticipante(participante)
      checkConsentimento(participante.reuniaoId)
    }
  }

  fun atribuirOrador(rotuloOrador: String, participanteId: String) {
    val sessaoId = _activeSessao.value?.id ?: currentSegmentos.value.firstOrNull()?.sessaoId ?: return
    val reuniaoId = _selectedReuniaoId.value ?: return

    viewModelScope.launch {
      repository.atribuirOrador(sessaoId, rotuloOrador, participanteId, reuniaoId)
    }
  }

  fun atualizarSegmento(segmento: SegmentoEntity) {
    viewModelScope.launch {
      repository.atualizarSegmento(segmento)
    }
  }

  fun gerarActa(reuniaoId: String) {
    viewModelScope.launch {
      repository.gerarActa(reuniaoId)
    }
  }

  fun aprovarActa(acta: ActaEntity, nomeAprovador: String, onError: (String) -> Unit = {}) {
    viewModelScope.launch {
      try {
        repository.aprovarActa(acta, nomeAprovador)
      } catch (e: Exception) {
        onError(e.message ?: "Erro ao aprovar a acta.")
      }
    }
  }

  fun atualizarSumulaActa(acta: ActaEntity, sumula: String, ordem: String, prox: String) {
    viewModelScope.launch {
      repository.atualizarSumulaActa(acta, sumula, ordem, prox)
    }
  }

  fun aplicarRetencao(dias: Int) {
    viewModelScope.launch {
      repository.aplicarPoliticaRetencao(dias)
    }
  }

  fun limparTodosOsDadosLocais() {
    viewModelScope.launch {
      repository.limparTodosOsDadosLocais()
      _selectedReuniaoId.value = null
      _activeSessao.value = null
      _selectedActaVersao.value = null
      _currentScreen.value = Screen.Home
    }
  }

  suspend fun exportarJson(): String {
    return repository.exportarTudoParaJson()
  }
}
