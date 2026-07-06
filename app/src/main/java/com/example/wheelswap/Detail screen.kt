package com.example.wheelswap

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import java.util.Locale
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
import com.google.firebase.firestore.FirebaseFirestore

class DetailScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageHelper.applyLanguage(this)
        val id = intent.getStringExtra("id") ?: ""
        val name = intent.getStringExtra("name") ?: ""
        val price = intent.getStringExtra("price") ?: ""
        val location = intent.getStringExtra("location") ?: ""
        val km = intent.getStringExtra("km") ?: ""
        val year = intent.getStringExtra("year") ?: ""
        val emoji = intent.getStringExtra("emoji") ?: ""
        val type = intent.getStringExtra("type") ?: "Car"
        val contact = intent.getStringExtra("contact") ?: "03001234567"
        val isSwapAvailable = intent.getBooleanExtra("isSwapAvailable", false)
        val healthScore = intent.getIntExtra("healthScore", 0)
        val modifications = intent.getStringExtra("modifications") ?: ""
        val sellerName = intent.getStringExtra("sellerName") ?: "User"
        val sellerProfilePic = intent.getStringExtra("sellerProfilePic") ?: ""
        val imageUrls = intent.getStringArrayListExtra("imageUrls") ?: arrayListOf()

        setContent {
            DetailContent(id, name, price, location, km, year, emoji, type, contact, isSwapAvailable, healthScore, modifications, sellerName, sellerProfilePic, imageUrls)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailContent(
    id: String,
    name: String,
    price: String,
    location: String,
    km: String,
    year: String,
    emoji: String,
    type: String,
    contact: String,
    isSwapAvailable: Boolean,
    healthScore: Int,
    modifications: String,
    sellerName: String,
    sellerProfilePic: String,
    imageUrls: List<String>
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    var showSwapDialog by rememberSaveable { mutableStateOf(false) }
    var showLegalDialog by rememberSaveable { mutableStateOf(false) }
    var showReportDialog by rememberSaveable { mutableStateOf(false) }
    
    // States for Offered Vehicle
    var offeredType by rememberSaveable { mutableStateOf("Car") }
    var offeredModel by rememberSaveable { mutableStateOf("") }
    var offeredYear by rememberSaveable { mutableStateOf("") }
    var offeredKm by rememberSaveable { mutableStateOf("") }
    var offeredCondition by rememberSaveable { mutableStateOf("Good") }
    var offeredPhotoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    
    var myVehiclePrice by rememberSaveable { mutableStateOf("") }
    
    // AI Valuation Feature for the main listing
    var aiValuationResult by remember { mutableStateOf<PricePrediction?>(null) }
    var isValuatingListing by rememberSaveable { mutableStateOf(false) }

    // States for Swap Analysis
    var swapPricePrediction by remember { mutableStateOf<PricePrediction?>(null) }
    var isAnalyzingSwap by rememberSaveable { mutableStateOf(false) }

    // Logic to update swap valuation if user changes details after first calculation
    LaunchedEffect(offeredModel, offeredYear, offeredKm, offeredCondition, offeredType) {
        if (swapPricePrediction != null) {
            val prediction = predictPrice(
                vehicleName = offeredModel,
                year = offeredYear.filter { it.isDigit() }.toIntOrNull() ?: 2024,
                mileageKm = offeredKm.filter { it.isDigit() }.toIntOrNull() ?: 0,
                condition = offeredCondition,
                selectedMods = emptyList()
            )
            if (prediction != null) {
                swapPricePrediction = prediction
                myVehiclePrice = prediction.estimatedPrice.toString()
            }
        }
    }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> offeredPhotoUri = uri }

    fun analyzeSwapVehicle() {
        if (offeredModel.isEmpty()) return
        isAnalyzingSwap = true
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            val prediction = predictPrice(
                vehicleName = offeredModel,
                year = offeredYear.filter { it.isDigit() }.toIntOrNull() ?: 2024,
                mileageKm = offeredKm.filter { it.isDigit() }.toIntOrNull() ?: 0,
                condition = offeredCondition,
                selectedMods = emptyList()
            )
            swapPricePrediction = prediction
            myVehiclePrice = prediction?.estimatedPrice?.toString() ?: "0"
            isAnalyzingSwap = false
        }, 1200)
    }

    fun performListingValuation() {
        isValuatingListing = true
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            // Map health score to condition string for AI
            val listingCondition = when {
                healthScore >= 10 -> "Showroom"
                healthScore >= 8 -> "Excellent"
                healthScore >= 6 -> "Good"
                healthScore >= 4 -> "Fair"
                else -> "Poor"
            }

            aiValuationResult = predictPrice(
                vehicleName = name,
                year = year.filter { it.isDigit() }.toIntOrNull() ?: 2020,
                mileageKm = km.filter { it.isDigit() }.toIntOrNull() ?: 0,
                condition = listingCondition,
                selectedMods = modifications.split(",").map { it.trim() }
            )
            isValuatingListing = false
        }, 1500)
    }

    val targetPriceNum = price.filter { it.isDigit() }.toLongOrNull() ?: 0L
    val myPriceNum = myVehiclePrice.filter { it.isDigit() }.toLongOrNull() ?: 0L
    val cashDifference = targetPriceNum - myPriceNum

    if (showLegalDialog) {
        ModalBottomSheet(
            onDismissRequest = { showLegalDialog = false },
            containerColor = Color(0xFF1E1E1E),
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Excise & CPLC Check 🛡️", color = Gold, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text("Select your region to verify legal status", color = Color.Gray, fontSize = 14.sp)
                
                Spacer(modifier = Modifier.height(24.dp))

                LegalLinkButton("Punjab (MTMIS) 🏛️", "https://mtmis.excise.punjab.gov.pk/")
                LegalLinkButton("Sindh Excise 🌊", "https://www.excise.gos.pk/vehicle/vehicle_verification")
                LegalLinkButton("Islamabad Excise ⛰️", "https://islamabadexcise.gov.pk/")
                LegalLinkButton("KP Excise 🏔️", "https://www.kpexcise.gov.pk/mvrecords/")
                LegalLinkButton("CPLC (Stolen Check) 🚨", "https://cplc.org.pk/")
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    "Pro-Tip: Tap 'Contact Seller' to ask for vehicle history privately.",
                    color = Gold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "Note: You will be redirected to the official government portal. Keep the license plate number ready.",
                    color = Color.Gray.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }

    if (showSwapDialog) {
        ModalBottomSheet(
            onDismissRequest = { showSwapDialog = false },
            containerColor = Color(0xFF1E1E1E),
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Text(stringResource(R.string.swap_portal_title), color = Color(0xFFFFD700), fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text(stringResource(R.string.swap_portal_subtitle), color = Color.Gray, fontSize = 14.sp)
                
                Spacer(modifier = Modifier.height(24.dp))

                Text(stringResource(R.string.step_vehicle_type), color = Color.White, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    listOf(
                        stringResource(R.string.filter_car) to "Car",
                        stringResource(R.string.filter_bike) to "Bike"
                    ).forEach { (display, typeValue) ->
                        FilterChip(
                            selected = offeredType == typeValue,
                            onClick = { offeredType = typeValue },
                            label = { Text(display) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFFD700),
                                containerColor = Color.DarkGray,
                                selectedLabelColor = Color.Black,
                                labelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Step 2: Photos
                Text(stringResource(R.string.step_upload_photo), color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black)
                        .border(1.dp, Color.DarkGray, RoundedCornerShape(16.dp))
                        .clickable { photoLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (offeredPhotoUri != null) {
                        AsyncImage(model = offeredPhotoUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color.Gray)
                            Text(stringResource(R.string.tap_add_photo), color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Step 3: Details
                Text(stringResource(R.string.step_vehicle_details), color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = offeredModel,
                    onValueChange = { offeredModel = it },
                    label = { Text(stringResource(R.string.model_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = swapFieldColors()
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = offeredYear,
                        onValueChange = { offeredYear = it },
                        label = { Text(stringResource(R.string.year_label)) },
                        modifier = Modifier.weight(1f),
                        colors = swapFieldColors()
                    )
                    OutlinedTextField(
                        value = offeredKm,
                        onValueChange = { offeredKm = it },
                        label = { Text(stringResource(R.string.km_label)) },
                        modifier = Modifier.weight(1f),
                        colors = swapFieldColors()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                Text(stringResource(R.string.condition_label), color = Color.Gray, fontSize = 12.sp)
                val conditions = listOf(
                    stringResource(R.string.showroom) to "Showroom",
                    stringResource(R.string.excellent) to "Excellent",
                    stringResource(R.string.good) to "Good",
                    stringResource(R.string.fair) to "Fair",
                    stringResource(R.string.poor) to "Poor"
                )
                Row(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    conditions.forEach { (display, condValue) ->
                        FilterChip(
                            selected = offeredCondition == condValue,
                            onClick = { offeredCondition = condValue },
                            label = { Text(display, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFFFD700), containerColor = Color.DarkGray, selectedLabelColor = Color.Black, labelColor = Color.White)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Step 4: Market Value Prediction
                Button(
                    onClick = { analyzeSwapVehicle() },
                    enabled = !isAnalyzingSwap && offeredModel.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isAnalyzingSwap) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(stringResource(R.string.ai_analyzing), color = Color.White)
                    } else {
                        Text(stringResource(R.string.get_market_value), fontWeight = FontWeight.Bold)
                    }
                }

                if (swapPricePrediction != null) {
                    val prediction = swapPricePrediction!!
                    Spacer(modifier = Modifier.height(20.dp))
                    Surface(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.ai_suggested_value), color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(formatPrice(prediction.estimatedPrice), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                            Text("${prediction.verdict} ${prediction.dealRating}", color = Color(0xFFFFD700), fontSize = 14.sp)
                            
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.DarkGray)
                            
                            val diff = targetPriceNum - prediction.estimatedPrice
                            Text(
                                text = if (diff >= 0) stringResource(R.string.cash_on_top) else stringResource(R.string.refund_requested),
                                color = if (diff >= 0) Color(0xFFFFD700) else Color.Green,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = formatPrice(Math.abs(diff)),
                                color = if (diff >= 0) Color(0xFFFFD700) else Color.Green,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            val diff = targetPriceNum - prediction.estimatedPrice
                            val diffText = if (diff >= 0) "plus ${formatPrice(diff)} cash" else "with a refund of ${formatPrice(Math.abs(diff))}"
                            val message = "Assalam-o-Alaikum! I'm interested in a swap for your $name. I'm offering my $offeredYear $offeredModel ($offeredKm KM, $offeredCondition condition). Market valuation suggests my vehicle is worth ${formatPrice(prediction.estimatedPrice)}, making the fair difference $diffText. Would you like to proceed?"
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://api.whatsapp.com/send?phone=$contact&text=${Uri.encode(message)}")
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(stringResource(R.string.send_proposal), color = Color.Black, fontWeight = FontWeight.Black)
                    }
                } else {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            val message = "Assalam-o-Alaikum! I'm interested in a swap for your $name. I'm offering my $offeredYear $offeredModel ($offeredKm KM, $offeredCondition condition). Would you like to proceed?"
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://api.whatsapp.com/send?phone=$contact&text=${Uri.encode(message)}")
                            }
                            context.startActivity(intent)
                        },
                        enabled = offeredModel.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(stringResource(R.string.send_proposal), color = Color.Black, fontWeight = FontWeight.Black)
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Image Gallery
        if (imageUrls.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(16.dp)),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(imageUrls) { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        modifier = Modifier.fillParentMaxWidth().fillMaxHeight(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFF1E1E1E)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 100.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(text = name, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Text(text = year, color = Color(0xFFFFD700), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = price, color = Color(0xFFFFD700), fontSize = 22.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        // NEW: Seller Info Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (sellerProfilePic.isNotEmpty()) {
                    AsyncImage(
                        model = sellerProfilePic,
                        contentDescription = null,
                        modifier = Modifier.size(50.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(50.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = sellerName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Verified Seller ✅", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // AI Valuation Button/Card
        if (aiValuationResult == null) {
            Button(
                onClick = { performListingValuation() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp),
                enabled = !isValuatingListing
            ) {
                if (isValuatingListing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFFFFD700))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(stringResource(R.string.ai_analyzing), color = Color.White)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFFFD700))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(stringResource(R.string.unsatisfied_title), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(stringResource(R.string.unsatisfied_subtitle), color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                }
            }
        } else {
            val prediction = aiValuationResult!!
            Surface(
                color = Color(0xFF1E1E1E),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.ai_insight), color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(prediction.dealRating, fontSize = 10.sp)
                        }
                        Text(stringResource(R.string.fair_market_value, formatPrice(prediction.estimatedPrice)), 
                            color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        Text(
                            text = stringResource(R.string.range_label, formatPrice(prediction.minPrice), formatPrice(prediction.maxPrice)),
                            color = Color(0xFFFFD700),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    IconButton(onClick = { aiValuationResult = null }) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Dismiss", tint = Color.Green)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Details Card
        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFF1E1E1E)).padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailRow(stringResource(R.string.location_key), translateLocation(location))
                DetailRow(stringResource(R.string.mileage_key), km)
                DetailRow(stringResource(R.string.exchange_key), if (isSwapAvailable) "YES ✅" else "NO ❌")
                if (modifications.isNotEmpty()) {
                    HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f), thickness = 0.5.dp)
                    Column {
                        Text(stringResource(R.string.modifications_key), color = Color.Gray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = modifications, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:$contact") }
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(stringResource(R.string.contact_seller), color = Color(0xFF121212), fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Innovation: Excise & CPLC Check
        Button(
            onClick = { showLegalDialog = true },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)), // Trust Green
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(stringResource(R.string.verify_legal), color = Color.White, fontWeight = FontWeight.Bold)
        }

        if (isSwapAvailable) {
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { showSwapDialog = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.request_swap), color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        
        // Safe Meeting Point
        OutlinedButton(
            onClick = {
                val gmmIntentUri = Uri.parse("geo:0,0?q=Public+Park+near+$location")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                context.startActivity(mapIntent)
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
        ) {
            Text(stringResource(R.string.safe_meetup), color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // NEW: Report Button
        TextButton(
            onClick = { showReportDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.report_ad), color = Color.Red.copy(alpha = 0.7f), fontSize = 12.sp)
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text(stringResource(R.string.report_title), color = Color.White) },
            text = { Text(stringResource(R.string.report_confirm), color = Color.Gray) },
            containerColor = Color(0xFF1E1E1E),
            confirmButton = {
                TextButton(onClick = {
                    showReportDialog = false
                    val docRef = db.collection("listings").document(id)
                    docRef.get().addOnSuccessListener { doc ->
                        val currentReports = doc.getLong("reportCount") ?: 0L
                        if (currentReports + 1 >= 4) {
                            docRef.delete().addOnSuccessListener {
                                Toast.makeText(context, R.string.report_removed, Toast.LENGTH_LONG).show()
                                (context as? ComponentActivity)?.finish()
                            }
                        } else {
                            docRef.update("reportCount", com.google.firebase.firestore.FieldValue.increment(1))
                            Toast.makeText(context, R.string.report_success, Toast.LENGTH_SHORT).show()
                        }
                    }
                }) {
                    Text(stringResource(R.string.report_title), color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text(stringResource(R.string.back), color = Color.White)
                }
            }
        )
    }
}

@Composable
fun LegalLinkButton(label: String, url: String) {
    val context = LocalContext.current
    OutlinedButton(
        onClick = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontWeight = FontWeight.Medium)
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Gold)
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = Color.Gray, fontSize = 14.sp)
        Text(text = value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun swapFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = Color(0xFFFFD700),
    unfocusedBorderColor = Color.Gray,
    focusedLabelColor = Color(0xFFFFD700),
    unfocusedLabelColor = Color.Gray
)
