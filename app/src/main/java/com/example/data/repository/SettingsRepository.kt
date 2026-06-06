package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences

enum class AppLanguage {
    BN, EN
}

enum class ThemeMode {
    DAY, NIGHT, EYE_CARE, GLASSMORPHISM
}

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    fun getAppLanguage(): AppLanguage {
        val name = prefs.getString("app_language", AppLanguage.BN.name) ?: AppLanguage.BN.name
        return try {
            AppLanguage.valueOf(name)
        } catch (e: Exception) {
            AppLanguage.BN
        }
    }

    fun setAppLanguage(lang: AppLanguage) {
        prefs.edit().putString("app_language", lang.name).apply()
    }

    fun getThemeMode(): ThemeMode {
        val name = prefs.getString("theme_mode", ThemeMode.DAY.name) ?: ThemeMode.DAY.name
        return try {
            ThemeMode.valueOf(name)
        } catch (e: Exception) {
            ThemeMode.DAY
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    fun getBudgetLimit(): Double {
        return prefs.getFloat("budget_limit", 0f).toDouble()
    }

    fun setBudgetLimit(limit: Double) {
        prefs.edit().putFloat("budget_limit", limit.toFloat()).apply()
    }

    // --- Challenges & Streaks Persistent State ---
    fun joinChallenge(id: String, joinedAt: Long) {
        prefs.edit().putLong("challenge_join_$id", joinedAt).apply()
    }

    fun getChallengeJoinDate(id: String): Long {
        return prefs.getLong("challenge_join_$id", 0L)
    }

    fun setChallengeCompleted(id: String, completed: Boolean) {
        prefs.edit().putBoolean("challenge_done_$id", completed).apply()
    }

    fun isChallengeCompleted(id: String): Boolean {
        return prefs.getBoolean("challenge_done_$id", false)
    }

    fun getChallengeProgress(id: String): Int {
        return prefs.getInt("challenge_progress_$id", 0)
    }

    fun setChallengeProgress(id: String, progress: Int) {
        prefs.edit().putInt("challenge_progress_$id", progress).apply()
    }
}
