package cv.claudiotavares.acta.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cv.claudiotavares.acta.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReuniaoDao {
  @Query("SELECT * FROM reunioes ORDER BY dataHoraInicio DESC")
  fun getAllReunioes(): Flow<List<ReuniaoEntity>>

  @Query("SELECT * FROM reunioes WHERE id = :id LIMIT 1")
  suspend fun getReuniaoById(id: String): ReuniaoEntity?

  @Query("SELECT * FROM reunioes WHERE id = :id LIMIT 1")
  fun getReuniaoByIdFlow(id: String): Flow<ReuniaoEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertReuniao(reuniao: ReuniaoEntity)

  @Update
  suspend fun updateReuniao(reuniao: ReuniaoEntity)

  @Query("DELETE FROM reunioes WHERE id = :id")
  suspend fun deleteReuniao(id: String)

  @Query("DELETE FROM reunioes")
  suspend fun clearAll()
}

@Dao
interface ParticipanteDao {
  @Query("SELECT * FROM participantes WHERE reuniaoId = :reuniaoId ORDER BY nome ASC")
  fun getParticipantesByReuniao(reuniaoId: String): Flow<List<ParticipanteEntity>>

  @Query("SELECT * FROM participantes WHERE reuniaoId = :reuniaoId ORDER BY nome ASC")
  suspend fun getParticipantesList(reuniaoId: String): List<ParticipanteEntity>

  @Query("SELECT * FROM participantes WHERE id = :id LIMIT 1")
  suspend fun getParticipanteById(id: String): ParticipanteEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertParticipante(participante: ParticipanteEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(participantes: List<ParticipanteEntity>)

  @Update
  suspend fun updateParticipante(participante: ParticipanteEntity)

  @Query("DELETE FROM participantes WHERE id = :id")
  suspend fun deleteParticipante(id: String)

  @Query("DELETE FROM participantes WHERE reuniaoId = :reuniaoId")
  suspend fun deleteByReuniao(reuniaoId: String)

  @Query("DELETE FROM participantes")
  suspend fun clearAll()
}

@Dao
interface SessaoDao {
  @Query("SELECT * FROM sessoes WHERE reuniaoId = :reuniaoId ORDER BY instanteInicio DESC")
  fun getSessoesByReuniao(reuniaoId: String): Flow<List<SessaoEntity>>

  @Query("SELECT * FROM sessoes WHERE reuniaoId = :reuniaoId ORDER BY instanteInicio DESC LIMIT 1")
  suspend fun getUltimaSessao(reuniaoId: String): SessaoEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSessao(sessao: SessaoEntity)

  @Update
  suspend fun updateSessao(sessao: SessaoEntity)

  @Query("DELETE FROM sessoes WHERE reuniaoId = :reuniaoId")
  suspend fun deleteByReuniao(reuniaoId: String)

  @Query("DELETE FROM sessoes")
  suspend fun clearAll()
}

@Dao
interface SegmentoDao {
  @Query("SELECT * FROM segmentos WHERE reuniaoId = :reuniaoId ORDER BY inicioMs ASC")
  fun getSegmentosByReuniao(reuniaoId: String): Flow<List<SegmentoEntity>>

  @Query("SELECT * FROM segmentos WHERE reuniaoId = :reuniaoId ORDER BY inicioMs ASC")
  suspend fun getSegmentosList(reuniaoId: String): List<SegmentoEntity>

  @Query("SELECT * FROM segmentos WHERE reuniaoId = :reuniaoId AND confianca < 0.75 ORDER BY inicioMs ASC")
  fun getSegmentosBaixaConfianca(reuniaoId: String): Flow<List<SegmentoEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSegmento(segmento: SegmentoEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(segmentos: List<SegmentoEntity>)

  @Update
  suspend fun updateSegmento(segmento: SegmentoEntity)

  @Query("UPDATE segmentos SET participanteId = :participanteId WHERE sessaoId = :sessaoId AND rotuloOrador = :rotuloOrador")
  suspend fun atribuirParticipanteAOrador(sessaoId: String, rotuloOrador: String, participanteId: String)

  @Query("UPDATE segmentos SET revisado = 1 WHERE reuniaoId = :reuniaoId")
  suspend fun marcarTodosComoRevisados(reuniaoId: String)

  @Query("DELETE FROM segmentos WHERE reuniaoId = :reuniaoId")
  suspend fun deleteByReuniao(reuniaoId: String)

  @Query("DELETE FROM segmentos")
  suspend fun clearAll()
}

@Dao
interface ActaDao {
  @Query("SELECT * FROM actas WHERE reuniaoId = :reuniaoId ORDER BY versao DESC LIMIT 1")
  fun getUltimaActa(reuniaoId: String): Flow<ActaEntity?>

  @Query("SELECT * FROM actas WHERE reuniaoId = :reuniaoId ORDER BY versao DESC")
  fun getTodasVersoes(reuniaoId: String): Flow<List<ActaEntity>>

  @Query("SELECT * FROM actas WHERE reuniaoId = :reuniaoId ORDER BY versao DESC")
  suspend fun getTodasVersoesList(reuniaoId: String): List<ActaEntity>

  @Query("SELECT * FROM actas WHERE reuniaoId = :reuniaoId AND versao = :versao LIMIT 1")
  suspend fun getActaPorVersao(reuniaoId: String, versao: Int): ActaEntity?

  @Query("SELECT * FROM actas WHERE id = :id LIMIT 1")
  suspend fun getActaById(id: String): ActaEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertActa(acta: ActaEntity)

  @Update
  suspend fun updateActa(acta: ActaEntity)

  @Query("DELETE FROM actas WHERE reuniaoId = :reuniaoId")
  suspend fun deleteByReuniao(reuniaoId: String)

  @Query("DELETE FROM actas")
  suspend fun clearAll()
}

@Dao
interface DeliberacaoDao {
  @Query("SELECT * FROM deliberacoes WHERE actaId = :actaId ORDER BY ancoraInicioMs ASC")
  fun getDeliberacoesPorActa(actaId: String): Flow<List<DeliberacaoEntity>>

  @Query("SELECT * FROM deliberacoes WHERE actaId = :actaId ORDER BY ancoraInicioMs ASC")
  suspend fun getDeliberacoesList(actaId: String): List<DeliberacaoEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(deliberacoes: List<DeliberacaoEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(deliberacao: DeliberacaoEntity)

  @Update
  suspend fun update(deliberacao: DeliberacaoEntity)

  @Query("DELETE FROM deliberacoes WHERE actaId = :actaId")
  suspend fun deletePorActa(actaId: String)

  @Query("DELETE FROM deliberacoes WHERE reuniaoId = :reuniaoId")
  suspend fun deleteByReuniao(reuniaoId: String)

  @Query("DELETE FROM deliberacoes")
  suspend fun clearAll()
}

@Dao
interface AccaoDao {
  @Query("SELECT * FROM accoes WHERE actaId = :actaId ORDER BY ancoraInicioMs ASC")
  fun getAccoesPorActa(actaId: String): Flow<List<AccaoEntity>>

  @Query("SELECT * FROM accoes WHERE actaId = :actaId ORDER BY ancoraInicioMs ASC")
  suspend fun getAccoesList(actaId: String): List<AccaoEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(accoes: List<AccaoEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(accao: AccaoEntity)

  @Update
  suspend fun update(accao: AccaoEntity)

  @Query("DELETE FROM accoes WHERE actaId = :actaId")
  suspend fun deletePorActa(actaId: String)

  @Query("DELETE FROM accoes WHERE reuniaoId = :reuniaoId")
  suspend fun deleteByReuniao(reuniaoId: String)

  @Query("DELETE FROM accoes")
  suspend fun clearAll()
}

@Dao
interface AuditoriaDao {
  @Query("SELECT * FROM auditoria ORDER BY timestamp DESC")
  fun getAllRegistos(): Flow<List<RegistoAuditoriaEntity>>

  @Query("SELECT * FROM auditoria WHERE reuniaoId = :reuniaoId ORDER BY timestamp DESC")
  fun getRegistosPorReuniao(reuniaoId: String): Flow<List<RegistoAuditoriaEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(registo: RegistoAuditoriaEntity)

  @Query("DELETE FROM auditoria WHERE reuniaoId = :reuniaoId")
  suspend fun deleteByReuniao(reuniaoId: String)

  @Query("DELETE FROM auditoria")
  suspend fun clearAll()
}
