package com.wingwing.spendy.ocr.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object OcrApiProvider {
    val ocrService: OCRService by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        Retrofit.Builder()
            .baseUrl("https://4bjje9ywby.apigw.ntruss.com/custom/v1/42614/f3dc4f524ec262ead5a026fa7a1bb2eb71b36b84d680b7bd9355d73b04ad54b4/document/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(OCRService::class.java)
    }
}