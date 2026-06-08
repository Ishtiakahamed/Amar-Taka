package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.data.repository.AppLanguage
import com.example.data.repository.ThemeMode
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.util.Localizer
import com.example.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HisabTab(viewModel: FinanceViewModel) {
    val context = LocalContext.current
    val appLanguage by viewModel.appLanguage.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    val isBn = appLanguage == AppLanguage.BN

    var searchText by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("ALL") } // ALL, INCOME, EXPENSE, SAVINGS, TRANSFER, LOAN
    var selectedWalletFilter by remember { mutableStateOf("ALL") } // ALL, ক্যাশ, বিকাশ, নগদ, রকেট, ব্যাংক, সঞ্চয়
    var selectedCategoryFilter by remember { mutableStateOf("ALL") } // ALL or actual category
    var selectedMonthFilter by remember { mutableStateOf("ALL") } // ALL, "2026-06", etc.
    
    var sortBy by remember { mutableStateOf("NEWEST") } // NEWEST, OLDEST, HIGHEST_AMOUNT

    var transactionToEdit by remember { mutableStateOf<Transaction?>(null) }

    // Derive distinct month list from transaction timestamps
    val monthList = remember(transactions) {
        val formats = SimpleDateFormat("yyyy-MM", Locale.US)
        transactions.map { formats.format(Date(it.date)) }.distinct().sortedDescending()
    }

    // Filtered transaction list derivation
    val filteredTransactions = remember(
        transactions, searchText, selectedTypeFilter, selectedWalletFilter,
        selectedCategoryFilter, selectedMonthFilter, sortBy
    ) {
        var list = transactions.asSequence()

        // 1. Search text notes or categories
        if (searchText.isNotBlank()) {
            list = list.filter {
                it.note.contains(searchText, ignoreCase = true) ||
                it.category.contains(searchText, ignoreCase = true)
            }
        }

        // 2. Filter by Type
        if (selectedTypeFilter != "ALL") {
            list = list.filter { it.type == selectedTypeFilter }
        }

        // 3. Filter by Wallet
        if (selectedWalletFilter != "ALL") {
            list = list.filter { it.wallet == selectedWalletFilter }
        }

        // 4. Filter by Category
        if (selectedCategoryFilter != "ALL") {
            list = list.filter { it.category == selectedCategoryFilter }
        }

        // 5. Filter by Month
        if (selectedMonthFilter != "ALL") {
            val formats = SimpleDateFormat("yyyy-MM", Locale.US)
            list = list.filter { formats.format(Date(it.date)) == selectedMonthFilter }
        }

        val resultList = list.toList()

        // 6. Sorting
        when (sortBy) {
            "OLDEST" -> resultList.sortedBy { it.date }
            "HIGHEST_AMOUNT" -> resultList.sortedByDescending { it.amount }
            else -> resultList.sortedByDescending { it.date }
        }
    }

    // Summary calculations on current filtered list
    val totalIncome = remember(filteredTransactions) {
        filteredTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    }
    val totalExpense = remember(filteredTransactions) {
        filteredTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    }
    val netBalance = totalIncome - totalExpense

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // --- SECTION A: SEARCH & TOTAL PILLS ---
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = { Text(if (isBn) "নোট অথবা খাতের নাম খুঁজুন..." else "Search notes or categories...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = if (searchText.isNotEmpty()) {
                {
                    IconButton(onClick = { searchText = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                    }
                }
            } else null
        )

        // Aggregated mini health cards for filtered transactions list
        val green = Color(0xFF10B981)
        val red = Color(0xFFEF4444)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(22.dp)),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0x25241C42))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(text = if (isBn) "মোট আয়" else "Total Income", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "৳ ${formatBDTWithLanguage(totalIncome, appLanguage)}", fontWeight = FontWeight.Bold, color = green, fontSize = 14.sp)
                }
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.08f)))
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(text = if (isBn) "মোট ব্যয়" else "Total Expense", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "৳ ${formatBDTWithLanguage(totalExpense, appLanguage)}", fontWeight = FontWeight.Bold, color = red, fontSize = 14.sp)
                }
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.08f)))
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(text = if (isBn) "অবশিষ্ট" else "Net", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "৳ ${formatBDTWithLanguage(netBalance, appLanguage)}", fontWeight = FontWeight.Bold, color = if (netBalance >= 0) green else red, fontSize = 14.sp)
                }
            }
        }

        // --- SECTION B: DROPDOWN CRITERIA FILTERS ---
        Text(text = if (isBn) "ফিল্টারিং ও সাজানো" else "Filters & Sorting", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 1. FILTER TYPE
            Box(modifier = Modifier.weight(1f)) {
                var expanded by remember { mutableStateOf(false) }
                Button(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Text(
                        text = when (selectedTypeFilter) {
                            "ALL" -> if (isBn) "সব টাইপ" else "All Type"
                            "INCOME" -> if (isBn) "আয়" else "Income"
                            "EXPENSE" -> if (isBn) "ব্যয়" else "Expense"
                            "SAVINGS" -> if (isBn) "সঞ্চয়" else "Savings"
                            "TRANSFER" -> if (isBn) "বদলি" else "Xfer"
                            else -> selectedTypeFilter
                        },
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    listOf("ALL", "EXPENSE", "INCOME", "SAVINGS", "TRANSFER", "LOAN").forEach { t ->
                        DropdownMenuItem(
                            text = { Text(t) },
                            onClick = { selectedTypeFilter = t; expanded = false }
                        )
                    }
                }
            }

            // 2. FILTER WALLET
            Box(modifier = Modifier.weight(1f)) {
                var expanded by remember { mutableStateOf(false) }
                Button(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Text(
                        text = if (selectedWalletFilter == "ALL") (if (isBn) "সব ওয়ালেট" else "All Account") else Localizer.translateWallet(selectedWalletFilter, appLanguage),
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    listOf("ALL", "ক্যাশ", "বিকাশ", "নগদ", "রকেট", "ব্যাংক", "সঞ্চয়").forEach { w ->
                        DropdownMenuItem(
                            text = { Text(Localizer.translateWallet(w, appLanguage)) },
                            onClick = { selectedWalletFilter = w; expanded = false }
                        )
                    }
                }
            }

            // 3. FILTER MONTH
            Box(modifier = Modifier.weight(1f)) {
                var expanded by remember { mutableStateOf(false) }
                Button(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Text(
                        text = if (selectedMonthFilter == "ALL") (if (isBn) "সব মাস" else "All Month") else selectedMonthFilter,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(text = { Text("ALL") }, onClick = { selectedMonthFilter = "ALL"; expanded = false })
                    monthList.forEach { m ->
                        DropdownMenuItem(text = { Text(m) }, onClick = { selectedMonthFilter = m; expanded = false })
                    }
                }
            }

            // 4. SORT DECISION
            Box(modifier = Modifier.weight(1f)) {
                var expanded by remember { mutableStateOf(false) }
                Button(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Text(
                        text = when (sortBy) {
                            "NEWEST" -> if (isBn) "রিসেন্ট" else "Newest"
                            "OLDEST" -> if (isBn) "পুরোনো" else "Oldest"
                            else -> if (isBn) "সর্বোচ্চ" else "Amount"
                        },
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    listOf("NEWEST", "OLDEST", "HIGHEST_AMOUNT").forEach { key ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    when (key) {
                                        "NEWEST" -> if (isBn) "নতুন প্রথম" else "Newest First"
                                        "OLDEST" -> if (isBn) "পুরোনো প্রথম" else "Oldest First"
                                        else -> if (isBn) "সর্বোচ্চ পরিমাণ" else "Highest Amount"
                                    }
                                )
                            },
                            onClick = { sortBy = key; expanded = false }
                        )
                    }
                }
            }
        }

        // Clear filter buttons row if filters are applied
        if (selectedTypeFilter != "ALL" || selectedWalletFilter != "ALL" || selectedMonthFilter != "ALL" || searchText.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(
                    onClick = {
                        selectedTypeFilter = "ALL"
                        selectedWalletFilter = "ALL"
                        selectedMonthFilter = "ALL"
                        searchText = ""
                    }
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBn) "ফিল্টার রিসেট করুন" else "Reset Filters", fontSize = 11.sp)
                }
            }
        }

        // --- SECTION C: TRANSACTIONS LIST FEED ---
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (filteredTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp), contentAlignment = Alignment.Center
                    ) {
                        Text(text = if (isBn) "কোনো হিসাব রেকর্ড পাওয়া যায়নি" else "No matching transactions found.", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            } else {
                items(filteredTransactions, key = { it.id }) { tx ->
                    LocalHisabRowItem(
                        tx = tx,
                        appLanguage = appLanguage,
                        onEdit = { transactionToEdit = tx },
                        onDuplicate = {
                            // Copy transaction content and save as new with standard timestamp
                            viewModel.addTransaction(
                                amount = tx.amount,
                                type = tx.type,
                                category = tx.category,
                                wallet = tx.wallet,
                                note = tx.note + (if (isBn) " (কপি)" else " (Duplicate)"),
                                date = System.currentTimeMillis()
                            )
                            Toast.makeText(context, if (isBn) "হিসাব সফলভাবে অনুকরণ করা হয়েছে" else "Transaction duplicated successfully", Toast.LENGTH_SHORT).show()
                        },
                        onDelete = {
                            viewModel.deleteTransaction(tx)
                            Toast.makeText(context, if (isBn) "হিসাব বাতিল করা হয়েছে" else "Record Deleted!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    if (transactionToEdit != null) {
        EditTransactionDialog(
            transaction = transactionToEdit!!,
            appLanguage = appLanguage,
            themeMode = themeMode,
            onDismiss = { transactionToEdit = null },
            onSave = { updated ->
                viewModel.updateTransaction(updated)
                transactionToEdit = null
                Toast.makeText(context, if (isBn) "সফলভাবে আপডেট করা হয়েছে!" else "Updated Successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun LocalHisabRowItem(
    tx: Transaction,
    appLanguage: AppLanguage,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(tx.date, appLanguage) {
        val formats = SimpleDateFormat("d MMM, yyyy (hh:mm a)", if (appLanguage == AppLanguage.BN) Locale("bn", "BD") else Locale.US)
        formats.format(Date(tx.date))
    }

    val incColor = Color(0xFF10B981)
    val expColor = Color(0xFFEF4444)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x1F241C42))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left hand description info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (tx.type == "INCOME") incColor.copy(alpha = 0.15f) else expColor.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val iconVector = if (tx.type == "TRANSFER") Icons.Default.Refresh else Localizer.getCategoryIcon(tx.category)
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = if (tx.type == "INCOME") incColor else expColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Clear Type badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        when (tx.type) {
                                            "INCOME" -> incColor.copy(alpha = 0.15f)
                                            "EXPENSE" -> expColor.copy(alpha = 0.15f)
                                            "SAVINGS" -> Color(0xFF38BDF8).copy(alpha = 0.15f)
                                            else -> Color(0xFFC084FC).copy(alpha = 0.15f)
                                        }
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = when (tx.type) {
                                        "INCOME" -> if (appLanguage == AppLanguage.BN) "আয়" else "Income"
                                        "EXPENSE" -> if (appLanguage == AppLanguage.BN) "ব্যয়" else "Expense"
                                        "SAVINGS" -> if (appLanguage == AppLanguage.BN) "সঞ্চয়" else "Savings"
                                        "LOAN" -> if (appLanguage == AppLanguage.BN) "ঋণ" else "Loan"
                                        else -> if (appLanguage == AppLanguage.BN) "বদলি" else "Transfer"
                                    },
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = when (tx.type) {
                                        "INCOME" -> incColor
                                        "EXPENSE" -> expColor
                                        "SAVINGS" -> Color(0xFF38BDF8)
                                        else -> Color(0xFFC084FC)
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = if (tx.type == "TRANSFER") {
                                    if (appLanguage == AppLanguage.BN) "স্থানান্তর" else "Transfer"
                                } else {
                                    Localizer.translateCategory(tx.category, appLanguage)
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White.copy(alpha = 0.1f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = Localizer.translateWallet(tx.wallet, appLanguage),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC084FC)
                                )
                            }
                        }
                        if (tx.note.isNotBlank()) {
                            Text(
                                text = tx.note,
                                fontSize = 11.sp,
                                color = Color.LightGray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(text = dateStr, fontSize = 9.sp, color = Color.Gray)
                    }
                }

                // Right hand side amount display
                Text(
                    text = "${if (tx.type == "INCOME") "+" else "-"}৳${formatBDTWithLanguage(tx.amount, appLanguage)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (tx.type == "INCOME") incColor else expColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.08f)))
            Spacer(modifier = Modifier.height(4.dp))

            // Action triggers row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Edit Trigger
                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                // 2. Duplicate Trigger
                IconButton(onClick = onDuplicate, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Duplicate", tint = IncomeGreen, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                // 3. Delete Trigger
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ExpenseRed, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun EditTransactionDialog(
    transaction: Transaction,
    appLanguage: AppLanguage,
    themeMode: ThemeMode,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit
) {
    var amount by remember { mutableStateOf(transaction.amount.toString()) }
    var type by remember { mutableStateOf(transaction.type) }
    var category by remember { mutableStateOf(transaction.category) }
    var wallet by remember { mutableStateOf(transaction.wallet) }
    var note by remember { mutableStateOf(transaction.note) }
    
    val wallets = listOf("ক্যাশ", "বিকাশ", "নগদ", "রকেট", "ব্যাংক", "সঞ্চয়")
    val incomeCategories = listOf("বেতন", "ব্যবসা", "ফ্রিল্যান্সিং", "উপহার", "অন্যান্য")
    val expenseCategories = listOf("খাবার", "বাজার", "শপিং", "জামাকাপড়", "যাতায়াত", "মোবাইল রিচার্জ", "ইন্টারনেট বিল", "বিদ্যুৎ বিল", "বাসা ভাড়া", "পড়াশোনা", "চিকিৎসা", "ওষুধ", "পরিবার", "বন্ধু", "বিনোদন", "ব্যক্তিগত যত্ন", "দান", "ঋণ পরিশোধ", "অন্যান্য")
    val savingsCategories = listOf("জরুরী ফান্ড", "ভবিষ্যৎ সঞ্চয়", "অন্যান্য")
    
    val isBn = appLanguage == AppLanguage.BN

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (themeMode == ThemeMode.GLASSMORPHISM) Color(0xFF1F1B3D) else MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = if (isBn) "ভুল সংশোধন ও সম্পাদনা" else "Edit Ledger Entry",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Type Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    val types = listOf("EXPENSE", "INCOME", "SAVINGS", "TRANSFER")
                    types.forEach { t ->
                        val isSel = type == t
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable {
                                    type = t
                                    category = when (t) {
                                        "EXPENSE" -> "অন্যান্য"
                                        "INCOME" -> "অন্যান্য"
                                        "SAVINGS" -> "ভবিষ্যৎ সঞ্চয়"
                                        else -> "বিকাশ"
                                    }
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (t) {
                                    "INCOME" -> if (isBn) "আয়" else "Inc"
                                    "EXPENSE" -> if (isBn) "ব্যয়" else "Exp"
                                    "SAVINGS" -> if (isBn) "সঞ্চয়" else "Sav"
                                    else -> if (isBn) "বদলি" else "Xfer"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Amount Input
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(if (isBn) "টাকার পরিমাণ" else "Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Description note
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(if (isBn) "নোট/বিবরণ লিখুন" else "Transaction Details") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Account Wallet Account Source Selector
                Text(if (isBn) "ওয়ালেট নির্বাচন করুন:" else "Select Wallet:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                FlowRowLayout(spacing = 6.dp) {
                    wallets.take(5).forEach { w ->
                        val isSel = wallet == w
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { wallet = w }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(Localizer.translateWallet(w, appLanguage), color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        }
                    }
                }
                
                // Account Categories selection list
                Text(if (isBn) "খরচ/আয়ের খাত:" else "Select Category:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                val cats = when (type) {
                    "INCOME" -> incomeCategories
                    "EXPENSE" -> expenseCategories
                    "SAVINGS" -> savingsCategories
                    else -> listOf("ক্যাশ", "বিকাশ", "নগদ", "রকেট", "ব্যাংক")
                }
                FlowRowLayout(spacing = 6.dp) {
                    cats.forEach { c ->
                        val isSel = category == c
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { category = c }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (type == "TRANSFER") Localizer.translateWallet(c, appLanguage) else Localizer.translateCategory(c, appLanguage),
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amtVal = amount.toDoubleOrNull()
                    if (amtVal != null && amtVal > 0) {
                        onSave(
                            transaction.copy(
                                amount = amtVal,
                                type = type,
                                category = category,
                                wallet = wallet,
                                note = note
                            )
                        )
                    }
                }
            ) {
                Text(if (isBn) "পরিবর্তন সংরক্ষণ করুন" else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBn) "বাতিল" else "Cancel")
            }
        }
    )
}
