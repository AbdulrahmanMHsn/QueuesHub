package com.queueshub.ui.car

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.sharp.Close
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
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
import com.queueshub.ui.CameraPreview
import com.queueshub.ui.MainActivity
import com.queueshub.ui.device.DeviceCardContent
import com.queueshub.ui.main.AppButton
import com.queueshub.ui.main.AppDropdownMenu
import com.queueshub.ui.main.DialogBoxLoading
import com.queueshub.ui.main.InputField
import com.queueshub.ui.navigation.Router
import com.queueshub.ui.theme.DarkRed
import com.queueshub.ui.theme.SpanishGreen
import com.queueshub.utils.CameraType
import com.queueshub.utils.toBitmap
import java.io.File

@Composable
fun ManualPlateScreen(
    paddingValues: PaddingValues = PaddingValues(),
    scaffoldState: ScaffoldState? = null, router: Router? = null
) {
    val goInfoConfirmation: () -> Unit = {
        router?.goInfoConfirmation()
    }
    val goManualLicense: () -> Unit = {
        router?.goManualLicense()
    }
    val goDeviceEntry: () -> Unit = {
        router?.goDeviceEntry()
    }
    val context = LocalContext.current

    val viewModel: CarViewModel = hiltViewModel()
    val modelsState by viewModel.state.collectAsState()
    val sharedViewModel: AppViewModel = hiltViewModel(context as MainActivity)
    val vmLog: LogsViewModel = hiltViewModel()


    sharedViewModel.onUpdate.value
    var openImage by rememberSaveable { (mutableStateOf(false)) }
    var openedImage: Bitmap? by rememberSaveable { mutableStateOf(null) }
    var cameraType by rememberSaveable { (mutableStateOf(CameraType.SHASIS)) }
    var showLoading by rememberSaveable { (mutableStateOf(false)) }
    var showTryAgain by rememberSaveable { (mutableStateOf(false)) }
    var openCamera by rememberSaveable { (mutableStateOf(false)) }
    var chasisImage: File? by rememberSaveable { (mutableStateOf(sharedViewModel.chasisImage)) }
    var isWorking: String by rememberSaveable { mutableStateOf("working") }
    var selectedOptionText by rememberSaveable { mutableStateOf(sharedViewModel.carModel) }
    val isNextAvailable = sharedViewModel.shaseh != ""
    LaunchedEffect(key1 = 0) {
        viewModel.fetchModels()
    }

    BackHandler(openCamera || openImage) {
        openCamera = false
        openImage = false
    }
    if (openCamera) {
        CameraPreview(cameraType = cameraType, onImageCaptured = {
            showLoading = true
        }, onTryAgain = {
            showLoading = false
            showTryAgain = true
        }) { string, image ->
            showLoading = false
            openCamera = false
            if (cameraType == CameraType.SHASIS) {
                chasisImage = image
                sharedViewModel.chasisImage = image
            }
        }
        if (showLoading) {

            Box(
                contentAlignment = Alignment.Center
            ) {
                DialogBoxLoading()
            }
        }
        if (showTryAgain) Toast.makeText(context, "قم بتصوير صورة اكثر وضوح", Toast.LENGTH_SHORT)
            .show()
    } else if (openImage && openedImage != null) {
        Box {

            Image(
                modifier = Modifier.fillMaxSize(),
                bitmap = openedImage!!.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
            )
            IconButton(modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp),
                onClick = {
                    openImage = false

                },
                content = {
                    Icon(
                        imageVector = Icons.Sharp.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier
                            .size(100.dp)
                            .padding(1.dp)
                            .border(1.dp, Color.White, CircleShape)
                    )
                })
        }
    } else
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            val (title, subtitle, notWorking, working, carType, chasis, model, next) = createRefs()
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
                sharedViewModel.updateUI()
            }

            var shasehText by rememberSaveable { mutableStateOf(sharedViewModel.shaseh) }
            InputField(modifier = Modifier
                .fillMaxWidth()
                .constrainAs(model) {
                    top.linkTo(carType.bottom, margin = 16.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .padding(horizontal = 24.dp),
                text = stringResource(id = R.string.model_id),
                keyboardType = KeyboardType.Number,
                content = shasehText,
                onValueChange = {
                    sharedViewModel.licenseAuto = false
                    sharedViewModel.shaseh = it
                    shasehText = it
                    sharedViewModel.updateUI()
                },
                imeAction = ImeAction.Go
            ) {
                if (isNextAvailable) {
                    goDeviceEntry()
                }
            }
            DeviceCardContent(
                Modifier
                    .constrainAs(chasis) {
                        top.linkTo(model.bottom, margin = 30.dp)
                    }
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                scaffoldState,
                R.string.shaseh_image,
                deviceImage = chasisImage?.toBitmap(),
                openImage = {
                    openImage = true
                    openedImage = it
                },
                imageUploaded = {
                    cameraType = CameraType.SHASIS
                    openCamera = true
                })
            AppButton(modifier = Modifier
                .constrainAs(next) {
                    top.linkTo(chasis.bottom, margin = 24.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .padding(bottom = 34.dp), text = R.string.next, isEnabled = isNextAvailable) {

                val chassis = sharedViewModel.shaseh
                val carModel = sharedViewModel.carModel

                val description = "تم تصوير الشاسيه رقم :  " + chassis + " ماركه:  " + carModel
                val carDetails = ApiLogItem(
                    sharedViewModel.plateNum,
                    description = description,
                    type = "chasiss",
                    sharedViewModel.selectedOrder?.id?.toInt(),
                )

                val logArray = ArrayList<ApiLogItem>()
                logArray.add(carDetails)
                val logModel = ApiLog(logArray)
                vmLog.addLogs(logModel)
                goDeviceEntry()
            }
        }
}

@Preview(locale = "ar")
@Composable
fun ManualPlateScreenPreview() {
    ManualPlateScreen()
}