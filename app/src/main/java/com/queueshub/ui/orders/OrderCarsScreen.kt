package com.queueshub.ui.orders

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import androidx.hilt.navigation.compose.hiltViewModel
import com.queueshub.R
import com.queueshub.domain.model.Car
import com.queueshub.domain.model.Order
import com.queueshub.ui.AppViewModel
import com.queueshub.ui.MainActivity
import com.queueshub.ui.car.isAvailableGroup
import com.queueshub.ui.main.AppButton
import com.queueshub.ui.main.DialogBoxLoading
import com.queueshub.ui.main.InputField
import com.queueshub.ui.navigation.Router
import com.queueshub.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun OrderCarsScreen(paddingValues: PaddingValues = PaddingValues(), router: Router? = null) {
    val context = LocalContext.current
    val sharedViewModel: AppViewModel = hiltViewModel(context as MainActivity)
    val viewModel: OrdersViewModel = hiltViewModel()
    val ordersState by viewModel.carsState.collectAsState()

    LaunchedEffect(key1 = 0) {
        sharedViewModel.clearData()
    }

    val goCarInfo: () -> Unit = {
        sharedViewModel.generatedId = UUID.randomUUID().toString()
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues) // Apply padding from parent
    ) {
        ordersState.cars?.let { cars ->
            sharedViewModel.orderId = sharedViewModel.selectedOrder?.id ?: 0
            sharedViewModel.selectedOrder?.let { order ->
                OrderCarsContent(
                    viewModel = viewModel,
                    sharedViewModel = sharedViewModel,
                    order = order,
                    cars = cars,
                    goCarInfo = goCarInfo,
                    goOrderInfo = goOrderInfo,
                    goAllOrders = goAllOrders
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
                contentAlignment = Alignment.Center,
                modifier = Modifier.matchParentSize()
            ) {
                DialogBoxLoading()
            }
        }

        ordersState.failure?.let { failure ->
            val content = failure.getContentIfNotHandled()
            content?.let { error ->
                LaunchedEffect(error) {
                    Toast.makeText(context, error.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

@Composable
private fun OrderCarsContent(
    viewModel: OrdersViewModel,
    sharedViewModel: AppViewModel,
    order: Order,
    cars: List<Car>,
    goCarInfo: () -> Unit,
    goOrderInfo: () -> Unit,
    goAllOrders: () -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    var collected by remember { mutableStateOf(0) }
    var receivedAmount by remember { mutableStateOf("0") }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with back button
        HeaderSection(goOrderInfo = goOrderInfo)

        // Title section
        TitleSection(order = order)

        // Cars list
        CarsListSection(
            modifier = Modifier.weight(1f),
            order = order,
            sharedViewModel = sharedViewModel,
            cars = cars,
            goCarInfo = goCarInfo
        )

        // Bottom button
        BottomButton(
            order = order,
            viewModel = viewModel,
            onShowDialog = { showDialog = true },
            goAllOrders = goAllOrders
        )
    }

    // Payment dialog
    if (showDialog) {
        PaymentDialog(
            order = order,
            collected = collected,
            receivedAmount = receivedAmount,
            onCollectedChange = { collected = it },
            onAmountChange = {
                receivedAmount = it
                viewModel.receivedAmount = if (it.isNotEmpty()) it.toIntOrNull() ?: 0 else 0
            },
            onConfirm = {
                viewModel.closeOrder(order.id)
                goAllOrders()
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
private fun HeaderSection(goOrderInfo: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(top = 16.dp, start = 16.dp)
            .fillMaxWidth()
            .clickable { goOrderInfo() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.right),
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
        Text(
            modifier = Modifier.padding(start = 16.dp),
            text = "رجوع",
            style = MaterialTheme.typography.subtitle1
        )
    }
}

@Composable
private fun TitleSection(order: Order) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 48.dp)
    ) {
        Text(
            text = "سيارات امر التشغيل",
            style = MaterialTheme.typography.subtitle1
        )
        Text(
            text = "تم الانتهاء من",
            style = MaterialTheme.typography.subtitle2,
            modifier = Modifier.padding(top = 8.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp)
        ) {
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
    }
}

@Composable
private fun CarsListSection(
    modifier: Modifier = Modifier,
    order: Order,
    sharedViewModel: AppViewModel,
    cars: List<Car>,
    goCarInfo: () -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Completed cars
        items(cars.sortedByDescending { it.status }) { car ->
            CompletedCarItem(car = car)
        }

        // Pending cars
        val pendingCarsCount = order.numberOfCars - order.finishedCars
        if (pendingCarsCount > 0) {
            itemsIndexed(List(pendingCarsCount) { it }) { index, _ ->
                PendingCarItem(
                    index = index,
                    isNext = index == 0,
                    onClick = { if (index == 0)

                    // add current date object of device to technicalStart

                        try {
                            val date = Date()
                            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
                            val formatted = formatter.format(date)
                            sharedViewModel.technicalStart = formatted
                        }catch (e: Exception) {
                            sharedViewModel.technicalStart = Date().toString()
                        }

                        goCarInfo() }
                )
            }
        }

        // Add more cars button (if all cars are finished)
        if (order.finishedCars >= order.numberOfCars) {
            item {
                AddCarButton(onClick = goCarInfo)
            }
        }
    }
}

@Composable
private fun CompletedCarItem(car: Car) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SpanishGreen, RoundedCornerShape(8.dp))
            .border(1.dp, SpanishGreen, RoundedCornerShape(8.dp))
    ) {
        Image(
            modifier = Modifier.padding(16.dp),
            painter = painterResource(id = R.drawable.done_order),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )

        Spacer(
            modifier = Modifier
                .fillMaxHeight()
                .width(2.dp)
                .background(Color.White)
        )

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = if (car.plateNum.isNullOrBlank()) "رقم الشاسية" else "رقم اللوحة",
                style = MaterialTheme.typography.button,
                fontSize = 10.sp
            )
            Text(
                text = if (car.plateNum.isNullOrBlank()) {
                    car.chassisNum ?: ""
                } else {
                    car.plateNum
                },
                style = MaterialTheme.typography.button,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun PendingCarItem(
    index: Int,
    isNext: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(
                1.dp,
                if (isNext) Tundora else LightGrey,
                RoundedCornerShape(8.dp)
            )
            .clickable(enabled = isNext) { onClick() }
    ) {
        Image(
            modifier = Modifier.padding(16.dp),
            painter = painterResource(
                id = if (isNext) R.drawable.next_order else R.drawable.group_8
            ),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )

        Spacer(
            modifier = Modifier
                .fillMaxHeight()
                .width(2.dp)
                .background(Color.White)
        )
    }
}

@Composable
private fun AddCarButton(onClick: () -> Unit) {
    IconButton(
        modifier = Modifier
            .padding(top = 16.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(8.dp),
        onClick = onClick
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "",
            tint = Teal400
        )
    }
}

@Composable
private fun BottomButton(
    order: Order,
    viewModel: OrdersViewModel,
    onShowDialog: () -> Unit,
    goAllOrders: () -> Unit
) {
    AppButton(
        modifier = Modifier.padding(bottom = 34.dp),
        text = R.string.done,
        isEnabled = order.numberOfCars - order.finishedCars <= 0,
    ) {
        if (!viewModel.shouldPaid) {
            viewModel.closeOrder(order.id)
            goAllOrders()
        } else {
            onShowDialog()
        }
    }
}

@Composable
private fun PaymentDialog(
    order: Order,
    collected: Int,
    receivedAmount: String,
    onCollectedChange: (Int) -> Unit,
    onAmountChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
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
                        onCollectedChange(1)
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
                        onCollectedChange(0)
                        onAmountChange("0")
                    }
                }

                if (collected == 1) {
                    InputField(
                        stringResource(id = R.string.received_amount),
                        KeyboardType.Decimal,
                        receivedAmount,
                        modifier = Modifier.padding(16.dp),
                        onValueChange = { newValue ->
                            if (newValue.isDigitsOnly()) {
                                onAmountChange(newValue)
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
                    .padding(bottom = 16.dp),
                text = R.string.done,
                isEnabled = order.numberOfCars - order.finishedCars <= 0,
            ) {
                onConfirm()
            }
        }
    )
}

@Preview(locale = "ar", showBackground = true)
@Composable
fun OrderCarsScreenPreview() {
    val mockOrder = Order(
        id = 123,
        customerName = "شركة الاختبار",
        address = "القاهرة",
        inCompany = 0,
        governorate = null,
        startDate = "2025-06-23 10:00:00",
        endDate = "2025-06-23 12:00:00",
        status = "on_progress",
        numberOfCars = 3,
        finishedCars = 1,
        governorateId = 1L,
        neededNumber = "12345",
        customerId = 456L,
        customerDelegator = "محمد أحمد",
        customerDelegatorPhone = "+20123456789",
        customerNationalId = "29901012345678",
        statusAr = "قيد التنفيذ",
        orderCreator = 789L,
        neededAmount = "1500",
        receivedAmount = "1500",
        neededName = "صيانة سيارات"
    )

    val mockCar = Car(
        id = 1,
        plateNum = "ABC123",
        chassisNum = "CHASSIS123",
        status = "completed",
        startDate = "2025-06-23 10:00:00",
        statusAr = "مكتمل",
        statusDate = "2025-06-23 11:00:00",
        motorNum = "MOTOR123",
    )

    OrderCarsContent(
        viewModel = hiltViewModel(),
        sharedViewModel = hiltViewModel(),
        order = mockOrder,
        cars = listOf(mockCar, mockCar.copy(id = 2, plateNum = "XYZ789", status = "pending")),
        goCarInfo = {},
        goOrderInfo = {},
        goAllOrders = {}
    )
}