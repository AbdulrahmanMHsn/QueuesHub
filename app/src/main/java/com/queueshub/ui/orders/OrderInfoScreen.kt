package com.queueshub.ui.orders

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.queueshub.R
import com.queueshub.domain.model.Order
import com.queueshub.ui.AppViewModel
import com.queueshub.ui.MainActivity
import com.queueshub.ui.car.showErrorSnackbar
import com.queueshub.ui.main.AppButton
import com.queueshub.ui.main.DialogBoxLoading
import com.queueshub.ui.main.InputField
import com.queueshub.ui.navigation.Router
import com.queueshub.ui.theme.ChathamsBlue
import com.queueshub.ui.theme.SpanishGreen
import com.queueshub.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OrderInfoScreen(paddingValues: PaddingValues = PaddingValues(), router: Router? = null) {
    val context = LocalContext.current
    val sharedViewModel: AppViewModel = hiltViewModel(context as MainActivity)
    val viewModel: OrdersViewModel = hiltViewModel()
    val ordersState by viewModel.state.collectAsState()

    val goOrderInfo: () -> Unit = {
        router?.goOrderCars()
    }
    val goAllOrders: () -> Unit = {
        sharedViewModel.goingBack = true
        router?.goAllOrders()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues) // Apply parent padding
    ) {
        sharedViewModel.selectedOrder?.let { order ->
            sharedViewModel.orderId = order.id
            OrderInfoContent(
                viewModel = viewModel,
                sharedViewModel,
                order = order,
                goOrderInfo = goOrderInfo,
                goAllOrders = goAllOrders
            )
        } ?: run {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = "لا يوجد طلبات حالياً"
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
private fun OrderInfoContent(
    viewModel: OrdersViewModel,
    sharedViewModel: AppViewModel,
    order: Order,
    goOrderInfo: () -> Unit,
    goAllOrders: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with back button
        HeaderSection(goAllOrders = goAllOrders)

        // Title
        TitleSection()

        // Main content card
        ContentCard(
            modifier = Modifier.weight(1f),
            viewModel = viewModel,
            sharedViewModel,
            order = order,
            goOrderInfo = goOrderInfo
        )
    }
}

@Composable
private fun HeaderSection(goAllOrders: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp)
            .clickable { goAllOrders() },
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
private fun TitleSection() {
    Text(
        text = "بيانات أمر التشغيل",
        modifier = Modifier.padding(top = 48.dp),
        style = MaterialTheme.typography.subtitle1
    )
}

@Composable
private fun ContentCard(
    modifier: Modifier = Modifier,
    viewModel: OrdersViewModel,
    sharedViewModel: AppViewModel,
    order: Order,
    goOrderInfo: () -> Unit
) {
    Card(
        modifier = modifier
            .padding(vertical = 24.dp, horizontal = 16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = Color.White,
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Customer Information Section
            CustomerInfoSection(order = order)

            // Payment Information (if applicable)
            if (order.neededAmount != "0") {
                PaymentInfoSection(order = order)
            }

            // Visit Information Section
            VisitInfoSection(order = order)

            // Bottom button with spacer to push it down
            Spacer(modifier = Modifier.weight(1f))
            BottomButton(
                viewModel = viewModel,
                sharedViewModel,
                order = order,
                goOrderInfo = goOrderInfo
            )
        }
    }
}

@Composable
private fun CustomerInfoSection(order: Order) {
    Column {
        SectionTitle(text = "بيانات العميل")

        InputField(
            text = "اسم الشركة/العميل",
            keyboardType = KeyboardType.Text,
            content = order.customerName,
            isEnabled = false,
            onValueChange = {},
            modifier = Modifier.padding(top = 16.dp),
            imeAction = ImeAction.Next
        )

        InputField(
            text = "رقم تواصل الموقع",
            keyboardType = KeyboardType.Text,
            content = order.neededNumber,
            isEnabled = true,
            readOnly = true,
            modifier = Modifier.padding(top = 16.dp),
            onValueChange = {},
            imeAction = ImeAction.Next
        )

        InputField(
            text = "اسم الشخص المسؤل",
            keyboardType = KeyboardType.Text,
            content = order.neededName,
            isEnabled = false,
            modifier = Modifier.padding(top = 16.dp),
            onValueChange = {},
            imeAction = ImeAction.Next
        )
    }
}

@Composable
private fun PaymentInfoSection(order: Order) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.mdi_money_100),
            contentDescription = "",
            modifier = Modifier.padding(end = 8.dp)
        )

        Text(
            text = "تحصيل رسوم امر التشغيل مبلغ ( ${order.neededAmount} جنيه )",
            color = SpanishGreen,
            style = MaterialTheme.typography.body2,
            modifier = Modifier.weight(1f)
        )

        Image(
            modifier = Modifier.padding(start = 8.dp),
            painter = painterResource(id = R.drawable.vector),
            contentDescription = ""
        )
    }
}

@Composable
private fun VisitInfoSection(order: Order) {
    Column(modifier = Modifier.padding(top = 24.dp)) {
        SectionTitle(text = "بيانات الزيارة")

        InputField(
            text = "تاريخ الزيارة",
            keyboardType = KeyboardType.Text,
            content = order.startDate.split(" ").getOrNull(0) ?: "",
            isEnabled = false,
            modifier = Modifier.padding(top = 16.dp),
            onValueChange = {},
            imeAction = ImeAction.Next
        )

        val timeValue = try {
            val timePart = order.startDate.split(" ").getOrNull(1) ?: "00:00:00"
            DateUtils.formatTime(
                timePart,
                "HH:mm:ss",
                Locale.US,
                "hh:mm aa",
                Locale.getDefault()
            )
        } catch (e: Exception) {
            "غير محدد"
        }

        InputField(
            text =  "وقت الزيارة",
            keyboardType = KeyboardType.Text,
           content =  timeValue,
            isEnabled = false,
            modifier = Modifier.padding(top = 16.dp),
            onValueChange = {},
            imeAction = ImeAction.Next
        )

        if (order.inCompany == 0) {
            InputField(
                text = "المحافظة",
                keyboardType = KeyboardType.Text,
                content = order.governorate?.name ?: "",
                isEnabled = false,
                modifier = Modifier.padding(top = 16.dp),
                onValueChange = {},
                imeAction = ImeAction.Next
            )
        }

        InputField(
            text = "العنوان",
            keyboardType = KeyboardType.Text,
            content = if (order.inCompany == 0) order.address else "شركة انفينيتي",
            isEnabled = false,
            modifier = Modifier.padding(top = 16.dp),
            onValueChange = {},
            imeAction = ImeAction.Next
        )
    }
}

@Composable
private fun BottomButton(
    viewModel: OrdersViewModel,
    sharedViewModel: AppViewModel,
    order: Order,
    goOrderInfo: () -> Unit
) {
    val orderDate = try {
        DateUtils.convertStringToDateTime(order.startDate, Locale.US)
    } catch (e: Exception) {
        Date() // Default to current date if parsing fails
    }


    AppButton(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        text = if (order.finishedCars == 0) R.string.start else R.string.next,
        isEnabled = orderDate.before(Date()) && (order.status == "on_progress" || !sharedViewModel.isAnyOrderOnProgress),
    ) {
            viewModel.startOrder()
            goOrderInfo()
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = ChathamsBlue,
        style = MaterialTheme.typography.subtitle1,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Preview(locale = "ar", showBackground = true)
@Composable
fun OrderInfoScreenPreview() {
    // Mock data for preview
    val mockOrder = Order(
        id = 123,
        customerName = "شركة الاختبار المحدودة",
        address = "القاهرة، مدينة نصر، شارع التحرير، برج التجارة الدولي",
        inCompany = 0,
        governorate = null,
        startDate = "2025-06-23 10:00:00",
        endDate = "2025-06-23 12:00:00",
        status = "on_progress",
        numberOfCars = 3,
        finishedCars = 1,
        governorateId = 1L,
        neededNumber = "+201234567890",
        customerId = 456L,
        customerDelegator = "محمد أحمد علي",
        customerDelegatorPhone = "+20123456789",
        customerNationalId = "29901012345678",
        statusAr = "قيد التنفيذ",
        orderCreator = 789L,
        neededAmount = "2500",
        receivedAmount = "1500",
        neededName = "أحمد محمد - مدير الصيانة"
    )

    // Show the content directly without ViewModel dependencies
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp)
                .clickable { },
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

        // Title
        Text(
            text = "بيانات أمر التشغيل",
            modifier = Modifier.padding(top = 48.dp),
            style = MaterialTheme.typography.subtitle1
        )

        // Main content card
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 24.dp, horizontal = 16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            backgroundColor = Color.White,
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Customer Information Section
                CustomerInfoSection(order = mockOrder)

                // Payment Information (if applicable)
                if (mockOrder.neededAmount != "0") {
                    PaymentInfoSection(order = mockOrder)
                }

                // Visit Information Section
                VisitInfoSection(order = mockOrder)

                // Bottom button with spacer to push it down
                Spacer(modifier = Modifier.weight(1f))
                
                // Mock button
                AppButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    text = if (mockOrder.finishedCars == 0) R.string.start else R.string.next,
                    isEnabled = true,
                ) {
                    // Mock action
                }
            }
        }
    }
}

@Preview(locale = "ar", showBackground = true)
@Composable
fun CustomerInfoSectionPreview() {
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
    
    Card(
        modifier = Modifier.padding(16.dp),
        shape = RoundedCornerShape(8.dp),
        backgroundColor = Color.White
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            CustomerInfoSection(order = mockOrder)
        }
    }
}

@Preview(locale = "ar", showBackground = true)
@Composable
fun PaymentInfoSectionPreview() {
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
        neededAmount = "3500", // With payment
        receivedAmount = "1500",
        neededName = "صيانة سيارات"
    )
    
    Card(
        modifier = Modifier.padding(16.dp),
        shape = RoundedCornerShape(8.dp),
        backgroundColor = Color.White
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            PaymentInfoSection(order = mockOrder)
        }
    }
}

@Preview(locale = "ar", showBackground = true)
@Composable
fun VisitInfoSectionPreview() {
    val mockOrder = Order(
        id = 123,
        customerName = "شركة الاختبار",
        address = "القاهرة، مدينة نصر، شارع التحرير",
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
    
    Card(
        modifier = Modifier.padding(16.dp),
        shape = RoundedCornerShape(8.dp),
        backgroundColor = Color.White
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            VisitInfoSection(order = mockOrder)
        }
    }
}