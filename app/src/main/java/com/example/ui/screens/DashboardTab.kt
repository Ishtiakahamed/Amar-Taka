package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.data.repository.AppLanguage
import com.example.data.repository.ThemeMode
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.GlassOnPrimary
import com.example.ui.theme.IncomeGreen
import com.example.ui.util.Localizer
import com.example.ui.util.StreakCalculator
import com.example.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*

// --- TRANSACTION DIGITS & HELPER TRANS ---
fun formatBDTWithLanguage(value: Double, lang: AppLanguage): String {
    val formatted = String.format(Locale.US, "%,.1f", value)
    if (lang == AppLanguage.BN) {
        val bnDigits = mapOf(
            '0' to '০', '1' to '১', '2' to '২', '3' to '৩', '4' to '৪',
            '5' to '৫', '6' to '৬', '7' to '৭', '8' to '৮', '9' to '৯',
            ',' to ',', '.' to '.'
        )
        return formatted.map { bnDigits[it] ?: it }.joinToString("")
    }
    return formatted
}

fun getGreeting(lang: AppLanguage): String {
    val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val isBn = lang == AppLanguage.BN
    return when {
        h in 5..11 -> if (isBn) "শুভ সকাল, স্বাগতম!" else "Good morning, welcome!"
        h in 12..15 -> if (isBn) "শুভ দুপুর, স্বাগতম!" else "Good afternoon, welcome!"
        h in 16..17 -> if (isBn) "শুভ বিকেল, স্বাগতম!" else "Good afternoon, welcome!"
        h in 18..21 -> if (isBn) "শুভ সন্ধ্যা, স্বাগতম!" else "Good evening, welcome!"
        else -> if (isBn) "শুভ রাত্রি, স্বাগতম!" else "Good night, welcome!"
    }
}

fun dbText(key: String, lang: AppLanguage): String {
    val isBn = lang == AppLanguage.BN
    return when (key) {
        "GREETING_WELCOME" -> if (isBn) "আসসালামু আলাইকুম" else "Welcome to Amar Taka"
        "MAIN_BALANCE" -> if (isBn) "মোট ব্যালেন্স" else "Total Balance"
        "TODAY_EXPENSE" -> if (isBn) "আজকের খরচ" else "Today's Expense"
        "TODAY_INCOME" -> if (isBn) "আজকের আয়" else "Today's Income"
        "SEVEN_DAYS_EXPENSE" -> if (isBn) "গত ৭ দিনের খরচ" else "Last 7 Days Expense"
        "MONTHLY_EXPENSE" -> if (isBn) "এই মাসের খরচ" else "This Month's Expense"
        "MONTHLY_INCOME" -> if (isBn) "এই মাসের আয়" else "This Month's Income"
        "MONTHLY_SAVINGS" -> if (isBn) "এই মাসের সঞ্চয়" else "This Month's Savings"
        "MONTHLY_PROFIT_LOSS" -> if (isBn) "লাভ / লস" else "Profit / Loss"
        "SAVINGS_PROGRESS" -> if (isBn) "সেভিংস প্রগ্রেস" else "Savings Progress"
        "STREAK_LABEL" -> if (isBn) "হিসাব স্ট্রিক" else "Streak Indicator"
        "HEALTH_SCORE" -> if (isBn) "মানি হেলথ স্কোর" else "Money Health Score"
        "DASHBOARD_STORY_TITLE" -> if (isBn) "আজকের গল্প" else "Dashboard Stories"
        "QUICK_ADD_TITLE" -> if (isBn) "দ্রুত হিসাব যোগ করুন" else "Quick Add"
        "CATEGORY_BREAKDOWN_TITLE" -> if (isBn) "কোন খাতে কত খরচ" else "Category Spending"
        "WALLET_BREAKDOWN_TITLE" -> if (isBn) "ওয়ালেট হিসাব" else "Wallet Balance & Usage"
        "RECENT_TIMELINE_TITLE" -> if (isBn) "সাম্প্রতিক লেনদেন" else "Recent Transactions"
        "FILTER_LABEL" -> if (isBn) "ফিল্টার করুন" else "Filter"
        "COMPARE_Y_SAFE" -> if (isBn) "গতকালের চেয়ে আজ খরচ কম হয়েছে ✅" else "Spent less than yesterday! ✅"
        "COMPARE_Y_WARN" -> if (isBn) "গতকালের চেয়ে আজ খরচ বেশি হয়েছে ⚠️" else "Spent more than yesterday! ⚠️"
        "COMPARE_W_SAFE" -> if (isBn) "গত সপ্তাহের চেয়ে এই সপ্তাহে খরচ কম হয়েছে 🔥" else "Spent less than last week! 🔥"
        "COMPARE_W_WARN" -> if (isBn) "গত সপ্তাহের চেয়ে এই সপ্তাহে খরচ বেশি হয়েছে ⚠️" else "Spent more than last week! ⚠️"
        "COMPARE_M_SAFE" -> if (isBn) "গত মাসের চেয়ে এই মাসে খরচ কমেছে 🔥" else "Spent less than last month! 🔥"
        "COMPARE_M_WARN" -> if (isBn) "গত মাসের চেয়ে এই মাসে খরচ বেড়েছে ⚠️" else "Spent more than last month! ⚠️"
        else -> key
    }
}

@Composable
fun DashboardTab(viewModel: FinanceViewModel) {
    val context = LocalContext.current
    val appLanguage by viewModel.appLanguage.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val budgetLimit by viewModel.budgetLimit.collectAsState()
    
    val isGlass = themeMode == ThemeMode.GLASSMORPHISM
    val isBn = appLanguage == AppLanguage.BN

    var selectedFilter by remember { mutableStateOf("THIS_MONTH") } // TODAY, 7_DAYS, THIS_MONTH, THIS_YEAR

    // Quick Entry fields
    var quickAmount by remember { mutableStateOf("") }
    var quickType by remember { mutableStateOf("EXPENSE") } // INCOME, EXPENSE, SAVINGS, LOAN, TRANSFER
    var quickCategory by remember { mutableStateOf("অন্যান্য") }
    var quickWallet by remember { mutableStateOf("ক্যাশ") }
    var quickNote by remember { mutableStateOf("") }
    var transferDestWallet by remember { mutableStateOf("বিকাশ") }
    var loanPersonName by remember { mutableStateOf("") }
    var loanType by remember { mutableStateOf("TOOK") } // GAVE, TOOK

    val now = System.currentTimeMillis()
    
    // --- Start times ---
    val startOfToday = remember(now) {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val startOfYesterday = remember(now) {
        Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val endOfYesterday = remember(now) {
        Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }
    val startOf7DaysAgo = remember(now) {
        Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -6)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val startOfPrev7DaysAgo = remember(now) {
        Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -13)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val endOfPrev7DaysAgo = remember(now) {
        Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -7)
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }
    val startOfThisMonth = remember(now) {
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val startOfLastMonth = remember(now) {
        Calendar.getInstance().apply {
            add(Calendar.MONTH, -1)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val endOfLastMonth = remember(now) {
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.DAY_OF_MONTH, -1)
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }
    val startOfThisYear = remember(now) {
        Calendar.getInstance().apply {
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    // Selected range bounding
    val (filterStart, filterEnd) = remember(selectedFilter, startOfToday, startOf7DaysAgo, startOfThisMonth, startOfThisYear, now) {
        when (selectedFilter) {
            "TODAY" -> Pair(startOfToday, now)
            "7_DAYS" -> Pair(startOf7DaysAgo, now)
            "THIS_MONTH" -> Pair(startOfThisMonth, now)
            "THIS_YEAR" -> Pair(startOfThisYear, now)
            else -> Pair(startOfThisMonth, now)
        }
    }

    // Wallets calculations
    val walletBalances = remember(transactions) {
        val balances = mutableMapOf(
            "ক্যাশ" to 0.0, "বিকাশ" to 0.0, "নগদ" to 0.0, "রকেট" to 0.0, "ব্যাংক" to 0.0, "সঞ্চয়" to 0.0
        )
        transactions.forEach { tx ->
            val amt = tx.amount
            when (tx.type) {
                "INCOME" -> balances[tx.wallet] = (balances[tx.wallet] ?: 0.0) + amt
                "EXPENSE" -> balances[tx.wallet] = (balances[tx.wallet] ?: 0.0) - amt
                "SAVINGS" -> {
                    balances[tx.wallet] = (balances[tx.wallet] ?: 0.0) - amt
                    balances["সঞ্চয়"] = (balances["সঞ্চয়"] ?: 0.0) + amt
                }
                "LOAN" -> {
                    if (tx.category == "লেন্ট" || tx.category == "GAVE" || tx.category == "Lent") {
                        balances[tx.wallet] = (balances[tx.wallet] ?: 0.0) - amt
                    } else {
                        balances[tx.wallet] = (balances[tx.wallet] ?: 0.0) + amt
                    }
                }
                "TRANSFER" -> {
                    balances[tx.wallet] = (balances[tx.wallet] ?: 0.0) - amt
                    if (balances.containsKey(tx.category)) {
                        balances[tx.category] = (balances[tx.category] ?: 0.0) + amt
                    }
                }
            }
        }
        balances
    }

    val totalBalance = remember(walletBalances) { walletBalances.values.sum() }

    // Cost computations
    val todayExpense = remember(transactions, startOfToday) {
        transactions.filter { it.type == "EXPENSE" && it.date >= startOfToday }.sumOf { it.amount }
    }
    val todayIncome = remember(transactions, startOfToday) {
        transactions.filter { it.type == "INCOME" && it.date >= startOfToday }.sumOf { it.amount }
    }
    val sevenDaysExpense = remember(transactions, startOf7DaysAgo) {
        transactions.filter { it.type == "EXPENSE" && it.date >= startOf7DaysAgo }.sumOf { it.amount }
    }
    val thisMonthIncome = remember(transactions, startOfThisMonth) {
        transactions.filter { it.type == "INCOME" && it.date >= startOfThisMonth }.sumOf { it.amount }
    }
    val thisMonthExpense = remember(transactions, startOfThisMonth) {
        transactions.filter { it.type == "EXPENSE" && it.date >= startOfThisMonth }.sumOf { it.amount }
    }
    val thisMonthSavings = remember(transactions, startOfThisMonth) {
        transactions.filter { it.type == "SAVINGS" && it.date >= startOfThisMonth }.sumOf { it.amount }
    }
    val monthlyProfitLoss = remember(thisMonthIncome, thisMonthExpense) { thisMonthIncome - thisMonthExpense }

    // Streak & Health
    val challengeStreak = remember(transactions) { StreakCalculator.calculateStreak(transactions) }
    val moneyHealthScore = remember(thisMonthIncome, thisMonthExpense, budgetLimit, challengeStreak) {
        var score = 70.0
        if (thisMonthIncome > 0) {
            val savingsRate = ((thisMonthIncome - thisMonthExpense) / thisMonthIncome) * 100.0
            if (savingsRate >= 30.0) score += 15.0
            else if (savingsRate in 10.0..29.0) score += 5.0
            else if (savingsRate < 0.0) score -= 15.0
        }
        if (budgetLimit > 0.0) {
            if (thisMonthExpense > budgetLimit) score -= 20.0
            else score += 10.0
        }
        if (challengeStreak >= 3) score += 5.0
        score.coerceIn(0.0, 100.0).toInt()
    }

    // Story insights list
    val dashboardStories = remember(transactions, startOfToday, thisMonthExpense, budgetLimit, appLanguage) {
        val list = mutableListOf<String>()
        val todayExpenses = transactions.filter { it.type == "EXPENSE" && it.date >= startOfToday }
        val isBnLocal = appLanguage == AppLanguage.BN

        if (todayExpenses.isNotEmpty()) {
            val lastTx = todayExpenses.last()
            val catTransl = Localizer.translateCategory(lastTx.category, appLanguage)
            val amtTransl = formatBDTWithLanguage(lastTx.amount, appLanguage)
            list.add(
                if (isBnLocal) "আজ আপনি $catTransl খাতে ৳$amtTransl খরচ করেছেন।"
                else "Today you spent ৳$amtTransl on $catTransl."
            )
            
            val maxCat = todayExpenses.groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
                .maxByOrNull { it.value }
            if (maxCat != null) {
                val catName = Localizer.translateCategory(maxCat.key, appLanguage)
                list.add(
                    if (isBnLocal) "আজকের সবচেয়ে বেশি খরচ হয়েছে $catName খাতে।"
                    else "Most of your spending today was in $catName."
                )
            }
            
            val maxWallet = todayExpenses.groupBy { it.wallet }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
                .maxByOrNull { it.value }
            if (maxWallet != null) {
                val wName = Localizer.translateWallet(maxWallet.key, appLanguage)
                list.add(
                    if (isBnLocal) "আজ $wName ওয়ালেট থেকে সবচেয়ে বেশি টাকা খরচ হয়েছে।"
                    else "Most money was spent from $wName today."
                )
            }
        }

        val todaySavings = transactions.filter { it.type == "SAVINGS" && it.date >= startOfToday }.sumOf { it.amount }
        if (todaySavings > 0) {
            list.add(
                if (isBnLocal) "আজ আপনি ৳${formatBDTWithLanguage(todaySavings, appLanguage)} সঞ্চয় করেছেন, ভালো কাজ 🔥"
                else "You saved ৳${formatBDTWithLanguage(todaySavings, appLanguage)} today, great job! 🔥"
            )
        }

        if (budgetLimit > 0.0) {
            if (thisMonthExpense > budgetLimit) {
                list.add(
                    if (isBnLocal) "আজকের বাজেট লিমিট অতিক্রম হয়ে গেছে, সাবধানে চলুন ⚠️"
                    else "Monthly budget limit crossed! Be careful! ⚠️"
                )
            } else {
                list.add(
                    if (isBnLocal) "আজকের বাজেট এখনো safe আছে ✅"
                    else "Your monthly budget target is completely safe! ✅"
                )
            }
        }

        if (todayExpenses.isEmpty()) {
            list.add(
                if (isBnLocal) "আজ কোনো অপ্রয়োজনীয় খরচ হয়নি, দারুণ discipline ✅"
                else "No extra spend logged today, absolute discipline! ✅"
            )
        }
        list
    }

    // Custom Category breakdown calculations for selected filtered period
    val categorybreakdown = remember(transactions, filterStart, filterEnd) {
        val filteredExpenses = transactions.filter { it.date in filterStart..filterEnd && it.type == "EXPENSE" }
        val sumTotal = filteredExpenses.sumOf { it.amount }
        filteredExpenses.groupBy { it.category }
            .mapValues { entry ->
                val sum = entry.value.sumOf { it.amount }
                val pct = if (sumTotal > 0) (sum / sumTotal) * 100 else 0.0
                Pair(sum, pct)
            }
            .toList()
            .sortedByDescending { it.second.first }
    }

    // Comparison values
    val yesterdayExpenseVal = remember(transactions, startOfYesterday, endOfYesterday) {
        transactions.filter { it.type == "EXPENSE" && it.date in startOfYesterday..endOfYesterday }.sumOf { it.amount }
    }
    val lastWeekExpenseVal = remember(transactions, startOfPrev7DaysAgo, endOfPrev7DaysAgo) {
        transactions.filter { it.type == "EXPENSE" && it.date in startOfPrev7DaysAgo..endOfPrev7DaysAgo }.sumOf { it.amount }
    }
    val lastMonthExpenseVal = remember(transactions, startOfLastMonth, endOfLastMonth) {
        transactions.filter { it.type == "EXPENSE" && it.date in startOfLastMonth..endOfLastMonth }.sumOf { it.amount }
    }

    val cardBg = if (isGlass) Color(0x3B1E293B) else MaterialTheme.colorScheme.surface

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. GREETING HEADER ---
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Column {
                Text(
                    text = getGreeting(appLanguage),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = SimpleDateFormat("EEEE, d MMMM yyyy", if (isBn) Locale("bn", "BD") else Locale.US).format(Date()),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        // --- 2. PERIOD FILTER CHIPS ---
        item {
            Text(text = dbText("FILTER_LABEL", appLanguage), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val filters = listOf("TODAY", "7_DAYS", "THIS_MONTH", "THIS_YEAR")
                filters.forEach { filterKey ->
                    val isSelected = selectedFilter == filterKey
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { selectedFilter = filterKey }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = dbText(filterKey, appLanguage),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // --- 3. TOTAL CURRENT BALANCE HERO CARD ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(if (isGlass) 1.dp else 0.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = if (isGlass) Color(0x610F172A) else MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = dbText("MAIN_BALANCE", appLanguage),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isGlass) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "৳ ${formatBDTWithLanguage(totalBalance, appLanguage)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isGlass) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.15f))
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = dbText("TODAY_INCOME", appLanguage), fontSize = 10.sp, color = Color.Gray)
                            Text(
                                text = "৳ ${formatBDTWithLanguage(todayIncome, appLanguage)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = IncomeGreen
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = dbText("TODAY_EXPENSE", appLanguage), fontSize = 10.sp, color = Color.Gray)
                            Text(
                                text = "৳ ${formatBDTWithLanguage(todayExpense, appLanguage)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ExpenseRed
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = dbText("MONTHLY_PROFIT_LOSS", appLanguage), fontSize = 10.sp, color = Color.Gray)
                            Text(
                                text = "৳ ${formatBDTWithLanguage(monthlyProfitLoss, appLanguage)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (monthlyProfitLoss >= 0) IncomeGreen else ExpenseRed
                            )
                        }
                    }
                }
            }
        }

        // --- 4. HEALTH METER & STREAKS ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Streaks tracker
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(if (isGlass) 1.dp else 0.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(18.dp)),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(ExpenseRed.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = dbText("STREAK_LABEL", appLanguage), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isBn) "$challengeStreak দিন" else "$challengeStreak Days",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = ExpenseRed
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = StreakCalculator.getStreakMotivation(challengeStreak, !isBn),
                            fontSize = 10.sp,
                            color = Color.Gray,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Money health score card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(if (isGlass) 1.dp else 0.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(18.dp)),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(IncomeGreen.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Favorite, contentDescription = null, tint = IncomeGreen, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = dbText("HEALTH_SCORE", appLanguage), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = formatBDTWithLanguage(moneyHealthScore.toDouble(), appLanguage).substringBefore("."),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = if (moneyHealthScore >= 75) IncomeGreen else if (moneyHealthScore >= 45) Color(0xFFFFB300) else ExpenseRed
                            )
                            Text(
                                text = " / ১০০",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 3.dp, start = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when {
                                moneyHealthScore >= 75 -> if (isBn) "চমৎকার বাজেট নিয়ম মানছেন 🌟" else "Excellent budget discipline! 🌟"
                                moneyHealthScore >= 45 -> if (isBn) "বাজেট নিয়ন্ত্রণ মোটামুটি ভালো 🔋" else "Budget health is moderate 🔋"
                                else -> if (isBn) "খরচ কমিয়ে বাজেট বাড়ানো প্রয়োজন ⚠️" else "Need to scale down expenses ⚠️"
                            },
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // --- 5. DUAL COMPARATIVE COST WIDGETS ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(if (isGlass) 1.dp else 0.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isBn) "দৈনিক ও মাসিক তুলনামূলক খরচ" else "Comparative Spends Widget",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // Today vs Yesterday
                    val dayDiff = todayExpense - yesterdayExpenseVal
                    val dayBetter = dayDiff <= 0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = dbText("DAY_COST", appLanguage), fontSize = 11.sp, color = Color.Gray)
                            Text(text = "৳ ${formatBDTWithLanguage(todayExpense, appLanguage)}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (dayBetter) IncomeGreen.copy(alpha = 0.15f) else ExpenseRed.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (dayBetter) dbText("COMPARE_Y_SAFE", appLanguage) else dbText("COMPARE_Y_WARN", appLanguage),
                                fontSize = 10.sp,
                                color = if (dayBetter) IncomeGreen else ExpenseRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)))

                    // This Month vs Last Month
                    val monthDiff = thisMonthExpense - lastMonthExpenseVal
                    val monthBetter = monthDiff <= 0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = dbText("MONTH_COST", appLanguage), fontSize = 11.sp, color = Color.Gray)
                            Text(text = "৳ ${formatBDTWithLanguage(thisMonthExpense, appLanguage)}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (monthBetter) IncomeGreen.copy(alpha = 0.15f) else ExpenseRed.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (monthBetter) dbText("COMPARE_M_SAFE", appLanguage) else dbText("COMPARE_M_WARN", appLanguage),
                                fontSize = 10.sp,
                                color = if (monthBetter) IncomeGreen else ExpenseRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // --- 6. SOCIAL FINANCIAL STORIES SECTION ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = dbText("DASHBOARD_STORY_TITLE", appLanguage),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (dashboardStories.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp), contentAlignment = Alignment.Center
                    ) {
                        Text(text = if (isBn) "কোন বিশেষ গল্প বা অগ্রগতি নেই" else "No budget stories logged today.", color = Color.Gray, fontSize = 11.sp)
                    }
                } else {
                    dashboardStories.forEach { story ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondary)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = story,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 7. QUICK INLINE TRANSACTION ENTRY METHOD ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(if (isGlass) 1.dp else 0.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = dbText("QUICK_ADD_TITLE", appLanguage),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleSmall
                    )

                    // Type switches selector row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        val types = listOf("EXPENSE", "INCOME", "SAVINGS", "TRANSFER", "LOAN")
                        types.forEach { t ->
                            val isSel = quickType == t
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable {
                                        quickType = t
                                        quickCategory = when (t) {
                                            "EXPENSE" -> "অন্যান্য"
                                            "INCOME" -> "অন্যান্য"
                                            "SAVINGS" -> "ভবিষ্যৎ সঞ্চয়"
                                            "LOAN" -> "বন্ধুবান্ধব"
                                            else -> "বিকাশ"
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val label = when (t) {
                                    "EXPENSE" -> if (isBn) "ব্যয়" else "Exp"
                                    "INCOME" -> if (isBn) "আয়" else "Inc"
                                    "SAVINGS" -> if (isBn) "সঞ্চয়" else "Sav"
                                    "TRANSFER" -> if (isBn) "বদলি" else "Xfer"
                                    else -> if (isBn) "ঋণ" else "Loan"
                                }
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Inputs Row
                    OutlinedTextField(
                        value = quickAmount,
                        onValueChange = { quickAmount = it },
                        label = { Text(if (isBn) "টাকার পরিমাণ (৳)" else "Amount (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Conditional components
                    if (quickType == "TRANSFER") {
                        // Source wallet -> already there (quickWallet)
                        // Dest wallet dropdown representation
                        Text(text = if (isBn) "কোথায় পাঠাবেন?" else "Destination Wallet:", fontSize = 11.sp, color = Color.Gray)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val listW = listOf("ক্যাশ", "বিকাশ", "নগদ", "রকেট", "ব্যাংক")
                            listW.forEach { w ->
                                val isSel = transferDestWallet == w
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { transferDestWallet = w }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(Localizer.translateWallet(w, appLanguage), color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                }
                            }
                        }
                    } else if (quickType == "LOAN") {
                        OutlinedTextField(
                            value = loanPersonName,
                            onValueChange = { loanPersonName = it },
                            label = { Text(if (isBn) "ব্যক্তির নাম" else "Person Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (loanType == "GAVE") IncomeGreen else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { loanType = "GAVE" }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isBn) "টাকা পাবো (Lent)" else "Lent (GAVE)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (loanType == "GAVE") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (loanType == "TOOK") ExpenseRed else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { loanType = "TOOK" }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isBn) "টাকা দেবো (Borrowed)" else "Borrowed (TOOK)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (loanType == "TOOK") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Source Account Wallet selection chips
                    Text(text = if (isBn) "কোথা থেকে লেনদেন হয়েছে?" else "Wallet Account Source:", fontSize = 11.sp, color = Color.Gray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val listW = listOf("ক্যাশ", "বিকাশ", "নগদ", "রকেট", "ব্যাংক")
                        listW.forEach { w ->
                            val isSel = quickWallet == w
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { quickWallet = w }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = Localizer.translateWallet(w, appLanguage),
                                    fontSize = 10.sp,
                                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Note Description input
                    OutlinedTextField(
                        value = quickNote,
                        onValueChange = { quickNote = it },
                        label = { Text(if (isBn) "সংক্ষিপ্ত নোট (ঐচ্ছিক)" else "Brief note (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Categories options row
                    Text(text = if (isBn) "খাত নির্বাচন করুন:" else "Select Category Tag:", fontSize = 11.sp, color = Color.Gray)
                    val cats = when (quickType) {
                        "EXPENSE" -> listOf("خাবার", "ভাড়া", "বাজার/শপিং", "পরিবহন", "चिकित্সা", "বিনোদন", "বিল", "অন্যান্য")
                        "INCOME" -> listOf("বেতন", "ব্যবসা", "ফ্রিল্যান্সিং", "উপহার", "অন্যান্য")
                        "SAVINGS" -> listOf("ভবিষ্যৎ সঞ্চয়", "জরুরী ফান্ড", "ডিপোজিট", "অন্যান্য")
                        "LOAN" -> listOf("বন্ধুবান্ধব", "পরিবার", "ব্যাংক লোন", "অন্যান্য")
                        else -> listOf("ক্যাশ", "বিকাশ", "নগদ", "রকেট", "ব্যাংক")
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val subList = cats.take(4) // Display key categories
                        subList.forEach { c ->
                            val isSel = quickCategory == c
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { quickCategory = c }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (quickType == "TRANSFER") Localizer.translateWallet(c, appLanguage) else Localizer.translateCategory(c, appLanguage),
                                    fontSize = 9.sp,
                                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Save Save Button trigger
                    Button(
                        onClick = {
                            val value = quickAmount.toDoubleOrNull()
                            if (value == null || value <= 0.0) {
                                Toast.makeText(context, if (isBn) "সঠিক টাকার অংক লিখুন!" else "Please type a valid amount!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            
                            val resolvedCat = if (quickType == "TRANSFER") transferDestWallet else quickCategory
                            val resolvedNote = when (quickType) {
                                "TRANSFER" -> if (isBn) "টাকা পাঠানো হয়েছে $transferDestWallet এ" else "Transferred funds to $transferDestWallet"
                                "LOAN" -> (if (loanType == "GAVE") "ঋণ দেওয়া হয়েছে: " else "ঋণ নেওয়া হয়েছে: ") + loanPersonName + " " + quickNote
                                else -> quickNote
                            }
                            
                            // Save Transaction
                            viewModel.addTransaction(
                                amount = value,
                                type = quickType,
                                category = resolvedCat,
                                wallet = quickWallet,
                                note = resolvedNote,
                                date = System.currentTimeMillis()
                            )

                            // Save Savings Goals deposition if savings selected
                            if (quickType == "SAVINGS" && viewModel.savingsGoals.value.isNotEmpty()) {
                                viewModel.updateSavingsAmount(viewModel.savingsGoals.value.first(), value)
                            }
                            
                            // Save loan directly if selected
                            if (quickType == "LOAN") {
                                viewModel.addLoan(
                                    personName = loanPersonName,
                                    amount = value,
                                    type = loanType,
                                    note = resolvedNote,
                                    dueDate = System.currentTimeMillis() + 14 * 24 * 3600 * 1000L
                                )
                            }

                            quickAmount = ""
                            quickNote = ""
                            loanPersonName = ""
                            Toast.makeText(context, dbText("ADD_SUCCESS", appLanguage), Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = dbText("QUICK_ADD_TITLE", appLanguage), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- 8. DETAILED SECTOR SPENDING CATEGORIES ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = dbText("CATEGORY_BREAKDOWN_TITLE", appLanguage),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (categorybreakdown.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp), contentAlignment = Alignment.Center
                    ) {
                        Text(text = if (isBn) "বানিজ্যিক বা ব্যক্তিগত খরচ ডাটা নেই।" else "Zero expense records found.", color = Color.Gray, fontSize = 11.sp)
                    }
                } else {
                    categorybreakdown.forEach { (cat, info) ->
                        val (sum, pct) = info
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = Localizer.translateCategory(cat, appLanguage),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = "৳${formatBDTWithLanguage(sum, appLanguage)} (${formatBDTWithLanguage(pct, appLanguage)}%)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Gray
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = (pct / 100.0).toFloat().coerceIn(0f, 1f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
        }

        // --- 9. WALLETS DETAILED ANALYSIS CARDS ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = dbText("WALLET_BREAKDOWN_TITLE", appLanguage),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                val walletsGroup = listOf("ক্যাশ", "বিকাশ", "নগদ", "রকেট", "ব্যাংক", "সঞ্চয়")
                walletsGroup.forEach { w ->
                    val bal = walletBalances[w] ?: 0.0
                    val monthlySpnt = transactions.filter { it.date >= startOfThisMonth && it.type == "EXPENSE" && it.wallet == w }.sumOf { it.amount }
                    val monthlyInc = transactions.filter { it.date >= startOfThisMonth && it.type == "INCOME" && it.wallet == w }.sumOf { it.amount }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = Localizer.translateWallet(w, appLanguage),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isBn) "আয়: ৳${formatBDTWithLanguage(monthlyInc, appLanguage)} | ব্যয়: ৳${formatBDTWithLanguage(monthlySpnt, appLanguage)}"
                                    else "Inc: ৳${formatBDTWithLanguage(monthlyInc, appLanguage)} | Exp: ৳${formatBDTWithLanguage(monthlySpnt, appLanguage)}",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                            Text(
                                text = "৳${formatBDTWithLanguage(bal, appLanguage)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = if (bal >= 0) IncomeGreen else ExpenseRed
                            )
                        }
                    }
                }
            }
        }

        // --- 10. RECENT ACTIVITY STORY TIMELINE ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = dbText("RECENT_TIMELINE_TITLE", appLanguage),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                val latestTransactions = transactions.takeLast(5).reversed()
                if (latestTransactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp), contentAlignment = Alignment.Center
                    ) {
                        Text(text = if (isBn) "কোনো লেনদেনের তথ্য পাওয়া যায়নি" else "No recent activities found.", color = Color.Gray, fontSize = 11.sp)
                    }
                } else {
                    latestTransactions.forEachIndexed { idx, tx ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Timeline visual circle nodes
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(30.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(if (tx.type == "INCOME") IncomeGreen else ExpenseRed)
                                )
                                if (idx < latestTransactions.size - 1) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(30.dp)
                                            .background(Color.Gray.copy(alpha = 0.2f))
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = Localizer.translateCategory(tx.category, appLanguage) + " (${Localizer.translateWallet(tx.wallet, appLanguage)})",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (tx.note.isNotBlank()) {
                                        Text(text = tx.note, fontSize = 10.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                                Text(
                                    text = "${if (tx.type == "INCOME") "+" else "-"}৳${formatBDTWithLanguage(tx.amount, appLanguage)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (tx.type == "INCOME") IncomeGreen else ExpenseRed
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
