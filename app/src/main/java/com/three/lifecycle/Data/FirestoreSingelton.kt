package com.three.lifecycle.Data
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreSingleton {

    private var firestore: FirebaseFirestore? = null

    fun getInstance(): FirebaseFirestore {
        if (firestore == null) {
            firestore = FirebaseFirestore.getInstance()
        }
        return firestore!!
    }
}




