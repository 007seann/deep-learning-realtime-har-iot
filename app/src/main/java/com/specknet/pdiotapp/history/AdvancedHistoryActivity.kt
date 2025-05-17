package com.specknet.pdiotapp.history

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.specknet.pdiotapp.R
import java.time.LocalDateTime

class AdvancedHistoryActivity : AppCompatActivity() {

    private lateinit var applyButton: Button
    private lateinit var cancelButton: Button

    private lateinit var calendarView: CalendarView

    private lateinit var date: LocalDateTime

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_advanced_history)

        applyButton = findViewById(R.id.apply_button)
        cancelButton = findViewById(R.id.cancel_calendar_button)

        calendarView = findViewById(R.id.calendarView)
        date = LocalDateTime.now()
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            date = LocalDateTime.of(year, month + 1, dayOfMonth, 0, 0)
            //Log.i("AdvancedHistoryActivity", "Date selected: $date")
        }

        setupClickListeners()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupClickListeners() {
        applyButton.setOnClickListener {
            //Log.i("AdvancedHistoryActivity", "Apply button clicked")
            val resultIntent = Intent()
            val stringDate = date.toString()
            resultIntent.putExtra("stringDate", stringDate)
            setResult(RESULT_OK, resultIntent)
            //Log.i("AdvancedHistoryActivity", "Date sent: $stringDate")
            finish()
        }
        cancelButton.setOnClickListener {
            //Log.i("AdvancedHistoryActivity", "Cancel button clicked")
            finish()
        }
    }

}