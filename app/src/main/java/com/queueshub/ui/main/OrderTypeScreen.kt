package com.queueshub.ui.main

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.queueshub.R
import com.queueshub.data.api.model.ApiLog
import com.queueshub.data.api.model.ApiLogItem
import com.queueshub.ui.AppViewModel
import com.queueshub.ui.MainActivity
import com.queueshub.ui.car.LogsViewModel
import com.queueshub.ui.navigation.Router
import com.queueshub.ui.theme.LightGrey
import com.queueshub.ui.theme.Teal400
import com.queueshub.ui.theme.Tundora
import com.queueshub.utils.OrderType
import com.queueshub.utils.OrderType.*

@Composable
fun OrderTypeScreen(
    paddingValues: PaddingValues = PaddingValues(),
    router: Router? = null,
) {

    val context = LocalContext.current

    val sharedViewModel: AppViewModel = hiltViewModel(context as MainActivity)
    sharedViewModel.onUpdate.value
    val vmLog: LogsViewModel = hiltViewModel()

    val goRemoval: () -> Unit = {
        router?.goDeviceRemove()
    }
    val goRepair: () -> Unit = {
        router?.goDeviceRepair()
    }
    val goReplace: () -> Unit = {
        router?.goDeviceReplace()
    }
    val goNote: () -> Unit = {
        router?.goOrderNote()
    }

    val uiState by sharedViewModel.state.collectAsState()
    if (uiState.loading) {
        Box(
            contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()
        ) {
            DialogBoxLoading()
        }
    }
    uiState.failure?.let {
        val content = it.getContentIfNotHandled()
        content?.let {
            Toast.makeText(
                context, it.localizedMessage, Toast.LENGTH_SHORT
            ).show()
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "غرض الأمر",
                modifier = Modifier.padding(top = 56.dp),
                style = MaterialTheme.typography.subtitle1
            )
            Text(text = "اختر امر التشغيل", style = MaterialTheme.typography.subtitle2)
            MultiToggleButton(sharedViewModel) {
                sharedViewModel.orderType = it
            }
        }
        AppButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 34.dp),
            text = R.string.next,
            isEnabled = sharedViewModel.orderType != UNDEFINED
        ) {

            var description =  ""

            when (sharedViewModel.orderType) {
                REMOVE_DEVICE -> description = "تم اختيار غرض فك الجهار"
                REPAIR_DEVICE -> description = "تم اختيار غرض صيانه "
                REPLACE_DEVICE -> description = "تم اختيار غرض استبدال ونقل "
                else -> {
                    description = "تم اختيار غرض تركيب جهاز جديد "
                }
            }

            val orderType = ApiLogItem(
                sharedViewModel.plateNum,
                description = description,
                type = "type",
                sharedViewModel.selectedOrder?.id?.toInt(),
                generatedId = sharedViewModel.generatedId,
            )

            val logArray = ArrayList<ApiLogItem>()
            logArray.add(orderType)
            val logModel = ApiLog(logArray)
            vmLog.addLogs(logModel)


            when (sharedViewModel.orderType) {
                REMOVE_DEVICE -> goRemoval()
                REPAIR_DEVICE -> goRepair()
                REPLACE_DEVICE -> goReplace()
                else -> {
                    sharedViewModel.saveOrderType(arrayListOf("تركيب"))
                    goNote()
                }
            }
        }
    }
}

@Composable
fun MultiToggleButton(
    viewModel: AppViewModel,
    onToggleChange: (OrderType) -> Unit
) {
    var currentSelection: OrderType = viewModel.orderType
    val selectedTint = Teal400
    val unselectedTint = Tundora
    val unselectedBorderTint = LightGrey
    Column(
        modifier = Modifier
            .padding(top = 35.dp)
            .padding(horizontal = 16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            val isNewDevice = currentSelection == NEW_DEVICE
            val isReplace = currentSelection == REPLACE_DEVICE
            OrderTypeWidget(
                modifier = Modifier.weight(1f),
                isNewDevice,
                NEW_DEVICE.value,
                R.drawable.new_device_selected,
                R.drawable.new_device,
                stringResource(id = R.string.new_device)
            ) { selection ->
                viewModel.orderType = OrderType.values().find { it.value == selection }!!
                viewModel.updateUI()
//                onToggleChange(currentSelection)
            }

            OrderTypeWidget(
                modifier = Modifier.weight(1f),
                isReplace,
                REPLACE_DEVICE.value,
                R.drawable.replace_device_selected,
                R.drawable.order_replacement,
                stringResource(id = R.string.replace_device)
            ) { selection ->
                viewModel.orderType = OrderType.values().find { it.value == selection }!!
                viewModel.updateUI()
//                onToggleChange(currentSelection)
            }

        }

        Spacer(
            modifier = Modifier.height(height = 16.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {

            val isRepair = currentSelection == REPAIR_DEVICE
            val isRemove = currentSelection == REMOVE_DEVICE
            OrderTypeWidget(
                modifier = Modifier.weight(1f),
                isRemove,
                REMOVE_DEVICE.value,
                R.drawable.remove_device_selected,
                R.drawable.remove_device,
                stringResource(id = R.string.remove_device)
            ) { selection ->
                viewModel.orderType = OrderType.values().find { it.value == selection }!!
                viewModel.updateUI()
//                onToggleChange(currentSelection)
            }

            OrderTypeWidget(
                modifier = Modifier.weight(1f),
                isRepair,
                REPAIR_DEVICE.value,
                R.drawable.repair_device_selected,
                R.drawable.repair_device,
                stringResource(id = R.string.repair_device)
            ) { selection ->
                viewModel.orderType = OrderType.values().find { it.value == selection }!!
                viewModel.updateUI()
//                onToggleChange(currentSelection)
            }

        }
    }
}

@Composable
fun OrderTypeWidget(
    modifier: Modifier,
    isSelected: Boolean,
    type: String,
    selectedImage: Any,
    unselectedImage: Any,
    text: String,
    onToggleChange: (String) -> Unit
) {
    val selectedTint = Teal400
    val unselectedBorderTint = LightGrey
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .toggleable(value = isSelected, enabled = true, onValueChange = { selected ->
                onToggleChange(type)

            })
            .border(
                BorderStroke(
                    1.dp, if (isSelected) selectedTint else unselectedBorderTint
                ), RoundedCornerShape(4.dp)
            )
            .background(Color.White)
    ) {
        val image: Painter =
            if (selectedImage is Int) painterResource(id = (if (isSelected) selectedImage else unselectedImage) as Int)
            else rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current)
                    .data(data = if (isSelected) selectedImage else unselectedImage)
                    .decoderFactory(SvgDecoder.Factory())
                    .apply(block = fun ImageRequest.Builder.() {
                        crossfade(true)
                    }).placeholder(R.drawable.ic_launcher_background).build()
            )
        Image(
            painter = image,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.align(Alignment.Center),
        )
        Text(
            style = MaterialTheme.typography.subtitle2,
            color = if (isSelected) Teal400 else Tundora,
            modifier = Modifier.align(Alignment.BottomCenter),
            text = text,
            maxLines = 1
        )

    }
}

@Preview(locale = "ar")
@Composable
fun OrderTypeScreenPreview() {
    OrderTypeScreen()
}