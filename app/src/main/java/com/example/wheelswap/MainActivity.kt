package com.example.wheelswap

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val Gold = Color(0xFFFFD700)
val DarkBg = Color(0xFF121212)
val CardBg = Color(0xFF1E1E1E)

data class Vehicle(
    val id: String = "",
    val name: String,
    val price: String,
    val location: String,
    val km: String,
    val type: String,
    val emoji: String,
    val year: String,
    val contact: String = "",
    val userId: String = "",
    val isSwapAvailable: Boolean = false,
    val healthScore: Int = 0,
    val modifications: String = "",
    val fuelType: String = "",
    val views: Int = 0,
    val isPaid: Boolean = false,
    val isVerified: Boolean = false,
    val sellerName: String = "",
    val sellerProfilePic: String = "",
    val imageUrls: List<String> = emptyList(),
    val expiryTimestamp: Long = 0L
)

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("WheelSwap", "Notification permission granted")
        } else {
            Log.d("WheelSwap", "Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageHelper.applyLanguage(this)
        askNotificationPermission()
        setContent {
            HomeScreen()
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // Already granted
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        val prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val currentLang = prefs.getString("Language", "en") ?: "en"
        val systemLocale = resources.configuration.locales[0].language
        
        if (currentLang != systemLocale) {
            recreate()
        } else {
            // Force a refresh of the Home screen to update profile details if changed
            setContent { HomeScreen() }
        }
    }
}

@Preview(showBackground = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val auth = if (isPreview) null else FirebaseAuth.getInstance()
    val db = if (isPreview) null else FirebaseFirestore.getInstance()

    var selectedFilter by rememberSaveable { mutableStateOf("All") }
    var citySearchQuery by rememberSaveable { mutableStateOf("") }
    var dynamicVehicleList by remember { mutableStateOf(listOf<Vehicle>()) }
    var isLoading by rememberSaveable { mutableStateOf(true) }
    var showProfileMenu by rememberSaveable { mutableStateOf(false) }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var hasUnreadMessages by remember { mutableStateOf(false) }

    // Listen for global unread messages
    LaunchedEffect(auth?.currentUser?.uid) {
        val uid = auth?.currentUser?.uid
        if (uid != null) {
            db?.collection("chats")
                ?.whereArrayContains("users", uid)
                ?.addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        var foundUnread = false
                        var checkCount = 0
                        val totalRooms = snapshot.documents.size
                        
                        if (totalRooms == 0) hasUnreadMessages = false
                        
                        snapshot.documents.forEach { roomDoc ->
                            db.collection("chats").document(roomDoc.id).collection("messages")
                                .whereEqualTo("status", "sent")
                                .get()
                                .addOnSuccessListener { msgSnapshot ->
                                    if (msgSnapshot.documents.any { it.getString("senderId") != uid }) {
                                        foundUnread = true
                                    }
                                    checkCount++
                                    if (checkCount == totalRooms || foundUnread) {
                                        hasUnreadMessages = foundUnread
                                    }
                                }
                        }
                    }
                }
        }
    }

    var savedListingIds by remember { mutableStateOf(setOf<String>()) }
    var currentUserProfilePic by remember { mutableStateOf<String?>(null) }

    // Fetch Saved Listings
    LaunchedEffect(auth?.currentUser?.uid) {
        val uid = auth?.currentUser?.uid
        if (uid != null) {
            db?.collection("users")?.document(uid)?.collection("saved")
                ?.addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        savedListingIds = snapshot.documents.map { it.id }.toSet()
                    }
                }
        }
    }

    // Fetch Current User Details
    LaunchedEffect(auth?.currentUser?.uid) {
        val uid = auth?.currentUser?.uid
        if (uid != null) {
            db?.collection("users")?.document(uid)?.get()?.addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val firstName = doc.getString("firstName")
                    if (firstName.isNullOrBlank()) {
                        // Profile incomplete, redirect to setup
                        context.startActivity(Intent(context, ProfileSetupActivity::class.java))
                        (context as? ComponentActivity)?.finish()
                    } else {
                        currentUserProfilePic = doc.getString("profileImageUrl")
                    }
                } else {
                    // User doc doesn't exist yet, redirect to setup
                    context.startActivity(Intent(context, ProfileSetupActivity::class.java))
                    (context as? ComponentActivity)?.finish()
                }
            }
        }
    }

    // Fetch listings from Firestore
    LaunchedEffect(Unit) {
        if (isPreview) {
            dynamicVehicleList = listOf(
                Vehicle(id = "1", name = "Civic Turbo RS", price = "PKR 85.5 Lac", location = "Lahore", km = "15,000 km", type = "Car", emoji = "🏎️", year = "2022", isSwapAvailable = true, healthScore = 9),
                Vehicle(id = "2", name = "Honda CD 70", price = "PKR 1.5 Lac", location = "Karachi", km = "5,000 km", type = "Bike", emoji = "🏍️", year = "2023", healthScore = 8)
            )
            isLoading = false
            return@LaunchedEffect
        }

        db?.collection("listings")
            ?.orderBy("timestamp", Query.Direction.DESCENDING)
            ?.addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { doc ->
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
                            healthScore = doc.getLong("healthScore")?.toInt() ?: 0,
                            modifications = doc.getString("modifications") ?: "",
                            fuelType = doc.getString("fuelType") ?: (if (vehicleType == "Car") "Petrol" else ""),
                            views = doc.getLong("views")?.toInt() ?: 0,
                            isPaid = doc.getBoolean("isPaid") ?: false,
                            isVerified = doc.getBoolean("isVerified") ?: false,
                            sellerName = doc.getString("sellerName") ?: "User",
                            sellerProfilePic = doc.getString("sellerProfilePic") ?: "",
                            imageUrls = doc.get("imageUrls") as? List<String> ?: emptyList(),
                            expiryTimestamp = doc.getLong("expiryTimestamp") ?: 0L
                        )
                    }
                    dynamicVehicleList = items
                    isLoading = false
                }
            }
    }

    val filtered = dynamicVehicleList.filter {
        val matchesTab = when (selectedTab) {
            1 -> it.userId == auth?.currentUser?.uid
            2 -> savedListingIds.contains(it.id)
            else -> true
        }
        
        // Location Filter Logic
        val matchesLocation = if (citySearchQuery.isEmpty() || citySearchQuery.lowercase() == "all pakistan") {
            true
        } else {
            it.location.contains(citySearchQuery, ignoreCase = true)
        }

        val isNotExpired = if (selectedTab == 0) {
            val timeNotExpired = (it.expiryTimestamp == 0L || it.expiryTimestamp > System.currentTimeMillis())
            val freeViewsNotExceeded = if (!it.isPaid) it.views < 700 else true
            timeNotExpired && freeViewsNotExceeded
        } else true

        matchesTab && 
                matchesLocation &&
                isNotExpired &&
                (selectedFilter == "All" || it.type == selectedFilter)
    }.sortedWith(compareByDescending<Vehicle> { it.views }.thenByDescending { it.id }) // Rank by engagement, then freshness (id as proxy)

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = null,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.app_name),
                                color = Gold,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.app_tagline),
                                color = Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showProfileMenu = true }) {
                            Box {
                                if (!currentUserProfilePic.isNullOrBlank()) {
                                    AsyncImage(
                                        model = currentUserProfilePic,
                                        contentDescription = stringResource(R.string.profile),
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .border(1.dp, Gold, CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = stringResource(R.string.profile),
                                        tint = Gold,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                
                                if (hasUnreadMessages) {
                                    Surface(
                                        modifier = Modifier.size(10.dp).align(Alignment.TopEnd),
                                        color = Color.Red,
                                        shape = CircleShape,
                                        border = androidx.compose.foundation.BorderStroke(1.5.dp, DarkBg)
                                    ) {}
                                }
                            }
                        }
                        DropdownMenu(
                            expanded = showProfileMenu,
                            onDismissRequest = { showProfileMenu = false },
                            modifier = Modifier.background(CardBg)
                        ) {
                            Text(
                                text = auth?.currentUser?.email ?: "User",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(16.dp)
                            )
                            HorizontalDivider(color = Color.DarkGray)
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.app_settings), color = Color.White) },
                                onClick = {
                                    showProfileMenu = false
                                    context.startActivity(Intent(context, SettingsActivity::class.java))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.safety_tips), color = Color.White) },
                                onClick = {
                                    showProfileMenu = false
                                    context.startActivity(Intent(context, SafetyTipsActivity::class.java))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.feedback), color = Color.White) },
                                onClick = {
                                    showProfileMenu = false
                                    context.startActivity(Intent(context, FeedbackActivity::class.java))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Swap Proposals 🔄", color = Color.White) },
                                onClick = {
                                    showProfileMenu = false
                                    context.startActivity(Intent(context, ProposalsActivity::class.java))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Messages 💬", color = Color.White) },
                                onClick = {
                                    showProfileMenu = false
                                    context.startActivity(Intent(context, ConversationsActivity::class.java))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.about_app), color = Color.White) },
                                onClick = {
                                    showProfileMenu = false
                                    context.startActivity(Intent(context, AboutActivity::class.java))
                                }
                            )
                            HorizontalDivider(color = Color.DarkGray)
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.logout), color = Color.Red) },
                                onClick = {
                                    showProfileMenu = false
                                    auth?.signOut()
                                    context.startActivity(Intent(context, LoginScreen::class.java))
                                    (context as? ComponentActivity)?.finish()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBg,
                    titleContentColor = Gold
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CardBg,
                contentColor = Gold,
                tonalElevation = 8.dp,
                modifier = Modifier.height(64.dp)
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text(stringResource(R.string.home)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkBg,
                        selectedTextColor = Gold,
                        indicatorColor = Gold,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text(stringResource(R.string.my_inventory)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkBg,
                        selectedTextColor = Gold,
                        indicatorColor = Gold,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(if (selectedTab == 2) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null) },
                    label = { Text("Saved") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkBg,
                        selectedTextColor = Gold,
                        indicatorColor = Gold,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    context.startActivity(Intent(context, SellActivity::class.java))
                },
                containerColor = Gold,
                contentColor = DarkBg,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.sell), fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ── City Filter (Search Bar) ──
            OutlinedTextField(
                value = citySearchQuery,
                onValueChange = { citySearchQuery = it },
                placeholder = { Text(stringResource(R.string.select_city), color = Color.Gray) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search, 
                        contentDescription = "Search City", 
                        tint = Gold,
                        modifier = Modifier.size(24.dp)
                    )
                },
                trailingIcon = {
                    if (citySearchQuery.isNotEmpty()) {
                        IconButton(onClick = { citySearchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear City", tint = Color.Gray)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Search
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = Color.DarkGray,
                    focusedContainerColor = CardBg,
                    unfocusedContainerColor = CardBg,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Gold
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Filter Chips (Type) ──
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    stringResource(R.string.filter_all) to "All",
                    stringResource(R.string.filter_car) to "Car",
                    stringResource(R.string.filter_bike) to "Bike"
                ).forEach { (display, filterValue) ->
                    val isSelected = selectedFilter == filterValue
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filterValue },
                        label = { Text(display) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Gold,
                            selectedLabelColor = DarkBg,
                            containerColor = CardBg,
                            labelColor = Gold
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) Gold else Color.Transparent
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Results Info ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isLoading) stringResource(R.string.loading_listings) else stringResource(R.string.recommended_for_you),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.items_count, filtered.size),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Vehicle List ──
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    scope.launch {
                        // Small delay to simulate refresh and allow UI to settle
                        delay(1000)
                        isRefreshing = false
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Gold)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 80.dp), // Space for FAB
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filtered) { vehicle ->
                            val isSaved = savedListingIds.contains(vehicle.id)
                            VehicleCard(
                                vehicle = vehicle,
                                isOwner = vehicle.userId == auth?.currentUser?.uid,
                                isSaved = isSaved,
                                onToggleSave = {
                                    val uid = auth?.currentUser?.uid
                                    if (uid != null) {
                                        val ref = db?.collection("users")?.document(uid)?.collection("saved")?.document(vehicle.id)
                                        if (isSaved) ref?.delete() else ref?.set(mapOf("timestamp" to System.currentTimeMillis()))
                                    }
                                },
                                onDelete = {
                                    db?.collection("listings")?.document(vehicle.id)?.delete()
                                },
                                onEdit = {
                                    val intent = Intent(context, SellActivity::class.java).apply {
                                        putExtra("isEdit", true)
                                        putExtra("id", vehicle.id)
                                        putExtra("name", vehicle.name)
                                        putExtra("price", vehicle.price)
                                        putExtra("location", vehicle.location)
                                        putExtra("km", vehicle.km)
                                        putExtra("year", vehicle.year)
                                        putExtra("type", vehicle.type)
                                        putExtra("fuelType", vehicle.fuelType)
                                        putExtra("contact", vehicle.contact)
                                        putExtra("modifications", vehicle.modifications)
                                        putStringArrayListExtra("imageUrls", ArrayList(vehicle.imageUrls))
                                    }
                                    context.startActivity(intent)
                                },
                                onClick = {
                                    // Increment view count in Firestore
                                    db?.collection("listings")?.document(vehicle.id)
                                        ?.update("views", com.google.firebase.firestore.FieldValue.increment(1))

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
}

@Composable
fun VehicleCard(
    vehicle: Vehicle,
    isOwner: Boolean,
    isSaved: Boolean,
    onToggleSave: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Image Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color.DarkGray)
            ) {
                if (vehicle.imageUrls.isNotEmpty()) {
                    AsyncImage(
                        model = vehicle.imageUrls.first(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = vehicle.emoji, fontSize = 60.sp)
                    }
                }

                // Three Dots Menu (Only for Owner)
                if (isOwner) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = Color.White
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(CardBg)
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.edit), color = Color.White) },
                                onClick = {
                                    showMenu = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.delete), color = Color.Red) },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                }
                            )
                        }
                    }
                } else {
                    // Save Button for others
                    IconButton(
                        onClick = onToggleSave,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Save",
                            tint = if (isSaved) Color.Red else Color.White
                        )
                    }
                }

                // NEW: Swap Badge
                if (vehicle.isSwapAvailable || vehicle.views > 20) {
                    Row(modifier = Modifier.align(Alignment.TopStart)) {
                        if (vehicle.isSwapAvailable) {
                            Surface(
                                color = Gold,
                                shape = RoundedCornerShape(bottomEnd = 12.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.swap_badge),
                                    color = DarkBg,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        if (vehicle.views > 20) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                color = Color.Red,
                                shape = RoundedCornerShape(bottomEnd = 12.dp, bottomStart = 12.dp)
                            ) {
                                Text(
                                    text = "🔥 HOT DEAL",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // NEW: Seller Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    if (vehicle.sellerProfilePic.isNotEmpty()) {
                        AsyncImage(
                            model = vehicle.sellerProfilePic,
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.DarkGray),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = vehicle.sellerName,
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = vehicle.name,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${vehicle.emoji} ${vehicle.type}",
                                color = Gold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            if (vehicle.healthScore > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.health_score, vehicle.healthScore),
                                    color = Color.Green.copy(alpha = 0.8f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = DarkBg,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = vehicle.year,
                                color = Gold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = stringResource(R.string.mileage_icon, vehicle.km),
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = vehicle.price,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f))

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoItem(icon = "", text = stringResource(R.string.location_label, translateLocation(vehicle.location)))
                    
                    if (vehicle.type == "Car" && vehicle.fuelType.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_gas_station),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (vehicle.fuelType == "Petrol") stringResource(R.string.petrol) else stringResource(R.string.diesel),
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun translateLocation(location: String): String {
    // If the location matches a city key, return its Urdu/English resource string
    // Otherwise return original (since listings are in English)
    return when(location.lowercase()) {
        "all pakistan" -> stringResource(R.string.all_pakistan)
        "multan" -> stringResource(R.string.city_multan)
        "lahore" -> stringResource(R.string.city_lahore)
        "karachi" -> stringResource(R.string.city_karachi)
        "islamabad" -> stringResource(R.string.city_islamabad)
        "faisalabad" -> stringResource(R.string.city_faisalabad)
        "rawalpindi" -> stringResource(R.string.city_rawalpindi)
        "gujranwala" -> stringResource(R.string.city_gujranwala)
        "peshawar" -> stringResource(R.string.city_peshawar)
        "quetta" -> stringResource(R.string.city_quetta)
        "sargodha" -> stringResource(R.string.city_sargodha)
        "sialkot" -> stringResource(R.string.city_sialkot)
        "bahawalpur" -> stringResource(R.string.city_bahawalpur)
        "sukkur" -> stringResource(R.string.city_sukkur)
        "jhang" -> stringResource(R.string.city_jhang)
        "sheikhupura" -> stringResource(R.string.city_sheikhupura)
        "larkana" -> stringResource(R.string.city_larkana)
        "gujrat" -> stringResource(R.string.city_gujrat)
        "mardan" -> stringResource(R.string.city_mardan)
        "kasur" -> stringResource(R.string.city_kasur)
        "rahim yar khan" -> stringResource(R.string.city_rahim_yar_khan)
        "sahiwal" -> stringResource(R.string.city_sahiwal)
        "okara" -> stringResource(R.string.city_okara)
        "wah cantonment" -> stringResource(R.string.city_wah_cantt)
        "dera ghazi khan" -> stringResource(R.string.city_dg_khan)
        "mirpur khas" -> stringResource(R.string.city_mirpur_khas)
        "nawabshah" -> stringResource(R.string.city_nawabshah)
        "chiniot" -> stringResource(R.string.city_chiniot)
        "kamoke" -> stringResource(R.string.city_kamoke)
        "burewala" -> stringResource(R.string.city_burewala)
        "jhelum" -> stringResource(R.string.city_jhelum)
        "sadiqabad" -> stringResource(R.string.city_sadiqabad)
        "khanewal" -> stringResource(R.string.city_khanewal)
        "hafizabad" -> stringResource(R.string.city_hafizabad)
        "kohat" -> stringResource(R.string.city_kohat)
        "abbottabad" -> stringResource(R.string.city_abbottabad)
        "khuzdar" -> stringResource(R.string.city_khuzdar)
        "muzaffargarh" -> stringResource(R.string.city_muzaffargarh)
        "shikarpur" -> stringResource(R.string.city_shikarpur)
        "muzaffarabad" -> stringResource(R.string.city_muzaffarabad)
        "mirpur (ajk)" -> stringResource(R.string.city_mirpur_ajk)
        "gilgit" -> stringResource(R.string.city_gilgit)
        "skardu" -> stringResource(R.string.city_skardu)
        "mansehra" -> stringResource(R.string.city_mansehra)
        "jacobabad" -> stringResource(R.string.city_jacobabad)
        "hub" -> stringResource(R.string.city_hub)
        "mingora" -> stringResource(R.string.city_mingora)
        "swat" -> stringResource(R.string.city_swat)
        "gwadar" -> stringResource(R.string.city_gwadar)
        "murree" -> stringResource(R.string.city_murree)
        "taxila" -> stringResource(R.string.city_taxila)
        "naran" -> stringResource(R.string.city_naran)
        "kaghan" -> stringResource(R.string.city_kaghan)
        "chitral" -> stringResource(R.string.city_chitral)
        "malam jabba" -> stringResource(R.string.city_malam_jabba)
        else -> location
    }
}

@Composable
fun InfoItem(icon: String, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        if (icon.isNotEmpty()) {
            Text(text = icon, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = text,
            color = Color.Gray,
            fontSize = 13.sp
        )
    }
}
