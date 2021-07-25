package com.jamie.hn.hostactivity.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.core.view.GravityCompat.START
import androidx.lifecycle.Observer
import com.jamie.hn.R
import com.jamie.hn.core.StoriesListType.UNKNOWN
import com.jamie.hn.core.ui.Ask
import com.jamie.hn.core.ui.Comments
import com.jamie.hn.core.ui.Jobs
import com.jamie.hn.core.ui.New
import com.jamie.hn.core.ui.SharedNavigationViewModel
import com.jamie.hn.core.ui.Show
import com.jamie.hn.core.ui.Top
import com.jamie.hn.databinding.ActivityMainBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val sharedNavigationViewModel by viewModel<SharedNavigationViewModel>()
    private val viewModel: MainActivityViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigationDrawer()
        initialiseTheme()
        initialiseLiveDataObservers()

        if (intent.action == Intent.ACTION_VIEW) {
            val storyId = intent.data?.getQueryParameter("id")?.toInt() ?: return
            sharedNavigationViewModel.navigate(Comments(storyId, UNKNOWN))
        }
    }

    private fun initialiseTheme() {
        val preferences = this.getSharedPreferences("Theme", MODE_PRIVATE)
        val isLight = preferences.getBoolean(getString(R.string.saved_theme), true)
        val nightMode: Int
        val theme: Int

        if (isLight) {
            theme = R.style.LightAppTheme
            nightMode = MODE_NIGHT_NO
            binding.lightThemeButton.setImageResource(R.drawable.ic_sun_filled_24)
            binding.darkThemeButton.setImageResource(R.drawable.ic_moon_outline_24)
        } else {
            theme = R.style.DarkAppTheme
            nightMode = MODE_NIGHT_YES
            binding.lightThemeButton.setImageResource(R.drawable.ic_sun_outline_24)
            binding.darkThemeButton.setImageResource(R.drawable.ic_moon_filled_24)
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
        setTheme(theme)
    }

    private fun setupNavigationDrawer() {
        val navigationDrawer = binding.navView
        navigationDrawer.setNavigationItemSelectedListener {
            sharedNavigationViewModel.navigate(getScreenFromMenuItem(it))
            binding.drawerLayout.closeDrawer(START)
            true
        }

        binding.lightThemeButton.setOnClickListener {
            viewModel.lightThemeSet()
        }

        binding.darkThemeButton.setOnClickListener {
            viewModel.darkThemeSet()
        }
    }

    private fun initialiseLiveDataObservers() {
        sharedNavigationViewModel.showDrawer().observe(this, Observer {
            binding.drawerLayout.openDrawer(START)
        })

        viewModel.themeIconsUpdate().observe(this, Observer {
            binding.drawerLayout.closeDrawer(START)
            binding.lightThemeButton.setImageResource(it.lightThemeIcon)
            binding.darkThemeButton.setImageResource(it.darkThemeIcon)
            AppCompatDelegate.setDefaultNightMode(it.newTheme)
        })
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
