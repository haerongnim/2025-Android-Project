package com.wingwing.spendy.data

data class Receipt(
    val id: String = "",
    val storeName: String = "",
    val amount: Int = 0,
    val date: String = "",
    val category: String = "",
    val location: String? = null
)