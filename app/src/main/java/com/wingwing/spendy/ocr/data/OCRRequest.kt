package com.wingwing.spendy.ocr.data

import java.util.*

data class OCRRequest(
    val images: List<Image>,
    val requestId: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val version: String = "v1"
) {
    data class Image(
        val format: String = "jpeg",
        val name: String = "spendy_receipt.jpg",
        val data: String // base64 인코딩 문자열
    )
}
