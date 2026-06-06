package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.Transaction
import com.example.data.model.Loan
import com.example.data.model.SavingsGoal
import com.example.data.repository.AppLanguage
import com.example.data.repository.FinanceRepository
import com.example.data.repository.SettingsRepository
import com.example.data.repository.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)
    private val financeRepository: FinanceRepository

    val transactions: StateFlow<List<Transaction>>
    val loans: StateFlow<List<Loan>>
    val savingsGoals: StateFlow<List<SavingsGoal>>

    private val _themeMode = MutableStateFlow(settingsRepository.getThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode

    private val _appLanguage = MutableStateFlow(settingsRepository.getAppLanguage())
    val appLanguage: StateFlow<AppLanguage> = _appLanguage

    private val _budgetLimit = MutableStateFlow(settingsRepository.getBudgetLimit())
    val budgetLimit: StateFlow<Double> = _budgetLimit

    init {
        val database = AppDatabase.getDatabase(application)
        financeRepository = FinanceRepository(database.financeDao())

        transactions = financeRepository.allTransactions.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        loans = financeRepository.allLoans.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        savingsGoals = financeRepository.allSavingsGoals.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
            _themeMode.value = mode
        }
    }

    fun setAppLanguage(lang: AppLanguage) {
        viewModelScope.launch {
            settingsRepository.setAppLanguage(lang)
            _appLanguage.value = lang
        }
    }

    fun setBudgetLimit(limit: Double) {
        viewModelScope.launch {
            settingsRepository.setBudgetLimit(limit)
            _budgetLimit.value = limit
        }
    }

    // Transactions
    fun addTransaction(amount: Double, type: String, category: String, wallet: String, note: String, date: Long) {
        viewModelScope.launch {
            financeRepository.insertTransaction(
                Transaction(amount = amount, type = type, category = category, wallet = wallet, note = note, date = date)
            )
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            financeRepository.deleteTransaction(transaction)
        }
    }

    // Loans
    fun addLoan(personName: String, amount: Double, type: String, note: String, dueDate: Long) {
        viewModelScope.launch {
            financeRepository.insertLoan(
                Loan(personName = personName, amount = amount, type = type, note = note, dueDate = dueDate)
            )
        }
    }

    fun toggleLoanPaid(loan: Loan) {
        viewModelScope.launch {
            financeRepository.updateLoan(loan.copy(isPaid = !loan.isPaid))
        }
    }

    fun deleteLoan(loan: Loan) {
        viewModelScope.launch {
            financeRepository.deleteLoan(loan)
        }
    }

    // Savings Goals
    fun addSavingsGoal(title: String, targetAmount: Double, initialAmount: Double) {
        viewModelScope.launch {
            financeRepository.insertSavingsGoal(
                SavingsGoal(title = title, targetAmount = targetAmount, currentAmount = initialAmount, initialAmount = initialAmount)
            )
        }
    }

    fun updateSavingsAmount(goal: SavingsGoal, amount: Double) {
        viewModelScope.launch {
            financeRepository.updateSavingsGoal(goal.copy(currentAmount = goal.currentAmount + amount))
        }
    }

    fun deleteSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            financeRepository.deleteSavingsGoal(goal)
        }
    }

    // Helper functions
    fun getMonthExpensesSum(): Double {
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        
        return transactions.value.filter { tx ->
            if (tx.type != "EXPENSE") return@filter false
            val cal = Calendar.getInstance().apply { timeInMillis = tx.date }
            cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
        }.sumOf { it.amount }
    }
}
