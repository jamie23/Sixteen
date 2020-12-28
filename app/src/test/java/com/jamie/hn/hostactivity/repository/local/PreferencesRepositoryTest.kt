package com.jamie.hn.hostactivity.repository.local

import android.content.Context
import android.content.SharedPreferences
import com.jamie.hn.core.BaseTest
import com.jamie.hn.hostactivity.repository.local.PreferencesRepository.Theme.DARK
import com.jamie.hn.hostactivity.repository.local.PreferencesRepository.Theme.LIGHT
import io.mockk.just
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PreferencesRepositoryTest : BaseTest() {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var sharedPreferences: SharedPreferences

    @MockK
    private lateinit var editor: SharedPreferences.Editor

    private lateinit var preferencesRepository: PreferencesRepository

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { context.getString(any()) } returns "theme"
        every { context.getSharedPreferences(any(), any()) } returns sharedPreferences
        every { sharedPreferences.edit() } returns editor
        every { editor.putBoolean(any(), any()) } returns editor
        every { editor.apply() } just Runs

        preferencesRepository = PreferencesRepository(context)
    }

    @Test
    fun `when setTheme is called with Light then set theme as true in sharedPreferences`() {
        preferencesRepository.setTheme(LIGHT)

        verify { editor.putBoolean("theme", true) }
    }

    @Test
    fun `when setTheme is called with Dark then set theme as false in sharedPreferences`() {
        preferencesRepository.setTheme(DARK)

        verify { editor.putBoolean("theme", false) }
    }
}
