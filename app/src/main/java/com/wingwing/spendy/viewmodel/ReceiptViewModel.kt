package com.wingwing.spendy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wingwing.spendy.data.Receipt
import com.wingwing.spendy.repository.ReceiptRepository
import kotlinx.coroutines.launch

class ReceiptViewModel : ViewModel() {
    private val repository = ReceiptRepository()

    fun addReceipt(receipt: Receipt) {
        viewModelScope.launch {
            repository.addReceipt(receipt)
        }
    }

    fun fetchReceipts(onResult: (List<Receipt>) -> Unit) {
        viewModelScope.launch {
            val receipts = repository.getReceipts()
            onResult(receipts)
        }
    }
}