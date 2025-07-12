package com.queueshub.ui.device

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Close
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import com.queueshub.R
import com.queueshub.data.api.model.ApiLog
import com.queueshub.data.api.model.ApiLogItem
import com.queueshub.domain.model.Maintenance
import com.queueshub.ui.AppViewModel
import com.queueshub.ui.CameraPreview
import com.queueshub.ui.MainActivity
import com.queueshub.ui.car.LogsViewModel
import com.queueshub.ui.main.AppButton
import com.queueshub.ui.main.DialogBoxLoading
import com.queueshub.ui.main.InputField
import com.queueshub.ui.navigation.Router
import com.queueshub.ui.theme.LightGrey
import com.queueshub.ui.theme.Teal400
import com.queueshub.ui.theme.Tundora
import com.queueshub.utils.CameraType
import com.queueshub.utils.toBitmap
import java.io.File

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun DeviceRepairScreen(
    paddingValues: PaddingValues = PaddingValues(),
    router: Router? = null,
    scaffoldState: ScaffoldState? = null
) {
    val goNote: () -> Unit = {
        router?.goOrderNote()
    }

    val context = LocalContext.current
    val sharedViewModel: AppViewModel = hiltViewModel(context as MainActivity)
    val vmLog: LogsViewModel = hiltViewModel()

    val uiState by sharedViewModel.state.collectAsState()

    if (uiState.loading) {
        Box(
            contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()
        ) {
            DialogBoxLoading()
        }
    }
    val viewModel: DeviceViewModel = hiltViewModel()
    val maintenanceState by viewModel.maintenanceState.collectAsState()
    var openCamera by rememberSaveable { (mutableStateOf(false)) }
    var showLoading by rememberSaveable { (mutableStateOf(false)) }
    var cameraType by rememberSaveable { (mutableStateOf(CameraType.MAINTENANCE)) }
    var nextAvailable = sharedViewModel.selectedMaintenances.size >0
    sharedViewModel.onUpdate.value
    var maintenanceImage: File? by rememberSaveable { (mutableStateOf(sharedViewModel.maintenanceFile)) }

    var openImage by rememberSaveable { (mutableStateOf(false)) }
    var openedImage: Bitmap? by rememberSaveable { mutableStateOf(null)}
    LaunchedEffect(0) {
        viewModel.fetchMaintenance()
    }

    BackHandler(openCamera||openImage) {
        openCamera = false
        openImage = false
    }
    if (openCamera) {
        CameraPreview(cameraType = cameraType, onImageCaptured = {
            showLoading = true
        }, onTryAgain = {}) { string, image ->
            showLoading = false
            openCamera = false
            maintenanceImage = image
            sharedViewModel.maintenanceFile = image
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
            IconButton(
                modifier = Modifier
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
    }else {
        maintenanceState.maintenances?.let { maintenancesList ->
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {


                val currentSelections = sharedViewModel.selectedMaintenances
                val (title, repairList, subtitle, pic, next) = createRefs()
                Text(
                    text = "ما تم تنفيذه", modifier = Modifier
                        .padding(top = 56.dp)
                        .constrainAs(title) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }, style = MaterialTheme.typography.subtitle1
                )

                Column(modifier = Modifier
                    .constrainAs(repairList) {
                        top.linkTo(title.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .padding(vertical = 19.dp, horizontal = 16.dp)) {
                    maintenancesList.forEachIndexed { index, maintenance ->
                        val selectedMaintenance = currentSelections.find { it.id == maintenance.id }
                        val maintenanceToDisplay = selectedMaintenance?: maintenance
                        val isSelected = selectedMaintenance != null
                        var main by remember {
                            mutableStateOf(maintenanceToDisplay.copy())
                        }

                        SelectableWidget(modifier = Modifier.padding(vertical = 8.dp),
                            isSelected = isSelected,
                            id = index.toLong(),
                            maintenance = main,
                            onToggleChange = {
                                if (isSelected) {
                                    currentSelections.remove(main)
                                } else {
                                    currentSelections.add(main)
                                }
                                sharedViewModel.updateUI()
                            },
                            onTextChanged = {

                                currentSelections.remove(main)
                                currentSelections.add(it)
                                main = it
                                sharedViewModel.updateUI()
                            })
                    }
                }

                Text(modifier = Modifier
                    .constrainAs(subtitle) {
                        top.linkTo(repairList.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .padding(vertical = 19.dp, horizontal = 16.dp),
                    text = "اضف صورة",
                    style = MaterialTheme.typography.subtitle1)

                DeviceCardContent(
                    Modifier
                        .constrainAs(pic) {
                            top.linkTo(subtitle.bottom, margin = 30.dp)
                        }
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 24.dp),
                    scaffoldState,
                    R.string.repair_image,
                    deviceImage = maintenanceImage?.toBitmap(),
                    imageUploaded = {
                        cameraType = CameraType.MAINTENANCE
                        openCamera = true
                    },
                    openImage = {
                        openImage = true
                        openedImage = it
                    }
                )
                AppButton(
                    modifier = Modifier.constrainAs(next) {
                        top.linkTo(pic.bottom, margin = 24.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }, text = R.string.done, isEnabled = nextAvailable
                ) {


                    val orderType = ApiLogItem(
                        sharedViewModel.plateNum,
                        description = "(الاختيار داخل الغرض صيانه الجهاز) :",
                        type = "inside_type",
                        sharedViewModel.selectedOrder?.id?.toInt(),
                        generatedId =sharedViewModel.generatedId,
                    )

                    val logArray = ArrayList<ApiLogItem>()

                    for (item in sharedViewModel.selectedMaintenances){
                        logArray.add(ApiLogItem(
                            sharedViewModel.plateNum,
                            description = item.name ,
                            type = "inside_type",
                            sharedViewModel.selectedOrder?.id?.toInt(),
                            generatedId =sharedViewModel.generatedId,
                        ))
                    }
                    logArray.add(orderType)
                    val logModel = ApiLog(logArray)
                    vmLog.addLogs(logModel)

                    sharedViewModel.saveOrderType(arrayListOf("صيانة"))
                    goNote()
                }
            }
        }
        if (maintenanceState.loading) {
            Box(
                contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()
            ) {
                DialogBoxLoading()
            }
        }
        maintenanceState.failure?.let {
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
fun SelectableWidget(
    modifier: Modifier,
    isSelected: Boolean,
    id: Long,
    maintenance: Maintenance,
    onToggleChange: (Maintenance) -> Unit,
    onTextChanged: (Maintenance) -> Unit
) {
    val selectedTint = Teal400
    val unselectedBorderTint = LightGrey
    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(value = isSelected, enabled = true, onValueChange = { selected ->
                if (selected) {
                    onToggleChange(maintenance)
                }
            })
            .border(
                BorderStroke(
                    1.dp, if (isSelected) selectedTint else unselectedBorderTint
                ), RoundedCornerShape(4.dp)
            )
            .background(Color.White), verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = isSelected, onCheckedChange = { onToggleChange(maintenance) })
        Text(
            style = MaterialTheme.typography.subtitle2,
            color = if (isSelected) Teal400 else Tundora,
            modifier = Modifier.padding(vertical = 8.dp),
            text = maintenance.name,
            maxLines = 1
        )
        if (maintenance.needsDescription) {

            InputField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 13.dp,vertical = 8.dp),
                text = "",
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
                content = maintenance.description,
                onValueChange = {
                    onTextChanged(maintenance.copy(description = it))
                },
                isEnabled = isSelected
            ) {
            }

        }

    }
}

@Preview(locale = "ar", showBackground = true)
@Composable
fun DeviceRepairScreenPreview() {
    // Mock data for preview
    val mockMaintenance = Maintenance(
        id = 1,
        name = "صيانة GPS",
        description = "إصلاح مشاكل GPS",
        needsDescription = true
    )
    
    SelectableWidget(
        modifier = Modifier.padding(8.dp),
        isSelected = true,
        id = 1L,
        maintenance = mockMaintenance,
        onToggleChange = {},
        onTextChanged = {}
    )
}