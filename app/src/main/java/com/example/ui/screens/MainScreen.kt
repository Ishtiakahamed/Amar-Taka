package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Loan
import com.example.data.model.SavingsGoal
import com.example.data.model.Transaction
import com.example.data.repository.AppLanguage
import com.example.data.repository.ThemeMode
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.GlassOnPrimary
import com.example.ui.theme.IncomeGreen
import com.example.ui.util.Localizer
import com.example.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*

// Formatter for BDT currency
fun formatBDT(value: Double): String {
    return String.format(Locale.US, "%,.2f", value)
}

// Glassmorphism border modifier
fun Modifier.glassBorder(themeMode: ThemeMode, shape: Shape): Modifier {
    return if (themeMode == ThemeMode.GLASSMORPHISM) {
        this.border(1.dp, Color.White.copy(alpha = 0.2f), shape)
    } else {
        this
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: FinanceViewModel) {
    val appLanguage by viewModel.appLanguage.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val savingsGoals by viewModel.savingsGoals.collectAsState()
    var currentTab by remember { mutableStateOf(0) }
    var showQuickAddSheet by remember { mutableStateOf(false) }

    val isGlass = themeMode == ThemeMode.GLASSMORPHISM
    val maxBg = if (isGlass) Color(0xFF0F172A) else MaterialTheme.colorScheme.background

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (appLanguage == AppLanguage.BN) "আমার টাকা" else "Amar Taka",
                        fontWeight = FontWeight.Bold,
                        color = if (isGlass) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isGlass) Color(0xFF0F172A) else MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = if (isGlass) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = if (isGlass) Color(0xCF1E293B) else MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                val items = listOf(
                    if (appLanguage == AppLanguage.BN) "হোম" else "Home",
                    if (appLanguage == AppLanguage.BN) "ক্যালেন্ডার" else "Calendar",
                    if (appLanguage == AppLanguage.BN) "হিসাব" else "Records",
                    if (appLanguage == AppLanguage.BN) "সঞ্চয়" else "Savings",
                    if (appLanguage == AppLanguage.BN) "প্রোফাইল" else "Profile"
                )
                val icons = listOf(
                    Icons.Default.Home,
                    Icons.Default.DateRange,
                    Icons.Default.List,
                    Icons.Default.Star,
                    Icons.Default.Person
                )

                items.forEachIndexed { index, label ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = label) },
                        label = { Text(label, fontSize = 10.sp) },
                        selected = currentTab == index,
                        onClick = { currentTab = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = if (isGlass) Color(0xFFC084FC) else MaterialTheme.colorScheme.primary,
                            indicatorColor = Color(0xFF8B5CF6),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showQuickAddSheet = true },
                containerColor = Color(0xFF8B5CF6),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (appLanguage == AppLanguage.BN) "+ হিসাব" else "+ Record",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        },
        containerColor = maxBg
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentTab) {
                0 -> DashboardTab(viewModel)
                1 -> CalendarView(viewModel)
                2 -> HisabTab(viewModel)
                3 -> SavingsTab(viewModel)
                4 -> ProfileTab(viewModel)
            }
        }
    }

    if (showQuickAddSheet) {
        GlobalQuickAddDialog(
            appLanguage = appLanguage,
            viewModel = viewModel,
            onDismiss = { showQuickAddSheet = false }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GlobalQuickAddDialog(
    appLanguage: AppLanguage,
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val themeMode by viewModel.themeMode.collectAsState()
    val savingsGoals by viewModel.savingsGoals.collectAsState()
    val isBn = appLanguage == AppLanguage.BN

    var quickType by remember { mutableStateOf("EXPENSE") }
    var quickAmount by remember { mutableStateOf("") }
    var quickWallet by remember { mutableStateOf("ক্যাশ") }
    var quickCategory by remember { mutableStateOf("অন্যান্য") }
    var quickNote by remember { mutableStateOf("") }
    var transferDestWallet by remember { mutableStateOf("বিকাশ") }
    var loanPersonName by remember { mutableStateOf("") }
    var loanType by remember { mutableStateOf("GAVE") } // GAVE (Lent), TOOK (Borrowed)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (themeMode == ThemeMode.GLASSMORPHISM) Color(0xFF1F1B3D) else MaterialTheme.colorScheme.surface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBn) "নতুন হিসাব যুক্ত করুন" else "Quick Transaction Ledger",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Type selector row
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
                                .background(if (isSel) Color(0xFF8B5CF6) else Color.Transparent)
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
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Amount
                OutlinedTextField(
                    value = quickAmount,
                    onValueChange = { quickAmount = it },
                    label = { Text(if (isBn) "টাকার পরিমাণ (৳)" else "Amount (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                // Extra sections
                if (quickType == "TRANSFER") {
                    Text(text = if (isBn) "কোথায় পাঠাবেন?" else "Target wallet account:", fontSize = 10.sp, color = Color.Gray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val listW = listOf("ক্যাশ", "বিকাশ", "নগদ", "রকেট", "ব্যাংক")
                        listW.forEach { w ->
                            val isSel = transferDestWallet == w
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { transferDestWallet = w }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(Localizer.translateWallet(w, appLanguage), color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                            }
                        }
                    }
                } else if (quickType == "LOAN") {
                    OutlinedTextField(
                        value = loanPersonName,
                        onValueChange = { loanPersonName = it },
                        label = { Text(if (isBn) "ব্যক্তির নাম" else "Person Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
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
                                fontSize = 10.sp,
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
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (loanType == "TOOK") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Source Account Wallet selection chips
                Text(text = if (isBn) "কোথা থেকে লেনদেন হয়েছে?" else "Source Wallet:", fontSize = 10.sp, color = Color.Gray)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val listW = listOf("ক্যাশ", "বিকাশ", "নগদ", "রকেট", "ব্যাংক")
                    listW.forEach { w ->
                        val isSel = quickWallet == w
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { quickWallet = w }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = Localizer.translateWallet(w, appLanguage),
                                fontSize = 10.sp,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Note Description
                OutlinedTextField(
                    value = quickNote,
                    onValueChange = { quickNote = it },
                    label = { Text(if (isBn) "সংক্ষিপ্ত নোট (ঐচ্ছিক)" else "Brief note (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                // Categories
                Text(text = if (isBn) "খাত নির্বাচন করুন:" else "Category:", fontSize = 10.sp, color = Color.Gray)
                val cats = when (quickType) {
                    "EXPENSE" -> listOf("খাবার", "ভাড়া", "বাজার/শপিং", "পরিবহন", "চিকিৎসা", "বিনোদন", "বিল", "অন্যান্য")
                    "INCOME" -> listOf("বেতন", "ব্যবসা", "ফ্রিল্যান্সিং", "উপহার", "অন্যান্য")
                    "SAVINGS" -> listOf("ভবিষ্যৎ সঞ্চয়", "জরুরী ফান্ড", "ডিপোজিট", "অন্যান্য")
                    "LOAN" -> listOf("বন্ধুবান্ধব", "পরিবার", "ব্যাংক লোন", "অন্যান্য")
                    else -> listOf("ক্যাশ", "বিকাশ", "নগদ", "রকেট", "ব্যাংক")
                }
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    cats.forEach { c ->
                        val isSel = quickCategory == c
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { quickCategory = c }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (quickType == "TRANSFER") Localizer.translateWallet(c, appLanguage) else Localizer.translateCategory(c, appLanguage),
                                fontSize = 10.sp,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val value = quickAmount.toDoubleOrNull()
                    if (value == null || value <= 0.0) {
                        Toast.makeText(context, if (isBn) "সঠিক টাকার অংক লিখুন!" else "Please write a valid amount!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    val resolvedCat = if (quickType == "TRANSFER") transferDestWallet else quickCategory
                    val resolvedNote = when (quickType) {
                        "TRANSFER" -> if (isBn) "স্থানান্তর: $quickWallet থেকে $transferDestWallet" else "Transferred from $quickWallet to $transferDestWallet"
                        "LOAN" -> (if (loanType == "GAVE") "ঋণ দেওয়া হয়েছে: " else "ঋণ নেওয়া হয়েছে: ") + loanPersonName + " " + quickNote
                        else -> quickNote
                    }
                    
                    viewModel.addTransaction(
                        amount = value,
                        type = quickType,
                        category = resolvedCat,
                        wallet = quickWallet,
                        note = resolvedNote,
                        date = System.currentTimeMillis()
                    )

                    if (quickType == "SAVINGS" && savingsGoals.isNotEmpty()) {
                        viewModel.updateSavingsAmount(savingsGoals.first(), value)
                    }
                    
                    if (quickType == "LOAN") {
                        viewModel.addLoan(
                            personName = loanPersonName,
                            amount = value,
                            type = loanType,
                            note = resolvedNote,
                            dueDate = System.currentTimeMillis() + 14 * 24 * 3600 * 1000L
                        )
                    }

                    onDismiss()
                    Toast.makeText(context, if (isBn) "সফলভাবে রেকর্ড করা হয়েছে! 🎉" else "Saved successfully! 🎉", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
            ) {
                Text(if (isBn) "সংরক্ষণ" else "Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBn) "বাতিল" else "Cancel")
            }
        }
    )
}

// --- TRACKER/HOME TAB ---
@Composable
fun TrackerTab(viewModel: FinanceViewModel) {
    val context = LocalContext.current
    val appLanguage by viewModel.appLanguage.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val budgetLimit by viewModel.budgetLimit.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val savingsGoals by viewModel.savingsGoals.collectAsState()

    var amountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var txType by remember { mutableStateOf("EXPENSE") } // INCOME or EXPENSE
    var selectedWallet by remember { mutableStateOf("ক্যাশ") }
    var selectedCategory by remember { mutableStateOf("অন্যান্য") }

    val monthExpense = viewModel.getMonthExpensesSum()
    val firstGoal = savingsGoals.firstOrNull()

    val isGlass = themeMode == ThemeMode.GLASSMORPHISM

    val wallets = listOf("ক্যাশ", "বিকাশ", "রকেট", "নগদ", "ব্যাংক")
    val incomeCategories = listOf("বেতন", "ব্যবসা", "ফ্রিল্যান্সিং", "উপহার", "অন্যান্য")
    val expenseCategories = listOf("খাবার", "ভাড়া", "বাজার/শপিং", "পরিবহন", "চিকিৎসা", "বিনোদন", "বিল", "অন্যান্য")

    LaunchedEffect(txType) {
        selectedCategory = if (txType == "INCOME") "বেতন" else "অন্যান্য"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Daily Streak and challenges card
        item {
            ChallengesCard(viewModel)
        }

        // Budget warnings and Goals Summary Card
        item {
            if (budgetLimit > 0.0) {
                val remaining = budgetLimit - monthExpense
                val isExceeded = remaining < 0.0
                val warnColor = if (isExceeded) ExpenseRed else IncomeGreen
                val warnText = when (themeMode) {
                    ThemeMode.GLASSMORPHISM -> Color(0xFFEF5350)
                    ThemeMode.NIGHT -> Color(0xFFEF5350)
                    else -> Color(0xFFC62828)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassBorder(themeMode, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isGlass) Color(0x2EE57373) else warnColor.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isExceeded) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isExceeded) warnText else IncomeGreen,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isExceeded) {
                                    if (appLanguage == AppLanguage.BN) "আপনি বাজেট খরচ সীমা অতিক্রম করেছেন!" else "You exceeded the budget!"
                                } else {
                                    if (appLanguage == AppLanguage.BN) "আপনি খরচ সীমার ভেতরে আছেন" else "You are within budget"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isExceeded) warnText else IncomeGreen
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (appLanguage == AppLanguage.BN) {
                                    "বাজেট বাকি: ৳${formatBDT(remaining)}"
                                } else {
                                    "Budget left: ৳${formatBDT(remaining)}"
                                },
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Add Transaction Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassBorder(themeMode, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isGlass) Color(0x1F334155) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (appLanguage == AppLanguage.BN) "নতুন হিসাব যুক্ত করুন" else "Add New Transaction",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Type Selector Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (txType == "INCOME") IncomeGreen else Color.Transparent)
                                .clickable { txType = "INCOME" }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (appLanguage == AppLanguage.BN) "আয় (Income)" else "Income",
                                fontWeight = FontWeight.Bold,
                                color = if (txType == "INCOME") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (txType == "EXPENSE") ExpenseRed else Color.Transparent)
                                .clickable { txType = "EXPENSE" }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (appLanguage == AppLanguage.BN) "ব্যয় (Expense)" else "Expense",
                                fontWeight = FontWeight.Bold,
                                color = if (txType == "EXPENSE") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Input Amount
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text(if (appLanguage == AppLanguage.BN) "টাকার পরিমাণ (৳)" else "Amount (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Input Note
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text(if (appLanguage == AppLanguage.BN) "নোট/বিবরণ" else "Note/Description (Optional)") },
                        placeholder = { Text(if (appLanguage == AppLanguage.BN) "যেমন: দুপুরের খাবার খরচ..." else "E.g., Lunch, Salary, etc...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Select Wallet
                    Text(
                        text = if (appLanguage == AppLanguage.BN) "হিসাব/ওয়ালেট নির্বাচন করুন:" else "Select Wallet:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        wallets.forEach { w ->
                            val isSelected = selectedWallet == w
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { selectedWallet = w }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = Localizer.translateWallet(w, appLanguage),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Select Category
                    Text(
                        text = if (appLanguage == AppLanguage.BN) "খরচ/আয়ের খাত বা শ্রেণী:" else "Select Category:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    val categories = if (txType == "INCOME") incomeCategories else expenseCategories
                    FlowRowLayout(
                        spacing = 6.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        categories.forEach { cat ->
                            val isSelected = selectedCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { selectedCategory = cat }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = Localizer.translateCategory(cat, appLanguage),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Add Button
                    Button(
                        onClick = {
                            val amt = amountStr.toDoubleOrNull()
                            if (amt == null || amt <= 0) {
                                Toast.makeText(
                                    context,
                                    if (appLanguage == AppLanguage.BN) "সঠিক টাকা প্রদান করুন" else "Please enter a valid amount",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }
                            viewModel.addTransaction(
                                amount = amt,
                                type = txType,
                                category = selectedCategory,
                                wallet = selectedWallet,
                                note = note,
                                date = System.currentTimeMillis()
                            )
                            amountStr = ""
                            note = ""

                            val feedbackMsg = if (txType == "EXPENSE") {
                                if (appLanguage == AppLanguage.BN) {
                                    val items = listOf(
                                        "খরচ যোগ হয়েছে ✅",
                                        "আজ একটু সাবধানে খরচ করুন 😅",
                                        "ভালো! হিসাব রাখা চালিয়ে যান 🔥",
                                        "আজকের budget limit খেয়াল রাখুন!"
                                    )
                                    items.random()
                                } else {
                                    val items = listOf(
                                        "Expense added successfully! ✅",
                                        "Spend a bit carefully today! 😅",
                                        "Great! Keep tracking your spending. 🔥",
                                        "Keep an eye on today's budget limit!"
                                    )
                                    items.random()
                                }
                            } else {
                                if (appLanguage == AppLanguage.BN) {
                                    "আয় যুক্ত হয়েছে! চমৎকার! 💰"
                                } else {
                                    "Income logged! Fantastic! 💰"
                                }
                            }
                            Toast.makeText(context, feedbackMsg, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (txType == "INCOME") IncomeGreen else ExpenseRed
                        )
                    ) {
                        Text(
                            text = if (appLanguage == AppLanguage.BN) "যুক্ত করুন" else "Add Transaction",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Active savings goals summary banner
        item {
            if (firstGoal != null) {
                val progressFloat = (firstGoal.currentAmount / firstGoal.targetAmount).toFloat().coerceIn(0f, 1f)
                val progressPct = (progressFloat * 100).toInt()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassBorder(themeMode, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isGlass) Color(0x1F1E293B) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${Localizer.t("SAVINGS_GOAL_TITLE", appLanguage)}${firstGoal.title}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "$progressPct%",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = IncomeGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = progressFloat,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = IncomeGreen,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// Custom flow row component to map categories beautifully
@Composable
fun FlowRowLayout(
    spacing: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val spacingPx = spacing.roundToPx()
        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0)) }

        var rowWidth = 0
        var rowHeight = 0
        var totalHeight = 0

        val rows = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()

        placeables.forEach { p ->
            if (rowWidth + p.width + spacingPx > constraints.maxWidth && currentRow.isNotEmpty()) {
                rows.add(currentRow)
                totalHeight += rowHeight + spacingPx
                rowWidth = 0
                rowHeight = 0
                currentRow = mutableListOf()
            }
            currentRow.add(p)
            rowWidth += p.width + spacingPx
            rowHeight = maxOf(rowHeight, p.height)
        }
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
            totalHeight += rowHeight
        }

        layout(constraints.maxWidth, maxOf(totalHeight, 0)) {
            var y = 0
            rows.forEach { row ->
                var x = 0
                var maxHeight = 0
                row.forEach { p ->
                    p.placeRelative(x, y)
                    x += p.width + spacingPx
                    maxHeight = maxOf(maxHeight, p.height)
                }
                y += maxHeight + spacingPx
            }
        }
    }
}

// --- HISTORY & REPORTS TAB ---
@Composable
fun HistoryTab(viewModel: FinanceViewModel) {
    val context = LocalContext.current
    val appLanguage by viewModel.appLanguage.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedMonth by remember { mutableStateOf("ALL") } // Month filter, formatted in Locale or ALL
    var historyMode by remember { mutableStateOf("LIST") } // LIST, CALENDAR, or YEARLY

    val filteredList = remember(transactions, searchQuery, selectedMonth) {
        transactions.filter { tx ->
            val matchQuery = searchQuery.isBlank() ||
                    tx.note.contains(searchQuery, ignoreCase = true) ||
                    tx.category.contains(searchQuery, ignoreCase = true) ||
                    Localizer.translateCategory(tx.category, appLanguage).contains(searchQuery, ignoreCase = true)

            val matchMonth = if (selectedMonth == "ALL") {
                true
            } else {
                val cal = Calendar.getInstance().apply { timeInMillis = tx.date }
                val monthString = SimpleDateFormat("MMMM yyyy", Locale.US).format(cal.time)
                monthString == selectedMonth
            }

            matchQuery && matchMonth
        }
    }

    // Monthly summation metrics
    val incomeSum = filteredList.filter { it.type == "INCOME" }.sumOf { it.amount }
    val expenseSum = filteredList.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val netBalance = incomeSum - expenseSum

    val isGlass = themeMode == ThemeMode.GLASSMORPHISM

    // Determine distinct months list
    val distinctMonths = remember(transactions) {
        val list = mutableListOf<String>()
        val cal = Calendar.getInstance()
        transactions.forEach { tx ->
            cal.timeInMillis = tx.date
            val monthStr = SimpleDateFormat("MMMM yyyy", Locale.US).format(cal.time)
            if (!list.contains(monthStr)) {
                list.add(monthStr)
            }
        }
        list
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Mode Selector Tab Bar
        TabRow(
            selectedTabIndex = when(historyMode) {
                "LIST" -> 0
                "CALENDAR" -> 1
                else -> 2
            },
            containerColor = if (isGlass) Color(0xFF1E293B) else MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = historyMode == "LIST",
                onClick = { historyMode = "LIST" },
                text = { Text(if (appLanguage == AppLanguage.BN) "ইতিহাস তালিকা" else "History List", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
            Tab(
                selected = historyMode == "CALENDAR",
                onClick = { historyMode = "CALENDAR" },
                text = { Text(if (appLanguage == AppLanguage.BN) "ক্যালেন্ডার ভিউ" else "Calendar View", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
            Tab(
                selected = historyMode == "YEARLY",
                onClick = { historyMode = "YEARLY" },
                text = { Text(if (appLanguage == AppLanguage.BN) "বার্ষিক হিসাব" else "Yearly Stats", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            when (historyMode) {
                "CALENDAR" -> {
                    CalendarView(viewModel)
                }
                "YEARLY" -> {
                    YearlyDashboard(viewModel)
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Monthly Metrics Overview Header Card
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .glassBorder(themeMode, RoundedCornerShape(20.dp)),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isGlass) Color(0x3B0F172A) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        text = Localizer.t("REPORT_SUMMARY_HEADER", appLanguage),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(Localizer.t("TOTAL_INCOME", appLanguage), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                                            Text("৳${formatBDT(incomeSum)}", fontWeight = FontWeight.Bold, color = IncomeGreen, fontSize = 15.sp)
                                        }
                                        Column {
                                            Text(Localizer.t("TOTAL_EXPENSE", appLanguage), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                                            Text("৳${formatBDT(expenseSum)}", fontWeight = FontWeight.Bold, color = ExpenseRed, fontSize = 15.sp)
                                        }
                                        Column {
                                            Text(Localizer.t("NET_BALANCE", appLanguage), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                                            Text("৳${formatBDT(netBalance)}", fontWeight = FontWeight.Bold, color = if (netBalance >= 0) IncomeGreen else ExpenseRed, fontSize = 15.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // Filters section: Search note + Dropdown month selector
                        item {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text(Localizer.t("REPORT_SEARCH_PLACEHOLDER", appLanguage), fontSize = 13.sp) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // Distinct Month filter selector chips
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // ALL option
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedMonth == "ALL") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { selectedMonth = "ALL" }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (appLanguage == AppLanguage.BN) "সব সময়" else "All Time",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedMonth == "ALL") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                distinctMonths.forEach { m ->
                                    val isSel = selectedMonth == m
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                            .clickable { selectedMonth = m }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = m,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // Export CSV option
                        item {
                            Button(
                                onClick = {
                                    val fileName = if (selectedMonth == "ALL") {
                                        val currentMonthName = SimpleDateFormat("MMMM", Locale.US).format(Date())
                                        val currentYearName = SimpleDateFormat("yyyy", Locale.US).format(Date())
                                        "AmarTaka_Monthly_Report_${currentMonthName}_${currentYearName}.csv"
                                    } else {
                                        val cleanMonth = selectedMonth.replace(" ", "_")
                                        "AmarTaka_Monthly_Report_${cleanMonth}.csv"
                                    }
                                    com.example.ui.util.CsvExporter.exportTransactions(context, filteredList, fileName)
                                    Toast.makeText(
                                        context,
                                        if (appLanguage == AppLanguage.BN) "রিপোর্ট শেয়ার করা হচ্ছে!" else "Sharing Monthly CSV Report!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (appLanguage == AppLanguage.BN) "রপ্তানি করুন (CSV)" else "Export Transactions CSV",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // List Header
                        item {
                            Text(
                                text = "${Localizer.t("REPORT_HISTORY_TITLE", appLanguage)} (${filteredList.size})",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Filtered transactions list
                        if (filteredList.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = Localizer.t("EMPTY_TX", appLanguage),
                                        color = Color.Gray,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        } else {
                            items(filteredList) { tx ->
                                TransactionRowItem(tx, appLanguage, onDelete = {
                                    viewModel.deleteTransaction(tx)
                                    Toast.makeText(
                                        context,
                                        if (appLanguage == AppLanguage.BN) "লেনদেন ডিলিট করা হয়েছে!" else "Transaction deleted successfully!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionRowItem(tx: Transaction, appLanguage: AppLanguage, onDelete: () -> Unit) {
    val dateString = remember(tx.date, appLanguage) {
        val locale = if (appLanguage == AppLanguage.BN) Locale("bn", "BD") else Locale.US
        SimpleDateFormat("d MMM, yyyy", locale).format(Date(tx.date))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Icon circle indicator
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (tx.type == "INCOME") IncomeGreen.copy(alpha = 0.15f) else ExpenseRed.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (tx.type == "INCOME") Icons.Default.ThumbUp else Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = if (tx.type == "INCOME") IncomeGreen else ExpenseRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = Localizer.translateCategory(tx.category, appLanguage),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = Localizer.translateWallet(tx.wallet, appLanguage),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    if (tx.note.isNotBlank()) {
                        Text(
                            text = tx.note,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = dateString,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "${if (tx.type == "INCOME") "+" else "-"}৳${formatBDT(tx.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (tx.type == "INCOME") IncomeGreen else ExpenseRed
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// --- LOANS & DEBTS TAB ---
@Composable
fun LoanScreen(viewModel: FinanceViewModel) {
    val context = LocalContext.current
    val appLanguage by viewModel.appLanguage.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val loans by viewModel.loans.collectAsState()

    var showForm by remember { mutableStateOf(false) }
    var inputName by remember { mutableStateOf("") }
    var inputAmount by remember { mutableStateOf("") }
    var inputNote by remember { mutableStateOf("") }
    var loanType by remember { mutableStateOf("GAVE") } // GAVE (Receivable) vs TOOK (Payable)
    var dueDate by remember { mutableStateOf(System.currentTimeMillis() + 7 * 24 * 3600 * 1000L) } // 1 week default

    val totalReceivable = loans.filter { it.type == "GAVE" && !it.isPaid }.sumOf { it.amount }
    val totalPayable = loans.filter { it.type == "TOOK" && !it.isPaid }.sumOf { it.amount }

    val formattedDueDate = remember(dueDate, appLanguage) {
        val locale = if (appLanguage == AppLanguage.BN) Locale("bn", "BD") else Locale.US
        SimpleDateFormat("d MMMM, yyyy", locale).format(Date(dueDate))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Receivables and Payables header dashboard card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassBorder(themeMode, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (appLanguage == AppLanguage.BN) "পাবো (আমি দিয়েছি)" else "Receivables (Lent)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "৳ ${formatBDT(totalReceivable)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = IncomeGreen
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (appLanguage == AppLanguage.BN) "দেবো (আমি নিয়েছি)" else "Payables (Borrowed)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "৳ ${formatBDT(totalPayable)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = ExpenseRed
                        )
                    }
                }
            }
        }

        // Inline toggle form trigger
        item {
            Button(
                onClick = { showForm = !showForm },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(if (showForm) Icons.Default.Close else Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (showForm) {
                        if (appLanguage == AppLanguage.BN) "ফর্ম বন্ধ করুন" else "Close Input Form"
                    } else {
                        if (appLanguage == AppLanguage.BN) "নতুন দেনা-পাওনা যোগ করুন" else "Add New Loan & Debt"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Add Loan Form Segment
        if (showForm) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Loan Type GAVE/TOOK Selector Buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (loanType == "GAVE") IncomeGreen else Color.Transparent)
                                    .clickable { loanType = "GAVE" }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (appLanguage == AppLanguage.BN) "টাকা পাবো" else "Lent (Receivable)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (loanType == "GAVE") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (loanType == "TOOK") ExpenseRed else Color.Transparent)
                                    .clickable { loanType = "TOOK" }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (appLanguage == AppLanguage.BN) "টাকা দেবো" else "Borrowed (Payable)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (loanType == "TOOK") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Inputs
                        OutlinedTextField(
                            value = inputName,
                            onValueChange = { inputName = it },
                            label = { Text(if (appLanguage == AppLanguage.BN) "ব্যক্তির নাম" else "Person's Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = inputAmount,
                            onValueChange = { inputAmount = it },
                            label = { Text(if (appLanguage == AppLanguage.BN) "টাকার পরিমাণ (৳)" else "Amount (৳)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        // Due Date display indicator
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .clickable {
                                    // Normally launches picker, mock pick 30 days ahead here
                                    dueDate = System.currentTimeMillis() + 30 * 24 * 3600 * 1000L
                                    Toast.makeText(context, "Maturity date changed", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (appLanguage == AppLanguage.BN) "পরিশোধের শেষ তারিখ" else "Due Date",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                                Text(text = formattedDueDate, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = if (appLanguage == AppLanguage.BN) "পরিবর্তন" else "Pick Date",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedTextField(
                            value = inputNote,
                            onValueChange = { inputNote = it },
                            label = { Text(if (appLanguage == AppLanguage.BN) "নোট/বিবরণ" else "Note/Description") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        // Save Entry Button
                        Button(
                            onClick = {
                                val amt = inputAmount.toDoubleOrNull()
                                if (inputName.isBlank() || amt == null || amt <= 0.0) {
                                    Toast.makeText(
                                        context,
                                        if (appLanguage == AppLanguage.BN) "সঠিক নাম ও টাকার তথ্য দিন" else "Please enter details correctly",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }
                                viewModel.addLoan(
                                    personName = inputName,
                                    amount = amt,
                                    type = loanType,
                                    note = inputNote,
                                    dueDate = dueDate
                                )
                                inputName = ""
                                inputAmount = ""
                                inputNote = ""
                                showForm = false
                                Toast.makeText(
                                    context,
                                    if (appLanguage == AppLanguage.BN) "দেনা-পাওনা তালিকাভুক্ত হয়েছে!" else "Entry recorded successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (loanType == "GAVE") IncomeGreen else ExpenseRed
                            )
                        ) {
                            Text(if (appLanguage == AppLanguage.BN) "তালিকাভুক্ত করুন" else "Add Entry", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Active Loans list Header
        item {
            Text(
                text = if (appLanguage == AppLanguage.BN) "চলতি দেনা-পাওনা তালিকা (${loans.size}টি)" else "Active Loan/Debt List (${loans.size})",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (loans.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (appLanguage == AppLanguage.BN) "কোনো ঋণ বা দেনা-পাওনা নেই।" else "No active loans or debts found.",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(loans) { loan ->
                LoanRowItem(
                    loan = loan,
                    appLanguage = appLanguage,
                    onTogglePaid = { viewModel.toggleLoanPaid(loan) },
                    onDelete = {
                        viewModel.deleteLoan(loan)
                        Toast.makeText(
                            context,
                            if (appLanguage == AppLanguage.BN) "ইতিহাস মুছে ফেলা হয়েছে!" else "Entry deleted successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        }
    }
}

@Composable
fun LoanRowItem(loan: Loan, appLanguage: AppLanguage, onTogglePaid: () -> Unit, onDelete: () -> Unit) {
    val dateStr = remember(loan.dueDate, appLanguage) {
        val locale = if (appLanguage == AppLanguage.BN) Locale("bn", "BD") else Locale.US
        SimpleDateFormat("d MMM, yyyy", locale).format(Date(loan.dueDate))
    }
    val isOverdue = !loan.isPaid && loan.dueDate < System.currentTimeMillis()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (loan.isPaid) Color.Gray.copy(alpha = 0.2f)
                            else if (loan.type == "GAVE") IncomeGreen.copy(alpha = 0.15f)
                            else ExpenseRed.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (loan.isPaid) Icons.Default.Check else Icons.Default.AccountBox,
                        contentDescription = null,
                        tint = if (loan.isPaid) Color.Gray else if (loan.type == "GAVE") IncomeGreen else ExpenseRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = loan.personName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (loan.isPaid) Color.LightGray
                                    else if (loan.type == "GAVE") IncomeGreen.copy(alpha = 0.2f)
                                    else ExpenseRed.copy(alpha = 0.2f)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (loan.isPaid) {
                                    if (appLanguage == AppLanguage.BN) "পরিশোধিত" else "Paid"
                                } else {
                                    if (loan.type == "GAVE") {
                                        if (appLanguage == AppLanguage.BN) "পাব" else "Lent"
                                    } else {
                                        if (appLanguage == AppLanguage.BN) "দেব" else "Borrowed"
                                    }
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (loan.isPaid) Color.DarkGray else if (loan.type == "GAVE") IncomeGreen else ExpenseRed
                            )
                        }
                    }
                    if (loan.note.isNotBlank()) {
                        Text(
                            text = loan.note,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = (if (appLanguage == AppLanguage.BN) "পরিশোধের তারিখ: " else "Due Date: ") + dateStr +
                                if (isOverdue) (if (appLanguage == AppLanguage.BN) " (অতিক্রান্ত!)" else " (Overdue!)") else "",
                        fontSize = 10.sp,
                        fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal,
                        color = if (isOverdue) Color.Red else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "৳${formatBDT(loan.amount)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (loan.isPaid) Color.Gray else if (loan.type == "GAVE") IncomeGreen else ExpenseRed
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = onTogglePaid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (loan.isPaid) Color.LightGray else MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                            contentColor = if (loan.isPaid) Color.White else MaterialTheme.colorScheme.secondary
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (loan.isPaid) {
                                if (appLanguage == AppLanguage.BN) "বাকি আছে" else "Set Unpaid"
                            } else {
                                if (appLanguage == AppLanguage.BN) "পরিশোধ" else "Mark Paid"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// --- SETTINGS & GOALS TAB ---
@Composable
fun SettingsScreen(viewModel: FinanceViewModel) {
    val themeMode by viewModel.themeMode.collectAsState()
    val limit by viewModel.budgetLimit.collectAsState()
    val savingsGoals by viewModel.savingsGoals.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val context = LocalContext.current

    var limitValue by remember { mutableStateOf(if (limit > 0) limit.toInt().toString() else "") }
    var showGoalDialog by remember { mutableStateOf(false) }

    val isGlass = themeMode == ThemeMode.GLASSMORPHISM

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App language header
        item {
            Text(
                text = if (appLanguage == AppLanguage.BN) "ভাষা নির্বাচন (Language)" else "Language Selection",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Language config options
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassBorder(themeMode, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isGlass) Color(0x1F2E1A23) else MaterialTheme.colorScheme.surface
                ),
                border = if (isGlass) null else CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (appLanguage == AppLanguage.BN) "অঙ্কন ভাষা পরিবর্তন করুন:" else "Select Theme Language:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (appLanguage == AppLanguage.BN) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { viewModel.setAppLanguage(AppLanguage.BN) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "বাংলা (BN)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (appLanguage == AppLanguage.BN) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (appLanguage == AppLanguage.EN) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { viewModel.setAppLanguage(AppLanguage.EN) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "English (EN)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (appLanguage == AppLanguage.EN) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // App theme / mood configurations header
        item {
            Text(
                text = if (appLanguage == AppLanguage.BN) "অ্যাপের চেহারা" else "App Appearance",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Theme picker selection segment
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassBorder(themeMode, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isGlass) Color(0x1F2B4C5F) else MaterialTheme.colorScheme.surface
                ),
                border = if (isGlass) null else CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (appLanguage == AppLanguage.BN) "চেহারা মুড বেছে নিন (Appearance Mode):" else "Select Appearance Mode:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (themeMode == ThemeMode.DAY) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { viewModel.setThemeMode(ThemeMode.DAY) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (appLanguage == AppLanguage.BN) "দিন (Day)" else "Day Mode",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (themeMode == ThemeMode.DAY) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (themeMode == ThemeMode.NIGHT) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { viewModel.setThemeMode(ThemeMode.NIGHT) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (appLanguage == AppLanguage.BN) "রাত (Night)" else "Night Mode",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (themeMode == ThemeMode.NIGHT) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (themeMode == ThemeMode.EYE_CARE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { viewModel.setThemeMode(ThemeMode.EYE_CARE) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (appLanguage == AppLanguage.BN) "চোখের সুরক্ষা" else "Eye Care",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (themeMode == ThemeMode.EYE_CARE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (themeMode == ThemeMode.GLASSMORPHISM) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { viewModel.setThemeMode(ThemeMode.GLASSMORPHISM) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (appLanguage == AppLanguage.BN) "গ্লাস মুড ✨" else "Glass Mode ✨",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (themeMode == ThemeMode.GLASSMORPHISM) GlassOnPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Budget configuration limits
        item {
            Text(
                text = if (appLanguage == AppLanguage.BN) "বাজেট সীমা নির্ধারণ" else "Budget Configuration Limit",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Limits settings entry box
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassBorder(themeMode, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (appLanguage == AppLanguage.BN) "মাসিক বাজেট খরচ সীমা সেট করুন (এই খরচ ছাড়িয়ে গেলে সতর্ক বার্তা পাবেন):" else "Set Monthly warning target limit threshold (warning prompt):",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = limitValue,
                            onValueChange = { limitValue = it },
                            label = { Text("৳") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Button(
                            onClick = {
                                val l = limitValue.toDoubleOrNull() ?: 0.0
                                viewModel.setBudgetLimit(l)
                                val msg = if (appLanguage == AppLanguage.BN) "বাজেট আপডেট সফল হয়েছে" else "Budget limit updated successfully"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (appLanguage == AppLanguage.BN) "সেভ" else "Save")
                        }
                    }
                }
            }
        }

        // Saving Goal configuration section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (appLanguage == AppLanguage.BN) "সঞ্চয়ী লক্ষ্যসমূহ" else "Savings Goals",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Button(
                    onClick = { showGoalDialog = true },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (appLanguage == AppLanguage.BN) "নতুন লক্ষ্য" else "New Goal", fontSize = 12.sp)
                }
            }
        }

        if (savingsGoals.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (appLanguage == AppLanguage.BN) "নির্ধারিত কোনো সঞ্চয় লক্ষ্য নেই।" else "No savings goals set yet.",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(savingsGoals) { goal ->
                SavingsGoalRowItem(
                    goal = goal,
                    appLanguage = appLanguage,
                    themeMode = themeMode,
                    onDeposit = { amt ->
                        viewModel.updateSavingsAmount(goal, amt)
                    },
                    onDelete = {
                        viewModel.deleteSavingsGoal(goal)
                        val msg = if (appLanguage == AppLanguage.BN) "সঞ্চয় লক্ষ্য বন্ধ করা হয়েছে!" else "Savings goal deleted!"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    if (showGoalDialog) {
        AddSavingsGoalDialog(
            appLanguage = appLanguage,
            themeMode = themeMode,
            onDismiss = { showGoalDialog = false },
            onConfirm = { name, target, initial ->
                viewModel.addSavingsGoal(name, target, initial)
                showGoalDialog = false
                val msg = if (appLanguage == AppLanguage.BN) "সঞ্চয় লক্ষ্য যুক্ত করা হয়েছে!" else "Savings goal added successfully!"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun SavingsGoalRowItem(
    goal: SavingsGoal,
    appLanguage: AppLanguage,
    themeMode: ThemeMode,
    onDeposit: (Double) -> Unit,
    onDelete: () -> Unit
) {
    var showDepositDialog by remember { mutableStateOf(false) }
    val progress = if (goal.currentAmount <= 0) 0f else (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = IncomeGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = goal.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val collText = if (appLanguage == AppLanguage.BN) "সংগৃহীত: ৳${formatBDT(goal.currentAmount)}" else "Collected: ৳${formatBDT(goal.currentAmount)}"
                val tarText = if (appLanguage == AppLanguage.BN) "লক্ষ্য: ৳${formatBDT(goal.targetAmount)}" else "Target: ৳${formatBDT(goal.targetAmount)}"
                Text(text = collText, fontSize = 12.sp, color = Color.Gray)
                Text(text = tarText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = IncomeGreen,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Button(
                    onClick = { showDepositDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text(if (appLanguage == AppLanguage.BN) "টাকা যোগ করুন" else "Deposit Taka", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showDepositDialog) {
        DepositTakaDialog(
            goalTitle = goal.title,
            appLanguage = appLanguage,
            themeMode = themeMode,
            onDismiss = { showDepositDialog = false },
            onSave = { amt ->
                onDeposit(amt)
                showDepositDialog = false
            }
        )
    }
}

@Composable
fun AddSavingsGoalDialog(
    appLanguage: AppLanguage,
    themeMode: ThemeMode,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var initial by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (themeMode == ThemeMode.GLASSMORPHISM) Color(0xFF1F1B3D) else MaterialTheme.colorScheme.surface,
        title = { Text(if (appLanguage == AppLanguage.BN) "নতুন সঞ্চয় লক্ষ্য যোগ করুন" else "Add New Savings Goal", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (appLanguage == AppLanguage.BN) "লক্ষ্যের নাম (যেমন: বাড়ি কেনা)" else "Goal Name (e.g., Buy House)") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it },
                    label = { Text(if (appLanguage == AppLanguage.BN) "লক্ষ্য টাকার পরিমাণ (৳)" else "Target Amount (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = initial,
                    onValueChange = { initial = it },
                    label = { Text(if (appLanguage == AppLanguage.BN) "প্রাথমিক জমা (৳)" else "Initial Deposit (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val tAmt = target.toDoubleOrNull() ?: 0.0
                    val iAmt = initial.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && tAmt > 0) {
                        onConfirm(name, tAmt, iAmt)
                    }
                }
            ) {
                Text(if (appLanguage == AppLanguage.BN) "যুক্ত করুন" else "Add Goal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (appLanguage == AppLanguage.BN) "বাতিল" else "Cancel")
            }
        }
    )
}

@Composable
fun DepositTakaDialog(
    goalTitle: String,
    appLanguage: AppLanguage,
    themeMode: ThemeMode,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var amt by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (themeMode == ThemeMode.GLASSMORPHISM) Color(0xFF1F1B3D) else MaterialTheme.colorScheme.surface,
        title = { Text(if (appLanguage == AppLanguage.BN) "সঞ্চয় যুক্ত করুন - $goalTitle" else "Deposit Savings - $goalTitle", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = amt,
                onValueChange = { amt = it },
                label = { Text(if (appLanguage == AppLanguage.BN) "কত টাকা জমা করবেন? (৳)" else "How much to deposit? (৳)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val value = amt.toDoubleOrNull() ?: 0.0
                    if (value > 0) {
                        onSave(value)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen)
            ) {
                Text(if (appLanguage == AppLanguage.BN) "জমা দিন" else "Deposit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (appLanguage == AppLanguage.BN) "বাতিল" else "Cancel")
            }
        }
    )
}
