package cv.claudiotavares.acta

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import cv.claudiotavares.acta.data.db.ActaDatabase
import cv.claudiotavares.acta.data.gemini.GeminiMinutesService
import cv.claudiotavares.acta.data.model.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ActaBusinessRulesTest {

  private lateinit var db: ActaDatabase
  private lateinit var context: Context

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    db = Room.inMemoryDatabaseBuilder(context, ActaDatabase::class.java)
      .allowMainThreadQueries()
      .build()
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun testAppNameIsACTA() {
    val appName = context.getString(R.string.app_name)
    assertEquals("ACTA", appName)
  }

  @Test
  fun testRegraConsentimentoObrigatorioParaGravacao() = runBlocking {
    val reuniaoId = "reuniao-teste-1"
    val part1 = ParticipanteEntity(
      reuniaoId = reuniaoId,
      nome = "Dr. Manuel Santos",
      funcao = "Presidente",
      email = "manuel@example.test",
      consentimentoGravacao = true,
      consentimentoTranscricao = true
    )
    val part2 = ParticipanteEntity(
      reuniaoId = reuniaoId,
      nome = "Eng. Rui Silva",
      funcao = "Diretor",
      email = "rui@example.test",
      consentimentoGravacao = false, // Não consentiu!
      consentimentoTranscricao = true
    )

    db.participanteDao().insertAll(listOf(part1, part2))

    val participantes = db.participanteDao().getParticipantesList(reuniaoId)
    val semConsentimento = participantes.filter { !it.consentimentoGravacao }

    // Business Rule 1 check
    assertTrue("Deve identificar participantes sem consentimento", semConsentimento.isNotEmpty())
    assertEquals(1, semConsentimento.size)
    assertEquals("Eng. Rui Silva", semConsentimento.first().nome)
  }

  @Test
  fun testValidacaoRigorosaDeAncorasTemporais() {
    val service = GeminiMinutesService()
    val duracaoMs = 60_000L // 1 minuto
    val segmentos = listOf(
      SegmentoEntity(
        sessaoId = "s1",
        reuniaoId = "r1",
        inicioMs = 0L,
        fimMs = 30_000L,
        texto = "Aprovado o orçamento para 2027.",
        confianca = 0.95f,
        rotuloOrador = "spk_0"
      ),
      SegmentoEntity(
        sessaoId = "s1",
        reuniaoId = "r1",
        inicioMs = 30_000L,
        fimMs = 60_000L,
        texto = "Ficou decidido avançar com o concurso público.",
        confianca = 0.92f,
        rotuloOrador = "spk_1"
      )
    )

    // Deliberação com âncora válida dentro da sessão
    val delibValida = DeliberacaoEntity(
      actaId = "acta-1",
      reuniaoId = "r1",
      versaoActa = 1,
      texto = "Aprovado o orçamento para 2027.",
      ancoraInicioMs = 5_000L,
      ancoraFimMs = 25_000L,
      verificada = false
    )

    // Deliberação com âncora fora da duração (inválida)
    val delibInvalida = DeliberacaoEntity(
      actaId = "acta-1",
      reuniaoId = "r1",
      versaoActa = 1,
      texto = "Deliberação inventada.",
      ancoraInicioMs = 120_000L,
      ancoraFimMs = 180_000L,
      verificada = false
    )

    val fakeResult = cv.claudiotavares.acta.data.gemini.GeneratedActaResult(
      sumulaExecutiva = "Súmula",
      ordemDeTrabalhos = "Ordem",
      proximaReuniao = "Próxima",
      deliberacoes = listOf(delibValida, delibInvalida),
      accoes = emptyList()
    )

    val validadas = service.validateAndEnforceAnchors(
      result = fakeResult,
      segmentos = segmentos
    )

    val d1 = validadas.deliberacoes.find { it.texto.contains("orçamento") }!!
    assertTrue("Âncora dentro da sessão deve ser verificada", d1.verificada)

    val d2 = validadas.deliberacoes.find { it.texto.contains("inventada") }!!
    assertFalse("Âncora fora da sessão não deve ser verificada", d2.verificada)
  }

  @Test
  fun testAccaoSemResponsavelMarcadaPorAtribuir() {
    val accao = AccaoEntity(
      actaId = "acta-1",
      reuniaoId = "r1",
      versaoActa = 1,
      descricao = "Elaborar caderno de encargos",
      responsavelId = null,
      responsavelNome = null,
      prazo = "30 dias",
      estado = "por atribuir",
      ancoraInicioMs = 10_000L,
      ancoraFimMs = 20_000L,
      verificada = true
    )

    assertEquals("por atribuir", accao.estado)
    assertNull(accao.responsavelId)
  }

  @Test
  fun testVersoesImutaveisEAuditoria() = runBlocking {
    val actaOriginal = ActaEntity(
      id = "acta-v1",
      reuniaoId = "r1",
      versao = 1,
      estado = "aprovada",
      sumulaExecutiva = "Súmula original aprovada.",
      ordemDeTrabalhos = "Ponto 1",
      proximaReuniao = "2027-01-01",
      aprovadoPor = "Dra. Teresa",
      dataAprovacao = System.currentTimeMillis()
    )

    db.actaDao().insertActa(actaOriginal)

    // Qualquer alteração cria v2, mantendo v1 inalterada
    val novaActa = actaOriginal.copy(
      id = "acta-v2",
      versao = 2,
      estado = "em_revisao",
      sumulaExecutiva = "Súmula com aditamento v2.",
      aprovadoPor = null,
      dataAprovacao = null
    )
    db.actaDao().insertActa(novaActa)

    val versoes = db.actaDao().getTodasVersoesList("r1")
    assertEquals(2, versoes.size)
    assertEquals("aprovada", versoes.find { it.versao == 1 }?.estado)
    assertEquals("em_revisao", versoes.find { it.versao == 2 }?.estado)
  }
}
