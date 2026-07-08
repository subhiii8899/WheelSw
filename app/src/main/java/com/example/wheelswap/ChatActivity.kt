package com.example.wheelswap

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

data class Message(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val status: String = "sent", // "sent", "read"
    val timestamp: Long = 0L
)

class ChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageHelper.applyLanguage(this)

        val receiverId = intent.getStringExtra("receiverId") ?: ""
        val receiverName = intent.getStringExtra("receiverName") ?: "User"
        val receiverPhoto = intent.getStringExtra("receiverPhoto") ?: ""
        val listingName = intent.getStringExtra("listingName") ?: ""

        setContent {
            ChatScreen(receiverId, receiverName, receiverPhoto, listingName, onBack = { finish() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(receiverId: String, receiverName: String, receiverPhoto: String, listingName: String, onBack: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val senderId = auth.currentUser?.uid ?: ""
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val chatId = if (senderId < receiverId) "${senderId}_$receiverId" else "${receiverId}_$senderId"
    
    var messages by remember { mutableStateOf(listOf<Message>()) }
    var textValue by remember { mutableStateOf("") }
    var senderName by remember { mutableStateOf("User") }
    var senderPhoto by remember { mutableStateOf("") }

    LaunchedEffect(senderId) {
        if (senderId.isNotEmpty()) {
            db.collection("users").document(senderId).get().addOnSuccessListener { doc ->
                val first = doc.getString("firstName") ?: ""
                val last = doc.getString("lastName") ?: ""
                senderName = "$first $last".trim().ifEmpty { "User" }
                senderPhoto = doc.getString("profileImageUrl") ?: ""
            }
        }
    }

    LaunchedEffect(chatId) {
        db.collection("chats").document(chatId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    messages = snapshot.documents.map { doc ->
                        Message(
                            id = doc.id,
                            senderId = doc.getString("senderId") ?: "",
                            text = doc.getString("text") ?: "",
                            status = doc.getString("status") ?: "sent",
                            timestamp = doc.getLong("timestamp") ?: 0L
                        )
                    }
                    
                    // Mark incoming messages as read
                    snapshot.documents.forEach { doc ->
                        if (doc.getString("senderId") != senderId && doc.getString("status") != "read") {
                            db.collection("chats").document(chatId).collection("messages").document(doc.id)
                                .update("status", "read")
                        }
                    }
                }
            }
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            val intent = Intent(context, UserPublicProfileActivity::class.java).apply {
                                putExtra("userId", receiverId)
                                putExtra("userName", receiverName)
                                putExtra("userPhoto", receiverPhoto)
                            }
                            context.startActivity(intent)
                        }
                    ) {
                        if (receiverPhoto.isNotEmpty()) {
                            AsyncImage(
                                model = receiverPhoto,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(18.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        Column {
                            Text(text = receiverName, color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            if (listingName.isNotEmpty()) {
                                Text(text = "Re: $listingName", color = Color.Gray, fontSize = 10.sp)
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Gold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        },
        bottomBar = {
            Surface(color = CardBg, tonalElevation = 4.dp) {
                Row(
                    modifier = Modifier.padding(12.dp).navigationBarsPadding().imePadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textValue,
                        onValueChange = { textValue = it },
                        placeholder = { Text("Type a message...", color = Color.Gray) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Gold,
                            unfocusedBorderColor = Color.DarkGray
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (textValue.isNotBlank()) {
                                val msgData = hashMapOf(
                                    "senderId" to senderId,
                                    "text" to textValue,
                                    "status" to "sent",
                                    "timestamp" to System.currentTimeMillis()
                                )
                                db.collection("chats").document(chatId).collection("messages").add(msgData)
                                
                                // Update chat metadata
                                val metadata = hashMapOf(
                                    "lastMessage" to textValue,
                                    "lastTimestamp" to msgData["timestamp"],
                                    "users" to listOf(senderId, receiverId)
                                )
                                
                                // Store user specific info so it doesn't get lost on logout
                                metadata["name_$senderId"] = senderName
                                metadata["photo_$senderId"] = senderPhoto
                                metadata["name_$receiverId"] = receiverName
                                metadata["photo_$receiverId"] = receiverPhoto

                                db.collection("chats").document(chatId).set(metadata, com.google.firebase.firestore.SetOptions.merge())
                                textValue = ""
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Gold)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = DarkBg)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            reverseLayout = false
        ) {
            items(messages) { message ->
                val isMine = message.senderId == senderId
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    contentAlignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Surface(
                        color = if (isMine) Gold else Color(0xFF2C2C2C),
                        shape = RoundedCornerShape(
                            topStart = 16.dp, topEnd = 16.dp,
                            bottomStart = if (isMine) 16.dp else 0.dp,
                            bottomEnd = if (isMine) 0.dp else 16.dp
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = message.text, color = if (isMine) Color.Black else Color.White)
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(Alignment.End)) {
                                Text(
                                    text = formatTime(message.timestamp),
                                    color = if (isMine) Color.Black.copy(alpha = 0.5f) else Color.Gray,
                                    fontSize = 8.sp
                                )
                                if (isMine) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    val tint = if (message.status == "read") Color(0xFF2E7D32) else Color.LightGray.copy(alpha = 0.5f)
                                    // Using two checks to simulate double tick
                                    Box {
                                        Icon(Icons.Default.Check, null, tint = tint, modifier = Modifier.size(10.dp))
                                        Icon(Icons.Default.Check, null, tint = tint, modifier = Modifier.size(10.dp).offset(x = 4.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
