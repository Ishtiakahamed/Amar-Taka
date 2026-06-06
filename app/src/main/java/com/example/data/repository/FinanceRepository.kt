package com.example.data.repository

import com.example.data.database.FinanceDao
import com.example.data.model.Transaction
import com.example.data.model.Loan
import com.example.data.model.SavingsGoal
import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val financeDao: FinanceDao) {
    val allTransactions: Flow<List<Transaction>> = financeDao.getAllTransactions()
    val allLoans: Flow<List<Loan>> = financeDao.getAllLoans()
    val allSavingsGoals: Flow<List<SavingsGoal>> = financeDao.getAllSavingsGoals()

    suspend fun insertTransaction(transaction: Transaction) {
        financeDao.insertTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        financeDao.deleteTransaction(transaction)
    }

    suspend fun insertLoan(loan: Loan) {
        financeDao.insertLoan(loan)
    }

    suspend fun updateLoan(loan: Loan) {
        financeDao.updateLoan(loan)
    }

    suspend fun deleteLoan(loan: Loan) {
        financeDao.deleteLoan(loan)
    }

    suspend fun insertSavingsGoal(goal: SavingsGoal) {
        financeDao.insertSavingsGoal(goal)
    }

    suspend fun updateSavingsGoal(goal: SavingsGoal) {
        financeDao.updateSavingsGoal(goal)
    }

    suspend fun deleteSavingsGoal(goal: SavingsGoal) {
        financeDao.deleteSavingsGoal(goal)
    }
}
