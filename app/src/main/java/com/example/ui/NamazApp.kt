package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.firebase.FirebaseManager
import com.example.ui.theme.MyApplicationTheme

enum class NamazNavTab {
    HOME, CALENDAR, COMPASS, PROFILE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NamazApp(
    viewModel: NamazViewModel = viewModel()
) {
    val context = LocalContext.current

    // Observe App State
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val completion by viewModel.currentCompletion.collectAsStateWithLifecycle()

    // Observe Real-time Parental Commands from Firebase Manager
    val screenBlockActive by FirebaseManager.screenBlockState.collectAsStateWithLifecycle()
    val actionUrl by FirebaseManager.parentalNoticeAction.collectAsStateWithLifecycle()

    // Tab state
    var currentTab by remember { mutableStateOf(NamazNavTab.HOME) }

    // Dynamic User Theme Selection
    val isSystemDark = isSystemInDarkTheme()
    val darkThemeEnabled = when (settings.themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemDark
    }

    val isBangla = settings.language == "bangla"

    // Refresh calendar date on start/resume
    LaunchedEffect(Unit) {
        viewModel.refreshDate()
    }

    // Outer style theme context mapping
    MyApplicationTheme(darkTheme = darkThemeEnabled, dynamicColor = false) {
        Box(modifier = Modifier.fillMaxSize()) {
            
            // Primary Application Layout Scaffold
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        modifier = Modifier.fillMaxWidth().testTag("bottom_navigation_bar"),
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        NavigationBarItem(
                            selected = currentTab == NamazNavTab.HOME,
                            onClick = { currentTab = NamazNavTab.HOME },
                            icon = { Text(text = "🕌", fontSize = 20.sp) },
                            label = { Text(if (isBangla) "হোম" else "Home") },
                            modifier = Modifier.testTag("nav_tab_home")
                        )
                        NavigationBarItem(
                            selected = currentTab == NamazNavTab.CALENDAR,
                            onClick = { currentTab = NamazNavTab.CALENDAR },
                            icon = { Text(text = "📅", fontSize = 20.sp) },
                            label = { Text(if (isBangla) "ক্যালেন্ডার" else "Calendar") },
                            modifier = Modifier.testTag("nav_tab_calendar")
                        )
                        NavigationBarItem(
                            selected = currentTab == NamazNavTab.COMPASS,
                            onClick = { currentTab = NamazNavTab.COMPASS },
                            icon = { Text(text = "🧭", fontSize = 20.sp) },
                            label = { Text(if (isBangla) "কম্পাস" else "Compass") },
                            modifier = Modifier.testTag("nav_tab_compass")
                        )
                        NavigationBarItem(
                            selected = currentTab == NamazNavTab.PROFILE,
                            onClick = { currentTab = NamazNavTab.PROFILE },
                            icon = { Text(text = "👤", fontSize = 20.sp) },
                            label = { Text(if (isBangla) "প্রোফাইল" else "Profile") },
                            modifier = Modifier.testTag("nav_tab_profile")
                        )
                    }
                },
                contentWindowInsets = WindowInsets.navigationBars
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentTab) {
                        NamazNavTab.HOME -> HomeScreen(
                            settings = settings,
                            onCitySelected = { newCity ->
                                viewModel.saveSettings(settings.copy(city = newCity))
                            }
                        )
                        NamazNavTab.CALENDAR -> CalendarScreen(
                            settings = settings
                        )
                        NamazNavTab.COMPASS -> CompassScreen(
                            settings = settings
                        )
                        NamazNavTab.PROFILE -> ProfileScreen(
                            settings = settings,
                            completion = completion,
                            onSaveSettings = { updated -> viewModel.saveSettings(updated) },
                            onSaveCompletion = { updated -> viewModel.saveCompletion(updated) }
                        )
                    }
                }
            }

            // --- CRITICAL RULE: DEVELOPER CLOSED APP BLOCK OVERLAY ---
            if (screenBlockActive) {
                // Handle hard back-presses on devices by intercepting and ignoring
                BackHandler { /* Stub to completely lock system navigation */ }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xE60A1412)) // Deep sophisticated charcoal-teal alpha overlay
                        .clickable(enabled = true, onClick = { /* Consumed touch clicks to prevent leaks */ })
                        .padding(24.dp)
                        .testTag("developer_block_overlay"),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(3.dp, Color(0xFFE5A11E), RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "🔒",
                                fontSize = 65.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            Text(
                                text = if (isBangla) "অ্যাপটি বন্ধ করা হয়েছে" else "Application Closed",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFD42C2C), // energetic error/info red
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            Text(
                                text = if (isBangla) {
                                    "এই অ্যাপটি ডেভেলপার বন্ধ করে দিয়েছেন। সাহায্য বা যোগাযোগের জন্য নিচের বাটনে চাপ দিন।"
                                } else {
                                    "This application has been closed by the developer. Please click the button below to contact the developer for access or assistance."
                                },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))

                            // Action button points precisely to contact link requested by user
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://dev-nazrul.web.app/contact"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // fallback
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD42C2C)),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("developer_block_contact_btn")
                            ) {
                                Text(
                                    text = if (isBangla) "যোগাযোগ করুন 📞" else "Contact Developer 📞",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
