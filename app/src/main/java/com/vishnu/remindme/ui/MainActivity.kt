package com.vishnu.remindme.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vishnu.remindme.Constants
import com.vishnu.remindme.R
import com.vishnu.remindme.ui.theme.RemindMeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkNotificationPermission()
        }

        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = Uri.parse("package:${packageName}")
            }
            startActivity(intent)
        }

        setContent {
            RemindMeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = Constants.SCREEN_HOME,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(route = Constants.SCREEN_HOME) {
                            HomeScreen(
                                modifier = Modifier,
                                navController = navController
                            )
                        }

                        composable(
                            route = Constants.SCREEN_ABOUT,
                            enterTransition = {
                                slideIntoContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Down,
                                    animationSpec = tween(durationMillis = 700)
                                ) + fadeIn(animationSpec = tween(durationMillis = 700))
                            },
                            exitTransition = {
                                slideOutOfContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Down,
                                    animationSpec = tween(durationMillis = 700)
                                ) + fadeOut(animationSpec = tween(durationMillis = 700))
                            },
                            popEnterTransition = {
                                slideIntoContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Down,
                                    animationSpec = tween(durationMillis = 700)
                                ) + fadeIn(animationSpec = tween(durationMillis = 700))
                            },
                            popExitTransition = {
                                slideOutOfContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Down,
                                    animationSpec = tween(durationMillis = 700)
                                ) + fadeOut(animationSpec = tween(durationMillis = 700))
                            }
                        ) {
                            AboutScreen(navController)
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkNotificationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(
                this,
                getString(R.string.notification_permission_mandatory),
                Toast.LENGTH_LONG
            ).show()
            finish()
        }
    }
}