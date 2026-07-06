package com.example.wheelswap

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import java.util.Locale
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class LanguageSelectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if language is already selected
        val prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        if (prefs.contains("Language")) {
            startActivity(Intent(this, LoginScreen::class.java))
            finish()
            return
        }

        setContent {
            LanguageSelectionScreen { lang ->
                saveLanguage(lang)
                startActivity(Intent(this, LoginScreen::class.java))
                finish()
            }
        }
    }

    private fun saveLanguage(lang: String) {
        val prefs = getSharedPreferences("Settings", MODE_PRIVATE)
        prefs.edit().putString("Language", lang).apply()
        LanguageHelper.applyLanguage(this)
    }
}

@Composable
fun LanguageSelectionScreen(onLanguageSelected: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = "Logo",
            modifier = Modifier.size(100.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Select Language",
            color = Gold,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "زبان منتخب کریں",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(48.dp))

        LanguageButton(
            title = "English",
            subtitle = "Continue in English",
            onClick = { onLanguageSelected("en") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LanguageButton(
            title = "اردو",
            subtitle = "اردو میں جاری رکھیں",
            onClick = { onLanguageSelected("ur") }
        )
    }
}

@Composable
fun LanguageButton(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = title, color = Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = subtitle, color = Color.Gray, fontSize = 12.sp)
            }
            Icon(
                painter = painterResource(id = android.R.drawable.ic_media_play),
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
