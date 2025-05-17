package com.specknet.pdiotapp.user

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.specknet.pdiotapp.MainActivity
import com.specknet.pdiotapp.R
import com.specknet.pdiotapp.utils.Constants
import com.specknet.pdiotapp.utils.HashedPasswordGenerator

class RegisterActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput1: EditText
    private lateinit var passwordInput2: EditText
    private lateinit var personalPhoneInput: EditText
    private lateinit var emergencyPhoneInput: EditText
    private lateinit var fullNameInput: EditText

    private lateinit var emailIncorrectText: TextView
    private lateinit var passwordIncorrectText: TextView
    private lateinit var passwordNotMatchText: TextView

    private lateinit var registerButton: Button
    private lateinit var cancelButton: Button

    private val TAG = "RegisterActivity"

    private fun checkInputs(email: String, password1: String, password2: String): Boolean {
        if (!UserInformationValidator.isEmailValid(email)) {
            emailIncorrectText.text = Constants.INVALID_EMAIL_FORMAT
            emailIncorrectText.visibility = View.VISIBLE
            //Log.i(TAG, "Invalid email format")
            return false
        }
        if (!UserInformationValidator.checkPasswordSyntax(password1)) {
            passwordIncorrectText.visibility = View.VISIBLE
            //Log.i(TAG, "Password too short: $password1")
            return false
        }
        if (!UserInformationValidator.checkPasswordMatch(password1, password2)) {
            passwordNotMatchText.visibility = View.VISIBLE
            //Log.i(TAG, "Passwords do not match: $password1, $password2")
            return false
        }
        return true
    }

    private fun checkUserExistence(user: UserData, email: String) {
        //val hashedEmail = user.hashedEmail

        val userReference = UserSession.getRealDB().reference.child("users").child(user.email)
        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    //Log.i(TAG, "User ${user.email} exists.")
                    emailIncorrectText.text = Constants.USER_EXISTS
                    emailIncorrectText.visibility = View.VISIBLE
                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed({
                        emailIncorrectText.visibility = View.INVISIBLE
                        passwordIncorrectText.visibility = View.INVISIBLE
                        passwordNotMatchText.visibility = View.INVISIBLE
                    }, 2000)
                }
                else {
                    //Log.i(TAG, "User ${user.email} does not exist.")
                    emailIncorrectText.visibility = View.INVISIBLE
                    writeNewUser(user)

                    UserSession.setUserEmail(email)
                    // UserSession.setHashedUserEmail(user.email)
                    //Log.i(TAG, "User session set: ${UserSession.getUserEmail()}")
                    startMainActivity()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                //Log.i(TAG, "Error reading user data from database: $error")
            }
        })
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun writeNewUser(userData: UserData) {
        UserSession.getRealDB().getReference("users").child(userData.email).setValue(userData)
        //Log.i(TAG, "User ${userData.email} successfully written to database.")
    }

    private fun setupClickListeners() {
        registerButton.setOnClickListener {
            val email = emailInput.text.toString().replace(".", "^")
            val password1 = passwordInput1.text.toString()
            val password2 = passwordInput2.text.toString()
            val personalPhone = personalPhoneInput.text.toString()
            val emergencyPhone = emergencyPhoneInput.text.toString()
            val fullName = fullNameInput.text.toString()

            if (checkInputs(email.replace("^", "."), password1, password2)) {
                val saltedHashedPassword = HashedPasswordGenerator.hashPassword(password1)
                val user = UserData(email, saltedHashedPassword, personalPhone, emergencyPhone, fullName)
                checkUserExistence(user, email)
            }
            else {
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    emailIncorrectText.visibility = View.INVISIBLE
                    passwordIncorrectText.visibility = View.INVISIBLE
                    passwordNotMatchText.visibility = View.INVISIBLE
                }, 2000)
            }
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        emailIncorrectText = findViewById(R.id.invalidEmailText)
        passwordIncorrectText = findViewById(R.id.password_incorrect)
        passwordNotMatchText = findViewById(R.id.password_not_match)

        emailInput = findViewById(R.id.editTextEmailAddress)
        passwordInput1 = findViewById(R.id.editTextPassword1)
        passwordInput2 = findViewById(R.id.editTextPassword2)
        personalPhoneInput = findViewById(R.id.editTextPhone)
        emergencyPhoneInput = findViewById(R.id.editTextEmergencyPhone)
        fullNameInput = findViewById(R.id.editTextFullName)

        registerButton = findViewById(R.id.profile_apply_button)
        cancelButton = findViewById(R.id.profile_cancel_button)

        setupClickListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        finish()
    }
}