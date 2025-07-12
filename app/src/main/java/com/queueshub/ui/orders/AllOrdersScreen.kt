package com.queueshub.ui.orders

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.queueshub.R
import com.queueshub.domain.model.Order
import com.queueshub.ui.navigation.Router
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.queueshub.ui.AppViewModel
import com.queueshub.ui.MainActivity
import com.queueshub.ui.main.DialogBoxLoading
import com.queueshub.ui.theme.*
import com.queueshub.utils.DateUtils
import java.util.*


@Composable
fun AllOrdersScreen(paddingValues: PaddingValues = PaddingValues(), router: Router? = null) {
    val viewModel: OrdersViewModel = hiltViewModel()
    val context = LocalContext.current
    val sharedViewModel: AppViewModel = hiltViewModel(context as MainActivity)
    val ordersState by viewModel.state.collectAsState()
    val goOrderInfo: (Order) -> Unit = {
        sharedViewModel.selectedOrder = it
        router?.goOrderInfo()
    }
    val goOrderCars: (Order) -> Unit = {
        sharedViewModel.selectedOrder = it
        router?.goOrderCars()
    }
    Column(Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)) {
        Text(
            text = "جميع أوامر التشغيل",
            modifier = Modifier,
            style = MaterialTheme.typography.subtitle1
        )
        LaunchedEffect(Unit) {
            viewModel.fetchAllOrders()
        }
        Box(
            modifier = Modifier
                .padding(top = 32.dp)
                .fillMaxSize()
        ) {
            ordersState.orders?.sortedBy { it.startDate }?.let {
                val anyInProgress = it.any { it.status == "on_progress" }
                sharedViewModel.isAnyOrderOnProgress = anyInProgress
                LaunchedEffect(anyInProgress) {

                    if (anyInProgress && !sharedViewModel.goingBack)
                        goOrderCars(it.find { it.status == "on_progress" }!!)
                }
                if (!anyInProgress || sharedViewModel.goingBack) {
                    OrdersList(it, anyInProgress, goOrderInfo)
                }
            } ?: run {
                Image(
                    modifier = Modifier.align(Alignment.Center),
                    painter = painterResource(id = R.drawable.no_orders),
                    contentDescription = "لا يوجد طلبات حالياً"
                )
            }
            if (ordersState.loading) {
                Box(
                    contentAlignment = Alignment.Center, modifier = Modifier.matchParentSize()
                ) {
                    DialogBoxLoading()
                }
            }
            ordersState.failure?.let {
                val content = it.getContentIfNotHandled()
                content?.let {
                    Toast.makeText(
                        context, it.localizedMessage, Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OrdersList(orders: List<Order>, anyInProgress: Boolean, goOrderInfo: (Order) -> Unit) {
    LazyColumn {
        orders.groupBy { it.startDate.split(" ")[0] }.forEach { (date, orders) ->

            stickyHeader(key = date) {
                DateHeader(date)
            }
            items(orders) { order ->
                OrderBox(order, anyInProgress, goOrderInfo)
            }
        }

    }
}

@Composable
fun OrderBox(
    order: Order,
    anyInProgress: Boolean,
    goOrderInfo: (Order) -> Unit
) {
    val selected = order.status == "on_progress"
    Column(
        modifier = Modifier
            .padding(vertical = 16.dp)
            .fillMaxWidth()
            .border(
                1.dp,
                if (selected) InProgress else if (anyInProgress) ChathamsBlue else Color.LightGray,
                RoundedCornerShape(8.dp)
            )
            .clickable {  goOrderInfo(order) }

    ) {
        if (selected) {
            Text(
                text = "قيد التنفيذ",
                style = MaterialTheme.typography.body1,
                color = InProgressText,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        InProgress.copy(alpha = .20f),
                        RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                    )
                    .padding(8.dp)
            )
        }
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Row(modifier = Modifier.padding(top = 16.dp)) {
                Icon(imageVector = Icons.Default.Badge, contentDescription = "")
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(
                        text = "الرمز التعريفي ",
                        style = MaterialTheme.typography.body1,
                        fontSize = 12.sp
                    )
                    Text(text = order.id.toString(), style = MaterialTheme.typography.body1)
                }
            }

            Row(modifier = Modifier.padding(top = 16.dp)) {
                Icon(imageVector = Icons.Default.Payment, contentDescription = "")
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(
                        text = "اسم الشركة/العميل",
                        style = MaterialTheme.typography.body1,
                        fontSize = 12.sp
                    )
                    Text(text = order.customerName, style = MaterialTheme.typography.body1)
                }
            }
            Row(modifier = Modifier.padding(top = 16.dp)) {
                Icon(imageVector = Icons.Default.LocationOn, contentDescription = "")
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(text = "العنوان", style = MaterialTheme.typography.body1, fontSize = 12.sp)
                    Text(
                        text = if (order.inCompany == 0) order.address else "شركة انفينيتي",
                        style = MaterialTheme.typography.body1
                    )
                }
            }
            if (order.inCompany == 0)
                Row(modifier = Modifier.padding(top = 16.dp)) {
                    Icon(imageVector = Icons.Default.LocationCity, contentDescription = "")
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(
                            text = "المحافظة",
                            style = MaterialTheme.typography.body1,
                            fontSize = 12.sp
                        )
                        Text(
                            text = order.governorate?.name ?: "",
                            style = MaterialTheme.typography.body1
                        )
                    }
                }
            Row(modifier = Modifier.padding(top = 16.dp)) {
                Icon(imageVector = Icons.Default.AccessTime, contentDescription = "")
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(text = "الوقت", style = MaterialTheme.typography.body1, fontSize = 12.sp)
                    Text(
                        text = "من ${
                            DateUtils.formatTime(
                                order.startDate.split(" ")[1],
                                "HH:mm:ss",
                                Locale.US,
                                "hh:mm a",
                                Locale.getDefault()
                            )
                        } الى  ${
                            DateUtils.formatTime(
                                order.endDate.split(" ")[1],
                                "HH:mm:ss",
                                Locale.US,
                                "hh:mm a",
                                Locale.getDefault()
                            )
                        }", style = MaterialTheme.typography.body1
                    )
                }
            }

                IconButton(modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 16.dp)
                    .background(
                        Teal400,
                        RoundedCornerShape(32.dp)
                    )
                    .padding(horizontal = 32.dp), onClick = { goOrderInfo(order) }) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "",
                        tint = Color.White
                    )
                }

        }
    }
}

@Composable
fun DateHeader(date: String) {
    Box(Modifier.fillMaxWidth().background(Background)){

    Text(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Teal400_20 ,
                RoundedCornerShape(8.dp)
            )
            .padding(10.dp),
        color =ChathamsBlue ,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.subtitle1,
        fontSize = 16.sp,
        text = DateUtils.formatTime(
            date,
            "yyyy-MM-dd",
            Locale.US,
            "EEE MM/dd",
            Locale.getDefault()
        ),
    )
    }
}

@Composable
fun AllOrdersScreenContent(
    orders: List<Order>?,
    loading: Boolean,
    failureMessage: String?,
    anyInProgress: Boolean,
    paddingValues: PaddingValues = PaddingValues(),
    goOrderInfo: (Order) -> Unit = {},
) {
    Column(Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)) {
        Text(
            text = "جميع أوامر التشغيل",
            modifier = Modifier,
            style = MaterialTheme.typography.subtitle1
        )
        Box(
            modifier = Modifier
                .padding(top = 32.dp)
                .fillMaxSize()
        ) {
            orders?.sortedBy { it.startDate }?.let {
                if (!anyInProgress) {
                    OrdersList(it, anyInProgress, goOrderInfo)
                }
            } ?: run {
                Image(
                    modifier = Modifier.align(Alignment.Center),
                    painter = painterResource(id = R.drawable.no_orders),
                    contentDescription = "لا يوجد طلبات حالياً"
                )
            }
            if (loading) {
                Box(
                    contentAlignment = Alignment.Center, modifier = Modifier.matchParentSize()
                ) {
                    DialogBoxLoading()
                }
            }
            failureMessage?.let {
                // In preview, just show a text instead of a Toast
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun AllOrdersScreenPreview() {
    // Mock Order data
    val mockOrder = Order(
        id = 123,
        customerName = "شركة الاختبار",
        address = "القاهرة",
        inCompany = 0,
        governorate = null,
        startDate = "2025-07-29 10:00:00",
        endDate = "2025-06-29 12:00:00",
        status = "in_progress",
        numberOfCars = 1,
        finishedCars = 1,
        governorateId = 1L,
        neededNumber = "12345",
        customerId = 456L,
        customerDelegator = "محمد أحمد",
        customerDelegatorPhone = "+20123456789",
        customerNationalId = "29901012345678",
        statusAr = "مكتمل",
        orderCreator = 789L,
        neededAmount = "1500",
        receivedAmount = "1500",
        neededName = "صيانة سيارات"
    )

    val mockOrderr =  Order(
        id = 123,
        customerName = "شركة الاختبار",
        address = "القاهرة",
        inCompany = 0,
        governorate = null,
        startDate = "2025-07-29 10:00:00",
        endDate = "2025-06-29 12:00:00",
        status = "on_progress",
        numberOfCars = 1,
        finishedCars = 1,
        governorateId = 1L,
        neededNumber = "12345",
        customerId = 456L,
        customerDelegator = "محمد أحمد",
        customerDelegatorPhone = "+20123456789",
        customerNationalId = "29901012345678",
        statusAr = "مكتمل",
        orderCreator = 789L,
        neededAmount = "1500",
        receivedAmount = "1500",
        neededName = "صيانة سيارات"
    )

    AllOrdersScreenContent(
        orders = listOf(mockOrder,mockOrderr),
        loading = false,
        failureMessage = null,
        anyInProgress = false
    )
}