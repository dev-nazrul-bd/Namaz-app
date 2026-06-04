package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BanglaHijriDate
import com.example.data.HolidayInfo
import com.example.data.PrayerTimesCalculator
import com.example.data.UserSettings
import java.util.Calendar

@Composable
fun CalendarScreen(
    settings: UserSettings,
    modifier: Modifier = Modifier
) {
    val isBangla = settings.language == "bangla"

    // Calendar state tracking
    val calendarHelper = remember { Calendar.getInstance() }
    var browsedYear by remember { mutableStateOf(2026) } // Default browser year
    var browsedMonth by remember { mutableStateOf(Calendar.JUNE) } // Default browser month

    var selectedDay by remember { mutableStateOf(4) } // Starts on June 4, 2026

    val daysInMonth = remember(browsedYear, browsedMonth) {
        val cal = Calendar.getInstance()
        cal.set(browsedYear, browsedMonth, 1)
        cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    val firstDayOfWeek = remember(browsedYear, browsedMonth) {
        val cal = Calendar.getInstance()
        cal.set(browsedYear, browsedMonth, 1)
        cal.get(Calendar.DAY_OF_WEEK)
    }

    // Bangla Gregorian Month labels
    val monthNamesBangla = listOf(
        "জানুয়ারী", "ফেব্রুয়ারী", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )
    val monthNamesEnglish = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    val currentMonthLabel = if (isBangla) {
        "${monthNamesBangla[browsedMonth]} ${PrayerTimesCalculator.convertToBengaliNumerals(browsedYear.toString())}"
    } else {
        "${monthNamesEnglish[browsedMonth]} $browsedYear"
    }

    // Build days dataset
    val selectedFullInfo = remember(browsedYear, browsedMonth, selectedDay) {
        PrayerTimesCalculator.convertToBanglaAndHijri(browsedYear, browsedMonth, selectedDay)
    }
    val selectedHoliday = remember(browsedYear, browsedMonth, selectedDay) {
        PrayerTimesCalculator.checkHoliday(browsedYear, browsedMonth, selectedDay)
    }
    val selectedDayPrayerTimes = remember(browsedYear, browsedMonth, selectedDay, settings.city) {
        PrayerTimesCalculator.getTimesForDate(browsedYear, browsedMonth, selectedDay, settings.city)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(14.dp)
    ) {
        // --- SCREEN SCENIC TOPPER CARD: Show Selected Bangla and Hijri Metadata ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Formatting matches Bangladeshi standard: "বৃহস্পতিবার, ৪ জুন, ২০২৬"
                val dateNumeral = if (isBangla) {
                    PrayerTimesCalculator.convertToBengaliNumerals(selectedDay.toString())
                } else {
                    selectedDay.toString()
                }
                val yearNumeral = if (isBangla) {
                    PrayerTimesCalculator.convertToBengaliNumerals(browsedYear.toString())
                } else {
                    browsedYear.toString()
                }
                val monthLabel = if (isBangla) monthNamesBangla[browsedMonth] else monthNamesEnglish[browsedMonth]

                Text(
                    text = if (isBangla) {
                        "${selectedFullInfo.dayOfWeekBangla}, $dateNumeral $monthLabel, $yearNumeral"
                    } else {
                        "${selectedFullInfo.dayOfWeekEnglish}, $monthLabel $selectedDay, $browsedYear"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))

                val hDayNumeral = if (isBangla) PrayerTimesCalculator.convertToBengaliNumerals(selectedFullInfo.hijriDay.toString()) else selectedFullInfo.hijriDay.toString()
                val hYearNumeral = if (isBangla) PrayerTimesCalculator.convertToBengaliNumerals(selectedFullInfo.hijriYear.toString()) else selectedFullInfo.hijriYear.toString()
                Text(
                    text = "${hDayNumeral} ${selectedFullInfo.hijriMonth} ${hYearNumeral}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))

                val bDayNumeral = if (isBangla) PrayerTimesCalculator.convertToBengaliNumerals(selectedFullInfo.banglaDay.toString()) else selectedFullInfo.banglaDay.toString()
                val bYearNumeral = if (isBangla) PrayerTimesCalculator.convertToBengaliNumerals(selectedFullInfo.banglaYear.toString()) else selectedFullInfo.banglaYear.toString()
                Text(
                    text = "${bDayNumeral} ${selectedFullInfo.banglaMonth} ${bYearNumeral}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.tertiary,
                    textAlign = TextAlign.Center
                )
            }
        }

        // --- CALENDAR BROWSER HEADER BAR (Month previous / next selectors) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (browsedMonth == Calendar.JANUARY) {
                        browsedMonth = Calendar.DECEMBER
                        browsedYear--
                    } else {
                        browsedMonth--
                    }
                    selectedDay = 1
                },
                modifier = Modifier.testTag("prev_month_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                    contentDescription = "Previous Month",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "$currentMonthLabel ${if (isBangla) "মাসের সময়সূচি" else "Schedule"}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("current_month_label")
            )

            IconButton(
                onClick = {
                    if (browsedMonth == Calendar.DECEMBER) {
                        browsedMonth = Calendar.JANUARY
                        browsedYear++
                    } else {
                        browsedMonth++
                    }
                    selectedDay = 1
                },
                modifier = Modifier.testTag("next_month_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
                    contentDescription = "Next Month",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // --- WEEKDAY LABELS ---
        val weekdayLabels = if (isBangla) {
            listOf("শনি", "রবি", "সোম", "মঙ্গল", "বুধ", "বৃহ", "শুক্র")
        } else {
            listOf("SAT", "SUN", "MON", "TUE", "WED", "THU", "FRI")
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            weekdayLabels.forEach { label ->
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (label.contains("শুক্র") || label.contains("FRI")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Small informational key/legend for three dates
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isBangla) "📅 মাঝখানে: ইংরেজি | ওপরে-ডানে: বাংলা | নিচে-বামে: আরবী" else "📅 Center: Gregorian | Top-Right: Bangla | Bottom-Left: Hijri",
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }

        // --- MONTH DAYS CALCULATED GRID ENGINE ---
        // Days grid. We shift according to first day of week. (Note: standard Gregorian calendar starts Sunday=1, Bangla starts Saturday=7)
        // Let's standardise first day of week matching our Weekday labels starting on Saturday:
        // Sat index=7 (mapped to offset), Sun=1, Mon=2, etc. Override offset nicely.
        val weekdayOffset = remember(firstDayOfWeek) {
            // firstDayOfWeek: Sunday is 1, Saturday is 7
            // Convert to Saturday-first grid (0 to 6 offset)
            when (firstDayOfWeek) {
                Calendar.SATURDAY -> 0
                Calendar.SUNDAY -> 1
                Calendar.MONDAY -> 2
                Calendar.TUESDAY -> 3
                Calendar.WEDNESDAY -> 4
                Calendar.THURSDAY -> 5
                Calendar.FRIDAY -> 6
                else -> 0
            }
        }

        val totalGridItems = remember(daysInMonth, weekdayOffset) {
            val list = mutableListOf<Int?>()
            for (i in 0 until weekdayOffset) {
                list.add(null) // Empty blocks
            }
            for (day in 1..daysInMonth) {
                list.add(day)
            }
            list
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .testTag("calendar_grid")
        ) {
            items(totalGridItems) { dayVal ->
                if (dayVal != null) {
                    val isSelected = dayVal == selectedDay

                    // Conversion calculations for small tags on date box
                    val itemFullInfo = remember(browsedYear, browsedMonth, dayVal) {
                        PrayerTimesCalculator.convertToBanglaAndHijri(browsedYear, browsedMonth, dayVal)
                    }
                    val itemHoliday = remember(browsedYear, browsedMonth, dayVal) {
                        PrayerTimesCalculator.checkHoliday(browsedYear, browsedMonth, dayVal)
                    }

                    // Base Colors
                    val borderStroke = when {
                        isSelected -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        itemHoliday.isHoliday -> BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                        else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    }

                    val containerColor = when {
                        isSelected -> MaterialTheme.colorScheme.primaryContainer
                        itemHoliday.isHoliday -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                        else -> Color.Transparent
                    }

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(containerColor)
                            .border(borderStroke, RoundedCornerShape(8.dp))
                            .clickable { selectedDay = dayVal }
                            .padding(4.dp)
                    ) {
                        // 1. Top Right: Bangla Month Day (Subtle Small, e.g. ২২)
                        Text(
                            text = if (isBangla) {
                                PrayerTimesCalculator.convertToBengaliNumerals(itemFullInfo.banglaDay.toString())
                            } else {
                                itemFullInfo.banglaDay.toString()
                            },
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            } else {
                                MaterialTheme.colorScheme.tertiary
                            },
                            modifier = Modifier.align(Alignment.TopEnd)
                        )

                        // 2. Center: Gregorian English Month Day (Prominent Bold, e.g. 4)
                        Text(
                            text = if (isBangla) PrayerTimesCalculator.convertToBengaliNumerals(dayVal.toString()) else dayVal.toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (itemHoliday.isHoliday && !isSelected) {
                                MaterialTheme.colorScheme.error
                            } else if (isSelected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.align(Alignment.Center)
                        )

                        // 3. Bottom Left: Islamic Hijri Month Day (Subtle Small, e.g. ১৮)
                        Text(
                            text = if (isBangla) {
                                PrayerTimesCalculator.convertToBengaliNumerals(itemFullInfo.hijriDay.toString())
                            } else {
                                itemFullInfo.hijriDay.toString()
                            },
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            } else {
                                MaterialTheme.colorScheme.secondary
                            },
                            modifier = Modifier.align(Alignment.BottomStart)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.aspectRatio(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // --- HOLIDAY BANNER NOTICE (if any) ---
        if (selectedHoliday.isHoliday) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isBangla) "🚨 ছুটির দিন! : ${selectedHoliday.banglaName}" else "🚨 Holiday! : ${selectedHoliday.englishName}",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        // --- SELECTED DAY'S HOURLY PRAYER TIMES LIST ---
        Text(
            text = if (isBangla) "নির্বাচিত দিনের সময়সূচি" else "Selected Day Times",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val rows = listOf(
                Pair("sahri", selectedDayPrayerTimes.sahri),
                Pair("fajr", selectedDayPrayerTimes.fajr),
                Pair("sunrise", selectedDayPrayerTimes.sunrise),
                Pair("dhuhr", selectedDayPrayerTimes.dhuhr),
                Pair("asr", selectedDayPrayerTimes.asr),
                Pair("maghrib", selectedDayPrayerTimes.maghrib),
                Pair("isha", selectedDayPrayerTimes.isha),
                Pair("iftar", selectedDayPrayerTimes.iftar)
            )

            items(rows) { (key, baseTime) ->
                val localizedName = if (isBangla) {
                    when (key) {
                        "sahri" -> "সাহরী"
                        "fajr" -> "ফজর"
                        "sunrise" -> "সূর্যোদয়"
                        "dhuhr" -> "যোহর"
                        "asr" -> "আসর"
                        "maghrib" -> "মাগরিব"
                        "isha" -> "ইশা"
                        "iftar" -> "ইফতার"
                        else -> key
                    }
                } else {
                    key.replaceFirstChar { it.uppercase() }
                }

                val convertedTimeNum = if (isBangla) {
                    PrayerTimesCalculator.convertToBengaliNumerals(baseTime)
                } else {
                    baseTime
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(6.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val iconStr = when (key) {
                            "sahri" -> "🥣"
                            "fajr" -> "🌅"
                            "sunrise" -> "☀️"
                            "dhuhr" -> "☀️"
                            "asr" -> "⛅"
                            "maghrib" -> "🌇"
                            "isha" -> "🌌"
                            "iftar" -> "🍽️"
                            else -> "⏰"
                        }
                        Text(text = iconStr, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = localizedName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = convertedTimeNum,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
