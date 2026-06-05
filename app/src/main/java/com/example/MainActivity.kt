package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.NamazApp
import com.startapp.sdk.adsbase.StartAppSDK

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Initialize StartApp SDK
    try {
      StartAppSDK.init(this, "205942114", false)
      StartAppSDK.enableReturnAds(false) // optional: disable return ads on app resume for better UX
    } catch (e: Exception) {
      e.printStackTrace()
    }

    enableEdgeToEdge()
    setContent {
      NamazApp()
    }
  }
}
