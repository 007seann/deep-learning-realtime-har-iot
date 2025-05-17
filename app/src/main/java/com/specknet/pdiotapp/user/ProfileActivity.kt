package com.specknet.pdiotapp.user

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.specknet.pdiotapp.R
import com.specknet.pdiotapp.utils.HashedPasswordGenerator

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileEditEmail: EditText
    private lateinit var profileEditPhone: EditText
    private lateinit var profileEditName: EditText
    private lateinit var profileCurrentPassword: EditText
    private lateinit var profileNewPassword: EditText
    private lateinit var profileEmergencyPhone: EditText

    private lateinit var applyPersonalPhoneButton: Button
    private lateinit var applyFullNameButton: Button
    private lateinit var applyPasswordButton: Button
    private lateinit var applyEmergencyPhoneNumberButton: Button

    private lateinit var incorrectPhoneNumberFormat: TextView
    private lateinit var nameEmpty: TextView
    private lateinit var passwordNotMatch: TextView
    private lateinit var incorrectNewPasswordFormat: TextView
    private lateinit var incorrectEmergencyPhoneNumberFormat: TextView

    private var handler = Handler(Looper.getMainLooper())

    private lateinit var cancelButton: Button

    private var TAG = "ProfileActivity"

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profileEditEmail = findViewById(R.id.profileEditEmail)
        profileEditPhone = findViewById(R.id.profileEditPhone)
        profileEditName = findViewById(R.id.profileEditName)
        profileCurrentPassword = findViewById(R.id.profileCurrentPassword)
        profileNewPassword = findViewById(R.id.profileNewPassword)
        profileEmergencyPhone = findViewById(R.id.profileEditEmergencyPhone)

        applyPersonalPhoneButton = findViewById(R.id.personal_phone_apply_button)
        applyFullNameButton = findViewById(R.id.full_name_apply_button)
        applyPasswordButton = findViewById(R.id.password_apply_button)
        applyEmergencyPhoneNumberButton = findViewById(R.id.emergency_phone_apply_button)
        cancelButton = findViewById(R.id.profile_cancel_button)

        incorrectPhoneNumberFormat = findViewById(R.id.incorrect_phone_number_format)
        nameEmpty = findViewById(R.id.name_empty)
        passwordNotMatch = findViewById(R.id.profile_password_incorrect)
        incorrectNewPasswordFormat = findViewById(R.id.profile_new_password_incorrect)
        incorrectEmergencyPhoneNumberFormat = findViewById(R.id.incorrect_emergency_phone_number_format)

        setupClickListeners()

        setupEditTexts()

    }

    private fun setupEditTexts() {
        UserSession.getRealDB().reference.child("users").child(UserSession.getUserEmail()!!).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    profileEditEmail.setText(UserSession.getUserEmail()?.replace("^", "."))
                    profileEditPhone.setText(dataSnapshot.child("personalPhone").value.toString())
                    profileEditName.setText(dataSnapshot.child("fullName").value.toString())
                    profileEmergencyPhone.setText(dataSnapshot.child("emergencyPhone").value.toString())
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                //Log.i(TAG, "Database error: $databaseError")
            }
        })
    }

    private fun editPhone() {
        val newPhone = profileEditPhone.text.toString()
        if (!UserInformationValidator.isPhoneValid(newPhone)) {
            //Log.i(TAG, "New phone number is not valid")
            incorrectPhoneNumberFormat.visibility = TextView.VISIBLE
            // Go back to INVISIBLE after 2 seconds
            handler.postDelayed({
                incorrectPhoneNumberFormat.visibility = TextView.INVISIBLE
            }, 2000)
            return
        }
        UserSession.getRealDB().reference.child("users").child(UserSession.getUserEmail()!!).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    UserSession.getRealDB().reference.
                    child("users").child(UserSession.getUserEmail()!!).
                    child("personalPhone").setValue(newPhone)
                    incorrectPhoneNumberFormat.text = "Applied!"
                    incorrectPhoneNumberFormat.visibility = TextView.VISIBLE
                    handler.postDelayed({
                        incorrectPhoneNumberFormat.text = "Format: 5 digits + Space + 5 Digits"
                        incorrectPhoneNumberFormat.visibility = TextView.INVISIBLE
                    }, 2000)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                //Log.i(TAG, "Database error: $databaseError")
            }
        })
    }

    private fun editName() {
        val newName = profileEditName.text.toString()
        if (newName.isEmpty()) {
            //Log.i(TAG, "New name is empty")
            nameEmpty.visibility = TextView.VISIBLE
            // Go back to INVISIBLE after 2 seconds
            handler.postDelayed({
                nameEmpty.visibility = TextView.INVISIBLE
            }, 2000)
            return
        }
        UserSession.getRealDB().reference.child("users").child(UserSession.getUserEmail()!!).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    UserSession.getRealDB().reference.
                    child("users").child(UserSession.getUserEmail()!!).
                    child("fullName").setValue(newName)
                    nameEmpty.text = "Applied!"
                    nameEmpty.visibility = TextView.VISIBLE
                    handler.postDelayed({
                        nameEmpty.text = "Name cannot be empty"
                        nameEmpty.visibility = TextView.INVISIBLE
                    }, 2000)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                //Log.i(TAG, "Database error: $databaseError")
            }
        })
    }

    private fun editPassword() {
        val currentPassword = profileCurrentPassword.text.toString()
        val newPassword = profileNewPassword.text.toString()
        if (!UserInformationValidator.checkPasswordSyntax(newPassword)) {
            //Log.i(TAG, "New password is not valid")
            incorrectNewPasswordFormat.visibility = TextView.VISIBLE
            // Go back to INVISIBLE after 2 seconds
            handler.postDelayed({
                incorrectNewPasswordFormat.visibility = TextView.INVISIBLE
            }, 2000)
            return
        }
        UserSession.getRealDB().reference.child("users").child(UserSession.getUserEmail()!!).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (HashedPasswordGenerator.verifyPassword(currentPassword, dataSnapshot.child("password").value.toString())) {
                        //Log.i(TAG, "Current password is correct")
                        UserSession.getRealDB().reference.
                        child("users").child(UserSession.getUserEmail()!!).
                        child("password").setValue(HashedPasswordGenerator.hashPassword(newPassword))
                        incorrectNewPasswordFormat.text = "Applied!"
                        incorrectNewPasswordFormat.visibility = TextView.VISIBLE
                        handler.postDelayed({
                            incorrectNewPasswordFormat.text = "8 Characters Min. 1 letter and 1 number."
                            incorrectNewPasswordFormat.visibility = TextView.INVISIBLE
                        }, 2000)
                    }
                    else {
                        passwordNotMatch.visibility = TextView.VISIBLE
                        // Go back to INVISIBLE after 2 seconds
                        handler.postDelayed({
                            passwordNotMatch.visibility = TextView.INVISIBLE
                        }, 2000)
                        //Log.i(TAG, "Current password is incorrect")
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                //Log.i(TAG, "Database error: $databaseError")
            }
        })
    }

    private fun editEmergencyPhone() {
        val newEmergencyPhone = profileEmergencyPhone.text.toString()
        if (!UserInformationValidator.isPhoneValid(newEmergencyPhone)) {
            //Log.i(TAG, "New emergency phone number is not valid")
            incorrectEmergencyPhoneNumberFormat.visibility = TextView.VISIBLE
            // Go back to INVISIBLE after 2 seconds
            handler.postDelayed({
                incorrectEmergencyPhoneNumberFormat.visibility = TextView.INVISIBLE
            }, 2000)
            return
        }
        UserSession.getRealDB().reference.child("users").child(UserSession.getUserEmail()!!).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    UserSession.getRealDB().reference.
                    child("users").child(UserSession.getUserEmail()!!).
                    child("emergencyPhone").setValue(newEmergencyPhone)
                    incorrectEmergencyPhoneNumberFormat.text = "Applied!"
                    incorrectEmergencyPhoneNumberFormat.visibility = TextView.VISIBLE
                    handler.postDelayed({
                        incorrectEmergencyPhoneNumberFormat.text = "Format: 5 digits + Space + 5 Digits"
                        incorrectEmergencyPhoneNumberFormat.visibility = TextView.INVISIBLE
                    }, 2000)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                //Log.i(TAG, "Database error: $databaseError")
            }
        })
    }

    private fun setupClickListeners() {
        applyPersonalPhoneButton.setOnClickListener {
            editPhone()
        }
        applyFullNameButton.setOnClickListener {
            editName()
        }
        applyPasswordButton.setOnClickListener {
            editPassword()
        }
        applyEmergencyPhoneNumberButton.setOnClickListener {
            editEmergencyPhone()
        }
        cancelButton.setOnClickListener {
            Log.i(TAG, "Cancel button clicked")
            finish()
        }
    }



}