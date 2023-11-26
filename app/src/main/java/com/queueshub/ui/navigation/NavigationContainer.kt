
package com.queueshub.ui.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.queueshub.ui.auth.LoginScreen
import com.queueshub.ui.car.CarInfoScreen
import com.queueshub.ui.car.ConfirmInfoScreen
import com.queueshub.ui.car.ManualLicenseScreen
import com.queueshub.ui.car.ManualPlateScreen
import com.queueshub.ui.device.*
import com.queueshub.ui.orders.AllOrdersScreen
import com.queueshub.ui.main.DoneScreen
import com.queueshub.ui.main.OrderTypeScreen
import com.queueshub.ui.orders.OrderCarsScreen
import com.queueshub.ui.orders.OrderInfoScreen
import com.queueshub.ui.orders.OrderNoteScreen
import com.queueshub.ui.sim.SimDetailsScreen
import com.queueshub.ui.sim.SimEntryScreen

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun NavigationContainer(
    router: Router,
    scaffoldState: ScaffoldState,
    navController: NavHostController,
    paddingValues: PaddingValues,
    startDestination: String = Screen.Login.route
) {
    val startDestination = remember { mutableStateOf(startDestination) }
    LaunchedEffect(startDestination) {
        if (startDestination.value == Screen.Orders.route) {
            router.goHome()
        }
    }
    NavHost(
        navController = navController,
        startDestination = startDestination.value,
    ) {
        composable(Screen.Login.route) {
            LoginScreen(paddingValues, router)
        }
        composable(Screen.CarInfo.route) {
            CarInfoScreen(scaffoldState,router)
        }
        composable(Screen.OrderInfo.route) {
            OrderInfoScreen(paddingValues, router)
        }
        composable(Screen.OrderNote.route) {
            OrderNoteScreen(paddingValues, router)
        }
        composable(Screen.Orders.route) {
            AllOrdersScreen(paddingValues, router)
        }
        composable(Screen.OrderCars.route) {
            OrderCarsScreen(paddingValues, router)
        }
        composable(Screen.ManualPlate.route) {
            ManualPlateScreen(paddingValues,scaffoldState, router)
        }
        composable(Screen.ManualLicense.route) {
            ManualLicenseScreen(paddingValues, router)
        }
        composable(Screen.InfoConfirmation.route) {
            ConfirmInfoScreen(paddingValues, router)
        }

        composable(Screen.DeviceEntry.route) {
            DeviceEntryScreen(paddingValues, router,scaffoldState)
        }
        composable(Screen.DeviceDetails.route) {
            DeviceDetailsScreen(scaffoldState, router)
        }
        composable(Screen.SimEntry.route) {
            SimEntryScreen(paddingValues, router,scaffoldState)
        }
        composable(Screen.SimDetails.route) {
            SimDetailsScreen(paddingValues, router)
        }
        composable(Screen.OrderType.route) {
            OrderTypeScreen(paddingValues, router)
        }
        composable(Screen.DeviceRemoval.route) {
            DeviceRemovalScreen(paddingValues, router)
        }
        composable(Screen.DeviceReplace.route) {
            DeviceReplaceScreen(paddingValues, router)
        }
        composable(Screen.DeviceRepair.route) {
            DeviceRepairScreen(paddingValues, router,scaffoldState)
        }
        composable(Screen.Done.route) {
            DoneScreen(paddingValues, router)
        }
    }
}