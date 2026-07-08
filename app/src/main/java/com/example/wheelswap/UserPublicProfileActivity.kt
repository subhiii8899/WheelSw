package com.example.wheelswap

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class UserPublicProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageHelper.applyLanguage(this)

        val targetUserId = intent.getStringExtra("userId") ?: ""
        val targetUserName = intent.getStringExtra("userName") ?: "User"
        val targetUserPhoto = intent.getStringExtra("userPhoto") ?: ""

        setContent {
            UserPublicProfileScreen(targetUserId, targetUserName, targetUserPhoto, onBack = { finish() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPublicProfileScreen(userId: String, userName: String, userPhoto: String, onBack: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    var userListings by remember { mutableStateOf(listOf<Vehicle>()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            db.collection("listings")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { snapshot ->
                    userListings = snapshot.documents.mapNotNull { doc ->
                        val vehicleType = doc.getString("type") ?: "Car"
                        Vehicle(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            price = doc.getString("price") ?: "",
                            location = doc.getString("location") ?: "",
                            km = doc.getString("km") ?: "",
                            type = vehicleType,
                            emoji = doc.getString("emoji") ?: "🚗",
                            year = doc.getString("year") ?: "",
                            contact = doc.getString("contact") ?: "",
                            userId = doc.getString("userId") ?: "",
                            isSwapAvailable = doc.getBoolean("isSwapAvailable") ?: false,
                            isVerified = doc.getBoolean("isVerified") ?: false,
                            healthScore = doc.getLong("healthScore")?.toInt() ?: 0,
                            modifications = doc.getString("modifications") ?: "",
                            fuelType = doc.getString("fuelType") ?: (if (vehicleType == "Car") "Petrol" else ""),
                            views = doc.getLong("views")?.toInt() ?: 0,
                            sellerName = doc.getString("sellerName") ?: "User",
                            sellerProfilePic = doc.getString("sellerProfilePic") ?: "",
                            imageUrls = doc.get("imageUrls") as? List<String> ?: emptyList(),
                            expiryTimestamp = doc.getLong("expiryTimestamp") ?: 0L
                        )
                    }.sortedByDescending { it.id } // Manual sort to avoid index requirement
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Seller Profile", color = Gold, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Gold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBg)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (userPhoto.isNotEmpty()) {
                    AsyncImage(
                        model = userPhoto,
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .border(2.dp, Gold, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.DarkGray)
                            .border(2.dp, Gold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = userName.firstOrNull()?.toString() ?: "U", color = Gold, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = userName, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                Text(text = "Active Listings", color = Color.Gray, fontSize = 14.sp)
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Gold)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(userListings) { vehicle ->
                        val context = LocalContext.current
                        VehicleCard(
                            vehicle = vehicle,
                            isOwner = false, // Viewed by others
                            isSaved = false, // Simplified for profile view
                            onToggleSave = {},
                            onDelete = {},
                            onEdit = {},
                            onClick = {
                                val intent = Intent(context, DetailScreen::class.java).apply {
                                    putExtra("id", vehicle.id)
                                    putExtra("name", vehicle.name)
                                    putExtra("price", vehicle.price)
                                    putExtra("location", vehicle.location)
                                    putExtra("km", vehicle.km)
                                    putExtra("year", vehicle.year)
                                    putExtra("emoji", vehicle.emoji)
                                    putExtra("type", vehicle.type)
                                    putExtra("contact", vehicle.contact)
                                    putExtra("isSwapAvailable", vehicle.isSwapAvailable)
                                    putExtra("healthScore", vehicle.healthScore)
                                    putExtra("modifications", vehicle.modifications)
                                    putExtra("sellerName", vehicle.sellerName)
                                    putExtra("sellerProfilePic", vehicle.sellerProfilePic)
                                    putExtra("sellerId", vehicle.userId)
                                    putExtra("isVerified", vehicle.isVerified)
                                    putStringArrayListExtra("imageUrls", ArrayList(vehicle.imageUrls))
                                }
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }
}
