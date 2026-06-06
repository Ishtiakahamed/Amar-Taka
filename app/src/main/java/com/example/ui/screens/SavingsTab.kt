package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import com.example.data.model.SavingsGoal
import com.example.data.model.Transaction
import com.example.data.repository.AppLanguage
import com.example.data.repository.ThemeMode
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.ExpenseRed
import com.example.ui.util.Localizer
import com.example.ui.util.StreakCalculator
import com.example.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SavingsTab(viewModel: FinanceViewModel) {
    val context = LocalContext.current
    val appLanguage by viewModel.appLanguage.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val savingsGoals by viewModel.savingsGoals.collectAsState()
    val budgetLimit by viewModel.budgetLimit.collectAsState()

    val joinedMap by viewModel.joinedChallenges.collectAsState()
    val completedSet by viewModel.completedChallenges.collectAsState()
    val progressMap by viewModel.challengeProgress.collectAsState()

    var showGoalDialog by remember { mutableStateOf(false) }

    val isBn = appLanguage == AppLanguage.BN
    val isGlass = themeMode == ThemeMode.GLASSMORPHISM
    val cardBg = if (isGlass) Color(0x3B1E293B) else MaterialTheme.colorScheme.surface

    // Calculate Streak
    val streakCount = remember(transactions) {
        StreakCalculator.calculateStreak(transactions)
    }

    val totalGoalSavings = savingsGoals.sumOf { it.currentAmount }

    // Badges conditions
    val isFirstEntryUnlocked = transactions.isNotEmpty()
    val is3DayStreakUnlocked = streakCount >= 3
    val is7DayStreakUnlocked = streakCount >= 7
    val is30DayStreakUnlocked = streakCount >= 30
    val isBudgetSaverUnlocked = budgetLimit > 0.0 && remember(transactions, budgetLimit) {
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val monthExpense = transactions.filter { tx ->
            if (tx.type != "EXPENSE") return@filter false
            val cal = Calendar.getInstance().apply { timeInMillis = tx.date }
            cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
        }.sumOf { it.amount }
        monthExpense <= budgetLimit
    }

    // Daily motivation quote list
    val quote = remember(appLanguage) {
        val quotesBn = listOf(
            "সঞ্চয় ছোট হলেও একদিন তা বড় বিপদে সাহায্য করে। 🌱",
            "বাজেট হলো নিজের অর্থকে পরিচালনা করার মানচিত্র। 🗺️",
            "অপ্রয়োজনীয় খরচ কমানো মানেই নিজের সঞ্চয় বাড়ানো! 🎯",
            "আজকের ছোট ছোট ত্যাগ আগামীকালের আর্থিক স্বাধীনতার মূল চাবিকাঠি। 🔥"
        )
        val quotesEn = listOf(
            "A small saving today is an umbrella for a rainy day tomorrow. 🌱",
            "A budget tells your money where to go, instead of wondering where it went. 🗺️",
            "Cutting extra spend today paves the way for tomorrow's wealth! 🎯",
            "Compound interest and constant practice creates miracles. 🔥"
        )
        val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) % 4
        if (appLanguage == AppLanguage.BN) quotesBn[dayOfWeek] else quotesEn[dayOfWeek]
    }

    // Weekly calculations for the last 7 days savings
    val weeklySavings = remember(transactions, appLanguage) {
        val list = mutableListOf<Pair<String, Double>>()
        val sdf = SimpleDateFormat("EEE", if (appLanguage == AppLanguage.BN) Locale("bn", "BD") else Locale.US)
        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = cal.timeInMillis
            cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59)
            val endOfDay = cal.timeInMillis
            val label = sdf.format(Date(startOfDay))
            val sum = transactions.filter { it.type == "SAVINGS" && it.date in startOfDay..endOfDay }.sumOf { it.amount }
            list.add(Pair(label, sum))
        }
        list
    }

    val maxWeeklySavingsAmount = remember(weeklySavings) {
        weeklySavings.maxOfOrNull { it.second } ?: 1.0
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(10.dp)) }

        // --- 1. DAILY SAVINGS QUOTE (আজকের অনুপ্রেরণা) ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(if (isGlass) 1.dp else 0.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (isGlass) Color(0x3B6B21A8) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💡", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isBn) "আজকের অনুপ্রেরণা" else "Daily Savings Quote",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = quote,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // --- 2. WEEKLY SAVINGS BAR CHART (সাপ্তাহিক সঞ্চয়) ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(if (isGlass) 1.dp else 0.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isBn) "সাপ্তাহিক সঞ্চয় (৭ দিন)" else "Weekly Savings (7 Days)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        weeklySavings.forEach { pair ->
                            val textLabel = pair.first
                            val amount = pair.second
                            val factor = if (maxWeeklySavingsAmount > 0) (amount / maxWeeklySavingsAmount).toFloat() else 0f
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (amount > 0) {
                                    Text(
                                        text = if (isBn) "৳${amount.toInt()}" else "৳${amount.toInt()}",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = IncomeGreen
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                }

                                Box(
                                    modifier = Modifier
                                        .width(14.dp)
                                        .height((100 * factor).coerceAtLeast(1f).dp)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(
                                            if (amount > 0) {
                                                Brush.verticalGradient(listOf(IncomeGreen, IncomeGreen.copy(alpha = 0.5f)))
                                            } else {
                                                androidx.compose.ui.graphics.SolidColor(Color.Gray.copy(alpha = 0.2f))
                                            }
                                        )
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = textLabel,
                                    fontSize = 9.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 3. ONGOING CHALLENGES (চলমান চ্যালেঞ্জ) ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isBn) "চলমান চ্যালেঞ্জসমূহ" else "Active Saving Challenges",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                ChallengeRow(
                    id = "CHALLENGE_7_DAY_SAVING",
                    title = if (isBn) "৭ দিনের সেভিং চ্যালেঞ্জ" else "7-Day Saving Tracker",
                    desc = if (isBn) "৭ দিন একটানা হিসাব রাখুন" else "Log transactions consecutively for 7 days",
                    progress = progressMap["CHALLENGE_7_DAY_SAVING"] ?: 0,
                    maxVal = 7,
                    isJoined = joinedMap.containsKey("CHALLENGE_7_DAY_SAVING"),
                    isCompleted = completedSet.contains("CHALLENGE_7_DAY_SAVING"),
                    onStart = { viewModel.startChallenge("CHALLENGE_7_DAY_SAVING") },
                    onAction = {
                        val msg = if (isBn) "হিসাব যোগ করলেই প্রগ্রেস বৃদ্ধি পাবে!" else "Add records daily to increment progress!"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                )

                ChallengeRow(
                    id = "CHALLENGE_NO_FAST_FOOD",
                    title = if (isBn) "ফাস্টফুড বর্জন চ্যালেঞ্জ" else "No Fast Food Challenge",
                    desc = if (isBn) "৭ দিন বাইরের ভারী খাবার এড়ানো" else "Avoid restaurant foods for 7 days",
                    progress = progressMap["CHALLENGE_NO_FAST_FOOD"] ?: 0,
                    maxVal = 7,
                    isJoined = joinedMap.containsKey("CHALLENGE_NO_FAST_FOOD"),
                    isCompleted = completedSet.contains("CHALLENGE_NO_FAST_FOOD"),
                    onStart = { viewModel.startChallenge("CHALLENGE_NO_FAST_FOOD") },
                    onAction = {
                        viewModel.incrementChallengeProgress("CHALLENGE_NO_FAST_FOOD", 7)
                        Toast.makeText(context, if (isBn) "সাশ্রয়ী দিন সফল! +১ দিন" else "Succeeded today! +1 Day", Toast.LENGTH_SHORT).show()
                    }
                )

                ChallengeRow(
                    id = "CHALLENGE_NO_RICKSHAW",
                    title = if (isBn) "রিকশা সাশ্রয় চ্যালেঞ্জ" else "No Extra Rickshaw Ride",
                    desc = if (isBn) "অল্প দূরত্ব হেঁটে ৭ দিন চলা" else "Walk short distances for 7 days",
                    progress = progressMap["CHALLENGE_NO_RICKSHAW"] ?: 0,
                    maxVal = 7,
                    isJoined = joinedMap.containsKey("CHALLENGE_NO_RICKSHAW"),
                    isCompleted = completedSet.contains("CHALLENGE_NO_RICKSHAW"),
                    onStart = { viewModel.startChallenge("CHALLENGE_NO_RICKSHAW") },
                    onAction = {
                        viewModel.incrementChallengeProgress("CHALLENGE_NO_RICKSHAW", 7)
                        Toast.makeText(context, if (isBn) "রিকশা সাশ্রয় সফল! +১ দিন" else "Saved rickshaw ride! +1 Day", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // --- 4. ACCUMULATED BADGES (অর্জিত ব্যাজ সমূহ) ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(if (isGlass) 1.dp else 0.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isBn) "অর্জিত সাফল্যের ব্যাজ সমূহ" else "Accumulated Achievement Badges",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item { BadgeItem("🥇", if (isBn) "১ম হিসাব" else "First entry", isFirstEntryUnlocked) }
                        item { BadgeItem("⚡", if (isBn) "৩ দিন" else "3d Streak", is3DayStreakUnlocked) }
                        item { BadgeItem("🔥", if (isBn) "৭ দিন" else "7d Streak", is7DayStreakUnlocked) }
                        item { BadgeItem("👑", if (isBn) "৩০ দিন" else "30d Streak", is30DayStreakUnlocked) }
                        item { BadgeItem("🛡️", if (isBn) "বাজেট হিরো" else "Budget Saver", isBudgetSaverUnlocked) }
                    }
                }
            }
        }

        // --- 5. SAVINGS GOALS READ & CREATE (সঞ্চয় লক্ষ্যসমূহ) ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBn) "আমার সঞ্চয় লক্ষ্যসমূহ" else "Savings Targets & Goals",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Button(
                    onClick = { showGoalDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBn) "+ লক্ষ্য" else "+ Goal", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (savingsGoals.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isBn) "নির্ধারিত কোনো সঞ্চয় লক্ষ্য নেই। যুক্ত করতে উপরের বোতামটি চাপুন।" else "No savings targets set yet. Tap above to create one.",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(savingsGoals) { goal ->
                SavingsGoalCardItem(
                    goal = goal,
                    appLanguage = appLanguage,
                    onDeposit = { amt ->
                        viewModel.updateSavingsAmount(goal, amt)
                        Toast.makeText(context, if (isBn) "সঞ্চয় যুক্ত করা হয়েছে!" else "Savings deposited!", Toast.LENGTH_SHORT).show()
                    },
                    onDelete = {
                        viewModel.deleteSavingsGoal(goal)
                        Toast.makeText(context, if (isBn) "সঞ্চয় লক্ষ্য মুছে ফেলা হয়েছে!" else "Goal deleted!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(10.dp)) }
    }

    if (showGoalDialog) {
        AddSavingsGoalModal(
            appLanguage = appLanguage,
            onDismiss = { showGoalDialog = false },
            onConfirm = { name, target, initial ->
                viewModel.addSavingsGoal(name, target, initial)
                showGoalDialog = false
                Toast.makeText(context, if (isBn) "সঞ্চয় লক্ষ্য যুক্ত করা হয়েছে!" else "Savings goal created successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun SavingsGoalCardItem(goal: SavingsGoal, appLanguage: AppLanguage, onDeposit: (Double) -> Unit, onDelete: () -> Unit) {
    val progress = if (goal.targetAmount <= 0) 0f else (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
    val isBn = appLanguage == AppLanguage.BN

    var showDepositDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(IncomeGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = IncomeGreen, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = goal.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showDepositDialog = true }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "Deposit", tint = IncomeGreen)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ExpenseRed)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isBn) "অর্জিত: ৳${goal.currentAmount.toInt()}" else "Saved: ৳${goal.currentAmount.toInt()}",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isBn) "লক্ষ্য: ৳${goal.targetAmount.toInt()}" else "Target: ৳${goal.targetAmount.toInt()}",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = IncomeGreen,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = IncomeGreen,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }

    if (showDepositDialog) {
        DepositMoneyModal(
            goalTitle = goal.title,
            appLanguage = appLanguage,
            onDismiss = { showDepositDialog = false },
            onSave = { amount ->
                onDeposit(amount)
                showDepositDialog = false
            }
        )
    }
}

@Composable
fun AddSavingsGoalModal(appLanguage: AppLanguage, onDismiss: () -> Unit, onConfirm: (String, Double, Double) -> Unit) {
    var title by remember { mutableStateOf("") }
    var targetStr by remember { mutableStateOf("") }
    var initialStr by remember { mutableStateOf("") }
    val isBn = appLanguage == AppLanguage.BN

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isBn) "নতুন সঞ্চয় লক্ষ্য যোগ করুন" else "Create New Savings Target", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isBn) "লক্ষ্যের নাম" else "Goal Title (e.g., Exam Prep)") }
                )
                OutlinedTextField(
                    value = targetStr,
                    onValueChange = { targetStr = it },
                    label = { Text(if (isBn) "মোট লক্ষ্যের টাকা (৳)" else "Target Amount (৳)") }
                )
                OutlinedTextField(
                    value = initialStr,
                    onValueChange = { initialStr = it },
                    label = { Text(if (isBn) "প্রাথমিক জমা (ঐচ্ছিক)" else "Initial Deposit (Optional)") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = targetStr.toDoubleOrNull() ?: 0.0
                    val initial = initialStr.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && target > 0) {
                        onConfirm(title, target, initial)
                    }
                }
            ) {
                Text(if (isBn) "নিশ্চিত করুন" else "Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBn) "বাতিল" else "Cancel")
            }
        }
    )
}

@Composable
fun DepositMoneyModal(goalTitle: String, appLanguage: AppLanguage, onDismiss: () -> Unit, onSave: (Double) -> Unit) {
    var amountStr by remember { mutableStateOf("") }
    val isBn = appLanguage == AppLanguage.BN

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isBn) "সঞ্চয় যুক্ত করুন: $goalTitle" else "Deposit to: $goalTitle", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = amountStr,
                onValueChange = { amountStr = it },
                label = { Text(if (isBn) "টাকার পরিমাণ (৳)" else "Amount (৳)") }
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onSave(amt)
                    }
                }
            ) {
                Text(if (isBn) "কনফার্ম" else "Deposit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBn) "বাতিল" else "Cancel")
            }
        }
    )
}


