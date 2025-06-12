package com.wingwing.spendy.ocr.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.wingwing.spendy.ocr.data.OCRRequest
import com.wingwing.spendy.ocr.network.OcrApiProvider
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

import com.google.gson.Gson

@Composable
fun OcrScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var ocrResult by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val base64 = compressAndEncodeImageToBase64(uri, context)
            Log.d("BASE64_LENGTH", "Base64 length: ${base64.length}")

            val request = OCRRequest(
                images = listOf(OCRRequest.Image(
                    format = "jpeg",
                    name = "spendy_receipt.jpg",
                    data = base64
                ))
//                version = "v1" // ← v1 엔드포인트와 맞춤!
            )

            val gson = Gson()
            Log.d("OCR_JSON_BODY", gson.toJson(request))

            scope.launch {
                isLoading = true
                try {
                    val response = OcrApiProvider.ocrService.requestOCR(request)
                    if (response.isSuccessful) {
                        val resultText = response.body()?.images?.flatMap {
                            it.receipt?.result?.subResults ?: emptyList()
                        }?.joinToString("\n") { it.text ?: "" }

                        ocrResult = resultText ?: "텍스트 없음"
                    } else {
                        val errorMsg = response.errorBody()?.string()
                        ocrResult = "서버 응답 오류: ${response.code()}\n${errorMsg ?: ""}"
                    }
                } catch (e: Exception) {
                    Log.e("OCR", "에러 발생", e)
                    ocrResult = "오류 발생: ${e.message}"
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { launcher.launch("image/*") }) {
            Text("영수증 이미지 선택")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else if (ocrResult != null) {
            Text("인식 결과:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(ocrResult ?: "", style = MaterialTheme.typography.bodyMedium, color = Color.Black)
        }
    }
}

/**
 * 이미지 압축 후 Base64로 인코딩
 */
fun compressAndEncodeImageToBase64(uri: Uri, context: android.content.Context): String {
    val inputStream = context.contentResolver.openInputStream(uri)
    val bitmap = BitmapFactory.decodeStream(inputStream)

    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream) // 압축률 60%
    val byteArray = outputStream.toByteArray()

    return Base64.encodeToString(byteArray, Base64.NO_WRAP)
}