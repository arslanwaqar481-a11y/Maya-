package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val timeLabel: String,
    val category: String, // e.g. "Work", "Personal", "Health", "Routine"
    val isCompleted: Boolean = false,
    val priority: String = "Normal" // "Urgent", "High", "Normal"
)
