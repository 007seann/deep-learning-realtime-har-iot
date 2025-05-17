package com.specknet.pdiotapp.utils

import org.mindrot.jbcrypt.BCrypt

class HashedPasswordGenerator {
    companion object {
        fun hashPassword(plainTextPassword: String): String {
            val salt = BCrypt.gensalt(12) // Generate a random salt with cost factor 12 (adjust cost factor as needed)
            return BCrypt.hashpw(plainTextPassword, salt)
        }

        fun verifyPassword(plainTextPassword: String, hashedPassword: String): Boolean {
            return BCrypt.checkpw(plainTextPassword, hashedPassword)
        }
    }

}