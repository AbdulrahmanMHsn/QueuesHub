package com.queueshub.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.queueshub.ui.navigation.Screen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: AppViewModel by viewModels()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userLogged.collect { logged->
                    if (logged.first){
                        viewModel.orderStarted.collect{ started->

                            startActivity(Intent(this@SplashActivity, MainActivity::class.java)
                                .putExtra("dest",Screen.Orders.route ))
                            finish()
                        }
                    }else{

                        startActivity(Intent(this@SplashActivity, MainActivity::class.java)
                            .putExtra("dest", Screen.Login.route))
                        finish()
                    }
                }
            }
        }

    }
}