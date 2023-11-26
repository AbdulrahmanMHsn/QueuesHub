package com.queueshub.ui.navigation

import androidx.navigation.NavHostController
import com.queueshub.ui.navigation.Routes.ROUTE_ORDERS
import com.queueshub.utils.putArgs

class RouterImpl(
    private val navHostController: NavHostController,
    private val startDestination: String = ROUTE_ORDERS
) : Router {


    override fun goHome() {
        navigate(Screen.Orders, removeFromHistory = true, singleTop = true)
    }

    override fun goOrderInfo() {
        navigate(Screen.OrderInfo, removeFromHistory = true, singleTop = true)
    }

    override fun goCarInfo() {
        navigate(Screen.CarInfo, removeFromHistory = true, singleTop = true)
    }

    override fun goInfoConfirmation() {
        navigate(Screen.InfoConfirmation)
    }

    override fun goManualLicense() {
        navigate(Screen.ManualLicense)
    }

    override fun goManualPlate() {
        navigate(Screen.ManualPlate)
    }

    override fun goAllOrders() {
        navigate(Screen.Orders, true, true,true)
    }

    override fun goOrderCars() {
        navigate(Screen.OrderCars)
    }

    override fun goDeviceEntry() {
        navigate(Screen.DeviceEntry)
    }

    override fun goDeviceDetails() {
        navigate(Screen.DeviceDetails)
    }

    override fun goSimEntry() {
        navigate(Screen.SimEntry)
    }

    override fun goSimDetails() {
        navigate(Screen.SimDetails)
    }

    override fun goOrderType() {
        navigate(Screen.OrderType)
    }

    override fun goDeviceRemove() {
        navigate(Screen.DeviceRemoval)
    }

    override fun goDeviceRepair() {
        navigate(Screen.DeviceRepair)
    }

    override fun goDeviceReplace() {
        navigate(Screen.DeviceReplace)
    }

    override fun goOrderNote() {
        navigate(Screen.OrderNote)
    }

    override fun goDone() {
        navigate(Screen.Done, true, true,true)
    }

    override fun goBack(dest:String) {
        navHostController.apply {
            navigateUp()
            navigate(dest) {
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    private fun navigate(
        screen: Screen,
        removeFromHistory: Boolean = false,
        singleTop: Boolean = false,
        noLogin: Boolean = false,
    ) {
        navHostController.apply {
            navigate(screen.route) {
                if (removeFromHistory) {
                    if (singleTop) {
                        if (noLogin)
                            popUpTo(Screen.Orders.route) {
                                inclusive = true
                            }
                        else
                            popUpTo(Screen.Login.route) {
                                inclusive = true
                            }
                    } else {
                        popUpTo(0) {
                            saveState = false
                        }
                    }

                } else {
                    restoreState = true
                }
                launchSingleTop = singleTop
            }
        }
    }

    private fun checkArgsAndNavigate(it: Any?, screen: Screen) {
        it?.let {
            navHostController.putArgs(Pair(screen.tag, it))
        }
        navigate(screen)
    }

    override fun <T : Any> getArgs(tag: String): T? {
        return try {
            navHostController.previousBackStackEntry?.arguments?.get(tag) as T?
        } catch (ex: Exception) {
            null
        }
    }

}