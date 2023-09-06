package com.three.lifecycle

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val firestoreSingleton = FirestoreSingleton()
    private val db = firestoreSingleton.getInstance(this)

    private var email: String = ""
    private var password: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {

            val emailEditText = findViewById<EditText>(R.id.inputEmailAddress)
            val passwordEditText = findViewById<EditText>(R.id.inputPassword)

            email = emailEditText.text.toString()
            password = passwordEditText.text.toString()

            validateLogin(email, password) { isValid ->

                if (isValid) {
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
}