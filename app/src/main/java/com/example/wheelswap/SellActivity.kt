package com.example.wheelswap

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
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
import java.util.*

class SellActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageHelper.applyLanguage(this)
        
        // Initialize Cloudinary with your Cloud Name
        try {
            MediaManager.init(this, mapOf("cloud_name" to "droixs6gx"))
        } catch (e: Exception) {
            // Already initialized
        }

        val isEdit = intent.getBooleanExtra("isEdit", false)
        val vehicleId = intent.getStringExtra("id") ?: ""

        setContent {
            SellScreen(
                activity = this,
                isEdit = isEdit,
                vehicleId = vehicleId,
                initialName = intent.getStringExtra("name") ?: "",
                initialPrice = intent.getStringExtra("price") ?: "",
                initialLocation = intent.getStringExtra("location") ?: "",
                initialKm = intent.getStringExtra("km") ?: "",
                initialYear = intent.getStringExtra("year") ?: "",
                initialType = intent.getStringExtra("type") ?: "Car",
                initialFuelType = intent.getStringExtra("fuelType") ?: "Petrol",
                initialContact = intent.getStringExtra("contact") ?: "",
                initialModifications = intent.getStringExtra("modifications") ?: "",
                initialImageUrls = intent.getStringArrayListExtra("imageUrls") ?: arrayListOf(),
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellScreen(
    activity: ComponentActivity,
    isEdit: Boolean,
    vehicleId: String,
    initialName: String,
    initialPrice: String,
    initialLocation: String,
    initialKm: String,
    initialYear: String,
    initialType: String,
    initialFuelType: String,
    initialContact: String,
    initialModifications: String,
    initialImageUrls: List<String>,
    onBack: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var price by rememberSaveable { mutableStateOf(initialPrice) }
    var location by rememberSaveable { mutableStateOf(initialLocation) }
    var km by rememberSaveable { mutableStateOf(initialKm) }
    var year by rememberSaveable { mutableStateOf(initialYear) }
    var contact by rememberSaveable { mutableStateOf(initialContact) }
    var modifications by rememberSaveable { mutableStateOf(initialModifications) }
    var type by rememberSaveable { mutableStateOf(initialType) }
    var fuelType by rememberSaveable { mutableStateOf(initialFuelType) }
    var isSwapAvailable by rememberSaveable { mutableStateOf(false) }
    
    var engineCondition by rememberSaveable { mutableFloatStateOf(8f) }
    var bodyCondition by rememberSaveable { mutableFloatStateOf(8f) }
    var interiorCondition by rememberSaveable { mutableFloatStateOf(8f) }
    
    var selectedDuration by rememberSaveable { mutableStateOf("7 Days") }
    var isDurationMenuExpanded by remember { mutableStateOf(false) }
    var showPaymentSheet by rememberSaveable { mutableStateOf(false) }
    var receiptUri by remember { mutableStateOf<Uri?>(null) }

    var remoteImages by remember { mutableStateOf(initialImageUrls) }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isUploading by rememberSaveable { mutableStateOf(false) }

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    
    var currentUserName by remember { mutableStateOf("User") }
    var currentUserProfilePic by remember { mutableStateOf("") }

    LaunchedEffect(auth.currentUser?.uid) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid).get().addOnSuccessListener { doc ->
                val first = doc.getString("firstName") ?: ""
                val last = doc.getString("lastName") ?: ""
                currentUserName = "$first $last".trim().ifEmpty { "User" }
                currentUserProfilePic = doc.getString("profileImageUrl") ?: ""
            }
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val remaining = 3 - remoteImages.size
        selectedImages = (selectedImages + uris).take(remaining)
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit Listing" else "Post a Listing", color = Gold, fontWeight = FontWeight.Bold) },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // NEW: Vehicle Type at the top
            Text("Select your vehicle type", color = Gold, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf("Car", "Bike").forEach { item ->
                    val isSelected = type == item
                    Button(
                        onClick = { type = item },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) Gold else CardBg
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(item, color = if (isSelected) DarkBg else Gold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Photos (Up to 3)", color = Color.Gray, modifier = Modifier.fillMaxWidth())
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(remoteImages) { url ->
                    Box(modifier = Modifier.size(100.dp)) {
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(24.dp)
                                .clickable { remoteImages = remoteImages - url },
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = CircleShape
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.White, modifier = Modifier.padding(4.dp))
                        }
                    }
                }

                items(selectedImages) { uri ->
                    Box(modifier = Modifier.size(100.dp)) {
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(24.dp)
                                .clickable { selectedImages = selectedImages - uri },
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = CircleShape
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.White, modifier = Modifier.padding(4.dp))
                        }
                    }
                }

                if (remoteImages.size + selectedImages.size < 3) {
                    item {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CardBg)
                                .clickable { imagePicker.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Image", tint = Gold)
                        }
                    }
                }
            }

            SellTextField(value = name, onValueChange = { name = it }, label = "Vehicle Name")
            SellTextField(value = price, onValueChange = { price = it }, label = "Price")
            SellTextField(value = location, onValueChange = { location = it }, label = "Location")
            SellTextField(value = km, onValueChange = { km = it }, label = "KM Driven")
            SellTextField(value = year, onValueChange = { year = it }, label = "Year")
            SellTextField(value = contact, onValueChange = { contact = it }, label = "Contact Number")
            SellTextField(
                value = modifications, 
                onValueChange = { modifications = it }, 
                label = "After-market Modifications (e.g. Wrap, Alloys, Audio)"
            )

            if (type == "Car") {
                Text(stringResource(R.string.fuel_type_label), color = Gold, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("Petrol", "Diesel").forEach { item ->
                        val isSelected = fuelType == item
                        val display = if (item == "Petrol") stringResource(R.string.petrol) else stringResource(R.string.diesel)
                        Button(
                            onClick = { fuelType = item },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) Gold else CardBg
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(display, color = if (isSelected) DarkBg else Gold)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("🔄 Open to Exchange/Swap?", color = Color.White, fontWeight = FontWeight.Bold)
                Switch(
                    checked = isSwapAvailable,
                    onCheckedChange = { isSwapAvailable = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Gold)
                )
            }

            Text("Health Report (Transparency)", color = Gold, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
            ConditionSlider(label = "Engine Health", value = engineCondition) { engineCondition = it }
            ConditionSlider(label = "Body Condition", value = bodyCondition) { bodyCondition = it }
            ConditionSlider(label = "Interior Cleanliness", value = interiorCondition) { interiorCondition = it }

            Text("Ad Visibility Duration ⏳", color = Gold, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
            val durations = listOf("7 Days", "15 Days", "1 Month", "2 Months")
            ExposedDropdownMenuBox(
                expanded = isDurationMenuExpanded,
                onExpandedChange = { isDurationMenuExpanded = !isDurationMenuExpanded }
            ) {
                val displayDuration = when(selectedDuration) {
                    "7 Days" -> stringResource(R.string.ad_duration_7_days)
                    "15 Days" -> stringResource(R.string.ad_duration_15_days)
                    "1 Month" -> stringResource(R.string.ad_duration_1_month)
                    "2 Months" -> stringResource(R.string.ad_duration_2_months)
                    else -> selectedDuration
                }
                OutlinedTextField(
                    value = displayDuration,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDurationMenuExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardBg,
                        unfocusedContainerColor = CardBg,
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                ExposedDropdownMenu(
                    expanded = isDurationMenuExpanded,
                    onDismissRequest = { isDurationMenuExpanded = false },
                    modifier = Modifier.background(CardBg)
                ) {
                    durations.forEach { duration ->
                        val durationLabel = when(duration) {
                            "7 Days" -> stringResource(R.string.ad_duration_7_days)
                            "15 Days" -> stringResource(R.string.ad_duration_15_days)
                            "1 Month" -> stringResource(R.string.ad_duration_1_month)
                            "2 Months" -> stringResource(R.string.ad_duration_2_months)
                            else -> duration
                        }
                        DropdownMenuItem(
                            text = { Text(durationLabel, color = Color.White) },
                            onClick = {
                                selectedDuration = duration
                                isDurationMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (name.isEmpty() || price.isEmpty() || location.isEmpty() || contact.isEmpty()) return@Button
                    if (selectedDuration != "7 Days") {
                        showPaymentSheet = true
                    } else {
                        startUploadProcess(isEdit, db, vehicleId, name, price, location, km, year, type, fuelType, contact, modifications, isSwapAvailable, engineCondition, bodyCondition, interiorCondition, selectedDuration, currentUserName, currentUserProfilePic, remoteImages, selectedImages, auth, { isUploading = it }, onBack, false)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold),
                shape = RoundedCornerShape(12.dp),
                enabled = !isUploading
            ) {
                if (isUploading) {
                    CircularProgressIndicator(color = DarkBg, modifier = Modifier.size(24.dp))
                } else {
                    Text(if (isEdit) "Update Listing" else "Post Listing", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }

    if (showPaymentSheet) {
        var selectedMethod by remember { mutableStateOf("JazzCash") }
        val receiptLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { receiptUri = it }
        
        ModalBottomSheet(
            onDismissRequest = { showPaymentSheet = false },
            containerColor = CardBg,
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 32.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.payment_title), color = Gold, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text(stringResource(R.string.payment_subtitle), color = Color.Gray, fontSize = 14.sp)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(stringResource(R.string.payment_instructions), color = Color.White, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                
                Spacer(modifier = Modifier.height(20.dp))

                val adminNumber = "03286029100"

                PaymentOption(
                    label = stringResource(R.string.jazzcash), 
                    subtitle = "Tap to open JazzCash App",
                    isSelected = selectedMethod == "JazzCash",
                    onSelect = { 
                        selectedMethod = "JazzCash"
                        val packageName = "com.techlogix.mobilinkcustomer"
                        val launchIntent = activity.packageManager.getLaunchIntentForPackage(packageName)
                        if (launchIntent != null) {
                            activity.startActivity(launchIntent)
                        } else {
                            // Open Play Store if not installed
                            try {
                                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
                            } catch (e: Exception) {
                                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
                            }
                        }
                    }
                )
                
                PaymentOption(
                    label = stringResource(R.string.bank_card), 
                    subtitle = "Secure Debit/Credit Card payment",
                    isSelected = selectedMethod == "Card",
                    onSelect = { 
                        selectedMethod = "Card"
                        // Card logic can be added here later (e.g. Stripe or manual entry)
                    }
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                // Receipt Upload Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.3f))
                        .border(1.dp, if(receiptUri != null) Color.Green else Color.DarkGray, RoundedCornerShape(12.dp))
                        .clickable { receiptLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (receiptUri != null) {
                        AsyncImage(model = receiptUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Gold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.upload_receipt), color = Gold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        if (receiptUri == null) {
                            Toast.makeText(activity, R.string.receipt_required, Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        showPaymentSheet = false
                        // Process the receipt upload then call startUploadProcess
                        isUploading = true
                        MediaManager.get().upload(receiptUri)
                            .option("folder", "receipts")
                            .option("unsigned", true)
                            .option("upload_preset", "wheelswap_preset")
                            .callback(object : UploadCallback {
                                override fun onStart(requestId: String?) {}
                                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                                    val receiptUrl = resultData?.get("secure_url") as? String ?: ""
                                    startUploadProcess(isEdit, db, vehicleId, name, price, location, km, year, type, fuelType, contact, modifications, isSwapAvailable, engineCondition, bodyCondition, interiorCondition, selectedDuration, currentUserName, currentUserProfilePic, remoteImages, selectedImages, auth, { isUploading = it }, onBack, true, receiptUrl)
                                }
                                override fun onError(requestId: String?, error: ErrorInfo?) {
                                    isUploading = false
                                    Toast.makeText(activity, "Receipt upload failed. Try again.", Toast.LENGTH_SHORT).show()
                                }
                                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                            }).dispatch()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isUploading
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(stringResource(R.string.pay_now), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentOption(label: String, subtitle: String, isSelected: Boolean, onSelect: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(containerColor = if (isSelected) Gold.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.3f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Gold else Color.DarkGray),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = isSelected, onClick = onSelect, colors = RadioButtonDefaults.colors(selectedColor = Gold))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(label, color = Color.White, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun ConditionSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Color.White, fontSize = 12.sp)
            Text("${value.toInt()}/10", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 1f..10f,
            steps = 8,
            colors = SliderDefaults.colors(thumbColor = Gold, activeTrackColor = Gold)
        )
    }
}

fun startUploadProcess(
    isEdit: Boolean, db: FirebaseFirestore, vehicleId: String, name: String, price: String, 
    location: String, km: String, year: String, type: String, fuelType: String, 
    contact: String, modifications: String, isSwapAvailable: Boolean,
    engineCondition: Float, bodyCondition: Float, interiorCondition: Float,
    selectedDuration: String, currentUserName: String, currentUserProfilePic: String,
    remoteImages: List<String>, selectedImages: List<Uri>, auth: FirebaseAuth,
    setUploading: (Boolean) -> Unit, onBack: () -> Unit, isPaid: Boolean, receiptUrl: String = ""
) {
    setUploading(true)
    val finalHealthScore = ((engineCondition + bodyCondition + interiorCondition) / 3).toInt()
    val durationDays = when(selectedDuration) {
        "7 Days" -> 7L
        "15 Days" -> 15L
        "1 Month" -> 30L
        "2 Months" -> 60L
        else -> 7L
    }
    val expiryTimestamp = System.currentTimeMillis() + (durationDays * 24 * 60 * 60 * 1000)

    if (isEdit) {
        val finalUrls = remoteImages.toMutableList()
        if (selectedImages.isEmpty()) {
            updateFirestore(db, vehicleId, name, price, location, km, year, type, fuelType, contact, modifications, isSwapAvailable, finalHealthScore, currentUserName, currentUserProfilePic, finalUrls, isPaid, receiptUrl) {
                setUploading(false)
                onBack()
            }
        } else {
            var finishedCount = 0
            selectedImages.forEach { uri ->
                MediaManager.get().upload(uri)
                    .option("folder", "wheelswap")
                    .option("unsigned", true)
                    .option("upload_preset", "wheelswap_preset")
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String?) {}
                        override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                            val url = resultData?.get("secure_url") as? String
                            if (url != null) finalUrls.add(url)
                            finishedCount++
                            if (finishedCount == selectedImages.size) {
                                updateFirestore(db, vehicleId, name, price, location, km, year, type, fuelType, contact, modifications, isSwapAvailable, finalHealthScore, currentUserName, currentUserProfilePic, finalUrls, isPaid, receiptUrl) {
                                    setUploading(false)
                                    onBack()
                                }
                            }
                        }
                        override fun onError(requestId: String?, error: ErrorInfo?) {
                            finishedCount++
                            if (finishedCount == selectedImages.size) {
                                updateFirestore(db, vehicleId, name, price, location, km, year, type, fuelType, contact, modifications, isSwapAvailable, finalHealthScore, currentUserName, currentUserProfilePic, finalUrls, isPaid, receiptUrl) {
                                    setUploading(false)
                                    onBack()
                                }
                            }
                        }
                        override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                        override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                    }).dispatch()
            }
        }
    } else {
        val uploadedUrls = mutableListOf<String>()
        if (selectedImages.isEmpty()) {
            saveToFirestore(db, auth.currentUser?.uid ?: "", currentUserName, currentUserProfilePic, name, price, location, km, year, type, fuelType, contact, modifications, isSwapAvailable, finalHealthScore, expiryTimestamp, emptyList(), isPaid, receiptUrl) {
                setUploading(false)
                onBack()
            }
        } else {
            var finishedCount = 0
            selectedImages.forEach { uri ->
                MediaManager.get().upload(uri)
                    .option("folder", "wheelswap")
                    .option("unsigned", true)
                    .option("upload_preset", "wheelswap_preset")
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String?) {}
                        override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                            val url = resultData?.get("secure_url") as? String
                            if (url != null) uploadedUrls.add(url)
                            finishedCount++
                            if (finishedCount == selectedImages.size) {
                                saveToFirestore(db, auth.currentUser?.uid ?: "", currentUserName, currentUserProfilePic, name, price, location, km, year, type, fuelType, contact, modifications, isSwapAvailable, finalHealthScore, expiryTimestamp, uploadedUrls, isPaid, receiptUrl) {
                                    setUploading(false)
                                    onBack()
                                }
                            }
                        }
                        override fun onError(requestId: String?, error: ErrorInfo?) {
                            finishedCount++
                            if (finishedCount == selectedImages.size) {
                                saveToFirestore(db, auth.currentUser?.uid ?: "", currentUserName, currentUserProfilePic, name, price, location, km, year, type, fuelType, contact, modifications, isSwapAvailable, finalHealthScore, expiryTimestamp, uploadedUrls, isPaid, receiptUrl) {
                                    setUploading(false)
                                    onBack()
                                }
                            }
                        }
                        override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                        override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                    }).dispatch()
            }
        }
    }
}

fun saveToFirestore(
    db: FirebaseFirestore, userId: String, sellerName: String, sellerProfilePic: String,
    name: String, price: String, location: String, km: String, 
    year: String, type: String, fuelType: String, contact: String, modifications: String,
    isSwapAvailable: Boolean, healthScore: Int, expiryTimestamp: Long, imageUrls: List<String>,
    isPaid: Boolean, receiptUrl: String, onComplete: () -> Unit
) {
    val vehicle = hashMapOf(
        "userId" to userId, "sellerName" to sellerName, "sellerProfilePic" to sellerProfilePic,
        "name" to name, "price" to price, "location" to location, "km" to km,
        "year" to year, "type" to type, "fuelType" to fuelType, "contact" to contact, "modifications" to modifications, 
        "isSwapAvailable" to isSwapAvailable, "healthScore" to healthScore, 
        "isPaid" to isPaid,
        "receiptUrl" to receiptUrl,
        "isVerified" to false, // Admin will change this to true after checking receipt
        "emoji" to (if (type == "Car") "🚗" else "🏍️"),
        "imageUrls" to imageUrls, "timestamp" to System.currentTimeMillis(),
        "expiryTimestamp" to expiryTimestamp,
        "views" to 0
    )
    db.collection("listings").add(vehicle).addOnSuccessListener { onComplete() }
}

fun updateFirestore(
    db: FirebaseFirestore, id: String, name: String, price: String, location: String, km: String,
    year: String, type: String, fuelType: String, contact: String, modifications: String,
    isSwapAvailable: Boolean, healthScore: Int, sellerName: String, sellerProfilePic: String,
    imageUrls: List<String>, isPaid: Boolean, receiptUrl: String, onComplete: () -> Unit
) {
    val updates = mapOf(
        "name" to name, "price" to price, "location" to location, "km" to km, "year" to year,
        "type" to type, "fuelType" to fuelType, "contact" to contact, "modifications" to modifications,
        "isSwapAvailable" to isSwapAvailable, "healthScore" to healthScore,
        "sellerName" to sellerName, "sellerProfilePic" to sellerProfilePic,
        "imageUrls" to imageUrls,
        "isPaid" to isPaid,
        "receiptUrl" to receiptUrl,
        "isVerified" to false,
        "emoji" to (if (type == "Car") "🚗" else "🏍️")
    )
    db.collection("listings").document(id).update(updates).addOnSuccessListener { onComplete() }
}

@Composable
fun SellTextField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) },
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
            focusedBorderColor = Gold, unfocusedBorderColor = Color.Gray,
            focusedLabelColor = Gold, unfocusedLabelColor = Color.Gray
        )
    )
}
