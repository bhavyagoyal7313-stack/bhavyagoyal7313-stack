package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "doubts")
data class DoubtEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentClass: String,
    val board: String,
    val subject: String,
    val question: String,
    val isHinglish: Boolean,
    val conceptExplanation: String,
    val stepByStepAnswer: String,
    val finalAnswer: String,
    val examTips: String,
    val commonMistakes: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isBookmarked: Boolean = false
)
