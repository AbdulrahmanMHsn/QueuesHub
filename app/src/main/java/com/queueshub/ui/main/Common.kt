package com.queueshub.ui.main

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.airbnb.lottie.compose.*
import com.queueshub.R
import com.queueshub.ui.theme.LightGrey
import com.queueshub.ui.theme.Teal400


@Composable
fun AppButton(
    modifier: Modifier, @StringRes text: Int, isEnabled: Boolean = true, onSubmit: () -> Unit
) {

    Button(
        modifier = modifier
            .padding(horizontal = 48.dp)
            .fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(backgroundColor = Teal400),
        shape = RoundedCornerShape(24.dp),
        onClick = { onSubmit() },
        enabled = isEnabled
    ) {
        Text(text = stringResource(id = text))
    }
}

@Composable
fun AppIconButton(
    modifier: Modifier, icon: ImageVector, isEnabled: Boolean = true, onSubmit: () -> Unit
) {

    Button(
        modifier = modifier.padding(horizontal = 48.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = Teal400),
        shape = RoundedCornerShape(24.dp),
        onClick = { onSubmit() },
        enabled = isEnabled
    ) {
        Icon(icon, contentDescription = "", tint = Color.White)
    }
}

@Composable
fun InputField(
    text: String,
    keyboardType: KeyboardType,
    content: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Done,
    isEnabled: Boolean = true,
    readOnly: Boolean = !isEnabled,
    submitButton: (String) -> Unit = {},
) {
    Column(
        modifier = modifier, horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = text, style = MaterialTheme.typography.body1
        )
        Spacer(
            modifier = Modifier.height(height = 4.dp)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(modifier = Modifier.fillMaxWidth(),
                value = content,
                onValueChange = onValueChange,
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = if (isEnabled) Color.White else LightGrey,
                    focusedBorderColor = Teal400,
                    unfocusedBorderColor = LightGrey,
                    cursorColor = Teal400,
                ),
                textStyle = MaterialTheme.typography.body1,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType, imeAction = imeAction
                ),
                readOnly = readOnly,
                enabled = isEnabled,
                keyboardActions = KeyboardActions(onGo = { submitButton(content) })
            )
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AppDropdownMenu(
    modifier: Modifier,
    value: String,
    enabled: Boolean = true,
    @StringRes label: Int,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier, horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = stringResource(id = label), style = MaterialTheme.typography.body1
        )
        Spacer(
            modifier = Modifier.height(height = 4.dp)
        )
        ExposedDropdownMenuBox(modifier = Modifier.fillMaxWidth(),
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            }) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                value = value,
                enabled = enabled,
                onValueChange = { },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expanded
                    )
                },
                textStyle = MaterialTheme.typography.body1,
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = Color.White,
                    focusedBorderColor = Teal400,
                    unfocusedBorderColor = LightGrey,
                    cursorColor = Teal400
                ),
            )
            ExposedDropdownMenu(modifier = Modifier.fillMaxWidth(),
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                }) {
                if (enabled)
                    options.forEach { selectionOption ->
                        DropdownMenuItem(onClick = {
                            onSelect(selectionOption)
                            expanded = false
                        }) {
                            Text(text = selectionOption)
                        }
                    }
            }
        }
    }
}


fun checkCameraPermissionOrOpenCam(
    context: Context,
    requestPermission: () -> Unit,
    showRationale: () -> Unit,
    permissionGranted: () -> Unit
) {
    when {
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED -> {
            permissionGranted()
        }
        ActivityCompat.shouldShowRequestPermissionRationale(
            context as Activity, Manifest.permission.CAMERA
        ) -> {
            showRationale()
        }
        else -> {
            requestPermission()
        }
    }
}

@Composable
fun showAlert(
    visibility: Boolean = false,
    @StringRes title: Int,
    @StringRes body: Int,
    @StringRes negative: Int,
    @StringRes positive: Int,
    onPositive: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (visibility) AlertDialog(onDismissRequest = {}, title = {
        Text(text = stringResource(id = title))
    }, text = {
        Text(stringResource(id = body))
    }, confirmButton = {
        Button(

            onClick = {
                onPositive()
            }) {
            Text(stringResource(id = positive))
        }
    }, dismissButton = {
        Button(

            onClick = {
                onDismiss()
            }) {
            Text(stringResource(id = negative))
        }
    })
}


@Composable
fun DialogBoxLoading(
    cornerRadius: Dp = 16.dp,
    paddingStart: Dp = 56.dp,
    paddingEnd: Dp = 56.dp,
    paddingTop: Dp = 32.dp,
    paddingBottom: Dp = 32.dp,
    progressIndicatorColor: Color = Color(0xFF35898f),
    progressIndicatorSize: Dp = 80.dp
) {

    Dialog(onDismissRequest = {}) {
        Surface(
            elevation = 4.dp, shape = RoundedCornerShape(cornerRadius)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 16.dp, horizontal = paddingStart),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier,
                    text = "برجاء الانتظار...",
                    style = TextStyle(
                        color = Color.Black, fontSize = 16.sp, fontFamily = FontFamily(
                            Font(R.font.vazirmatn, FontWeight.Normal)
                        )
                    )
                )
                Loader(modifier = Modifier.height(paddingStart))

                // Please wait text
            }
        }
    }
}
@Composable
fun Loader(modifier: Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading))
    val lottieProgress by animateLottieCompositionAsState( composition = composition, iterations = LottieConstants.IterateForever )
    LottieAnimation(composition,modifier=modifier,
        progress = { lottieProgress })
}
@Composable
fun ProgressIndicatorLoading(progressIndicatorSize: Dp, progressIndicatorColor: Color) {

    val infiniteTransition = rememberInfiniteTransition()

    val angle by infiniteTransition.animateFloat(initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = keyframes {
            durationMillis = 600
        })
    )

    CircularProgressIndicator(
        progress = 1f, modifier = Modifier
            .size(progressIndicatorSize)
            .rotate(angle)
            .border(
                12.dp, brush = Brush.sweepGradient(
                    listOf(
                        Color.White, // add background color first
                        progressIndicatorColor.copy(alpha = 0.1f), progressIndicatorColor
                    )
                ), shape = CircleShape
            ), strokeWidth = 1.dp, color = Color.White // Set background color
    )
}