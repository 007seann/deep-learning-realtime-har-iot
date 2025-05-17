package com.specknet.pdiotapp.user

import java.security.MessageDigest

class UserData(
    val email: String,
    val password: String,
    val personalPhone: String,
    val emergencyPhone: String,
    val fullName: String,
) {
    companion object {
        fun hashEmail(email: String): String {
            val messageDigest = MessageDigest.getInstance("SHA-256")
            val bytes = messageDigest.digest(email.toByteArray())
            val hexString = StringBuilder(2 * bytes.size)
            for (byte in bytes) {
                val hex = String.format("%02x", byte)
                hexString.append(hex)
            }
            return hexString.toString()
        }
    }

}