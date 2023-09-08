package com.three.lifecycle

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.google.firebase.firestore.FieldPath
import kotlin.properties.Delegates

class HomeActivity : AppCompatActivity() {

    private val firestoreSingleton = FirestoreSingleton()
    private val db = firestoreSingleton.getInstance(this)
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userId: String
    private var autoLogin by Delegates.notNull<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        sharedPreferences = getSharedPreferences("UserPref", MODE_PRIVATE)
        userId = sharedPreferences.getString("userId", "").toString()
        autoLogin = sharedPreferences.getBoolean("autoLogin", false)


        if (userId.isNotEmpty()) {
            getUserSpec(userId)
        }else{
            val homeActivity = Intent(this, HomeActivity::class.java)
            startActivity(homeActivity)
        }

        setupListeners()
    }

    private fun setupListeners() {
        val settingsButton = findViewById<Button>(R.id.settingsButton)
        val homeButton = findViewById<Button>(R.id.homeButton)
        val logOutButton = findViewById<Button>(R.id.logoutButton)

        settingsButton.setOnClickListener { navigateToSettings() }
        homeButton.setOnClickListener { navigateToProfile() }
        logOutButton.setOnClickListener { logOut() }
    }

    private fun navigateToSettings() {
        val settingIntent = Intent(this, SettingsActivity::class.java)
        startActivity(settingIntent)
    }

    private fun navigateToProfile() {
        val homeActivity = Intent(this, HomeActivity::class.java)
        startActivity(homeActivity)
    }

    private fun logOut() {
        logoutUser(userId)
        clearUserData()
        val mainActivity = Intent(this, MainActivity::class.java)
        startActivity(mainActivity)
        finish()
    }

    override fun onStop() {
        super.onStop()
        if (!autoLogin) {
            logoutUser(userId)
            clearUserData()
        }
    }

    private fun logoutUser(userId: String) {
        try {
            val sharedPreferences = getSharedPreferences("UserPref", MODE_PRIVATE)
            sharedPreferences.edit {
                remove("userId")
                remove("email")
                remove("password")
                remove("isLoggedIn")
            }

            db.collection("users")
                .whereEqualTo(FieldPath.documentId(), userId)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        db.collection("users").document(document.id).update("isLoggedIn", false)
                            .addOnSuccessListener {
                                Log.w("validateLogin", "Succeeded updating document.")
                            }.addOnFailureListener { exception ->
                                Log.w("validateLogin", "Error updating document.", exception)
                            }
                    }
                }.addOnFailureListener { exception ->
                    Log.w("validateLogin", "Error querying database.", exception)
                }
        } catch (e: Exception) {
            Log.e("logoutUser", "Error logging out user", e)
        }
    }

    private fun getUserSpec(userId: String) {
        if (userId.isNotEmpty()) {
            val ageTextView = findViewById<TextView>(R.id.profilAgeTextView)
            val nameTextView = findViewById<TextView>(R.id.profileNameTextView)

            Log.d("Profilemanager", userId)

            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val ageNumber = document.getLong("age")
                        val nameString = document.getString("email")
                        val getIsLoggedIn = document.getBoolean("isLoggedIn")

                        Log.d("Profilemanager", "ageNumber: $ageNumber, nameString: $nameString")

                        val ageValue = ageNumber?.toString() ?: "N/A"
                        val nameValue = nameString ?: "N/A"

                        nameTextView.text = "Name: $nameValue"
                        ageTextView.text = "Age: $ageValue"
                    } else {
                        Log.d("Profilemanager", "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("Profilemanager", "Error getting document", exception)
                }
        } else {
            Log.e("Profilemanager", "userId is empty.")
        }
    }

    private fun clearUserData() {
        val sharedPreferences = getSharedPreferences("UserPref", MODE_PRIVATE)
        sharedPreferences.edit {
            remove("userId")
            remove("isLoggedIn")
        }
    }

}


