package com.three.lifecycle.activitys

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.three.lifecycle.data.DatabaseManager
import com.three.lifecycle.R
import com.three.lifecycle.data.User

class SettingsActivity : AppCompatActivity() {

    private var saveEmailInput: String = ""
    private var savePasswordInput: String = ""
    private var saveAgeInput: String = ""
    private var savedGenderId: Int? = null
    private var saveCheckBoxState: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setupUI(savedInstanceState)
        setupListeners()
    }

    private fun setupUI(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            saveEmailInput = savedInstanceState.getString("email_value").toString()
            savePasswordInput = savedInstanceState.getString("password_value").toString()
            saveAgeInput = savedInstanceState.getString("age_value").toString()
            saveCheckBoxState = savedInstanceState.getBoolean("auto_login_value")
            savedGenderId = savedInstanceState.getInt("gender_value", -1)
        }

        val emailEditText = findViewById<EditText>(R.id.inputNewEmailAdress)
        val passwordEditText = findViewById<EditText>(R.id.inputNewPassword)
        val ageEditText = findViewById<EditText>(R.id.inputNewAge)

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val view = currentFocus as? EditText

                when (view?.tag) {
                    "email" -> saveEmailInput = s.toString()
                    "password" -> savePasswordInput = s.toString()
                    "age" -> saveAgeInput = s.toString()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        emailEditText.addTextChangedListener(textWatcher)
        emailEditText.tag = "email"

        passwordEditText.addTextChangedListener(textWatcher)
        passwordEditText.tag = "password"

        ageEditText.addTextChangedListener(textWatcher)
        ageEditText.tag = "age"

        val updateDrivingLicense = findViewById<CheckBox>(R.id.checkBoxDrivingLicense)
        updateDrivingLicense.setOnCheckedChangeListener { _, isChecked ->
            saveCheckBoxState = isChecked
        }

        val updateGroupGender = findViewById<RadioGroup>(R.id.gender)
        updateGroupGender.setOnCheckedChangeListener { group, checkedId ->
            savedGenderId = when (checkedId) {
                R.id.radioMale -> 0
                R.id.radioFemale -> 1
                else -> null
            }
        }
    }

    private fun setupListeners() {
        val databaseManager = DatabaseManager(this)

        val homeButton = findViewById<Button>(R.id.homeButton)
        val logOutButton = findViewById<Button>(R.id.logoutButton)
        val submitButton = findViewById<Button>(R.id.submitButton)
        val cancelButton = findViewById<Button>(R.id.cancel_button_2)
        val navigationManager = NavigationManager(intent, this)

        submitButton.setOnClickListener {
            val user = User(
                saveEmailInput,
                savePasswordInput,
                saveCheckBoxState,
                if (savedGenderId == 0) "Male" else "Female",
                saveAgeInput.toInt()
            )

            databaseManager.updateUserInDatabase(user)
            Toast.makeText(this,"Updating", Toast.LENGTH_LONG).show()
        }
        cancelButton.setOnClickListener { navigationManager.navigateToProfile() }
        homeButton.setOnClickListener { navigationManager.navigateToProfile() }
        logOutButton.setOnClickListener {
            databaseManager.logoutUser()
            navigationManager.navigateToMain()
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString("email_value", saveEmailInput)
        outState.putString("password_value", savePasswordInput)
        outState.putString("age_value", saveAgeInput)
        outState.putBoolean("auto_login_value", saveCheckBoxState)
        outState.putInt("gender_value", savedGenderId ?: -1)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        saveEmailInput = savedInstanceState.getString("email_value").toString()
        savePasswordInput = savedInstanceState.getString("password_value").toString()
        saveAgeInput = savedInstanceState.getString("age_value").toString()
        saveCheckBoxState = savedInstanceState.getBoolean("auto_login_value")
        savedGenderId = savedInstanceState.getInt("gender_value", -1)
    }
}

