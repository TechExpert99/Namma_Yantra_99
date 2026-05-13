package com.nayak.nammayantara.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nayak.nammayantara.ui.theme.*

@Composable
fun CustomerApp(onLogout: () -> Unit) {
    val navController = rememberNavController()

    val items = listOf(
        Triple("home",     "🏠", "Home"),
        Triple("bookings", "📋", "Bookings"),
        Triple("reviews",  "⭐", "Reviews"),
        Triple("profile",  "👤", "Profile"),
    )

    Scaffold(
        containerColor = YantraAsphalt,
        bottomBar = {
            NavigationBar(containerColor = YantraSurface, tonalElevation = 0.dp) {
                val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
                items.forEach { (route, icon, label) ->
                    NavigationBarItem(
                        selected = currentRoute == route,
                        onClick  = {
                            navController.navigate(route) {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState    = true
                            }
                        },
                        icon = {
                            Text(icon, fontSize = 20.sp,
                                style = LocalTextStyle.current.copy(
                                    color = if (currentRoute == route) YantraAmber else YantraGrey60))
                        },
                        label = {
                            Text(label, style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (currentRoute == route) FontWeight.SemiBold else FontWeight.Normal)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedTextColor   = YantraAmber,
                            unselectedTextColor = YantraGrey60,
                            indicatorColor      = YantraAmber.copy(alpha = 0.12f),
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = "home",
            modifier = Modifier.padding(innerPadding)) {
            composable("home")     { CustomerHomeScreen() }
            composable("bookings") { CustomerBookingsScreen() }
            composable("reviews")  { CustomerReviewsScreen() }
            composable("profile")  { ProfileScreen(onLogout = onLogout) }
        }
    }
}