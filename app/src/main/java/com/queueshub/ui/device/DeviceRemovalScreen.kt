package com.queueshub.ui.device

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import com.queueshub.R
import com.queueshub.data.api.model.ApiLog
import com.queueshub.data.api.model.ApiLogItem
import com.queueshub.ui.AppViewModel
import com.queueshub.ui.MainActivity
import com.queueshub.ui.car.LogsViewModel
import com.queueshub.ui.car.isAvailableGroup
import com.queueshub.ui.main.AppButton
import com.queueshub.ui.main.DialogBoxLoading
import com.queueshub.ui.main.OrderTypeWidget
import com.queueshub.ui.navigation.Router
import com.queueshub.ui.theme.*
import com.queueshub.utils.*

@Composable
fun DeviceRemovalScreen(
    paddingValues: PaddingValues = PaddingValues(),
    router: Router? = null,
) {
    val context = LocalContext.current
    val viewModel: AppViewModel = hiltViewModel(context as MainActivity)
    val vmLog: LogsViewModel = hiltViewModel()
    viewModel.onUpdate.value

    val goNote: () -> Unit = {
        router?.goOrderNote()
    }
    var isWorking: String by rememberSaveable { mutableStateOf("working") }

    val uiState by viewModel.state.collectAsState()

    if (uiState.loading) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            DialogBoxLoading()
        }
    }
    uiState.failure?.let {
        val content = it.getContentIfNotHandled()
        content?.let {
            Toast.makeText(
                context,
                it.localizedMessage,
                Toast.LENGTH_SHORT,
            ).show()
        }
    }
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        val (title, notWorking, working, subtitle, owner, next) = createRefs()
        Text(
            text = "حالة الجهاز",
            modifier = Modifier
                .padding(top = 56.dp)
                .constrainAs(title) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            style = MaterialTheme.typography.subtitle1,
        )

        isAvailableGroup(
            modifier = Modifier
                .constrainAs(notWorking) {
                    top.linkTo(title.bottom, margin = 16.dp)
                    end.linkTo(parent.end, margin = 16.dp)
                    start.linkTo(working.end, margin = 12.dp)
                }
                .padding(horizontal = 12.dp),
            isWorking == "not_working",
            false,
            DarkRed,
            Icons.Default.Clear,
            R.string.not_working,
        ) {
            isWorking = "not_working"

            viewModel.removeDeviceStatus = isWorking
//            viewModel.updateUI()
        }

        isAvailableGroup(
            modifier = Modifier
                .constrainAs(working) {
                    top.linkTo(title.bottom, margin = 16.dp)
                    start.linkTo(parent.start, margin = 16.dp)
                    end.linkTo(notWorking.start, margin = 12.dp)
                }
                .padding(horizontal = 12.dp),
            isWorking == "working",
            true,
            SpanishGreen,
            Icons.Default.Check,
            R.string.working,
        ) {
            isWorking = "working"
            viewModel.removeDeviceStatus = isWorking
            viewModel.updateUI()
        }
        Text(
            modifier = Modifier
                .constrainAs(subtitle) {
                    top.linkTo(working.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .padding(vertical = 19.dp, horizontal = 16.dp),
            text = "الجهاز مع",
            style = MaterialTheme.typography.subtitle1,
        )
        MultiToggleButton(
            modifier = Modifier
                .constrainAs(owner) {
                    top.linkTo(subtitle.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .padding(vertical = 19.dp, horizontal = 16.dp),
            viewModel = viewModel,
            onToggleChange = {
                viewModel.removeDeviceWith = it
                viewModel.updateUI()
            },
        )

        AppButton(
            modifier = Modifier.constrainAs(next) {
                bottom.linkTo(parent.bottom, margin = 24.dp, goneMargin = 24.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            },
            text = R.string.next,
        ) {
            var description = "(الاختيار داخل الغرض فك الجهاز) :"

            val deviceStatus = if (viewModel.removeDeviceStatus == "working") "تعمل" else "لاتعمل"

            description = description + " حاله الجهاز: " + deviceStatus + " الجهاز مع : " + viewModel.removeDeviceWith

            val orderType = ApiLogItem(
                viewModel.plateNum,
                description = description,
                type = "inside_type",
                viewModel.selectedOrder?.id?.toInt(),
                generatedId =viewModel.generatedId,
            )

            val logArray = ArrayList<ApiLogItem>()
            logArray.add(orderType)
            val logModel = ApiLog(logArray)
            vmLog.addLogs(logModel)

            viewModel.saveOrderType(arrayListOf("فك"))
            goNote()
        }
    }
}

@Composable
fun MultiToggleButton(
    viewModel: AppViewModel,
    modifier: Modifier,
    onToggleChange: (String) -> Unit,
) {
    var currentSelection = viewModel.removeDeviceWith
    Column(
        modifier = modifier,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            val isWithClient = currentSelection == WITH_CLIENT
            val isWithInfinity = currentSelection == WITH_INFINITY
            OrderTypeWidget(
                modifier = Modifier.weight(1f),
                isWithClient,
                WITH_CLIENT,
                R.drawable.with_client_selected,
                R.drawable.with_client,
                stringResource(id = R.string.with_client),
            ) {
                currentSelection = it
                onToggleChange(it)
            }

            OrderTypeWidget(
                modifier = Modifier.weight(1f),
                isWithInfinity,
                WITH_INFINITY,
                R.drawable.infinity,
                R.drawable.infinity,
                stringResource(id = R.string.with_infinity),
            ) {
                currentSelection = it
                onToggleChange(it)
            }
        }
    }
}

@Preview(locale = "ar")
@Composable
fun DeviceRemovalScreenPreview() {
    DeviceRemovalScreen()
}
