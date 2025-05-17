package com.specknet.pdiotapp.user

import com.specknet.pdiotapp.utils.Constants

class UserInformationValidator {

    companion object {
        fun isEmailValid(email: String): Boolean {
            val emailRegex = Constants.EMAIL_REGEX
            val regex = Regex(emailRegex)
            return regex.matches(email)
        }
        fun checkPasswordSyntax(password: String): Boolean {
            val passwordRegex = Constants.PASSWORD_REGEX
            val regex = Regex(passwordRegex)
            return regex.matches(password)
        }

        fun checkPasswordMatch(password1: String, password2: String): Boolean {
            return password1 == password2
        }

        fun isPhoneValid(phone: String): Boolean {
            val phoneRegex = Constants.PHONE_REGEX
            val regex = Regex(phoneRegex)
            return regex.matches(phone)
        }

    }
}