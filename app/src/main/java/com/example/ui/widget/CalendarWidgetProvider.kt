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

class CalendarWidgetProvider : AppWidgetProvider() {

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
        val views = RemoteViews(context.packageName, R.layout.calendar_widget_layout)

        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val currentDay = cal.get(Calendar.DAY_OF_MONTH)

        val isBangla = settings.language == "bangla"
        val bh = PrayerTimesCalculator.convertToBanglaAndHijri(year, month, currentDay)

        // Month translation mapping
        val currentMonthLabel = if (isBangla) {
            when (month) {
                Calendar.JANUARY -> "জানুয়ারি"
                Calendar.FEBRUARY -> "ফেব্রুয়ারি"
                Calendar.MARCH -> "মার্চ"
                Calendar.APRIL -> "এপ্রিল"
                Calendar.MAY -> "মে"
                Calendar.JUNE -> "জুন"
                Calendar.JULY -> "জুলাই"
                Calendar.AUGUST -> "আগস্ট"
                Calendar.SEPTEMBER -> "সেপ্টেম্বর"
                Calendar.OCTOBER -> "অক্টোবর"
                Calendar.NOVEMBER -> "নভেম্বর"
                Calendar.DECEMBER -> "ডিসেম্বর"
                else -> ""
            }
        } else {
            when (month) {
                Calendar.JANUARY -> "January"
                Calendar.FEBRUARY -> "February"
                Calendar.MARCH -> "March"
                Calendar.APRIL -> "April"
                Calendar.MAY -> "May"
                Calendar.JUNE -> "June"
                Calendar.JULY -> "July"
                Calendar.AUGUST -> "August"
                Calendar.SEPTEMBER -> "September"
                Calendar.OCTOBER -> "October"
                Calendar.NOVEMBER -> "November"
                Calendar.DECEMBER -> "December"
                else -> ""
            }
        }

        val yearStr = if (isBangla) PrayerTimesCalculator.convertToBengaliNumerals(year.toString()) else year.toString()
        views.setTextViewText(R.id.calendar_widget_month, "$currentMonthLabel $yearStr")

        // Bangla Hijri subtitle metadata
        val extraText = if (isBangla) {
            val bYearStr = PrayerTimesCalculator.convertToBengaliNumerals(bh.banglaYear.toString())
            val hYearStr = PrayerTimesCalculator.convertToBengaliNumerals(bh.hijriYear.toString())
            "$bYearStr বঙ্গাব্দ | $hYearStr হিজরি"
        } else {
            "${bh.banglaYear} BS | ${bh.hijriYear} AH"
        }
        views.setTextViewText(R.id.calendar_widget_extra, extraText)

        // Weekday labels header rows translations (Saturday first)
        val tempCal = Calendar.getInstance()
        tempCal.set(Calendar.YEAR, year)
        tempCal.set(Calendar.MONTH, month)
        tempCal.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK)
        val maxDays = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val weekdayOffset = when (firstDayOfWeek) {
            Calendar.SATURDAY -> 0
            Calendar.SUNDAY -> 1
            Calendar.MONDAY -> 2
            Calendar.TUESDAY -> 3
            Calendar.WEDNESDAY -> 4
            Calendar.THURSDAY -> 5
            Calendar.FRIDAY -> 6
            else -> 0
        }

        val totalGridItems = mutableListOf<Int?>()
        for (i in 0 until weekdayOffset) {
            totalGridItems.add(null)
        }
        for (d in 1..maxDays) {
            totalGridItems.add(d)
        }

        // Loop through all 42 grid items (6 rows * 7 columns) to populate English/Gregorian, Bangla and Hijri dates
        for (idx in 0 until 42) {
            val row = idx / 7
            val col = idx % 7

            val resNameCell = "cell_r${row + 1}c${col + 1}"
            val resNameEng = "cell_r${row + 1}c${col + 1}_eng"
            val resNameBng = "cell_r${row + 1}c${col + 1}_bng"
            val resNameHij = "cell_r${row + 1}c${col + 1}_hij"

            val cellId = context.resources.getIdentifier(resNameCell, "id", context.packageName)
            val idEng = context.resources.getIdentifier(resNameEng, "id", context.packageName)
            val idBng = context.resources.getIdentifier(resNameBng, "id", context.packageName)
            val idHij = context.resources.getIdentifier(resNameHij, "id", context.packageName)

            if (cellId != 0 && idEng != 0 && idBng != 0 && idHij != 0) {
                val dayVal = if (idx < totalGridItems.size) totalGridItems[idx] else null

                if (dayVal != null) {
                    val itemFullInfo = PrayerTimesCalculator.convertToBanglaAndHijri(year, month, dayVal)

                    val displayedEngNum = if (isBangla) PrayerTimesCalculator.convertToBengaliNumerals(dayVal.toString()) else dayVal.toString()
                    val displayedBngNum = if (isBangla) {
                        PrayerTimesCalculator.convertToBengaliNumerals(itemFullInfo.banglaDay.toString())
                    } else {
                        itemFullInfo.banglaDay.toString()
                    }
                    val displayedHijNum = if (isBangla) {
                        PrayerTimesCalculator.convertToBengaliNumerals(itemFullInfo.hijriDay.toString())
                    } else {
                        itemFullInfo.hijriDay.toString()
                    }

                    views.setTextViewText(idEng, displayedEngNum)
                    views.setTextViewText(idBng, displayedBngNum)
                    views.setTextViewText(idHij, displayedHijNum)

                    if (dayVal == currentDay) {
                        views.setInt(cellId, "setBackgroundResource", R.drawable.widget_calendar_cell_bg)
                    } else {
                        views.setInt(cellId, "setBackgroundResource", 0)
                    }
                } else {
                    // Empty cell
                    views.setTextViewText(idEng, "")
                    views.setTextViewText(idBng, "")
                    views.setTextViewText(idHij, "")
                    views.setInt(cellId, "setBackgroundResource", 0)
                }
            }
        }

        // Tap on calendar widget should open Namaz application main screen
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            appWidgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.calendar_widget_month, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
