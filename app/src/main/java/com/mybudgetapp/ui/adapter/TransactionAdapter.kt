package com.mybudgetapp.ui.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mybudgetapp.R
import com.mybudgetapp.data.Transaction
import java.text.NumberFormat
import java.util.*

class TransactionAdapter(
    private val transactions: List<Transaction>,
    private val onDeleteClick: (Transaction) -> Unit,
    private val onEditClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tv_transaction_date)
        val tvItem: TextView = view.findViewById(R.id.tv_transaction_item)
        val tvAmount: TextView = view.findViewById(R.id.tv_transaction_amount)
        val tvCategory: TextView = view.findViewById(R.id.tv_transaction_category)
        val tvTags: TextView = view.findViewById(R.id.tv_transaction_tags)
        val tvMemo: TextView = view.findViewById(R.id.tv_transaction_memo)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]
        val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
        
        holder.tvDate.text = transaction.date
        holder.tvItem.text = "${getTypeEmoji(transaction.type)} ${transaction.item}"
        holder.tvAmount.text = "${if (transaction.type == "수입") "+" else "-"}${numberFormat.format(transaction.amount)}원"
        holder.tvCategory.text = transaction.category
        
        // 태그 표시 (고정지출/필수항목)
        val tags = mutableListOf<String>()
        if (transaction.isFixed) tags.add("🔄 고정지출")
        if (transaction.isNecessary) tags.add("⭐ 필수")
        
        if (tags.isNotEmpty()) {
            holder.tvTags.text = tags.joinToString("  ")
            holder.tvTags.visibility = View.VISIBLE
        } else {
            holder.tvTags.visibility = View.GONE
        }
        
        holder.tvMemo.text = if (transaction.memo.isNullOrEmpty()) "메모 없음" else transaction.memo
        
        // 수입/지출에 따른 색상 변경
        val textColor = if (transaction.type == "수입") {
            holder.itemView.context.getColor(R.color.income_color)
        } else {
            holder.itemView.context.getColor(R.color.expense_color)
        }
        holder.tvAmount.setTextColor(textColor)
        
        // 클릭 이벤트
        holder.itemView.setOnClickListener {
            // 일반 클릭 - 수정
            onEditClick(transaction)
        }
        
        holder.itemView.setOnLongClickListener {
            // 길게 누르기 - 삭제 확인 다이얼로그
            showDeleteConfirmDialog(holder.itemView, transaction)
            true
        }
    }
    
    override fun getItemCount() = transactions.size
    
    private fun getTypeEmoji(type: String): String {
        return when (type) {
            "수입" -> "💰"
            "지출" -> "💸"
            else -> "💱"
        }
    }
    
    private fun showDeleteConfirmDialog(view: View, transaction: Transaction) {
        AlertDialog.Builder(view.context)
            .setTitle("🗑️ 삭제 확인")
            .setMessage("'${transaction.item}' 항목을 삭제하시겠습니까?\n\n삭제된 데이터는 복구할 수 없습니다.")
            .setPositiveButton("🗑️ 삭제") { _, _ ->
                onDeleteClick(transaction)
            }
            .setNegativeButton("❌ 취소", null)
            .show()
    }
} 