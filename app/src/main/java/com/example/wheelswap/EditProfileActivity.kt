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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalContext
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

class EditProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageHelper.applyLanguage(this)

        // Initialize MediaManager if not already done
        try {
            MediaManager.init(this, mapOf("cloud_name" to "droixs6gx"))
        } catch (e: Exception) {}

        setContent {
            EditProfileScreen(onBack = { finish() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(onBack: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val user = auth.currentUser
    val context = LocalContext.current

    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var currentProfileUrl by remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (user != null) {
            db.collection("users").document(user.uid).get().addOnSuccessListener { doc ->
                firstName = doc.getString("firstName") ?: ""
                lastName = doc.getString("lastName") ?: ""
                currentProfileUrl = doc.getString("profileImageUrl") ?: ""
                isLoading = false
            }
        }
    }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> selectedUri = uri }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", color = Gold, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Gold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Gold)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Image
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(CardBg)
                        .border(2.dp, Gold, CircleShape)
                        .clickable { photoLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedUri != null) {
                        AsyncImage(model = selectedUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else if (currentProfileUrl.isNotEmpty()) {
                        AsyncImage(model = currentProfileUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
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
                    colors = editFieldColors()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = editFieldColors()
                )

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = {
                        if (firstName.isBlank() || lastName.isBlank()) return@Button
                        isSaving = true

                        if (selectedUri != null) {
                            MediaManager.get().upload(selectedUri)
                                .option("folder", "profiles")
                                .option("unsigned", true)
                                .option("upload_preset", "wheelswap_preset")
                                .callback(object : UploadCallback {
                                    override fun onStart(requestId: String?) {}
                                    override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                                        val url = resultData?.get("secure_url") as? String
                                        updateUserProfile(db, user?.uid ?: "", firstName, lastName, url ?: "", onBack)
                                    }
                                    override fun onError(requestId: String?, error: ErrorInfo?) {
                                        updateUserProfile(db, user?.uid ?: "", firstName, lastName, currentProfileUrl, onBack)
                                    }
                                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                                }).dispatch()
                        } else {
                            updateUserProfile(db, user?.uid ?: "", firstName, lastName, currentProfileUrl, onBack)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = DarkBg, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Save Changes", color = DarkBg, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

fun updateUserProfile(db: FirebaseFirestore, uid: String, first: String, last: String, url: String, onComplete: () -> Unit) {
    val data = mapOf(
        "firstName" to first,
        "lastName" to last,
        "profileImageUrl" to url
    )
    db.collection("users").document(uid).update(data).addOnSuccessListener {
        // Also update all user listings to keep them in sync
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
fun editFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = Gold,
    unfocusedBorderColor = Color.Gray,
    focusedLabelColor = Gold,
    unfocusedLabelColor = Color.Gray
)
