package com.three.lifecycle

import android.os.Bundle
import android.util.Log
import android.widget.Button
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
                if (!isLoggedIn) {
                    finish()
                }
            }
        }

        if (savedInstanceState == null) {

            val bundle = Bundle()
            bundle.putString("email", email)

            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()

            val myFragment = ProfileFragment()
            myFragment.arguments = bundle
            myFragment.setFirestoreReference(db)

            fragmentTransaction.add(R.id.fragment_container, myFragment)
            fragmentTransaction.commit()
        }

        val settingsButton = findViewById<Button>(R.id.settingsButton)
        settingsButton.setOnClickListener {

            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()

            val settingsFragment = SettingsFragment()
            fragmentTransaction.replace(R.id.fragment_container, settingsFragment)

            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }

        val homeButton = findViewById<Button>(R.id.homeButton)
        homeButton.setOnClickListener {

            val bundle = Bundle()
            bundle.putString("email", email)

            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()

            val myFragment = ProfileFragment()
            myFragment.arguments = bundle
            myFragment.setFirestoreReference(db)

            fragmentTransaction.replace(R.id.fragment_container, myFragment)
            fragmentTransaction.commit()
        }

        val logOutButton = findViewById<Button>(R.id.logoutButton)
        logOutButton.setOnClickListener {

            if (email != null) {
                logoutUser(email)
                finish()
            }
        }

    }

    private fun logoutUser(email: String) {

        db.collection("users").whereEqualTo("email", email).get().addOnSuccessListener { result ->
                for (document in result) {

                    db.collection("users").document(document.id).update("isLoggedIn", false)
                        .addOnSuccessListener {
                            Log.w("validateLogin", "Succeeded updating document.")
                        }.addOnFailureListener { exception ->
                            Log.w("validateLogin", "Error updating document.", exception)
                        }

                    break
                }
            }.addOnFailureListener { exception ->
                Log.w("validateLogin", "Error querying database.", exception)
            }

    }

    private fun isUserLoggedIn(email: String, callback: (Boolean) -> Unit) {

        db.collection("users").whereEqualTo("email", email).whereEqualTo("isLoggedIn", true).get()
            .addOnSuccessListener { result ->
                val isLoggedIn = !result.isEmpty
                Log.d("isUserLoggedIn", "User with email $email is logged in: $isLoggedIn")
                callback(isLoggedIn)
            }.addOnFailureListener { exception ->
                Log.w("isUserLoggedIn", "Error checking login status.", exception)
                callback(false)
            }
    }

}