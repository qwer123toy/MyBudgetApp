package com.mybudgetapp.ui.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.mybudgetapp.R
import com.mybudgetapp.data.DatabaseHelper
import com.mybudgetapp.data.Transaction
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionFragment : Fragment() {
    
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var etItem: EditText
    private lateinit var etAmount: EditText
    private lateinit var etMemo: EditText
    private lateinit var tvDate: TextView
    private lateinit var spinnerType: Spinner
    private lateinit var spinnerCategory: Spinner
    private lateinit var switchFixed: Switch
    private lateinit var switchNecessary: Switch
    private lateinit var btnSave: Button
    
    private var selectedDate = Calendar.getInstance()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_transaction, container, false)
        
        databaseHelper = DatabaseHelper(requireContext())
        initViews(view)
        setupSpinners()
        setupDatePicker()
        setupSaveButton()
        
        return view
    }
    
    private fun initViews(view: View) {
        etItem = view.findViewById(R.id.et_item)
        etAmount = view.findViewById(R.id.et_amount)
        etMemo = view.findViewById(R.id.et_memo)
        tvDate = view.findViewById(R.id.tv_date)
        spinnerType = view.findViewById(R.id.spinner_type)
        spinnerCategory = view.findViewById(R.id.spinner_category)
        switchFixed = view.findViewById(R.id.switch_fixed)
        switchNecessary = view.findViewById(R.id.switch_necessary)
        btnSave = view.findViewById(R.id.btn_save)
        
        updateDateDisplay()
    }
    
    private fun setupSpinners() {
        val typeAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.transaction_types,
            android.R.layout.simple_spinner_item
        )
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = typeAdapter
        
        val categoryAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.categories,
            android.R.layout.simple_spinner_item
        )
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = categoryAdapter
    }
    
    private fun setupDatePicker() {
        tvDate.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    selectedDate.set(year, month, dayOfMonth)
                    updateDateDisplay()
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }
    
    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일 (E)", Locale.KOREA)
        tvDate.text = "📅 ${dateFormat.format(selectedDate.time)}"
    }
    
    private fun setupSaveButton() {
        btnSave.setOnClickListener {
            saveTransaction()
        }
    }
    
    private fun saveTransaction() {
        val item = etItem.text.toString().trim()
        val amountStr = etAmount.text.toString().trim()
        val memo = etMemo.text.toString().trim()
        
        if (item.isEmpty()) {
            Toast.makeText(context, "💝 항목명을 입력해주세요!", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (amountStr.isEmpty()) {
            Toast.makeText(context, "💰 금액을 입력해주세요!", Toast.LENGTH_SHORT).show()
            return
        }
        
        val amount = amountStr.toIntOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(context, "🔢 올바른 금액을 입력해주세요!", Toast.LENGTH_SHORT).show()
            return
        }
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val transaction = Transaction(
            date = dateFormat.format(selectedDate.time),
            type = spinnerType.selectedItem.toString(),
            item = item,
            amount = amount,
            category = spinnerCategory.selectedItem.toString(),
            isFixed = switchFixed.isChecked,
            isNecessary = switchNecessary.isChecked,
            memo = memo.ifEmpty { null }
        )
        
        val result = databaseHelper.insertTransaction(transaction)
        
        if (result != -1L) {
            Toast.makeText(context, "🎉 성공적으로 저장되었어요!", Toast.LENGTH_SHORT).show()
            clearForm()
        } else {
            Toast.makeText(context, "😢 저장에 실패했습니다!", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun clearForm() {
        etItem.text.clear()
        etAmount.text.clear()
        etMemo.text.clear()
        spinnerType.setSelection(0)
        spinnerCategory.setSelection(0)
        switchFixed.isChecked = false
        switchNecessary.isChecked = true
        selectedDate = Calendar.getInstance()
        updateDateDisplay()
    }
} 