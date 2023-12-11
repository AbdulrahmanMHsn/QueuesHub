package com.queueshub.ui.sim

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.text.isDigitsOnly
import androidx.hilt.navigation.compose.hiltViewModel
import com.queueshub.R
import com.queueshub.data.api.model.ApiLog
import com.queueshub.data.api.model.ApiLogItem
import com.queueshub.ui.AppViewModel
import com.queueshub.ui.MainActivity
import com.queueshub.ui.car.LogsViewModel
import com.queueshub.ui.car.isAvailableGroup
import com.queueshub.ui.main.AppButton
import com.queueshub.ui.main.AppDropdownMenu
import com.queueshub.ui.main.InputField
import com.queueshub.ui.navigation.Router
import com.queueshub.ui.theme.DarkRed
import com.queueshub.ui.theme.SpanishGreen
import kotlinx.coroutines.delay

@Composable
fun SimDetailsScreen(
    paddingValues: PaddingValues = PaddingValues(),
    router: Router? = null
) {

    val goOrderType: () -> Unit = {
        router?.goOrderType()
    }
    val context = LocalContext.current
    val viewModel: AppViewModel = hiltViewModel(context as MainActivity)
    val vmLog: LogsViewModel = hiltViewModel()
    var TawreedSim: Int by rememberSaveable { mutableStateOf(0) }
    viewModel.processSimImage()
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        val (title, tarkeebSimCons, tawreedSimCons, provider, gsm, sn, next) = createRefs()
        Text(
            text = "بيانات الشريحة", modifier = Modifier
                .padding(top = 56.dp)
                .constrainAs(title) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }, style = MaterialTheme.typography.subtitle1
        )

        val isNextAvailable = viewModel.simSerial != "" && (viewModel.simGSM.isEmpty() ||viewModel.simGSM.length == 11)
        var snText = viewModel.simSerial
        var gsmText = viewModel.simGSM
        viewModel.onUpdate.value

        isAvailableGroup(
            modifier = Modifier
                .constrainAs(tarkeebSimCons) {
                    top.linkTo(title.bottom, margin = 16.dp)
                    end.linkTo(parent.end, margin = 16.dp)
                    start.linkTo(tawreedSimCons.end, margin = 12.dp)
                }
                .padding(horizontal = 12.dp),
            TawreedSim == 0,
            false,
            DarkRed,
            Icons.Default.Clear,
            R.string.tarkeeb,
        ) {
            TawreedSim = 0
            viewModel.isSimSupplied = 0
        }

        isAvailableGroup(
            modifier = Modifier
                .constrainAs(tawreedSimCons) {
                    top.linkTo(title.bottom, margin = 16.dp)
                    start.linkTo(parent.start, margin = 16.dp)
                    end.linkTo(tarkeebSimCons.start, margin = 12.dp)
                }
                .padding(horizontal = 12.dp),
            TawreedSim == 1,
            true,
            SpanishGreen,
            Icons.Default.Check,
            R.string.tawreed,
        ) {
            TawreedSim = 1
            viewModel.isSimSupplied = 1
        }

        var selectedOptionText =
            if (snText.startsWith("89200220")) "فودافون" else if (snText.startsWith("89200305")) "اتصالات" else if (snText.startsWith(
                    "8920018"
                )
            ) "اورانح" else "فودافون"
        AppDropdownMenu(modifier = Modifier
            .fillMaxWidth()
            .constrainAs(provider) {
                top.linkTo(tarkeebSimCons.bottom, margin = 4.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
            .padding(horizontal = 24.dp, vertical = 16.dp),
            value = selectedOptionText,
            label = R.string.sim_provider,
            options = listOf("فودافون", "اتصالات", "اورانج", "وي")) {
            selectedOptionText = it
        }

        InputField(modifier = Modifier
            .fillMaxWidth()
            .constrainAs(sn) {
                top.linkTo(provider.bottom, margin = 16.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
            .padding(horizontal = 24.dp, vertical = 8.dp),
            text = stringResource(id = R.string.sn),
            keyboardType = KeyboardType.Number,
            content = snText,
            onValueChange = {
                viewModel.simSerial = it
                viewModel.updateUI()
            },
            imeAction = ImeAction.Next
        )

        InputField(modifier = Modifier
            .fillMaxWidth()
            .constrainAs(gsm) {
                top.linkTo(sn.bottom, margin = 16.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
            .padding(horizontal = 24.dp, vertical = 8.dp),
            text = stringResource(id = R.string.gsm),
            keyboardType = KeyboardType.Number,
            content = gsmText,
            onValueChange = {
                if (it.isDigitsOnly() && it.length <= 11) {
                    viewModel.simGSM = it
                    viewModel.updateUI()
                }
            },
            imeAction = ImeAction.Done
        )


        AppButton(modifier = Modifier
            .constrainAs(next) {
                top.linkTo(gsm.bottom, margin = 24.dp, goneMargin = 24.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
            .padding(bottom = 34.dp), isEnabled = isNextAvailable, text = R.string.next) {

            val serialLog = ApiLogItem(
                viewModel.plateNum,
                description = "رقم مسلسل الشريحه:  " +  viewModel.simSerial,
                type = "sim_serial",
                viewModel.selectedOrder?.id?.toInt(),
                generatedId =viewModel.generatedId,
            )

            val simGsmLog = ApiLogItem(
                viewModel.plateNum,
                description = "رقم الشريحه gsm :  " + viewModel.simGSM,
                type = "sim_gsm",
                viewModel.selectedOrder?.id?.toInt(),
                generatedId = viewModel.generatedId,
            )


            val logArray = ArrayList<ApiLogItem>()
            logArray.add(serialLog)
            logArray.add(simGsmLog)
            val logModel = ApiLog(logArray)
            vmLog.addLogs(logModel)

            goOrderType()
        }
    }
}

@Preview(locale = "ar")
@Composable
fun SimDetailsScreenPreview() {
    SimDetailsScreen()
}