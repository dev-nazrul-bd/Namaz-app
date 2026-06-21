package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.PrayerDayTimes
import com.example.data.PrayerTimesCalculator
import com.example.data.UserSettings
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Locale
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

data class CountryTimeZone(
    val englishName: String,
    val banglaName: String,
    val tzId: String,
    val flag: String
)

val COUNTRY_TIMEZONES = listOf(
    CountryTimeZone("Bangladesh", "বাংলাদেশ", "Asia/Dhaka", "🇧🇩"),
    CountryTimeZone("Saudi Arabia", "সৌদি আরব", "Asia/Riyadh", "🇸🇦"),
    CountryTimeZone("United Arab Emirates", "সংযুক্ত আরব আমিরাত (ইউএই)", "Asia/Dubai", "🇦🇪"),
    CountryTimeZone("United Kingdom", "যুক্তরাজ্য (লন্ডন)", "Europe/London", "🇬🇧"),
    CountryTimeZone("United States (New York)", "যুক্তরাষ্ট্র (নিউ ইয়র্ক)", "America/New_York", "🇺🇸"),
    CountryTimeZone("United States (Los Angeles)", "যুক্তরাষ্ট্র (লস অ্যাঞ্জেলেস)", "America/Los_Angeles", "🇺🇸"),
    CountryTimeZone("Malaysia", "মালয়েশিয়া", "Asia/Kuala_Lumpur", "🇲🇾"),
    CountryTimeZone("Singapore", "সিঙ্গাপুর", "Asia/Singapore", "🇸🇬"),
    CountryTimeZone("Qatar", "কাতার", "Asia/Qatar", "🇶🇦"),
    CountryTimeZone("Kuwait", "কুয়েত", "Asia/Kuwait", "🇰🇼"),
    CountryTimeZone("Oman", "ওমান", "Asia/Muscat", "🇴🇲"),
    CountryTimeZone("Canada", "কানাডা (টরন্টো)", "America/Toronto", "🇨🇦"),
    CountryTimeZone("Australia (Sydney)", "অস্ট্রেলিয়া (সিডনি)", "Australia/Sydney", "🇦🇺"),
    CountryTimeZone("India", "ভারত", "Asia/Kolkata", "🇮🇳"),
    CountryTimeZone("Italy", "ইতালি", "Europe/Rome", "🇮🇹"),
    CountryTimeZone("Germany", "জার্মানি", "Europe/Berlin", "🇩🇪")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    settings: UserSettings,
    onCitySelected: (String) -> Unit,
    onSaveSettings: (UserSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    val isBangla = settings.language == "bangla"
    val context = LocalContext.current

    // Live Clock State
    var calendarState by remember { mutableStateOf(Calendar.getInstance()) }
    LaunchedEffect(Unit) {
        while (true) {
            calendarState = Calendar.getInstance()
            delay(1000)
        }
    }

    // Calc Period of Day Greeting & Image mapping
    val hour = calendarState.get(Calendar.HOUR_OF_DAY)
    val timePeriod = when {
        hour in 4..5 -> "dawn"
        hour in 6..11 -> "morning"
        hour in 12..15 -> "noon"
        hour in 16..17 -> "afternoon"
        hour in 18..19 -> "evening"
        else -> "night"
    }

    val greeting = when (timePeriod) {
        "dawn" -> if (isBangla) "শুভ ভোর (ভোর)" else "Peaceful Dawn"
        "morning" -> if (isBangla) "শুভ সকাল (সকাল)" else "Good Morning"
        "noon" -> if (isBangla) "শুভ দুপুর (দুপুর)" else "Good Noon"
        "afternoon" -> if (isBangla) "শুভ বিকাল (বিকেল)" else "Good Afternoon"
        "evening" -> if (isBangla) "শুভ সন্ধ্যা (সন্ধ্যা)" else "Good Evening"
        else -> if (isBangla) "শুভ রাত্রি (রাত)" else "Good Night"
    }

    val timePeriodImage = when (timePeriod) {
        "dawn" -> "https://images.unsplash.com/photo-1517373116369-9bdb8cdc9f62?w=600&auto=format&fit=crop" // Beautiful sunrise mist/dawn
        "morning" -> "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=600&auto=format&fit=crop" // Clear bright green field
        "noon" -> "https://images.unsplash.com/photo-1444492415953-ad6e37609eb3?w=600&auto=format&fit=crop" // Bright solar sky midday
        "afternoon" -> "https://images.unsplash.com/photo-1447752875215-b2761acb3c5d?w=600&auto=format&fit=crop" // Golden autumn rays
        "evening" -> "https://images.unsplash.com/photo-1472214222541-d510753a4707?w=600&auto=format&fit=crop" // Serene dark crimson sunset
        else -> "https://images.unsplash.com/photo-1506318137071-a8e063b4bec0?w=600&auto=format&fit=crop" // Starry night moon crescent
    }

    // Dynamic Clock Formatting in English (English numerals & letters AM/PM)
    val displayHour = calendarState.get(Calendar.HOUR).let { if (it == 0) 12 else it }
    val amPmStr = if (calendarState.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
    val clockStr = String.format(
        Locale.US,
        "%02d:%02d:%02d %s",
        displayHour,
        calendarState.get(Calendar.MINUTE),
        calendarState.get(Calendar.SECOND),
        amPmStr
    )

    // Calculate prayer times for current city and day
    val year = calendarState.get(Calendar.YEAR)
    val month = calendarState.get(Calendar.MONTH)
    val day = calendarState.get(Calendar.DAY_OF_MONTH)
    val calculatedTimes = remember(year, month, day, settings.city) {
        PrayerTimesCalculator.getTimesForDate(year, month, day, settings.city)
    }

    // Map calculated times into lists
    val prayerKeys = listOf("sehri", "fajr", "sunrise", "dhuhr", "asr", "sunset", "maghrib", "iftar", "isha")
    val prayerNamesBangla = mapOf(
        "sehri" to "সাহরী", "fajr" to "ফজর", "sunrise" to "সূর্যোদয়",
        "dhuhr" to "যোহর", "asr" to "আসর", "sunset" to "সূর্যাস্ত",
        "maghrib" to "মাগরিব", "iftar" to "ইফতার", "isha" to "ইশা"
    )
    val prayerNamesEnglish = mapOf(
        "sehri" to "Sehri", "fajr" to "Fajr", "sunrise" to "Sunrise",
        "dhuhr" to "Dhuhr", "asr" to "Asr", "sunset" to "Sunset",
        "maghrib" to "Maghrib", "iftar" to "Iftar", "isha" to "Isha"
    )

    val prayerTimesList = listOf(
        Pair("sehri", calculatedTimes.sahri),
        Pair("fajr", calculatedTimes.fajr),
        Pair("sunrise", calculatedTimes.sunrise),
        Pair("dhuhr", calculatedTimes.dhuhr),
        Pair("asr", calculatedTimes.asr),
        Pair("sunset", calculatedTimes.sunset),
        Pair("maghrib", calculatedTimes.maghrib),
        Pair("iftar", calculatedTimes.iftar),
        Pair("isha", calculatedTimes.isha)
    )

    // City Selection Dropdown state
    var dropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // App Header: "Namaz"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🕌",
                    fontSize = 20.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (isBangla) "নামাজ" else "Namaz",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("app_title")
            )
        }

        // Live Clock centered Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = clockStr,
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("live_clock")
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = greeting,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bangladesh Scenic Image Container
        val periodLabelDisplayName = when (timePeriod) {
            "dawn" -> if (isBangla) "মনোরম ভোর 🌅" else "Peaceful Dawn 🌅"
            "morning" -> if (isBangla) "স্নিগ্ধ সকাল ☀️" else "Fresh Morning ☀️"
            "noon" -> if (isBangla) "উজ্জ্বল দুপুর 🌤️" else "Bright Midday 🌤️"
            "afternoon" -> if (isBangla) "মিষ্টি বিকেল 🍂" else "Serene Afternoon 🍂"
            "evening" -> if (isBangla) "মনোমুগ্ধকর সন্ধ্যা 🌇" else "Scenic Evening 🌇"
            else -> if (isBangla) "শান্ত রাত 🌙" else "Tranquil Night 🌙"
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(timePeriodImage)
                    .crossfade(true)
                    .build(),
                contentDescription = "Scenic Time Period Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                error = painterResource(id = android.R.drawable.presence_online) // placeholder online status fallback
            )
            // Beautiful overlay gradient to make banner highly aesthetic
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        )
                    )
            )
            Text(
                text = periodLabelDisplayName,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            )
        }

        // Premium Single-Country World Clock Wallpaper Widget
        Spacer(modifier = Modifier.height(12.dp))
        
        val firstTz = settings.firstWidgetTzId
        val calInTz = Calendar.getInstance(java.util.TimeZone.getTimeZone(firstTz))
        val currentHourInTz = calInTz.get(Calendar.HOUR).let { if (it == 0) 12 else it }
        val currentMinInTz = calInTz.get(Calendar.MINUTE)
        val amPmInTz = if (calInTz.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
        
        val widgetHourStr = String.format(Locale.US, "%02d", currentHourInTz).let {
            if (isBangla) PrayerTimesCalculator.convertToBengaliNumerals(it) else it
        }
        val widgetMinStr = String.format(Locale.US, "%02d", currentMinInTz).let {
            if (isBangla) PrayerTimesCalculator.convertToBengaliNumerals(it) else it
        }
        
        val widgetTimeStr = "$widgetHourStr:$widgetMinStr"
        
        val daysEng = listOf("", "SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
        val monthsEng = listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")

        val daysBng = listOf("", "রবি", "সোম", "মঙ্গল", "বুধ", "বৃহস্পতি", "শুক্র", "শনি")
        val monthsBng = listOf("জানু:", "ফেব্রু:", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টে:", "অক্টো:", "নভে:", "ডিসে:")

        val dayOfWeekIdx = calInTz.get(Calendar.DAY_OF_WEEK)
        val monthIdx = calInTz.get(Calendar.MONTH)
        val dayOfMonthVal = calInTz.get(Calendar.DAY_OF_MONTH)

        val textDate = if (isBangla) {
            val bDayNum = PrayerTimesCalculator.convertToBengaliNumerals(dayOfMonthVal.toString())
            "${daysBng[dayOfWeekIdx]}, $bDayNum ${monthsBng[monthIdx]}"
        } else {
            "${daysEng[dayOfWeekIdx]}, ${monthsEng[monthIdx]} $dayOfMonthVal"
        }

        // Calculate upcoming prayer based on local time
        val localCal = Calendar.getInstance()
        val localHour = localCal.get(Calendar.HOUR_OF_DAY)
        val localMin = localCal.get(Calendar.MINUTE)
        val currentMinutes = localHour * 60 + localMin

        fun parseTimeToMinutesLocal(timeStr: String, isPm: Boolean): Int {
            return try {
                val parts = timeStr.replace(" AM", "").replace(" PM", "").split(":")
                var h = parts[0].toInt()
                val m = parts[1].toInt()
                if (isPm && h < 12) h += 12
                h * 60 + m
            } catch (e: Exception) {
                0
            }
        }

        val fajrMin = parseTimeToMinutesLocal(calculatedTimes.fajr, false)
        val sunriseMin = parseTimeToMinutesLocal(calculatedTimes.sunrise, false)
        val dhuhrMin = parseTimeToMinutesLocal(calculatedTimes.dhuhr, true)
        val asrMin = parseTimeToMinutesLocal(calculatedTimes.asr, true)
        val maghribMin = parseTimeToMinutesLocal(calculatedTimes.maghrib, true)
        val ishaMin = parseTimeToMinutesLocal(calculatedTimes.isha, true)

        val upcomingSalahName: String
        val upcomingSalahTime: String

        when {
            currentMinutes < fajrMin -> {
                upcomingSalahName = if (isBangla) "ফজর" else "FAJR"
                upcomingSalahTime = calculatedTimes.fajr
            }
            currentMinutes < sunriseMin -> {
                upcomingSalahName = if (isBangla) "সূর্যোদয়" else "SUNRISE"
                upcomingSalahTime = calculatedTimes.sunrise
            }
            currentMinutes < dhuhrMin -> {
                upcomingSalahName = if (isBangla) "যোহর" else "DHUHR"
                upcomingSalahTime = calculatedTimes.dhuhr
            }
            currentMinutes < asrMin -> {
                upcomingSalahName = if (isBangla) "আসর" else "ASR"
                upcomingSalahTime = calculatedTimes.asr
            }
            currentMinutes < maghribMin -> {
                upcomingSalahName = if (isBangla) "মাগরিব" else "MAGHRIB"
                upcomingSalahTime = calculatedTimes.maghrib
            }
            currentMinutes < ishaMin -> {
                upcomingSalahName = if (isBangla) "এশা" else "ISHA"
                upcomingSalahTime = calculatedTimes.isha
            }
            else -> {
                upcomingSalahName = if (isBangla) "ফজর" else "FAJR"
                upcomingSalahTime = calculatedTimes.fajr
            }
        }

        val formattedSalahTime = if (isBangla) PrayerTimesCalculator.convertToBengaliNumerals(upcomingSalahTime) else upcomingSalahTime
        val upcomingSalahStr = "$upcomingSalahName: $formattedSalahTime"
        
        val selectedCountry = COUNTRY_TIMEZONES.find { it.tzId == firstTz } ?: COUNTRY_TIMEZONES[0]
        val countryDisplayName = if (isBangla) selectedCountry.banglaName else selectedCountry.englishName
        
        var showSettingsTzSelector by remember { mutableStateOf(false) }
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { showSettingsTzSelector = true }
                .testTag("world_clock_widget"),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background image from resource
                Image(
                    painter = painterResource(id = com.example.R.drawable.img_clock_bg_1781923452823),
                    contentDescription = "Widget Leaf Background",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Dark glassmorphism overlay to guarantee premium look and accessibility
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.35f), Color.Black.copy(alpha = 0.6f))
                            )
                        )
                )
                
                // Content Layer
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Selected Country/City Tag
                    Row(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = selectedCountry.flag, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$countryDisplayName (${firstTz.substringAfter("/").replace("_", " ")})",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Change Clock Timezone",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(11.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Large Lock Screen Styled Clock Time Text with adjacent smaller AM/PM
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = widgetTimeStr,
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Light,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = amPmInTz,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Light,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    // Date + Next Salah details
                    Text(
                        text = "$textDate  ⏰  $upcomingSalahStr",
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Selection Dialog for Clock Country
        if (showSettingsTzSelector) {
            AlertDialog(
                onDismissRequest = { showSettingsTzSelector = false },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showSettingsTzSelector = false }) {
                        Text(text = if (isBangla) "বাতিল" else "Cancel")
                    }
                },
                title = {
                    Text(
                        text = if (isBangla) "দেশ / টাইম জোন নির্বাচন করুন" else "Select Country / Time Zone",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        items(COUNTRY_TIMEZONES) { tzItem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSaveSettings(settings.copy(firstWidgetTzId = tzItem.tzId))
                                        showSettingsTzSelector = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = tzItem.flag, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (isBangla) tzItem.banglaName else tzItem.englishName,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = tzItem.tzId,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Grid-Section Heading & Division Selector Selector row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isBangla) "নামাজের সময়সূচি" else "Prayer Schedule",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Spinner-like active dropdown menu
            Box {
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
                        .clickable { dropdownExpanded = true }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("city_dropdown"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location Pin",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBangla) {
                            PrayerTimesCalculator.CITIES_BENGALI[settings.city] ?: settings.city
                        } else {
                            settings.city
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown indicator",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    PrayerTimesCalculator.CITIES.forEach { cityItem ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (isBangla) {
                                        PrayerTimesCalculator.CITIES_BENGALI[cityItem] ?: cityItem
                                    } else {
                                        cityItem
                                    }
                                )
                            },
                            onClick = {
                                onCitySelected(cityItem)
                                dropdownExpanded = false
                            },
                            modifier = Modifier.testTag("city_item_$cityItem")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Display 9 static items in rows of 3 columns
        val chunkedList = prayerTimesList.chunked(3)
        chunkedList.forEach { rowList ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowList.forEach { (key, originalTime) ->
                    val localizedName = if (isBangla) {
                        prayerNamesBangla[key] ?: key
                    } else {
                        prayerNamesEnglish[key] ?: key
                    }
                    val formattedTime = if (isBangla) {
                        PrayerTimesCalculator.convertToBengaliNumerals(originalTime)
                    } else {
                        originalTime
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.2f)
                            .testTag("prayer_card_$key"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = localizedName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = formattedTime,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Parses prayer times and current time to calculate live counting towards next Waqt.
 */
private fun calculateCountdown(
    currentTime: Calendar,
    times: PrayerDayTimes,
    isBangla: Boolean
): String {
    val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
    val currentMin = currentTime.get(Calendar.MINUTE)
    val currentSec = currentTime.get(Calendar.SECOND)
    val currentMinutes = currentHour * 60 + currentMin

    // Parse the 6 boundary times
    val fajrTime = parseTimeString(times.fajr, isSpansHalfDay = false) // morning
    val sunriseTime = parseTimeString(times.sunrise, isSpansHalfDay = false) // morning (sunrise)
    val dhuhrTime = parseTimeString(times.dhuhr, isSpansHalfDay = true) // noon (11:56 - 12:02)
    val asrTime = parseTimeString(times.asr, isSpansHalfDay = true) // afternoon
    val maghribTime = parseTimeString(times.maghrib, isSpansHalfDay = true) // evening
    val ishaTime = parseTimeString(times.isha, isSpansHalfDay = true) // night

    val targetMinutes: Int
    val labelBangla: String
    val labelEnglish: String

    when {
        currentMinutes in fajrTime until sunriseTime -> {
            targetMinutes = sunriseTime
            labelBangla = "ফজরের ওয়াক্ত শেষ হতে (সূর্যোদয়) বাকি : "
            labelEnglish = "Time remaining for Sunrise: "
        }
        currentMinutes in sunriseTime until dhuhrTime -> {
            targetMinutes = dhuhrTime
            labelBangla = "যোহরের ওয়াক্ত শুরু হতে বাকি : "
            labelEnglish = "Time remaining to Dhuhr: "
        }
        currentMinutes in dhuhrTime until asrTime -> {
            targetMinutes = asrTime
            labelBangla = "আসরের ওয়াক্ত শুরু হতে বাকি : "
            labelEnglish = "Time remaining to Asr: "
        }
        currentMinutes in asrTime until maghribTime -> {
            targetMinutes = maghribTime
            labelBangla = "মাগরিবের ওয়াক্ত শুরু হতে বাকি : "
            labelEnglish = "Time remaining to Maghrib: "
        }
        currentMinutes in maghribTime until ishaTime -> {
            targetMinutes = ishaTime
            labelBangla = "ইশার ওয়াক্ত শুরু হতে বাকি : "
            labelEnglish = "Time remaining to Isha: "
        }
        else -> {
            // Isha to next day Fajr
            targetMinutes = if (currentMinutes >= ishaTime) {
                fajrTime + 24 * 60
            } else {
                fajrTime
            }
            labelBangla = "ফজরের ওয়াক্ত শুরু হতে বাকি : "
            labelEnglish = "Time remaining to Fajr: "
        }
    }

    val totalRemainingSeconds = (targetMinutes * 60) - (currentMinutes * 60 + currentSec)
    val remainingSeconds = if (totalRemainingSeconds < 0) 0 else totalRemainingSeconds

    val remainingHours = remainingSeconds / 3600
    val remainingMins = (remainingSeconds % 3600) / 60
    val remainingSecs = remainingSeconds % 60

    return if (isBangla) {
        val hStr = PrayerTimesCalculator.convertToBengaliNumerals(String.format(Locale.US, "%02d", remainingHours))
        val mStr = PrayerTimesCalculator.convertToBengaliNumerals(String.format(Locale.US, "%02d", remainingMins))
        val sStr = PrayerTimesCalculator.convertToBengaliNumerals(String.format(Locale.US, "%02d", remainingSecs))
        "$labelBangla$hStr ঘণ্টা $mStr মিনিট $sStr সেকেন্ড"
    } else {
        String.format(
            Locale.US,
            "%s%02dh %02dm %02ds",
            labelEnglish,
            remainingHours,
            remainingMins,
            remainingSecs
        )
    }
}

private fun parseTimeString(timeStr: String, isSpansHalfDay: Boolean = false): Int {
    try {
        val parts = timeStr.split(":")
        var h = parts[0].toInt()
        val m = parts[1].toInt()
        // Simple heuristic conversion for 12 hours formatting standard to daily absolute minutes
        if (isSpansHalfDay && h < 12) {
            h += 12
        }
        return h * 60 + m
    } catch (e: Exception) {
        return 0
    }
}
