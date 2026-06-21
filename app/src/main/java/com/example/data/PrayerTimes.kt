package com.example.data

import java.util.Calendar

data class PrayerDayTimes(
    val dateString: String, // "d-M-yyyy"
    val day: Int,
    val sahri: String,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val sunset: String,
    val maghrib: String,
    val isha: String,
    val iftar: String
)

data class BanglaHijriDate(
    val banglaDay: Int,
    val banglaMonth: String,
    val banglaYear: Int,
    val hijriDay: Int,
    val hijriMonth: String,
    val hijriYear: Int,
    val dayOfWeekBangla: String,
    val dayOfWeekEnglish: String
)

data class HolidayInfo(
    val englishName: String,
    val banglaName: String,
    val isHoliday: Boolean
)

data class AnchorRow(
    val day: Int,
    val sahri: String,
    val fajr: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val sunrise: String
)

data class AnchorMinutes(
    val day: Int,
    val sahri: Int,
    val fajr: Int,
    val dhuhr: Int,
    val asr: Int,
    val maghrib: Int,
    val isha: Int,
    val sunrise: Int
)

object PrayerTimesCalculator {

    // Cities configuration
    val CITIES = listOf(
        "Dhaka", "Gazipur", "Shariatpur", "Madaripur", "Pirojpur", "Barisal", "Jhalokati", "Barguna",
        "Mymensingh", "Tangail", "Bagerhat", "Jamalpur", "Sherpur", "Manikganj", "Faridpur", "Gopalganj",
        "Sirajganj", "Narail", "Khulna", "Magura", "Rajbari", "Pabna", "Satkhira", "Kushtia", "Jessore",
        "Rangpur", "Jhenaidah", "Nilphamari", "Chuadanga", "Kurigram", "Gaibandha", "Rajshahi", "Bogra",
        "Meherpur", "Lalmonirhat", "Chapainawabganj", "Naogaon", "Natore", "Dinajpur", "Thakurgaon", "Panchagarh",
        "Narsingdi", "Narayanganj", "Munshiganj", "Chandpur", "Kishoreganj", "Patuakhali", "Bhola", "Laxmipur",
        "Netrokona", "Comilla", "Brahmanbaria", "Noakhali", "Feni", "Sunamganj", "Habiganj", "Chittagong",
        "Cox's Bazar", "Sylhet", "Moulvibazar", "Khagrachari", "Rangamati", "Bandarban"
    )

    val CITIES_BENGALI = mapOf(
        "Dhaka" to "ঢাকা", "Gazipur" to "গাজীপুরে", "Shariatpur" to "শরীয়তপুর", "Madaripur" to "মাদারীপুর",
        "Pirojpur" to "পিরোজপুর", "Barisal" to "বরিশাল", "Jhalokati" to "ঝালকাঠি", "Barguna" to "বরগুনা",
        "Mymensingh" to "ময়মনসিংহ", "Tangail" to "টাঙ্গাইল", "Bagerhat" to "বাগেরহাট", "Jamalpur" to "জামালপুর",
        "Sherpur" to "শেরপুর", "Manikganj" to "মানিকগঞ্জ", "Faridpur" to "ফরিদপুর", "Gopalganj" to "গোপালগঞ্জ",
        "Sirajganj" to "সিরাজগঞ্জ", "Narail" to "নড়াইল", "Khulna" to "খুলনা", "Magura" to "মাগুরা",
        "Rajbari" to "রাজবাড়ী", "Pabna" to "পাবনা", "Satkhira" to "সাতক্ষীরা", "Kushtia" to "কুষ্টিয়া",
        "Jessore" to "যশোর", "Rangpur" to "রংপুর", "Jhenaidah" to "ঝিনাইদহ", "Nilphamari" to "নীলফামারী",
        "Chuadanga" to "চুয়াডাঙ্গা", "Kurigram" to "কুড়িগ্রাম", "Gaibandha" to "গাইবান্ধা", "Rajshahi" to "রাজশাহী",
        "Bogra" to "বগুড়া", "Meherpur" to "মেহেরপুর", "Lalmonirhat" to "লালমনিরহাট", "Chapainawabganj" to "চাঁপাইনবাবগঞ্জ",
        "Naogaon" to "নওগাঁ", "Natore" to "নাটোর", "Dinajpur" to "দিনাজপুর", "Thakurgaon" to "ঠাকুরগাঁও",
        "Panchagarh" to "পঞ্চগড়", "Narsingdi" to "নরসিংদী", "Narayanganj" to "নারায়ণগঞ্জ", "Munshiganj" to "মুন্সীগঞ্জ",
        "Chandpur" to "চাঁদপুর", "Kishoreganj" to "কিশোরগঞ্জ", "Patuakhali" to "পটুয়াখালী", "Bhola" to "ভোলা",
        "Laxmipur" to "লক্ষ্মীপুর", "Netrokona" to "নেত্রকোনা", "Comilla" to "কুমিল্লা", "Brahmanbaria" to "ব্রাহ্মণবাড়িয়া",
        "Noakhali" to "নোয়াখালী", "Feni" to "ফেনী", "Sunamganj" to "সুনামগঞ্জ", "Habiganj" to "হবিগঞ্জ",
        "Chittagong" to "চট্টগ্রাম", "Cox's Bazar" to "কক্সবাজার", "Sylhet" to "সিলেট", "Moulvibazar" to "মৌলভীবাজার",
        "Khagrachari" to "খাগড়াছড়ি", "Rangamati" to "রাঙ্গামাটি", "Bandarban" to "বান্দরবান"
    )

    // Permanent Dhaka schedule anchors from Islamic Foundation Bangladesh permanent prayer times
    private val ANCHORS = mapOf(
        0 to listOf( // January
            AnchorRow(1, "5:16", "5:22", "12:06", "3:46", "5:27", "6:45", "6:41"),
            AnchorRow(6, "5:18", "5:24", "12:08", "3:50", "5:30", "6:48", "6:42"),
            AnchorRow(12, "5:20", "5:26", "12:10", "3:54", "5:34", "6:51", "6:43"),
            AnchorRow(18, "5:20", "5:26", "12:13", "3:58", "5:38", "6:55", "6:43"),
            AnchorRow(24, "5:19", "5:25", "12:14", "4:03", "5:43", "6:59", "6:41"),
            AnchorRow(30, "5:18", "5:24", "12:16", "4:07", "5:47", "7:03", "6:40")
        ),
        1 to listOf( // February
            AnchorRow(1, "5:18", "5:24", "12:16", "4:08", "5:48", "7:04", "6:39"),
            AnchorRow(6, "5:16", "5:22", "12:16", "4:11", "5:52", "7:07", "6:37"),
            AnchorRow(12, "5:13", "5:19", "12:16", "4:15", "5:55", "7:10", "6:33"),
            AnchorRow(18, "5:09", "5:15", "12:16", "4:18", "5:59", "7:13", "6:29"),
            AnchorRow(24, "5:05", "5:11", "12:15", "4:20", "6:02", "7:16", "6:25"),
            AnchorRow(30, "5:02", "5:08", "12:15", "4:22", "6:04", "7:17", "6:21")
        ),
        2 to listOf( // March
            AnchorRow(1, "5:01", "5:07", "12:14", "4:22", "6:05", "7:18", "6:20"),
            AnchorRow(6, "4:57", "5:03", "12:13", "4:24", "6:07", "7:20", "6:16"),
            AnchorRow(12, "4:51", "4:57", "12:12", "4:26", "6:10", "7:23", "6:10"),
            AnchorRow(18, "4:45", "4:51", "12:10", "4:27", "6:12", "7:25", "6:04"),
            AnchorRow(24, "4:39", "4:45", "12:08", "4:28", "6:14", "7:28", "5:58"),
            AnchorRow(30, "4:31", "4:37", "12:06", "4:29", "6:17", "7:32", "5:52")
        ),
        3 to listOf( // April
            AnchorRow(1, "4:29", "4:35", "12:06", "4:29", "6:18", "7:33", "5:50"),
            AnchorRow(6, "4:24", "4:30", "12:05", "4:29", "6:20", "7:35", "5:45"),
            AnchorRow(12, "4:18", "4:24", "12:03", "4:30", "6:23", "7:38", "5:39"),
            AnchorRow(18, "4:11", "4:17", "12:01", "4:30", "6:25", "7:42", "5:34"),
            AnchorRow(24, "4:05", "4:11", "12:00", "4:30", "6:28", "7:45", "5:29"),
            AnchorRow(30, "4:00", "4:06", "11:59", "4:31", "6:30", "7:49", "5:24")
        ),
        4 to listOf( // May
            AnchorRow(1, "3:59", "4:05", "11:59", "4:31", "6:31", "7:50", "5:24"),
            AnchorRow(6, "3:53", "3:59", "11:59", "4:31", "6:33", "7:54", "5:20"),
            AnchorRow(12, "3:49", "3:55", "11:58", "4:32", "6:36", "7:58", "5:17"),
            AnchorRow(18, "3:46", "3:52", "11:58", "4:33", "6:39", "8:01", "5:14"),
            AnchorRow(24, "3:42", "3:48", "11:59", "4:34", "6:42", "8:06", "5:12"),
            AnchorRow(30, "3:40", "3:46", "11:59", "4:35", "6:45", "8:09", "5:10")
        ),
        5 to listOf( // June
            AnchorRow(1, "3:39", "3:45", "12:00", "4:35", "6:46", "8:11", "5:10"),
            AnchorRow(6, "3:38", "3:44", "12:01", "4:36", "6:47", "8:13", "5:10"),
            AnchorRow(12, "3:38", "3:44", "12:02", "4:38", "6:50", "8:16", "5:10"),
            AnchorRow(18, "3:38", "3:44", "12:03", "4:39", "6:51", "8:18", "5:11"),
            AnchorRow(24, "3:39", "3:45", "12:04", "4:40", "6:53", "8:19", "5:11"),
            AnchorRow(30, "3:43", "3:48", "12:06", "4:42", "6:53", "8:20", "5:14")
        ),
        6 to listOf( // July
            AnchorRow(1, "3:42", "3:48", "12:06", "4:42", "6:54", "8:20", "5:14"),
            AnchorRow(6, "3:44", "3:50", "12:07", "4:43", "6:54", "8:20", "5:16"),
            AnchorRow(12, "3:48", "3:54", "12:08", "4:43", "6:53", "8:18", "5:18"),
            AnchorRow(18, "3:51", "3:57", "12:08", "4:43", "6:52", "8:15", "5:21"),
            AnchorRow(24, "3:55", "4:01", "12:08", "4:43", "6:49", "8:12", "5:24"),
            AnchorRow(30, "3:59", "4:05", "12:08", "4:42", "6:46", "8:07", "5:26")
        ),
        7 to listOf( // August
            AnchorRow(1, "4:00", "4:06", "12:08", "4:42", "6:45", "8:05", "5:27"),
            AnchorRow(6, "4:04", "4:10", "12:08", "4:41", "6:42", "8:01", "5:29"),
            AnchorRow(12, "4:07", "4:13", "12:07", "4:39", "6:38", "7:56", "5:32"),
            AnchorRow(18, "4:11", "4:17", "12:05", "4:37", "6:33", "7:50", "5:34"),
            AnchorRow(24, "4:14", "4:20", "12:04", "4:34", "6:28", "7:44", "5:36"),
            AnchorRow(30, "4:17", "4:23", "12:02", "4:30", "6:22", "7:37", "5:39")
        ),
        8 to listOf( // September
            AnchorRow(1, "4:18", "4:24", "12:02", "4:29", "6:20", "7:35", "5:39"),
            AnchorRow(6, "4:21", "4:27", "12:00", "4:25", "6:15", "7:30", "5:41"),
            AnchorRow(12, "4:23", "4:29", "11:58", "4:21", "6:09", "7:23", "5:43"),
            AnchorRow(18, "4:26", "4:32", "11:56", "4:17", "6:03", "7:16", "5:46"),
            AnchorRow(24, "4:28", "4:34", "11:54", "4:12", "5:57", "7:10", "5:47"),
            AnchorRow(30, "4:30", "4:36", "11:52", "4:07", "5:50", "7:03", "5:49")
        ),
        9 to listOf( // October
            AnchorRow(1, "4:31", "4:37", "11:51", "4:06", "5:49", "7:02", "5:49"),
            AnchorRow(6, "4:33", "4:39", "11:50", "4:02", "5:44", "6:57", "5:51"),
            AnchorRow(12, "4:35", "4:41", "11:49", "3:57", "5:39", "6:52", "5:54"),
            AnchorRow(18, "4:37", "4:43", "11:47", "3:53", "5:33", "6:47", "5:57"),
            AnchorRow(24, "4:40", "4:46", "11:46", "3:48", "5:28", "6:42", "6:00"),
            AnchorRow(30, "4:42", "4:48", "11:45", "3:44", "5:24", "6:39", "6:03")
        ),
        10 to listOf( // November
            AnchorRow(1, "4:43", "4:49", "11:45", "3:43", "5:23", "6:38", "6:04"),
            AnchorRow(6, "4:46", "4:52", "11:45", "3:41", "5:20", "6:35", "6:07"),
            AnchorRow(12, "4:49", "4:55", "11:46", "3:38", "5:17", "6:33", "6:11"),
            AnchorRow(18, "4:52", "4:58", "11:47", "3:36", "5:15", "6:32", "6:15"),
            AnchorRow(24, "4:56", "5:00", "11:49", "3:35", "5:14", "6:31", "6:19"),
            AnchorRow(30, "4:59", "5:05", "11:51", "3:35", "5:14", "6:32", "6:23")
        ),
        11 to listOf( // December
            AnchorRow(1, "5:00", "5:06", "11:51", "3:35", "5:14", "6:32", "6:24"),
            AnchorRow(6, "5:03", "5:09", "11:53", "3:35", "5:14", "6:33", "6:28"),
            AnchorRow(12, "5:07", "5:13", "11:56", "3:37", "5:16", "6:35", "6:31"),
            AnchorRow(18, "5:10", "5:16", "11:58", "3:39", "5:18", "6:37", "6:35"),
            AnchorRow(24, "5:13", "5:19", "12:02", "3:42", "5:21", "6:40", "6:38"),
            AnchorRow(30, "5:15", "5:21", "12:04", "3:45", "5:25", "6:44", "6:40")
        )
    )

    private fun parseAnchorRow(row: AnchorRow): AnchorMinutes {
        return AnchorMinutes(
            day = row.day,
            sahri = parseTime(row.sahri, isPM = false),
            fajr = parseTime(row.fajr, isPM = false),
            dhuhr = parseDhuhr(row.dhuhr),
            asr = parseTime(row.asr, isPM = true),
            maghrib = parseTime(row.maghrib, isPM = true),
            isha = parseTime(row.isha, isPM = true),
            sunrise = parseTime(row.sunrise, isPM = false)
        )
    }

    private fun parseTime(str: String, isPM: Boolean): Int {
        val parts = str.split(":")
        var hr = parts[0].toInt()
        val min = parts[1].toInt()
        if (isPM && hr < 12) {
            hr += 12
        }
        return hr * 60 + min
    }

    private fun parseDhuhr(str: String): Int {
        val parts = str.split(":")
        val hr = parts[0].toInt()
        val min = parts[1].toInt()
        return hr * 60 + min
    }

    private fun interpolate(d: Int, d1: Int, d2: Int, v1: Int, v2: Int): Int {
        if (d1 == d2) return v1
        val fraction = (d - d1).toDouble() / (d2 - d1).toDouble()
        return (v1 + fraction * (v2 - v1)).toInt()
    }

    private fun getDaysInMonth(year: Int, m: Int): Int {
        return when (m) {
            Calendar.JANUARY -> 31
            Calendar.FEBRUARY -> if (year % 4 == 0) 29 else 28
            Calendar.MARCH -> 31
            Calendar.APRIL -> 30
            Calendar.MAY -> 31
            Calendar.JUNE -> 30
            Calendar.JULY -> 31
            Calendar.AUGUST -> 31
            Calendar.SEPTEMBER -> 30
            Calendar.OCTOBER -> 31
            Calendar.NOVEMBER -> 30
            Calendar.DECEMBER -> 31
            else -> 30
        }
    }

    fun getBaseDhakaTimes(year: Int, m: Int, d: Int): AnchorMinutes {
        val monthAnchorsList = ANCHORS[m] ?: throw IllegalArgumentException("Invalid month $m")
        val parsedAnchors = monthAnchorsList.map { parseAnchorRow(it) }

        if (d <= 1) return parsedAnchors.first()
        
        val exactMatch = parsedAnchors.find { it.day == d }
        if (exactMatch != null) return exactMatch

        var lower: AnchorMinutes = parsedAnchors.first()
        var upper: AnchorMinutes = parsedAnchors.last()
        
        if (d in 1..30) {
            for (i in 0 until parsedAnchors.size - 1) {
                if (d >= parsedAnchors[i].day && d <= parsedAnchors[i+1].day) {
                    lower = parsedAnchors[i]
                    upper = parsedAnchors[i+1]
                    break
                }
            }
            
            return AnchorMinutes(
                day = d,
                sahri = interpolate(d, lower.day, upper.day, lower.sahri, upper.sahri),
                fajr = interpolate(d, lower.day, upper.day, lower.fajr, upper.fajr),
                dhuhr = interpolate(d, lower.day, upper.day, lower.dhuhr, upper.dhuhr),
                asr = interpolate(d, lower.day, upper.day, lower.asr, upper.asr),
                maghrib = interpolate(d, lower.day, upper.day, lower.maghrib, upper.maghrib),
                isha = interpolate(d, lower.day, upper.day, lower.isha, upper.isha),
                sunrise = interpolate(d, lower.day, upper.day, lower.sunrise, upper.sunrise)
            )
        } else {
            lower = parsedAnchors.last() // day 30 anchor
            val nextMonth = (m + 1) % 12
            val nextMonthFirstRow = ANCHORS[nextMonth]!!.first()
            val nextMonthFirstRowParsed = parseAnchorRow(nextMonthFirstRow)
            
            val daysInMonth = getDaysInMonth(year, m)
            val lowerDay = 30
            val upperDay = daysInMonth + 1
            
            return AnchorMinutes(
                day = d,
                sahri = interpolate(d, lowerDay, upperDay, lower.sahri, nextMonthFirstRowParsed.sahri),
                fajr = interpolate(d, lowerDay, upperDay, lower.fajr, nextMonthFirstRowParsed.fajr),
                dhuhr = interpolate(d, lowerDay, upperDay, lower.dhuhr, nextMonthFirstRowParsed.dhuhr),
                asr = interpolate(d, lowerDay, upperDay, lower.asr, nextMonthFirstRowParsed.asr),
                maghrib = interpolate(d, lowerDay, upperDay, lower.maghrib, nextMonthFirstRowParsed.maghrib),
                isha = interpolate(d, lowerDay, upperDay, lower.isha, nextMonthFirstRowParsed.isha),
                sunrise = interpolate(d, lowerDay, upperDay, lower.sunrise, nextMonthFirstRowParsed.sunrise)
            )
        }
    }

    // Bengali Month Names
    val BANGLA_MONTHS = listOf(
        "বৈশাখ", "জ্যৈষ্ঠ", "আষাঢ়", "শ্রাবণ", "ভাদ্র", "আশ্বিন",
        "কার্তিক", "অগ্রহায়ণ", "পৌষ", "মাঘ", "ফাল্গুন", "চৈত্র"
    )

    // Hijri Month Names
    val HIJRI_MONTHS = listOf(
        "মহরম", "সফর", "রবিউল আউয়াল", "রবিউস সানি", "জুমাদাল উলা", "জুমাদাস সানি",
        "রজব", "শাবান", "রমজান", "শাওয়াল", "জিলকদ", "জিলহজ্জ"
    )

    val DAYS_OF_WEEK_BENGALI = mapOf(
        Calendar.SUNDAY to "রবিবার",
        Calendar.MONDAY to "সোমবার",
        Calendar.TUESDAY to "মঙ্গলবার",
        Calendar.WEDNESDAY to "বুধবার",
        Calendar.THURSDAY to "বৃহস্পতিবার",
        Calendar.FRIDAY to "শুক্রবার",
        Calendar.SATURDAY to "শনিবার"
    )

    val DAYS_OF_WEEK_ENGLISH = mapOf(
        Calendar.SUNDAY to "Sunday",
        Calendar.MONDAY to "Monday",
        Calendar.TUESDAY to "Tuesday",
        Calendar.WEDNESDAY to "Wednesday",
        Calendar.THURSDAY to "Thursday",
        Calendar.FRIDAY to "Friday",
        Calendar.SATURDAY to "Saturday"
    )

    fun getCityOffsetMinutes(city: String): Pair<Int, Int> {
        return when (city) {
            // Adds
            "Gazipur", "Shariatpur", "Madaripur", "Pirojpur", "Barisal", "Jhalokati", "Barguna" -> Pair(1, 1)
            "Mymensingh", "Tangail", "Bagerhat", "Jamalpur", "Sherpur", "Manikganj" -> Pair(2, 2)
            "Faridpur", "Gopalganj", "Sirajganj", "Narail", "Khulna" -> Pair(3, 3)
            "Magura", "Rajbari", "Pabna" -> Pair(4, 4)
            "Satkhira", "Kushtia", "Jessore", "Rangpur", "Jhenaidah", "Nilphamari", "Chuadanga", "Kurigram", "Gaibandha" -> Pair(6, 6)
            "Rajshahi", "Bogra", "Meherpur", "Lalmonirhat" -> Pair(7, 7)
            "Chapainawabganj", "Naogaon", "Natore" -> Pair(8, 8)
            "Dinajpur", "Thakurgaon", "Panchagarh" -> Pair(6, 11) // Sahri +6, Iftar +11
            
            // Subtractions
            "Narsingdi", "Narayanganj", "Munshiganj", "Chandpur" -> Pair(-1, -1)
            "Kishoreganj", "Patuakhali", "Bhola", "Laxmipur" -> Pair(-2, -2)
            "Netrokona", "Comilla", "Brahmanbaria" -> Pair(-3, -3)
            "Noakhali", "Feni", "Sunamganj", "Habiganj" -> Pair(-4, -4)
            "Chittagong" -> Pair(-5, -5)
            "Cox's Bazar", "Sylhet", "Moulvibazar" -> Pair(-6, -6)
            "Khagrachari", "Rangamati", "Bandarban" -> Pair(-7, -7)
            
            else -> Pair(0, 0)
        }
    }

    fun getTimesForDate(year: Int, month: Int, day: Int, city: String): PrayerDayTimes {
        val (sahriOffset, iftarOffset) = getCityOffsetMinutes(city)
        val dhakaRow = getBaseDhakaTimes(year, month, day)

        val adjustedSahri = dhakaRow.sahri + sahriOffset
        val adjustedFajr = dhakaRow.fajr + sahriOffset
        val adjustedSunrise = dhakaRow.sunrise + sahriOffset
        val adjustedDhuhr = dhakaRow.dhuhr
        val adjustedAsr = dhakaRow.asr
        val adjustedSunset = dhakaRow.maghrib + iftarOffset
        val adjustedMaghrib = dhakaRow.maghrib + iftarOffset
        val adjustedIsha = dhakaRow.isha + iftarOffset
        val adjustedIftar = dhakaRow.maghrib + iftarOffset

        return PrayerDayTimes(
            dateString = "$day-${month + 1}-$year",
            day = day,
            sahri = minutesToTimeString(adjustedSahri),
            fajr = minutesToTimeString(adjustedFajr),
            sunrise = minutesToTimeString(adjustedSunrise),
            dhuhr = minutesToTimeString(adjustedDhuhr),
            asr = minutesToTimeString(adjustedAsr),
            sunset = minutesToTimeString(adjustedSunset),
            maghrib = minutesToTimeString(adjustedMaghrib),
            isha = minutesToTimeString(adjustedIsha),
            iftar = minutesToTimeString(adjustedIftar)
        )
    }

    private fun minutesToTimeString(minutesInDay: Int): String {
        var hrs = (minutesInDay / 60)
        val mins = (minutesInDay % 60)
        if (hrs > 12) {
            hrs -= 12
        }
        if (hrs == 0) {
            hrs = 12
        }
        return String.format("%d:%02d", hrs, mins)
    }

    /**
     * Custom algorithm to convert Gregorian Date to Bangla and Arabic Hijri Calendar formats
     */
    fun convertToBanglaAndHijri(year: Int, month: Int, day: Int): BanglaHijriDate {
        val cal = Calendar.getInstance()
        cal.set(year, month, day)
        val dayOfWeekId = cal.get(Calendar.DAY_OF_WEEK)
        val dayOfWeekBangla = DAYS_OF_WEEK_BENGALI[dayOfWeekId] ?: "বৃহস্পতিবার"
        val dayOfWeekEnglish = DAYS_OF_WEEK_ENGLISH[dayOfWeekId] ?: "Thursday"

        // --- BANGLA CONVERSION ---
        // Quick accurate offset mapping for Bangladesh Standard Calendar
        // In BD Pohela Boishakh is April 14.
        val banglaYear = if (month > Calendar.APRIL || (month == Calendar.APRIL && day >= 14)) {
            year - 593
        } else {
            year - 594
        }

        var bMonthIdx = 0
        var bDay = 1

        val absoluteDayOfBanglaYear = getDaysSincePohelaBoishakh(year, month, day)
        if (absoluteDayOfBanglaYear >= 0) {
            // Bangla months: first 6 months have 31 days (Boishakh to Bhadra), last 6 have 30 days
            var tempDays = absoluteDayOfBanglaYear
            bMonthIdx = 0
            while (tempDays >= 0) {
                val monthDays = if (bMonthIdx < 6) 31 else 30
                if (tempDays < monthDays) {
                    bDay = tempDays + 1
                    break
                }
                tempDays -= monthDays
                bMonthIdx++
            }
        } else {
            // Before Pohela Boishakh (Jan 1 to April 13)
            var tempDays = getDaysFromJan1ToApril13(year, month, day)
            // We start counting from Chaitra or Poush depending on date
            // Poush (starts Dec 15 roughly), Magh (starts Jan 14), Falgun (starts Feb 13), Chaitra (starts Mar 15)
            // Let's do a simple exact lookup to make compile safe and accurate
            val (mIdx, dayVal) = getBanglaDateBeforePohelaBoishakh(year, month, day)
            bMonthIdx = mIdx
            bDay = dayVal
        }

        val bMonthName = BANGLA_MONTHS[bMonthIdx % 12]

        // --- HIJRI CONVERSION ---
        // Dhu al-Hijjah 18, 1447 AH corresponds exactly to June 4, 2026
        // Let's build a precise lookup for 2026 Hijri Months
        var hYear = 1447
        var hMonthIdx = 0
        var hDay = 1

        if (year == 2026) {
            // Accurate start of Islamic Hijri months in 2026:
            // Rajab (starts Dec 10, 2025 -> Jan 1, 2026 is Jan 1 - Dec 10 = Rajab 12 approx)
            val daysFromJan1 = getDayOfYear(year, month, day)
            // Align offsets:
            // Shaban starts Jan 20
            // Ramadan starts Feb 18
            // Shawwal starts Mar 20 (Eid-ul-Fitr Mar 20)
            // Dhu al-Qi'dah starts Apr 18
            // Dhu al-Hijjah starts May 18 (Dhu al-Hijjah 18 is June 4!)
            // Muharram 1448 starts June 16 (Islamic New Year 1448)
            // Safar starts Jul 16
            // Rabi' al-Awwal starts Aug 14
            // Rabi' ath-Thani starts Sep 13
            // Jumada al-Awwal starts Oct 12
            // Jumada ath-Thani starts Nov 11
            // Rajab starts Dec 10

            val monthStarts = listOf(
                Pair(20, "shaban"),         // Jan 20
                Pair(49, "ramadan"),        // Feb 18
                Pair(79, "shawwal"),        // Mar 20
                Pair(108, "dhu_qi_dah"),    // Apr 18
                Pair(138, "dhu_hijjah"),    // May 18
                Pair(167, "muharram"),      // Jun 16 (hYear becomes 1448)
                Pair(197, "safar"),         // Jul 16
                Pair(226, "rabi_awwal"),    // Aug 14
                Pair(256, "rabi_thani"),    // Sep 13
                Pair(285, "jumada_awwal"),  // Oct 12
                Pair(315, "jumada_thani"),  // Nov 11
                Pair(344, "rajab"),         // Dec 10
                Pair(366, "end")
            )

            if (daysFromJan1 < 20) {
                // Rajab 1447 continue
                hMonthIdx = 6 // Rajab
                hDay = daysFromJan1 + 11 // Dec 21 to Jan 1
                hYear = 1447
            } else {
                for (i in 0 until monthStarts.size - 1) {
                    val currentStart = monthStarts[i].first
                    val nextStart = monthStarts[i + 1].first
                    if (daysFromJan1 in currentStart until nextStart) {
                        hDay = daysFromJan1 - currentStart + 1
                        hMonthIdx = when (monthStarts[i].second) {
                            "shaban" -> 7
                            "ramadan" -> 8
                            "shawwal" -> 9
                            "dhu_qi_dah" -> 10
                            "dhu_hijjah" -> 11
                            "muharram" -> 0
                            "safar" -> 1
                            "rabi_awwal" -> 2
                            "rabi_thani" -> 3
                            "jumada_awwal" -> 4
                            "jumada_thani" -> 5
                            "rajab" -> 6
                            else -> 0
                        }
                        hYear = if (daysFromJan1 >= 167) 1448 else 1447
                        break
                    }
                }
            }
        } else {
            // General approximation fallback
            hDay = day
            hMonthIdx = month
            hYear = year - 579
        }

        val hMonthName = HIJRI_MONTHS[hMonthIdx % 12]

        return BanglaHijriDate(
            banglaDay = bDay,
            banglaMonth = bMonthName,
            banglaYear = banglaYear,
            hijriDay = hDay,
            hijriMonth = hMonthName,
            hijriYear = hYear,
            dayOfWeekBangla = dayOfWeekBangla,
            dayOfWeekEnglish = dayOfWeekEnglish
        )
    }

    private fun getDaysSincePohelaBoishakh(year: Int, month: Int, day: Int): Int {
        val calCurrent = Calendar.getInstance()
        calCurrent.set(year, month, day)

        val calBoishakh = Calendar.getInstance()
        calBoishakh.set(year, Calendar.APRIL, 14)

        if (calCurrent.before(calBoishakh)) {
            return -1
        }
        val diffTime = calCurrent.timeInMillis - calBoishakh.timeInMillis
        return (diffTime / (1000 * 60 * 60 * 24)).toInt()
    }

    private fun getDaysFromJan1ToApril13(year: Int, month: Int, day: Int): Int {
        val calCurrent = Calendar.getInstance()
        calCurrent.set(year, month, day)

        val calJan1 = Calendar.getInstance()
        calJan1.set(year, Calendar.JANUARY, 1)

        val diffTime = calCurrent.timeInMillis - calJan1.timeInMillis
        return (diffTime / (1000 * 60 * 60 * 24)).toInt()
    }

    private fun getBanglaDateBeforePohelaBoishakh(year: Int, month: Int, day: Int): Pair<Int, Int> {
        val daysFromJan1 = getDayOfYear(year, month, day)
        // Jan 1 to Jan 13: Magh index starts Jan 14. So before Jan 14: Poush.
        // Poush starts roughly Dec 15.
        if (month == Calendar.JANUARY && day < 14) {
            return Pair(8, day + 17) // Poush (index 8)
        }
        // Magh starts Jan 14
        if (daysFromJan1 in 14..43) {
            return Pair(9, daysFromJan1 - 14 + 1) // Magh (index 9)
        }
        // Falgun starts Feb 13
        val leapOffset = if (year % 4 == 0) 1 else 0
        val isFebLeap = month == Calendar.FEBRUARY && year % 4 == 0
        
        val falgunStart = 44 // Feb 13 non leap
        val chaitraStart = 74 + leapOffset // Mar 15

        if (daysFromJan1 in falgunStart until chaitraStart) {
            return Pair(10, daysFromJan1 - falgunStart + 1) // Falgun (index 10)
        }
        // Chaitra starts Mar 15
        if (daysFromJan1 in chaitraStart..103) {
            return Pair(11, daysFromJan1 - chaitraStart + 1) // Chaitra (index 11)
        }
        return Pair(0, 1)
    }

    /**
     * Determines whether a day is a public holiday in Bangladesh (year 2026).
     */
    fun checkHoliday(year: Int, month: Int, day: Int): HolidayInfo {
        if (year != 2026) {
            // General weekend checks as default holiday
            val cal = Calendar.getInstance()
            cal.set(year, month, day)
            val dOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val isFr = dOfWeek == Calendar.FRIDAY
            return HolidayInfo(
                englishName = if (isFr) "Weekend Friday" else "",
                banglaName = if (isFr) "সাপ্তাহিক ছুটি (শুক্রবার)" else "",
                isHoliday = isFr
            )
        }

        // Exact holidays for Bangladesh in 2026
        var holidayEng = ""
        var holidayBng = ""
        var isH = false

        // Check explicit date holidays
        when (month) {
            Calendar.FEBRUARY -> {
                if (day == 21) {
                    holidayEng = "Mother Language Day"
                    holidayBng = "শহীদ দিবস ও আন্তর্জাতিক মাতৃভাষা দিবস"
                    isH = true
                }
            }
            Calendar.MARCH -> {
                if (day == 17) {
                    holidayEng = "Sheikh Mujibur Birthday"
                    holidayBng = "বঙ্গবন্ধুর জন্মবার্ষিকী"
                    isH = true
                } else if (day in 20..22) {
                    holidayEng = "Eid-ul-Fitr Holiday"
                    holidayBng = "ঈদুল ফিতর"
                    isH = true
                } else if (day == 26) {
                    holidayEng = "Independence Day"
                    holidayBng = "স্বাধীনতা দিবস"
                    isH = true
                }
            }
            Calendar.APRIL -> {
                if (day == 14) {
                    holidayEng = "Bengali New Year"
                    holidayBng = "পহেলা বৈশাখ"
                    isH = true
                }
            }
            Calendar.MAY -> {
                if (day == 1) {
                    holidayEng = "May Day"
                    holidayBng = "মে দিবস"
                    isH = true
                } else if (day == 2) {
                    holidayEng = "Buddha Purnima"
                    holidayBng = "বুদ্ধ পূর্ণিমা"
                    isH = true
                } else if (day in 26..28) {
                    holidayEng = "Eid-ul-Adha Holiday"
                    holidayBng = "ঈদুল আজহা"
                    isH = true
                }
            }
            Calendar.JUNE -> {
                if (day == 25) {
                    holidayEng = "Ashura (10 Muharram)"
                    holidayBng = "পবিত্র আশুরা"
                    isH = true
                }
            }
            Calendar.AUGUST -> {
                if (day == 15) {
                    holidayEng = "National Mourning Day"
                    holidayBng = "জাতীয় শোক দিবস"
                    isH = true
                } else if (day == 25) {
                    holidayEng = "Janmashtami"
                    holidayBng = "জন্মাষ্টমী"
                    isH = true
                }
            }
            Calendar.SEPTEMBER -> {
                if (day == 15) {
                    holidayEng = "Eid-e-Miladunnabi"
                    holidayBng = "ঈদে মিলাদুন্নবী"
                    isH = true
                }
            }
            Calendar.OCTOBER -> {
                if (day == 21) {
                    holidayEng = "Durga Puja / Dashami"
                    holidayBng = "দুর্গাপূজা (বিজয়া দশমী)"
                    isH = true
                }
            }
            Calendar.DECEMBER -> {
                if (day == 16) {
                    holidayEng = "Victory Day"
                    holidayBng = "বিজয় দিবস"
                    isH = true
                } else if (day == 25) {
                    holidayEng = "Christmas Day"
                    holidayBng = "যীশুর জন্মদিন (বড়দিন)"
                    isH = true
                }
            }
        }

        // Fridays are always holiday highlights as requested by user
        val cal = Calendar.getInstance()
        cal.set(year, month, day)
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
            isH = true
            if (holidayEng.isEmpty()) {
                holidayEng = "Friday"
                holidayBng = "সাপ্তাহিক ছুটি (শুক্রবার)"
            }
        }

        return HolidayInfo(englishName = holidayEng, banglaName = holidayBng, isHoliday = isH)
    }

    /**
     * Converts standard digits to Bengali numerals
     */
    fun convertToBengaliNumerals(englishNumberStr: String): String {
        return englishNumberStr
            .replace('0', '০')
            .replace('1', '১')
            .replace('2', '২')
            .replace('3', '৩')
            .replace('4', '৪')
            .replace('5', '৫')
            .replace('6', '৬')
            .replace('7', '৭')
            .replace('8', '৮')
            .replace('9', '৯')
            .replace("AM", "AM")
            .replace("PM", "PM")
    }

    // Direct helper to display "০৫:৫৮:০১" style Bengali clock formatting
    fun formatClockBengali(hour: Int, minute: Int, second: Int, useAmPm: Boolean): String {
        val amPmStr = if (useAmPm) {
            if (hour >= 12) " PM" else " AM"
        } else {
            ""
        }
        val formattedHour = if (useAmPm) {
            val h = hour % 12
            if (h == 0) 12 else h
        } else {
            hour
        }
        val rawTime = String.format("%02d:%02d:%02d%s", formattedHour, minute, second, amPmStr)
        return convertToBengaliNumerals(rawTime)
    }

    private fun getDayOfYear(year: Int, month: Int, day: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(year, month, day)
        return cal.get(Calendar.DAY_OF_YEAR)
    }
}
