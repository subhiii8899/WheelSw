package com.example.wheelswap

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LanguageHelper {
    fun applyLanguage(context: Context) {
        val prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val lang = prefs.getString("Language", "en") ?: "en"
        
        val locale = Locale(lang)
        Locale.setDefault(locale)
        
        val config = Configuration()
        config.setLocale(locale)
        
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}
