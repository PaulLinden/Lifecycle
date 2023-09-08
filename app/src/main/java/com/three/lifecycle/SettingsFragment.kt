package com.three.lifecycle

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class SettingsFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var rootView: View
    private var firestore: FirebaseFirestore? = null

    fun setFirestoreReference(db: FirebaseFirestore) {
        firestore = db
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_settings, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        val inputNewEmailAdress = rootView.findViewById<EditText>(R.id.inputNewEmailAdress)
        val inputNewPassword = rootView.findViewById<EditText>(R.id.inputNewPassword)
        val inputNewTitle = rootView.findViewById<EditText>(R.id.inputNewTitle)
        val inputNewAdress = rootView.findViewById<EditText>(R.id.inputNewAdress)
        val inputNewAge = rootView.findViewById<EditText>(R.id.inputNewAge)
        val submitButton = rootView.findViewById<Button>(R.id.submitButton)

        submitButton.setOnClickListener {

            val updateUser = User(
                email = inputNewEmailAdress.text.toString(),
                password = inputNewPassword.text.toString(),
                title = inputNewTitle.text.toString(),
                address = inputNewAdress.text.toString(),
                age = inputNewAge.text.toString().toIntOrNull() ?: 0
            )
            val currentUser = arguments?.getString("email")
            if (currentUser != null) {
                updateUserInDatabase(updateUser, currentUser)
            }
        }
    }
    private fun updateUserInDatabase(user: User, currentUser: String) {
        firestore?.collection("users")?.whereEqualTo("email", currentUser)?.get()
            ?.addOnSuccessListener { result ->
                for (document in result) {
                    val docRef = firestore?.collection("users")?.document(document.id)

                    val updates = mapOf(
                        "email" to user.email,
                        "password" to user.password,
                        "title" to user.title,
                        "address" to user.address,
                        "age" to user.age
                    )

                    docRef?.update(updates)?.addOnSuccessListener {
                            Log.d("validateLogin", "Succeeded updating document.")
                        }?.addOnFailureListener { exception ->
                            Log.d("validateLogin", "Error updating document.", exception)
                        }
                    break
                }
            }?.addOnFailureListener { exception ->
                Log.d("validateLogin", "Error querying database.", exception)
            }
    }
}
