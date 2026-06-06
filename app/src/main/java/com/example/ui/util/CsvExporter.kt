package com.example.ui.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.model.Transaction
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {
    fun exportTransactions(context: Context, transactions: List<Transaction>, filename: String) {
        val headers = "Date,Type,Amount,Category,Wallet,Note\n"
        val rows = StringBuilder().append(headers)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        
        transactions.forEach { tx ->
            val dateStr = dateFormat.format(Date(tx.date))
            // Escape notes containing quotes or commas to generate valid, standard-compliant CSVs
            val cleanNote = tx.note.replace("\"", "\"\"")
            val noteField = if (cleanNote.contains(",") || cleanNote.contains("\n") || cleanNote.contains("\"")) {
                "\"$cleanNote\""
            } else {
                cleanNote
            }
            rows.append("$dateStr,${tx.type},${tx.amount},${tx.category},${tx.wallet},$noteField\n")
        }

        try {
            val cacheDir = context.cacheDir
            // Ensure cache directory exists
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            val file = File(cacheDir, filename)
            file.writeText(rows.toString())

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, filename.replace(".csv", ""))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Amar Taka Report"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
