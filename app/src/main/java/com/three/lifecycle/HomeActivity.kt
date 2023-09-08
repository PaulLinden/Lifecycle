package com.three.lifecycle

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FieldPath

class HomeActivity : AppCompatActivity() {

    private lateinit var userId: String
    private val fragmentManager = supportFragmentManager
    private val firestoreSingleton = FirestoreSingleton()
    private var currentFragmentTag: String? = null
    private val db = firestoreSingleton.getInstance(this)

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("userId", userId)
        currentFragmentTag?.let { outState.putString("currentFragmentTag", it) }
        super.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val sharedPreferences = getSharedPreferences("UserPref", MODE_PRIVATE)
        userId = sharedPreferences.getString("userId", "").toString()

        if (savedInstanceState == null) {
            setupProfileFragment()
        }

        setupListeners()
    }

    private fun setupProfileFragment() {
        val profileFragment = ProfileFragment()
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.addToBackStack(null)
        profileFragment.setFirestoreReference(db)

        val bundle = Bundle().apply {
            val sharedPreferences = getSharedPreferences("UserPref", MODE_PRIVATE)
            userId = sharedPreferences.getString("userId", "").toString()
            putString("userId", userId)
        }

        profileFragment.arguments = bundle
        fragmentTransaction.add(R.id.fragment_container, profileFragment)
        fragmentTransaction.commit()
    }

    private fun setupListeners() {
        val settingsButton = findViewById<Button>(R.id.settingsButton)
        val homeButton = findViewById<Button>(R.id.homeButton)
        val logOutButton = findViewById<Button>(R.id.logoutButton)

        settingsButton.setOnClickListener { navigateToSettings() }
        homeButton.setOnClickListener { navigateToProfileFragment() }
        logOutButton.setOnClickListener { logOut() }
    }

    private fun navigateToSettings() {
        val settingsFragment = SettingsFragment()
        settingsFragment.setFirestoreReference(db)

        val bundle = Bundle().apply {
            putString("userId", userId)
        }

        settingsFragment.arguments = bundle

        replaceFragment(settingsFragment)
    }

    private fun navigateToProfileFragment() {
        val profileFragment = ProfileFragment()
        profileFragment.setFirestoreReference(db)

        val bundle = Bundle().apply {
            putString("userId", userId)
        }

        profileFragment.arguments = bundle

        replaceFragment(profileFragment)
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }

    private fun logOut() {
        if (userId.isNotEmpty()) {
            logoutUser(userId)
            finish()
        }
    }

    private fun logoutUser(userId: String) {
        try {
            val sharedPreferences = getSharedPreferences("UserPref", MODE_PRIVATE)
            sharedPreferences.edit {
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
}

