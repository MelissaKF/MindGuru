package com.example.mindguru

import android.os.Bundle
import android.util.Log
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.mindguru.databinding.ActivityMainBinding
import com.example.mindguru.ui.MainViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

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
            setupBottomNav()
        } else {
            disablePlayFieldNavigation()
        }
    }

    override fun onDestroy() {
        lifecycleScope.launch {
            Log.d("MainActivityLogout", "Succeed")
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                viewModel.logout()
            }
        }
        super.onDestroy()
        appClosed = true
    }

    private fun disablePlayFieldNavigation(): Boolean {
        binding.bottomNavMenu.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.playFieldFragment) {
                val user = FirebaseAuth.getInstance().currentUser
                if (user == null) {
                    Toast.makeText(this, "Bitte loggen Sie sich zuerst ein", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnItemSelectedListener false
                }
            }
            true
        }
        return false
    }

    private fun setupBottomNav() {
        val playFieldMenuItem = binding.bottomNavMenu.menu.findItem(R.id.playFieldFragment)
        playFieldMenuItem.isEnabled = true
    }
}