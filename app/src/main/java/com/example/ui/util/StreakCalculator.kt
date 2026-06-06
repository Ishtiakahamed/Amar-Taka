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
                streak == 0 -> "Let's lock in your tracking habits! Add a transaction daily. 🚀"
                streak == 1 -> "Great start! Day 1 complete. Keep it up! ✨"
                streak in 2..3 -> "Awesome! You are tracking consistently. 🔋"
                streak in 4..6 -> "Fabulous! You're building solid financial discipline! 🔥"
                streak in 7..29 -> "7+ Days Streak! Exceptional commitment! 🏆"
                else -> "$streak Days Money Discipline Unlocked! Superb! 👑"
            }
        } else {
            when (streak) {
                0 -> "আজকের কোনো হিসাব যুক্ত করা হয়নি। চলুন অভ্যাসটি ধরে রাখি! 🚀"
                1 -> "শুরুটা সুন্দর হয়েছে! হিসাব রাখা শুরু করুন। ✨"
                3 -> "আপনি ভালোভাবে হিসাব রাখছেন! ৩ দিনের streak চলছে! 🔋"
                7 -> "৭ দিনের হিসাব streak! দারুণ! অসাধারণ প্রতিশ্রুতি! 🔥"
                30 -> "৩০ দিনের money discipline unlocked! অদম্য সংকল্প! 👑"
                else -> {
                    when {
                        streak > 30 -> "$streak দিনের money discipline unlocked! আপনি একজন ফাইনান্স বস! 👑"
                        streak >= 7 -> "$streak দিনের হিসাব streak চলছে! দারুণ গতি! 🔥"
                        streak >= 3 -> "$streak দিনের হিসাব বজায় রেখেছেন! ভালো হচ্ছে! 🔋"
                        else -> "$streak দিনের হিসাব streak চলছে। দুর্দান্ত! ✨"
                    }
                }
            }
        }
    }
}
