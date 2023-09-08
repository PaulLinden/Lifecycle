package com.three.lifecycle

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore

class SettingsFragment : Fragment() {

    private lateinit var rootView: View
    private var firestore: FirebaseFirestore? = null

    private var saveEmailInput:String = ""
    private var savePasswordInput:String = ""
    private var saveAgeInput:String = ""
    private var savedGenderId: Int? = null
    private var saveCheckBoxState:Boolean = false

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

        if (savedInstanceState != null) {
            savedInstanceState.getString("email_value")
            savedInstanceState.getString("password_value")
            savedInstanceState.getBoolean("checkBox_value")
            savedInstanceState.getString("password_value")
            savedInstanceState.getBoolean("gender_value")
            savedInstanceState.getString("age_value")
        }

        val userId = arguments?.getString("userId").toString()

        val updateEmail = rootView.findViewById<EditText>(R.id.inputNewEmailAdress)
        val updatePassword = rootView.findViewById<EditText>(R.id.inputNewPassword)
        val updateDrivingLicense = rootView.findViewById<CheckBox>(R.id.checkBoxDrivingLicense)
        val updateGroupGender = rootView.findViewById<RadioGroup>(R.id.gender)
        val updateAge = rootView.findViewById<EditText>(R.id.inputNewAge)

        val uiList = mutableListOf(updateEmail, updatePassword, updateAge)
        uiList.forEach { ui ->
            ui.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    when (ui) {
                        updateEmail -> saveEmailInput = p0.toString()
                        updatePassword -> savePasswordInput = p0.toString()
                        updateAge -> saveAgeInput = p0.toString()
                        else -> {}
                    }
                }

                override fun afterTextChanged(p0: Editable?) {}
            })
        }

        updateDrivingLicense.setOnCheckedChangeListener { _, isChecked ->
            saveCheckBoxState = isChecked
        }

        updateGroupGender.setOnCheckedChangeListener { group, checkedId ->
            savedGenderId = checkedId
        }

        val submitButton = rootView.findViewById<Button>(R.id.submitButton)
        submitButton.setOnClickListener {

            val updateUser = User(

                email = updateEmail?.text.toString(),
                password = updatePassword.text.toString(),
                drivingLicense = updateDrivingLicense.isChecked,
                gender = when (updateGroupGender.checkedRadioButtonId) {
                    R.id.radioMale -> "Male"
                    R.id.radioFemale -> "Female"
                    else -> "Other"
                },
                age = updateAge.text.toString().toIntOrNull() ?: 0
            )

            updateUserInDatabase(updateUser, userId)
        }
    }

    private fun updateUserInDatabase(user: User, userId: String) {

        firestore?.collection("users")?.whereEqualTo(FieldPath.documentId(), userId)?.get()
            ?.addOnSuccessListener { result ->
                for (document in result) {
                    val docRef = firestore?.collection("users")?.document(document.id)

                    val updates = mapOf(
                        "email" to user.email,
                        "password" to user.password,
                        "drivingLicense" to user.drivingLicense,
                        "gender" to user.gender,
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString("email_value", saveEmailInput)
        outState.putString("password_value", savePasswordInput)
        outState.putString("age_value", saveAgeInput)
        outState.putBoolean("auto_login_value", saveCheckBoxState)
        outState.putInt("gender_value", savedGenderId ?: -1)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        if (savedInstanceState != null) {
            saveEmailInput = savedInstanceState.getString("email_value").toString()
            savePasswordInput = savedInstanceState.getString("password_value").toString()
            saveAgeInput = savedInstanceState.getString("age_value").toString()
            saveCheckBoxState = savedInstanceState.getBoolean("auto_login_value")
            savedGenderId = savedInstanceState.getInt("gender_value", -1)
        }
    }
}
