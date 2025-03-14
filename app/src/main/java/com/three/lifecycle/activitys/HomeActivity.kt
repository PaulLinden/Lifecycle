package com.three.lifecycle.activitys

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.three.lifecycle.data.DatabaseManager
import com.three.lifecycle.R

class HomeActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        if (savedInstanceState == null){
        setUserSpec()
        }

        setupListeners()
    }

    private fun setupListeners() {
        val databaseManager = DatabaseManager(this)

        val settingsButton = findViewById<Button>(R.id.settingsButton)
        val logOutButton = findViewById<Button>(R.id.logoutButton)

        val navigationManager = NavigationManager(intent, this)

        settingsButton.setOnClickListener { navigationManager.navigateToSettings() }
        logOutButton.setOnClickListener {
            databaseManager.logoutUser()
            navigationManager.navigateToMain()
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        updateTextViewsFromPreferences()
    }

    override fun onStop() {
        super.onStop()

        val sharedPreferences = getSharedPreferences("UserPref", MODE_PRIVATE)
        val autoLogin = sharedPreferences.getBoolean("autoLogin", false)

        if (!autoLogin) {
            val databaseManager = DatabaseManager(this)
            databaseManager.logoutUser()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setUserSpec() {
        val databaseManager = DatabaseManager(this)

        databaseManager.getUserSpec(
            onSuccess = { user ->
                val ageTextView = findViewById<TextView>(R.id.profilAgeTextView)
                val nameTextView = findViewById<TextView>(R.id.profileNameTextView)
                val genderTextView = findViewById<TextView>(R.id.genderTextView)
                val driversLicTextView = findViewById<TextView>(R.id.driverLicTextView)

                val ageValue = user.age.toString()
                val nameValue = user.email
                val genderValue = user.gender
                val driverLicValue = user.drivingLicense

                nameTextView.text = "Name: $nameValue"
                ageTextView.text = "Age: $ageValue"
                genderTextView.text = "Gender: $genderValue"
                driversLicTextView.text = "Driving Lic: $driverLicValue"

                val sharedPreferences = getSharedPreferences("UserSpecPref", MODE_PRIVATE)
                sharedPreferences.edit {
                    putString("nameValue", nameValue)
                    putString("ageValue", ageValue)
                    putString("genderValue", genderValue)
                    putString("driverValue", driverLicValue.toString())
                }
            },
            onFailure = { exception ->
                Log.e("Profile manager", "Error getting document", exception)
            }
        )
    }

    @SuppressLint("SetTextI18n")
    private fun updateTextViewsFromPreferences() {
        val sharedPreferences = getSharedPreferences("UserSpecPref", MODE_PRIVATE)

        val ageTextView = findViewById<TextView>(R.id.profilAgeTextView)
        val nameTextView = findViewById<TextView>(R.id.profileNameTextView)
        val genderTextView = findViewById<TextView>(R.id.genderTextView)
        val driversLicTextView = findViewById<TextView>(R.id.driverLicTextView)

        val nameValue = sharedPreferences.getString("nameValue", "")
        val ageValue = sharedPreferences.getString("ageValue", "")
        val genderValue = sharedPreferences.getString("genderValue", "")
        val driverValue = sharedPreferences.getString("driverValue", "")

        if (nameValue != null && ageValue != null && genderValue != null && driverValue != null) {
            nameTextView.text = "Name: $nameValue"
            ageTextView.text = "Age: $ageValue"
            genderTextView.text = "Gender: $genderValue"
            driversLicTextView.text = "Driving Lic: $driverValue"
        }
}
}

