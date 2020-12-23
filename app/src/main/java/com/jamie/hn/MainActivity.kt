package com.jamie.hn

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.core.view.GravityCompat.START
import androidx.lifecycle.Observer
import com.jamie.hn.core.ui.Ask
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigationDrawer()
        initialiseLiveDataObservers()
    }

    private fun setupNavigationDrawer() {
        val navigationDrawer = binding.navView
        navigationDrawer.setNavigationItemSelectedListener {
            sharedNavigationViewModel.navigate(getScreenFromMenuItem(it))
            binding.drawerLayout.closeDrawer(START)
            true
        }
    }

    private fun initialiseLiveDataObservers() {
        sharedNavigationViewModel.showDrawer().observe(this, Observer {
            binding.drawerLayout.openDrawer(START)
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
