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

        // Initialize UI elements
        emailEditText = findViewById(R.id.inputEmailAddress)
        passwordEditText = findViewById(R.id.inputPassword)
        autoLoginCheckBox = findViewById(R.id.autoLoginCheckBox)

        // Set up click listener for login button
        val loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {

            email = emailEditText.text.toString()
            password = passwordEditText.text.toString()

            validateLogin(email, password) { (isValid, documentId) ->
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

                    val profileIntent = Intent(this, HomeActivity::class.java)
                    startActivity(profileIntent)

                } else {
                    Log.d("Validation_Error","Validation Error")
                }
            }
        }

        // Set up click listener for register button
        val registerButton = findViewById<Button>(R.id.submitButton)
        registerButton.setOnClickListener {

            val registerIntent = Intent(this, RegisterActivity::class.java)
            startActivity(registerIntent)
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
    private fun validateLogin(email: String, password: String, callback: (Pair<Boolean, String?>) -> Unit) {
        db.collection("users").whereEqualTo("email", email).get().addOnSuccessListener { result ->

            for (document in result) {
                val verifyPassword = document.getString("password")
                if (password == verifyPassword) {

                    db.collection("users").document(document.id).update("isLoggedIn", true)
                        .addOnSuccessListener {
                            // Pass both the result and the document ID
                            callback(Pair(true, document.id))
                        }.addOnFailureListener { exception ->
                            Log.w("validateLogin", "Error updating document.", exception)
                            callback(Pair(false, null))
                        }
                    return@addOnSuccessListener
                }
            }
            callback(Pair(false, null))

        }.addOnFailureListener { exception ->
            Log.w("validateLogin", "Error querying database.", exception)
            callback(Pair(false, null))
        }
    }
}
