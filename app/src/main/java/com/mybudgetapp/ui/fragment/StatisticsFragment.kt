package com.mybudgetapp.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.mybudgetapp.R
import com.mybudgetapp.data.DatabaseHelper
import com.mybudgetapp.data.BudgetPreferences
import java.text.NumberFormat
import java.util.*

class StatisticsFragment : Fragment() {
    
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var budgetPreferences: BudgetPreferences
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalExpense: TextView
    private lateinit var tvBalance: TextView
    private lateinit var tvTransactionCount: TextView
    private lateinit var tvFixedExpense: TextView
    private lateinit var tvUnnecessaryExpense: TextView
    private lateinit var tvBudget: TextView
    private lateinit var tvBudgetRemaining: TextView
    private lateinit var tvIncomeExpenseRatio: TextView
    private lateinit var tvMonthChange: TextView
    private lateinit var pieChart: PieChart
    private lateinit var spinnerMonthSelection: Spinner
    private var selectedMonth: String = ""
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_statistics, container, false)
        
        databaseHelper = DatabaseHelper(requireContext())
        budgetPreferences = BudgetPreferences(requireContext())
        selectedMonth = databaseHelper.getCurrentMonth()
        initViews(view)
        setupMonthSpinner()
        loadStatistics()
        
        return view
    }
    
    private fun initViews(view: View) {
        tvTotalIncome = view.findViewById(R.id.tv_total_income)
        tvTotalExpense = view.findViewById(R.id.tv_total_expense)
        tvBalance = view.findViewById(R.id.tv_balance)
        tvTransactionCount = view.findViewById(R.id.tv_transaction_count)
        tvFixedExpense = view.findViewById(R.id.tv_fixed_expense)
        tvUnnecessaryExpense = view.findViewById(R.id.tv_unnecessary_expense)
        tvBudget = view.findViewById(R.id.tv_budget)
        tvBudgetRemaining = view.findViewById(R.id.tv_budget_remaining)
        tvIncomeExpenseRatio = view.findViewById(R.id.tv_income_expense_ratio)
        tvMonthChange = view.findViewById(R.id.tv_month_change)
        pieChart = view.findViewById(R.id.pie_chart)
        spinnerMonthSelection = view.findViewById(R.id.spinner_month_selection)
    }
    
    private fun setupMonthSpinner() {
        val availableMonths = databaseHelper.getAvailableMonths()
        val displayMonths = availableMonths.map { month ->
            // "2024-12" -> "2024년 12월"
            val parts = month.split("-")
            "${parts[0]}년 ${parts[1]}월"
        }
        
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, displayMonths)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMonthSelection.adapter = adapter
        
        // 현재 월을 기본 선택
        val currentIndex = availableMonths.indexOf(selectedMonth)
        if (currentIndex >= 0) {
            spinnerMonthSelection.setSelection(currentIndex)
        }
        
        spinnerMonthSelection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedMonth = availableMonths[position]
                loadStatistics()
            }
            
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
    
    private fun loadStatistics() {
        val currentMonth = selectedMonth
        val previousMonth = databaseHelper.getPreviousMonth()
        val monthlyTransactions = databaseHelper.getTransactionsByMonth(currentMonth)
        val previousMonthTransactions = databaseHelper.getTransactionsByMonth(previousMonth)
        
        // 기본 통계
        val totalIncome = monthlyTransactions.filter { it.type == "수입" }.sumOf { it.amount }
        val totalExpense = monthlyTransactions.filter { it.type == "지출" }.sumOf { it.amount }
        val balance = totalIncome - totalExpense
        val transactionCount = monthlyTransactions.size
        
        // 상세 통계
        val fixedExpense = databaseHelper.getFixedExpenses(currentMonth)
        val unnecessaryExpense = databaseHelper.getUnnecessaryExpenses(currentMonth)
        val variableExpense = totalExpense - fixedExpense
        
        // 예산 관련
        val budget = budgetPreferences.getBudget(currentMonth)
        val budgetRemaining = budget - totalExpense
        
        // 수입 대비 지출 비율
        val incomeExpenseRatio = if (totalIncome > 0) {
            (totalExpense.toDouble() / totalIncome.toDouble() * 100).toInt()
        } else 0
        
        // 전월 대비 변화율
        val previousTotalExpense = previousMonthTransactions.filter { it.type == "지출" }.sumOf { it.amount }
        val monthChangePercent = if (previousTotalExpense > 0) {
            ((totalExpense - previousTotalExpense).toDouble() / previousTotalExpense.toDouble() * 100).toInt()
        } else 0
        
        val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
        
        // UI 업데이트
        tvTotalIncome.text = "${numberFormat.format(totalIncome)}원"
        tvTotalExpense.text = "${numberFormat.format(totalExpense)}원"
        tvBalance.text = "${numberFormat.format(balance)}원"
        tvTransactionCount.text = "${transactionCount}건"
        tvFixedExpense.text = "${numberFormat.format(fixedExpense)}원"
        tvUnnecessaryExpense.text = "${numberFormat.format(unnecessaryExpense)}원"
        
        // 예산 정보
        if (budget > 0) {
            tvBudget.text = "${numberFormat.format(budget)}원"
            tvBudgetRemaining.text = if (budgetRemaining >= 0) {
                "${numberFormat.format(budgetRemaining)}원"
            } else {
                "${numberFormat.format(-budgetRemaining)}원 초과"
            }
            
            // 예산 초과 여부에 따른 색상 변경
            val budgetColor = if (budgetRemaining >= 0) {
                requireContext().getColor(R.color.cute_success)
            } else {
                requireContext().getColor(R.color.cute_error)
            }
            tvBudgetRemaining.setTextColor(budgetColor)
        } else {
            tvBudget.text = "설정되지 않음"
            tvBudgetRemaining.text = "예산을 설정해주세요"
        }
        
        // 수입 대비 지출 비율
        tvIncomeExpenseRatio.text = "${incomeExpenseRatio}%"
        val ratioColor = when {
            incomeExpenseRatio <= 70 -> requireContext().getColor(R.color.cute_success)
            incomeExpenseRatio <= 90 -> requireContext().getColor(R.color.cute_warning)
            else -> requireContext().getColor(R.color.cute_error)
        }
        tvIncomeExpenseRatio.setTextColor(ratioColor)
        
        // 전월 대비 변화율
        val changeText = if (monthChangePercent > 0) {
            "↗️ ${monthChangePercent}% 증가"
        } else if (monthChangePercent < 0) {
            "↘️ ${-monthChangePercent}% 감소"
        } else {
            "➡️ 변화 없음"
        }
        tvMonthChange.text = changeText
        
        val changeColor = when {
            monthChangePercent <= 0 -> requireContext().getColor(R.color.cute_success)
            monthChangePercent <= 20 -> requireContext().getColor(R.color.cute_warning)
            else -> requireContext().getColor(R.color.cute_error)
        }
        tvMonthChange.setTextColor(changeColor)
        
        // 잔액에 따른 색상 변경
        val balanceColor = if (balance >= 0) {
            requireContext().getColor(R.color.cute_success)
        } else {
            requireContext().getColor(R.color.cute_error)
        }
        tvBalance.setTextColor(balanceColor)
        
        // 파이차트 설정
        setupPieChart(currentMonth)
    }
    
    private fun setupPieChart(month: String) {
        val categoryExpenses = databaseHelper.getCategoryExpenses(month)
        
        if (categoryExpenses.isEmpty()) {
            pieChart.visibility = View.GONE
            return
        }
        
        pieChart.visibility = View.VISIBLE
        
        val entries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()
        
        val colorArray = arrayOf(
            Color.parseColor("#FF6B9D"), // 핑크
            Color.parseColor("#FFB74D"), // 오렌지
            Color.parseColor("#81C784"), // 그린
            Color.parseColor("#64B5F6"), // 블루
            Color.parseColor("#BA68C8"), // 퍼플
            Color.parseColor("#FFD54F"), // 옐로우
            Color.parseColor("#FF8A65"), // 딥오렌지
            Color.parseColor("#4DB6AC"), // 틸
            Color.parseColor("#F06292"), // 핑크라이트
            Color.parseColor("#9575CD"), // 딥퍼플
            Color.parseColor("#AED581"), // 라이트그린
            Color.parseColor("#90CAF9")  // 라이트블루
        )
        
        var colorIndex = 0
        for ((category, amount) in categoryExpenses) {
            entries.add(PieEntry(amount.toFloat(), category))
            colors.add(colorArray[colorIndex % colorArray.size])
            colorIndex++
        }
        
        val dataSet = PieDataSet(entries, "카테고리별 지출")
        dataSet.colors = colors
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = Color.WHITE
        
        val data = PieData(dataSet)
        pieChart.data = data
        
        // 차트 스타일링
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.setExtraOffsets(5f, 10f, 5f, 5f)
        pieChart.dragDecelerationFrictionCoef = 0.95f
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.WHITE)
        pieChart.setTransparentCircleColor(Color.WHITE)
        pieChart.setTransparentCircleAlpha(110)
        pieChart.holeRadius = 58f
        pieChart.transparentCircleRadius = 61f
        pieChart.setDrawCenterText(true)
        pieChart.centerText = "📊\n카테고리별\n지출 비율"
        pieChart.setCenterTextSize(14f)
        pieChart.setCenterTextColor(requireContext().getColor(R.color.cute_primary))
        pieChart.rotationAngle = 0f
        pieChart.isRotationEnabled = true
        pieChart.isHighlightPerTapEnabled = true
        
        // 범례 설정
        val legend = pieChart.legend
        legend.isEnabled = true
        legend.textSize = 12f
        legend.textColor = requireContext().getColor(R.color.cute_text)
        
        pieChart.animateY(1000)
        pieChart.invalidate()
    }
    
    override fun onResume() {
        super.onResume()
        setupMonthSpinner() // 스피너 새로고침 (새로운 달이 생겼을 수 있음)
        loadStatistics()
    }
} 