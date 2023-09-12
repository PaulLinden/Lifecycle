package com.three.lifecycle.activitys

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.three.lifecycle.data.DatabaseManager
import com.three.lifecycle.R

class MainActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var autoLoginCheckBox: CheckBox

    private var email: String = ""
    private var password: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navigationManager = NavigationManager(intent, this)

        emailEditText = findViewById(R.id.inputEmailAddress)
        passwordEditText = findViewById(R.id.inputPassword)
        autoLoginCheckBox = findViewById(R.id.autoLoginCheckBox)


        val loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {

            email = emailEditText.text.toString()
            password = passwordEditText.text.toString()

            val databaseManager = DatabaseManager(this)
            databaseManager.validateLogin(email, password) { (isValid, documentId) ->
                if (isValid) {

                    if (autoLoginCheckBox.isChecked) {
                        val sharedPreferences = getSharedPreferences("UserPref", MODE_PRIVATE)
                        val userPrefsEditor = sharedPreferences.edit()
                        userPrefsEditor.putBoolean("isLoggedIn", true)
                        userPrefsEditor.putString("userId", documentId)
                        userPrefsEditor.putBoolean("autoLogin", true)
                        userPrefsEditor.apply()
                    }

                    val sharedPreferences = getSharedPreferences("UserPref", MODE_PRIVATE)
                    val userPrefsEditor = sharedPreferences.edit()
                    userPrefsEditor.putBoolean("isLoggedIn", true)
                    userPrefsEditor.putString("userId", documentId)
                    userPrefsEditor.apply()

                    navigationManager.navigateToProfile()

                } else {
                    Log.d("Validation_Error","Validation Error")
                }
            }
        }

        val registerButton = findViewById<Button>(R.id.submitButton)
        registerButton.setOnClickListener {
            navigationManager.navigateToRegister()
        }
    }

    override fun onResume() {
        super.onResume()

        val sharedPreferences = getSharedPreferences("UserPref", MODE_PRIVATE)

        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        val savedEmail = sharedPreferences.getString("email", "")
        val savedPassword = sharedPreferences.getString("password", "")

        emailEditText.setText(savedEmail)
        passwordEditText.setText(savedPassword.toString())

        if (isLoggedIn) {
            val profileIntent = Intent(this, HomeActivity::class.java)
            profileIntent.putExtra("email", savedEmail)
            startActivity(profileIntent)
            finish()
        }
    }
    override fun onPause() {
        super.onPause()

        val sharedPreferences = getSharedPreferences("UserPref", MODE_PRIVATE)
        val userPrefsEditor = sharedPreferences.edit()

        userPrefsEditor.putString("email", emailEditText.text.toString())
        userPrefsEditor.putString("password", passwordEditText.text.toString())
        userPrefsEditor.putBoolean("autoLogin", autoLoginCheckBox.isChecked)
        userPrefsEditor.apply()
    }

}
