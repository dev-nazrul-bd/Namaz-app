package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PrayerCompletion
import com.example.data.PrayerTimesCalculator
import com.example.data.UserSettings
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    settings: UserSettings,
    completion: PrayerCompletion,
    onSaveSettings: (UserSettings) -> Unit,
    onSaveCompletion: (PrayerCompletion) -> Unit,
    modifier: Modifier = Modifier
) {
    val isBangla = settings.language == "bangla"
    val focusManager = LocalFocusManager.current

    // Local state for Name TextField
    var editingName by remember { mutableStateOf(settings.userName) }
    var isEditingNameEnabled by remember { mutableStateOf(false) }

    // Calc completion stats
    val prayersTicked = listOf(
        completion.fajr,
        completion.dhuhr,
        completion.asr,
        completion.maghrib,
        completion.isha
    )
    val completedCount = prayersTicked.count { it }
    val progressPercent = (completedCount * 100) / 5

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- SECTION 1: PROFILE IDENTIFIER ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Static high-fidelity avatar icon representing prayer silhouette
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "👳", fontSize = 38.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Editable user-profile name with high accessibility controls
                if (isEditingNameEnabled) {
                    OutlinedTextField(
                        value = editingName,
                        onValueChange = { editingName = it },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .testTag("name_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            onSaveSettings(settings.copy(userName = editingName))
                            isEditingNameEnabled = false
                            focusManager.clearFocus()
                        }),
                        trailingIcon = {
                            IconButton(onClick = {
                                onSaveSettings(settings.copy(userName = editingName))
                                isEditingNameEnabled = false
                                focusManager.clearFocus()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Save Name",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = settings.userName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.testTag("profile_username")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = { isEditingNameEnabled = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit profiles name",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Text(
                    text = if (isBangla) "আজকের নামাজের লক্ষ্যমাত্রা" else "Today's Salat Goal Setter",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // --- SECTION 2: METRICS / SALAT STREAK TRACKER ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Text(
                        text = if (isBangla) "নামাজ চেক-লিস্ট এবং অগ্রগতি" else "Prayer Check-List & Progress",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Horizontal Progress Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        LinearProgressIndicator(
                            progress = { completedCount / 5f },
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = if (isBangla) {
                                "${PrayerTimesCalculator.convertToBengaliNumerals(progressPercent.toString())}%"
                            } else {
                                "$progressPercent%"
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Daily completions checklists
                    val prayers = listOf(
                        Triple("fajr", if (isBangla) "ফজর" else "Fajr", completion.fajr),
                        Triple("dhuhr", if (isBangla) "যোহর" else "Dhuhr", completion.dhuhr),
                        Triple("asr", if (isBangla) "আসর" else "Asr", completion.asr),
                        Triple("maghrib", if (isBangla) "মাগরিব" else "Maghrib", completion.maghrib),
                        Triple("isha", if (isBangla) "ইশা" else "Isha", completion.isha)
                    )

                    prayers.forEach { (key, label, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp) // Accessibility standard target size
                                .clickable {
                                    val updatedCompletion = when (key) {
                                        "fajr" -> completion.copy(fajr = !value)
                                        "dhuhr" -> completion.copy(dhuhr = !value)
                                        "asr" -> completion.copy(asr = !value)
                                        "maghrib" -> completion.copy(maghrib = !value)
                                        "isha" -> completion.copy(isha = !value)
                                        else -> completion
                                    }
                                    onSaveCompletion(updatedCompletion)
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = label,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Checkbox(
                                checked = value,
                                onCheckedChange = {
                                    val updatedCompletion = when (key) {
                                        "fajr" -> completion.copy(fajr = it)
                                        "dhuhr" -> completion.copy(dhuhr = it)
                                        "asr" -> completion.copy(asr = it)
                                        "maghrib" -> completion.copy(maghrib = it)
                                        "isha" -> completion.copy(isha = it)
                                        else -> completion
                                    }
                                    onSaveCompletion(updatedCompletion)
                                },
                                modifier = Modifier.testTag("checkbox_$key")
                            )
                        }
                    }
                }
            }
        }

        // --- SECTION 3: SYSTEM SETTINGS CUSTOMIZATION ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isBangla) "পছন্দনীয় সেটিংস" else "Customize Settings",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    // 1. Language Select
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBangla) "অ্যাপের ভাষা" else "App Language",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Row {
                            TextButton(
                                onClick = { onSaveSettings(settings.copy(language = "bangla")) },
                                colors = ButtonDefaults.textButtonColors(
                                    containerColor = if (isBangla) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    contentColor = if (isBangla) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
                                modifier = Modifier.testTag("lang_bn_btn")
                            ) {
                                Text("বাংলা")
                            }
                            TextButton(
                                onClick = { onSaveSettings(settings.copy(language = "english")) },
                                colors = ButtonDefaults.textButtonColors(
                                    containerColor = if (!isBangla) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    contentColor = if (!isBangla) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                                modifier = Modifier.testTag("lang_en_btn")
                            ) {
                                Text("English")
                            }
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

                    // 2. Dark Mode Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBangla) "ডার্ক মোড" else "Dark Theme",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Row {
                            val activeVal = settings.themeMode
                            TextButton(
                                onClick = { onSaveSettings(settings.copy(themeMode = "light")) },
                                colors = ButtonDefaults.textButtonColors(
                                    containerColor = if (activeVal == "light") MaterialTheme.colorScheme.primary else Color.Transparent,
                                    contentColor = if (activeVal == "light") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("theme_light_btn")
                            ) {
                                Text(if (isBangla) "লাইট" else "Light")
                            }
                            TextButton(
                                onClick = { onSaveSettings(settings.copy(themeMode = "dark")) },
                                colors = ButtonDefaults.textButtonColors(
                                    containerColor = if (activeVal == "dark") MaterialTheme.colorScheme.primary else Color.Transparent,
                                    contentColor = if (activeVal == "dark") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("theme_dark_btn")
                            ) {
                                Text(if (isBangla) "ডার্ক" else "Dark")
                            }
                            TextButton(
                                onClick = { onSaveSettings(settings.copy(themeMode = "system")) },
                                colors = ButtonDefaults.textButtonColors(
                                    containerColor = if (activeVal == "system") MaterialTheme.colorScheme.primary else Color.Transparent,
                                    contentColor = if (activeVal == "system") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("theme_system_btn")
                            ) {
                                Text(if (isBangla) "সিস্টেম" else "System")
                            }
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

                    // 3. Notification switches
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Bell Notification",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBangla) "নামাজের সময়মতো সতর্কবার্তা" else "Prayer Alerts & Reminders",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Switch(
                            checked = settings.notificationsEnabled,
                            onCheckedChange = { onSaveSettings(settings.copy(notificationsEnabled = it)) },
                            modifier = Modifier.testTag("notif_reminders_switch")
                        )
                    }
                }
            }
        }
    }
}
