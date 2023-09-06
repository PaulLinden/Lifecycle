package com.three.lifecycle
import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreSingleton {

    private var firestore: FirebaseFirestore? = null

    fun getInstance(appContext: Context): FirebaseFirestore {
        if (firestore == null) {
            firestore = FirebaseFirestore.getInstance()
        }
        return firestore!!
    }
}




