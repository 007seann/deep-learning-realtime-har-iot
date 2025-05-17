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
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.specknet.pdiotapp.MainActivity
import com.specknet.pdiotapp.R
import com.specknet.pdiotapp.utils.HashedPasswordGenerator
import kotlin.system.exitProcess

class LoginActivity : AppCompatActivity() {

    // buttons and textviews
    private lateinit var loginButton: Button
    lateinit var registerButton: Button

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText

    private lateinit var errorText: TextView

    private val TAG = "LoginActivity"

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun verifyUser(email: String, password: String) {
        val userReference = UserSession.getRealDB().reference.child("users").child(email)
        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists() &&
                    HashedPasswordGenerator.verifyPassword(
                        password,
                        dataSnapshot.child("password").value.toString())
                ) {
                    //Log.i(TAG, "Valid credentials, $email, $password")
                    startMainActivity()

                    UserSession.setUserEmail(email)

                    emailInput.text.clear()
                    passwordInput.text.clear()
                }
                else {
                    //Log.i(TAG, "Invalid credentials, $email, $password")
                    errorText.visibility = View.VISIBLE

                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed({
                        errorText.visibility = View.GONE
                    }, 2000)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                //Log.i(TAG, "Error reading user data from database: $error")
                errorText.visibility = View.VISIBLE

                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    errorText.visibility = View.GONE
                }, 2000)
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginButton = findViewById(R.id.login_button)
        registerButton = findViewById(R.id.register_button)

        emailInput = findViewById(R.id.editTextTextEmailAddress)
        passwordInput = findViewById(R.id.editTextTextPassword)
        errorText = findViewById(R.id.error_text)

        setupClickListeners()

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }


        UserSession.clearUserEmail()
        UserSession.clearActivities()
    }

    private fun setupClickListeners() {
        loginButton.setOnClickListener {
            //Log.i(TAG, "Login button clicked")
            val email = emailInput.text.toString().replace(".", "^")
            val password = passwordInput.text.toString()

            verifyUser(email, password)
        }
        registerButton.setOnClickListener {
            //Log.i(TAG, "Register button clicked")
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exitProcess(0)
    }

}