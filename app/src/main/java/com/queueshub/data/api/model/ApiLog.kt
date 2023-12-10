package com.queueshub.data.api.model


import com.google.gson.annotations.SerializedName

data class ApiLog(

	val logs: ArrayList<ApiLogItem>? = null,

)
data class ApiLogItem(

	@field:SerializedName("plate_num")
	val plateNum: Any? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("order_id")
	val orderId: Int? = null,

	@field:SerializedName("id")
	val generatedId: String? = null
)
