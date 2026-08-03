package com.resalescanner.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.resalescanner.app.ui.AppViewModel
import com.resalescanner.app.ui.ResaleScannerApp
import com.resalescanner.app.ui.theme.ResaleScannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val container = (application as ResaleScannerApplication).container
            val appViewModel: AppViewModel = viewModel(factory = AppViewModel.Factory(container.inventoryRepository, container.inventoryExporter, container.productSearchRepository))
            ResaleScannerTheme { ResaleScannerApp(appViewModel) }
        }
    }
}
