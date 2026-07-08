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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
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

data class ChatRoom(
    val id: String = "",
    val lastMessage: String = "",
    val lastTimestamp: Long = 0L,
    val users: List<String> = emptyList(),
    val receiverName: String = "",
    val receiverPhoto: String = "",
    val hasUnread: Boolean = false
)

class ConversationsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageHelper.applyLanguage(this)
        setContent {
            ConversationsScreen(onBack = { finish() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsScreen(onBack: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    var chatRooms by remember { mutableStateOf(listOf<ChatRoom>()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            db.collection("chats")
                .whereArrayContains("users", userId)
                .orderBy("lastTimestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        val rooms = snapshot.documents.map { doc ->
                            val users = doc.get("users") as? List<String> ?: emptyList()
                            val otherId = users.firstOrNull { it != userId } ?: ""
                            
                            ChatRoom(
                                id = doc.id,
                                lastMessage = doc.getString("lastMessage") ?: "",
                                lastTimestamp = doc.getLong("lastTimestamp") ?: 0L,
                                users = users,
                                receiverName = doc.getString("name_$otherId") ?: "User",
                                receiverPhoto = doc.getString("photo_$otherId") ?: "",
                                hasUnread = false // We'll calculate this separately or use a flag
                            )
                        }
                        chatRooms = rooms
                        
                        // Check for unread messages in each room
                        rooms.forEachIndexed { index, room ->
                            db.collection("chats").document(room.id).collection("messages")
                                .whereEqualTo("status", "sent")
                                .get()
                                .addOnSuccessListener { msgSnapshot ->
                                    val containsIncomingUnread = msgSnapshot.documents.any { it.getString("senderId") != userId }
                                    if (containsIncomingUnread) {
                                        chatRooms = chatRooms.toMutableList().apply {
                                            this[index] = this[index].copy(hasUnread = true)
                                        }
                                    }
                                }
                        }
                    }
                    isLoading = false
                }
        }
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Messages 💬", color = Gold, fontWeight = FontWeight.Bold) },
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
        } else if (chatRooms.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No messages yet.", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                items(chatRooms) { room ->
                    ChatRoomItem(room, userId)
                }
            }
        }
    }
}

@Composable
fun ChatRoomItem(room: ChatRoom, currentUserId: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val receiverId = room.users.first { it != currentUserId }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(context, ChatActivity::class.java).apply {
                    putExtra("receiverId", receiverId)
                    putExtra("receiverName", room.receiverName)
                    putExtra("receiverPhoto", room.receiverPhoto)
                }
                context.startActivity(intent)
            }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                if (room.receiverPhoto.isNotEmpty()) {
                    AsyncImage(
                        model = room.receiverPhoto,
                        contentDescription = null,
                        modifier = Modifier.size(50.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.DarkGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = room.receiverName.firstOrNull()?.toString() ?: "U", color = Gold, fontWeight = FontWeight.Bold)
                    }
                }
                
                if (room.hasUnread) {
                    Surface(
                        modifier = Modifier.size(12.dp).align(Alignment.TopEnd),
                        color = Color.Red,
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(2.dp, CardBg)
                    ) {}
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = room.receiverName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    text = room.lastMessage, 
                    color = if (room.hasUnread) Gold else Color.Gray, 
                    fontSize = 14.sp, 
                    maxLines = 1,
                    fontWeight = if (room.hasUnread) FontWeight.Bold else FontWeight.Normal
                )
            }
            
            Text(text = formatTime(room.lastTimestamp), color = Color.Gray, fontSize = 10.sp)
        }
    }
}
