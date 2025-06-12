package com.wingwing.spendy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.wingwing.spendy.ocr.ui.OcrScreen
import com.google.firebase.FirebaseApp
import com.wingwing.spendy.ui.theme.SpendyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContent {
            SpendyTheme(dynamicColor = false){
                SpendyApp()
            }
//            OcrScreen()
        }
    }
}
