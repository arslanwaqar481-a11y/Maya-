package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [ReminderEntity::class, CommandLogEntity::class], version = 1, exportSchema = false)
abstract class JarvisDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
    abstract fun commandLogDao(): CommandLogDao

    companion object {
        @Volatile
        private var INSTANCE: JarvisDatabase? = null

        fun getDatabase(context: Context): JarvisDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JarvisDatabase::class.java,
                    "jarvis_core_db"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getDatabase(context).reminderDao()
                            dao.insertReminder(
                                ReminderEntity(
                                    title = "Morning Diagnostics & Briefing",
                                    timeLabel = "08:00 AM",
                                    category = "Routine",
                                    isCompleted = false,
                                    priority = "High"
                                )
                            )
                            dao.insertReminder(
                                ReminderEntity(
                                    title = "Team Sync & Operations Check",
                                    timeLabel = "11:30 AM",
                                    category = "Work",
                                    isCompleted = false,
                                    priority = "Normal"
                                )
                            )
                            dao.insertReminder(
                                ReminderEntity(
                                    title = "Hydration & Physical Recharge",
                                    timeLabel = "03:00 PM",
                                    category = "Health",
                                    isCompleted = false,
                                    priority = "Normal"
                                )
                            )
                            dao.insertReminder(
                                ReminderEntity(
                                    title = "Evening Power Down Protocol",
                                    timeLabel = "09:30 PM",
                                    category = "Routine",
                                    isCompleted = false,
                                    priority = "High"
                                )
                            )

                            val logDao = getDatabase(context).commandLogDao()
                            logDao.insertLog(
                                CommandLogEntity(
                                    commandText = "System initialization",
                                    responseText = "JARVIS Core Online. All sub-systems operational.",
                                    actionType = "SYSTEM_INIT",
                                    isSuccess = true
                                )
                            )
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
