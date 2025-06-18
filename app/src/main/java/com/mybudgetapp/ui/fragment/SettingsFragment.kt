package com.mybudgetapp.ui.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.mybudgetapp.R
import com.mybudgetapp.data.BudgetPreferences
import com.mybudgetapp.data.DatabaseHelper
import java.text.NumberFormat
import java.util.*

class SettingsFragment : Fragment() {
    
    private lateinit var budgetPreferences: BudgetPreferences
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var btnSetBudget: Button
    private lateinit var tvCurrentBudget: TextView
    private lateinit var switchAutoApply: Switch
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        
        budgetPreferences = BudgetPreferences(requireContext())
        databaseHelper = DatabaseHelper(requireContext())
        initViews(view)
        setupBudgetSettings()
        loadCurrentBudget()
        
        return view
    }
    
    private fun initViews(view: View) {
        btnSetBudget = view.findViewById(R.id.btn_set_budget)
        tvCurrentBudget = view.findViewById(R.id.tv_current_budget)
        switchAutoApply = view.findViewById(R.id.switch_auto_apply)
    }
    
    private fun setupBudgetSettings() {
        btnSetBudget.setOnClickListener {
            showBudgetSettingDialog()
        }
        
        switchAutoApply.setOnCheckedChangeListener { _, isChecked ->
            budgetPreferences.setAutoApply(isChecked)
            val message = if (isChecked) {
                "🔄 다음 달에 자동으로 예산이 적용됩니다!"
            } else {
                "✋ 매월 수동으로 예산을 설정해야 합니다."
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun loadCurrentBudget() {
        val currentMonth = databaseHelper.getCurrentMonth()
        val budget = budgetPreferences.getBudget(currentMonth)
        val autoApply = budgetPreferences.isAutoApply()
        
        if (budget > 0) {
            val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
            tvCurrentBudget.text = "${numberFormat.format(budget)}원"
        } else {
            tvCurrentBudget.text = "설정되지 않음"
        }
        
        switchAutoApply.isChecked = autoApply
    }
    
    private fun showBudgetSettingDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_budget_setting, null)
        val etBudget = dialogView.findViewById<EditText>(R.id.et_budget_amount)
        
        // 현재 예산이 있으면 미리 입력
        val currentMonth = databaseHelper.getCurrentMonth()
        val currentBudget = budgetPreferences.getBudget(currentMonth)
        if (currentBudget > 0) {
            etBudget.setText(currentBudget.toString())
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("💰 월 예산 설정")
            .setMessage("이번 달 예산을 설정해주세요")
            .setView(dialogView)
            .setPositiveButton("💝 설정") { _, _ ->
                val budgetText = etBudget.text.toString().trim()
                if (budgetText.isNotEmpty()) {
                    try {
                        val budgetAmount = budgetText.toInt()
                        if (budgetAmount > 0) {
                            budgetPreferences.setBudget(currentMonth, budgetAmount)
                            loadCurrentBudget()
                            
                            val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
                            Toast.makeText(
                                context, 
                                "🎉 예산이 ${numberFormat.format(budgetAmount)}원으로 설정되었어요!", 
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(context, "😅 0원보다 큰 금액을 입력해주세요!", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: NumberFormatException) {
                        Toast.makeText(context, "😅 올바른 숫자를 입력해주세요!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "😅 예산 금액을 입력해주세요!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("❌ 취소", null)
            .show()
    }
    
    override fun onResume() {
        super.onResume()
        loadCurrentBudget()
    }
} 