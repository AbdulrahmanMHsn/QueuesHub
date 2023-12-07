package com.queueshub.ui.device

import android.Manifest
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.sharp.Close
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
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
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.queueshub.R
import com.queueshub.data.api.ApiConstants.BASE_URL
import com.queueshub.data.api.model.ApiLog
import com.queueshub.data.api.model.ApiLogItem
import com.queueshub.domain.model.Sensor
import com.queueshub.ui.AppViewModel
import com.queueshub.ui.CameraPreview
import com.queueshub.ui.MainActivity
import com.queueshub.ui.car.LogsViewModel
import com.queueshub.ui.car.isAvailableGroup
import com.queueshub.ui.car.showErrorSnackbar
import com.queueshub.ui.main.*
import com.queueshub.ui.navigation.Router
import com.queueshub.ui.theme.*
import com.queueshub.utils.CameraType
import com.queueshub.utils.toBitmap

@Composable
fun DeviceDetailsScreen(
    scaffoldState: ScaffoldState? = null, router: Router? = null
) {
    val goSimEntry: () -> Unit = {
        router?.goSimEntry()
    }
    val vmLog: LogsViewModel = hiltViewModel()

    var openCamera by remember { (mutableStateOf(false)) }
    var openImage by remember { (mutableStateOf(false)) }
    val cameraType = CameraType.SENSOR
    var openedImage: Bitmap? by remember { mutableStateOf(null) }
    val context = LocalContext.current
    val sharedViewModel: AppViewModel = hiltViewModel(context as MainActivity)
    val viewModel: DeviceViewModel = hiltViewModel()
    val sensorsState by viewModel.state.collectAsState()
    var showLoading by remember { (mutableStateOf(false)) }
    var selectedSensorForImage: Sensor? by remember { mutableStateOf(null) }
    LaunchedEffect(0) {

        viewModel.fetchSensors()
    }
    sharedViewModel.onUpdate.value
    val isNextAvailable = sharedViewModel.imei.length == 15
    var TawreedDevice: Int by remember { mutableStateOf(0) }

    BackHandler(openCamera||openImage) {
        openCamera = false
        openImage = false
    }
    if (openCamera) {
        Log.e("camera", "opening camera")
        CameraPreview(cameraType = cameraType, onImageCaptured = {
            showLoading = true
        }, onTryAgain = {}) { string, image ->
            showLoading = false
            openCamera = false
            val findSensor =
                sharedViewModel.selectedSensors.find { it.id == selectedSensorForImage!!.id }
            sharedViewModel.selectedSensors.remove(findSensor)
            selectedSensorForImage!!.file = image
            sharedViewModel.selectedSensors.add(selectedSensorForImage!!)
        }
        if (showLoading) {

            Box(
                contentAlignment = Alignment.Center
            ) {
                DialogBoxLoading()
            }
        }
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
    } else {
        Box {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                sensorsState.sensors?.let {
                    LazyVerticalGrid(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(), contentPadding = PaddingValues(16.dp),
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item(span = { GridItemSpan(2) }) {
                            SensorsHeader(modifier = Modifier)
                        }
                        item(span = { GridItemSpan(2) }) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                isAvailableGroup(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    TawreedDevice == 0,
                                    false,
                                    DarkRed,
                                    Icons.Default.Clear,
                                    R.string.tarkeeb,
                                ) {
                                    TawreedDevice = 0
                                    sharedViewModel.isSupplied = 0
                                }

                                isAvailableGroup(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    TawreedDevice == 1,
                                    true,
                                    SpanishGreen,
                                    Icons.Default.Check,
                                    R.string.tawreed,
                                ) {
                                    TawreedDevice = 1
                                    sharedViewModel.isSupplied = 1
                                }
                            }
                        }

                        item(span = { GridItemSpan(2) }) {
                            var typeText by rememberSaveable { mutableStateOf("") }
                            var snText = sharedViewModel.serial
                            var imeiText = sharedViewModel.imei
                            Column {

                                InputField(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 8.dp),
                                    text = stringResource(id = R.string.device_type),
                                    content = typeText,
                                    onValueChange = { typeText = it },
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next,
                                    isEnabled = false
                                )
                                InputField(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 8.dp),
                                    text = stringResource(id = R.string.sn),
                                    content = snText,
                                    onValueChange = {
                                        sharedViewModel.serial = it
                                        sharedViewModel.updateUI()
                                    },
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next,
                                    isEnabled = false
                                )

                                InputField(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 8.dp),
                                    text = stringResource(id = R.string.imei),
                                    keyboardType = KeyboardType.Number,
                                    content = imeiText,
                                    onValueChange = {
                                        if (it.length <= 15) {
                                            sharedViewModel.imei = it
                                            sharedViewModel.updateUI()
                                        }
                                    },
                                    imeAction = ImeAction.Done,
                                    isEnabled = true
                                )

                            }
                        }
                        sensorsState.sensors?.let { sensorsList ->

                            sharedViewModel.onUpdate.value

                            val currentSelections = sharedViewModel.selectedSensors
                            sensorsList.forEach { currentSensor ->
                                item(span = { GridItemSpan(if (currentSensor.needAttach) 2 else 1) }) {
                                    val selectedSensor = currentSelections.find { it.id == currentSensor.id }
                                    val sensorToDisplay = selectedSensor?: currentSensor
                                    val isSelected = selectedSensor != null
                                    SensorWidget(modifier = Modifier
                                        .fillMaxWidth(),
                                        scaffoldState,
                                        sharedViewModel,
                                        isSelected,
                                        BASE_URL + "storage/" + sensorToDisplay.path,
                                        BASE_URL + "storage/" + sensorToDisplay.path,
                                        sensorToDisplay,
                                        openCameraPreview = {
                                            selectedSensorForImage = it
                                            openCamera = true
                                        },
                                        openImage = {
                                            openImage = true
                                            openedImage = it
                                        })
                                }
                            }
                        }
                    }
                }
                AppButton(
                    modifier = Modifier.padding(bottom = 34.dp),
                    isEnabled = isNextAvailable,
                    text = R.string.next
                ) {

                    val imei = sharedViewModel.imei
                    val deviceSerial = sharedViewModel.serial

                    val imeiLog = ApiLogItem(
                        sharedViewModel.plateNum,
                        description = "رقم الهويه: " + imei,
                        type = "device_imei",
                        sharedViewModel.selectedOrder?.id?.toInt(),
                    )
                    val serialLog = ApiLogItem(
                        sharedViewModel.plateNum,
                        description = "رقم مسلسل الجهاز:  " + deviceSerial,
                        type = "device_serial",
                        sharedViewModel.selectedOrder?.id?.toInt(),
                    )


                    val status = if (sharedViewModel.isSupplied==0){
                        " تركيب الجهاز فقط"
                    }else " توريد الجهاز فقط"

                    val deviceLog = ApiLogItem(
                        sharedViewModel.plateNum,
                        description = status,
                        type = "device_supplied",
                        sharedViewModel.selectedOrder?.id?.toInt(),
                    )

                    val sensorsLogs = ArrayList<ApiLogItem>()
                    for (sensor in sharedViewModel.selectedSensors){
                        val description = if(sensor.isTawreed) " تم توريد الحساس: "
                        else " فقط تم تركيب الحساس: "

                        sensorsLogs.add(ApiLogItem(
                            sharedViewModel.plateNum,
                            description = description + sensor.name ,
                            type = "sensor",
                            sharedViewModel.selectedOrder?.id?.toInt(),
                        ))
                    }




                    val logArray = ArrayList<ApiLogItem>()
                    logArray.add(imeiLog)
                    logArray.add(serialLog)
                    logArray.add(deviceLog)
                    logArray.addAll(sensorsLogs)
                    val logModel = ApiLog(logArray)
                    vmLog.addLogs(logModel)
                    goSimEntry()
                }
            }
            if (sensorsState.loading) {
                Box(
                    contentAlignment = Alignment.Center, modifier = Modifier.matchParentSize()
                ) {
                    DialogBoxLoading()
                }
            }
            sensorsState.failure?.let {
                val content = it.getContentIfNotHandled()
                content?.let {
                    Toast.makeText(
                        context, it.localizedMessage, Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

}

@Composable
fun SensorWidget(
    modifier: Modifier,
    scaffoldState: ScaffoldState?,
    sharedViewModel: AppViewModel,
    isSelected: Boolean,
    selectedImage: Any,
    unselectedImage: Any,
    sensor: Sensor,
    openCameraPreview: (Sensor) -> Unit,
    openImage: (Bitmap) -> Unit,
) {
    var alertVisibility by rememberSaveable { (mutableStateOf(false)) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            openCameraPreview(sensor)
        } else {
            showErrorSnackbar(context, coroutineScope, scaffoldState)
        }
    }
    val selectedTint = Teal400
    val unselectedBorderTint = LightGrey
    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .toggleable(value = isSelected, enabled = true, onValueChange = { selected ->
                if (selected) {
                    sharedViewModel.selectedSensors.add(sensor)
                } else {
                    sharedViewModel.selectedSensors.remove(sensor)
                }
                sharedViewModel.updateUI()
            })
            .border(
                BorderStroke(
                    1.dp, if (isSelected) selectedTint else unselectedBorderTint
                ), RoundedCornerShape(4.dp)
            )
            .background(Color.White, RoundedCornerShape(4.dp)),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Column(
            modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val image: Painter =
                if (selectedImage is Int) painterResource(id = (if (isSelected) selectedImage else unselectedImage) as Int)
                else rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(data = if (isSelected) selectedImage else unselectedImage)
                        .decoderFactory(SvgDecoder.Factory())
                        .apply(block = fun ImageRequest.Builder.() {
                            crossfade(true)
                        }).build()
                )
            Image(
                painter = image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(16.dp)
                    .size(48.dp),
            )
            Text(
                style = MaterialTheme.typography.subtitle2,
                color = if (isSelected) Teal400 else Tundora,
                modifier = Modifier.padding(bottom = 16.dp),
                text = sensor.name,
                maxLines = 1
            )
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp),
                color = Color.Gray,
                thickness = 1.dp
            )

            var tawreed by remember { mutableStateOf(true) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = tawreed, onClick = {
                    tawreed = true
                    sensor.isTawreed = tawreed
                    sharedViewModel.selectedSensors.remove(sensor)
                    sharedViewModel.selectedSensors.add(sensor)
                })

                Text(
                    style = MaterialTheme.typography.subtitle2,
                    color = if (tawreed) SpanishGreen else Color.Gray,
                    text = stringResource(id = R.string.tawreed),
                    maxLines = 1,
                    fontSize = 14.sp
                )
                Icon(
                    Icons.Default.Check,
                    contentDescription = "",
                    tint = if (tawreed) SpanishGreen else Color.Gray
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = !tawreed, onClick = {
                    tawreed = false
                    sensor.isTawreed = tawreed
                    sharedViewModel.selectedSensors.remove(sensor)
                    sharedViewModel.selectedSensors.add(sensor)

                })

                Text(
                    style = MaterialTheme.typography.subtitle2,
                    color = if (!tawreed) DarkRed else Color.Gray,
                    text = stringResource(id = R.string.tarkeeb),
                    maxLines = 1,
                    fontSize = 14.sp
                )
                Icon(
                    Icons.Default.Close,
                    contentDescription = "",
                    tint = if (!tawreed) DarkRed else Color.Gray
                )
            }
        }
        if (sensor.needAttach) {
            Divider(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp),
                color = Color.Gray,
                thickness = 1.dp
            )
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(start = 2.dp)
                    .weight(1f)
                    .align(Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (sensor.file != null) {

                    Image(
                        modifier = Modifier
                            .size(120.dp)
                            .clickable { openImage(sensor.file!!.toBitmap()) },
                        bitmap = sensor.file!!.toBitmap().asImageBitmap(),
                        contentDescription = ""
                    )
                } else {

                    Text(
                        style = MaterialTheme.typography.subtitle2,
                        color = ChathamsBlue,
                        modifier = Modifier,
                        text = "إضافة صورة",
                        maxLines = 1
                    )
                }
                AppIconButton(
                    onSubmit = {
                        checkCameraPermissionOrOpenCam(context, {
                            launcher.launch(
                                Manifest.permission.CAMERA
                            )
                        }, {
                            alertVisibility = true
                        }, {
                            openCameraPreview(sensor)
                        })
                    },
                    // Assign reference "button" to the Button composable
                    // and constrain it to the top of the ConstraintLayout
                    modifier = Modifier, icon = Icons.Default.PhotoCamera
                )
            }
        }
    }
}


@Composable
fun SensorsHeader(modifier: Modifier) {
    Text(
        text = "بيانات الجهاز",
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 56.dp),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.subtitle1
    )
    Text(
        modifier = modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        text = "الحساسات",
        style = MaterialTheme.typography.subtitle1
    )
}

@Composable
fun MultiSelectGroup(
    sharedViewModel: AppViewModel, modifier: Modifier, sensorsList: List<Sensor>
) {
    sharedViewModel.onUpdate.value

    val currentSelections = sharedViewModel.selectedSensors
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        sensorsList.forEach { currentSensor ->
            item(span = { GridItemSpan(if (currentSensor.needAttach) 2 else 1) }) {
                val isSelected = currentSelections.contains(currentSensor)
                OrderTypeWidget(
                    modifier = Modifier.fillMaxWidth(),
                    isSelected,
                    currentSensor.id.toString(),
                    BASE_URL + "storage/" + currentSensor.path,
                    BASE_URL + "storage/" + currentSensor.path,
                    currentSensor.name
                ) { clicked ->
                    val clickedSense = sensorsList.find { it.id.toString() == clicked }!!
                    if (isSelected) {
                        currentSelections.remove(clickedSense)
                    } else {
                        currentSelections.add(clickedSense)
                    }
                    sharedViewModel.updateUI()
                }
            }
        }
    }

}

@Preview(locale = "ar")
@Composable
fun DeviceDetailsScreenPreview() {
    DeviceDetailsScreen()
}