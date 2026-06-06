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

    private val _profileName = MutableStateFlow(settingsRepository.getProfileName())
    val profileName: StateFlow<String> = _profileName

    private val _profileImageUri = MutableStateFlow(settingsRepository.getProfileImageUri())
    val profileImageUri: StateFlow<String> = _profileImageUri

    // --- StateFlows for challenges ---
    val joinedChallenges = MutableStateFlow<Map<String, Long>>(emptyMap())
    val completedChallenges = MutableStateFlow<Set<String>>(emptySet())
    val challengeProgress = MutableStateFlow<Map<String, Int>>(emptyMap())

    private val challengeList = listOf(
        "CHALLENGE_7_DAY_SAVING",
        "CHALLENGE_30_DAY_SAVING",
        "CHALLENGE_NO_FAST_FOOD",
        "CHALLENGE_NO_RICKSHAW",
        "CHALLENGE_SAVE_500",
        "CHALLENGE_SAVE_1000"
    )

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

        loadChallengesState()
    }

    private fun loadChallengesState() {
        val joins = mutableMapOf<String, Long>()
        val completes = mutableSetOf<String>()
        val prog = mutableMapOf<String, Int>()
        challengeList.forEach { id ->
            val date = settingsRepository.getChallengeJoinDate(id)
            if (date > 0) {
                joins[id] = date
            }
            if (settingsRepository.isChallengeCompleted(id)) {
                completes.add(id)
            }
            prog[id] = settingsRepository.getChallengeProgress(id)
        }
        joinedChallenges.value = joins
        completedChallenges.value = completes
        challengeProgress.value = prog
    }

    fun startChallenge(id: String) {
        viewModelScope.launch {
            settingsRepository.joinChallenge(id, System.currentTimeMillis())
            settingsRepository.setChallengeProgress(id, 0)
            settingsRepository.setChallengeCompleted(id, false)
            loadChallengesState()
        }
    }

    fun incrementChallengeProgress(id: String, maxProgress: Int) {
        viewModelScope.launch {
            val current = settingsRepository.getChallengeProgress(id)
            val next = (current + 1).coerceAtMost(maxProgress)
            settingsRepository.setChallengeProgress(id, next)
            if (next >= maxProgress) {
                settingsRepository.setChallengeCompleted(id, true)
            }
            loadChallengesState()
        }
    }

    fun setChallengeProgress(id: String, progress: Int, maxProgress: Int) {
        viewModelScope.launch {
            settingsRepository.setChallengeProgress(id, progress)
            if (progress >= maxProgress) {
                settingsRepository.setChallengeCompleted(id, true)
            } else {
                settingsRepository.setChallengeCompleted(id, false)
            }
            loadChallengesState()
        }
    }

    fun resetChallenge(id: String) {
        viewModelScope.launch {
            settingsRepository.joinChallenge(id, 0L)
            settingsRepository.setChallengeProgress(id, 0)
            settingsRepository.setChallengeCompleted(id, false)
            loadChallengesState()
        }
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

    fun updateProfile(name: String, imageUri: String) {
        viewModelScope.launch {
            settingsRepository.setProfileName(name)
            settingsRepository.setProfileImageUri(imageUri)
            _profileName.value = name
            _profileImageUri.value = imageUri
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

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            financeRepository.insertTransaction(transaction)
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

    fun clearAllData() {
        viewModelScope.launch {
            transactions.value.forEach { financeRepository.deleteTransaction(it) }
            loans.value.forEach { financeRepository.deleteLoan(it) }
            savingsGoals.value.forEach { financeRepository.deleteSavingsGoal(it) }
            setBudgetLimit(0.0)
        }
    }
}
