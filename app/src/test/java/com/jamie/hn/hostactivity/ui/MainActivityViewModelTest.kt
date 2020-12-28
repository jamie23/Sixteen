package com.jamie.hn.hostactivity.ui

import androidx.lifecycle.Observer
import com.jamie.hn.core.BaseTest
import com.jamie.hn.core.InstantExecutorExtension
import com.jamie.hn.hostactivity.repository.local.PreferencesRepository
import com.jamie.hn.hostactivity.repository.local.PreferencesRepository.Theme.DARK
import com.jamie.hn.hostactivity.repository.local.PreferencesRepository.Theme.LIGHT
import com.jamie.hn.hostactivity.ui.MainActivityViewModel.ThemeChange
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class)
class MainActivityViewModelTest : BaseTest() {

    @MockK
    private lateinit var resourceProvider: MainActivityResourceProvider

    @RelaxedMockK
    private lateinit var preferencesRepository: PreferencesRepository

    private lateinit var mainActivityViewModel: MainActivityViewModel

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { resourceProvider.lightThemeEnabledIcon } returns 1
        every { resourceProvider.lightThemeDisabledIcon } returns 2
        every { resourceProvider.darkThemeEnabledIcon } returns 3
        every { resourceProvider.darkThemeDisabledIcon } returns 4
        every { resourceProvider.lightTheme } returns 5
        every { resourceProvider.darkTheme } returns 6

        mainActivityViewModel = MainActivityViewModel(
            resourceProvider,
            preferencesRepository
        )
    }

    @Test
    fun `when lightThemeSet is called then set theme on the repository and post theme change with correct parameters`() {
        val observer = spyk<Observer<ThemeChange>>()

        mainActivityViewModel.themeIconsUpdate().observeForever(observer)
        mainActivityViewModel.lightThemeSet()

        verify { preferencesRepository.setTheme(LIGHT) }
        verify { observer.onChanged(ThemeChange(1, 4, 5)) }
    }

    @Test
    fun `when darkThemeSet is called then set theme on the repository and post theme change with correct parameters`() {
        val observer = spyk<Observer<ThemeChange>>()

        mainActivityViewModel.themeIconsUpdate().observeForever(observer)
        mainActivityViewModel.darkThemeSet()

        verify { preferencesRepository.setTheme(DARK) }
        verify { observer.onChanged(ThemeChange(2, 3, 6)) }
    }
}
