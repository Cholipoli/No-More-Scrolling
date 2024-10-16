package com.example.nomorescrolling

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text // Use material3 or material, depending on your version
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier


@Composable
fun OverlayContent() {
    Box(modifier = Modifier.fillMaxSize().background(Color(0x80000000))) { // Semi-transparent background
        Text("You have been scrolling for too long!", color = Color.White)
        // Add more UI components as needed
    }
}