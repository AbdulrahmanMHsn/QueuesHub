package com.queueshub.ui.navigation

import androidx.annotation.DrawableRes
import com.queueshub.ui.navigation.Routes.ROUTE_CAR_INFO
import com.queueshub.ui.navigation.Routes.ROUTE_DEVICE_DETAILS
import com.queueshub.ui.navigation.Routes.ROUTE_DEVICE_ENTRY
import com.queueshub.ui.navigation.Routes.ROUTE_DEVICE_REMOVAL
import com.queueshub.ui.navigation.Routes.ROUTE_DEVICE_REPAIR
import com.queueshub.ui.navigation.Routes.ROUTE_DEVICE_REPLACE
import com.queueshub.ui.navigation.Routes.ROUTE_DONE
import com.queueshub.ui.navigation.Routes.ROUTE_HOME
import com.queueshub.ui.navigation.Routes.ROUTE_INFO_CONFIRMATION
import com.queueshub.ui.navigation.Routes.ROUTE_LOGIN
import com.queueshub.ui.navigation.Routes.ROUTE_MANUAL_LICENSE
import com.queueshub.ui.navigation.Routes.ROUTE_MANUAL_PLATE
import com.queueshub.ui.navigation.Routes.ROUTE_NOTE_INFO
import com.queueshub.ui.navigation.Routes.ROUTE_ORDERS
import com.queueshub.ui.navigation.Routes.ROUTE_ORDER_CARS
import com.queueshub.ui.navigation.Routes.ROUTE_ORDER_INFO
import com.queueshub.ui.navigation.Routes.ROUTE_ORDER_TYPE
import com.queueshub.ui.navigation.Routes.ROUTE_SIM_DETAILS
import com.queueshub.ui.navigation.Routes.ROUTE_SIM_ENTRY

object Routes {
    const val ROUTE_LOGIN = "ROUTE_LOGIN"
    const val ROUTE_HOME = "ROUTE_HOME"
    const val ROUTE_ORDER_INFO = "ROUTE_ORDER_INFO"
    const val ROUTE_NOTE_INFO = "ROUTE_NOTE_INFO"
    const val ROUTE_ORDERS = "ROUTE_ORDERS"
    const val ROUTE_ORDER_CARS= "ROUTE_ORDER_CARS"
    const val ROUTE_CAR_INFO = "ROUTE_CAR_INFO"
    const val ROUTE_MANUAL_PLATE = "ROUTE_MANUAL_PLATE"
    const val ROUTE_MANUAL_LICENSE = "ROUTE_MANUAL_LICENSE"
    const val ROUTE_INFO_CONFIRMATION = "ROUTE_INFO_CONFIRMATION"
    const val ROUTE_DEVICE_ENTRY = "ROUTE_DEVICE_ENTRY"
    const val ROUTE_DEVICE_DETAILS = "ROUTE_DEVICE_DETAILS"
    const val ROUTE_SIM_ENTRY = "ROUTE_SIM_ENTRY"
    const val ROUTE_SIM_DETAILS = "ROUTE_SIM_DETAILS"
    const val ROUTE_ORDER_TYPE = "ROUTE_ORDER_TYPE"
    const val ROUTE_DEVICE_REMOVAL = "ROUTE_DEVICE_REMOVAL"
    const val ROUTE_DEVICE_REPLACE = "ROUTE_DEVICE_REPLACE"
    const val ROUTE_DEVICE_REPAIR = "ROUTE_DEVICE_REPAIR"
    const val ROUTE_DONE = "ROUTE_DONE"
}

sealed class Screen(
    val route: String,
    var tag: String = route,
    val title: String = "",
    @DrawableRes val icon: Int = 0
) {

    object Login : Screen(route = ROUTE_LOGIN, title = "Login")
    object Home : Screen(route = ROUTE_CAR_INFO, title = "Home")
    object OrderInfo : Screen(route = ROUTE_ORDER_INFO, title = "Order_Info")
    object OrderNote : Screen(route = ROUTE_NOTE_INFO, title = "Order_Note")
    object Orders : Screen(route = ROUTE_ORDERS, title = "Orders")
    object OrderCars : Screen(route = ROUTE_ORDER_CARS, title = "OrderCars")
    object CarInfo : Screen(route = ROUTE_CAR_INFO, title = "CarInfo")
    object ManualPlate : Screen(route = ROUTE_MANUAL_PLATE, title = "ManualPlate")
    object ManualLicense : Screen(route = ROUTE_MANUAL_LICENSE, title = "ManualLicense")
    object InfoConfirmation : Screen(route = ROUTE_INFO_CONFIRMATION, title = "InfoConfirmation")
    object DeviceEntry : Screen(route = ROUTE_DEVICE_ENTRY, title = "DeviceEntry")
    object DeviceDetails : Screen(route = ROUTE_DEVICE_DETAILS, title = "DeviceDetails")
    object SimEntry : Screen(route = ROUTE_SIM_ENTRY, title = "SimEntry")
    object SimDetails : Screen(route = ROUTE_SIM_DETAILS, title = "SimDetails")
    object OrderType : Screen(route = ROUTE_ORDER_TYPE, title = "OrderType")
    object DeviceRemoval : Screen(route = ROUTE_DEVICE_REMOVAL, title = "DeviceRemoval")
    object DeviceReplace : Screen(route = ROUTE_DEVICE_REPLACE, title = "DeviceReplace")
    object DeviceRepair : Screen(route = ROUTE_DEVICE_REPAIR, title = "DeviceRepair")
    object Done : Screen(route = ROUTE_DONE, title = "done")


}