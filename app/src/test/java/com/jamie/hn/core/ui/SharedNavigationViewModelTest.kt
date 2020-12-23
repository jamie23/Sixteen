package com.jamie.hn.core.ui

import androidx.lifecycle.Observer
import com.jamie.hn.core.BaseTest
import com.jamie.hn.core.Event
import com.jamie.hn.core.InstantExecutorExtension
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class)
class SharedNavigationViewModelTest : BaseTest() {

    private lateinit var sharedNavigationViewModel: SharedNavigationViewModel

    @BeforeEach
    fun setup() {
        sharedNavigationViewModel = SharedNavigationViewModel()
    }

    @Nested
    inner class Navigate {

        @Test
        fun `when navigate is called but next screen is equal to current screen then do not post new screen event`() {
            val observer = spyk<Observer<Event<Screen>>>()

            sharedNavigationViewModel.currentScreen = Top
            sharedNavigationViewModel.navigateNextScreen().observeForever(observer)
            sharedNavigationViewModel.navigate(Top)

            verify(exactly = 0) { observer.onChanged(any()) }
        }

        @Test
        fun `when navigate is called and next screen does not equal current screen then update the current screen and post new screen event`() {
            val observer = spyk<Observer<Event<Screen>>>()
            val nextScreen = slot<Event<Screen>>()

            sharedNavigationViewModel.currentScreen = Top
            sharedNavigationViewModel.navigateNextScreen().observeForever(observer)
            sharedNavigationViewModel.navigate(Ask)

            verify(exactly = 1) { observer.onChanged(capture(nextScreen)) }
            assertEquals(Ask, sharedNavigationViewModel.currentScreen)
            assertEquals(Ask, nextScreen.captured.getContentIfNotHandled())
        }
    }

    @Nested
    inner class NavigationIconSelected {

        @Test
        fun `when navigationIconSelected is called then post value to showDrawer`() {
            val observer = spyk<Observer<Event<Unit>>>()
            sharedNavigationViewModel.showDrawer().observeForever(observer)

            sharedNavigationViewModel.navigationIconSelected()
            verify { observer.onChanged(any()) }
        }
    }
}
