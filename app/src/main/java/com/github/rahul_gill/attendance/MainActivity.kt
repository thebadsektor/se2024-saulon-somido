package com.github.rahul_gill.attendance

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.github.rahul_gill.attendance.db.DatabaseHelper
import com.github.rahul_gill.attendance.prefs.PreferenceManager
import com.github.rahul_gill.attendance.ui.RootNavHost
import com.github.rahul_gill.attendance.ui.comps.AttendanceAppTheme
import com.github.rahul_gill.attendance.ui.comps.ColorSchemeType


class MainActivity : ComponentActivity() {
    private lateinit var databaseHelper: DatabaseHelper
    private val prefName = "AuthenticationPrefs"
    private val isAuthenticatedKey = "isAuthenticated"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        databaseHelper = DatabaseHelper(this)


        val isAuthenticated = checkAuthenticationState()
        if (!isAuthenticated) {
            // Redirect to AuthenticationActivity if not authenticated
            val intent = Intent(this, AuthenticationActivity::class.java)
            startActivity(intent)
            finish() // Close MainActivity
            return
        }


        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT, Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT, Color.TRANSPARENT
            )
        )
        setContent {
            val followSystemColor = PreferenceManager.followSystemColors.asState()
            val seedColor = PreferenceManager.colorSchemeSeed.asState()
            val theme = PreferenceManager.themeConfig.asState()
            val darkThemeType = PreferenceManager.darkThemeType.asState()
            AttendanceAppTheme(
                colorSchemeType = if (followSystemColor.value) ColorSchemeType.Dynamic else ColorSchemeType.WithSeed(
                    seedColor.value
                ),
                themeConfig = theme.value,
                darkThemeType = darkThemeType.value
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RootNavHost()
                }
            }
        }
    }

    private fun checkAuthenticationState(): Boolean {
        val sharedPreferences = getSharedPreferences(prefName, MODE_PRIVATE)
        return sharedPreferences.getBoolean(isAuthenticatedKey, false)
    }


    private fun setAuthenticationState(isAuthenticated: Boolean) {
        val sharedPreferences = getSharedPreferences(prefName, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(isAuthenticatedKey, isAuthenticated)
        editor.apply()
    }


    override fun onStop() {
        super.onStop()
        setAuthenticationState(false)
    }



}
