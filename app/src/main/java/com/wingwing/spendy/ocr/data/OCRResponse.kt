package com.wingwing.spendy.ocr.data

data class OCRResponse(
    val images: List<OCRImage>
)

data class OCRImage(
    val receipt: Receipt?
)

data class Receipt(
    val result: ReceiptResult?
)

data class ReceiptResult(
    val subResults: List<SubResult>?
)

data class SubResult(
    val text: String?, // 인식된 텍스트 (한 항목)
    val fields: List<Field>?
)

data class Field(
    val name: FieldItem?,
    val value: FieldItem?
)

data class FieldItem(
    val text: String?
)