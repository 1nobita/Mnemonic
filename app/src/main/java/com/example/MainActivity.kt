package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AppNavigation
import com.example.ui.theme.MnemonicTheme
import com.example.ui.viewmodel.AuraViewModel
import com.example.ui.viewmodel.AuraViewModelFactory
import com.example.ui.viewmodel.FutureTwinViewModel
import com.example.ui.viewmodel.MnemViewModel
import com.example.ui.viewmodel.MnemViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MnemonicTheme {
                val app = applicationContext as MnemonicApplication
                val repository = app.memoryRepository

                val mnemViewModel: MnemViewModel = viewModel(factory = MnemViewModelFactory(repository))
                val auraViewModel: AuraViewModel = viewModel(factory = AuraViewModelFactory(repository))
                val futureTwinViewModel: FutureTwinViewModel = viewModel()

                AppNavigation(
                    auraViewModel = auraViewModel,
                    mnemViewModel = mnemViewModel,
                    futureTwinViewModel = futureTwinViewModel
                )
            }
        }
    }
}
