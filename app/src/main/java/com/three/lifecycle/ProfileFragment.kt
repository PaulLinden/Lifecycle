package com.three.lifecycle

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore


class ProfileFragment : Fragment() {

    private lateinit var rootView: View
    private var firestore: FirebaseFirestore? = null

    private var ageValue: String = ""
    private var titleValue: String = ""

    fun setFirestoreReference(db: FirebaseFirestore) {
        firestore = db
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_profile, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val email = arguments?.getString("email")
        if (email != null) {
            getUserSpec(email)
        }
    }

    private fun getUserSpec(email: String) {

        val ageTextView = rootView.findViewById<TextView>(R.id.profilAgeTextView)
        val titleTextView = rootView.findViewById<TextView>(R.id.profilTitleTextView)

        firestore?.collection("users")
            ?.whereEqualTo("email", email)
            ?.whereEqualTo("isLoggedIn", true)
            ?.get()
            ?.addOnSuccessListener { result ->
                for (document in result) {

                    val ageNumber = document.getLong("age")
                    val titleString = document.getString("title")

                    if (ageNumber != null && titleString != null) {
                        ageValue = ageNumber.toString()
                        titleValue = titleString
                    }
                }
                val agePlaceholder = getString(R.string.age_placeholder)
                val titlePlaceholder = getString(R.string.title_placeholder)
                val formattedAge = String.format(agePlaceholder, ageValue)

                ageTextView.text = formattedAge
                titleTextView.text = String.format(titlePlaceholder, titleValue)
            }
            ?.addOnFailureListener { exception ->
                Log.w("read", "Error getting documents.", exception)
            }
    }
}