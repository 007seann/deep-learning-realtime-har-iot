package com.specknet.pdiotapp.utils

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.specknet.pdiotapp.activity.ActivityData
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.random.Random

object RandomDataGenerator {
    @RequiresApi(Build.VERSION_CODES.O)
    fun generateRandomActivityData(num: Int): List<ActivityData> {
        val activityDataList: MutableList<ActivityData> = ArrayList()
        for (i in 0 until num) {
            val date = generateRandomLocalDateTime()
            val activityPair = generateRandomActivity()
            activityDataList.add(ActivityData(date, activityPair.first, activityPair.second))
        }
        return activityDataList
    }

    private fun generateRandomActivity(): Pair<String, String> {
        val activity = Constants.ACTIVITY_MAP.values.random()
        val subActivity = Constants.SUBACTIVITY_MAP.values.random()
        return Pair(activity, subActivity)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateRandomLocalDateTime(): LocalDateTime {
        val startDateTime = LocalDateTime.of(2020, 1, 1, 0, 0, 0)
        val endDateTime = LocalDateTime.now()
        val secondsBetween = ChronoUnit.SECONDS.between(startDateTime, endDateTime)
        val randomSeconds = Random.nextLong(0, secondsBetween + 1) // Adjust the upper bound to be inclusive
        val randomDate = startDateTime.plusSeconds(randomSeconds).withSecond(1)
        Log.i("RandomDataGenerator", "Generated random date: $randomDate")
        return randomDate
    }
}