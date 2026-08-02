package com.resalescanner.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import android.net.Uri
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.resalescanner.app.ui.screens.HomeScreen
import com.resalescanner.app.ui.screens.ExportInventoryScreen
import com.resalescanner.app.ui.screens.InventoryScreen
import com.resalescanner.app.ui.screens.ItemEditorScreen
import com.resalescanner.app.ui.screens.ProfitCalculatorScreen
import com.resalescanner.app.ui.screens.ResultsScreen
import com.resalescanner.app.ui.screens.ScannerScreen
import com.resalescanner.app.ui.screens.SearchByNameScreen
import com.resalescanner.app.ui.screens.SplashScreen
import com.resalescanner.app.ui.screens.TakePictureScreen

private data class Destination(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
private val destinations = listOf(
    Destination("home", "Home", Icons.Outlined.Home),
    Destination("scan", "Scan", Icons.Outlined.QrCodeScanner),
    Destination("inventory", "Inventory", Icons.Outlined.Inventory2),
)

@Composable
fun ResaleScannerApp(viewModel: AppViewModel) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val current = backStack?.destination
    val showNavigation = current?.route in destinations.map { it.route }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(showNavigation) {
                NavigationBar {
                    destinations.forEach { destination ->
                        NavigationBarItem(
                            selected = current?.hierarchy?.any { it.route == destination.route } == true,
                            onClick = { navController.navigate(destination.route) { popUpTo("home"); launchSingleTop = true } },
                            icon = { Icon(destination.icon, destination.label) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            NavHost(navController, startDestination = "splash") {
                composable("splash") { SplashScreen { navController.navigate("home") { popUpTo("splash") { inclusive = true } } } }
                composable("home") { HomeScreen(viewModel, onScan = { navController.navigate("scan") }, onTakePicture = { navController.navigate("picture") }, onSearch = { navController.navigate("search") }, onInventory = { navController.navigate("inventory") }, onProfitCalculator = { navController.navigate("profit") }, onExport = { navController.navigate("export") }) }
                composable("inventory") { InventoryScreen(viewModel, onAdd = { navController.navigate("edit/new") }, onEdit = { navController.navigate("edit/${it.id}") }) }
                composable("scan") { ScannerScreen(onBarcode = { navController.navigate("results/${Uri.encode(it)}") }) }
                composable("picture") { TakePictureScreen({ navController.popBackStack() }, { navController.navigate("results/${Uri.encode("Photo identified item")}") }) }
                composable("search") { SearchByNameScreen({ navController.popBackStack() }, { navController.navigate("results/${Uri.encode(it)}") }) }
                composable("profit") { ProfitCalculatorScreen { navController.popBackStack() } }
                composable("export") { ExportInventoryScreen(viewModel) { navController.popBackStack() } }
                composable("results/{query}") { entry ->
                    ResultsScreen(
                        viewModel = viewModel,
                        query = Uri.decode(entry.arguments?.getString("query").orEmpty()),
                        onBack = { navController.popBackStack() },
                        onAdded = { navController.navigate("inventory") { popUpTo("home") } },
                    )
                }
                composable("edit/{itemId}?barcode={barcode}") { entry ->
                    val id = entry.arguments?.getString("itemId")?.toLongOrNull()
                    val barcode = entry.arguments?.getString("barcode").orEmpty()
                    ItemEditorScreen(viewModel, id, barcode, onBack = { navController.popBackStack() })
                }
            }
        }
    }
}
