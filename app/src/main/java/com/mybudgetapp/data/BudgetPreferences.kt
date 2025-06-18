package com.mybudgetapp.data

import android.content.Context
import android.content.SharedPreferences

class BudgetPreferences(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "budget_prefs", Context.MODE_PRIVATE
    )
    
    companion object {
        private const val KEY_BUDGET_PREFIX = "budget_"
        private const val KEY_AUTO_APPLY = "auto_apply_budget"
    }
    
    // 월별 예산 설정
    fun setBudget(month: String, amount: Int) {
        prefs.edit()
            .putInt("$KEY_BUDGET_PREFIX$month", amount)
            .apply()
    }
    
    // 월별 예산 조회
    fun getBudget(month: String): Int {
        return prefs.getInt("$KEY_BUDGET_PREFIX$month", 0)
    }
    
    // 자동 적용 설정
    fun setAutoApply(autoApply: Boolean) {
        prefs.edit()
            .putBoolean(KEY_AUTO_APPLY, autoApply)
            .apply()
    }
    
    // 자동 적용 여부 조회
    fun isAutoApply(): Boolean {
        return prefs.getBoolean(KEY_AUTO_APPLY, false)
    }
    
    // 다음 달 예산 자동 적용
    fun applyBudgetToNextMonth(currentMonth: String, nextMonth: String) {
        if (isAutoApply()) {
            val currentBudget = getBudget(currentMonth)
            if (currentBudget > 0) {
                setBudget(nextMonth, currentBudget)
            }
        }
    }
} 