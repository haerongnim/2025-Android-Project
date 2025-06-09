package com.wingwing.spendy.ocr.data

import android.content.Context
import android.net.Uri
import android.util.Base64

fun encodeImageToBase64(uri: Uri, context: Context): String {
    val inputStream = context.contentResolver.openInputStream(uri)
    val bytes = inputStream?.readBytes()
    return Base64.encodeToString(bytes, Base64.NO_WRAP)
}