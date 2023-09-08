package com.three.lifecycle

import android.widget.CheckBox
import android.widget.RadioGroup

data class User(
        val email: String,
        val password: String,
        val drivingLicense: Boolean,
        val gender: String,
        val age: Int
)