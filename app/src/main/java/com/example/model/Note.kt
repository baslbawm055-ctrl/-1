package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val language: String,
    val isPinned: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val tags: String = "",
    val explanation: String = "",
    val referenceLink: String = "",
    val difficulty: String = "Beginner",
    val colorHex: String = "#1E293B",
    val attachments: String = ""
)
