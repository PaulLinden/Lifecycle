package com.three.lifecycle

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.log


class ProfileFragment : Fragment() {

    private lateinit var rootView: View
    private var firestore: FirebaseFirestore? = null
    private lateinit var userId: String

    fun setFirestoreReference(db: FirebaseFirestore) {
        firestore = db
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_profile, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = arguments?.getString("userId").toString()
        getUserSpec(userId)
    }

    private fun getUserSpec(userId: String) {
        val ageTextView = rootView.findViewById<TextView>(R.id.profilAgeTextView)
        val nameTextView = rootView.findViewById<TextView>(R.id.profileNameTextView)

        firestore?.collection("users")
            ?.whereEqualTo(FieldPath.documentId(), userId)
            ?.whereEqualTo("isLoggedIn", true)
            ?.get()
            ?.addOnSuccessListener { result ->
                Log.d("Profilemanager", "Error ")
                for (document in result) {
                    val ageNumber = document.getLong("age")
                    val nameString = document.getString("email")

                    val ageValue = ageNumber?.toString() ?: "N/A"
                    val nameValue = nameString ?: "N/A"

                    Log.d("Profilemanager", "Error ")
                    activity?.runOnUiThread {
                        nameTextView.text = "Name: $nameValue"
                        ageTextView.text = "Age: $ageValue"
                    }
                }
            }
            ?.addOnFailureListener { exception ->
                Log.d("Profilemanager", "Error getting documents.", exception)
            }
    }
}