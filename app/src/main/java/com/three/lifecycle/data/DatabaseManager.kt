package com.three.lifecycle.data

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import com.google.firebase.firestore.FieldPath

class DatabaseManager(private  val context: Context) {

    private val firestoreSingleton = FirestoreSingleton()
    private val db = firestoreSingleton.getInstance()
    private val sharedPreferences = context.getSharedPreferences("UserPref", MODE_PRIVATE)
    private val userId = sharedPreferences.getString("userId", "").toString()

    fun addUserToDatabase(user: User) {
        try {
            db.collection("users")
                .add(user)
                .addOnSuccessListener { documentReference ->
                    Log.d(
                        "validateLogin",
                        "DocumentSnapshot added with ID: ${documentReference.id}"
                    )
                }
                .addOnFailureListener { e ->
                    Log.d("validateLogin", "Error adding document", e)
                }
        }catch (e: Exception){
            Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG).show()
        }
    }
    fun updateUserInDatabase(user: User) {
        try {
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
                                val sharedPreferences =
                                    context.getSharedPreferences("UserSpecPref", MODE_PRIVATE)
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
        }catch (e: Exception){
            Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG).show()
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
        try {
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
        }catch (e: Exception){
        Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG).show()
    }
    }
    fun validateLogin(email: String, password: String, callback: (Pair<Boolean, String?>) -> Unit) {
        try {
            db.collection("users").whereEqualTo("email", email).get()
                .addOnSuccessListener { result ->

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
        }catch (e: Exception){
            Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG).show()
        }
    }
}
