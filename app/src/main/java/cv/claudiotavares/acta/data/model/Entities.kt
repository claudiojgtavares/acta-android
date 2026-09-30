package cv.claudiotavares.acta.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "reunioes")
data class ReuniaoEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val titulo: String,
  val dataHoraInicio: Long = System.currentTimeMillis(),
  val dataHoraTermo: Long? = null,
  val local: String = "Sala Principal / Presencial",
  val linguaOrigem: String = "Português (Portugal)",
  val linguaAlvo: String = "Português (Portugal)",
  val estado: String = "agendada" // agendada | em_curso | concluida
)

@Entity(
  tableName = "participantes",
  indices = [Index("reuniaoId")]
)
data class ParticipanteEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val reuniaoId: String,
  val nome: String,
  val funcao: String,
  val email: String,
  val consentimentoGravacao: Boolean = false,
  val consentimentoTranscricao: Boolean = false
)

@Entity(
  tableName = "sessoes",
  indices = [Index("reuniaoId")]
)
data class SessaoEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val reuniaoId: String,
  val instanteInicio: Long = System.currentTimeMillis(),
  val instanteTermo: Long? = null
)

@Entity(
  tableName = "segmentos",
  indices = [Index("sessaoId"), Index("reuniaoId")]
)
data class SegmentoEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val sessaoId: String,
  val reuniaoId: String,
  val inicioMs: Long,
  val fimMs: Long,
  val texto: String,
  val confianca: Float = 0.95f, // Se < 0.75f, baixa confiança
  val rotuloOrador: String = "spk_1", // spk_1, spk_2...
  val participanteId: String? = null,
  val revisado: Boolean = false
)

@Entity(
  tableName = "actas",
  indices = [Index("reuniaoId")]
)
data class ActaEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val reuniaoId: String,
  val versao: Int = 1,
  val estado: String = "rascunho", // rascunho | em_revisao | aprovada
  val sumulaExecutiva: String,
  val ordemDeTrabalhos: String = "1. Ponto de situação dos projetos.\n2. Orçamento e prazos.\n3. Decisões operacionais.",
  val proximaReuniao: String = "A definir oportunamente pela coordenação.",
  val dataCriacao: Long = System.currentTimeMillis(),
  val dataAprovacao: Long? = null,
  val aprovadoPor: String? = null,
  val revisaoAbertaPeloMenosUmaVez: Boolean = false
)

@Entity(
  tableName = "deliberacoes",
  indices = [Index("actaId"), Index("reuniaoId")]
)
data class DeliberacaoEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val actaId: String,
  val reuniaoId: String,
  val versaoActa: Int = 1,
  val texto: String,
  val ancoraInicioMs: Long, // OBRIGATÓRIO, nunca nulo
  val ancoraFimMs: Long,   // OBRIGATÓRIO, nunca nulo
  val verificada: Boolean = true
)

@Entity(
  tableName = "accoes",
  indices = [Index("actaId"), Index("reuniaoId")]
)
data class AccaoEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val actaId: String,
  val reuniaoId: String,
  val versaoActa: Int = 1,
  val descricao: String,
  val responsavelId: String? = null,
  val responsavelNome: String? = null,
  val prazo: String? = null,
  val estado: String = "por atribuir", // "por atribuir" se sem responsável, "atribuida", "concluida"
  val ancoraInicioMs: Long, // OBRIGATÓRIO, nunca nulo
  val ancoraFimMs: Long,   // OBRIGATÓRIO, nunca nulo
  val verificada: Boolean = true
)

@Entity(tableName = "auditoria")
data class RegistoAuditoriaEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val reuniaoId: String,
  val timestamp: Long = System.currentTimeMillis(),
  val autor: String = "Secretário(a) da Reunião",
  val accao: String,
  val detalhe: String
)
