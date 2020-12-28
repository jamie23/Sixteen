package com.jamie.hn.hostactivity.ui

import android.app.UiModeManager.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import com.jamie.hn.R

class MainActivityResourceProvider {
    val lightThemeDisabledIcon = R.drawable.ic_sun_outline_24
    val lightThemeEnabledIcon = R.drawable.ic_sun_filled_24
    val darkThemeDisabledIcon = R.drawable.ic_moon_outline_24
    val darkThemeEnabledIcon = R.drawable.ic_moon_filled_24
    val lightTheme = MODE_NIGHT_NO
    val darkTheme = MODE_NIGHT_YES
}
