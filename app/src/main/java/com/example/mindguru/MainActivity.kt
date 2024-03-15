package com.example.mindguru

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.ImageButton
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.example.mindguru.databinding.ActivityMainBinding
import com.example.mindguru.ui.MainViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var backButton: ImageButton
    private lateinit var settingsButton: ImageButton
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

        backButton = findViewById(R.id.backButton)
        settingsButton = findViewById(R.id.settingsButton)

        backButton.setOnClickListener {
            navController.navigateUp()
        }

        settingsButton.setOnClickListener {
            val action = NavGraphDirections.actionGlobalProfileFragment()
            navController.navigate(action)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            backButton.visibility = if (destination.id == R.id.welcomeFragment) {
                View.GONE
            } else {
                View.VISIBLE
            }

            settingsButton.visibility = if (destination.id == R.id.welcomeFragment || destination.id == R.id.loginFragment) {
                View.GONE
            } else {
                View.VISIBLE
            }
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
}