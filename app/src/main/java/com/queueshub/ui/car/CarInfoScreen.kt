package com.queueshub.ui.car

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.sharp.Close
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import com.queueshub.BuildConfig
import com.queueshub.R
import com.queueshub.ui.AppViewModel
import com.queueshub.ui.CameraPreview
import com.queueshub.ui.MainActivity
import com.queueshub.ui.main.*
import com.queueshub.ui.navigation.Router
import com.queueshub.ui.theme.DarkRed
import com.queueshub.ui.theme.SpanishGreen
import com.queueshub.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File


@ExperimentalGetImage
@Composable
fun CarInfoScreen(
    scaffoldState: ScaffoldState? = null, router: Router? = null
) {
    val context = LocalContext.current
    val viewModel: AppViewModel = hiltViewModel(context as MainActivity)
    var plateAvailable: Int by rememberSaveable { mutableStateOf(-1) }
    var licenseAvailable: Int by rememberSaveable { mutableStateOf(-1) }
    var nextAvailable = viewModel.isNextCarInfoAvailable(plateAvailable, licenseAvailable)
    var openCamera by rememberSaveable { (mutableStateOf(false)) }
    var openImage by rememberSaveable { (mutableStateOf(false)) }
    var openedImage: Bitmap? by rememberSaveable { mutableStateOf(null) }
    var showLoading by rememberSaveable { (mutableStateOf(false)) }
    var cameraType by rememberSaveable { (mutableStateOf(CameraType.CAR_PLATE)) }
    var plateImage: File? by rememberSaveable { (mutableStateOf(null)) }
    var licenseImage: File? by rememberSaveable { (mutableStateOf(null)) }
    var licenseImage2: File? by rememberSaveable { (mutableStateOf(null)) }

    val goManualPlate: () -> Unit = {
        router?.goManualPlate()
    }
    val goManualLicense: () -> Unit = {
        router?.goManualLicense()
    }
    val goConfirmation: () -> Unit = {
        router?.goInfoConfirmation()
    }
    BackHandler(openCamera || openImage) {
        openCamera = false
        openImage = false
    }
    if (openCamera) {
        Log.e("camera", "opening camera")
        CameraPreview(cameraType = cameraType, onImageCaptured = {
            showLoading = true
        }, onTryAgain = {}) { string, image ->
            showLoading = false
            Log.e("camera", "opening camera ${string}")
            if (cameraType == CameraType.CAR_PLATE) {
                Log.e("camera", "finished camera")
                plateImage = image
                openCamera = false
                viewModel.plateImage = image
                viewModel.plateText = string
            } else if (cameraType == CameraType.CAR_LICENSE) {
                licenseImage = image
                cameraType = CameraType.CAR_LICENSE2
                viewModel.licenseImage = image
                viewModel.licenseText = string
            } else if (cameraType == CameraType.CAR_LICENSE2) {
                licenseImage2 = image
                viewModel.licenseImage2 = image
                openCamera = false
                viewModel.licenseText2 = string
            }
            nextAvailable = viewModel.isNextCarInfoAvailable(plateAvailable, licenseAvailable)
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
    } else {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            val (title, subtitle, plate, license, next) = createRefs()
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
                },
                text = "التقط صور لوحة العربية ورخصة العربية",
                style = MaterialTheme.typography.subtitle2
            )

            CarInfoCardContent(
                Modifier
                    .constrainAs(plate) {
                        top.linkTo(subtitle.bottom, margin = 30.dp)
                    }
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                plateAvailable,
                scaffoldState,
                R.string.car_plate,
                R.drawable.car_plate,
                openCameraPreview = {
                    viewModel.plateInfoAuto = true
                    cameraType = CameraType.CAR_PLATE
                    openCamera = true
                },
                negativeText = R.string.not_available,
                imageBitmap = plateImage?.toBitmap(),
                isAvailable = {
                    viewModel.plateInfoAuto = it
                    cameraType = CameraType.CAR_PLATE
                    plateAvailable = if (it) 1 else 0
                    nextAvailable =
                        viewModel.isNextCarInfoAvailable(plateAvailable, licenseAvailable)
                }, openImage = {
                    openImage = true
                    openedImage = it
                })
            CarInfoCardContent(
                Modifier
                    .constrainAs(license) {
                        top.linkTo(plate.bottom, margin = 24.dp)
                    }
                    .padding(horizontal = 16.dp),
                licenseAvailable,
                scaffoldState,
                R.string.car_license,
                R.drawable.car_license,

                isEnabled = plateAvailable == 1,
                openCameraPreview = {
                    viewModel.licenseAuto = true
                    cameraType = CameraType.CAR_LICENSE
                    openCamera = true
                },
                negativeText = R.string.mashoba,
                imageBitmap = licenseImage?.toBitmap(),
                imageBitmap2 = licenseImage2?.toBitmap(),
                isAvailable = {
                    viewModel.licenseAuto = it
                    licenseAvailable = if (it) 1 else 0
                    nextAvailable =
                        viewModel.isNextCarInfoAvailable(plateAvailable, licenseAvailable)
                }, openImage = {
                    openImage = true
                    openedImage = it
                })
            AppButton(modifier = Modifier
                .constrainAs(next) {
                    bottom.linkTo(parent.bottom, margin = 24.dp)
                    top.linkTo(license.bottom, margin = 24.dp)
                }
                .padding(bottom = 34.dp), text = R.string.next, isEnabled = nextAvailable) {
                if (plateAvailable == 0) {
                    goManualPlate()
                } else if (licenseAvailable == 0) {
                    goManualLicense()
                } else {
                    goConfirmation()
                }
            }
        }
    }
}

@Composable
fun CarInfoCardContent(
    modifier: Modifier,
    availability: Int,
    scaffoldState: ScaffoldState?,
    @StringRes title: Int,
    @DrawableRes image: Int,
    imageBitmap: Bitmap?,
    isEnabled: Boolean = true,
    imageBitmap2: Bitmap? = null,
    @StringRes negativeText: Int,
    isAvailable: (Boolean) -> Unit,
    openCameraPreview: (Boolean) -> Unit,
    openImage: (Bitmap) -> Unit
) {
    var alertVisibility by rememberSaveable { (mutableStateOf(false)) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            openCameraPreview(true)
        } else {
            showErrorSnackbar(context, coroutineScope, scaffoldState)
        }
    }
    ConstraintLayout(
        modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (imageBitmap != null) SpanishGreen else Color.White,
                RoundedCornerShape(4.dp)
            )
            .background(Color.White, RoundedCornerShape(4.dp))
    ) {
        // Create references for the composables to constrain
        val (text, done, available, notAvailable, pic, button) = createRefs()
        if (imageBitmap != null)
            Image(modifier = Modifier.constrainAs(done) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
            }, painter = painterResource(id = R.drawable.group_26), contentDescription = "")
        Text(
            text = stringResource(id = title),
            modifier = Modifier.constrainAs(text) {
                top.linkTo(parent.top, margin = 24.dp)
                end.linkTo(parent.end, margin = 24.dp)
                start.linkTo(parent.start, margin = 24.dp)
            },
        )
        val horizontalChain =
            createHorizontalChain(notAvailable, available, chainStyle = ChainStyle.Packed)

        isAvailableGroup(
            modifier = Modifier
                .constrainAs(notAvailable) {
                    top.linkTo(text.bottom, margin = 16.dp)
                    end.linkTo(parent.end, margin = 16.dp)
                    start.linkTo(notAvailable.end, margin = 12.dp)
                }
                .padding(horizontal = 12.dp),
            availability == 0,
            false,
            DarkRed,
            Icons.Default.Clear,
            negativeText,
            isEnabled
        ) {
            isAvailable(it)
        }

        isAvailableGroup(
            modifier = Modifier
                .constrainAs(available) {
                    top.linkTo(text.bottom, margin = 16.dp)
                    start.linkTo(parent.start, margin = 16.dp)
                    end.linkTo(notAvailable.start, margin = 12.dp)
                }
                .padding(horizontal = 12.dp),
            availability == 1,
            true,
            SpanishGreen,
            Icons.Default.Check,
            R.string.available,
            isEnabled
        ) {
            isAvailable(it)
        }
        Column(modifier = Modifier.constrainAs(pic) {
            top.linkTo(available.bottom, margin = 32.dp)
            end.linkTo(parent.end, margin = 32.dp)
            start.linkTo(parent.start, margin = 32.dp)
        }) {
            if (imageBitmap != null) {

                Image(
                    modifier = Modifier.clickable { openImage(imageBitmap) },
                    bitmap = imageBitmap.asImageBitmap(),
                    contentDescription = "some useful description",
                )
            } else {
                val image: Painter = painterResource(id = image)

                Image(
                    painter = image,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
            }
            if (imageBitmap2 != null) {
                Image(
                    modifier = Modifier.clickable { openImage(imageBitmap2) },
                    bitmap = imageBitmap2.asImageBitmap(),
                    contentDescription = "some useful description",
                )
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
                openCameraPreview(true)
                openCamera() {

                }
            })
        },
            // Assign reference "button" to the Button composable
            // and constrain it to the top of the ConstraintLayout
            modifier = Modifier
                .constrainAs(button) {
                    top.linkTo(pic.bottom, margin = 24.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .padding(horizontal = 40.dp)
                .padding(bottom = 12.dp),
            isEnabled = availability == 1,
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

fun showErrorSnackbar(
    context: Context, coroutineScope: CoroutineScope, scaffoldState: ScaffoldState?
) {

    coroutineScope.launch { // using the `coroutineScope` to `launch` showing the snackbar
        // taking the `snackbarHostState` from the attached `scaffoldState`
        val snackbarResult = scaffoldState?.snackbarHostState?.showSnackbar(
            message = "This is your message", actionLabel = "Do something."
        )
        when (snackbarResult) {
            SnackbarResult.Dismissed -> Logger.d("Dismissed")
            SnackbarResult.ActionPerformed -> {
                context.startActivity(Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }

            else -> {}
        }
    }
}

fun openCamera(
    imageUploaded: () -> Unit
) {
    imageUploaded()
}

@Composable
fun isAvailableGroup(
    modifier: Modifier,
    isSelected: Boolean,
    type: Boolean,
    color: Color,
    icon: ImageVector,
    @StringRes text: Int,
    enabled: Boolean = true,
    fontSize: TextUnit = 16.sp,
    isAvailable: (Boolean) -> Unit,
) {
    Row(
        modifier = modifier
            .toggleable(value = isSelected, enabled = enabled, onValueChange = { selected ->
                if (selected) {
                    isAvailable(type)
                }
            })
            .border(1.dp, if (enabled) color else Color.Gray, RoundedCornerShape(4.dp))
            .background(if (isSelected) color else Color.White, shape = RoundedCornerShape(4.dp))
            .padding(vertical = 7.dp, horizontal = 16.dp)
    ) {
        Text(
            style = MaterialTheme.typography.subtitle2,
            color = if (enabled) if (isSelected) Color.White else color else Color.Gray,
            text = stringResource(id = text),
            maxLines = 1,
            fontSize = fontSize
        )
        Icon(
            icon,
            contentDescription = "",
            tint = if (enabled) if (isSelected) Color.White else color else Color.Gray
        )
    }
}

@Preview(locale = "ar")
@ExperimentalGetImage
@Composable
fun CarInfoScreenPreview() {
    CarInfoScreen()
}

@Preview(locale = "ar")
@Composable
fun isAvailableGroupPreview() {
    isAvailableGroup(modifier = Modifier.background(Color(0xFFE9E9E9)),
        false,
        true,
        Color.Green,
        Icons.Default.Check,
        R.string.available,
        true,
        fontSize = 16.sp,
        {})
    isAvailableGroup(modifier = Modifier.background(Color(0xFFE9E9E9)),
        false,
        false,
        Color.Red,
        Icons.Default.Clear,
        R.string.available,
        false,
        fontSize = 16.sp,
        {})
}