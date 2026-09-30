package cv.claudiotavares.acta.data.repository

import android.content.Context
import cv.claudiotavares.acta.data.db.ActaDatabase
import cv.claudiotavares.acta.data.demo.DemoDataProvider
import cv.claudiotavares.acta.data.gemini.GeminiMinutesService
import cv.claudiotavares.acta.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class ActaRepository(private val context: Context) {
  private val database = ActaDatabase.getDatabase(context)
  private val reuniaoDao = database.reuniaoDao()
  private val participanteDao = database.participanteDao()
  private val sessaoDao = database.sessaoDao()
  private val segmentoDao = database.segmentoDao()
  private val actaDao = database.actaDao()
  private val deliberacaoDao = database.deliberacaoDao()
  private val accaoDao = database.accaoDao()
  private val auditoriaDao = database.auditoriaDao()

  private val geminiService = GeminiMinutesService()
  private val prefs = context.getSharedPreferences("acta_preferences", Context.MODE_PRIVATE)

  suspend fun initializeDatabaseIfNeeded() = withContext(Dispatchers.IO) {
    val userCleared = prefs.getBoolean("user_explicitly_cleared_data", false)
    val demoInitialized = prefs.getBoolean("demo_data_initialized", false)

    if (!userCleared && !demoInitialized) {
      val demo = DemoDataProvider.createDemoData()
      reuniaoDao.insertReuniao(demo.reuniao)
      participanteDao.insertAll(demo.participantes)
      sessaoDao.insertSessao(demo.sessao)
      segmentoDao.insertAll(demo.segmentos)
      actaDao.insertActa(demo.acta)
      deliberacaoDao.insertAll(demo.deliberacoes)
      accaoDao.insertAll(demo.accoes)
      demo.auditorias.forEach { auditoriaDao.insert(it) }

      prefs.edit().putBoolean("demo_data_initialized", true).apply()
    }
  }

  // Flows
  fun getAllReunioes(): Flow<List<ReuniaoEntity>> = reuniaoDao.getAllReunioes()
  fun getReuniaoFlow(id: String): Flow<ReuniaoEntity?> = reuniaoDao.getReuniaoByIdFlow(id)
  fun getParticipantesFlow(reuniaoId: String): Flow<List<ParticipanteEntity>> = participanteDao.getParticipantesByReuniao(reuniaoId)
  fun getSegmentosFlow(reuniaoId: String): Flow<List<SegmentoEntity>> = segmentoDao.getSegmentosByReuniao(reuniaoId)
  fun getSegmentosBaixaConfiancaFlow(reuniaoId: String): Flow<List<SegmentoEntity>> = segmentoDao.getSegmentosBaixaConfianca(reuniaoId)
  fun getUltimaActaFlow(reuniaoId: String): Flow<ActaEntity?> = actaDao.getUltimaActa(reuniaoId)
  fun getTodasVersoesFlow(reuniaoId: String): Flow<List<ActaEntity>> = actaDao.getTodasVersoes(reuniaoId)
  fun getDeliberacoesFlow(actaId: String): Flow<List<DeliberacaoEntity>> = deliberacaoDao.getDeliberacoesPorActa(actaId)
  fun getAccoesFlow(actaId: String): Flow<List<AccaoEntity>> = accaoDao.getAccoesPorActa(actaId)
  fun getAuditoriaFlow(reuniaoId: String): Flow<List<RegistoAuditoriaEntity>> = auditoriaDao.getRegistosPorReuniao(reuniaoId)
  fun getAllAuditoriaFlow(): Flow<List<RegistoAuditoriaEntity>> = auditoriaDao.getAllRegistos()

  // Actions
  suspend fun criarReuniao(reuniao: ReuniaoEntity, autor: String = "Secretário(a)") = withContext(Dispatchers.IO) {
    reuniaoDao.insertReuniao(reuniao)
    auditoriaDao.insert(
      RegistoAuditoriaEntity(
        reuniaoId = reuniao.id,
        autor = autor,
        accao = "criacao_reuniao",
        detalhe = "Criação da reunião '${reuniao.titulo}' agendada para ${reuniao.local}."
      )
    )
  }

  suspend fun adicionarParticipante(participante: ParticipanteEntity) = withContext(Dispatchers.IO) {
    participanteDao.insertParticipante(participante)
    auditoriaDao.insert(
      RegistoAuditoriaEntity(
        reuniaoId = participante.reuniaoId,
        autor = "Coordenação",
        accao = "adicao_participante",
        detalhe = "Participante adicionado: ${participante.nome} (${participante.funcao})."
      )
    )
  }

  suspend fun atualizarParticipante(participante: ParticipanteEntity) = withContext(Dispatchers.IO) {
    participanteDao.updateParticipante(participante)
    auditoriaDao.insert(
      RegistoAuditoriaEntity(
        reuniaoId = participante.reuniaoId,
        autor = "Coordenação",
        accao = "atualizacao_participante",
        detalhe = "Atualização de consentimentos para ${participante.nome}: Gravação=${if (participante.consentimentoGravacao) "Sim" else "Não"}, Transcrição=${if (participante.consentimentoTranscricao) "Sim" else "Não"}."
      )
    )
  }

  suspend fun removerParticipante(participante: ParticipanteEntity) = withContext(Dispatchers.IO) {
    participanteDao.deleteParticipante(participante.id)
    auditoriaDao.insert(
      RegistoAuditoriaEntity(
        reuniaoId = participante.reuniaoId,
        autor = "Coordenação",
        accao = "remocao_participante",
        detalhe = "Participante removido: ${participante.nome}."
      )
    )
  }

  /**
   * REGRA DE NEGÓCIO OBRIGATÓRIA 1:
   * "Nunca inicies uma sessão sem que todos os participantes marcados como presentes
   * tenham consentimentoGravacao = true."
   */
  suspend fun verificarConsentimentosParaGravacao(reuniaoId: String): ConsentimentoCheckResult = withContext(Dispatchers.IO) {
    val participantes = participanteDao.getParticipantesList(reuniaoId)
    if (participantes.isEmpty()) {
      return@withContext ConsentimentoCheckResult(
        apto = false,
        totalParticipantes = 0,
        semConsentimento = emptyList(),
        motivoBloqueio = "A reunião não possui participantes registados. Adicione pelo menos um participante com consentimento antes de gravar."
      )
    }

    val semConsentimento = participantes.filter { !it.consentimentoGravacao }
    if (semConsentimento.isNotEmpty()) {
      val nomes = semConsentimento.joinToString(", ") { it.nome }
      return@withContext ConsentimentoCheckResult(
        apto = false,
        totalParticipantes = participantes.size,
        semConsentimento = semConsentimento,
        motivoBloqueio = "Início bloqueado: Os seguintes participantes presentes não têm consentimento de gravação registado: $nomes."
      )
    }

    return@withContext ConsentimentoCheckResult(
      apto = true,
      totalParticipantes = participantes.size,
      semConsentimento = emptyList(),
      motivoBloqueio = null
    )
  }

  suspend fun iniciarSessaoGravacao(reuniaoId: String): SessaoEntity = withContext(Dispatchers.IO) {
    val sessao = SessaoEntity(
      id = UUID.randomUUID().toString(),
      reuniaoId = reuniaoId,
      instanteInicio = System.currentTimeMillis()
    )
    sessaoDao.insertSessao(sessao)

    val reuniao = reuniaoDao.getReuniaoById(reuniaoId)
    if (reuniao != null) {
      reuniaoDao.updateReuniao(reuniao.copy(estado = "em_curso"))
    }

    auditoriaDao.insert(
      RegistoAuditoriaEntity(
        reuniaoId = reuniaoId,
        autor = "Sistema ACTA",
        accao = "inicio_sessao",
        detalhe = "Início formal de sessão de gravação com áudio ativo e transcrição em tempo real."
      )
    )

    sessao
  }

  suspend fun terminarSessaoGravacao(sessaoId: String, reuniaoId: String) = withContext(Dispatchers.IO) {
    val sessoes = sessaoDao.getUltimaSessao(reuniaoId)
    if (sessoes != null) {
      sessaoDao.updateSessao(sessoes.copy(instanteTermo = System.currentTimeMillis()))
    }
    val reuniao = reuniaoDao.getReuniaoById(reuniaoId)
    if (reuniao != null) {
      reuniaoDao.updateReuniao(reuniao.copy(estado = "concluida", dataHoraTermo = System.currentTimeMillis()))
    }

    auditoriaDao.insert(
      RegistoAuditoriaEntity(
        reuniaoId = reuniaoId,
        autor = "Sistema ACTA",
        accao = "fim_sessao",
        detalhe = "Sessão de gravação terminada com sucesso."
      )
    )
  }

  suspend fun gravarSegmentoAoVivo(segmento: SegmentoEntity) = withContext(Dispatchers.IO) {
    segmentoDao.insertSegmento(segmento)
  }

  /**
   * ECRÃ 6: "Ao tocar num rótulo 'spk_N' pela primeira vez, pede para escolher
   * a que participante corresponde; propaga a escolha a todos os segmentos
   * com o mesmo rótulo na sessão."
   */
  suspend fun atribuirOrador(
    sessaoId: String,
    rotuloOrador: String,
    participanteId: String,
    reuniaoId: String
  ) = withContext(Dispatchers.IO) {
    segmentoDao.atribuirParticipanteAOrador(sessaoId, rotuloOrador, participanteId)
    val part = participanteDao.getParticipanteById(participanteId)

    auditoriaDao.insert(
      RegistoAuditoriaEntity(
        reuniaoId = reuniaoId,
        autor = "Utilizador",
        accao = "atribuicao_orador",
        detalhe = "Rótulo '$rotuloOrador' atribuído ao participante '${part?.nome ?: participanteId}' e propagado a todos os segmentos da sessão."
      )
    )
  }

  suspend fun atualizarSegmento(segmento: SegmentoEntity) = withContext(Dispatchers.IO) {
    segmentoDao.updateSegmento(segmento.copy(revisado = true))
    auditoriaDao.insert(
      RegistoAuditoriaEntity(
        reuniaoId = segmento.reuniaoId,
        autor = "Utilizador",
        accao = "edicao_segmento",
        detalhe = "Segmento editado e validado [${segmento.inicioMs}ms - ${segmento.fimMs}ms]."
      )
    )
  }

  suspend fun marcarRevisaoAberta(reuniaoId: String) = withContext(Dispatchers.IO) {
    val segmentos = segmentoDao.getSegmentosList(reuniaoId)
    if (segmentos.isNotEmpty()) {
      segmentoDao.marcarTodosComoRevisados(reuniaoId)
    }

    // Regra: desbloqueia a aprovação na acta atual
    val actas = database.actaDao()
    val reuniao = reuniaoDao.getReuniaoById(reuniaoId)
    // Find latest acta and set revisaoAbertaPeloMenosUmaVez = true
    val ultimaActa = actaDao.getActaPorVersao(reuniaoId, 1) // or latest
    val allVersions = actaDao.getActaPorVersao(reuniaoId, 1)
    // We update all actas for this meeting to indicate review was opened
    val todas = database.openHelper.readableDatabase
    // Direct update query:
    database.runInTransaction {
      database.openHelper.writableDatabase.execSQL(
        "UPDATE actas SET revisaoAbertaPeloMenosUmaVez = 1 WHERE reuniaoId = '$reuniaoId'"
      )
    }

    auditoriaDao.insert(
      RegistoAuditoriaEntity(
        reuniaoId = reuniaoId,
        autor = "Utilizador",
        accao = "revisao_aberta",
        detalhe = "Ecrã de revisão de transcrição acedido. Validação humana iniciada."
      )
    )
  }

  /**
   * Generates structured draft minutes using Gemini service with strict anchor validation.
   */
  suspend fun gerarActa(reuniaoId: String): String = withContext(Dispatchers.IO) {
    val segmentos = segmentoDao.getSegmentosList(reuniaoId)
    val participantes = participanteDao.getParticipantesList(reuniaoId)
    val reuniao = reuniaoDao.getReuniaoById(reuniaoId)

    val todasVersoes = database.openHelper.readableDatabase
    var proximaVersao = 1
    val cursor = database.openHelper.readableDatabase.query(
      "SELECT MAX(versao) FROM actas WHERE reuniaoId = '$reuniaoId'"
    )
    if (cursor.moveToFirst() && !cursor.isNull(0)) {
      proximaVersao = cursor.getInt(0) + 1
    }
    cursor.close()

    val novaActaId = UUID.randomUUID().toString()

    val resultado = geminiService.generateMinutes(
      actaId = novaActaId,
      reuniaoId = reuniaoId,
      versao = proximaVersao,
      segmentos = segmentos,
      participantes = participantes
    )

    val novaActa = ActaEntity(
      id = novaActaId,
      reuniaoId = reuniaoId,
      versao = proximaVersao,
      estado = "rascunho",
      sumulaExecutiva = resultado.sumulaExecutiva,
      ordemDeTrabalhos = resultado.ordemDeTrabalhos,
      proximaReuniao = resultado.proximaReuniao,
      dataCriacao = System.currentTimeMillis(),
      revisaoAbertaPeloMenosUmaVez = false // Must open review before approving!
    )

    actaDao.insertActa(novaActa)
    deliberacaoDao.insertAll(resultado.deliberacoes)
    accaoDao.insertAll(resultado.accoes)

    auditoriaDao.insert(
      RegistoAuditoriaEntity(
        reuniaoId = reuniaoId,
        autor = "Motor de IA ACTA (Gemini)",
        accao = "geracao_acta",
        detalhe = "Proposta de Acta v$proximaVersao gerada com ${resultado.deliberacoes.size} deliberações e ${resultado.accoes.size} ações com âncoras temporais validadas."
      )
    )

    novaActaId
  }

  /**
   * REGRA DE NEGÓCIO OBRIGATÓRIA 4:
   * "Depois de 'aprovada', qualquer alteração à acta cria uma nova versão;
   * a versão anterior mantém-se acessível e imutável."
   */
  suspend fun criarNovaVersaoDeActaAprovada(actaOriginal: ActaEntity): String = withContext(Dispatchers.IO) {
    val novaVersaoNum = actaOriginal.versao + 1
    val novaActaId = UUID.randomUUID().toString()

    val novaActa = actaOriginal.copy(
      id = novaActaId,
      versao = novaVersaoNum,
      estado = "em_revisao",
      dataCriacao = System.currentTimeMillis(),
      dataAprovacao = null,
      aprovadoPor = null,
      revisaoAbertaPeloMenosUmaVez = true
    )

    actaDao.insertActa(novaActa)

    // Clone deliberations
    val delibs = deliberacaoDao.getDeliberacoesList(actaOriginal.id)
    val novasDelibs = delibs.map {
      it.copy(id = UUID.randomUUID().toString(), actaId = novaActaId, versaoActa = novaVersaoNum)
    }
    deliberacaoDao.insertAll(novasDelibs)

    // Clone actions
    val accoes = accaoDao.getAccoesList(actaOriginal.id)
    val novasAccoes = accoes.map {
      it.copy(id = UUID.randomUUID().toString(), actaId = novaActaId, versaoActa = novaVersaoNum)
    }
    accaoDao.insertAll(novasAccoes)

    auditoriaDao.insert(
      RegistoAuditoriaEntity(
        reuniaoId = actaOriginal.reuniaoId,
        autor = "Utilizador",
        accao = "nova_versao_acta",
        detalhe = "Criada nova versão v$novaVersaoNum a partir da v${actaOriginal.versao} aprovada. Versão anterior preservada imutável."
      )
    )

    novaActaId
  }

  /**
   * Aprova a acta formalmente.
   * Só permitido se revisaoAbertaPeloMenosUmaVez == true!
   */
  suspend fun aprovarActa(acta: ActaEntity, nomeAprovador: String) = withContext(Dispatchers.IO) {
    if (!acta.revisaoAbertaPeloMenosUmaVez) {
      throw IllegalStateException("A aprovação da acta exige a revisão prévia da transcrição.")
    }

    val aprovada = acta.copy(
      estado = "aprovada",
      dataAprovacao = System.currentTimeMillis(),
      aprovadoPor = nomeAprovador
    )
    actaDao.updateActa(aprovada)

    auditoriaDao.insert(
      RegistoAuditoriaEntity(
        reuniaoId = acta.reuniaoId,
        autor = nomeAprovador,
        accao = "aprovacao_acta",
        detalhe = "Acta versão ${acta.versao} formalmente aprovada por $nomeAprovador. Documento selado e imutável."
      )
    )
  }

  suspend fun atualizarSumulaActa(acta: ActaEntity, novaSumula: String, novaOrdem: String, novaProx: String) = withContext(Dispatchers.IO) {
    if (acta.estado == "aprovada") {
      // Create new version automatically
      val novaId = criarNovaVersaoDeActaAprovada(acta)
      val nova = actaDao.getActaById(novaId)
      if (nova != null) {
        actaDao.updateActa(nova.copy(sumulaExecutiva = novaSumula, ordemDeTrabalhos = novaOrdem, proximaReuniao = novaProx))
      }
    } else {
      actaDao.updateActa(acta.copy(sumulaExecutiva = novaSumula, ordemDeTrabalhos = novaOrdem, proximaReuniao = novaProx))
      auditoriaDao.insert(
        RegistoAuditoriaEntity(
          reuniaoId = acta.reuniaoId,
          autor = "Utilizador",
          accao = "edicao_acta",
          detalhe = "Atualização do texto da súmula da acta v${acta.versao}."
        )
      )
    }
  }

  suspend fun aplicarPoliticaRetencao(diasLimite: Int) = withContext(Dispatchers.IO) {
    val limiteMs = System.currentTimeMillis() - (diasLimite * 24L * 60L * 60L * 1000L)
    // Find meetings older than limit
    val reunioes = database.openHelper.readableDatabase.query(
      "SELECT id FROM reunioes WHERE dataHoraInicio < $limiteMs"
    )
    val idsParaEliminar = mutableListOf<String>()
    while (reunioes.moveToNext()) {
      idsParaEliminar.add(reunioes.getString(0))
    }
    reunioes.close()

    idsParaEliminar.forEach { id ->
      eliminarReuniao(id)
    }
  }

  suspend fun eliminarReuniao(reuniaoId: String) = withContext(Dispatchers.IO) {
    val reuniao = reuniaoDao.getReuniaoById(reuniaoId)
    reuniaoDao.deleteReuniao(reuniaoId)
    participanteDao.deleteByReuniao(reuniaoId)
    sessaoDao.deleteByReuniao(reuniaoId)
    segmentoDao.deleteByReuniao(reuniaoId)
    actaDao.deleteByReuniao(reuniaoId)
    deliberacaoDao.deleteByReuniao(reuniaoId)
    accaoDao.deleteByReuniao(reuniaoId)
    auditoriaDao.deleteByReuniao(reuniaoId)
  }

  /**
   * CRITÉRIO DE ACEITAÇÃO:
   * "Eliminar todos os dados locais nas definições apaga tudo e volta ao estado vazio
   * (sem os dados de demonstração)."
   */
  suspend fun limparTodosOsDadosLocais() = withContext(Dispatchers.IO) {
    reuniaoDao.clearAll()
    participanteDao.clearAll()
    sessaoDao.clearAll()
    segmentoDao.clearAll()
    actaDao.clearAll()
    deliberacaoDao.clearAll()
    accaoDao.clearAll()
    auditoriaDao.clearAll()

    // Mark that the user explicitly cleared the database, so demo data is NOT re-inserted!
    prefs.edit()
      .putBoolean("user_explicitly_cleared_data", true)
      .putBoolean("demo_data_initialized", true)
      .apply()
  }

  suspend fun exportarTudoParaJson(): String = withContext(Dispatchers.IO) {
    val root = JSONObject()
    val reunioesArr = JSONArray()

    val cursor = database.openHelper.readableDatabase.query("SELECT * FROM reunioes")
    while (cursor.moveToNext()) {
      val obj = JSONObject().apply {
        put("id", cursor.getString(cursor.getColumnIndexOrThrow("id")))
        put("titulo", cursor.getString(cursor.getColumnIndexOrThrow("titulo")))
        put("local", cursor.getString(cursor.getColumnIndexOrThrow("local")))
        put("estado", cursor.getString(cursor.getColumnIndexOrThrow("estado")))
        put("dataHoraInicio", cursor.getLong(cursor.getColumnIndexOrThrow("dataHoraInicio")))
      }
      reunioesArr.put(obj)
    }
    cursor.close()
    root.put("reunioes", reunioesArr)
    root.put("timestampExportacao", System.currentTimeMillis())
    root.put("aplicacao", "ACTA")
    root.toString(2)
  }
}

data class ConsentimentoCheckResult(
  val apto: Boolean,
  val totalParticipantes: Int,
  val semConsentimento: List<ParticipanteEntity>,
  val motivoBloqueio: String?
)
