package com.three.lifecycle.activitys

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

class NavigationManager(
    private var intent: Intent,
    private val appCompatActivity: AppCompatActivity
) {
    fun navigateToSettings() {
        intent = Intent(appCompatActivity, SettingsActivity::class.java)
        appCompatActivity.startActivity(intent)
    }

    fun navigateToProfile() {
        intent = Intent(appCompatActivity, HomeActivity::class.java)
        appCompatActivity.startActivity(intent)
    }

    fun navigateToMain() {
        intent = Intent(appCompatActivity, MainActivity::class.java)
        appCompatActivity.startActivity(intent)
    }

    fun navigateToRegister() {
        intent = Intent(appCompatActivity, RegisterActivity::class.java)
        appCompatActivity.startActivity(intent)
    }
}