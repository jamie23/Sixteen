package com.jamie.hn.core.ui

import android.view.MenuItem
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jamie.hn.R
import com.jamie.hn.core.Event

class SharedNavigationViewModel : ViewModel() {

    var currentScreen: Screen = Top
    private val navigateNextScreen = MutableLiveData<Event<Screen>>()
    fun navigateNextScreen(): LiveData<Event<Screen>> = navigateNextScreen

    // Used for drawer items
    fun navigate(menuItem: MenuItem) {
        val nextScreen = getScreenFromMenuItem(menuItem)

        if (currentScreen == nextScreen) return

        navigateNextScreen.value = Event(nextScreen)
    }

    // Used for non drawer items e.g. article/comments
    fun navigate(nextScreen: Screen) {
        if (currentScreen == nextScreen) {
            return
        } else {
            currentScreen = nextScreen
        }

        navigateNextScreen.value = Event(nextScreen)
    }

    private fun getScreenFromMenuItem(menuItem: MenuItem) =
        when (menuItem.itemId) {
            R.id.drawerTopStories -> Top
            R.id.drawerAskHN -> Ask
            R.id.drawerJobStories -> Jobs
            R.id.drawerNewStories -> New
            R.id.drawerShowHN -> Show
            else -> throw IllegalArgumentException("Unsupported screen chosen")
        }
}
