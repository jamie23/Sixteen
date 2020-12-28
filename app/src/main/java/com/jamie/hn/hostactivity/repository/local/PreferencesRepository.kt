package com.jamie.hn.hostactivity.repository.local

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.jamie.hn.R
import com.jamie.hn.hostactivity.repository.local.PreferencesRepository.Theme.LIGHT

class PreferencesRepository(
    private val context: Context
) {
    fun setTheme(theme: Theme) {
        val preferences = context.getSharedPreferences("Theme", MODE_PRIVATE)
        with(preferences.edit()) {
            putBoolean(context.getString(R.string.saved_theme), theme == LIGHT)
            apply()
        }
    }

    enum class Theme {
        LIGHT, DARK
    }
}
