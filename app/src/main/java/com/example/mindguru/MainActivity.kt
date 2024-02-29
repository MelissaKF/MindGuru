package com.example.mindguru

import android.content.Context
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.mindguru.databinding.ActivityMainBinding
import com.example.mindguru.ui.MainViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private var appClosed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)

        val controller = window.insetsController

        if (controller != null) {
            controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            controller.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavMenu.setupWithNavController(navController)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            // Der Benutzer ist eingeloggt
            setupBottomNav()
        } else {
            disablePlayFieldNavigation()
        }
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations && !appClosed) {
            viewModel.logout()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        appClosed = true
    }

    private fun disablePlayFieldNavigation(): Boolean {
        val playFieldMenuItem = binding.bottomNavMenu.menu.findItem(R.id.playFieldFragment)
        playFieldMenuItem.isEnabled = false
        Toast.makeText(this@MainActivity, "Bitte loggen Sie sich zuerst ein", Toast.LENGTH_SHORT).show()
        return false
    }

    private fun setupBottomNav() {
        val playFieldMenuItem = binding.bottomNavMenu.menu.findItem(R.id.playFieldFragment)
        playFieldMenuItem.isEnabled = true
    }
}