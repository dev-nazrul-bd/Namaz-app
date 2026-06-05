package com.example.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.NamazDatabase
import com.example.data.PrayerTimesCalculator
import com.example.data.UserSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class PrayerWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val coroutineScope = CoroutineScope(Dispatchers.IO)
        coroutineScope.launch {
            val dao = NamazDatabase.getDatabase(context).namazDao()
            val settings = dao.getSettings() ?: UserSettings()
            
            launch(Dispatchers.Main) {
                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId, settings)
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
        val views = RemoteViews(context.packageName, R.layout.prayer_widget_layout)

        // Get Current Times
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        val isBangla = settings.language == "bangla"
        val times = PrayerTimesCalculator.getTimesForDate(year, month, day, settings.city)
        val bh = PrayerTimesCalculator.convertToBanglaAndHijri(year, month, day)

        // Title and City
        views.setTextViewText(R.id.widget_title, if (isBangla) "🕌 নামাজ সময়সূচী" else "🕌 Prayer Times")
        val cityStr = if (isBangla) {
            PrayerTimesCalculator.CITIES_BENGALI[settings.city] ?: settings.city
        } else {
            settings.city
        }
        views.setTextViewText(R.id.widget_city, cityStr)

        // Date Display
        val dateText = if (isBangla) {
            val bDayStr = PrayerTimesCalculator.convertToBengaliNumerals(bh.banglaDay.toString())
            val bYearStr = PrayerTimesCalculator.convertToBengaliNumerals(bh.banglaYear.toString())
            val hDayStr = PrayerTimesCalculator.convertToBengaliNumerals(bh.hijriDay.toString())
            val hYearStr = PrayerTimesCalculator.convertToBengaliNumerals(bh.hijriYear.toString())
            "$bDayStr ${bh.banglaMonth} $bYearStr ব. | $hDayStr ${bh.hijriMonth} $hYearStr হি."
        } else {
            "${bh.banglaDay} ${bh.banglaMonth} ${bh.banglaYear} BS | ${bh.hijriDay} ${bh.hijriMonth} ${bh.hijriYear} AH"
        }
        views.setTextViewText(R.id.widget_date_text, dateText)

        // 12h formatting converter standard helper
        fun formatTime(time: String): String {
            return if (isBangla) PrayerTimesCalculator.convertToBengaliNumerals(time) else time
        }

        // Setup individual cell items
        views.setTextViewText(R.id.title_sehri, if (isBangla) "সাহরী" else "Sehri")
        views.setTextViewText(R.id.time_sehri, formatTime(times.sahri))

        views.setTextViewText(R.id.title_fajr, if (isBangla) "ফজর" else "Fajr")
        views.setTextViewText(R.id.time_fajr, formatTime(times.fajr))

        views.setTextViewText(R.id.title_sunrise, if (isBangla) "সূর্যোদয়" else "Sunrise")
        views.setTextViewText(R.id.time_sunrise, formatTime(times.sunrise))

        views.setTextViewText(R.id.title_dhuhr, if (isBangla) "যোহর" else "Dhuhr")
        views.setTextViewText(R.id.time_dhuhr, formatTime(times.dhuhr))

        views.setTextViewText(R.id.title_asr, if (isBangla) "আসর" else "Asr")
        views.setTextViewText(R.id.time_asr, formatTime(times.asr))

        views.setTextViewText(R.id.title_sunset, if (isBangla) "সূর্যাস্ত" else "Sunset")
        views.setViewVisibility(R.id.title_sunset, android.view.View.VISIBLE)
        views.setTextViewText(R.id.time_sunset, formatTime(times.sunset))

        views.setTextViewText(R.id.title_iftar, if (isBangla) "ইফতার" else "Iftar")
        views.setTextViewText(R.id.time_iftar, formatTime(times.iftar))

        views.setTextViewText(R.id.title_maghrib, if (isBangla) "মাগরিব" else "Maghrib")
        views.setTextViewText(R.id.time_maghrib, formatTime(times.maghrib))

        views.setTextViewText(R.id.title_isha, if (isBangla) "এশা" else "Isha")
        views.setTextViewText(R.id.time_isha, formatTime(times.isha))

        // Dynamic Next Waqt Remaining Countdown info inside the widget
        val currentHour = cal.get(Calendar.HOUR_OF_DAY)
        val currentMin = cal.get(Calendar.MINUTE)
        val currentMinutes = currentHour * 60 + currentMin

        fun parseTimeStringLocal(timeStr: String, isSpansHalfDay: Boolean = false): Int {
            try {
                val parts = timeStr.split(":")
                var h = parts[0].toInt()
                val m = parts[1].toInt()
                if (isSpansHalfDay && h < 12) {
                    h += 12
                }
                return h * 60 + m
            } catch (e: Exception) {
                return 0
            }
        }

        val fajrTime = parseTimeStringLocal(times.fajr, isSpansHalfDay = false)
        val sunriseTime = parseTimeStringLocal(times.sunrise, isSpansHalfDay = false)
        val dhuhrTime = parseTimeStringLocal(times.dhuhr, isSpansHalfDay = true)
        val asrTime = parseTimeStringLocal(times.asr, isSpansHalfDay = true)
        val maghribTime = parseTimeStringLocal(times.maghrib, isSpansHalfDay = true)
        val ishaTime = parseTimeStringLocal(times.isha, isSpansHalfDay = true)

        val targetMinutes: Int
        val labelBangla: String
        val labelEnglish: String

        when {
            currentMinutes in fajrTime until sunriseTime -> {
                targetMinutes = sunriseTime
                labelBangla = "ফজরের ওয়াক্ত শেষ হতে বাকি : "
                labelEnglish = "Time to Sunrise: "
            }
            currentMinutes in sunriseTime until dhuhrTime -> {
                targetMinutes = dhuhrTime
                labelBangla = "যোহরের ওয়াক্ত শুরু হতে বাকি : "
                labelEnglish = "Time to Dhuhr: "
            }
            currentMinutes in dhuhrTime until asrTime -> {
                targetMinutes = asrTime
                labelBangla = "আসরের ওয়াক্ত শুরু হতে বাকি : "
                labelEnglish = "Time to Asr: "
            }
            currentMinutes in asrTime until maghribTime -> {
                targetMinutes = maghribTime
                labelBangla = "মাগরিবের ওয়াক্ত শুরু হতে বাকি : "
                labelEnglish = "Time to Maghrib: "
            }
            currentMinutes in maghribTime until ishaTime -> {
                targetMinutes = ishaTime
                labelBangla = "ইশার ওয়াক্ত শুরু হতে বাকি : "
                labelEnglish = "Time to Isha: "
            }
            else -> {
                targetMinutes = if (currentMinutes >= ishaTime) fajrTime + 24 * 60 else fajrTime
                labelBangla = "ফজরের ওয়াক্ত শুরু হতে বাকি : "
                labelEnglish = "Time to Fajr: "
            }
        }

        val remainingMinutes = targetMinutes - currentMinutes
        val hRemaining = if (remainingMinutes < 0) 0 else remainingMinutes / 60
        val mRemaining = if (remainingMinutes < 0) 0 else remainingMinutes % 60

        val countdownLabel = if (isBangla) labelBangla else labelEnglish
        val countdownTimeText = if (isBangla) {
            val hStr = PrayerTimesCalculator.convertToBengaliNumerals(String.format(Locale.US, "%02d", hRemaining))
            val mStr = PrayerTimesCalculator.convertToBengaliNumerals(String.format(Locale.US, "%02d", mRemaining))
            "$hStr ঘণ্টা $mStr মিনিট"
        } else {
            String.format(Locale.US, "%02dh %02dm", hRemaining, mRemaining)
        }

        views.setTextViewText(R.id.widget_countdown_label, countdownLabel)
        views.setTextViewText(R.id.widget_countdown_time, countdownTimeText)

        // Make widget clickable to open main app
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            appWidgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

        // Push update to homescreen manager
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
