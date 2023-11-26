package com.queueshub.ui.sim

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import com.queueshub.R
import com.queueshub.ui.*
import com.queueshub.ui.car.showErrorSnackbar
import com.queueshub.ui.device.DeviceCardContent
import com.queueshub.ui.main.*
import com.queueshub.ui.navigation.Router
import com.queueshub.ui.theme.SpanishGreen
import com.queueshub.utils.CameraType
import com.queueshub.utils.toBitmap
import java.io.File

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun SimEntryScreen(
    paddingValues: PaddingValues = PaddingValues(),
    router: Router? = null,
    scaffoldState: ScaffoldState? = null
) {
    val context = LocalContext.current
    val viewModel: AppViewModel = hiltViewModel(context as MainActivity)
    val goSimDetails: () -> Unit = {
        router?.goSimDetails()
    }
    var openCameraSim by remember { (mutableStateOf(false)) }
    var nextAvailable: Boolean by rememberSaveable { mutableStateOf(viewModel.simImage != null) }
    var openScanner by rememberSaveable { (mutableStateOf(false)) }
    var showLoading by rememberSaveable { (mutableStateOf(false)) }
    var showTryAgain by rememberSaveable { (mutableStateOf(false)) }
    var dataType by rememberSaveable { (mutableStateOf("scan")) }
    var cameraType by rememberSaveable { (mutableStateOf(CameraType.SIM))}
    var simImage: File? by rememberSaveable { (mutableStateOf(viewModel.simImage )) }
    var openImage by rememberSaveable { (mutableStateOf(false)) }
    var openedImage: Bitmap? by rememberSaveable { mutableStateOf(null) }

    BackHandler(openCameraSim||openScanner||openImage) {
        openCameraSim = false
        openScanner = false
        openImage = false
    }
    if (openCameraSim) {
        CameraPreview(cameraType = cameraType, onImageCaptured = {
            showLoading = true
        }, onTryAgain = {
            showLoading = false
            showTryAgain = true
        }) { string, image ->
            showLoading = false
            openCameraSim = false

            if (cameraType == CameraType.SIM) {
                simImage = image
                viewModel.simImage = image
            } else {
                if (string.isNotBlank()) {
                    try {
                        val list = string.split(",")
                        viewModel.simSerial = list[0]
                        viewModel.simGSM = list[1]
                    } catch (_: Exception) {
                    }
                }
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
    } else if (openScanner) {
        CameraSIMBarcodePreview(onUpdateIMEI = {
            viewModel.simSerial = it
        }, onUpdateGsm = {
            viewModel.simGSM = it
        }, onCloseScanner = { openScanner = false })
        if (showLoading) {

            Box(
                contentAlignment = Alignment.Center
            ) {
                DialogBoxLoading()
            }
        }
    } else {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            val (title, subtitle, notWorking, working, carType, plate, model, next) = createRefs()
            Text(
                text = "بيانات الشريحة",
                modifier = Modifier
                    .padding(top = 56.dp)
                    .constrainAs(title) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                style = MaterialTheme.typography.subtitle1
            )
            Text(
                modifier = Modifier.constrainAs(subtitle) {
                    top.linkTo(title.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }, text = "التقط صور الشريحة", style = MaterialTheme.typography.subtitle2
            )

            DeviceCardContent(
                Modifier
                    .constrainAs(plate) {
                        top.linkTo(subtitle.bottom, margin = 30.dp)
                    }
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                scaffoldState,
                R.string.sim_image,
                deviceImage = simImage?.toBitmap(),
                imageUploaded = {
                    cameraType = CameraType.SIM
                    openCameraSim = true
                    nextAvailable = true
                },
                openImage = {
                    openImage = true
                    openedImage = it
                }
            )
            Row(
                Modifier
                    .height(IntrinsicSize.Min)
                    .constrainAs(carType) {
                        top.linkTo(plate.bottom, margin = 30.dp)
                    }
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)) {

                DeviceCardContent(
                    Modifier
                        .padding(end = 4.dp)
                        .weight(1f),
                    scaffoldState,
                    R.string.scan_code,
                    image = R.drawable.barcode,
                    isEnabled = nextAvailable,
                    isDone = viewModel.simGSM != "" || viewModel.simSerial != "" && dataType == "scan",
                    deviceImage = null,
                    openImage = {
                        openImage = true
                        openedImage = it
                    },
                    imageUploaded = {
                        openScanner = true
                        dataType = "scan"
                    })

                DeviceCardContent(
                    Modifier
                        .padding(start = 4.dp)
                        .fillMaxHeight()
                        .weight(1f),
                    scaffoldState,
                    R.string.capture_code,
                    image = R.drawable.capture_code,
                    isEnabled = nextAvailable,
                    isDone = viewModel.simGSM != "" || viewModel.simSerial != "" && dataType == "capture",
                    deviceImage = null,
                    openImage = {
                        openImage = true
                        openedImage = it
                    },
                    imageUploaded = {
                        dataType = "capture"
                        cameraType = CameraType.SIM_CAPTURE
                        openCameraSim = true
                        nextAvailable = true
                    })
            }

            AppButton(
                modifier = Modifier
                    .constrainAs(next) {
                        top.linkTo(carType.bottom, margin = 24.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .padding(bottom = 34.dp),
                text = R.string.next, isEnabled = nextAvailable,
            ) {
                goSimDetails()
            }
        }
    }
}

@Composable
fun SIMCardContent(
    modifier: Modifier,
    scaffoldState: ScaffoldState?,
    @StringRes title: Int,
    @DrawableRes image: Int? = null,
    simImage: Bitmap? = null,
    openCamera: () -> Unit
) {
    var alertVisibility by rememberSaveable { (mutableStateOf(false)) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            openCamera()
        } else {
            showErrorSnackbar(context, coroutineScope, scaffoldState)
        }
    }
    ConstraintLayout(
        modifier
            .fillMaxWidth()

            .border(
                1.dp,
                if (simImage != null) SpanishGreen else Color.White,
                RoundedCornerShape(4.dp)
            )
            .background(Color.White, RoundedCornerShape(4.dp))
            .background(Color.White, RoundedCornerShape(4.dp))
    ) {
        // Create references for the composables to constrain
        val (text, done, pic, button) = createRefs()
        Text(
            text = stringResource(id = title),
            modifier = Modifier.constrainAs(text) {
                top.linkTo(parent.top, margin = 24.dp)
                end.linkTo(parent.end, margin = 24.dp)
                start.linkTo(parent.start, margin = 24.dp)
            },
        )

        if (simImage != null) {
            Image(modifier = Modifier.constrainAs(done) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
            }, painter = painterResource(id = R.drawable.group_26), contentDescription = "")

            Image(
                modifier = Modifier.constrainAs(pic) {
                    top.linkTo(text.bottom, margin = 32.dp)
                    end.linkTo(parent.end, margin = 32.dp)
                    start.linkTo(parent.start, margin = 32.dp)
                },
                bitmap = simImage.asImageBitmap(),
                contentDescription = "some useful description",
            )
        } else {
            image?.let { res ->
                val painter: Painter = painterResource(id = res)

                Image(painter = painter,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.constrainAs(pic) {
                        top.linkTo(text.bottom, margin = 32.dp)
                        end.linkTo(parent.end, margin = 32.dp)
                        start.linkTo(parent.start, margin = 32.dp)
                    })
            }
        }

        AppIconButton(onSubmit = {
            checkCameraPermissionOrOpenCam(context, {
                launcher.launch(
                    Manifest.permission.CAMERA
                )
            }, {
                alertVisibility = true
            }, {
                openCamera()
            })
        },
            // Assign reference "button" to the Button composable
            // and constrain it to the top of the ConstraintLayout
            modifier = Modifier
                .constrainAs(button) {
                    top.linkTo(pic.bottom, margin = 24.dp, goneMargin = 24.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .padding(horizontal = 40.dp)
                .padding(bottom = 12.dp),
            icon = Icons.Default.PhotoCamera)
    }

    showAlert(alertVisibility,
        R.string.camera_permission_disclosure,
        R.string.camera_permission_desc,
        R.string.cancel,
        R.string.ok,
        {
            alertVisibility = false
            launcher.launch(
                Manifest.permission.CAMERA
            )

        },
        {
            alertVisibility = false
            showErrorSnackbar(context, coroutineScope, scaffoldState)
        })
}

@Preview(locale = "ar")
@Composable
fun SimEntryScreenPreview() {
    SimEntryScreen()
}