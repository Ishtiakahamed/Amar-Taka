package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.data.repository.AppLanguage
import com.example.data.repository.ThemeMode
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.ExpenseRed
import com.example.ui.util.CsvExporter
import com.example.ui.util.Localizer
import com.example.ui.util.StreakCalculator
import com.example.ui.viewmodel.FinanceViewModel
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileTab(viewModel: FinanceViewModel) {
    val context = LocalContext.current
    val appLanguage by viewModel.appLanguage.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val budgetLimit by viewModel.budgetLimit.collectAsState()

    val profileName by viewModel.profileName.collectAsState()
    val profileImageUri by viewModel.profileImageUri.collectAsState()

    var tempProfileName by remember(profileName) { mutableStateOf(profileName) }
    var tempProfileImageUri by remember(profileImageUri) { mutableStateOf(profileImageUri) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val takeFlags: Int = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            tempProfileImageUri = uri.toString()
        }
    }

    val isBn = appLanguage == AppLanguage.BN
    val isGlass = themeMode == ThemeMode.GLASSMORPHISM
    val cardBg = if (isGlass) Color(0x3B1E293B) else MaterialTheme.colorScheme.surface

    var showClearConfirm by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var budgetValueStr by remember { mutableStateOf(budgetLimit.toString()) }
    var showLoanScreenInModal by remember { mutableStateOf(false) }

    // Aggregate monthly statistics (This Month)
    val now = Calendar.getInstance()
    val currentMonth = now.get(Calendar.MONTH)
    val currentYear = now.get(Calendar.YEAR)

    val currentMonthTxs = remember(transactions) {
        transactions.filter { tx ->
            val cal = Calendar.getInstance().apply { timeInMillis = tx.date }
            cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
        }
    }

    val monthIncome = currentMonthTxs.filter { it.type == "INCOME" }.sumOf { it.amount }
    val monthExpense = currentMonthTxs.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val monthSavings = currentMonthTxs.filter { it.type == "SAVINGS" }.sumOf { it.amount }
    val profitLoss = monthIncome - monthExpense

    val streakCount = remember(transactions) {
        StreakCalculator.calculateStreak(transactions)
    }

    // Top Category (frequency filter)
    val topCategory = remember(currentMonthTxs, appLanguage) {
        val expenses = currentMonthTxs.filter { it.type == "EXPENSE" }
        if (expenses.isEmpty()) {
            if (appLanguage == AppLanguage.BN) "কোনো ব্যয় নেই" else "No spending"
        } else {
            val freq = expenses.groupBy { it.category }.mapValues { it.value.size }
            val topCat = freq.maxByOrNull { it.value }?.key ?: "অন্যান্য"
            Localizer.translateCategory(topCat, appLanguage)
        }
    }

    // Money Health Score calculation
    val healthScore = remember(monthIncome, monthExpense, currentMonthTxs) {
        if (monthIncome <= 0.0) {
            if (monthExpense > 0.0) 30 else 80
        } else {
            val ratio = monthExpense / monthIncome
            val score = 100 - (ratio * 100).toInt()
            score.coerceIn(10, 100)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(10.dp)) }

        // --- 1. PROFILE SUMMARY (প্রোফাইল সারসংক্ষেপ) ---
        item {
            val nameToDisplay = if (profileName.isNotEmpty()) profileName else (if (isBn) "আমার পার্সোনাল অ্যাকাউন্ট" else "My Personal Ledger")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(if (isGlass) 1.dp else 0.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (profileImageUri.isNotEmpty()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(profileImageUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = if (isBn) "প্রোফাইল ছবি" else "Profile Picture",
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Text("👨‍🎓", fontSize = 28.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = nameToDisplay,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isBn) "অফলাইন স্টুডেন্ট ওয়ালেট ম্যানেজার" else "Offline Student Wallet Manager",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // --- PROFILE CUSTOMIZATION (প্রোফাইল কাস্টমাইজেশন) ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(if (isGlass) 1.dp else 0.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (isBn) "প্রোফাইল কাস্টমাইজেশন" else "Profile Customization",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (tempProfileImageUri.isNotEmpty()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(tempProfileImageUri)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = if (isBn) "প্রোফাইল ছবি" else "Profile Picture",
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Text("👨‍🎓", fontSize = 32.sp)
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = {
                                    imagePickerLauncher.launch("image/*")
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isBn) "ছবি পরিবর্তন করুন" else "Change Photo", fontSize = 11.sp)
                            }

                            if (tempProfileImageUri.isNotEmpty()) {
                                TextButton(
                                    onClick = {
                                        tempProfileImageUri = ""
                                    },
                                    colors = ButtonDefaults.textButtonColors(contentColor = ExpenseRed)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isBn) "ছবি মুছে ফেলুন" else "Remove Photo", fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = tempProfileName,
                        onValueChange = { tempProfileName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(if (isBn) "আপনার নাম" else "Your Name", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                        )
                    )

                    Button(
                        onClick = {
                            viewModel.updateProfile(tempProfileName, tempProfileImageUri)
                            Toast.makeText(context, if (isBn) "প্রোফাইল সফলভাবে আপডেট হয়েছে" else "Profile successfully updated", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(if (isBn) "সংরক্ষণ করুন" else "Save Settings", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- 2. MONTHLY REPORT CARD (মাসিক রিপোর্ট কার্ড) ---
        item {
            val nameToDisplay = if (profileName.isNotEmpty()) profileName else (if (isBn) "আমার পার্সোনাল অ্যাকাউন্ট" else "My Personal Ledger")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(if (isGlass) 1.dp else 0.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isBn) "মাসিক রিপোর্ট কার্ড" else "Monthly Status Card",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Profile info on Report card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (profileImageUri.isNotEmpty()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(profileImageUri)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Text("👨‍🎓", fontSize = 16.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = nameToDisplay,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isBn) "অফলাইন স্টুডেন্ট ওয়ালেট ম্যানেজার" else "Offline Student Wallet Manager",
                                fontSize = 9.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), modifier = Modifier.padding(bottom = 8.dp))

                    // Health Score Gauge
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isBn) "আর্থিক স্বাস্থ্য স্কোর:" else "Financial Health Score:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when {
                                        healthScore >= 80 -> IncomeGreen.copy(alpha = 0.2f)
                                        healthScore >= 50 -> Color.Yellow.copy(alpha = 0.2f)
                                        else -> ExpenseRed.copy(alpha = 0.2f)
                                    }
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$healthScore/১০০",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    healthScore >= 80 -> IncomeGreen
                                    healthScore >= 50 -> Color(0xFFF1C40F)
                                    else -> ExpenseRed
                                }
                            )
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 4.dp))

                    // Details grid
                    ReportRow(label = if (isBn) "মোট আয় (Month)" else "Total Income (Month)", value = "৳${monthIncome.toInt()}", valueColor = IncomeGreen)
                    ReportRow(label = if (isBn) "মোট ব্যয় (Month)" else "Total Expense (Month)", value = "৳${monthExpense.toInt()}", valueColor = ExpenseRed)
                    ReportRow(label = if (isBn) "মোট সঞ্চয় (Month)" else "Total Savings (Month)", value = "৳${monthSavings.toInt()}", valueColor = Color(0xFF0EA5E9))
                    ReportRow(
                        label = if (isBn) "লাভ / ক্ষতি" else "Profit / Loss",
                        value = "${if (profitLoss >= 0) "+" else "-"}৳${Math.abs(profitLoss.toInt())}",
                        valueColor = if (profitLoss >= 0) IncomeGreen else ExpenseRed
                    )
                    ReportRow(label = if (isBn) "শীর্ষ ব্যয়ের খাত" else "Top Expense Tag", value = topCategory, valueColor = MaterialTheme.colorScheme.onSurface)
                    ReportRow(label = if (isBn) "হিসাব রাখার স্ট্রেইক" else "Current Streak", value = if (isBn) "$streakCount দিন" else "$streakCount Days", valueColor = Color(0xFFF1C40F))
                }
            }
        }

        // --- 3. EXPORTS & STATS (রিপোর্ট ও এক্সপোর্ট) ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isBn) "রিপোর্ট ও ডাটা রপ্তানি" else "Reporting & Data Export",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Button(
                    onClick = {
                        val fileName = "AmarTaka_Complete_Export_${System.currentTimeMillis()}.csv"
                        CsvExporter.exportTransactions(context, transactions, fileName)
                        Toast.makeText(context, if (isBn) "সমগ্র হিসাব সফলভাবে সিএসভি রপ্তানি করা হয়েছে!" else "CSV exported successfully!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isBn) "এক্সেল ও সিএসভি সিঙ্ক করুন (CSV)" else "Export Transactions as CSV")
                }
            }
        }

        // --- 4. BACKUP & RESTORE (সম্পূর্ণ অফলাইন ব্যাকআপ) ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(if (isGlass) 1.dp else 0.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isBn) "নিরাপত্তা ও অফলাইন ব্যাকআপ" else "Backup and Offline Security",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (isBn) "আপনার ব্যালেন্স এবং সমস্ত হিসাবের তথ্য কোনো রিমোট সার্ভারে পাঠানো হয় না — এটি সম্পূর্ণ অফলাইন এবং শতভাগ নিরাপদ।" 
                               else "Your ledger details and balances are never uploaded. It is strictly offline and completely safe.",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                // Simple text backup to clipboard
                                val textBuilder = StringBuilder()
                                transactions.forEach {
                                    textBuilder.append("${it.type},${it.amount},${it.category},${it.wallet},${it.note},${it.date}\n")
                                }
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("AmarTakaBackup", textBuilder.toString())
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, if (isBn) "ব্যাকআপ কোড ক্লিপবোর্ডে কপি হয়েছে! এটি নোটপ্যাডে জমা রাখতে পারেন।" else "Backup code copied to clipboard!", Toast.LENGTH_LONG).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isBn) "ডাটা ব্যাকআপ" else "Backup", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                Toast.makeText(context, if (isBn) "ক্লিপবোর্ডে কপি করা ব্যাকআপ থেকে পরবর্তীতে রিস্টোর করা যাবে।" else "Import / Restore available via CSV export backup.", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isBn) "রিস্টোর করুন" else "Restore", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // --- 5. THEME & LANGUAGE (থিম ও ভাষা) ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(if (isGlass) 1.dp else 0.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isBn) "ভাষা ও থিম কনফিগারেশন" else "Language & Dynamic Themes",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Theme selector row
                    Text(text = if (isBn) "অ্যাপের রূপ নির্বাচন:" else "Personalize Theme Layout:", fontSize = 11.sp, color = Color.Gray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(ThemeMode.DAY, ThemeMode.NIGHT, ThemeMode.EYE_CARE, ThemeMode.GLASSMORPHISM).forEach { mode ->
                            val isSel = themeMode == mode
                            val label = when (mode) {
                                ThemeMode.DAY -> if (isBn) "লাইট" else "Light"
                                ThemeMode.NIGHT -> if (isBn) "ডার্ক" else "Dark"
                                ThemeMode.EYE_CARE -> if (isBn) "আই-কেয়ার" else "Warm"
                                ThemeMode.GLASSMORPHISM -> if (isBn) "গ্লাস" else "Glass"
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { viewModel.setThemeMode(mode) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Language toggles
                    Text(text = if (isBn) "ভাষা পরিবর্তন:" else "Select App Language:", fontSize = 11.sp, color = Color.Gray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.setAppLanguage(AppLanguage.BN) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (appLanguage == AppLanguage.BN) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (appLanguage == AppLanguage.BN) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text("বাংলা", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.setAppLanguage(AppLanguage.EN) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (appLanguage == AppLanguage.EN) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (appLanguage == AppLanguage.EN) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text("English", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- 6. APP SETTINGS & FEAT LOCKS (অ্যাপ সেটিংস ও দেনা-পাওনা) ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(if (isGlass) 1.dp else 0.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isBn) "উন্নত সেটিংস" else "Advanced Ledgers",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // 1. Debt Loan screen shortcut
                    SettingsOptionRow(
                        title = if (isBn) "দেনা-পাওনা (Loans & Debts)" else "Loans & Debts",
                        description = if (isBn) "কার কত দেনা-পাওনা আছে তার সম্পূর্ণ হিসাব" else "Track and audit borrowings / lendings",
                        icon = Icons.Default.AccountBox,
                        onClick = { showLoanScreenInModal = true },
                        isBn = isBn
                    )

                    // 2. Clear Daily Budget Limit
                    SettingsOptionRow(
                        title = if (isBn) "দৈনিক বাজেট সীমা" else "Daily Budget Limit",
                        description = if (isBn) "বাজেট ওভার হলে সতর্কতা দেখাবে (৳$budgetLimit)" else "Current Daily Limit set to ৳$budgetLimit",
                        icon = Icons.Default.Warning,
                        onClick = { showBudgetDialog = true },
                        isBn = isBn
                    )

                    // 3. Clear data button
                    SettingsOptionRow(
                        title = if (isBn) "ডাটা রিসেট করুন" else "Factory Reset Apps",
                        description = if (isBn) "অ্যাপের সমস্ত ডাটা মুছে ফেলুন" else "Permanently wipe out local ledger databases",
                        icon = Icons.Default.Delete,
                        onClick = { showClearConfirm = true },
                        isBn = isBn,
                        titleColor = ExpenseRed
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }

    // Modal budget setter dialog
    if (showBudgetDialog) {
        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            containerColor = if (themeMode == ThemeMode.GLASSMORPHISM) Color(0xFF1F1B3D) else MaterialTheme.colorScheme.surface,
            title = { Text(if (isBn) "দৈনিক বাজেট সীমা পরিবর্তন" else "Change Daily Budget Limit", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = budgetValueStr,
                    onValueChange = { budgetValueStr = it },
                    label = { Text(if (isBn) "বাজেট সীমা (৳)" else "Limit (৳)") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val value = budgetValueStr.toDoubleOrNull() ?: 0.0
                        if (value >= 0.0) {
                            viewModel.setBudgetLimit(value)
                            showBudgetDialog = false
                            Toast.makeText(context, if (isBn) "বাজেট সীমা সফলভাবে আপডেট করা হয়েছে!" else "Updated limit successfully!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text(if (isBn) "পরিবর্তন করুন" else "Change")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBudgetDialog = false }) {
                    Text(if (isBn) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Clear all confirm Dialog
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            containerColor = if (themeMode == ThemeMode.GLASSMORPHISM) Color(0xFF1F1B3D) else MaterialTheme.colorScheme.surface,
            title = { Text(if (isBn) "ডাটা মুছে ফেলতে চান?" else "Format Device Database?", color = ExpenseRed, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = if (isBn) "সতর্কতা: আপনার সকল লেনদেন, সঞ্চয়ের লক্ষ্য এবং দেনা-পাওনার হিসাব চিরতরে মুছে যাবে। এটি রিস্টোর করা যাবে না।"
                           else "Warning: All accounts, savings, and borrowings will be deleted permanently. This action cannot be reversed."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showClearConfirm = false
                        Toast.makeText(context, if (isBn) "সমস্ত ডাটা মুছে ফেলা হয়েছে!" else "All ledger database formatted!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text(if (isBn) "মুছে ফেলুন" else "Delete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text(if (isBn) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Modal view for complete LoanScreen to keep "Debt management" fully operational in 5-tab layout
    if (showLoanScreenInModal) {
        AlertDialog(
            onDismissRequest = { showLoanScreenInModal = false },
            containerColor = if (themeMode == ThemeMode.GLASSMORPHISM) Color(0xFF1F1B3D) else MaterialTheme.colorScheme.surface,
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxSize().padding(10.dp),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isBn) "দেনা-পাওনা ম্যানেজার" else "Borrowed & Lent Ledger", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { showLoanScreenInModal = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            },
            text = {
                Box(modifier = Modifier.fillMaxSize()) {
                    LoanScreen(viewModel = viewModel)
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
fun ReportRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
fun SettingsOptionRow(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isBn: Boolean,
    titleColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = titleColor)
            Text(text = description, fontSize = 10.sp, color = Color.Gray)
        }
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Open", tint = Color.Gray, modifier = Modifier.size(18.dp))
    }
}
