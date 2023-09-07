package com.three.lifecycle

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class SettingsFragment : Fragment() {

    private lateinit var rootView: View
    private var firestore: FirebaseFirestore? = null

    private var newAge: String = ""

    fun setFirestoreReference(db: FirebaseFirestore) {
        firestore = db
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_settings, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        rootView.findViewById<EditText>(R.id.inputNewEmailAdress).setText(sharedPreferences.getString("email", ""))
        rootView.findViewById<EditText>(R.id.inputNewPassword).setText(sharedPreferences.getString("password", ""))
        rootView.findViewById<EditText>(R.id.inputNewTitle).setText(sharedPreferences.getString("title", ""))
        rootView.findViewById<EditText>(R.id.inputNewAdress).setText(sharedPreferences.getString("address", ""))
        rootView.findViewById<EditText>(R.id.inputNewAge).setText(sharedPreferences.getString("age", ""))

        val submitButton = rootView.findViewById<Button>(R.id.submitButton)
        submitButton.setOnClickListener {

            val inputNewEmailAdress = rootView.findViewById<EditText>(R.id.inputNewEmailAdress)
            val inputNewPassword = rootView.findViewById<EditText>(R.id.inputNewPassword)
            val inputNewTitle = rootView.findViewById<EditText>(R.id.inputNewTitle)
            val inputNewAdress = rootView.findViewById<EditText>(R.id.inputNewAdress)
            val inputNewAge = rootView.findViewById<EditText>(R.id.inputNewAge)

            val updateUser = User(
                email = inputNewEmailAdress.text.toString(),
                password = inputNewPassword.text.toString(),
                title = inputNewTitle.text.toString(),
                address = inputNewAdress.text.toString(),
                age = inputNewAge.text.toString().toIntOrNull() ?: 0
            )

            val currentUser = arguments?.getString("email")
            if (currentUser != null) {
                editCredentials(updateUser, currentUser)
            }

            val editor = sharedPreferences.edit()

            editor.putString("email", inputNewEmailAdress.text.toString())
            editor.putString("password", inputNewPassword.text.toString())
            editor.putString("title", inputNewTitle.text.toString())
            editor.putString("address", inputNewAdress.text.toString())
            editor.putString("age", inputNewAge.text.toString())

            editor.apply()
        }
    }

    fun editCredentials(user: User, currentUser: String) {
        firestore?.collection("users")
            ?.whereEqualTo("email", currentUser)?.get()?.addOnSuccessListener { result ->
                for (document in result) {
                    val docRef = firestore?.collection("users")?.document(document.id)

                    val updates = mapOf(
                        "email" to user.email,
                        "password" to user.password,
                        "title" to user.title,
                        "address" to user.address,
                        "age" to user.age
                    )

                    docRef?.update(updates)
                        ?.addOnSuccessListener {
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


