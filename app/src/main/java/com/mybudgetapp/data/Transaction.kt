package com.mybudgetapp.data

data class Transaction(
    val id: Long = 0,
    val date: String, // yyyy-MM-dd 형식
    val type: String, // "수입" or "지출"
    val item: String, // 항목명
    val amount: Int, // 금액
    val category: String, // 카테고리
    val isFixed: Boolean = false, // 고정지출 여부
    val isNecessary: Boolean = true, // 필요성 여부
    val memo: String? = null // 메모
) 