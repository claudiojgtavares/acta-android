package cv.claudiotavares.acta.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import cv.claudiotavares.acta.data.model.*

@Database(
  entities = [
    ReuniaoEntity::class,
    ParticipanteEntity::class,
    SessaoEntity::class,
    SegmentoEntity::class,
    ActaEntity::class,
    DeliberacaoEntity::class,
    AccaoEntity::class,
    RegistoAuditoriaEntity::class
  ],
  version = 1,
  exportSchema = false
)
abstract class ActaDatabase : RoomDatabase() {
  abstract fun reuniaoDao(): ReuniaoDao
  abstract fun participanteDao(): ParticipanteDao
  abstract fun sessaoDao(): SessaoDao
  abstract fun segmentoDao(): SegmentoDao
  abstract fun actaDao(): ActaDao
  abstract fun deliberacaoDao(): DeliberacaoDao
  abstract fun accaoDao(): AccaoDao
  abstract fun auditoriaDao(): AuditoriaDao

  companion object {
    @Volatile
    private var INSTANCE: ActaDatabase? = null

    fun getDatabase(context: Context): ActaDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          ActaDatabase::class.java,
          "acta_database"
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
