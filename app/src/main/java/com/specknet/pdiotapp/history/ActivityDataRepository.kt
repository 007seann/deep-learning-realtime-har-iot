package com.specknet.pdiotapp.history

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.specknet.pdiotapp.activity.ActivityData
import com.specknet.pdiotapp.user.UserData
import com.specknet.pdiotapp.user.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class ActivityDataRepository(private val realDB: FirebaseDatabase) {

    var dailyActivities = emptyArray<ActivityData>()
    var weeklyActivities = emptyArray<ActivityData>()
    var monthlyActivities = emptyArray<ActivityData>()
    var advancedActivities = emptyArray<ActivityData>()
    var allActivities = mutableListOf<ActivityData>()

    private val TAG = "ActivityDataRepository"

    fun loadActivities() {
        CoroutineScope(Dispatchers.IO).launch {
            val hashedEmail = UserData.hashEmail(UserSession.getUserEmail()!!)
            realDB.reference.child(hashedEmail).addValueEventListener(object :
                ValueEventListener {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // dataSnapshot contains the data you retrieved
                    if (dataSnapshot.exists()) {
                        // Handle the retrieved data here
                        for (child in dataSnapshot.children) {
                            val date = child.key
                            val activities = child.value.toString().split(", ")
                            val activity = activities[0]
                            val subActivity = activities[1]
                            //Log.i(TAG, "Date: $date, Activity: $activity, Sub-activity: $subActivity")
                            allActivities.add(ActivityData(LocalDateTime.parse(date), activity, subActivity))
                        }
                        filterActivities("daily", "")
                        filterActivities("weekly", "")
                        filterActivities("monthly", "")
                    } else {
                        // Data does not exist at the specified location
                        //Log.i(TAG, "No data found")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle errors, if any
                    //Log.i(TAG, "Database error: " + databaseError.message)
                }
            })
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun filterActivities(filter: String, date: String) {
        val currentTime = LocalDateTime.now()
        when (filter) {
            "daily" -> {
                dailyActivities = allActivities.filter { it.date.toLocalDate() == currentTime.toLocalDate() }.toTypedArray()
            }
            "weekly" -> {
                weeklyActivities = allActivities.filter { it.date.isAfter(currentTime.minusDays(7)) }.toTypedArray()
            }
            "monthly" -> {
                monthlyActivities = allActivities.filter { it.date.isAfter(currentTime.minusDays(31)) }.toTypedArray()
            }
            "advanced" -> {
                advancedActivities = allActivities.filter { it.date.toLocalDate() == LocalDateTime.parse(date).toLocalDate() }.toTypedArray()
            }
        }
    }
}