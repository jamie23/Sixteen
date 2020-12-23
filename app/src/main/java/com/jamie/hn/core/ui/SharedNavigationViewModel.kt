package com.jamie.hn.core.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jamie.hn.core.Event

class SharedNavigationViewModel : ViewModel() {

    var currentScreen: Screen = Top

    private val navigateNextScreen = MutableLiveData<Event<Screen>>()
    fun navigateNextScreen(): LiveData<Event<Screen>> = navigateNextScreen

    private val showDrawer = MutableLiveData<Event<Unit>>()
    fun showDrawer(): LiveData<Event<Unit>> = showDrawer

    fun navigate(nextScreen: Screen) {
        if (currentScreen == nextScreen) {
            return
        } else {
            currentScreen = nextScreen
        }

        navigateNextScreen.value = Event(nextScreen)
    }

    fun navigationIconSelected() {
        showDrawer.value = Event(Unit)
    }
}
