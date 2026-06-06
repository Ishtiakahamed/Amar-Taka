package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.data.repository.AppLanguage
import com.example.ui.theme.IncomeGreen
import com.example.ui.util.StreakCalculator
import com.example.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChallengesCard(viewModel: FinanceViewModel) {
    val context = LocalContext.current
    val appLanguage by viewModel.appLanguage.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val savingsGoals by viewModel.savingsGoals.collectAsState()
    val budgetLimit by viewModel.budgetLimit.collectAsState()

    val joinedMap by viewModel.joinedChallenges.collectAsState()
    val completedSet by viewModel.completedChallenges.collectAsState()
    val progressMap by viewModel.challengeProgress.collectAsState()

    var isExpanded by remember { mutableStateOf(false) }

    // Calculate Streak & Motivation
    val streakCount = remember(transactions) {
        StreakCalculator.calculateStreak(transactions)
    }
    val motivationText = remember(streakCount, appLanguage) {
        StreakCalculator.getStreakMotivation(streakCount, appLanguage == AppLanguage.EN)
    }

    // Badge Verification Logic
    val isFirstEntryUnlocked = transactions.isNotEmpty()
    val is3DayStreakUnlocked = streakCount >= 3
    val is7DayStreakUnlocked = streakCount >= 7
    val is30DayStreakUnlocked = streakCount >= 30

    val isBudgetSaverUnlocked = remember(transactions, budgetLimit) {
        if (budgetLimit <= 0.0) false else {
            val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val monthExpense = transactions.filter { tx ->
                if (tx.type != "EXPENSE") return@filter false
                val cal = Calendar.getInstance().apply { timeInMillis = tx.date }
                cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
            }.sumOf { it.amount }
            monthExpense <= budgetLimit
        }
    }

    val isNoExtraSpendUnlocked = remember(transactions) {
        if (transactions.isEmpty()) false else {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val last7Days = (0..6).map { i ->
                val c = Calendar.getInstance()
                c.add(Calendar.DAY_OF_YEAR, -i)
                dateFormat.format(c.time)
            }
            val spentDays = transactions.filter { it.type == "EXPENSE" }.map { dateFormat.format(Date(it.date)) }.toSet()
            last7Days.any { !spentDays.contains(it) }
        }
    }

    val isBestSavingMonthUnlocked = remember(transactions) {
        if (transactions.isEmpty()) false else {
            val grouped = transactions.groupBy {
                val cal = Calendar.getInstance().apply { timeInMillis = it.date }
                "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}"
            }
            grouped.any { (_, txs) ->
                val inc = txs.filter { it.type == "INCOME" }.sumOf { it.amount }
                val exp = txs.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                inc > exp
            }
        }
    }

    // Goal savings checking for dynamic challenges
    val totalGoalSavings = savingsGoals.sumOf { it.currentAmount }

    // Auto-update dynamic challenges progress
    LaunchedEffect(totalGoalSavings) {
        // Save 500 challenge
        if (joinedMap.containsKey("CHALLENGE_SAVE_500")) {
            val pct = (totalGoalSavings / 500.0 * 100).toInt().coerceIn(0, 100)
            viewModel.setChallengeProgress("CHALLENGE_SAVE_500", pct, 100)
        }
        // Save 1000 challenge
        if (joinedMap.containsKey("CHALLENGE_SAVE_1000")) {
            val pct = (totalGoalSavings / 1000.0 * 100).toInt().coerceIn(0, 100)
            viewModel.setChallengeProgress("CHALLENGE_SAVE_1000", pct, 100)
        }
    }

    // Auto-update saving days challenges progress based on streak
    LaunchedEffect(streakCount) {
        // 7-day challenge
        if (joinedMap.containsKey("CHALLENGE_7_DAY_SAVING")) {
            viewModel.setChallengeProgress("CHALLENGE_7_DAY_SAVING", streakCount, 7)
        }
        // 30-day challenge
        if (joinedMap.containsKey("CHALLENGE_30_DAY_SAVING")) {
            viewModel.setChallengeProgress("CHALLENGE_30_DAY_SAVING", streakCount, 30)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Streak Header Area
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF9800).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔥", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        val streakText = if (appLanguage == AppLanguage.BN) {
                            "আপনার $streakCount দিনের হিসাব streak চলছে 🔥"
                        } else {
                            "You are on a $streakCount-day tracking streak! 🔥"
                        }
                        Text(
                            text = streakText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = motivationText,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subtitle expander click area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (appLanguage == AppLanguage.BN) "সাফল্যের ব্যাজ ও মানি চ্যালেঞ্জসমূহ" else "Badges & Money Challenges",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    // --- Badges Section ---
                    Text(
                        text = if (appLanguage == AppLanguage.BN) "অর্জনকারী ব্যাজ খতিয়ান:" else "Achievements & Rewards:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Horizontal scrolling badges row
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            BadgeItem("🥇", if (appLanguage == AppLanguage.BN) "প্রথম হিসাব" else "First Entry", isFirstEntryUnlocked)
                            BadgeItem("⚡", if (appLanguage == AppLanguage.BN) "৩ দিন" else "3d Streak", is3DayStreakUnlocked)
                            BadgeItem("🔥", if (appLanguage == AppLanguage.BN) "৭ দিন" else "7d Streak", is7DayStreakUnlocked)
                            BadgeItem("👑", if (appLanguage == AppLanguage.BN) "৩০ দিন" else "30d Streak", is30DayStreakUnlocked)
                            BadgeItem("🛡️", if (appLanguage == AppLanguage.BN) "বাজেট হিরো" else "Budget Saver", isBudgetSaverUnlocked)
                            BadgeItem("🌱", if (appLanguage == AppLanguage.BN) "সাশ্রয়ী" else "Safe Day", isNoExtraSpendUnlocked)
                            BadgeItem("🏆", if (appLanguage == AppLanguage.BN) "সেরা সঞ্চয়" else "Best Saver", isBestSavingMonthUnlocked)
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), modifier = Modifier.padding(bottom = 14.dp))

                    // --- Challenges List ---
                    Text(
                        text = if (appLanguage == AppLanguage.BN) "মানি সেভিং চ্যালেঞ্জসমূহ:" else "Student-Friendly Challenges:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    ChallengeRow(
                        id = "CHALLENGE_7_DAY_SAVING",
                        title = if (appLanguage == AppLanguage.BN) "৭ দিনের সেভিং চ্যালেঞ্জ" else "7-Day Saving Tracker",
                        desc = if (appLanguage == AppLanguage.BN) "৭ দিন একটানা হিসাব রাখুন" else "Log transactions consecutively for 7 days",
                        progress = progressMap["CHALLENGE_7_DAY_SAVING"] ?: 0,
                        maxVal = 7,
                        isJoined = joinedMap.containsKey("CHALLENGE_7_DAY_SAVING"),
                        isCompleted = completedSet.contains("CHALLENGE_7_DAY_SAVING"),
                        onStart = { viewModel.startChallenge("CHALLENGE_7_DAY_SAVING") },
                        onAction = {
                            val msg = if (appLanguage == AppLanguage.BN) "হিসাব যোগ করলেই প্রগ্রেস বৃদ্ধি পাবে!" else "Add accounts daily to increment progress!"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    )

                    ChallengeRow(
                        id = "CHALLENGE_30_DAY_SAVING",
                        title = if (appLanguage == AppLanguage.BN) "৩০ দিনের সেভিং চ্যালেঞ্জ" else "30-Day Budget Saver",
                        desc = if (appLanguage == AppLanguage.BN) "৩০ দিন একটানা হিসাব রাখবার ব্রত" else "Log transactions consecutively for 30 days",
                        progress = progressMap["CHALLENGE_30_DAY_SAVING"] ?: 0,
                        maxVal = 30,
                        isJoined = joinedMap.containsKey("CHALLENGE_30_DAY_SAVING"),
                        isCompleted = completedSet.contains("CHALLENGE_30_DAY_SAVING"),
                        onStart = { viewModel.startChallenge("CHALLENGE_30_DAY_SAVING") },
                        onAction = {
                            val msg = if (appLanguage == AppLanguage.BN) "নিয়মিত হিসাব রাখুন অটো আপডেট হবে!" else "Update accounts regularly to get progress!"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    )

                    ChallengeRow(
                        id = "CHALLENGE_NO_FAST_FOOD",
                        title = if (appLanguage == AppLanguage.BN) "ফাস্টফুড বর্জন চ্যালেঞ্জ" else "No Fast Food Challenge",
                        desc = if (appLanguage == AppLanguage.BN) "৭ দিন বাইরের রিচ ফুড এড়ানো" else "Avoid restaurant & heavy snacks for 7 days",
                        progress = progressMap["CHALLENGE_NO_FAST_FOOD"] ?: 0,
                        maxVal = 7,
                        isJoined = joinedMap.containsKey("CHALLENGE_NO_FAST_FOOD"),
                        isCompleted = completedSet.contains("CHALLENGE_NO_FAST_FOOD"),
                        onStart = { viewModel.startChallenge("CHALLENGE_NO_FAST_FOOD") },
                        onAction = {
                            viewModel.incrementChallengeProgress("CHALLENGE_NO_FAST_FOOD", 7)
                            Toast.makeText(context, if (appLanguage == AppLanguage.BN) "আজকের দিনটি সফল! +১ দিন" else "Succeeded today! +1 Day", Toast.LENGTH_SHORT).show()
                        }
                    )

                    ChallengeRow(
                        id = "CHALLENGE_NO_RICKSHAW",
                        title = if (appLanguage == AppLanguage.BN) "রিকশা সাশ্রয় চ্যালেঞ্জ" else "No Extra Rickshaw Ride",
                        desc = if (appLanguage == AppLanguage.BN) "অল্প দূরত্ব হেঁটে ৭ দিন চলা" else "Walk short distances for 7 days",
                        progress = progressMap["CHALLENGE_NO_RICKSHAW"] ?: 0,
                        maxVal = 7,
                        isJoined = joinedMap.containsKey("CHALLENGE_NO_RICKSHAW"),
                        isCompleted = completedSet.contains("CHALLENGE_NO_RICKSHAW"),
                        onStart = { viewModel.startChallenge("CHALLENGE_NO_RICKSHAW") },
                        onAction = {
                            viewModel.incrementChallengeProgress("CHALLENGE_NO_RICKSHAW", 7)
                            Toast.makeText(context, if (appLanguage == AppLanguage.BN) "আজ সাশ্রয় করেছেন! +১ দিন" else "Saved today! +1 Day", Toast.LENGTH_SHORT).show()
                        }
                    )

                    ChallengeRow(
                        id = "CHALLENGE_SAVE_500",
                        title = if (appLanguage == AppLanguage.BN) "৳৫০০ সঞ্চয় চ্যালেঞ্জ" else "Save ৳500 Challenge",
                        desc = if (appLanguage == AppLanguage.BN) "জমা লক্ষ্যসমূহে মোট ৫০০৳ জমানো" else "Reach ৳500 total in savings targets",
                        progress = (totalGoalSavings * 100 / 500.0).toInt().coerceIn(0, 100),
                        maxVal = 10000,
                        isJoined = joinedMap.containsKey("CHALLENGE_SAVE_500"),
                        isCompleted = completedSet.contains("CHALLENGE_SAVE_500") || totalGoalSavings >= 500.0,
                        onStart = { viewModel.startChallenge("CHALLENGE_SAVE_500") },
                        onAction = {
                            val msg = if (appLanguage == AppLanguage.BN) "সঞ্চয় লক্ষ্যসমূহে টাকা যোগ করুন!" else "Add deposits to goals for automatic progress!"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        isPercentage = true
                    )

                    ChallengeRow(
                        id = "CHALLENGE_SAVE_1000",
                        title = if (appLanguage == AppLanguage.BN) "৳১০০০ সঞ্চয় চ্যালেঞ্জ" else "Save ৳1000 Challenge",
                        desc = if (appLanguage == AppLanguage.BN) "জма লক্ষ্যসমূহে মোট ১০০০৳ জমানো" else "Reach ৳1000 total in savings targets",
                        progress = (totalGoalSavings * 100 / 1000.0).toInt().coerceIn(0, 100),
                        maxVal = 10000,
                        isJoined = joinedMap.containsKey("CHALLENGE_SAVE_1000"),
                        isCompleted = completedSet.contains("CHALLENGE_SAVE_1000") || totalGoalSavings >= 1000.0,
                        onStart = { viewModel.startChallenge("CHALLENGE_SAVE_1000") },
                        onAction = {
                            val msg = if (appLanguage == AppLanguage.BN) "সঞ্চয় করুন আর প্রগ্রেস দেখুন" else "Deposit savings to automate progress!"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        isPercentage = true
                    )
                }
            }
        }
    }
}

@Composable
fun BadgeItem(emoji: String, label: String, isUnlocked: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.width(54.dp)
    ) {
        val bgBrush = if (isUnlocked) {
            Brush.radialGradient(listOf(Color(0xFFFFD700).copy(alpha = 0.3f), Color.Transparent))
        } else {
            Brush.radialGradient(listOf(Color.Gray.copy(alpha = 0.1f), Color.Transparent))
        }

        Box(
            modifier = Modifier
                .size(42.dp)
                .background(bgBrush)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoji,
                fontSize = if (isUnlocked) 24.sp else 18.sp
            )
        }
        Text(
            text = label,
            fontSize = 9.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else Color.LightGray,
            maxLines = 1
        )
    }
}

@Composable
fun ChallengeRow(
    id: String,
    title: String,
    desc: String,
    progress: Int,
    maxVal: Int,
    isJoined: Boolean,
    isCompleted: Boolean,
    onStart: () -> Unit,
    onAction: () -> Unit,
    isPercentage: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(text = desc, fontSize = 10.sp, color = Color.Gray)

                if (isJoined && !isCompleted) {
                    Spacer(modifier = Modifier.height(6.dp))
                    val scoreText = if (isPercentage) "$progress%" else "$progress / $maxVal"
                    Text(
                        text = "প্রগতি: $scoreText",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val indicatorProgress = if (isPercentage) {
                        (progress / 100f).coerceIn(0f, 1f)
                    } else {
                        (progress.toFloat() / maxVal).coerceIn(0f, 1f)
                    }
                    LinearProgressIndicator(
                        progress = indicatorProgress,
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = IncomeGreen,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            if (!isJoined) {
                Button(
                    onClick = onStart,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("শুরু করুন", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            } else if (isCompleted) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = IncomeGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("সম্পন্ন ✅", fontSize = 10.sp, color = IncomeGreen, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onAction,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("আপডেট", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
