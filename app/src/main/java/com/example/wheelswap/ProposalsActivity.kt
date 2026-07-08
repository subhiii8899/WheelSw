package com.example.wheelswap

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class Proposal(
    val id: String = "",
    val listingId: String = "",
    val listingName: String = "",
    val sellerId: String = "",
    val sellerName: String = "",
    val sellerProfilePic: String = "",
    val buyerId: String = "",
    val buyerName: String = "",
    val buyerProfilePic: String = "",
    val buyerContact: String = "",
    val vehicleModel: String = "",
    val vehicleYear: String = "",
    val vehicleKm: String = "",
    val vehicleCondition: String = "",
    val vehicleImageUrl: String = "",
    val status: String = "Pending",
    val timestamp: Long = 0L
)

class ProposalsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageHelper.applyLanguage(this)
        setContent {
            ProposalsScreen(onBack = { finish() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProposalsScreen(onBack: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid ?: ""
    val context = LocalContext.current

    var receivedProposals by remember { mutableStateOf(listOf<Proposal>()) }
    var sentProposals by remember { mutableStateOf(listOf<Proposal>()) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            // Received Proposals
            db.collection("proposals")
                .whereEqualTo("sellerId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        receivedProposals = snapshot.documents.map { doc ->
                            Proposal(
                                id = doc.id,
                                listingId = doc.getString("listingId") ?: "",
                                listingName = doc.getString("listingName") ?: "",
                                sellerId = doc.getString("sellerId") ?: "",
                                sellerName = doc.getString("sellerName") ?: "Seller",
                                sellerProfilePic = doc.getString("sellerProfilePic") ?: "",
                                buyerId = doc.getString("buyerId") ?: "",
                                buyerName = doc.getString("buyerName") ?: "",
                                buyerProfilePic = doc.getString("buyerProfilePic") ?: "",
                                buyerContact = doc.getString("buyerContact") ?: "",
                                vehicleModel = doc.getString("vehicleModel") ?: "",
                                vehicleYear = doc.getString("vehicleYear") ?: "",
                                vehicleKm = doc.getString("vehicleKm") ?: "",
                                vehicleCondition = doc.getString("vehicleCondition") ?: "",
                                vehicleImageUrl = doc.getString("vehicleImageUrl") ?: "",
                                status = doc.getString("status") ?: "Pending",
                                timestamp = doc.getLong("timestamp") ?: 0L
                            )
                        }
                    }
                }

            // Sent Proposals
            db.collection("proposals")
                .whereEqualTo("buyerId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        sentProposals = snapshot.documents.map { doc ->
                            Proposal(
                                id = doc.id,
                                listingId = doc.getString("listingId") ?: "",
                                listingName = doc.getString("listingName") ?: "",
                                sellerId = doc.getString("sellerId") ?: "",
                                sellerName = doc.getString("sellerName") ?: "Seller",
                                sellerProfilePic = doc.getString("sellerProfilePic") ?: "",
                                buyerId = doc.getString("buyerId") ?: "",
                                buyerName = doc.getString("buyerName") ?: "",
                                buyerProfilePic = doc.getString("buyerProfilePic") ?: "",
                                buyerContact = doc.getString("buyerContact") ?: "",
                                vehicleModel = doc.getString("vehicleModel") ?: "",
                                vehicleYear = doc.getString("vehicleYear") ?: "",
                                vehicleKm = doc.getString("vehicleKm") ?: "",
                                vehicleCondition = doc.getString("vehicleCondition") ?: "",
                                vehicleImageUrl = doc.getString("vehicleImageUrl") ?: "",
                                status = doc.getString("status") ?: "Pending",
                                timestamp = doc.getLong("timestamp") ?: 0L
                            )
                        }
                    }
                    isLoading = false
                }
        }
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            Column(modifier = Modifier.background(DarkBg)) {
                TopAppBar(
                    title = { Text("Swap Proposals 🔄", color = Gold, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Gold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = DarkBg,
                    contentColor = Gold,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Gold
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Received", color = if (selectedTab == 0) Gold else Color.Gray) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Sent", color = if (selectedTab == 1) Gold else Color.Gray) }
                    )
                }
            }
        }
    ) { padding ->
        val currentList = if (selectedTab == 0) receivedProposals else sentProposals
        
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Gold)
            }
        } else if (currentList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(if (selectedTab == 0) "No proposals received." else "You haven't sent any proposals.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(currentList) { proposal ->
                    ProposalCard(proposal, isReceived = selectedTab == 0) { newStatus ->
                        db.collection("proposals").document(proposal.id).update("status", newStatus)
                        
                        if (newStatus == "Accepted" || newStatus == "Rejected") {
                            sendStatusNotification(context, proposal.listingName, newStatus)
                        }
                    }
                }
            }
        }
    }
}

fun sendStatusNotification(context: Context, listingName: String, status: String) {
    val channelId = "proposals_notifications"
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, "Proposals", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)
    }

    val intent = Intent(context, ProposalsActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.app_logo)
        .setContentTitle("Proposal $status")
        .setContentText("Your proposal for $listingName has been $status. Wanna chat now?")
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)

    notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
}

@Composable
fun ProposalCard(proposal: Proposal, isReceived: Boolean, onStatusChange: (String) -> Unit) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (proposal.status == "Pending") Gold.copy(alpha = 0.3f) else Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isReceived) {
                    if (proposal.buyerProfilePic.isNotEmpty()) {
                        AsyncImage(
                            model = proposal.buyerProfilePic,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                    }
                } else {
                    Icon(Icons.Default.Home, contentDescription = null, tint = Gold, modifier = Modifier.size(40.dp))
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    if (isReceived) {
                        Text(text = proposal.buyerName, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(text = "wants to swap for your ${proposal.listingName}", color = Color.Gray, fontSize = 12.sp)
                    } else {
                        Text(text = "Your offer for ${proposal.listingName}", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(text = "Sent to seller", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Surface(
                color = Color.Black.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (proposal.vehicleImageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = proposal.vehicleImageUrl,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Column {
                        Text(if (isReceived) "Offered Vehicle:" else "Your Offered Vehicle:", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("${proposal.vehicleYear} ${proposal.vehicleModel}", color = Color.White, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Mileage: ${proposal.vehicleKm} km", color = Color.Gray, fontSize = 12.sp)
                            Text("Condition: ${proposal.vehicleCondition}", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chat button always visible for every proposal to discuss details
            Button(
                onClick = {
                    val receiverId = if (isReceived) proposal.buyerId else proposal.sellerId
                    val receiverName = if (isReceived) proposal.buyerName else proposal.sellerName
                    val receiverPhoto = if (isReceived) proposal.buyerProfilePic else proposal.sellerProfilePic
                    
                    val intent = Intent(context, ChatActivity::class.java).apply {
                        putExtra("receiverId", receiverId)
                        putExtra("receiverName", receiverName)
                        putExtra("receiverPhoto", receiverPhoto)
                        putExtra("listingName", proposal.listingName)
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth().height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
            ) {
                Text("Chat to discuss details 💬", color = Color.White, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (proposal.status == "Pending" && isReceived) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onStatusChange("Rejected") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject", color = Color.Red)
                    }
                    Button(
                        onClick = { onStatusChange("Accepted") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Green, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Accept", color = Color.Green)
                    }
                }
            } else {
                Surface(
                    color = when(proposal.status) {
                        "Accepted" -> Color.Green.copy(alpha = 0.1f)
                        "Rejected" -> Color.Red.copy(alpha = 0.1f)
                        else -> Color.Gray.copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = if (isReceived) "You ${proposal.status}" else "Seller Status: ${proposal.status}",
                            color = when(proposal.status) {
                                "Accepted" -> Color.Green
                                "Rejected" -> Color.Red
                                else -> Color.Gray
                            },
                            fontWeight = FontWeight.Bold
                        )
                        
                        if (proposal.status == "Accepted" && isReceived && proposal.buyerContact.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val message = "Hello! I accepted your swap proposal for ${proposal.listingName}. Let's discuss further."
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse("https://api.whatsapp.com/send?phone=${proposal.buyerContact}&text=${Uri.encode(message)}")
                                    }
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Contact on WhatsApp", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
