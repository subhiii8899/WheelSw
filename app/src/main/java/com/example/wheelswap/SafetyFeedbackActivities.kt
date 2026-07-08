package com.example.wheelswap

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// ── SAFETY TIPS ACTIVITY ──
class SafetyTipsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageHelper.applyLanguage(this)
        setContent {
            SafetyTipsScreen { finish() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafetyTipsScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.safety_tips), color = Gold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = Gold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            SafetyCard(stringResource(R.string.safety_tip_1_title), stringResource(R.string.safety_tip_1_desc))
            SafetyCard(stringResource(R.string.safety_tip_2_title), stringResource(R.string.safety_tip_2_desc))
            SafetyCard(stringResource(R.string.safety_tip_3_title), stringResource(R.string.safety_tip_3_desc))
            SafetyCard(stringResource(R.string.safety_tip_4_title), stringResource(R.string.safety_tip_4_desc))
        }
    }
}

@Composable
fun SafetyCard(title: String, desc: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = Gold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(desc, color = Color.White, fontSize = 14.sp)
        }
    }
}

// ── FEEDBACK ACTIVITY ──
class FeedbackActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageHelper.applyLanguage(this)
        setContent {
            FeedbackScreen { finish() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(onBack: () -> Unit) {
    var feedbackText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    
    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.feedback), color = Gold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = Gold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text(stringResource(R.string.feedback_question), color = Color.White, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = feedbackText,
                onValueChange = { feedbackText = it },
                modifier = Modifier.fillMaxWidth().height(200.dp),
                placeholder = { Text(stringResource(R.string.feedback_placeholder), color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                enabled = !isSubmitting
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (feedbackText.isBlank()) return@Button
                    
                    isSubmitting = true
                    val user = auth.currentUser
                    val feedbackData = hashMapOf(
                        "userId" to (user?.uid ?: "Anonymous"),
                        "userEmail" to (user?.email ?: "N/A"),
                        "feedback" to feedbackText,
                        "timestamp" to System.currentTimeMillis()
                    )
                    
                    db.collection("feedback").add(feedbackData)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Feedback submitted! Thank you.", Toast.LENGTH_SHORT).show()
                            onBack()
                        }
                        .addOnFailureListener {
                            isSubmitting = false
                            Toast.makeText(context, "Submission failed. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold),
                enabled = !isSubmitting && feedbackText.isNotBlank()
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = DarkBg)
                } else {
                    Text(stringResource(R.string.feedback_submit), color = DarkBg, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ── ABOUT ACTIVITY ──
class AboutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageHelper.applyLanguage(this)
        setContent {
            AboutScreen { finish() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about_app), color = Gold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = Gold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(24.dp)) {
            Text(stringResource(R.string.app_name), color = Gold, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Text(stringResource(R.string.app_version), color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Text(stringResource(R.string.about_desc), color = Color.White, fontSize = 16.sp, lineHeight = 24.sp)
        }
    }
}
