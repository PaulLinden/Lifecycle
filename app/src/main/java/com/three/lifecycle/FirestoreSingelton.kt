package com.three.lifecycle
import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreSingleton {

    private var instance: FirebaseFirestore? = null

    fun getInstance(context: Context): FirebaseFirestore {

        if (instance == null) {
            synchronized(FirestoreSingleton::class.java) {
                if (instance == null) {
                    instance = FirebaseFirestore.getInstance()
                }
            }
        }
        return instance!!
    }
}




