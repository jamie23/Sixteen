package com.jamie.hn

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.GravityCompat.START
import com.jamie.hn.core.ui.SharedNavigationViewModel
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
    }

    private fun setupNavigationDrawer() {
        val navigationDrawer = binding.navView
        navigationDrawer.setNavigationItemSelectedListener {
            sharedNavigationViewModel.navigate(it)
            binding.drawerLayout.closeDrawer(START)
            true
        }
    }
}
