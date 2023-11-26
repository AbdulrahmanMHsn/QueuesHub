package com.queueshub.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*

class DateUtils {
    companion object {

        fun convertStringToDateTime(
            date: String,
            locale: Locale = Locale.getDefault()
        ): Date {
            return try {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale).parse(date)!!
            } catch (e: Exception) {
                SimpleDateFormat("yyyy-MM-dd hh:mm a", locale).parse(date)!!
            }
        }

        fun formatTime(
            s: String,
            inFormat: String,
            inLocale: Locale,
            outFormat: String,
            outLocale: Locale
        ): String {

            val sdf = SimpleDateFormat(inFormat, inLocale)
            return try {
                val date3 = sdf.parse(s)
                //new format
                val sdf2 = SimpleDateFormat(outFormat, outLocale)
                //formatting the given time to new format with AM/PM
                date3?.let { sdf2.format(it) } ?: "00:00:00"
            } catch (e: ParseException) {
                "00:00:00"
            }
        }

    }
}