package com.example.wheelswap

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Intent

class ProfileSetupActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageHelper.applyLanguage(this)

        // Initialize MediaManager if not already done
        try {
            MediaManager.init(this, mapOf("cloud_name" to "droixs6gx"))
        } catch (e: Exception) {}

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            finish()
            return
        }

        setContent {
            ProfileSetupScreen(
                onComplete = {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(onComplete: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val user = auth.currentUser

    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var profileUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by rememberSaveable { mutableStateOf(false) }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> profileUri = uri }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Complete Your Profile",
            color = Gold,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Tell us who you are",
            color = Color.Gray,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Profile Picture Picker
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(CardBg)
                .border(2.dp, Gold, CircleShape)
                .clickable { photoLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (profileUri != null) {
                AsyncImage(
                    model = profileUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Gold, modifier = Modifier.size(40.dp))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = setupFieldColors()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = setupFieldColors()
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                if (firstName.isBlank() || lastName.isBlank()) return@Button
                isUploading = true

                if (profileUri != null) {
                    try {
                        MediaManager.get().upload(profileUri)
                            .option("folder", "profiles")
                            .option("unsigned", true)
                            .option("upload_preset", "wheelswap_preset")
                            .callback(object : UploadCallback {
                                override fun onStart(requestId: String?) {}
                                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                                    val url = resultData?.get("secure_url") as? String
                                    saveUser(db, user?.uid ?: "", firstName, lastName, url ?: "", onComplete)
                                }
                                override fun onError(requestId: String?, error: ErrorInfo?) {
                                    saveUser(db, user?.uid ?: "", firstName, lastName, "", onComplete)
                                }
                                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                            }).dispatch()
                    } catch (e: Exception) {
                        saveUser(db, user?.uid ?: "", firstName, lastName, "", onComplete)
                    }
                } else {
                    saveUser(db, user?.uid ?: "", firstName, lastName, "", onComplete)
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Gold),
            shape = RoundedCornerShape(12.dp),
            enabled = !isUploading && firstName.isNotBlank() && lastName.isNotBlank()
        ) {
            if (isUploading) {
                CircularProgressIndicator(color = DarkBg, modifier = Modifier.size(24.dp))
            } else {
                Text("Start Swapping", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

fun saveUser(db: FirebaseFirestore, uid: String, first: String, last: String, url: String, onComplete: () -> Unit) {
    val data = mapOf(
        "firstName" to first,
        "lastName" to last,
        "profileImageUrl" to url,
        "uid" to uid
    )
    db.collection("users").document(uid).set(data).addOnSuccessListener {
        // Update any existing listings if they exist (rare for new setup but good for data integrity)
        db.collection("listings").whereEqualTo("userId", uid).get().addOnSuccessListener { snapshot ->
            val batch = db.batch()
            for (doc in snapshot.documents) {
                batch.update(doc.reference, mapOf(
                    "sellerName" to "$first $last",
                    "sellerProfilePic" to url
                ))
            }
            batch.commit().addOnCompleteListener { onComplete() }
        }.addOnFailureListener { onComplete() }
    }.addOnFailureListener { onComplete() }
}

@Composable
fun setupFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = Gold,
    unfocusedBorderColor = Color.Gray,
    focusedLabelColor = Gold,
    unfocusedLabelColor = Color.Gray
)
