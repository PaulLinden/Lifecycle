package com.three.lifecycle.activitys

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.three.lifecycle.data.DatabaseManager
import com.three.lifecycle.R
import com.three.lifecycle.data.User


class RegisterActivity : AppCompatActivity() {

    private var saveEmailInput: String = ""
    private var savePasswordInput: String = ""
    private var saveAgeInput: String = ""
    private var savedGenderId: Int? = null
    private var saveCheckBoxState: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val navigationManager = NavigationManager(intent, this)

        if (savedInstanceState != null) {
            savedInstanceState.getString("email_value")
            savedInstanceState.getString("password_value")
            savedInstanceState.getBoolean("checkBox_value")
            savedInstanceState.getString("password_value")
            savedInstanceState.getBoolean("gender_value")
            savedInstanceState.getString("age_value")
        }

        val inputNewEmailAddress = findViewById<EditText>(R.id.inputNewEmailAdress)
        val inputNewPassword = findViewById<EditText>(R.id.inputNewPassword)
        val checkBoxDrivingLicense = findViewById<CheckBox>(R.id.checkBoxDrivingLicense)
        val radioGroupGender = findViewById<RadioGroup>(R.id.gender)
        val inputNewAge = findViewById<EditText>(R.id.inputNewAge)

        val uiList = mutableListOf(inputNewEmailAddress, inputNewPassword, inputNewAge)
        uiList.forEach { ui ->
            ui.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    when (ui) {
                        inputNewEmailAddress -> {
                            saveEmailInput = p0.toString()
                        }

                        inputNewPassword -> {
                            savePasswordInput = p0.toString()
                        }

                        inputNewAge -> {
                            saveAgeInput = p0.toString()
                        }

                        else -> {}
                    }
                }

                override fun afterTextChanged(p0: Editable?) {}
            })
        }

        checkBoxDrivingLicense.setOnCheckedChangeListener { _, isChecked ->
            saveCheckBoxState = isChecked
        }

        radioGroupGender.setOnCheckedChangeListener { _, checkedId ->
            savedGenderId = checkedId
        }

        val registerButton = findViewById<Button>(R.id.submitButton)
        registerButton.setOnClickListener {

            val newUser = User(
                email = inputNewEmailAddress.text.toString(),
                password = inputNewPassword.text.toString(),
                drivingLicense = checkBoxDrivingLicense.isChecked,
                gender = when (radioGroupGender.checkedRadioButtonId) {
                    R.id.radioMale -> "Male"
                    R.id.radioFemale -> "Female"
                    else -> "Other"
                },
                age = inputNewAge.text.toString().toIntOrNull() ?: 0
            )

            val databaseManager = DatabaseManager(this)
            databaseManager.addUserToDatabase(newUser)

            navigationManager.navigateToMain()
        }

        val cancelButton = findViewById<Button>(R.id.cancel_button)
        cancelButton.setOnClickListener {

            navigationManager.navigateToMain()
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

