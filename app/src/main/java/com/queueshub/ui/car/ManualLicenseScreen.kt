
package com.queueshub.ui.car

import android.widget.Toast
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import com.queueshub.R
import com.queueshub.data.api.model.ApiLog
import com.queueshub.data.api.model.ApiLogItem
import com.queueshub.ui.AppViewModel
import com.queueshub.ui.MainActivity
import com.queueshub.ui.device.DeviceViewModel
import com.queueshub.ui.main.AppButton
import com.queueshub.ui.main.AppDropdownMenu
import com.queueshub.ui.main.DialogBoxLoading
import com.queueshub.ui.main.InputField
import com.queueshub.ui.navigation.Router
import com.queueshub.ui.theme.DarkRed
import com.queueshub.ui.theme.SpanishGreen

@Composable
fun ManualLicenseScreen(paddingValues: PaddingValues = PaddingValues(), router: Router? = null) {

    var isWorking: String by rememberSaveable { mutableStateOf("working") }

    val context = LocalContext.current
    val viewModel: CarViewModel = hiltViewModel()
    val modelsState by viewModel.state.collectAsState()
    val sharedViewModel: AppViewModel = hiltViewModel(context as MainActivity)
    val vmLog: LogsViewModel = hiltViewModel()

    val goDeviceEntry: () -> Unit = {
        router?.goDeviceEntry()
    }




    LaunchedEffect(key1 = 0) {
        viewModel.fetchModels()
        if (sharedViewModel.plateInfoAuto) {
            sharedViewModel.extractDataFromPlate()
        }
    }
    val isNextAvailable = sharedViewModel.plateNum != ""
    sharedViewModel.onUpdate.value
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        val (title, subtitle, notWorking, working, carType, plate, model, next) = createRefs()
        Text(
            text = "بيانات العربية", modifier = Modifier
                .padding(top = 56.dp)
                .constrainAs(title) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }, style = MaterialTheme.typography.subtitle1
        )
        Text(
            modifier = Modifier.constrainAs(subtitle) {
                top.linkTo(title.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }, text = "حالة العربية", style = MaterialTheme.typography.subtitle2
        )

        isAvailableGroup(
            modifier = Modifier
                .constrainAs(notWorking) {
                    top.linkTo(subtitle.bottom, margin = 16.dp)
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
            sharedViewModel.carStatus = isWorking
        }

        isAvailableGroup(
            modifier = Modifier
                .constrainAs(working) {
                    top.linkTo(subtitle.bottom, margin = 16.dp)
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
            sharedViewModel.carStatus = isWorking
        }

        val options = modelsState.models?: listOf()
        var selectedOptionText by rememberSaveable { mutableStateOf(sharedViewModel.carModel) }
        AppDropdownMenu(modifier = Modifier
            .fillMaxWidth()
            .constrainAs(carType) {
                top.linkTo(working.bottom, margin = 4.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
            .padding(horizontal = 24.dp, vertical = 16.dp),
            value = selectedOptionText,
            label = R.string.car_type,
            options = options) {
            sharedViewModel.licenseAuto = false
            sharedViewModel.carModel = it
            selectedOptionText = it
        }

        var plateText = sharedViewModel.plateNum
        var modelText = sharedViewModel.shaseh
        InputField(modifier = Modifier
            .fillMaxWidth()
            .constrainAs(plate) {
                top.linkTo(carType.bottom, margin = 16.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
            .padding(horizontal = 24.dp, vertical = 16.dp),
            text = stringResource(id = R.string.plate_num),
            content = plateText,
            onValueChange = {
                sharedViewModel.plateInfoAuto = false
                sharedViewModel.plateNum = it
                sharedViewModel.updateUI()
            },
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Go, isEnabled = true
        ) {
        }

        InputField(modifier = Modifier
            .fillMaxWidth()
            .constrainAs(model) {
                top.linkTo(plate.bottom, margin = 16.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
            .padding(horizontal = 24.dp, vertical = 16.dp),
            text = stringResource(id = R.string.model_id_not),
            content = modelText,
            onValueChange = {
                sharedViewModel.licenseAuto = false
                sharedViewModel.shaseh = it
                sharedViewModel.updateUI()
            },
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Go
        ) {
            goDeviceEntry()
        }

        AppButton(modifier = Modifier
            .constrainAs(next) {
                top.linkTo(model.bottom, margin = 24.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
            .padding(bottom = 34.dp), isEnabled = isNextAvailable, text = R.string.next) {


            val chassis = sharedViewModel.shaseh

            val description = "تم تصوير الشاسيه رقم :  " + chassis
            val carDetails = ApiLogItem(
                sharedViewModel.plateNum,
                description = description,
                type = "chasiss",
                sharedViewModel.selectedOrder?.id?.toInt(),
                generatedId =sharedViewModel.generatedId,
            )

            val logArray = ArrayList<ApiLogItem>()
            logArray.add(carDetails)
            val logModel = ApiLog(logArray)
            vmLog.addLogs(logModel)
            goDeviceEntry()
            // ////////////////////////////// Add Logs //////////////////////////////////
        }
    }
}

@Preview(locale = "ar")
@Composable
fun ManualLicenseScreenPreview() {
    ManualLicenseScreen()
}
