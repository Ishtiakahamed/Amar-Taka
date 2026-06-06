package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "loans")
data class Loan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personName: String,
    val amount: Double,
    val type: String, // GAVE or TOOK (Lent vs Borrowed)
    val note: String,
    val dueDate: Long,
    val isPaid: Boolean = false,
    val date: Long = System.currentTimeMillis()
)
