package com.three.lifecycle

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

    private var newEmail: String = ""
    private var newPassword: String = ""
    private var newTitle: String = ""
    private var newAdress: String = ""
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

        val submitButton = rootView.findViewById<Button>(R.id.submitButton)
        submitButton.setOnClickListener {

            val inputNewEmailAdress = rootView.findViewById<EditText>(R.id.inputNewEmailAdress)
            val inputNewPassword = rootView.findViewById<EditText>(R.id.inputNewPassword)
            val inputNewTitle = rootView.findViewById<EditText>(R.id.inputNewTitle)
            val inputNewAdress = rootView.findViewById<EditText>(R.id.inputNewAdress)
            val inputNewAge = rootView.findViewById<EditText>(R.id.inputNewAge)

            newEmail = inputNewEmailAdress.text.toString()
            newPassword = inputNewPassword.text.toString()
            newTitle = inputNewTitle.text.toString()
            newAdress = inputNewAdress.text.toString()
            newAge = inputNewAge.text.toString()

            editCredentials()
        }
    }

    fun editCredentials(){
        val email: String = "Ada@mail.com"

        firestore?.collection("users")
            ?.whereEqualTo("email", email)?.get()?.addOnSuccessListener { result ->

                for (document in result) {
                    val docRef = firestore?.collection("users")?.document(document.id)

                    val updates = hashMapOf(
                        "adress" to newAdress,
                        "age" to newAge.toInt(),
                        "email" to "Ada@mail.com",
                        "password" to "1234",
                        "title" to newTitle
                    )

                    docRef?.update(updates as Map<String, Any>)
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

