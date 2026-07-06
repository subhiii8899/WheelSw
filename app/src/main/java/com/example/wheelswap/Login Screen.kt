package com.example.wheelswap

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LoginScreen : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential).addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        db.collection("users").document(user.uid).get().addOnSuccessListener { doc ->
                            if (doc.exists() && !doc.getString("firstName").isNullOrBlank()) {
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            } else {
                                // Go to setup even if Google provided names, so they can add a pic
                                startActivity(Intent(this, ProfileSetupActivity::class.java))
                                finish()
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, getString(R.string.firebase_failed, authTask.exception?.message), Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: ApiException) {
            Toast.makeText(this, getString(R.string.google_failed, e.statusCode), Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageHelper.applyLanguage(this)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Auto-redirect if already logged in
        if (auth.currentUser != null) {
            checkProfileAndRedirect()
        }

        setContent {
            LoginContent(
                onGoogleSignIn = { signInWithGoogle() },
                onAuthAction = { firstName, lastName, email, password, confirmPassword, isLoginMode ->
                    handleAuth(firstName, lastName, email, password, confirmPassword, isLoginMode)
                }
            )
        }
    }

    private fun handleAuth(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String,
        isLoginMode: Boolean
    ) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.email_pass_required), Toast.LENGTH_SHORT).show()
            return
        }

        if (isLoginMode) {
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    checkProfileAndRedirect()
                } else {
                    Toast.makeText(this, getString(R.string.login_failed, task.exception?.message), Toast.LENGTH_LONG).show()
                }
            }
        } else {
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // New users always go to Profile Setup
                    startActivity(Intent(this, ProfileSetupActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, getString(R.string.reg_failed, task.exception?.message), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun checkProfileAndRedirect() {
        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid).get().addOnSuccessListener { doc ->
            if (doc.exists() && !doc.getString("firstName").isNullOrBlank()) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                startActivity(Intent(this, ProfileSetupActivity::class.java))
                finish()
            }
        }.addOnFailureListener {
            // Fallback to setup if firestore check fails
            startActivity(Intent(this, ProfileSetupActivity::class.java))
            finish()
        }
    }

    private fun signInWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInLauncher.launch(googleSignInClient.signInIntent)
    }
}

@Composable
fun LoginContent(
    onGoogleSignIn: () -> Unit,
    onAuthAction: (String, String, String, String, String, Boolean) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = "App Logo",
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "WheelSwap",
            color = Gold,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (isLoginMode) stringResource(R.string.welcome_back) else stringResource(R.string.create_account),
            color = Color.Gray,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.email_address)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = authTextFieldColors()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = PasswordVisualTransformation(),
            colors = authTextFieldColors()
        )

        if (!isLoginMode) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text(stringResource(R.string.confirm_password)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = PasswordVisualTransformation(),
                colors = authTextFieldColors()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onAuthAction("", "", email, password, confirmPassword, isLoginMode) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Gold),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (isLoginMode) stringResource(R.string.login_btn) else stringResource(R.string.signup_btn),
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { isLoginMode = !isLoginMode }) {
            Text(
                text = if (isLoginMode) stringResource(R.string.no_account) else stringResource(R.string.already_account),
                color = Gold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.DarkGray)
            Text(
                text = stringResource(R.string.or_text),
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.DarkGray)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onGoogleSignIn,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.icons8_google_logo_48),
                contentDescription = "Google Logo",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.google_signin),
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.terms_privacy),
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}

@Composable
fun authTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = Gold,
    unfocusedBorderColor = Color.Gray,
    focusedLabelColor = Gold,
    unfocusedLabelColor = Color.Gray
)
