package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey val id: Int = 1,
    val language: String = "english", // "bangla" or "english"
    val city: String = "Dhaka",
    val themeMode: String = "light", // "system", "light", "dark"
    val notificationsEnabled: Boolean = true,
    val userName: String = "User",
    val userTitle: String = "Namaz Goal-Setter"
)

@Entity(tableName = "prayer_completion")
data class PrayerCompletion(
    @PrimaryKey val date: String, // "YYYY-MM-DD"
    val fajr: Boolean = false,
    val dhuhr: Boolean = false,
    val asr: Boolean = false,
    val maghrib: Boolean = false,
    val isha: Boolean = false
)

@Dao
interface NamazDao {
    @Query("SELECT * FROM user_settings WHERE id = 1")
    fun getSettingsFlow(): Flow<UserSettings?>

    @Query("SELECT * FROM user_settings WHERE id = 1")
    suspend fun getSettings(): UserSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: UserSettings)

    @Query("SELECT * FROM prayer_completion WHERE date = :date")
    fun getPrayerCompletionFlow(date: String): Flow<PrayerCompletion?>

    @Query("SELECT * FROM prayer_completion WHERE date = :date")
    suspend fun getPrayerCompletion(date: String): PrayerCompletion?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePrayerCompletion(completion: PrayerCompletion)

    @Query("SELECT * FROM prayer_completion")
    fun getAllCompletionsFlow(): Flow<List<PrayerCompletion>>
}

@Database(entities = [UserSettings::class, PrayerCompletion::class], version = 1, exportSchema = false)
abstract class NamazDatabase : RoomDatabase() {
    abstract fun namazDao(): NamazDao

    companion object {
        @Volatile
        private var INSTANCE: NamazDatabase? = null

        fun getDatabase(context: Context): NamazDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NamazDatabase::class.java,
                    "namaz_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class NamazRepository(private val dao: NamazDao) {
    val settingsFlow: Flow<UserSettings?> = dao.getSettingsFlow()
    val allCompletionsFlow: Flow<List<PrayerCompletion>> = dao.getAllCompletionsFlow()

    suspend fun getSettings(): UserSettings {
        return dao.getSettings() ?: UserSettings().also { dao.saveSettings(it) }
    }

    suspend fun saveSettings(settings: UserSettings) {
        dao.saveSettings(settings)
    }

    fun getPrayerCompletionFlow(date: String): Flow<PrayerCompletion?> {
        return dao.getPrayerCompletionFlow(date)
    }

    suspend fun getPrayerCompletion(date: String): PrayerCompletion {
        return dao.getPrayerCompletion(date) ?: PrayerCompletion(date = date).also { dao.savePrayerCompletion(it) }
    }

    suspend fun savePrayerCompletion(completion: PrayerCompletion) {
        dao.savePrayerCompletion(completion)
    }
}
