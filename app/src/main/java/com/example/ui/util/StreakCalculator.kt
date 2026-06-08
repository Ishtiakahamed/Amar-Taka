package com.example.ui.util

import com.example.data.model.Transaction
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object StreakCalculator {
    fun calculateStreak(transactions: List<Transaction>): Int {
        if (transactions.isEmpty()) return 0
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        
        val todayStr = dateFormat.format(Date())
        
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = dateFormat.format(cal.time)
        
        // Extract unique days with at least 1 transaction
        val txDays = transactions.map { dateFormat.format(Date(it.date)) }.toSet()
        
        // If there's no transaction both today and yesterday, the streak is broken (0)
        if (!txDays.contains(todayStr) && !txDays.contains(yesterdayStr)) {
            return 0
        }
        
        var streak = 0
        val checkCal = Calendar.getInstance()
        // If today has no transaction, the active streak count can still be measured backwards starting from yesterday
        if (!txDays.contains(todayStr)) {
            checkCal.add(Calendar.DAY_OF_YEAR, -1)
        }
        
        while (true) {
            val dayStr = dateFormat.format(checkCal.time)
            if (txDays.contains(dayStr)) {
                streak++
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }

    fun getStreakMotivation(streak: Int, isEnglish: Boolean): String {
        return if (isEnglish) {
            when {
                streak == 0 -> "Begin your tracking habits by adding a transaction daily."
                streak == 1 -> "Great start! Day 1 complete. Consistent tracking helps save money."
                streak in 2..3 -> "Awesome! You are tracking consistently."
                streak in 4..6 -> "Fabulous! You are building solid financial discipline."
                streak in 7..29 -> "7+ Days Streak! Exceptional commitment to your budget."
                else -> "$streak Days Money Discipline Unlocked! Superb."
            }
        } else {
            when (streak) {
                0 -> "আজকের কোনো হিসাব যুক্ত করা হয়নি। প্রতিদিন হিসাব রাখার অভ্যাসটি গড়ে তুলুন।"
                1 -> "প্রথম দিনের হিসাব সংরক্ষণ সম্পন্ন হয়েছে। নিয়মিত ট্র্যাক পরিচালনা করুন।"
                3 -> "আপনি ধারাবাহিকভাবে ৩ দিন যাবত হিসাব সংরক্ষণ করছেন।"
                7 -> "দারুণ! টানা ৭ দিন নিখুঁতভাবে আর্থিক হিসাব সংরক্ষণ করেছেন।"
                30 -> "টানা ৩০ দিন আর্থিক নিয়মানুবর্তিতা সফলভাবে বজায় রেখেছেন।"
                else -> {
                    when {
                        streak > 30 -> "টানা $streak দিন আর্থিক নিয়মানুবর্তিতা সফলভাবে বজায় রেখেছেন।"
                        streak >= 7 -> "টানা $streak দিন আর্থিক হিসাব সংরক্ষণ করছেন। দারুণ গতি।"
                        streak >= 3 -> "টানা $streak দিন আর্থিক হিসাব সফলভাবে বজায় রেখেছেন।"
                        else -> "টানা $streak দিন আর্থিক হিসাব সংরক্ষণ চলছে।"
                    }
                }
            }
        }
    }
}
