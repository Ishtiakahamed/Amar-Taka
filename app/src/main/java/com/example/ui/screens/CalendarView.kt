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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.data.repository.AppLanguage
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarView(viewModel: FinanceViewModel) {
    val context = LocalContext.current
    val appLanguage by viewModel.appLanguage.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val budgetLimit by viewModel.budgetLimit.collectAsState()

    var currentYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var currentMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) } // 0-based

    var selectedDay by remember { mutableStateOf<Int?>(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) }

    // Constants for weeks
    val weekDaysEn = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
    val weekDaysBn = listOf("রবি", "সোম", "মঙ্গল", "বুধ", "বৃহ:", "শুক্র", "শনি")
    val weekDays = if (appLanguage == AppLanguage.BN) weekDaysBn else weekDaysEn

    val monthsBn = listOf("জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর")
    val monthsEn = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")

    // Setup Calendar helper
    val calendarHelper = remember(currentYear, currentMonth) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val firstDayOfWeek = calendarHelper.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday...
    val maxDays = calendarHelper.getActualMaximum(Calendar.DAY_OF_MONTH)

    // Filter transactions to the current selected Month/Year
    val monthTransactions = remember(transactions, currentYear, currentMonth) {
        transactions.filter { tx ->
            val cal = Calendar.getInstance().apply { timeInMillis = tx.date }
            cal.get(Calendar.YEAR) == currentYear && cal.get(Calendar.MONTH) == currentMonth
        }
    }

    // Daily budget calculation = overall budgetLimit / 30 days
    val dailyBudgetLimit = if (budgetLimit > 0.0) budgetLimit / 30.0 else 0.0

    // Map of day to list of transactions
    val dayTransactionsMap = remember(monthTransactions) {
        monthTransactions.groupBy { tx ->
            val cal = Calendar.getInstance().apply { timeInMillis = tx.date }
            cal.get(Calendar.DAY_OF_MONTH)
        }
    }

    // Selected day transactions to view details underneath
    val activeTransactions = remember(selectedDay, dayTransactionsMap) {
        if (selectedDay == null) emptyList() else dayTransactionsMap[selectedDay!!] ?: emptyList()
    }

    fun switchPreviousMonth() {
        if (currentMonth == 0) {
            currentMonth = 11
            currentYear--
        } else {
            currentMonth--
        }
        selectedDay = 1
    }

    fun switchNextMonth() {
        if (currentMonth == 11) {
            currentMonth = 0
            currentYear++
        } else {
            currentMonth++
        }
        selectedDay = 1
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Month navigation header bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { switchPreviousMonth() }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Prev Month")
                    }
                    Text(
                        text = "${if (appLanguage == AppLanguage.BN) monthsBn[currentMonth] else monthsEn[currentMonth]} $currentYear",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { switchNextMonth() }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Month")
                    }
                }
            }
        }

        // Color coding legends
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendIndicator(
                    color = IncomeGreen,
                    text = if (appLanguage == AppLanguage.BN) "বাজেটের মধ্যে ✅" else "Within Budget"
                )
                LegendIndicator(
                    color = ExpenseRed,
                    text = if (appLanguage == AppLanguage.BN) "বাজেট পার 😅" else "Over Budget"
                )
                LegendIndicator(
                    color = Color.LightGray,
                    text = if (appLanguage == AppLanguage.BN) "হিসাব নেই" else "No Entry"
                )
            }
        }

        // Calendar Grid System Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Days names header
                    Row(modifier = Modifier.fillMaxWidth()) {
                        weekDays.forEach { dayName ->
                            Text(
                                text = dayName,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Render Days in rows of 7 items
                    val totalSlots = 35 + firstDayOfWeek
                    val daySlots = (1..totalSlots).toList()
                    val rowsCount = (daySlots.size + 6) / 7

                    for (rowIdx in 0 until rowsCount) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (colIdx in 0..6) {
                                val slotIndex = rowIdx * 7 + colIdx
                                val dayNum = slotIndex - (firstDayOfWeek - 2)

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (dayNum in 1..maxDays) {
                                        val isCurrentlySelected = selectedDay == dayNum
                                        val dayTxs = dayTransactionsMap[daynumToKey(dayNum)] ?: emptyList()

                                        // Compute visual coloring
                                        val (bgColor, textColor) = if (dayTxs.isEmpty()) {
                                            Pair(Color.Transparent, MaterialTheme.colorScheme.onSurface)
                                        } else {
                                            val dayExpenses = dayTxs.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                                            val overBudget = dailyBudgetLimit > 0.0 && dayExpenses > dailyBudgetLimit
                                            if (overBudget) {
                                                Pair(ExpenseRed.copy(alpha = 0.25f), ExpenseRed)
                                            } else {
                                                Pair(IncomeGreen.copy(alpha = 0.25f), IncomeGreen)
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                                .background(if (isCurrentlySelected) MaterialTheme.colorScheme.primary else bgColor)
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isCurrentlySelected) Color.Transparent else (if (dayTxs.isEmpty()) Color.Transparent else textColor.copy(alpha = 0.5f)),
                                                    shape = CircleShape
                                                )
                                                .clickable { selectedDay = dayNum }
                                                .padding(4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (appLanguage == AppLanguage.BN) getBanglaNumber(dayNum) else dayNum.toString(),
                                                fontSize = 12.sp,
                                                fontWeight = if (isCurrentlySelected || dayTxs.isNotEmpty()) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isCurrentlySelected) Color.White else (if (dayTxs.isNotEmpty()) textColor else MaterialTheme.colorScheme.onSurface)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Header for transactions underneath selected date
        item {
            if (selectedDay != null) {
                Text(
                    text = if (appLanguage == AppLanguage.BN) {
                        "${getBanglaNumber(selectedDay!!)} ${monthsBn[currentMonth]}, $currentYear সালের লেনদেন (${activeTransactions.size})"
                    } else {
                        "Transactions of ${monthsEn[currentMonth]} $selectedDay, $currentYear (${activeTransactions.size})"
                    },
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Listed day transactions
        if (activeTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (appLanguage == AppLanguage.BN) "এই দিনে কোনো ট্রানজেকশন নেই" else "No transactions logged on this day.",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(activeTransactions) { tx ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                                text = translateCategoryText(tx.category, appLanguage),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = translateWalletText(tx.wallet, appLanguage),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                if (tx.note.isNotBlank()) {
                                    Text(
                                        text = tx.note,
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${if (tx.type == "INCOME") "+" else "-"}৳${formatBDTFloat(tx.amount)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (tx.type == "INCOME") IncomeGreen else ExpenseRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LegendIndicator(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(text = text, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

// Convert day index to secure lookup key
private fun daynumToKey(dayNum: Int): Int {
    return dayNum
}

private fun getBanglaNumber(num: Int): String {
    val bnDigits = listOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    return num.toString().map { char ->
        if (char.isDigit()) bnDigits[char - '0'] else char
    }.joinToString("")
}

private fun translateCategoryText(cat: String, lang: AppLanguage): String {
    if (lang == AppLanguage.BN) return cat
    return when (cat) {
        "খাবার" -> "Food"
        "পরিবহন" -> "Transport"
        "উপহার" -> "Gift"
        "বেতন" -> "Salary"
        "বিনোদন" -> "Entertainment"
        "অন্যান্য" -> "Others"
        "ভাড়া" -> "Rent"
        "বাজার/শপিং" -> "Shopping"
        "चिकित्सा" -> "Medical"
        "চিকিৎসা" -> "Medical"
        "বিল" -> "Bill"
        "ব্যবসা" -> "Business"
        "ফ্রিল্যান্সিং" -> "Freelancing"
        else -> cat
    }
}

private fun translateWalletText(wallet: String, lang: AppLanguage): String {
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

private fun formatBDTFloat(value: Double): String {
    return String.format(Locale.US, "%,d", value.toInt())
}
