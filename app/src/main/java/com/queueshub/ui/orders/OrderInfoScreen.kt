package com.queueshub.ui.orders

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.queueshub.R
import com.queueshub.domain.model.Order
import com.queueshub.ui.AppViewModel
import com.queueshub.ui.CameraBarcodePreview
import com.queueshub.ui.MainActivity
import com.queueshub.ui.main.AppButton
import com.queueshub.ui.main.DialogBoxLoading
import com.queueshub.ui.main.InputField
import com.queueshub.ui.navigation.Router
import com.queueshub.ui.theme.ChathamsBlue
import com.queueshub.ui.theme.SpanishGreen
import com.queueshub.utils.DateUtils
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
    Box(modifier = Modifier.fillMaxSize()) {
        sharedViewModel.selectedOrder?.let {
            sharedViewModel.orderId = it.id
            OrderInfo(viewModel, it, goOrderInfo,goAllOrders)
        } ?: run {
            Text(modifier = Modifier.align(Alignment.Center), text = "لا يوجد طلبات حالياً")
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

@Composable
fun OrderInfo(viewModel: OrdersViewModel, order: Order, goOrderInfo: () -> Unit, goAllOrders: () -> Unit) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
// Fixed content here
        Row(
            modifier = Modifier
                .padding(top = 16.dp, start = 16.dp)
                .align(Alignment.Start)
                .clickable {
                    goAllOrders()
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

        val orderDate = DateUtils.convertStringToDateTime(order.startDate, Locale.US)
        Text(
            text = "بيانات أمر التشغيل",
            modifier = Modifier.padding(top = 48.dp),
            style = MaterialTheme.typography.subtitle1
        )
        Card(
            modifier = Modifier
                .padding(vertical = 24.dp, horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            backgroundColor = Color.White
        ) {
            Column(
                horizontalAlignment = Alignment.Start, modifier = Modifier
                    .padding(all = 16.dp)
                    .verticalScroll(
                        rememberScrollState()
                    )
            ) {
                Text(
                    text = "بيانات العميل",
                    color = ChathamsBlue, style = MaterialTheme.typography.subtitle1
                )
                InputField(
                    "اسم الشركة/العميل",
                    KeyboardType.Text,
                    order.customerName,
                    isEnabled = false,
                    onValueChange = {
                    },
                    modifier = Modifier.padding(top = 16.dp),
                    imeAction = ImeAction.Next
                ) {
                }
                InputField(
                    "رقم تواصل الموقع",
                    KeyboardType.Text,
                    order.neededNumber,
                    isEnabled = true,
                    readOnly = true,
                    modifier = Modifier.padding(top = 16.dp),
                    onValueChange = {
                    },
                    imeAction = ImeAction.Next
                ) {
                }
                InputField(
                    "اسم الشخص المسؤل",
                    KeyboardType.Text,
                    order.neededName,
                    isEnabled = false,
                    modifier = Modifier.padding(top = 16.dp),
                    onValueChange = {
                    },
                    imeAction = ImeAction.Next
                ) {
                }
                if (order.neededAmount != "0")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.mdi_money_100),
                            contentDescription = ""
                        )
                        Text(
                            text = "تحصيل رسوم امر التشغيل مبلغ ( ${order.neededAmount} جنيه )",
                            color = SpanishGreen,
                        )
                        Image(
                            modifier = Modifier.padding(start = 8.dp),
                            painter = painterResource(id = R.drawable.vector),
                            contentDescription = ""
                        )
                    }

                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = "بيانات الزيارة",
                    color = ChathamsBlue, style = MaterialTheme.typography.subtitle1
                )
                InputField(
                    "تاريخ الزيارة",
                    KeyboardType.Text,
                    order.startDate.split(" ")[0],
                    isEnabled = false,
                    modifier = Modifier.padding(top = 16.dp),
                    onValueChange = {
                    },
                    imeAction = ImeAction.Next
                ) {
                }
                InputField(
                    "وقت الزيارة",
                    KeyboardType.Text,
                    DateUtils.formatTime(
                        order.startDate.split(" ")[1],
                        "HH:mm:ss",
                        Locale.US,
                        "hh:mm aa",
                        Locale.getDefault()
                    ),
                    isEnabled = false,
                    modifier = Modifier.padding(top = 16.dp),
                    onValueChange = {
                    },
                    imeAction = ImeAction.Next
                ) {
                }
                if (order.inCompany == 0)
                    InputField(
                        "المحافظة",
                        KeyboardType.Text,
                        order.governorate?.name ?: "",
                        isEnabled = false,
                        modifier = Modifier.padding(top = 16.dp),
                        onValueChange = {
                        },
                        imeAction = ImeAction.Next
                    ) {
                    }
                InputField(
                    "العنوان",
                    KeyboardType.Text,
                    if (order.inCompany == 0) order.address else "شركة انفينيتي",
                    isEnabled = false,
                    modifier = Modifier.padding(top = 16.dp),
                    onValueChange = {
                    },
                    imeAction = ImeAction.Next
                ) {
                }

                AppButton(
                    modifier = Modifier.padding(vertical = 34.dp),
                    text = if (order.finishedCars == 0) R.string.start else R.string.next,
                    isEnabled = orderDate.before(Date()),
                ) {
                    viewModel.startOrder()
                    goOrderInfo()
                    // TODO WHEN FINISH ORDER
                }
            }
        }


    }
}

