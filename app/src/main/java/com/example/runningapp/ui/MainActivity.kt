package com.example.runningapp.ui

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.runningapp.R
import com.example.runningapp.appComponent
import com.example.runningapp.databinding.ActivityMainBinding
import com.example.runningapp.utilities.Constants

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navHostFragment: NavHostFragment
    lateinit var toolbar: androidx.appcompat.widget.Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appComponent.inject(this)

        toolbar = binding.toolbar

        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragmentContainer) as NavHostFragment

        checkIntentAction(intent)

        binding.bottomNavigationView.setupWithNavController(navHostFragment.navController)
        navHostFragment.navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigationView.visibility =
                if (destination.id == R.id.trackingFragment ||
                    destination.id == R.id.setupFragment
                ) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
        }
    }

    override fun onStart() {
        super.onStart()
        checkIntentAction(intent)
    }

    private fun checkIntentAction(intent: Intent?) {
        intent?.let {
            if (it.action == Constants.ACTION_TRACKING_SERVICE_NOTIFICATION_PRESSED) {
                navHostFragment.navController.navigate(R.id.trackingFragment)
                it.action = null
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        checkIntentAction(intent)
    }
}