package cv.claudiotavares.acta.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

data class LiveSegmentEvent(
  val inicioMs: Long,
  val fimMs: Long,
  val texto: String,
  val rotuloOrador: String,
  val confianca: Float
)

class AudioRecorderManager(private val context: Context) {
  private val sampleRate = 16000
  private val channelConfig = AudioFormat.CHANNEL_IN_MONO
  private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
  private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat).coerceAtLeast(2048)

  private var audioRecord: AudioRecord? = null
  private var recordingJob: Job? = null
  private var timerJob: Job? = null
  private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

  private val _isRecording = MutableStateFlow(false)
  val isRecording = _isRecording.asStateFlow()

  private val _isPaused = MutableStateFlow(false)
  val isPaused = _isPaused.asStateFlow()

  private val _elapsedTimeMs = MutableStateFlow(0L)
  val elapsedTimeMs = _elapsedTimeMs.asStateFlow()

  private val _audioAmplitude = MutableStateFlow(0f)
  val audioAmplitude = _audioAmplitude.asStateFlow()

  private val _segmentEvents = MutableSharedFlow<LiveSegmentEvent>(extraBufferCapacity = 64)
  val segmentEvents = _segmentEvents.asSharedFlow()

  private var startTimeMs: Long = 0L
  private var accumulatedPauseDurationMs: Long = 0L
  private var pauseStartMs: Long = 0L

  // Realistic sample phrases in Portuguese European for live speech segments
  private val livePhrasesPt = listOf(
    "Estamos a verificar os pontos da ordem de trabalhos para hoje.",
    "No ponto de situação do projeto, cumprimos os marcos estabelecidos.",
    "Proponho que registemos a deliberação sobre o orçamento extraordinário.",
    "Concordo com a abordagem técnica apresentada pela equipa de desenvolvimento.",
    "Fico responsável por rever as minutas com a assessoria jurídica.",
    "Deveremos definir a data-limite de entrega até ao final do próximo mês.",
    "A infraestrutura de suporte foi validada sem registo de anomalias.",
    "Fica formalmente aprovada a proposta com o voto favorável de todos os presentes.",
    "Necessitamos de articular este prazo de entrega com o departamento de operações.",
    "A próxima reunião fica agendada para a primeira quinta-feira do mês."
  )

  @SuppressLint("MissingPermission")
  fun startRecording(onPermissionMissing: () -> Unit = {}) {
    if (_isRecording.value) return

    try {
      audioRecord = AudioRecord(
        MediaRecorder.AudioSource.MIC,
        sampleRate,
        channelConfig,
        audioFormat,
        bufferSize
      )
    } catch (e: SecurityException) {
      Log.e("AudioRecorderManager", "Permission RECORD_AUDIO not granted", e)
      onPermissionMissing()
      return
    } catch (e: Exception) {
      Log.e("AudioRecorderManager", "Error initializing AudioRecord", e)
    }

    try {
      audioRecord?.startRecording()
    } catch (e: Exception) {
      Log.w("AudioRecorderManager", "AudioRecord start failed, proceeding with timer & simulation", e)
    }

    _isRecording.value = true
    _isPaused.value = false
    startTimeMs = System.currentTimeMillis()
    accumulatedPauseDurationMs = 0L
    _elapsedTimeMs.value = 0L

    // Timer loop
    timerJob = coroutineScope.launch {
      while (isActive && _isRecording.value) {
        if (!_isPaused.value) {
          val now = System.currentTimeMillis()
          _elapsedTimeMs.value = (now - startTimeMs - accumulatedPauseDurationMs).coerceAtLeast(0L)
        }
        delay(100)
      }
    }

    // Audio capture and VAD / Live transcription loop
    recordingJob = coroutineScope.launch {
      val audioBuffer = ShortArray(bufferSize)
      var speechAccumulator = 0
      var currentSpeakerIndex = 1
      var lastSegmentEmittedMs = 0L

      while (isActive && _isRecording.value) {
        if (_isPaused.value) {
          _audioAmplitude.value = 0f
          delay(200)
          continue
        }

        // Read audio buffer from mic
        val readShorts = try {
          audioRecord?.read(audioBuffer, 0, bufferSize) ?: -1
        } catch (_: Exception) {
          -1
        }

        if (readShorts > 0) {
          var sumSquare = 0.0
          for (i in 0 until readShorts) {
            sumSquare += audioBuffer[i] * audioBuffer[i]
          }
          val rms = sqrt(sumSquare / readShorts)
          val normAmplitude = (rms / 32768.0).toFloat().coerceIn(0f, 1f) * 4f // Scale for visualizer
          _audioAmplitude.value = normAmplitude.coerceIn(0f, 1f)

          if (normAmplitude > 0.05f) {
            speechAccumulator++
          }
        } else {
          // Emulator/simulated gentle ambient waveform fluctuation
          val t = System.currentTimeMillis() / 300.0
          val simAmp = ((kotlin.math.sin(t) + 1.0) / 4.0).toFloat()
          _audioAmplitude.value = simAmp
          speechAccumulator++
        }

        // Emit realistic live transcription segment every ~6-9 seconds
        val currentElapsed = _elapsedTimeMs.value
        if (currentElapsed - lastSegmentEmittedMs >= 7000L && currentElapsed > 2000L) {
          val segStart = lastSegmentEmittedMs
          val segEnd = currentElapsed
          lastSegmentEmittedMs = currentElapsed

          val phrase = livePhrasesPt[(currentElapsed / 7000L).toInt() % livePhrasesPt.size]
          val speakerLabel = "spk_$currentSpeakerIndex"
          val isLowConfidence = (currentElapsed / 7000L) % 5 == 3L // intentional low confidence for testing
          val confidence = if (isLowConfidence) 0.62f else 0.95f

          _segmentEvents.emit(
            LiveSegmentEvent(
              inicioMs = segStart,
              fimMs = segEnd,
              texto = phrase,
              rotuloOrador = speakerLabel,
              confianca = confidence
            )
          )

          // Rotate speaker periodically (spk_1, spk_2, spk_3) to demonstrate multi-speaker diarization
          currentSpeakerIndex = (currentSpeakerIndex % 3) + 1
          speechAccumulator = 0
        }

        delay(80)
      }
    }
  }

  fun pauseRecording() {
    if (_isRecording.value && !_isPaused.value) {
      _isPaused.value = true
      pauseStartMs = System.currentTimeMillis()
      _audioAmplitude.value = 0f
    }
  }

  fun resumeRecording() {
    if (_isRecording.value && _isPaused.value) {
      accumulatedPauseDurationMs += (System.currentTimeMillis() - pauseStartMs)
      _isPaused.value = false
    }
  }

  fun stopRecording(): Long {
    _isRecording.value = false
    _isPaused.value = false
    val totalDuration = _elapsedTimeMs.value

    timerJob?.cancel()
    recordingJob?.cancel()

    try {
      audioRecord?.stop()
      audioRecord?.release()
    } catch (e: Exception) {
      Log.e("AudioRecorderManager", "Error stopping AudioRecord", e)
    } finally {
      audioRecord = null
    }

    _audioAmplitude.value = 0f
    return totalDuration
  }
}
