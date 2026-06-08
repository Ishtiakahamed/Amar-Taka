package com.example.ui.util

import com.example.data.repository.AppLanguage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

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
        if (lang == AppLanguage.BN) {
            return when (cat) {
                "Meal / Food", "Food", "খাবার/খাদ্য" -> "খাবার"
                "Groceries" -> "বাজার"
                "Shopping" -> "শপিং"
                "Clothes" -> "জামাকাপড়"
                "Transport" -> "যাতায়াত"
                "Mobile Recharge" -> "মোবাইল রিচার্জ"
                "Internet Bill" -> "ইন্টারনেট বিল"
                "Electricity Bill" -> "বিদ্যুৎ বিল"
                "House Rent" -> "বাসা ভাড়া"
                "Education" -> "পড়াশোনা"
                "Medical Treatment" -> "চিকিৎসা"
                "Medicine" -> "ওষুধ"
                "Family" -> "পরিবার"
                "Friends" -> "বন্ধু"
                "Entertainment" -> "বিনোদন"
                "Personal Care" -> "ব্যক্তিগত যত্ন"
                "Donation", "Charity" -> "দান"
                "Debt Repayment", "Loan Pain" -> "ঋণ পরিশোধ"
                "Others" -> "অন্যান্য"
                
                // Income
                "Salary" -> "বেতন"
                "Business" -> "ব্যবসা"
                "Freelancing" -> "ফ্রিল্যান্সিং"
                "Gift" -> "উপহার"
                else -> cat
            }
        } else {
            return when (cat) {
                "খাবার", "খাবার/খাদ্য" -> "Meal / Food"
                "বাজার" -> "Groceries"
                "শপিং" -> "Shopping"
                "জামাকাপড়" -> "Clothes"
                "যাতায়াত" -> "Transport"
                "মোবাইল রিচার্জ" -> "Mobile Recharge"
                "ইন্টারনেট বিল" -> "Internet Bill"
                "বিদ্যুৎ বিল" -> "Electricity Bill"
                "বাসা ভাড়া" -> "House Rent"
                "পড়াশোনা" -> "Education"
                "চিকিৎসা" -> "Medical Treatment"
                "ওষুধ" -> "Medicine"
                "পরিবার" -> "Family"
                "বন্ধু" -> "Friends"
                "বিনোদন" -> "Entertainment"
                "ব্যক্তিগত যত্ন" -> "Personal Care"
                "দান" -> "Donation"
                "ঋণ পরিশোধ" -> "Debt Repayment"
                "অন্যান্য" -> "Others"
                
                // Income
                "বেতন" -> "Salary"
                "ব্যবসা" -> "Business"
                "ফ্রিল্যান্সিং" -> "Freelancing"
                "উপহার" -> "Gift"
                else -> cat
            }
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
            "সঞ্চয়" -> "Savings"
            else -> wallet
        }
    }

    fun getCategoryIcon(cat: String): androidx.compose.ui.graphics.vector.ImageVector {
        val cleanCat = when (cat) {
            "Meal / Food", "Food", "খাবার", "খাবার/খাদ্য" -> "food"
            "Groceries", "বাজার" -> "groceries"
            "Shopping", "শপিং" -> "shopping"
            "Clothes", "জামাকাপড়", "পোশাক", "পোশাক/কাপড়" -> "clothes"
            "Transport", "যাতায়াত", "পরিবহন" -> "transport"
            "Mobile Recharge", "মোবাইল রিচার্জ" -> "mobile"
            "Internet Bill", "ইন্টারনেট বিল" -> "internet"
            "Electricity Bill", "বিদ্যুৎ বিল" -> "electricity"
            "House Rent", "বাসা ভাড়া", "ভাড়া" -> "rent"
            "Education", "পড়াশোনা", "শিক্ষা" -> "education"
            "Medical Treatment", "চিকিৎসা" -> "medical"
            "Medicine", "ওষুধ" -> "medicine"
            "Family", "পরিবার" -> "family"
            "Friends", "বন্ধু" -> "friends"
            "Entertainment", "বিনোদন" -> "entertainment"
            "Personal Care", "ব্যক্তিগত যত্ন" -> "personal"
            "Donation", "дан", "দান" -> "donation"
            "Debt Repayment", "Loan Paid", "ঋণ পরিশোধ", "ঋণ" -> "loan"
            "Salary", "বেতন" -> "salary"
            "Business", "ব্যবসা" -> "business"
            "Freelancing", "ফ্রিল্যান্সিং" -> "freelancing"
            "Gift", "উপহার" -> "gift"
            "Savings", "সঞ্চয়" -> "savings"
            else -> "others"
        }
        return when (cleanCat) {
            "food" -> Icons.Default.Star
            "groceries" -> Icons.Default.ShoppingCart
            "shopping" -> Icons.Default.ShoppingCart
            "clothes" -> Icons.Default.Person
            "transport" -> Icons.Default.Refresh
            "mobile" -> Icons.Default.Call
            "internet" -> Icons.Default.Info
            "electricity" -> Icons.Default.Warning
            "rent" -> Icons.Default.Home
            "education" -> Icons.Default.List
            "medical" -> Icons.Default.Warning
            "medicine" -> Icons.Default.CheckCircle
            "family" -> Icons.Default.Person
            "friends" -> Icons.Default.Share
            "entertainment" -> Icons.Default.PlayArrow
            "personal" -> Icons.Default.Person
            "donation" -> Icons.Default.Share
            "loan" -> Icons.Default.AccountBox
            "salary" -> Icons.Default.ThumbUp
            "business" -> Icons.Default.Home
            "freelancing" -> Icons.Default.AccountBox
            "gift" -> Icons.Default.Share
            "savings" -> Icons.Default.Star
            else -> Icons.Default.Menu
        }
    }
}
