package com.queueshub.ui.orders

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.queueshub.R
import com.queueshub.ui.AppViewModel
import com.queueshub.ui.MainActivity
import com.queueshub.ui.car.isAvailableGroup
import com.queueshub.ui.main.AppButton
import com.queueshub.ui.main.DialogBoxLoading
import com.queueshub.ui.navigation.Router
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.core.text.isDigitsOnly
import com.queueshub.domain.model.Car
import com.queueshub.domain.model.Order
import com.queueshub.ui.main.InputField
import com.queueshub.ui.theme.*


@Composable
fun OrderCarsScreen(paddingValues: PaddingValues = PaddingValues(), router: Router? = null) {

    val context = LocalContext.current

    val sharedViewModel: AppViewModel = hiltViewModel(context as MainActivity)
    sharedViewModel.clearData()
    val viewModel: OrdersViewModel = hiltViewModel()
    val ordersState by viewModel.carsState.collectAsState()
    val goCarInfo: () -> Unit = {
        router?.goCarInfo()
    }
    val goAllOrders: () -> Unit = {
        router?.goAllOrders()
    }
    val goOrderInfo: () -> Unit = {
        if (!sharedViewModel.showedInfo) {
            router?.goOrderInfo()
            sharedViewModel.showedInfo = true
        }
    }
    LaunchedEffect(sharedViewModel.selectedOrder) {
        sharedViewModel.selectedOrder?.let { viewModel.getOrders(it) }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        ordersState.cars?.let {
            sharedViewModel.orderId = sharedViewModel.selectedOrder?.id ?: 0
            sharedViewModel.selectedOrder?.let { it1 ->
                orderCarsContent(
                    viewModel, it1, it, goCarInfo, goOrderInfo,goAllOrders
                )
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun orderCarsContent(
    viewmodel: OrdersViewModel,
    order: Order,
    cars: List<Car>,
    goCarInfo: () -> Unit,
    goOrderInfo: () -> Unit,
    goAllOrders: () -> Unit,
) {
    var showDialog: Boolean by remember { mutableStateOf(false) }
    var collected: Int by remember { mutableStateOf(0) }
    var receivedAmount: String by remember { mutableStateOf("0") }
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        // Fixed content here
        Row(
            modifier = Modifier
                .padding(top = 16.dp, start = 16.dp)
                .align(Alignment.Start)
                .clickable {
                    goOrderInfo()
                }, verticalAlignment = Alignment.CenterVertically
        ) {

            val image: Painter = painterResource(id = R.drawable.right)

            Image(
                painter = image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )

            Text(
                modifier = Modifier.padding(start = 16.dp),
                text = "رجوع",
                style = MaterialTheme.typography.subtitle1
            )
        }
        Text(
            text = "سيارات امر التشغيل",
            modifier = Modifier.padding(top = 48.dp),
            style = MaterialTheme.typography.subtitle1
        )
        Text(
            modifier = Modifier, text = "تم الانتهاء من", style = MaterialTheme.typography.subtitle2
        )

        Row(verticalAlignment = Alignment.CenterVertically) {

            Text(
                text = "${order.finishedCars}",
                fontSize = 35.sp,
                color = SpanishGreen,
                style = MaterialTheme.typography.subtitle2
            )
            Text(
                text = "/${order.numberOfCars}",
                fontSize = 30.sp,
                style = MaterialTheme.typography.subtitle2
            )
        }

        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // your scrollable content here
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(cars.sortedByDescending { it.status }) { car ->
                    Row(
                        modifier = Modifier
                            .padding(6.dp)
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                            .background(
                                SpanishGreen,
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.dp,
                                SpanishGreen,
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        val image: Painter =
                            painterResource(id = R.drawable.done_order)
                        Image(
                            modifier = Modifier.padding(16.dp),
                            painter = image,
                            contentDescription = null,
                            contentScale = ContentScale.Crop
                        )
                        Divider(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(2.dp),
                            color = Color.White,
                            thickness = 2.dp
                        )
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = if(car.plateNum.isNullOrBlank())  "رقم الشاسية" else "رقم اللوحة",
                                style = MaterialTheme.typography.button,
                                fontSize = 10.sp
                            )
                            Text(
                                text = if(car.plateNum.isNullOrBlank()) car.chassisNum?:""  else car.plateNum,
                                style = MaterialTheme.typography.button,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                if(order.numberOfCars - order.finishedCars >0) {
                    val x = arrayOfNulls<String?>(order.numberOfCars - order.finishedCars)
                    itemsIndexed(x) { index: Int, item: String? ->
                        Row(
                            modifier = Modifier
                                .padding(6.dp)
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                                .background(
                                    Color.White,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    if (index == 0) Tundora else LightGrey,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    if (index == 0) {
                                        goCarInfo()
                                    }
                                }
                        ) {
                            val image: Painter =
                                painterResource(id = if (index != 0) R.drawable.group_8 else R.drawable.next_order)
                            Image(
                                modifier = Modifier.padding(16.dp),
                                painter = image,
                                contentDescription = null,
                                contentScale = ContentScale.Crop
                            )
                            Divider(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(2.dp),
                                color = Color.White,
                                thickness = 2.dp
                            )

                        }
                    }
                }
                if (order.finishedCars >= order.numberOfCars) {
                    item {
                        IconButton(modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 16.dp)
                            .background(
                                Color.White,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp), onClick = { goCarInfo() }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "",
                                tint = Teal400
                            )
                        }
                    }
                }
            }
        }


        AppButton(
            modifier = Modifier.padding(bottom = 34.dp),
            text = R.string.done, isEnabled = order.numberOfCars - order.finishedCars <= 0,
        ) {
            if (!viewmodel.shouldPaid) {
                viewmodel.closeOrder(order.id)
                goAllOrders()
            }
            else {
                showDialog = true
            }
        }
        if (showDialog) {
            AlertDialog(modifier = Modifier
                .height(400.dp)
                .padding(vertical = 16.dp),
                onDismissRequest = {},
                title = {

                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        text = "تحصيل رسوم أمر التشغيل",
                        style = MaterialTheme.typography.subtitle1,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column {
                        Text(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            text = "هل تم تحصيل رسوم امر التشغيل؟",
                            style = MaterialTheme.typography.subtitle2
                        )


                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {

                            isAvailableGroup(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 4.dp),
                                collected == 1,
                                true,
                                SpanishGreen,
                                Icons.Default.Check,
                                R.string.done_collecting,
                                fontSize = 12.sp,
                            ) {
                                collected = 1
                            }

                            isAvailableGroup(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 4.dp),
                                collected == 0,
                                false,
                                DarkRed,
                                Icons.Default.Clear,
                                R.string.not_collected,
                                fontSize = 12.sp,
                            ) {
                                collected = 0
                                viewmodel.receivedAmount = 0
                            }
                        }

                        if (collected == 1) {
                            InputField(
                                stringResource(id = R.string.received_amount),
                                KeyboardType.Decimal,
                                receivedAmount,
                                modifier = Modifier.padding(16.dp),
                                onValueChange = {
                                    if (it.isDigitsOnly()) {
                                        receivedAmount = it
                                        if (it.isNotEmpty()) viewmodel.receivedAmount = it.toInt()
                                        else viewmodel.receivedAmount = 0
                                    }
                                },
                                imeAction = ImeAction.Done
                            )

                        }
                    }
                },
                confirmButton = {
                    AppButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 34.dp),
                        text = R.string.done,
                        isEnabled = order.numberOfCars - order.finishedCars <= 0,
                    ) {
                        viewmodel.closeOrder(order.id)
                        goAllOrders()
                    }
                })
        }
    }
}
