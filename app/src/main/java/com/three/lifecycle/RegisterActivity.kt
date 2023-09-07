package com.three.lifecycle

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private val firestoreSingleton = FirestoreSingleton()
    private val db = firestoreSingleton.getInstance(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val inputNewEmailAddress = findViewById<EditText>(R.id.inputNewEmailAdress)
        val inputNewPassword = findViewById<EditText>(R.id.inputNewPassword)
        val inputNewTitle = findViewById<EditText>(R.id.inputNewTitle)
        val inputNewAddress = findViewById<EditText>(R.id.inputNewAdress)
        val inputNewAge = findViewById<EditText>(R.id.inputNewAge)

        val registerButton = findViewById<Button>(R.id.registerButton)
        registerButton.setOnClickListener {

            val newUser = User(
                email = inputNewEmailAddress.text.toString(),
                password = inputNewPassword.text.toString(),
                title = inputNewTitle.text.toString(),
                address = inputNewAddress.text.toString(),
                age = inputNewAge.text.toString().toIntOrNull() ?: 0
            )

            addUserToDatabase(newUser)

            val mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)
        }
    }

    private fun addUserToDatabase(user: User) {
        db.collection("users")
            .add(user)
            .addOnSuccessListener { documentReference ->
                Log.d("validateLogin", "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("validateLogin", "Error adding document", e)
            }
    }
}

