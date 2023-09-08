package com.three.lifecycle

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val firestoreSingleton = FirestoreSingleton()
    private val db = firestoreSingleton.getInstance(this)

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var autoLoginCheckBox: CheckBox

    private var email: String = ""
    private var password: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        emailEditText = findViewById(R.id.inputEmailAddress)
        passwordEditText = findViewById(R.id.inputPassword)
        autoLoginCheckBox = findViewById(R.id.autoLoginCheckBox)

        val loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {

            email = emailEditText.text.toString()
            password = passwordEditText.text.toString()

            validateLogin(email, password) { isValid ->
                if (isValid) {

                    if (autoLoginCheckBox.isChecked) {
                        val sharedPreferences = getSharedPreferences("UserPref", MODE_PRIVATE)
                        val userPrefsEditor = sharedPreferences.edit()
                        userPrefsEditor.putBoolean("isLoggedIn", true)
                        userPrefsEditor.apply()
                    }

                    val profileIntent = Intent(this, HomeActivity::class.java)
                    profileIntent.putExtra("email", email)
                    startActivity(profileIntent)
                } else {
                    // Login failed
                }
            }
        }

        val registerButton = findViewById<Button>(R.id.registerButton)
        registerButton.setOnClickListener {

            val registerIntent = Intent(this, RegisterActivity::class.java)
            startActivity(registerIntent)
        }
    }
    private fun validateLogin(email: String, password: String, callback: (Boolean) -> Unit) {
        db.collection("users").whereEqualTo("email", email).get().addOnSuccessListener { result ->

                for (document in result) {
                    val passwordDatabase = document.getString("password")
                    if (password == passwordDatabase) {

                        db.collection("users").document(document.id).update("isLoggedIn", true)
                            .addOnSuccessListener {
                                callback(true)
                            }.addOnFailureListener { exception ->
                                Log.w("validateLogin", "Error updating document.", exception)
                                callback(false)
                            }
                        break
                    }
                }
            }.addOnFailureListener { exception ->
                Log.w("validateLogin", "Error querying database.", exception)
                callback(false)
            }
    }

    override fun onResume() {
        super.onResume()

        val sharedPreferences = getSharedPreferences("UserPref", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        val savedEmail = sharedPreferences.getString("email", "")
        val savedPassword = sharedPreferences.getInt("password", 0)

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

        val sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE)
        val userPrefsEditor = sharedPreferences.edit()

        userPrefsEditor.putString("email", emailEditText.text.toString())
        userPrefsEditor.putInt("password", passwordEditText.text.toString().toInt())
        userPrefsEditor.putBoolean("autoLogin", autoLoginCheckBox.isChecked)
        userPrefsEditor.apply()
    }
}