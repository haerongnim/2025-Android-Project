package com.wingwing.spendy.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.wingwing.spendy.data.Receipt
import kotlinx.coroutines.tasks.await

class ReceiptRepository {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("receipts")

    suspend fun addReceipt(receipt: Receipt) {
        collection.add(receipt).await()
    }

    suspend fun getReceipts(): List<Receipt> {
        return collection.get().await().toObjects(Receipt::class.java)
    }
}