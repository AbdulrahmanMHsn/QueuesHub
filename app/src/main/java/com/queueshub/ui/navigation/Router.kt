package com.queueshub.ui.navigation

interface Router {
    fun goHome()
    fun goOrderInfo()
    fun goCarInfo()
    fun goInfoConfirmation()
    fun goManualLicense()
    fun goManualPlate()
    fun goAllOrders()
    fun goOrderCars()
    fun goDeviceEntry()
    fun goDeviceDetails()
    fun goSimEntry()
    fun goSimDetails()
    fun goOrderType()
    fun goDeviceRemove()
    fun goDeviceRepair()
    fun goDeviceReplace()
    fun goOrderNote()
    fun goDone()
    fun goBack(dest:String = Routes.ROUTE_ORDERS)
    fun <T : Any> getArgs(tag: String): T?
}