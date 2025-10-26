package com.queueshub.ui.car

import android.util.Log
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
import com.queueshub.ui.main.AppButton
import com.queueshub.ui.main.AppDropdownMenu
import com.queueshub.ui.main.InputField
import com.queueshub.ui.navigation.Router
import com.queueshub.ui.theme.DarkRed
import com.queueshub.ui.theme.SpanishGreen
import com.queueshub.utils.Logger

@Composable
fun ConfirmInfoScreen(paddingValues: PaddingValues = PaddingValues(), router: Router? = null) {

    val context = LocalContext.current
    val viewModel: CarViewModel = hiltViewModel()
    val vmLog: LogsViewModel = hiltViewModel()
    val modelsState by viewModel.state.collectAsState()
    val sharedViewModel: AppViewModel = hiltViewModel(context as MainActivity)

    val goDeviceEntry: () -> Unit = {
        router?.goDeviceEntry()
    }

    LaunchedEffect(key1 = 0) {
        viewModel.fetchModels()
        if (sharedViewModel.plateInfoAuto) {

            sharedViewModel.extractDataFromPlate()
        }
        if (sharedViewModel.licenseAuto) {
            // sharedViewModel.extractDataFromLicense()
            sharedViewModel.finalExtractDataFromCarLicense()
        }

    }
    sharedViewModel.onUpdate.value
    val isNextAvailable = sharedViewModel.plateNum != ""
    var isWorking: String by rememberSaveable { mutableStateOf("working") }
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        val (title, subtitle, notWorking, working, carType, model, color, plate, model_num, motor, next) = createRefs()
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
        val options = modelsState.models ?: listOf()
        var selectedOptionText by rememberSaveable { mutableStateOf(sharedViewModel.carModel) }
        AppDropdownMenu(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(carType) {
                    top.linkTo(working.bottom, margin = 4.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .padding(horizontal = 24.dp, vertical = 8.dp),
            label = R.string.car_type,
            value = selectedOptionText,
            options = options) {
            sharedViewModel.licenseAuto = false
            sharedViewModel.carModel = it
            selectedOptionText = it
        }

        Row(
            modifier = Modifier
                .constrainAs(model) {
                    top.linkTo(carType.bottom, margin = 16.dp)
                    start.linkTo(parent.start)
                    end.linkTo(color.start)
                }
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)) {

            var modelText by rememberSaveable { mutableStateOf(sharedViewModel.year) }
            var colorText by rememberSaveable { mutableStateOf(sharedViewModel.color) }

            Logger.d("ConfirmInfoScreen modelText $modelText")
            Logger.d("ConfirmInfoScreen colorText $colorText")
            InputField(
                modifier = Modifier.weight(1f),
                text = stringResource(id = R.string.model_name),
                keyboardType = KeyboardType.Text,
                content = sharedViewModel.year,
                onValueChange = {
                    modelText = it
                },
                imeAction = ImeAction.Next,
                isEnabled = false
            )
            InputField(
                modifier = Modifier.weight(1f),
                text = stringResource(id = R.string.color),
                keyboardType = KeyboardType.Text,
                content = sharedViewModel.color,
                onValueChange = {
                    colorText = it
                },
                imeAction = ImeAction.Next,
                isEnabled = false
            )
        }
        var plateText = sharedViewModel.plateNum
        Log.i("TAGTAGTAG", "ConfirmInfoScreen: ${sharedViewModel.plateNum}")

        var modelText = sharedViewModel.shaseh
        var motorText by rememberSaveable { mutableStateOf(sharedViewModel.motor) }

        Logger.d("ConfirmInfoScreen2 sharedViewModel.shaseh ${sharedViewModel.shaseh}")
        Logger.d("ConfirmInfoScreen2 sharedViewModel.motor ${sharedViewModel.motor}")
        Logger.d("ConfirmInfoScreen2 sharedViewModel.carModel ${sharedViewModel.carModel}")
        Logger.d("ConfirmInfoScreen2 sharedViewModel.color ${sharedViewModel.color}")
        Logger.d("ConfirmInfoScreen2 sharedViewModel.year ${sharedViewModel.year}")
        Logger.d("ConfirmInfoScreen2 modelText $modelText")
        Logger.d("ConfirmInfoScreen2 motorText $motorText")
        InputField(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(plate) {
                    top.linkTo(model.bottom, margin = 16.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .padding(horizontal = 24.dp, vertical = 8.dp),
            text = stringResource(id = R.string.plate_num),
            content = plateText,
            onValueChange = {
                sharedViewModel.plateInfoAuto = false
                sharedViewModel.plateNum = it
                sharedViewModel.updateUI()
            },
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next,
            isEnabled = true
        )

        InputField(modifier = Modifier
            .fillMaxWidth()
            .constrainAs(model_num) {
                top.linkTo(plate.bottom, margin = 16.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
            .padding(horizontal = 24.dp, vertical = 8.dp),
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

        InputField(modifier = Modifier
            .fillMaxWidth()
            .constrainAs(motor) {
                top.linkTo(model_num.bottom, margin = 16.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
            .padding(horizontal = 24.dp, vertical = 8.dp),
            text = stringResource(id = R.string.motor_id),
            content = sharedViewModel.motor,
            onValueChange = {
                // motorText = it
            },
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Go,
            isEnabled = false
        ) {}

        AppButton(modifier = Modifier
            .constrainAs(next) {
                top.linkTo(motor.bottom, margin = 24.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
            .padding(bottom = 34.dp), isEnabled = isNextAvailable, text = R.string.next) {
            val chassis = sharedViewModel.shaseh
            val motor = sharedViewModel.motor
            val plateNum = sharedViewModel.plateNum
            val carColor = sharedViewModel.color
            val carModel = sharedViewModel.carModel
            val carYear = sharedViewModel.year
            val carStatus = if (sharedViewModel.carStatus == "working") "تعمل" else "لاتعمل"

            val description =
                "تم إضافه بيانات العربيه (" + " ماركه :" + carModel + ", موديل : " + carYear + ", اللون :" + carColor + ", اللوحه : " + plateNum + ", الشاسيه : " + chassis + ", الموتور : " + motor + ", الحاله : " + carStatus + ")"
            val carDetails = ApiLogItem(
                plateNum,
                description = description,
                type = "car_details",
                sharedViewModel.selectedOrder?.id?.toInt(),
                generatedId = sharedViewModel.generatedId,
            )

            val logArray = ArrayList<ApiLogItem>()
            logArray.add(carDetails)
            val logModel = ApiLog(logArray)
            vmLog.addLogs(logModel)
            goDeviceEntry()
        }
    }
}

@Preview(locale = "ar", showBackground = true)
@Composable
fun ConfirmInfoScreenPreview() {
    // Mock data for preview
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "بيانات العربية",
            style = MaterialTheme.typography.subtitle1
        )
        Text(
            text = "حالة العربية",
            style = MaterialTheme.typography.subtitle2
        )

        // Mock dropdown
        AppDropdownMenu(
            modifier = Modifier.fillMaxWidth(),
            value = "تويوتا",
            label = R.string.car_type,
            options = listOf("تويوتا", "هوندا", "نيسان")
        ) {}

        // Mock input fields
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InputField(
                modifier = Modifier.weight(1f),
                text = "الموديل",
                content = "2023",
                onValueChange = {},
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
                isEnabled = false
            ) {}

            InputField(
                modifier = Modifier.weight(1f),
                text = "اللون",
                content = "أبيض",
                onValueChange = {},
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
                isEnabled = false
            ) {}
        }

        InputField(
            modifier = Modifier.fillMaxWidth(),
            text = "رقم اللوحة",
            content = "ABC123",
            onValueChange = {},
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next,
            isEnabled = true
        ) {}

        InputField(
            modifier = Modifier.fillMaxWidth(),
            text = "رقم الشاسيه",
            content = "CHASSIS123",
            onValueChange = {},
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Go
        ) {}

        InputField(
            modifier = Modifier.fillMaxWidth(),
            text = "رقم الموتور",
            content = "MOTOR123",
            onValueChange = {},
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Go,
            isEnabled = false
        ) {}
    }
}