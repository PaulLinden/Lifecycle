package com.three.lifecycle

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.core.content.edit
import com.google.firebase.firestore.FieldPath

class DatabaseManager(private  val context: Context) {

    private val firestoreSingleton = FirestoreSingleton()
    private val db = firestoreSingleton.getInstance(context)
    private val sharedPreferences = context.getSharedPreferences("UserPref", MODE_PRIVATE)
    private val userId = sharedPreferences.getString("userId", "").toString()

    fun addUserToDatabase(user: User) {
        db.collection("users")
            .add(user)
            .addOnSuccessListener { documentReference ->
                Log.d("validateLogin", "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.d("validateLogin", "Error adding document", e)
            }
    }
    fun updateUserInDatabase(user: User) {
        db.collection("users")
            .whereEqualTo(FieldPath.documentId(), userId)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val docRef = db.collection("users").document(document.id)

                    val updates = mapOf(
                        "email" to user.email,
                        "password" to user.password,
                        "drivingLicense" to user.drivingLicense,
                        "gender" to user.gender,
                        "age" to user.age
                    )

                    docRef.update(updates)
                        .addOnSuccessListener {
                            Log.d("validateLogin", "Succeeded updating document.")

                            // Update SharedPreferences after successful update
                            val sharedPreferences = context.getSharedPreferences("UserSpecPref", MODE_PRIVATE)
                            sharedPreferences.edit {
                                putString("nameValue", user.email)
                                putString("ageValue", user.age.toString())
                            }
                        }.addOnFailureListener { exception ->
                            Log.d("validateLogin", "Error updating document.", exception)
                        }
                    break
                }
            }.addOnFailureListener { exception ->
                Log.d("validateLogin", "Error querying database.", exception)
            }
    }

    fun logoutUser() {
        try {
            db.collection("users")
                .document(userId)
                .update("isLoggedIn", false)
                .addOnSuccessListener {
                    Log.w("validateLogin", "Succeeded updating document.")
                }
                .addOnFailureListener { exception ->
                    Log.w("validateLogin", "Error updating document.", exception)
                }
        } catch (e: Exception) {
            Log.e("logoutUser", "Error logging out user", e)
        }

        sharedPreferences.edit {
            clearUserData()
        }
    }
    private fun clearUserData() {
        val sharedPreferences = context.getSharedPreferences("UserPref", MODE_PRIVATE)
        sharedPreferences.edit {
            clear()
        }
    }
    fun getUserSpec(onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit) {

        if (userId.isNotEmpty()) {
            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val ageNumber = document.getLong("age")
                        val nameString = document.getString("email")
                        val driver = document.getBoolean("drivingLicense")
                        val gender = document.getString("gender")

                        val ageValue = ageNumber?.toString() ?: "N/A"
                        val nameValue = nameString ?: "N/A"
                        val genderValue = gender ?: "N/A"

                        val user = driver?.let {
                            User(
                                email = nameValue,
                                password = "",
                                drivingLicense = it,
                                gender = genderValue,
                                age = ageValue.toIntOrNull() ?: 0
                            )
                        }

                        if (user != null) {
                            onSuccess(user)
                        }
                    } else {
                        onFailure(Exception("No such document"))
                    }
                }
                .addOnFailureListener { exception ->
                    onFailure(exception)
                }
        } else {
            onFailure(Exception("userId is empty"))
        }
    }
    fun validateLogin(email: String, password: String, callback: (Pair<Boolean, String?>) -> Unit) {
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
