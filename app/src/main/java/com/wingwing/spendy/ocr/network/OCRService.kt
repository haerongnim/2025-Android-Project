package com.wingwing.spendy.ocr.network

import com.wingwing.spendy.ocr.data.OCRRequest
import com.wingwing.spendy.ocr.data.OCRResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface OCRService {
    @Headers(
        "Content-Type: application/json; charset=UTF-8",
        "X-OCR-SECRET: SkVCTmtHd1VFR2VHam9LYW5IeUNiWFdRUUVXU0JkZ0Y="
    )
    @POST("api/v1/ocr/receipt")
    suspend fun requestOCR(@Body request: OCRRequest): Response<OCRResponse>
}