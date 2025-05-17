package com.specknet.pdiotapp.history

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.specknet.pdiotapp.R
import com.specknet.pdiotapp.activity.ActivityDataAdapter
import com.specknet.pdiotapp.user.UserSession


class HistoryActivity : AppCompatActivity() {

    private lateinit var dailyButton: Button
    private lateinit var weeklyButton: Button
    private lateinit var monthlyButton: Button
    private lateinit var advancedButton: Button

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ActivityDataAdapter

    private lateinit var dataRepos: ActivityDataRepository

    private var TAG = "HistoryActivity"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        dailyButton = findViewById(R.id.daily_button)
        weeklyButton = findViewById(R.id.weekly_button)
        monthlyButton = findViewById(R.id.monthly_button)
        advancedButton = findViewById(R.id.advanced_button)

        recyclerView = findViewById(R.id.recyclerView)

        setupClickListeners()

        dataRepos = ActivityDataRepository(UserSession.getRealDB())
        dataRepos.loadActivities()

        adapter = ActivityDataAdapter(arrayOf())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupClickListeners() {
        dailyButton.setOnClickListener{
            dailyButton.setBackgroundColor(resources.getColor(R.color.highlighted_button))
            weeklyButton.setBackgroundColor(resources.getColor(R.color.not_highlighted_button))
            monthlyButton.setBackgroundColor(resources.getColor(R.color.not_highlighted_button))
            advancedButton.setBackgroundColor(resources.getColor(R.color.not_highlighted_button))
            //Log.i("HistoryActivity", "Daily button clicked")
            runOnUiThread {
                adapter.updateData(dataRepos.dailyActivities)
            }
        }
        weeklyButton.setOnClickListener{
            dailyButton.setBackgroundColor(resources.getColor(R.color.not_highlighted_button))
            weeklyButton.setBackgroundColor(resources.getColor(R.color.highlighted_button))
            monthlyButton.setBackgroundColor(resources.getColor(R.color.not_highlighted_button))
            advancedButton.setBackgroundColor(resources.getColor(R.color.not_highlighted_button))
            //Log.i("HistoryActivity", "Weekly button clicked")
            runOnUiThread {
                adapter.updateData(dataRepos.weeklyActivities)
            }
        }
        monthlyButton.setOnClickListener{
            dailyButton.setBackgroundColor(resources.getColor(R.color.not_highlighted_button))
            weeklyButton.setBackgroundColor(resources.getColor(R.color.not_highlighted_button))
            monthlyButton.setBackgroundColor(resources.getColor(R.color.highlighted_button))
            advancedButton.setBackgroundColor(resources.getColor(R.color.not_highlighted_button))
            //Log.i("HistoryActivity", "Monthly button clicked")
            runOnUiThread {
                adapter.updateData(dataRepos.monthlyActivities)
            }
        }
        advancedButton.setOnClickListener {
            //Log.i("HistoryActivity", "All button clicked")
            dailyButton.setBackgroundColor(resources.getColor(R.color.not_highlighted_button))
            weeklyButton.setBackgroundColor(resources.getColor(R.color.not_highlighted_button))
            monthlyButton.setBackgroundColor(resources.getColor(R.color.not_highlighted_button))
            advancedButton.setBackgroundColor(resources.getColor(R.color.highlighted_button))
            val introIntent = Intent(this, AdvancedHistoryActivity::class.java)
            startActivityForResult(introIntent, 1)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, receivedData: Intent?) {
        super.onActivityResult(requestCode, resultCode, receivedData)
        if (requestCode === 1 && resultCode === RESULT_OK) {
            val date = receivedData?.getStringExtra("stringDate")
            Log.i("HistoryActivity", "Date received: $date")
            dataRepos.filterActivities("advanced", date!!)
            val advancedActivities = dataRepos.advancedActivities
            runOnUiThread {
                adapter.updateData(advancedActivities)
            }
        }
    }
}