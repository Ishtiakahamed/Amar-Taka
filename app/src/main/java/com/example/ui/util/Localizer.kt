package com.example.ui.util

import com.example.data.repository.AppLanguage

object Localizer {
    private val englishMap = mapOf(
        "REPORT_HISTORY_TITLE" to "Transaction History",
        "REPORT_SUMMARY_HEADER" to "Monthly Summary",
        "TOTAL_INCOME" to "Total Income",
        "TOTAL_EXPENSE" to "Total Expense",
        "NET_BALANCE" to "Net Balance",
        "REPORT_SEARCH_PLACEHOLDER" to "Search note or category...",
        "EMPTY_TX" to "No transactions logged yet",
        "SAVINGS_GOAL_TITLE" to "Savings Goal: ",
        "SAVINGS_GOAL_DEFAULT" to "My Savings Target",
        "SAVINGS_GOAL_COLLECTED" to "Collected: ",
        "SAVINGS_GOAL_TARGET" to "Target: "
    )

    private val banglaMap = mapOf(
        "REPORT_HISTORY_TITLE" to "লেনদেনের ইতিহাস",
        "REPORT_SUMMARY_HEADER" to "মাসিক হিসাব বিবরণী",
        "TOTAL_INCOME" to "মোট আয়",
        "TOTAL_EXPENSE" to "মোট ব্যয়",
        "NET_BALANCE" to "অবশিষ্ট ব্যালেন্স",
        "REPORT_SEARCH_PLACEHOLDER" to "নোট অথবা খাতের নাম খুজুন...",
        "EMPTY_TX" to "কোন লেনদেন নেই",
        "SAVINGS_GOAL_TITLE" to "সঞ্চয়ের লক্ষ্য: ",
        "SAVINGS_GOAL_DEFAULT" to "আমার সঞ্চয় লক্ষ্য",
        "SAVINGS_GOAL_COLLECTED" to "সংগৃহীত: ",
        "SAVINGS_GOAL_TARGET" to "লক্ষ্য: "
    )

    fun t(key: String, lang: AppLanguage): String {
        return if (lang == AppLanguage.BN) {
            banglaMap[key] ?: key
        } else {
            englishMap[key] ?: key
        }
    }

    fun translateCategory(cat: String, lang: AppLanguage): String {
        if (lang == AppLanguage.BN) return cat
        // Map some common bn categories to en if any
        return when (cat) {
            "খাবার" -> "Food"
            "পরিবহন" -> "Transport"
            "উপহার" -> "Gift"
            "বেতন" -> "Salary"
            "বিনোদন" -> "Entertainment"
            "অন্যান্য" -> "Others"
            else -> cat
        }
    }

    fun translateWallet(wallet: String, lang: AppLanguage): String {
        if (lang == AppLanguage.BN) return wallet
        return when (wallet) {
            "ক্যাশ" -> "Cash"
            "বিকাশ" -> "bKash"
            "রকেট" -> "Rocket"
            "নগদ" -> "Nagad"
            "ব্যাংক" -> "Bank"
            else -> wallet
        }
    }
}
