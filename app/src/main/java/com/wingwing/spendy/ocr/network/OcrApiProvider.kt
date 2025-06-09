package com.wingwing.spendy.ocr.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object OcrApiProvider {
    val ocrService: OCRService by lazy {
        Retrofit.Builder()
            .baseUrl("https://4bjje9ywby.apigw.ntruss.com/custom/v1/42614/f3dc4f524ec262ead5a026fa7a1bb2eb71b36b84d680b7bd9355d73b04ad54b4/document/receipt/") // 꼭 끝에 `/` 포함!
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OCRService::class.java)
    }
}