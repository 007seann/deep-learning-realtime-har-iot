package com.specknet.pdiotapp.user

import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.google.firebase.Firebase
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import com.specknet.pdiotapp.activity.ActivityData


object UserSession {
    private var userEmail: String? = null
    private var activityDataList: MutableList<ActivityData> = mutableListOf()
    private var TAG = "UserSession"
    private var realDB = Firebase.database("https://pdiot-6a52b-default-rtdb.europe-west1.firebasedatabase.app")
    private var module = Python.getInstance().getModule("main")

    fun getRealDB(): FirebaseDatabase {
        return realDB
    }

    fun addActivity(activityData: ActivityData) {
        activityDataList.add(activityData)
        //Log.i(TAG, "Activity added: $activityData")
    }

    fun getActivities(): MutableList<ActivityData> {
        //Log.i(TAG, "Activities retrieved: $activityDataList")
        return activityDataList
    }

    fun clearActivities() {
        activityDataList.clear()
        //Log.i(TAG, "Activities cleared.")
    }

    fun setActivities(activities: MutableList<ActivityData>) {
        activityDataList = activities
        //Log.i(TAG, "Activities set: $activityDataList")
    }

    fun setUserEmail(email: String) {
        userEmail = email
        //Log.i(TAG, "User email set: $userEmail")
    }

    fun getUserEmail(): String? {
        //Log.i(TAG, "Hashed user email retrieved: $userEmail")
        return userEmail
    }

    fun clearUserEmail() {
        userEmail = null
        //Log.i(TAG, "User email cleared.")
    }

    fun getModule(): PyObject {
        return module
    }

}