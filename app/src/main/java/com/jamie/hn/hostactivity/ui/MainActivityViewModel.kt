package com.jamie.hn.hostactivity.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jamie.hn.hostactivity.repository.local.PreferencesRepository
import com.jamie.hn.hostactivity.repository.local.PreferencesRepository.Theme.DARK
import com.jamie.hn.hostactivity.repository.local.PreferencesRepository.Theme.LIGHT

class MainActivityViewModel(
    private val resourceProvider: MainActivityResourceProvider,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val themeIconsUpdate = MutableLiveData<ThemeChange>()
    fun themeIconsUpdate(): LiveData<ThemeChange> = themeIconsUpdate

    fun lightThemeSet() {
        preferencesRepository.setTheme(LIGHT)
        themeIconsUpdate.value = ThemeChange(
            resourceProvider.lightThemeEnabledIcon,
            resourceProvider.darkThemeDisabledIcon,
            resourceProvider.lightTheme
        )
    }

    fun darkThemeSet() {
        preferencesRepository.setTheme(DARK)
        themeIconsUpdate.value = ThemeChange(
            resourceProvider.lightThemeDisabledIcon,
            resourceProvider.darkThemeEnabledIcon,
            resourceProvider.darkTheme
        )
    }

    data class ThemeChange(
        val lightThemeIcon: Int,
        val darkThemeIcon: Int,
        val newTheme: Int
    )
}
