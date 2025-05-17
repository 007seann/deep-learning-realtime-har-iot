package com.specknet.pdiotapp.activity

import java.time.LocalDateTime

data class ActivityData(val date: LocalDateTime, val activity: String, val subActivity: String) {
    override fun toString(): String {
        return "$date, $activity, $subActivity"
    }
}