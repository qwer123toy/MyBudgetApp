package com.mybudgetapp.ui.fragment

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mybudgetapp.R
import com.mybudgetapp.data.DatabaseHelper
import com.mybudgetapp.data.Transaction
import com.mybudgetapp.ui.adapter.TransactionAdapter

class HistoryFragment : Fragment() {
    
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var adapter: TransactionAdapter
    private lateinit var spinnerMonth: Spinner
    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerType: Spinner
    private lateinit var btnFilter: Button
    private lateinit var btnClearFilter: Button
    private lateinit var switchFixed: Switch
    private lateinit var switchNecessary: Switch
    
    private val transactions = mutableListOf<Transaction>()
    private val allTransactions = mutableListOf<Transaction>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        
        databaseHelper = DatabaseHelper(requireContext())
        initViews(view)
        setupSpinners()
        setupRecyclerView()
        setupFilterButtons()
        loadAllTransactions()
        
        return view
    }
    
    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view_transactions)
        emptyView = view.findViewById(R.id.tv_empty_view)
        spinnerMonth = view.findViewById(R.id.spinner_month)
        spinnerCategory = view.findViewById(R.id.spinner_category)
        spinnerType = view.findViewById(R.id.spinner_type)
        btnFilter = view.findViewById(R.id.btn_filter)
        btnClearFilter = view.findViewById(R.id.btn_clear_filter)
        switchFixed = view.findViewById(R.id.switch_filter_fixed)
        switchNecessary = view.findViewById(R.id.switch_filter_necessary)
    }
    
    private fun setupSpinners() {
        // 월 스피너 (최근 12개월)
        val months = generateMonthList()
        val monthAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMonth.adapter = monthAdapter
        
        // 카테고리 스피너
        val categories = listOf("전체") + resources.getStringArray(R.array.categories).toList()
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = categoryAdapter
        
        // 타입 스피너
        val types = listOf("전체", "수입", "지출")
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, types)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = typeAdapter
    }
    
    private fun generateMonthList(): List<String> {
        val months = mutableListOf("전체")
        val calendar = java.util.Calendar.getInstance()
        
        for (i in 0..11) {
            val month = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault())
                .format(calendar.time)
            val displayMonth = java.text.SimpleDateFormat("yyyy년 MM월", java.util.Locale.KOREA)
                .format(calendar.time)
            months.add(displayMonth)
            calendar.add(java.util.Calendar.MONTH, -1)
        }
        
        return months
    }
    
    private fun setupRecyclerView() {
        adapter = TransactionAdapter(
            transactions,
            { transaction -> deleteTransaction(transaction) },
            { transaction -> editTransaction(transaction) }
        )
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }
    
    private fun setupFilterButtons() {
        btnFilter.setOnClickListener {
            applyFilters()
        }
        
        btnClearFilter.setOnClickListener {
            clearFilters()
        }
    }
    
    private fun applyFilters() {
        val selectedMonth = if (spinnerMonth.selectedItemPosition == 0) {
            null // "전체" 선택됨
        } else {
            // 선택된 월을 yyyy-MM 형식으로 변환
            val displayMonth = spinnerMonth.selectedItem.toString()
            convertDisplayMonthToFormat(displayMonth)
        }
        
        val selectedCategory = if (spinnerCategory.selectedItemPosition == 0) {
            null // "전체" 선택됨
        } else {
            spinnerCategory.selectedItem.toString()
        }
        
        val selectedType = if (spinnerType.selectedItemPosition == 0) {
            null // "전체" 선택됨
        } else {
            spinnerType.selectedItem.toString()
        }
        
        val isFixed = if (switchFixed.isChecked) true else null
        val isNecessary = if (switchNecessary.isChecked) true else null
        
        val filteredTransactions = databaseHelper.getFilteredTransactions(
            month = selectedMonth,
            category = selectedCategory,
            type = selectedType,
            isFixed = isFixed,
            isNecessary = isNecessary
        )
        
        updateTransactionList(filteredTransactions)
        Toast.makeText(context, "🔍 필터가 적용되었어요! (${filteredTransactions.size}건)", Toast.LENGTH_SHORT).show()
    }
    
    private fun convertDisplayMonthToFormat(displayMonth: String): String {
        // "2024년 12월" -> "2024-12"
        return displayMonth.replace("년 ", "-").replace("월", "")
    }
    
    private fun clearFilters() {
        spinnerMonth.setSelection(0)
        spinnerCategory.setSelection(0)
        spinnerType.setSelection(0)
        switchFixed.isChecked = false
        switchNecessary.isChecked = false
        
        updateTransactionList(allTransactions)
        Toast.makeText(context, "🧹 필터가 초기화되었어요!", Toast.LENGTH_SHORT).show()
    }
    
    private fun loadAllTransactions() {
        val allTrans = databaseHelper.getAllTransactions()
        allTransactions.clear()
        allTransactions.addAll(allTrans)
        updateTransactionList(allTrans)
    }
    
    private fun updateTransactionList(transactionList: List<Transaction>) {
        transactions.clear()
        transactions.addAll(transactionList)
        adapter.notifyDataSetChanged()
        
        if (transactions.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
        }
    }
    
    private fun deleteTransaction(transaction: Transaction) {
        val result = databaseHelper.deleteTransaction(transaction.id)
        if (result > 0) {
            Toast.makeText(context, "😊 삭제되었어요!", Toast.LENGTH_SHORT).show()
            loadAllTransactions() // 전체 데이터 다시 로드
        } else {
            Toast.makeText(context, "😢 삭제에 실패했습니다!", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun editTransaction(transaction: Transaction) {
        showEditTransactionDialog(transaction)
    }
    
    private fun showEditTransactionDialog(transaction: Transaction) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_transaction, null)
        
        val etDate = dialogView.findViewById<EditText>(R.id.et_edit_date)
        val spinnerType = dialogView.findViewById<Spinner>(R.id.spinner_edit_type)
        val etItem = dialogView.findViewById<EditText>(R.id.et_edit_item)
        val etAmount = dialogView.findViewById<EditText>(R.id.et_edit_amount)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinner_edit_category)
        val switchFixed = dialogView.findViewById<Switch>(R.id.switch_edit_fixed)
        val switchNecessary = dialogView.findViewById<Switch>(R.id.switch_edit_necessary)
        val etMemo = dialogView.findViewById<EditText>(R.id.et_edit_memo)
        
        // 기존 값 설정
        etDate.setText(transaction.date)
        etItem.setText(transaction.item)
        etAmount.setText(transaction.amount.toString())
        switchFixed.isChecked = transaction.isFixed
        switchNecessary.isChecked = transaction.isNecessary
        etMemo.setText(transaction.memo)
        
        // 타입 스피너 설정
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listOf("수입", "지출"))
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = typeAdapter
        spinnerType.setSelection(if (transaction.type == "수입") 0 else 1)
        
        // 카테고리 스피너 설정
        val categories = resources.getStringArray(R.array.categories)
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = categoryAdapter
        val categoryIndex = categories.indexOf(transaction.category)
        if (categoryIndex >= 0) {
            spinnerCategory.setSelection(categoryIndex)
        }
        
        // 날짜 선택 기능
        etDate.setOnClickListener {
            showDatePickerDialog(etDate)
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("✏️ 거래 수정")
            .setView(dialogView)
            .setPositiveButton("💝 수정") { _, _ ->
                val updatedTransaction = transaction.copy(
                    date = etDate.text.toString(),
                    type = spinnerType.selectedItem.toString(),
                    item = etItem.text.toString(),
                    amount = etAmount.text.toString().toIntOrNull() ?: 0,
                    category = spinnerCategory.selectedItem.toString(),
                    isFixed = switchFixed.isChecked,
                    isNecessary = switchNecessary.isChecked,
                    memo = etMemo.text.toString()
                )
                
                val result = databaseHelper.updateTransaction(updatedTransaction)
                if (result > 0) {
                    Toast.makeText(context, "✨ 수정되었어요!", Toast.LENGTH_SHORT).show()
                    loadAllTransactions()
                } else {
                    Toast.makeText(context, "😢 수정에 실패했습니다!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("❌ 취소", null)
            .show()
    }
    
    private fun showDatePickerDialog(editText: EditText) {
        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH)
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        
        DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val dateStr = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            editText.setText(dateStr)
        }, year, month, day).show()
    }
    
    override fun onResume() {
        super.onResume()
        loadAllTransactions() // 프래그먼트가 다시 표시될 때 데이터 새로고침
    }
} 