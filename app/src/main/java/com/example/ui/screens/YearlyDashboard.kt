package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
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
import com.example.ui.util.CsvExporter
import com.example.ui.util.Localizer
import com.example.ui.viewmodel.FinanceViewModel
import java.util.Calendar

@Composable
fun YearlyDashboard(viewModel: FinanceViewModel) {
    val context = LocalContext.current
    val appLanguage by viewModel.appLanguage.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }

    // Aggregate transactions for the selected year
    val yearTransactions = remember(transactions, selectedYear) {
        transactions.filter { tx ->
            val cal = Calendar.getInstance().apply { timeInMillis = tx.date }
            cal.get(Calendar.YEAR) == selectedYear
        }
    }

    val totalIncome = remember(yearTransactions) {
        yearTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    }
    val totalExpense = remember(yearTransactions) {
        yearTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    }
    val totalSavings = totalIncome - totalExpense

    // Month-by-month calculation (0..11)
    val monthlyData = remember(yearTransactions) {
        (0..11).map { monthIndex ->
            val monthTx = yearTransactions.filter { tx ->
                val cal = Calendar.getInstance().apply { timeInMillis = tx.date }
                cal.get(Calendar.MONTH) == monthIndex
            }
            val inc = monthTx.filter { it.type == "INCOME" }.sumOf { it.amount }
            val exp = monthTx.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            Pair(inc, exp)
        }
    }

    // Best saving month (highest inc - exp)
    val bestSavingMonthIndex = remember(monthlyData) {
        var bestIdx = -1
        var maxSavings = 0.0
        monthlyData.forEachIndexed { idx, pair ->
            val savings = pair.first - pair.second
            if (savings > maxSavings) {
                maxSavings = savings
                bestIdx = idx
            }
        }
        bestIdx
    }

    // Highest expense month
    val highestExpenseMonthIndex = remember(monthlyData) {
        var highestIdx = -1
        var maxExpense = 0.0
        monthlyData.forEachIndexed { idx, pair ->
            if (pair.second > maxExpense) {
                maxExpense = pair.second
                highestIdx = idx
            }
        }
        highestIdx
    }

    // Top yearly spending category
    val topSpendingCategory = remember(yearTransactions) {
        yearTransactions
            .filter { it.type == "EXPENSE" }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .maxByOrNull { it.value }
    }

    val monthsBn = listOf("জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর")
    val monthsEn = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")

    fun getMonthName(idx: Int): String {
        return if (appLanguage == AppLanguage.BN) monthsBn[idx] else monthsEn[idx]
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Year Controller header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedYear-- }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Prev Year")
                    }
                    Text(
                        text = if (appLanguage == AppLanguage.BN) "বছর: $selectedYear" else "Year: $selectedYear",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = { selectedYear++ }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Year")
                    }
                }
            }
        }

        // Metrics Dashboard
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (appLanguage == AppLanguage.BN) "$selectedYear সালের বার্ষিক বিবরণী" else "$selectedYear Annual Summary",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (appLanguage == AppLanguage.BN) "মোট আয়" else "Total Income",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Text("৳${formatBDT(totalIncome)}", fontWeight = FontWeight.Bold, color = IncomeGreen, fontSize = 16.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (appLanguage == AppLanguage.BN) "মোট ব্যয়" else "Total Expense",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Text("৳${formatBDT(totalExpense)}", fontWeight = FontWeight.Bold, color = ExpenseRed, fontSize = 16.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (appLanguage == AppLanguage.BN) "মোট সঞ্চয়" else "Total Savings",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "৳${formatBDT(totalSavings)}",
                                fontWeight = FontWeight.Bold,
                                color = if (totalSavings >= 0) IncomeGreen else ExpenseRed,
                                fontSize = 16.sp
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (appLanguage == AppLanguage.BN) "চলতি ব্যালেন্স" else "Current Balance",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            val balance = totalIncome - totalExpense
                            Text(
                                text = "৳${formatBDT(balance)}",
                                fontWeight = FontWeight.Bold,
                                color = if (balance >= 0) IncomeGreen else ExpenseRed,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Export Yearly Report Card
        item {
            Button(
                onClick = {
                    val fileName = "AmarTaka_Yearly_Report_$selectedYear.csv"
                    CsvExporter.exportTransactions(context, yearTransactions, fileName)
                    val toastMsg = if (appLanguage == AppLanguage.BN) "বার্ষিক রিপোর্ট তৈরি হয়েছে!" else "Yearly CSV Report Ready!"
                    Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (appLanguage == AppLanguage.BN) "$selectedYear সালের এক্সেল রপ্তানি (CSV)" else "Export $selectedYear Excel Report (CSV)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )
            }
        }

        // Highlights / Insights Cards
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (appLanguage == AppLanguage.BN) "বার্ষিক বিশেষ হাইলাইটস" else "Annual Financial Insights",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Best saving Month
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (appLanguage == AppLanguage.BN) "সেরা সঞ্চয়ী মাস:" else "Best Saving Month:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        if (bestSavingMonthIndex != -1) {
                            val savingsVal = monthlyData[bestSavingMonthIndex].first - monthlyData[bestSavingMonthIndex].second
                            Text(
                                text = "${getMonthName(bestSavingMonthIndex)} (৳${formatBDT(savingsVal)})",
                                fontWeight = FontWeight.Bold,
                                color = IncomeGreen,
                                fontSize = 12.sp
                            )
                        } else {
                            Text(
                                text = if (appLanguage == AppLanguage.BN) "কোনোটিই নয়" else "None",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    // Highest Expense Month
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (appLanguage == AppLanguage.BN) "সর্বোচ্চ ব্যয়ের মাস:" else "Highest Expense Month:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        if (highestExpenseMonthIndex != -1) {
                            val expenseVal = monthlyData[highestExpenseMonthIndex].second
                            Text(
                                text = "${getMonthName(highestExpenseMonthIndex)} (৳${formatBDT(expenseVal)})",
                                fontWeight = FontWeight.Bold,
                                color = ExpenseRed,
                                fontSize = 12.sp
                            )
                        } else {
                            Text(text = "-", fontSize = 12.sp, color = Color.Gray)
                        }
                    }

                    // Top spending Category
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (appLanguage == AppLanguage.BN) "সর্বোচ্চ ব্যয়ের খাত:" else "Top Spending Category:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        if (topSpendingCategory != null) {
                            Text(
                                text = "${Localizer.translateCategory(topSpendingCategory.key, appLanguage)} (৳${formatBDT(topSpendingCategory.value)})",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 12.sp
                            )
                        } else {
                            Text(text = "-", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }

        // Monthly Grid Label
        item {
            Text(
                text = if (appLanguage == AppLanguage.BN) "মাস ভিত্তিক হিসাব খতিয়ান" else "Month-by-Month Balance Ledger",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // January to December Summary Row Cards
        items(12) { monthIndex ->
            val data = monthlyData[monthIndex]
            val hasActivity = data.first > 0 || data.second > 0
            val savings = data.first - data.second

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (hasActivity) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.2f)) {
                        Text(
                            text = getMonthName(monthIndex),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (hasActivity) MaterialTheme.colorScheme.onSurface else Color.Gray
                        )
                        if (hasActivity) {
                            Text(
                                text = if (appLanguage == AppLanguage.BN) {
                                    "সঞ্চয়: ৳${formatBDT(savings)}"
                                } else {
                                    "Saved: ৳${formatBDT(savings)}"
                                },
                                fontSize = 11.sp,
                                color = if (savings >= 0) IncomeGreen else ExpenseRed
                            )
                        } else {
                            Text(
                                text = if (appLanguage == AppLanguage.BN) "কোনো লেনদেন নেই" else "No transactions logged",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    if (hasActivity) {
                        Column(
                            modifier = Modifier.weight(1.8f),
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(IncomeGreen, RoundedCornerShape(3.dp))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "৳${formatBDT(data.first)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = IncomeGreen
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(ExpenseRed, RoundedCornerShape(3.dp))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "৳${formatBDT(data.second)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ExpenseRed
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
