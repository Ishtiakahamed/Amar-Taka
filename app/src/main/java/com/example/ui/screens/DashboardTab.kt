package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.data.repository.AppLanguage
import com.example.data.repository.ThemeMode
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.util.Localizer
import com.example.ui.util.StreakCalculator
import com.example.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*

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

@Composable
fun DashboardTab(viewModel: FinanceViewModel) {
    val context = LocalContext.current
    val appLanguage by viewModel.appLanguage.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val budgetLimit by viewModel.budgetLimit.collectAsState()

    val profileName by viewModel.profileName.collectAsState()
    val profileImageUri by viewModel.profileImageUri.collectAsState()
    val savingsGoals by viewModel.savingsGoals.collectAsState()

    val isGlass = themeMode == ThemeMode.GLASSMORPHISM
    val isBn = appLanguage == AppLanguage.BN
    val cardBg = if (isGlass) Color(0x3B1E293B) else MaterialTheme.colorScheme.surface

    var selectedFilter by remember { mutableStateOf("THIS_MONTH") } // TODAY, 7_DAYS, THIS_MONTH, THIS_YEAR

    val breakdownList = remember(transactions) {
        val filteredExpenses = transactions.filter { it.type == "EXPENSE" }
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

    val now = System.currentTimeMillis()

    // Bounding Ranges Timestamps
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

    // Wallets Map Calculation (Offline local aggregation)
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

    // Calculations of requested Summary Metrics fields
    val totalBalance = remember(walletBalances) { walletBalances.values.sum() }

    val todayIncome = remember(transactions, startOfToday) {
        transactions.filter { it.type == "INCOME" && it.date >= startOfToday }.sumOf { it.amount }
    }
    val todayExpense = remember(transactions, startOfToday) {
        transactions.filter { it.type == "EXPENSE" && it.date >= startOfToday }.sumOf { it.amount }
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
    val profitLoss = thisMonthIncome - thisMonthExpense

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

    // Active Saving Goal progress calculation
    val activeGoalProgress = remember(savingsGoals) {
        val firstGoal = savingsGoals.firstOrNull()
        if (firstGoal != null) {
            (firstGoal.currentAmount * 100 / firstGoal.targetAmount).toInt().coerceIn(0, 100)
        } else {
            0
        }
    }
    val activeGoalName = remember(savingsGoals) {
        savingsGoals.firstOrNull()?.title ?: ""
    }

    // Dynamic Stories (আজকের গল্প): 2-3 dynamic insights based on true ledger data
    val dashboardStories = remember(transactions, startOfToday, thisMonthExpense, budgetLimit, appLanguage) {
        val list = mutableListOf<String>()
        val todayExpenses = transactions.filter { it.type == "EXPENSE" && it.date >= startOfToday }
        val isBnLocal = appLanguage == AppLanguage.BN

        if (todayExpenses.isNotEmpty()) {
            val totalCatMap = todayExpenses.groupBy { it.category }.mapValues { it.value.sumOf { it.amount } }
            val topFood = totalCatMap["খাবার"] ?: 0.0
            if (topFood > 0.0) {
                list.add(
                    if (isBnLocal) "আজ Food খাতে ৳${formatBDTWithLanguage(topFood, appLanguage)} খরচ হয়েছে।"
                    else "Spent ৳${formatBDTWithLanguage(topFood, appLanguage)} on Food today."
                )
            } else {
                val maxCat = totalCatMap.maxByOrNull { it.value }
                if (maxCat != null) {
                    val translatedCat = Localizer.translateCategory(maxCat.key, appLanguage)
                    val formattedAmt = formatBDTWithLanguage(maxCat.value, appLanguage)
                    list.add(
                        if (isBnLocal) "আজ $translatedCat খাতে ৳$formattedAmt খরচ হয়েছে।"
                        else "Spent ৳$formattedAmt on $translatedCat today."
                    )
                }
            }
        } else {
            list.add(
                if (isBnLocal) "আজ কোনো বাড়তি খরচ হয়নি, আপনার ওয়ালেট হ্যাপী আছে! ✅"
                else "No card expenses logged today. Your wallet is happy! ✅"
            )
        }

        if (budgetLimit > 0.0) {
            if (thisMonthExpense > budgetLimit) {
                list.add(
                    if (isBnLocal) "এই মাসের বাজেট সীমা অতিক্রম হয়েছে ⚠️"
                    else "Monthly budget limit tier exceeded! ⚠️"
                )
            } else {
                list.add(
                    if (isBnLocal) "আজকের বাজেট safe আছে ✅"
                    else "Your active budget is safe & within limits! ✅"
                )
            }
        }

        // Compare with yesterday's expense
        val yesterdayExpenseVal = transactions.filter { it.type == "EXPENSE" && it.date in startOfYesterday..endOfYesterday }.sumOf { it.amount }
        val diff = yesterdayExpenseVal - todayExpense
        if (diff > 0 && todayExpense > 0) {
            list.add(
                if (isBnLocal) "গতকালের চেয়ে আজ ৳${formatBDTWithLanguage(diff, appLanguage)} কম খরচ হয়েছে। 🎉"
                else "Spent ৳${formatBDTWithLanguage(diff, appLanguage)} less than yesterday! 🎉"
            )
        } else if (diff < 0) {
            list.add(
                if (isBnLocal) "গতকালের চেয়ে আজ ৳${formatBDTWithLanguage(-diff, appLanguage)} বেশি খরচ হয়েছে। ⚠️"
                else "Spent ৳${formatBDTWithLanguage(-diff, appLanguage)} more than yesterday. ⚠️"
            )
        }

        if (thisMonthSavings > 0) {
            list.add(
                if (isBnLocal) "আজ আপনি ৳${formatBDTWithLanguage(thisMonthSavings, appLanguage)} সঞ্চয় করেছেন 🔥"
                else "You secured ৳${formatBDTWithLanguage(thisMonthSavings, appLanguage)} in savings today! 🔥"
            )
        }
        list.take(3)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. GREETING HEADER ---
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (profileName.isNotEmpty()) {
                            if (isBn) "হ্যালো, $profileName!" else "Hello, $profileName!"
                        } else {
                            if (isBn) "হ্যালো, শুভ দিন!" else "Hello, welcome!"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = SimpleDateFormat("EEEE, d MMMM yyyy", if (isBn) Locale("bn", "BD") else Locale.US).format(Date()),
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                if (profileImageUri.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(profileImageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = if (isBn) "প্রোফাইল ছবি" else "Profile Picture",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💰", fontSize = 16.sp)
                    }
                }
            }
        }

        // --- 2. TOTAL CURRENT BALANCE HERO CARD ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(if (isGlass) 1.dp else 0.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = if (isGlass) Color(0x7A0F172A) else MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isBn) "মোট ব্যালেন্স" else "Total Balance",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isGlass) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "৳ ${formatBDTWithLanguage(totalBalance, appLanguage)}",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isGlass) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.12f)))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick mini ledger values inside Hero card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = if (isBn) "আজকের আয়" else "Today's Income", fontSize = 9.sp, color = Color.Gray)
                            Text(text = "৳${formatBDTWithLanguage(todayIncome, appLanguage)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = IncomeGreen)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = if (isBn) "আজকের খরচ" else "Today's Expense", fontSize = 9.sp, color = Color.Gray)
                            Text(text = "৳${formatBDTWithLanguage(todayExpense, appLanguage)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ExpenseRed)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = if (isBn) "লাভ / ক্ষতি" else "Net gain/loss", fontSize = 9.sp, color = Color.Gray)
                            Text(
                                text = "৳${formatBDTWithLanguage(profitLoss, appLanguage)}", 
                                fontSize = 12.sp, 
                                fontWeight = FontWeight.Bold, 
                                color = if (profitLoss >= 0.0) IncomeGreen else ExpenseRed
                            )
                        }
                    }
                }
            }
        }

        // --- 3. ACCOUNTS & WALLET BALANCES SUMMARY GRID ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (isBn) "ওয়ালেট ও একাউন্ট হিসাব" else "My Wallets & Accounts",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WalletCard(
                        title = if (isBn) "ক্যাশ" else "Cash",
                        amount = walletBalances["ক্যাশ"] ?: 0.0,
                        emoji = "💵",
                        modifier = Modifier.weight(1f),
                        appLanguage = appLanguage,
                        cardBg = cardBg
                    )
                    WalletCard(
                        title = if (isBn) "বিকাশ" else "bKash",
                        amount = walletBalances["বিকাশ"] ?: 0.0,
                        emoji = "📱",
                        modifier = Modifier.weight(1f),
                        appLanguage = appLanguage,
                        cardBg = cardBg
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WalletCard(
                        title = if (isBn) "নগদ" else "Nagad",
                        amount = walletBalances["নগদ"] ?: 0.0,
                        emoji = "🍊",
                        modifier = Modifier.weight(1f),
                        appLanguage = appLanguage,
                        cardBg = cardBg
                    )
                    WalletCard(
                        title = if (isBn) "রকেট" else "Rocket",
                        amount = walletBalances["রকেট"] ?: 0.0,
                        emoji = "🚀",
                        modifier = Modifier.weight(1f),
                        appLanguage = appLanguage,
                        cardBg = cardBg
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WalletCard(
                        title = if (isBn) "ব্যাংক" else "Bank",
                        amount = walletBalances["ব্যাংক"] ?: 0.0,
                        emoji = "🏦",
                        modifier = Modifier.weight(1f),
                        appLanguage = appLanguage,
                        cardBg = cardBg
                    )
                    WalletCard(
                        title = if (isBn) "সঞ্চয় ব্যালেন্স" else "Savings Bal",
                        amount = walletBalances["সঞ্চয়"] ?: 0.0,
                        emoji = "🐖",
                        modifier = Modifier.weight(1f),
                        appLanguage = appLanguage,
                        cardBg = cardBg
                    )
                }
            }
        }

        // --- 4. DYNAMIC STORIES (আজকের গল্প) ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(if (isGlass) 1.dp else 0.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📖", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "আজকের গল্প (Insights)" else "Today's Story Insights",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    dashboardStories.forEach { story ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = story,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // --- 5. KEY FINANCIAL METRICS GRID CARD ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(if (isGlass) 1.dp else 0.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isBn) "ড্যাশবোর্ড সারসংক্ষেপ" else "Ledger Summary Grid",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(modifier = Modifier.fillMaxWidth()) {
                        MetricCell(
                            label = if (isBn) "৭ দিনের খরচ" else "7d Expense",
                            value = "৳${formatBDTWithLanguage(sevenDaysExpense, appLanguage)}",
                            modifier = Modifier.weight(1f)
                        )
                        Box(modifier = Modifier.width(1.dp).height(36.dp).background(Color.Gray.copy(alpha = 0.15f)))
                        MetricCell(
                            label = if (isBn) "এই মাসের আয়" else "This Month Inc",
                            value = "৳${formatBDTWithLanguage(thisMonthIncome, appLanguage)}",
                            modifier = Modifier.weight(1f),
                            valueColor = IncomeGreen
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        MetricCell(
                            label = if (isBn) "এই মাসের খরচ" else "This Month Exp",
                            value = "৳${formatBDTWithLanguage(thisMonthExpense, appLanguage)}",
                            modifier = Modifier.weight(1f),
                            valueColor = ExpenseRed
                        )
                        Box(modifier = Modifier.width(1.dp).height(36.dp).background(Color.Gray.copy(alpha = 0.15f)))
                        MetricCell(
                            label = if (isBn) "এই মাসের সঞ্চয়" else "This Month Sav",
                            value = "৳${formatBDTWithLanguage(thisMonthSavings, appLanguage)}",
                            modifier = Modifier.weight(1f),
                            valueColor = Color(0xFF0EA5E9)
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        MetricCell(
                            label = if (isBn) "মানি হেলথ স্কোর" else "Money Health",
                            value = "$moneyHealthScore/১০০",
                            modifier = Modifier.weight(1f),
                            valueColor = if (moneyHealthScore >= 75) IncomeGreen else Color(0xFFF1C40F)
                        )
                        Box(modifier = Modifier.width(1.dp).height(36.dp).background(Color.Gray.copy(alpha = 0.15f)))
                        MetricCell(
                            label = if (isBn) "হিসাব স্ট্রেইক" else "Active Streak",
                            value = if (isBn) "$challengeStreak দিন" else "$challengeStreak Days",
                            modifier = Modifier.weight(1f),
                            valueColor = Color(0xFFEE5A24)
                        )
                    }

                    // Active saving goal card embedded
                    if (savingsGoals.isNotEmpty()) {
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = if (isBn) "সঞ্চয় লক্ষ্য প্রগতি ($activeGoalName)" else "Saving Goal Progress ($activeGoalName)",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                LinearProgressIndicator(
                                    progress = activeGoalProgress / 100f,
                                    modifier = Modifier
                                        .width(150.dp)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = IncomeGreen
                                )
                            }
                            Text(
                                text = "$activeGoalProgress%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = IncomeGreen
                            )
                        }
                    }
                }
            }
        }

        // --- 6. SPENDING CATEGORY BREAKDOWN ---

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isBn) "কোন খাতে কত খরচ" else "Sector Wise Spends Breakdown",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                if (breakdownList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = if (isBn) "খাতওয়ারি খরচের কোনো হিসাব পাওয়া যায়নি" else "No spending breakdown available.", color = Color.Gray, fontSize = 11.sp)
                    }
                } else {
                    breakdownList.forEach { (catName, sumPctPair) ->
                        val amount = sumPctPair.first
                        val pct = sumPctPair.second
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = Localizer.translateCategory(catName, appLanguage), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "৳${amount.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(text = "${pct.toInt()}%", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(15.dp)) }
    }
}

@Composable
fun WalletCard(
    title: String,
    amount: Double,
    emoji: String,
    modifier: Modifier = Modifier,
    appLanguage: AppLanguage,
    cardBg: Color
) {
    Card(
        modifier = modifier.height(64.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = title, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "৳${formatBDTWithLanguage(amount, appLanguage).substringBefore(".")}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(text = emoji, fontSize = 18.sp)
        }
    }
}

@Composable
fun MetricCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        modifier = modifier.padding(vertical = 4.dp, horizontal = 12.dp)
    ) {
        Text(text = label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}
