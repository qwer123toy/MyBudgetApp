package com.mybudgetapp.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    
    companion object {
        private const val DATABASE_NAME = "budget.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_TRANSACTIONS = "transactions"
        
        // 컬럼명
        private const val COLUMN_ID = "id"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_TYPE = "type"
        private const val COLUMN_ITEM = "item"
        private const val COLUMN_AMOUNT = "amount"
        private const val COLUMN_CATEGORY = "category"
        private const val COLUMN_IS_FIXED = "is_fixed"
        private const val COLUMN_IS_NECESSARY = "is_necessary"
        private const val COLUMN_MEMO = "memo"
    }
    
    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_TRANSACTIONS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_DATE TEXT NOT NULL,
                $COLUMN_TYPE TEXT NOT NULL,
                $COLUMN_ITEM TEXT NOT NULL,
                $COLUMN_AMOUNT INTEGER NOT NULL,
                $COLUMN_CATEGORY TEXT NOT NULL,
                $COLUMN_IS_FIXED INTEGER DEFAULT 0,
                $COLUMN_IS_NECESSARY INTEGER DEFAULT 1,
                $COLUMN_MEMO TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)
    }
    
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTIONS")
        onCreate(db)
    }
    
    fun insertTransaction(transaction: Transaction): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_DATE, transaction.date)
            put(COLUMN_TYPE, transaction.type)
            put(COLUMN_ITEM, transaction.item)
            put(COLUMN_AMOUNT, transaction.amount)
            put(COLUMN_CATEGORY, transaction.category)
            put(COLUMN_IS_FIXED, if (transaction.isFixed) 1 else 0)
            put(COLUMN_IS_NECESSARY, if (transaction.isNecessary) 1 else 0)
            put(COLUMN_MEMO, transaction.memo)
        }
        return db.insert(TABLE_TRANSACTIONS, null, values)
    }
    
    fun getAllTransactions(): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_TRANSACTIONS,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_DATE DESC"
        )
        
        cursor.use {
            while (it.moveToNext()) {
                val transaction = Transaction(
                    id = it.getLong(it.getColumnIndexOrThrow(COLUMN_ID)),
                    date = it.getString(it.getColumnIndexOrThrow(COLUMN_DATE)),
                    type = it.getString(it.getColumnIndexOrThrow(COLUMN_TYPE)),
                    item = it.getString(it.getColumnIndexOrThrow(COLUMN_ITEM)),
                    amount = it.getInt(it.getColumnIndexOrThrow(COLUMN_AMOUNT)),
                    category = it.getString(it.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                    isFixed = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_FIXED)) == 1,
                    isNecessary = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_NECESSARY)) == 1,
                    memo = it.getString(it.getColumnIndexOrThrow(COLUMN_MEMO))
                )
                transactions.add(transaction)
            }
        }
        return transactions
    }
    
    fun deleteTransaction(id: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_TRANSACTIONS, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }
    
    fun updateTransaction(transaction: Transaction): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_DATE, transaction.date)
            put(COLUMN_TYPE, transaction.type)
            put(COLUMN_ITEM, transaction.item)
            put(COLUMN_AMOUNT, transaction.amount)
            put(COLUMN_CATEGORY, transaction.category)
            put(COLUMN_IS_FIXED, if (transaction.isFixed) 1 else 0)
            put(COLUMN_IS_NECESSARY, if (transaction.isNecessary) 1 else 0)
            put(COLUMN_MEMO, transaction.memo)
        }
        return db.update(TABLE_TRANSACTIONS, values, "$COLUMN_ID = ?", arrayOf(transaction.id.toString()))
    }
    
    fun getTransactionsByMonth(month: String): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_TRANSACTIONS,
            null,
            "$COLUMN_DATE LIKE ?",
            arrayOf("$month%"),
            null,
            null,
            "$COLUMN_DATE DESC"
        )
        
        cursor.use {
            while (it.moveToNext()) {
                val transaction = Transaction(
                    id = it.getLong(it.getColumnIndexOrThrow(COLUMN_ID)),
                    date = it.getString(it.getColumnIndexOrThrow(COLUMN_DATE)),
                    type = it.getString(it.getColumnIndexOrThrow(COLUMN_TYPE)),
                    item = it.getString(it.getColumnIndexOrThrow(COLUMN_ITEM)),
                    amount = it.getInt(it.getColumnIndexOrThrow(COLUMN_AMOUNT)),
                    category = it.getString(it.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                    isFixed = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_FIXED)) == 1,
                    isNecessary = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_NECESSARY)) == 1,
                    memo = it.getString(it.getColumnIndexOrThrow(COLUMN_MEMO))
                )
                transactions.add(transaction)
            }
        }
        return transactions
    }
    
    fun getCurrentMonth(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return dateFormat.format(Date())
    }
    
    // 카테고리별 지출 통계
    fun getCategoryExpenses(month: String): Map<String, Int> {
        val categoryMap = mutableMapOf<String, Int>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_TRANSACTIONS,
            arrayOf(COLUMN_CATEGORY, "SUM($COLUMN_AMOUNT) as total"),
            "$COLUMN_TYPE = ? AND $COLUMN_DATE LIKE ?",
            arrayOf("지출", "$month%"),
            COLUMN_CATEGORY,
            null,
            null
        )
        
        cursor.use {
            while (it.moveToNext()) {
                val category = it.getString(it.getColumnIndexOrThrow(COLUMN_CATEGORY))
                val total = it.getInt(it.getColumnIndexOrThrow("total"))
                categoryMap[category] = total
            }
        }
        return categoryMap
    }
    
    // 고정지출 합계
    fun getFixedExpenses(month: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM($COLUMN_AMOUNT) as total FROM $TABLE_TRANSACTIONS WHERE $COLUMN_TYPE = ? AND $COLUMN_DATE LIKE ? AND $COLUMN_IS_FIXED = 1",
            arrayOf("지출", "$month%")
        )
        
        cursor.use {
            if (it.moveToFirst()) {
                return it.getInt(it.getColumnIndexOrThrow("total"))
            }
        }
        return 0
    }
    
    // 불필요 지출 합계
    fun getUnnecessaryExpenses(month: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM($COLUMN_AMOUNT) as total FROM $TABLE_TRANSACTIONS WHERE $COLUMN_TYPE = ? AND $COLUMN_DATE LIKE ? AND $COLUMN_IS_NECESSARY = 0",
            arrayOf("지출", "$month%")
        )
        
        cursor.use {
            if (it.moveToFirst()) {
                return it.getInt(it.getColumnIndexOrThrow("total"))
            }
        }
        return 0
    }
    
    // 필터링된 거래 조회
    fun getFilteredTransactions(
        month: String? = null,
        category: String? = null,
        type: String? = null,
        isFixed: Boolean? = null,
        isNecessary: Boolean? = null
    ): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val db = readableDatabase
        
        val whereConditions = mutableListOf<String>()
        val whereArgs = mutableListOf<String>()
        
        month?.let {
            whereConditions.add("$COLUMN_DATE LIKE ?")
            whereArgs.add("$it%")
        }
        
        category?.let {
            whereConditions.add("$COLUMN_CATEGORY = ?")
            whereArgs.add(it)
        }
        
        type?.let {
            whereConditions.add("$COLUMN_TYPE = ?")
            whereArgs.add(it)
        }
        
        isFixed?.let {
            whereConditions.add("$COLUMN_IS_FIXED = ?")
            whereArgs.add(if (it) "1" else "0")
        }
        
        isNecessary?.let {
            whereConditions.add("$COLUMN_IS_NECESSARY = ?")
            whereArgs.add(if (it) "1" else "0")
        }
        
        val whereClause = if (whereConditions.isNotEmpty()) {
            whereConditions.joinToString(" AND ")
        } else null
        
        val cursor = db.query(
            TABLE_TRANSACTIONS,
            null,
            whereClause,
            if (whereArgs.isNotEmpty()) whereArgs.toTypedArray() else null,
            null,
            null,
            "$COLUMN_DATE DESC"
        )
        
        cursor.use {
            while (it.moveToNext()) {
                val transaction = Transaction(
                    id = it.getLong(it.getColumnIndexOrThrow(COLUMN_ID)),
                    date = it.getString(it.getColumnIndexOrThrow(COLUMN_DATE)),
                    type = it.getString(it.getColumnIndexOrThrow(COLUMN_TYPE)),
                    item = it.getString(it.getColumnIndexOrThrow(COLUMN_ITEM)),
                    amount = it.getInt(it.getColumnIndexOrThrow(COLUMN_AMOUNT)),
                    category = it.getString(it.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                    isFixed = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_FIXED)) == 1,
                    isNecessary = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_NECESSARY)) == 1,
                    memo = it.getString(it.getColumnIndexOrThrow(COLUMN_MEMO))
                )
                transactions.add(transaction)
            }
        }
        return transactions
    }
    
    // 전월 대비 변화율 계산을 위한 전월 데이터
    fun getPreviousMonth(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        val dateFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
    
    // 거래 데이터가 있는 모든 월 목록 조회
    fun getAvailableMonths(): List<String> {
        val months = mutableListOf<String>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT DISTINCT substr($COLUMN_DATE, 1, 7) as month FROM $TABLE_TRANSACTIONS ORDER BY month DESC",
            null
        )
        
        cursor.use {
            while (it.moveToNext()) {
                val month = it.getString(it.getColumnIndexOrThrow("month"))
                months.add(month)
            }
        }
        
        // 현재 월이 없으면 추가 (데이터가 없어도 선택할 수 있게)
        val currentMonth = getCurrentMonth()
        if (!months.contains(currentMonth)) {
            months.add(0, currentMonth)
        }
        
        return months
    }
} 