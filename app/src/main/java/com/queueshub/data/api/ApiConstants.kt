package com.queueshub.data.api
object ApiConstants {
    const val BASE_ENDPOINT = "https://stg-scanner-api.infinity-egy.net/api/"
    const val BASE_URL = "https://stg-scanner-api.infinity-egy.net/"
    const val LOGIN_ENDPOINT = "auth/loginMobile"
    const val CURRENT_ENDPOINT = "orderCar/getOrderCarsForOrder"
    const val MY_ORDERS_ENDPOINT = "order/getMyCurrentOrders"
    const val SENSORS_ENDPOINT = "sensor/getSensors"
    const val CARS_TYPES_ENDPOINT = "carModel/getCarModels"
    const val MAINTENANCE_ENDPOINT = "maintenance/getMaintenances"
    const val CREATE_ORDER_ENDPOINT = "orderCar/createOrderCar"
    const val CLOSE_ORDER_ENDPOINT = "order/changeOrderStatusForTechnical"
    const val LOG_DATA = "systemLog/createSystemLog"
}

object ApiParameters {
    const val API_KEY = "api-key"
    const val PAGE = "page"
    const val THUMBNAIL_TYPE = "Standard Thumbnail"
    const val MEDIUM210_TYPE = "mediumThreeByTwo210"
    const val MEDIUM440_TYPE = "mediumThreeByTwo440"
}

