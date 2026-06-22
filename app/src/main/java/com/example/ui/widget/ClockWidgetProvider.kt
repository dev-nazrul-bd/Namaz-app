package com.example.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.NamazDatabase
import com.example.data.PrayerTimesCalculator
import com.example.data.PrayerDayTimes
import com.example.data.UserSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class ClockWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // 1. Instant synchronous rendering with default settings to prevent system cold-start timeouts
        val fallbackSettings = UserSettings()
        for (appWidgetId in appWidgetIds) {
            try {
                updateAppWidget(context, appWidgetManager, appWidgetId, fallbackSettings)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        // 2. Query actual preferences and database information asynchronously
        val pendingResult = goAsync()
        val coroutineScope = CoroutineScope(Dispatchers.IO)
        coroutineScope.launch {
            try {
                val dao = NamazDatabase.getDatabase(context).namazDao()
                val settings = dao.getSettings() ?: UserSettings()
                
                for (appWidgetId in appWidgetIds) {
                    try {
                        updateAppWidget(context, appWidgetManager, appWidgetId, settings)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    pendingResult?.finish()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        val pendingResult = goAsync()
        val coroutineScope = CoroutineScope(Dispatchers.IO)
        coroutineScope.launch {
            try {
                val dao = NamazDatabase.getDatabase(context).namazDao()
                val settings = dao.getSettings() ?: UserSettings()
                updateAppWidget(context, appWidgetManager, appWidgetId, settings)
            } catch (e: Exception) {
                e.printStackTrace()
                try {
                    updateAppWidget(context, appWidgetManager, appWidgetId, UserSettings())
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            } finally {
                try {
                    pendingResult?.finish()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        settings: UserSettings
    ) {
        val views = RemoteViews(context.packageName, R.layout.clock_widget_layout)

        val isBangla = settings.language == "bangla"
        val tzId = settings.firstWidgetTzId // Use firstWidgetTzId as single timezone selection
        val timeZone = TimeZone.getTimeZone(tzId)

        // Date & Time in the selected timezone
        val cal = Calendar.getInstance(timeZone)
        val rawHour = cal.get(Calendar.HOUR)
        val formattedHour = if (rawHour == 0) 12 else rawHour
        val formattedMinute = cal.get(Calendar.MINUTE)
        val amPmStr = if (cal.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"

        val clockTime = String.format(Locale.US, "%02d:%02d", formattedHour, formattedMinute).let {
            if (isBangla) PrayerTimesCalculator.convertToBengaliNumerals(it) else it
        }

        views.setTextViewText(R.id.widget_clock_text, clockTime)
        views.setTextViewText(R.id.widget_ampm_text, amPmStr)

        // Hide timezone indicator completely (as requested: দেশের নাম দেখাবে না)
        views.setViewVisibility(R.id.widget_timezone_indicator, android.view.View.GONE)

        // Use widget options to dynamically scale text sizes based on available size
        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
        val minWidth = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) ?: 0
        val minHeight = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT) ?: 0

        val clockTextSize: Float
        val ampmTextSize: Float
        val subtitleTextSize: Float

        if (minHeight > 0 && (minHeight < 70 || minWidth < 120)) {
            // Extremely compact / Small widget (small font and reduced boldness visually)
            clockTextSize = 25f
            ampmTextSize = 9f
            subtitleTextSize = 9f
        } else if (minHeight > 0 && (minHeight < 90 || minWidth < 160)) {
            // Medium widget
            clockTextSize = 34f
            ampmTextSize = 11f
            subtitleTextSize = 10f
        } else {
            // Default/Large widget
            clockTextSize = 42f
            ampmTextSize = 13f
            subtitleTextSize = 11f
        }

        views.setTextViewTextSize(R.id.widget_clock_text, TypedValue.COMPLEX_UNIT_SP, clockTextSize)
        views.setTextViewTextSize(R.id.widget_ampm_text, TypedValue.COMPLEX_UNIT_SP, ampmTextSize)
        views.setTextViewTextSize(R.id.widget_subtitle_text, TypedValue.COMPLEX_UNIT_SP, subtitleTextSize)

        // Determine matching country info with flags
        val (flag, _) = getCountryInfo(tzId, isBangla)

        // Date Display
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val month = cal.get(Calendar.MONTH)
        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)

        val dateStr = formatDate(dayOfWeek, month, dayOfMonth, isBangla)

        // Combine date + flag (no alarms / prayer times on the clock widget as requested)
        val finalSubtitleText = "$dateStr $flag"
        views.setTextViewText(R.id.widget_subtitle_text, finalSubtitleText)

        // Click to Open Main App
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            appWidgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

        // Update
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun getCountryInfo(tzId: String, isBangla: Boolean): Pair<String, String> {
        return when (tzId) {
            "Asia/Dhaka" -> Pair("🇧🇩", if (isBangla) "বাংলাদেশ" else "Bangladesh")
            "Asia/Riyadh" -> Pair("🇸🇦", if (isBangla) "সৌদি আরব" else "Saudi Arabia")
            "Asia/Dubai" -> Pair("🇦🇪", if (isBangla) "সংযুক্ত আরব আমিরাত" else "UAE")
            "Europe/London" -> Pair("🇬🇧", if (isBangla) "যুক্তরাজ্য" else "United Kingdom")
            "America/New_York" -> Pair("🇺🇸", if (isBangla) "যুক্তরাষ্ট্র (নিউ ইয়র্ক)" else "US (NY)")
            "America/Los_Angeles" -> Pair("🇺🇸", if (isBangla) "যুক্তরাষ্ট্র (লস অ্যাঞ্জেলেস)" else "US (LA)")
            "Asia/Kuala_Lumpur" -> Pair("🇲🇾", if (isBangla) "মালয়েশিয়া" else "Malaysia")
            "Asia/Singapore" -> Pair("🇸🇬", if (isBangla) "সিঙ্গাপুর" else "Singapore")
            "Asia/Qatar" -> Pair("🇶🇦", if (isBangla) "কাতার" else "Qatar")
            "Asia/Kuwait" -> Pair("🇰🇼", if (isBangla) "কুয়েত" else "Kuwait")
            "Asia/Muscat" -> Pair("🇴🇲", if (isBangla) "ওমান" else "Oman")
            "America/Toronto" -> Pair("🇨🇦", if (isBangla) "কানাডা" else "Canada")
            "Australia/Sydney" -> Pair("🇦🇺", if (isBangla) "অস্ট্রেলিয়া" else "Australia")
            "Asia/Kolkata" -> Pair("🇮🇳", if (isBangla) "ভারত" else "India")
            "Europe/Rome" -> Pair("🇮🇹", if (isBangla) "ইতালি" else "Italy")
            "Europe/Berlin" -> Pair("🇩🇪", if (isBangla) "জার্মানি" else "Germany")
            else -> Pair("🌐", if (isBangla) "বিশ্ব ঘড়ি" else "World Clock")
        }
    }

    private fun formatDate(dayOfWeek: Int, month: Int, dayOfMonth: Int, isBangla: Boolean): String {
        val daysEng = listOf("", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val monthsEng = listOf("Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Aug", "Sept", "Oct", "Nov", "Dec")

        val daysBng = listOf("", "রবি", "সোম", "মঙ্গল", "বুধ", "বৃহস্পতি", "শুক্র", "শনি")
        val monthsBng = listOf("জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর")

        return if (isBangla) {
            val banglaDayNum = PrayerTimesCalculator.convertToBengaliNumerals(dayOfMonth.toString())
            "${daysBng[dayOfWeek]}, $banglaDayNum ${monthsBng[month]}"
        } else {
            "${daysEng[dayOfWeek]}, ${monthsEng[month]} $dayOfMonth"
        }
    }

    private fun getUpcomingSalah(cal: Calendar, times: PrayerDayTimes, isBangla: Boolean): String {
        val currentMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)

        fun parseTime(timeStr: String, isPm: Boolean): Int {
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

        // Parse approximate indices
        val fajrMin = parseTime(times.fajr, false)
        val sunriseMin = parseTime(times.sunrise, false)
        val dhuhrMin = parseTime(times.dhuhr, true)
        val asrMin = parseTime(times.asr, true)
        val maghribMin = parseTime(times.maghrib, true)
        val ishaMin = parseTime(times.isha, true)

        val name: String
        val time: String

        when {
            currentMinutes < fajrMin -> {
                name = if (isBangla) "ফজর" else "FAJR"
                time = times.fajr
            }
            currentMinutes < sunriseMin -> {
                name = if (isBangla) "সূর্যোদয়" else "SUNRISE"
                time = times.sunrise
            }
            currentMinutes < dhuhrMin -> {
                name = if (isBangla) "যোহর" else "DHUHR"
                time = times.dhuhr
            }
            currentMinutes < asrMin -> {
                name = if (isBangla) "আসর" else "ASR"
                time = times.asr
            }
            currentMinutes < maghribMin -> {
                name = if (isBangla) "মাগরিব" else "MAGHRIB"
                time = times.maghrib
            }
            currentMinutes < ishaMin -> {
                name = if (isBangla) "এশা" else "ISHA"
                time = times.isha
            }
            else -> {
                name = if (isBangla) "ফজর" else "FAJR"
                time = times.fajr
            }
        }

        val formattedTime = if (isBangla) PrayerTimesCalculator.convertToBengaliNumerals(time) else time
        return "$name: $formattedTime"
    }
}
