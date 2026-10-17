package com.kawanova.curlforge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.ads.MobileAds
import com.kawanova.curlforge.ui.CurlViewModel
import com.kawanova.curlforge.ui.navigation.AppNavigation
import com.kawanova.curlforge.ui.theme.CurlForgeTheme
import com.kawanova.curlforge.ui.theme.Slate950

class MainActivity : ComponentActivity() {

    private val viewModel: CurlViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Google Mobile Ads SDK
        MobileAds.initialize(this) {}

        setContent {
            CurlForgeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Slate950
                ) {
                    val navController = rememberNavController()
                    Column(modifier = Modifier.fillMaxSize()) {
                        AppNavigation(
                            navController = navController,
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
