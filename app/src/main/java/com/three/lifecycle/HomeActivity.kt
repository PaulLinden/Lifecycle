package com.three.lifecycle

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    private val firestoreSingleton = FirestoreSingleton()
    private val db = firestoreSingleton.getInstance(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val email = intent.getStringExtra("email")
        if (email != null) {

            isUserLoggedIn(email) { isLoggedIn ->
                if (isLoggedIn) {
                    getUserSpec(email)
                } else {
                    finish()
                }
            }
        } else {
            finish()
        }
        
        if (savedInstanceState == null) {
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            val myFragment = ProfileFragment()
            fragmentTransaction.add(R.id.fragment_container, myFragment)
            fragmentTransaction.commit()
        }
        
        val settingsButton = findViewById<Button>(R.id.settingsButton)
        settingsButton.setOnClickListener {

            val settingsIntent = Intent(this, SettingsFragment::class.java)
            startActivity(settingsIntent)
        }

        val logOutButton = findViewById<Button>(R.id.logoutButton)
        logOutButton.setOnClickListener {

            if (email != null) {
                logoutUser(email)
                finish()
            }
        }

    }

    private fun getUserSpec(email:String) {
        var ageOutput = ""
        var titleOutput = ""

        val ageTextView = findViewById<TextView>(R.id.profilAgeTextView)
        val titleTextView = findViewById<TextView>(R.id.profilTitleTextView)

        db.collection("users")
            .whereEqualTo("email", email)
            .whereEqualTo("isLoggedIn", true)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {

                    val ageNumber = document.getLong("age")
                    val titleString = document.getString("title")

                    if (ageNumber != null && titleString != null) {
                        ageOutput = ageNumber.toString()
                        titleOutput = titleString
                    }

                }
                val agePlaceholder = getString(R.string.age_placeholder)
                val titlePlaceholder = getString(R.string.title_placeholder)
                val formattedAge = String.format(agePlaceholder, ageOutput)

                ageTextView.text = formattedAge
                titleTextView.text = String.format(titlePlaceholder, titleOutput)
            }
            .addOnFailureListener { exception ->
                Log.w("read", "Error getting documents.", exception)
            }
    }

    private fun logoutUser(email: String) {

        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {

                    db.collection("users")
                        .document(document.id)
                        .update("isLoggedIn", false)
                        .addOnSuccessListener {
                            Log.w("validateLogin", "Succeeded updating document.")
                        }
                        .addOnFailureListener { exception ->
                            Log.w("validateLogin", "Error updating document.", exception)
                        }

                    break
                }
            }
            .addOnFailureListener{ exception ->
                Log.w("validateLogin", "Error querying database.", exception)
            }

    }

    private fun isUserLoggedIn(email: String, callback: (Boolean) -> Unit) {

        db.collection("users")
            .whereEqualTo("email", email)
            .whereEqualTo("isLoggedIn", true)
            .get()
            .addOnSuccessListener { result ->
                val isLoggedIn = !result.isEmpty
                Log.d("isUserLoggedIn", "User with email $email is logged in: $isLoggedIn")
                callback(isLoggedIn)
            }
            .addOnFailureListener { exception ->
                Log.w("isUserLoggedIn", "Error checking login status.", exception)
                callback(false)
            }
    }
}
